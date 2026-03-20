import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const PACKAGE_NAME = "com.horsegallop"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

interface VerifyPurchaseRequest {
  purchaseToken: string
  productId: string
}

interface ServiceAccountKey {
  client_email: string
  private_key: string
}

interface GooglePlaySubscriptionResponse {
  paymentState?: number
  expiryTimeMillis?: string
  cancelReason?: number
  [key: string]: unknown
}

// Base64url encode a Uint8Array without padding
function base64urlEncode(data: Uint8Array): string {
  const base64 = btoa(String.fromCharCode(...data))
  return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "")
}

// Encode a plain string as base64url
function base64urlEncodeString(str: string): string {
  const encoder = new TextEncoder()
  return base64urlEncode(encoder.encode(str))
}

// Convert PEM private key string to CryptoKey for RS256 signing
async function importRsaPrivateKey(pem: string): Promise<CryptoKey> {
  const pemContents = pem
    .replace(/-----BEGIN PRIVATE KEY-----/g, "")
    .replace(/-----END PRIVATE KEY-----/g, "")
    .replace(/\s+/g, "")

  const binaryDer = Uint8Array.from(atob(pemContents), (c) => c.charCodeAt(0))

  return await crypto.subtle.importKey(
    "pkcs8",
    binaryDer.buffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  )
}

// Build and sign a JWT for Google OAuth2 service account authentication
async function buildServiceAccountJwt(serviceAccount: ServiceAccountKey): Promise<string> {
  const now = Math.floor(Date.now() / 1000)

  const header = { alg: "RS256", typ: "JWT" }
  const payload = {
    iss: serviceAccount.client_email,
    scope: "https://www.googleapis.com/auth/androidpublisher",
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now,
  }

  const encodedHeader = base64urlEncodeString(JSON.stringify(header))
  const encodedPayload = base64urlEncodeString(JSON.stringify(payload))
  const signingInput = `${encodedHeader}.${encodedPayload}`

  const privateKey = await importRsaPrivateKey(serviceAccount.private_key)
  const encoder = new TextEncoder()
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    privateKey,
    encoder.encode(signingInput)
  )

  const encodedSignature = base64urlEncode(new Uint8Array(signature))
  return `${signingInput}.${encodedSignature}`
}

// Exchange a signed JWT for a Google OAuth2 access token
async function fetchAccessToken(jwt: string): Promise<string> {
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  })

  if (!response.ok) {
    const errText = await response.text()
    throw new Error(`OAuth2 token exchange failed: ${response.status} — ${errText}`)
  }

  const data = await response.json()
  if (!data.access_token) {
    throw new Error("OAuth2 response did not contain access_token")
  }
  return data.access_token as string
}

// Query the Google Play Developer API for a subscription purchase
async function fetchPlaySubscription(
  accessToken: string,
  productId: string,
  purchaseToken: string
): Promise<GooglePlaySubscriptionResponse> {
  const url =
    `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/` +
    `${PACKAGE_NAME}/purchases/subscriptions/${productId}/tokens/${purchaseToken}`

  const response = await fetch(url, {
    headers: { Authorization: `Bearer ${accessToken}` },
  })

  if (!response.ok) {
    const errText = await response.text()
    throw new Error(`Google Play API error: ${response.status} — ${errText}`)
  }

  return (await response.json()) as GooglePlaySubscriptionResponse
}

// Map a Google Play product ID to a subscription tier string
function productIdToTier(productId: string): string {
  switch (productId) {
    case "horsegallop_pro_monthly":
      return "PRO_MONTHLY"
    case "horsegallop_pro_yearly":
      return "PRO_YEARLY"
    default:
      return "FREE"
  }
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  try {
    // 1. JWT doğrulama — Authorization header'dan kullanıcıyı belirle
    const authHeader = req.headers.get("Authorization")
    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: "Missing authorization header", verified: false }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      )
    }

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    )

    const { data: { user }, error: authError } = await supabase.auth.getUser(
      authHeader.replace("Bearer ", "")
    )

    if (authError || !user) {
      return new Response(
        JSON.stringify({ error: "Unauthorized", verified: false }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      )
    }

    // 2. userId JWT'den geliyor — body'de artık userId yok
    const userId = user.id

    // 3. Parse request body (userId artık body'de yok)
    const body: VerifyPurchaseRequest = await req.json()
    const { purchaseToken, productId } = body

    if (!purchaseToken || !productId) {
      return new Response(
        JSON.stringify({ error: "purchaseToken and productId are required", verified: false }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      )
    }

    // 4. Load and parse service account key
    const serviceAccountKeyRaw = Deno.env.get("GOOGLE_PLAY_SERVICE_ACCOUNT_KEY")
    if (!serviceAccountKeyRaw) {
      throw new Error("GOOGLE_PLAY_SERVICE_ACCOUNT_KEY environment variable is not set")
    }
    const serviceAccount: ServiceAccountKey = JSON.parse(serviceAccountKeyRaw)

    // 5. Authenticate with Google via manual JWT
    const jwt = await buildServiceAccountJwt(serviceAccount)
    const accessToken = await fetchAccessToken(jwt)

    // 6. Query Google Play Developer API
    const subscription = await fetchPlaySubscription(accessToken, productId, purchaseToken)

    // 7. Validate purchase
    const paymentState = subscription.paymentState
    const expiryTimeMillis = subscription.expiryTimeMillis
      ? parseInt(subscription.expiryTimeMillis, 10)
      : 0

    const isPaymentReceived = paymentState === 1
    const isNotExpired = expiryTimeMillis > Date.now()
    const isVerified = isPaymentReceived && isNotExpired

    if (!isVerified) {
      return new Response(
        JSON.stringify({ verified: false, tier: "FREE", expiresAt: null }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      )
    }

    // 8. Update user_profiles — JWT'den gelen userId kullanılıyor
    const expiryDate = new Date(expiryTimeMillis)
    const tier = productIdToTier(productId)

    const { error: updateError } = await supabase
      .from("user_profiles")
      .update({
        is_pro: true,
        subscription_tier: tier,
        subscription_expires_at: expiryDate.toISOString(),
      })
      .eq("id", userId)

    if (updateError) {
      throw new Error(`Failed to update user_profiles: ${updateError.message}`)
    }

    return new Response(
      JSON.stringify({ verified: true, tier, expiresAt: expiryDate.toISOString() }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    )
  } catch (error) {
    return new Response(
      JSON.stringify({ error: (error as Error).message, verified: false }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    )
  }
})
