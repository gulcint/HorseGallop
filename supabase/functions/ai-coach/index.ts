import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const GROQ_API_KEY = Deno.env.get("GROQ_API_KEY") ?? ""
const GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

interface AiCoachRequest {
  message: string
  userId: string
  conversationHistory?: Array<{ role: string; content: string }>
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  try {
    const { message, userId, conversationHistory = [] }: AiCoachRequest = await req.json()

    const systemPrompt =
      "Sen HorseGallop'un Türkçe at binme koçusun. Binicilik, at bakımı, antrenman planları ve yarış takibi konularında uzman ve samimi bir danışmansın. Kullanıcıya kısa, pratik ve motive edici cevaplar ver. Türkçe konuş."

    const messages = [
      { role: "system", content: systemPrompt },
      ...conversationHistory.map((m) => ({
        role: m.role === "user" ? "user" : "assistant",
        content: m.content,
      })),
      { role: "user", content: message },
    ]

    const groqResponse = await fetch(GROQ_API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: "llama-3.1-8b-instant",
        messages,
        temperature: 0.7,
        max_tokens: 512,
      }),
    })

    if (!groqResponse.ok) {
      const err = await groqResponse.text()
      throw new Error(`Groq API error: ${groqResponse.status} — ${err}`)
    }

    const groqData = await groqResponse.json()
    const reply =
      groqData.choices?.[0]?.message?.content ??
      "Şu anda cevap veremiyorum, lütfen tekrar dene."

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    )

    await supabase.from("ai_coach_messages").insert([
      { user_id: userId, role: "user", content: message },
      { user_id: userId, role: "assistant", content: reply },
    ])

    return new Response(JSON.stringify({ reply, success: true }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    })
  } catch (error) {
    return new Response(
      JSON.stringify({ error: (error as Error).message, success: false }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    )
  }
})
