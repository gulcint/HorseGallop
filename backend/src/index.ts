import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import type { BarnDto, BarnInstructorDto, BarnReviewDto, BarnListDto, LessonDto, LessonListDto, HorseTipDto, HorseTipListDto, BreedDto, BreedListDto } from "./contracts";
import { buildHomeDashboard } from "./home-service";
import {
  parseLimit,
  parseOptionalDate,
  parseOptionalNumber,
  parseOptionalString,
  parseRequiredId,
} from "./validators";
import fetch from "node-fetch";
import * as cheerio from "cheerio";

admin.initializeApp();

const db = admin.firestore();
const FEDERATION_SOURCE_STALE_MINUTES = 24 * 60;

type UserProfileDto = {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  city: string;
  birthDate: string;
  photoUrl: string | null;
  countryCode: string;
  weight: number | null;
};

type UpdateUserProfileInput = {
  firstName?: unknown;
  lastName?: unknown;
  phone?: unknown;
  city?: unknown;
  birthDate?: unknown;
  countryCode?: unknown;
  weight?: unknown;
};

function normalizeString(value: unknown, maxLen: number): string {
  if (typeof value !== "string") return "";
  return value.trim().slice(0, maxLen);
}

function parseBirthDateToTimestamp(value: unknown): admin.firestore.Timestamp | null {
  if (typeof value !== "string" || value.trim().length === 0) {
    return null;
  }

  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value.trim());
  if (!match) {
    throw new HttpsError("invalid-argument", "birthDate must be yyyy-MM-dd");
  }

  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);

  const date = new Date(Date.UTC(year, month - 1, day));
  if (Number.isNaN(date.getTime())) {
    throw new HttpsError("invalid-argument", "birthDate is invalid");
  }

  return admin.firestore.Timestamp.fromDate(date);
}

function formatBirthDateFromFirestore(value: unknown): string {
  if (!value) return "";

  if (value instanceof admin.firestore.Timestamp) {
    const date = value.toDate();
    const year = date.getUTCFullYear();
    const month = String(date.getUTCMonth() + 1).padStart(2, "0");
    const day = String(date.getUTCDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  if (typeof value === "number") {
    const date = new Date(value);
    if (!Number.isNaN(date.getTime())) {
      const year = date.getUTCFullYear();
      const month = String(date.getUTCMonth() + 1).padStart(2, "0");
      const day = String(date.getUTCDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    }
  }

  if (typeof value === "string") {
    return value;
  }

  return "";
}

function parseWeight(value: unknown): number | null {
  if (value === null || value === undefined || value === "") return null;
  if (typeof value !== "number" || Number.isNaN(value)) {
    throw new HttpsError("invalid-argument", "weight must be a number");
  }
  if (value < 0 || value > 500) {
    throw new HttpsError("invalid-argument", "weight out of range");
  }
  return value;
}

function parsePhone(value: unknown): string {
  if (value === null || value === undefined) return "";
  if (typeof value !== "string") {
    throw new HttpsError("invalid-argument", "phone must be a string");
  }
  const cleaned = value.trim();
  if (cleaned.length > 15 || !/^\d*$/.test(cleaned)) {
    throw new HttpsError("invalid-argument", "phone must contain only digits and max 15 chars");
  }
  return cleaned;
}

function buildDto(snapshotData: FirebaseFirestore.DocumentData, fallbackEmail: string): UserProfileDto {
  const weightValue = snapshotData.weight;
  const weight = typeof weightValue === "number" ? weightValue : null;

  return {
    firstName: normalizeString(snapshotData.firstName, 60),
    lastName: normalizeString(snapshotData.lastName, 60),
    email: normalizeString(snapshotData.email, 120) || fallbackEmail,
    phone: normalizeString(snapshotData.phone, 15),
    city: normalizeString(snapshotData.city, 80),
    birthDate: formatBirthDateFromFirestore(snapshotData.birthDate),
    photoUrl: typeof snapshotData.photoUrl === "string" ? snapshotData.photoUrl : null,
    countryCode: normalizeString(snapshotData.countryCode, 8) || "+90",
    weight,
  };
}

function parseDateMs(value: unknown): number {
  if (value instanceof admin.firestore.Timestamp) {
    return value.toMillis();
  }
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }
  if (typeof value === "string") {
    const ms = Date.parse(value);
    return Number.isNaN(ms) ? 0 : ms;
  }
  if (
    typeof value === "object" &&
    value !== null &&
    "seconds" in value &&
    typeof (value as { seconds: unknown }).seconds === "number"
  ) {
    return (value as { seconds: number }).seconds * 1000;
  }
  return 0;
}

function normalizeBarnDto(
  id: string,
  data: FirebaseFirestore.DocumentData,
  instructors: BarnInstructorDto[] = [],
  reviews: BarnReviewDto[] = []
): BarnDto {
  return {
    id,
    name: normalizeString(data.name, 120),
    description: normalizeString(data.description, 500),
    location: normalizeString(data.location, 180),
    lat: typeof data.lat === "number" ? data.lat : 0,
    lng: typeof data.lng === "number" ? data.lng : 0,
    tags: Array.isArray(data.tags) ? data.tags.filter((x): x is string => typeof x === "string") : [],
    amenities: Array.isArray(data.amenities)
      ? data.amenities.filter((x): x is string => typeof x === "string")
      : [],
    rating: typeof data.rating === "number" ? data.rating : 0,
    reviewCount: typeof data.reviewCount === "number" ? data.reviewCount : 0,
    heroImageUrl: typeof data.heroImageUrl === "string" ? data.heroImageUrl : undefined,
    capacity: typeof data.capacity === "number" ? data.capacity : undefined,
    phone: typeof data.phone === "string" ? data.phone : undefined,
    instructors,
    reviews,
  };
}

function normalizeLessonDto(id: string, data: FirebaseFirestore.DocumentData): LessonDto {
  return {
    id,
    date: normalizeString(data.date, 40),
    title: normalizeString(data.title, 160),
    instructorName: normalizeString(data.instructorName, 120),
    durationMin: typeof data.durationMin === "number" ? data.durationMin : 0,
    level: normalizeString(data.level, 40),
    price: typeof data.price === "number" ? data.price : 0,
  };
}

const FEDERATION_BASE_URL = "https://www.binicilik.org.tr";
const SCRAPE_CACHE_COLLECTION = "scrape_cache";
const GEOCODE_CACHE_COLLECTION = "geocode_cache";
const FEDERATED_BARNS_SYNC_STATUS_KEY = "federated_barns_sync_status";
const FEDERATION_MANUAL_SYNC_STATUS_KEY = "federation_manual_sync_status";
const FEDERATED_BARNS_CACHE_KEY = "federated_barns";
const EQUESTRIAN_ANNOUNCEMENTS_CACHE_KEY = "equestrian_announcements";
const EQUESTRIAN_COMPETITIONS_CACHE_KEY = "equestrian_competitions";
const FEDERATION_HEALTH_BARNS_KEY = "federation_health_barns";
const FEDERATION_HEALTH_ANNOUNCEMENTS_KEY = "federation_health_announcements";
const FEDERATION_HEALTH_COMPETITIONS_KEY = "federation_health_competitions";
const MANUAL_SYNC_MIN_INTERVAL_MS = 5 * 60 * 1000;
const FALLBACK_BARNS: BarnDto[] = [
  {
    id: "barn_adin_country",
    name: "Adin Country",
    description: "Beginner to pro riding lessons with indoor arena support.",
    location: "Istanbul, TR",
    lat: 41.0082,
    lng: 28.9784,
    tags: ["cafe", "indoor_arena", "parking", "lessons", "open_now"],
    amenities: ["cafe", "indoor_arena", "parking", "lessons", "open_now"],
    rating: 4.7,
    reviewCount: 124,
    instructors: [],
    reviews: [],
  },
  {
    id: "barn_sable_ranch",
    name: "Sable Ranch",
    description: "Trail and endurance focused training with boarding support.",
    location: "Sariyer, Istanbul, TR",
    lat: 41.0151,
    lng: 29.0037,
    tags: ["outdoor_arena", "trail", "parking", "boarding"],
    amenities: ["outdoor_arena", "trail", "parking", "boarding"],
    rating: 4.5,
    reviewCount: 89,
    instructors: [],
    reviews: [],
  },
];

type EquestrianAnnouncementDto = {
  id: string;
  title: string;
  summary: string;
  publishedAtLabel: string;
  detailUrl: string;
  imageUrl?: string;
};

type EquestrianCompetitionDto = {
  id: string;
  title: string;
  location: string;
  dateLabel: string;
  detailUrl: string;
};

type CoordinatesDto = {
  lat: number;
  lng: number;
};

type NotificationWriteDto = {
  type: "general" | "reservation" | "lesson" | "horse_health";
  title: string;
  body: string;
  targetId?: string;
  targetRoute?: string;
  notificationKey?: string;
};

type FederatedBarnSyncStatusDto = {
  status: string;
  syncedAt: string;
  itemCount: number;
  errorMessage?: string;
};

type FederationSourceHealthItemDto = {
  source: "barns" | "announcements" | "competitions";
  status: string;
  itemCount: number;
  lastAttemptAt: string;
  lastSuccessAt: string;
  dataAgeMinutes: number;
  isStale: boolean;
  errorMessage?: string;
};

type FederationManualSyncDto = {
  syncedAt: string;
  barnsCount: number;
  announcementsCount: number;
  competitionsCount: number;
  throttled: boolean;
};

const TURKEY_CITY_COORDS: Record<string, CoordinatesDto> = {
  "İSTANBUL": { lat: 41.0082, lng: 28.9784 },
  "ANKARA": { lat: 39.9334, lng: 32.8597 },
  "İZMİR": { lat: 38.4237, lng: 27.1428 },
  "BURSA": { lat: 40.1885, lng: 29.0610 },
  "ADANA": { lat: 37.0000, lng: 35.3213 },
  "KOCAELİ": { lat: 40.7654, lng: 29.9408 },
  "ANTALYA": { lat: 36.8969, lng: 30.7133 },
  "KONYA": { lat: 37.8746, lng: 32.4932 },
  "KAYSERİ": { lat: 38.7205, lng: 35.4826 },
  "ÇORUM": { lat: 40.5506, lng: 34.9556 },
  "ESKİŞEHİR": { lat: 39.7667, lng: 30.5256 },
  "GAZİANTEP": { lat: 37.0662, lng: 37.3833 },
  "DÜZCE": { lat: 40.8438, lng: 31.1565 },
  "SAMSUN": { lat: 41.2867, lng: 36.3300 },
  "NEVŞEHİR": { lat: 38.6244, lng: 34.7240 },
  "ŞANLIURFA": { lat: 37.1674, lng: 38.7955 },
};

function toAbsoluteUrl(url: string): string {
  if (!url) return "";
  if (/^https?:\/\//i.test(url)) return url;
  return new URL(url, `${FEDERATION_BASE_URL}/`).toString();
}

function normalizeHtmlText(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

function sanitizeNotificationKey(value: string): string {
  return value.replace(/[^a-zA-Z0-9_-]+/g, "_").slice(0, 120);
}

function horseHealthTypeLabel(type: string): string {
  switch (type) {
  case "FARRIER":
    return "nalbant";
  case "VACCINATION":
    return "asi";
  case "DENTAL":
    return "dis bakimi";
  case "VET":
    return "veteriner kontrolu";
  case "DEWORMING":
    return "parazit uygulamasi";
  default:
    return "saglik takibi";
  }
}

function currentIstanbulDateKey(offsetDays = 0): string {
  const now = new Date();
  now.setUTCDate(now.getUTCDate() + offsetDays);
  return new Intl.DateTimeFormat("sv-SE", {
    timeZone: "Europe/Istanbul",
  }).format(now);
}

function isWithinUpcomingReminderWindow(date: string): boolean {
  const today = currentIstanbulDateKey(0);
  const tomorrow = currentIstanbulDateKey(1);
  return date >= today && date <= tomorrow;
}

async function writeUserNotification(uid: string, payload: NotificationWriteDto): Promise<string> {
  const notificationsRef = db.collection("users").doc(uid).collection("notifications");
  const notificationKey = payload.notificationKey ? sanitizeNotificationKey(payload.notificationKey) : "";
  const ref = notificationKey ? notificationsRef.doc(notificationKey) : notificationsRef.doc();

  await ref.set({
    type: payload.type,
    title: normalizeString(payload.title, 160),
    body: normalizeString(payload.body, 500),
    timestamp: Date.now(),
    isRead: false,
    targetId: payload.targetId ? normalizeString(payload.targetId, 120) : null,
    targetRoute: payload.targetRoute ? normalizeString(payload.targetRoute, 160) : null,
    notificationKey: notificationKey || null,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });

  return ref.id;
}

async function maybeWriteHorseHealthReminder(
  uid: string,
  horseId: string,
  eventId: string,
  horseName: string,
  type: string,
  date: string
): Promise<void> {
  if (!isWithinUpcomingReminderWindow(date)) return;

  const typeLabel = horseHealthTypeLabel(type);
  await writeUserNotification(uid, {
    type: "horse_health",
    title: "Yaklasan saglik randevusu",
    body: `${horseName || "Atin"} icin ${typeLabel} ${date} tarihinde planlandi.`,
    targetId: horseId,
    targetRoute: `horseHealth/${horseId}/${encodeURIComponent(horseName || "Saglik")}`,
    notificationKey: `horse_health_${uid}_${horseId}_${eventId}_${date}`,
  });
}

async function fetchFederationHtml(pathOrUrl: string): Promise<string> {
  const url = /^https?:\/\//i.test(pathOrUrl) ? pathOrUrl : toAbsoluteUrl(pathOrUrl);
  const response = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36",
      "Accept": "text/html,application/xhtml+xml",
      "Accept-Language": "tr-TR,tr;q=0.9,en;q=0.8",
      "Referer": FEDERATION_BASE_URL,
    },
  } as Record<string, unknown>);

  if (!response.ok) {
    throw new HttpsError("unavailable", `Federation returned HTTP ${response.status}`);
  }

  return response.text();
}

async function readScrapeCache<T>(key: string): Promise<T | null> {
  const doc = await db.collection(SCRAPE_CACHE_COLLECTION).doc(key).get();
  if (!doc.exists) return null;
  const data = doc.data();
  return (data?.payload as T | undefined) ?? null;
}

async function writeScrapeCache<T>(key: string, payload: T): Promise<void> {
  await db.collection(SCRAPE_CACHE_COLLECTION).doc(key).set({
    payload,
    fetchedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });
}

function inferCityFromAddress(address: string): string {
  const normalized = normalizeHtmlText(address);
  if (!normalized) return "";
  const slashParts = normalized.split("/").map((x) => x.trim()).filter(Boolean);
  if (slashParts.length > 0) {
    return slashParts[slashParts.length - 1];
  }
  const words = normalized.split(/\s+/).filter(Boolean);
  return words.length > 0 ? words[words.length - 1] : "";
}

function normalizeCityKey(value: string): string {
  return normalizeHtmlText(value).toLocaleUpperCase("tr-TR");
}

async function resolveFederatedBarnCoordinates(
  barnId: string,
  address: string
): Promise<CoordinatesDto> {
  if (!address) return { lat: 0, lng: 0 };

  const cacheRef = db.collection(GEOCODE_CACHE_COLLECTION).doc(`federated_barn_${barnId}`);
  const cacheDoc = await cacheRef.get();
  if (cacheDoc.exists) {
    const data = cacheDoc.data();
    if (data?.address === address && typeof data.lat === "number" && typeof data.lng === "number") {
      return { lat: data.lat, lng: data.lng };
    }
  }

  const cityKey = normalizeCityKey(inferCityFromAddress(address));
  const fallbackCoords = TURKEY_CITY_COORDS[cityKey];

  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&countrycodes=tr&q=${encodeURIComponent(address)}`,
      {
        headers: {
          "User-Agent": "HorseGallop/1.0 (federated-barns-geocode)",
          "Accept-Language": "tr-TR,tr;q=0.9,en;q=0.8",
        },
      } as Record<string, unknown>
    );

    if (response.ok) {
      const results = await response.json() as Array<{ lat?: string; lon?: string }>;
      const lat = Number(results[0]?.lat);
      const lng = Number(results[0]?.lon);
      if (Number.isFinite(lat) && Number.isFinite(lng)) {
        const coords = { lat, lng };
        await cacheRef.set({
          address,
          ...coords,
          source: "nominatim",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, { merge: true });
        return coords;
      }
    }
  } catch (_error) {
    // Fall through to city-level fallback.
  }

  if (fallbackCoords) {
    await cacheRef.set({
      address,
      ...fallbackCoords,
      source: "city_fallback",
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, { merge: true });
    return fallbackCoords;
  }

  return { lat: 0, lng: 0 };
}

async function mapWithConcurrency<T, R>(
  items: T[],
  concurrency: number,
  worker: (item: T, index: number) => Promise<R>
): Promise<R[]> {
  const results: R[] = new Array(items.length);
  let cursor = 0;

  const runners = Array.from({ length: Math.max(1, concurrency) }, async () => {
    while (cursor < items.length) {
      const currentIndex = cursor++;
      results[currentIndex] = await worker(items[currentIndex], currentIndex);
    }
  });

  await Promise.all(runners);
  return results;
}

function buildBarnDescription(address: string, email: string, website: string): string {
  return [
    address && `Adres: ${address}`,
    email && `E-posta: ${email}`,
    website && `Web: ${website}`,
  ].filter(Boolean).join(" • ");
}

async function scrapeFederatedBarns(): Promise<BarnDto[]> {
  const html = await fetchFederationHtml("/Kulup/11/Federe-kulupler");
  const $ = cheerio.load(html);
  const modals = $("div.modal[id^='federedetail']").toArray();

  const barns = await mapWithConcurrency(modals, 3, async (modal) => {
    const $modal = $(modal);
    const id = ($modal.attr("id") || "").replace("federedetail", "").trim();
    if (!id) {
      return {
        id: "",
        name: "",
        description: "",
        location: "",
        lat: 0,
        lng: 0,
        tags: [],
        amenities: [],
        rating: 0,
        reviewCount: 0,
        instructors: [],
        reviews: [],
      } satisfies BarnDto;
    }

    const title = normalizeHtmlText($modal.find(".modal-title").first().text());
    const imageUrl = toAbsoluteUrl($modal.find("img").first().attr("src") || "");

    const fields: Record<string, string> = {};
    $modal.find("table tr").each((_rowIndex, row) => {
      const cells = $(row).find("td");
      const key = normalizeHtmlText(cells.eq(0).text()).toLowerCase();
      const rawValueCell = cells.eq(2);
      let value = normalizeHtmlText(rawValueCell.text());
      if (key.includes("web")) {
        const link = rawValueCell.find("a").attr("href") || "";
        value = normalizeHtmlText(link || value);
      }
      if (key) {
        fields[key] = value;
      }
    });

    const address = fields["adres"] || "";
    const email = fields["e posta"] || "";
    const website = fields["web sitesi"] || "";
    const phone = fields["telefon"] || "";
    const city = inferCityFromAddress(address);
    const coords = await resolveFederatedBarnCoordinates(id, address);

    return {
      id,
      name: title,
      description: buildBarnDescription(address, email, website),
      location: city || address,
      lat: coords.lat,
      lng: coords.lng,
      tags: [],
      amenities: [],
      rating: 0,
      reviewCount: 0,
      heroImageUrl: imageUrl || undefined,
      capacity: 0,
      phone: phone || undefined,
      instructors: [],
      reviews: [],
    };
  });

  return barns
    .filter((item) => item.id.trim().length > 0)
    .sort((a, b) => a.name.localeCompare(b.name, "tr"));
}

async function getFederatedBarnsPayload(): Promise<BarnListDto> {
  const cached = await readScrapeCache<BarnListDto>(FEDERATED_BARNS_CACHE_KEY);
  if (cached?.items?.length) return cached;
  return { items: FALLBACK_BARNS };
}

async function getFederatedBarnDetailPayload(id: string): Promise<BarnDto> {
  const cached = await readScrapeCache<BarnListDto>(FEDERATED_BARNS_CACHE_KEY);
  const barn = cached?.items.find((item) => item.id === id);
  if (barn) return barn;

  const fallbackBarn = FALLBACK_BARNS.find((item) => item.id === id);
  if (fallbackBarn) return fallbackBarn;

  throw new HttpsError("not-found", "Barn not found");
}

async function syncFederatedBarnDirectory(): Promise<BarnListDto> {
  const items = await scrapeFederatedBarns();
  const payload = { items };
  await writeScrapeCache(FEDERATED_BARNS_CACHE_KEY, payload);
  await db.collection(SCRAPE_CACHE_COLLECTION).doc(FEDERATED_BARNS_SYNC_STATUS_KEY).set({
    status: "success",
    itemCount: items.length,
    syncedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });
  await writeFederationSourceHealth(FEDERATION_HEALTH_BARNS_KEY, {
    status: "success",
    itemCount: items.length,
    errorMessage: admin.firestore.FieldValue.delete(),
    lastAttemptAt: admin.firestore.FieldValue.serverTimestamp(),
    lastSuccessAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return payload;
}

async function getFederatedBarnSyncStatusPayload(): Promise<FederatedBarnSyncStatusDto> {
  const doc = await db.collection(SCRAPE_CACHE_COLLECTION).doc(FEDERATED_BARNS_SYNC_STATUS_KEY).get();
  const data = doc.data() || {};
  const syncedAtValue = data.syncedAt;
  let syncedAt = "";
  if (syncedAtValue instanceof admin.firestore.Timestamp) {
    syncedAt = syncedAtValue.toDate().toISOString();
  } else if (typeof syncedAtValue === "string") {
    syncedAt = syncedAtValue;
  }

  return {
    status: typeof data.status === "string" ? data.status : "idle",
    syncedAt,
    itemCount: typeof data.itemCount === "number" ? data.itemCount : 0,
    errorMessage: typeof data.errorMessage === "string" ? data.errorMessage : undefined,
  };
}

async function scrapeEquestrianAnnouncements(): Promise<EquestrianAnnouncementDto[]> {
  const html = await fetchFederationHtml("/Anasayfa/Duyuruarsiv");
  const $ = cheerio.load(html);

  return $("article.post.haber").map((_index, article) => {
    const $article = $(article);
    const linkEl = $article.find("h4.entry-title a").first();
    const title = normalizeHtmlText(linkEl.text());
    const detailUrl = toAbsoluteUrl(linkEl.attr("href") || "");
    const summary = normalizeHtmlText($article.find("p.mt-5").first().clone().find("a").remove().end().text());
    const imageUrl = toAbsoluteUrl($article.find(".post-thumb img").first().attr("src") || "");
    const day = normalizeHtmlText($article.find(".entry-date .day").text());
    const month = normalizeHtmlText($article.find(".entry-date .month").text());
    const year = normalizeHtmlText($article.find(".entry-date .year").text());
    const publishedAtLabel = normalizeHtmlText([day, month, year].filter(Boolean).join(" "));
    const idMatch = detailUrl.match(/\/Duyuru\/(\d+)\//i);
    const id = idMatch?.[1] || title;

    return {
      id,
      title,
      summary,
      publishedAtLabel,
      detailUrl,
      imageUrl: imageUrl || undefined,
    };
  }).get();
}

async function scrapeEquestrianCompetitions(): Promise<EquestrianCompetitionDto[]> {
  const html = await fetchFederationHtml("/Anasayfa/Yarislar");
  const $ = cheerio.load(html);

  return $("table.table-telefon-rehberi tbody tr").map((_index, row) => {
    const cells = $(row).find("td");
    const titleLink = cells.eq(0).find("a").first();
    const title = normalizeHtmlText(titleLink.text());
    if (!title) return null;
    const detailUrl = toAbsoluteUrl(titleLink.attr("href") || "");
    const dateLabel = normalizeHtmlText(cells.eq(1).text());
    const location = normalizeHtmlText(cells.eq(2).text());
    const idMatch = detailUrl.match(/\/Yarisma\/(\d+)\//i);
    const id = idMatch?.[1] || title;

    return {
      id,
      title,
      location,
      dateLabel,
      detailUrl,
    };
  }).get().filter((item): item is EquestrianCompetitionDto => item !== null);
}

async function syncEquestrianAgendaCache(): Promise<void> {
  const attemptedAt = admin.firestore.FieldValue.serverTimestamp();
  const announcementAttemptIso = new Date().toISOString();
  const competitionAttemptIso = announcementAttemptIso;
  const [announcementsResult, competitionsResult] = await Promise.allSettled([
    scrapeEquestrianAnnouncements(),
    scrapeEquestrianCompetitions(),
  ]);

  if (announcementsResult.status === "fulfilled") {
    await writeScrapeCache(EQUESTRIAN_ANNOUNCEMENTS_CACHE_KEY, { items: announcementsResult.value });
    await writeFederationSourceHealth(FEDERATION_HEALTH_ANNOUNCEMENTS_KEY, {
      status: "success",
      itemCount: announcementsResult.value.length,
      errorMessage: admin.firestore.FieldValue.delete(),
      lastAttemptAt: attemptedAt,
      lastSuccessAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } else {
    await writeFederationSourceHealth(FEDERATION_HEALTH_ANNOUNCEMENTS_KEY, {
      status: "error",
      itemCount: 0,
      errorMessage: announcementsResult.reason instanceof Error ? announcementsResult.reason.message : "Unknown scrape error",
      lastAttemptAt: announcementAttemptIso,
    });
  }

  if (competitionsResult.status === "fulfilled") {
    await writeScrapeCache(EQUESTRIAN_COMPETITIONS_CACHE_KEY, { items: competitionsResult.value });
    await writeFederationSourceHealth(FEDERATION_HEALTH_COMPETITIONS_KEY, {
      status: "success",
      itemCount: competitionsResult.value.length,
      errorMessage: admin.firestore.FieldValue.delete(),
      lastAttemptAt: attemptedAt,
      lastSuccessAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } else {
    await writeFederationSourceHealth(FEDERATION_HEALTH_COMPETITIONS_KEY, {
      status: "error",
      itemCount: 0,
      errorMessage: competitionsResult.reason instanceof Error ? competitionsResult.reason.message : "Unknown scrape error",
      lastAttemptAt: competitionAttemptIso,
    });
  }

  if (announcementsResult.status === "rejected" || competitionsResult.status === "rejected") {
    throw new HttpsError("unavailable", "One or more federation sources could not be refreshed");
  }
}

function timestampToIso(value: unknown): string {
  if (value instanceof admin.firestore.Timestamp) {
    return value.toDate().toISOString();
  }
  return typeof value === "string" ? value : "";
}

async function triggerManualFederationSync(): Promise<FederationManualSyncDto> {
  return triggerFederationSyncInternal(false);
}

async function triggerFederationDebugSyncInternal(): Promise<FederationManualSyncDto> {
  return triggerFederationSyncInternal(true);
}

async function triggerFederationSyncInternal(force: boolean): Promise<FederationManualSyncDto> {
  const statusRef = db.collection(SCRAPE_CACHE_COLLECTION).doc(FEDERATION_MANUAL_SYNC_STATUS_KEY);
  const statusDoc = await statusRef.get();
  const statusData = statusDoc.data() || {};
  const lastTriggered = statusData.syncedAt instanceof admin.firestore.Timestamp
    ? statusData.syncedAt.toMillis()
    : 0;

  if (!force && lastTriggered && Date.now() - lastTriggered < MANUAL_SYNC_MIN_INTERVAL_MS) {
    return {
      syncedAt: timestampToIso(statusData.syncedAt),
      barnsCount: typeof statusData.barnsCount === "number" ? statusData.barnsCount : 0,
      announcementsCount: typeof statusData.announcementsCount === "number" ? statusData.announcementsCount : 0,
      competitionsCount: typeof statusData.competitionsCount === "number" ? statusData.competitionsCount : 0,
      throttled: true,
    };
  }

  const [barnsPayload, announcements, competitions] = await Promise.all([
    syncFederatedBarnDirectory(),
    scrapeEquestrianAnnouncements(),
    scrapeEquestrianCompetitions(),
  ]);

  await Promise.all([
    writeScrapeCache(EQUESTRIAN_ANNOUNCEMENTS_CACHE_KEY, { items: announcements }),
    writeScrapeCache(EQUESTRIAN_COMPETITIONS_CACHE_KEY, { items: competitions }),
    writeFederationSourceHealth(FEDERATION_HEALTH_ANNOUNCEMENTS_KEY, {
      status: "success",
      itemCount: announcements.length,
      errorMessage: admin.firestore.FieldValue.delete(),
      lastAttemptAt: admin.firestore.FieldValue.serverTimestamp(),
      lastSuccessAt: admin.firestore.FieldValue.serverTimestamp(),
    }),
    writeFederationSourceHealth(FEDERATION_HEALTH_COMPETITIONS_KEY, {
      status: "success",
      itemCount: competitions.length,
      errorMessage: admin.firestore.FieldValue.delete(),
      lastAttemptAt: admin.firestore.FieldValue.serverTimestamp(),
      lastSuccessAt: admin.firestore.FieldValue.serverTimestamp(),
    }),
  ]);

  await statusRef.set({
    syncedAt: admin.firestore.FieldValue.serverTimestamp(),
    barnsCount: barnsPayload.items.length,
    announcementsCount: announcements.length,
    competitionsCount: competitions.length,
  }, { merge: true });

  const refreshed = await statusRef.get();
  const refreshedData = refreshed.data() || {};
  return {
    syncedAt: timestampToIso(refreshedData.syncedAt),
    barnsCount: typeof refreshedData.barnsCount === "number" ? refreshedData.barnsCount : barnsPayload.items.length,
    announcementsCount: typeof refreshedData.announcementsCount === "number" ? refreshedData.announcementsCount : announcements.length,
    competitionsCount: typeof refreshedData.competitionsCount === "number" ? refreshedData.competitionsCount : competitions.length,
    throttled: false,
  };
}

async function writeFederationSourceHealth(
  key: string,
  payload: FirebaseFirestore.UpdateData<FirebaseFirestore.DocumentData>
): Promise<void> {
  await db.collection(SCRAPE_CACHE_COLLECTION).doc(key).set(payload, { merge: true });
}

const timestampOrStringToIso = timestampToIso;

function calculateDataAgeMinutes(lastSuccessAt: string): number {
  if (!lastSuccessAt) return -1;
  const millis = Date.parse(lastSuccessAt);
  if (Number.isNaN(millis)) return -1;
  return Math.max(0, Math.floor((Date.now() - millis) / 60000));
}

function resolveFederationSourceStatus(status: string, dataAgeMinutes: number): { status: string; isStale: boolean } {
  const isStale = status === "success" && dataAgeMinutes >= FEDERATION_SOURCE_STALE_MINUTES;
  return {
    status: isStale ? "stale" : status,
    isStale,
  };
}

async function getFederationSourceHealthPayload(): Promise<FederationSourceHealthItemDto[]> {
  const keys: Array<{ source: FederationSourceHealthItemDto["source"]; key: string }> = [
    { source: "barns", key: FEDERATION_HEALTH_BARNS_KEY },
    { source: "announcements", key: FEDERATION_HEALTH_ANNOUNCEMENTS_KEY },
    { source: "competitions", key: FEDERATION_HEALTH_COMPETITIONS_KEY },
  ];

  const docs = await Promise.all(
    keys.map(async ({ source, key }) => {
      const snapshot = await db.collection(SCRAPE_CACHE_COLLECTION).doc(key).get();
      const data = snapshot.data() || {};
      const lastAttemptAt = timestampOrStringToIso(data.lastAttemptAt);
      const lastSuccessAt = timestampOrStringToIso(data.lastSuccessAt);
      const rawStatus = typeof data.status === "string" ? data.status : "idle";
      const dataAgeMinutes = calculateDataAgeMinutes(lastSuccessAt);
      const resolvedStatus = resolveFederationSourceStatus(rawStatus, dataAgeMinutes);
      return {
        source,
        status: resolvedStatus.status,
        itemCount: typeof data.itemCount === "number" ? data.itemCount : 0,
        lastAttemptAt,
        lastSuccessAt,
        dataAgeMinutes,
        isStale: resolvedStatus.isStale,
        errorMessage: typeof data.errorMessage === "string" ? data.errorMessage : undefined,
      } satisfies FederationSourceHealthItemDto;
    })
  );

  return docs;
}

export const getUserProfile = onCall({ region: "us-central1" }, async (request) => {
  const auth = request.auth;
  if (!auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const uid = auth.uid;
  const userRecord = await admin.auth().getUser(uid);
  const docRef = db.collection("users").doc(uid);
  const doc = await docRef.get();

  if (!doc.exists) {
    const displayNameParts = (userRecord.displayName || "").trim().split(/\s+/).filter(Boolean);
    const firstName = displayNameParts[0] || "";
    const lastName = displayNameParts.slice(1).join(" ");

    const initData: FirebaseFirestore.DocumentData = {
      firstName,
      lastName,
      email: userRecord.email || "",
      phone: "",
      city: "",
      birthDate: null,
      photoUrl: userRecord.photoURL || null,
      countryCode: "+90",
      weight: null,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await docRef.set(initData, { merge: true });
    return buildDto(initData, userRecord.email || "");
  }

  return buildDto(doc.data() || {}, userRecord.email || "");
});

export const updateUserProfile = onCall({ region: "us-central1" }, async (request) => {
  const auth = request.auth;
  if (!auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const payload = (request.data || {}) as UpdateUserProfileInput;

  const updates: FirebaseFirestore.DocumentData = {
    firstName: normalizeString(payload.firstName, 60),
    lastName: normalizeString(payload.lastName, 60),
    phone: parsePhone(payload.phone),
    city: normalizeString(payload.city, 80),
    countryCode: normalizeString(payload.countryCode, 8) || "+90",
    weight: parseWeight(payload.weight),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  const birthDateTimestamp = parseBirthDateToTimestamp(payload.birthDate);
  updates.birthDate = birthDateTimestamp;

  await db.collection("users").doc(auth.uid).set(updates, { merge: true });

  return { success: true };
});

export const getHomeDashboard = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const limit = parseLimit((request.data || {}).limit, 6, 20);
  const ridesSnap = await db
    .collection("rides")
    .where("uid", "==", request.auth.uid)
    .orderBy("startedAt", "desc")
    .limit(200)
    .get();

  const rides = ridesSnap.docs.map((doc) => {
    const data = doc.data();
    const durationMinRaw = typeof data.durationMin === "number"
      ? data.durationMin
      : (typeof data.durationSec === "number" ? data.durationSec / 60 : 0);

    return {
      id: doc.id,
      distanceKm: typeof data.distanceKm === "number" ? data.distanceKm : 0,
      durationMin: durationMinRaw,
      calories: typeof data.calories === "number" ? data.calories : 0,
      barnName: normalizeString(data.barnName, 120),
      startedAtMs: parseDateMs(data.startedAt || data.createdAt || data.dateMillis),
    };
  });

  return buildHomeDashboard(rides, limit);
});

export const getBarns = onCall({ region: "us-central1" }, async (request): Promise<BarnListDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const data = request.data || {};
  parseOptionalNumber((data as { lat?: unknown }).lat, "lat");
  parseOptionalNumber((data as { lng?: unknown }).lng, "lng");
  parseOptionalNumber((data as { radiusKm?: unknown }).radiusKm, "radiusKm");
  return getFederatedBarnsPayload();
});

export const getBarnDetail = onCall({ region: "us-central1" }, async (request): Promise<BarnDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const id = parseRequiredId((request.data || {}).id, "id");
  return getFederatedBarnDetailPayload(id);
});

export const getFederatedBarns = onCall({ region: "us-central1" }, async (request): Promise<BarnListDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }
  return getFederatedBarnsPayload();
});

export const getFederatedBarnDetail = onCall({ region: "us-central1" }, async (request): Promise<BarnDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }
  const id = parseRequiredId((request.data || {}).id, "id");
  return getFederatedBarnDetailPayload(id);
});

export const syncFederatedBarns = onSchedule(
  {
    region: "us-central1",
    schedule: "every day 04:00",
    timeZone: "Europe/Istanbul",
    retryCount: 1,
  },
  async () => {
    try {
      await syncFederatedBarnDirectory();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown sync error";
      await db.collection(SCRAPE_CACHE_COLLECTION).doc(FEDERATED_BARNS_SYNC_STATUS_KEY).set({
        status: "error",
        errorMessage,
        syncedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });
      throw error;
    }
  }
);

export const syncEquestrianAgenda = onSchedule(
  {
    region: "us-central1",
    schedule: "every day 04:15",
    timeZone: "Europe/Istanbul",
    retryCount: 1,
  },
  async () => {
    await syncEquestrianAgendaCache();
  }
);

export const getFederatedBarnsSyncStatus = onCall(
  { region: "us-central1" },
  async (request): Promise<FederatedBarnSyncStatusDto> => {
    if (!request.auth?.uid) {
      throw new HttpsError("unauthenticated", "Authentication required");
    }
    return getFederatedBarnSyncStatusPayload();
  }
);

export const getFederationSourceHealth = onCall(
  { region: "us-central1" },
  async (request): Promise<{ items: FederationSourceHealthItemDto[] }> => {
    if (!request.auth?.uid) {
      throw new HttpsError("unauthenticated", "Authentication required");
    }
    return { items: await getFederationSourceHealthPayload() };
  }
);

export const triggerFederationDebugSync = onCall(
  { region: "us-central1" },
  async (request): Promise<FederationManualSyncDto> => {
    if (!request.auth?.uid) {
      throw new HttpsError("unauthenticated", "Authentication required");
    }

    return triggerFederationDebugSyncInternal();
  }
);

export const triggerFederationManualSync = onCall(
  { region: "us-central1" },
  async (request): Promise<FederationManualSyncDto> => {
    if (!request.auth?.uid) {
      throw new HttpsError("unauthenticated", "Authentication required");
    }
    return triggerManualFederationSync();
  }
);

export const getLessons = onCall({ region: "us-central1" }, async (request): Promise<LessonListDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const fromDate = parseOptionalDate((request.data || {}).from, "from");
  const toDate = parseOptionalDate((request.data || {}).to, "to");

  const snapshot = await db.collection("lessons").get();
  let items = snapshot.docs.map((doc) => normalizeLessonDto(doc.id, doc.data()));

  if (fromDate) {
    items = items.filter((item) => item.date.slice(0, 10) >= fromDate);
  }
  if (toDate) {
    items = items.filter((item) => item.date.slice(0, 10) <= toDate);
  }

  items.sort((a, b) => a.date.localeCompare(b.date));
  return { items };
});

export const getHorseTips = onCall({ region: "us-central1" }, async (request): Promise<HorseTipListDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const locale = parseOptionalString((request.data || {}).locale, 10) || "en";

  const normalizeHorseTip = (id: string, data: FirebaseFirestore.DocumentData): HorseTipDto => ({
    id,
    title: normalizeString(data.title, 200),
    body: normalizeString(data.body, 600),
    category: normalizeString(data.category, 50),
    locale: normalizeString(data.locale, 10),
  });

  let snapshot = await db.collection("horse_tips").where("locale", "==", locale).get();
  if (snapshot.empty && locale !== "en") {
    snapshot = await db.collection("horse_tips").where("locale", "==", "en").get();
  }

  const items = snapshot.docs.map((doc) => normalizeHorseTip(doc.id, doc.data()));
  return { items };
});

export const getBreeds = onCall({ region: "us-central1" }, async (request): Promise<BreedListDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const snapshot = await db.collection("horse_breeds").orderBy("sortOrder").get();
  const items: BreedDto[] = snapshot.docs.map((doc) => {
    const data = doc.data();
    return {
      id: doc.id,
      nameEn: normalizeString(data.nameEn, 80),
      nameTr: normalizeString(data.nameTr, 80),
      sortOrder: typeof data.sortOrder === "number" ? data.sortOrder : 99,
    };
  });

  return { items };
});

export const getAppContent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const locale = parseOptionalString((request.data || {}).locale, 10) || "tr";
  const doc = await db.collection("app_content").doc(locale).get();
  if (!doc.exists) {
    return {
      locale,
      home: null,
      barn: null,
      common: null,
      auth: null,
      onboarding: null,
      ride: null,
      settings: null,
    };
  }

  return {
    locale,
    ...(doc.data() || {}),
  };
});

// ─── Reservation Functions ─────────────────────────────────────────────────

export const bookLesson = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const lessonId = parseRequiredId((request.data || {}).lessonId, "lessonId");

  const lessonDoc = await db.collection("lessons").doc(lessonId).get();
  if (!lessonDoc.exists) throw new HttpsError("not-found", "Lesson not found");
  const lesson = lessonDoc.data()!;

  const existing = await db.collection("reservations")
    .where("userId", "==", uid)
    .where("lessonId", "==", lessonId)
    .where("status", "!=", "cancelled")
    .limit(1)
    .get();
  if (!existing.empty) throw new HttpsError("already-exists", "Already booked this lesson");

  const ref = db.collection("reservations").doc();
  const lessonTitle = normalizeString(lesson.title, 160);
  const lessonDate = normalizeString(lesson.date, 40);
  const instructorName = normalizeString(lesson.instructorName, 120);

  await ref.set({
    userId: uid, lessonId, lessonTitle, lessonDate, instructorName,
    status: "confirmed",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await writeUserNotification(uid, {
    type: "reservation",
    title: "Rezervasyon olusturuldu",
    body: `${lessonTitle} dersi ${lessonDate} icin onaylandi.`,
    targetId: ref.id,
    targetRoute: "myReservations",
    notificationKey: `reservation_confirmed_${ref.id}`,
  });

  return { id: ref.id, lessonId, lessonTitle, lessonDate, instructorName, status: "confirmed", createdAt: new Date().toISOString() };
});

export const cancelReservation = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const reservationId = parseRequiredId((request.data || {}).reservationId, "reservationId");

  const ref = db.collection("reservations").doc(reservationId);
  const doc = await ref.get();
  if (!doc.exists) throw new HttpsError("not-found", "Reservation not found");
  if (doc.data()!.userId !== uid) throw new HttpsError("permission-denied", "Not your reservation");

  await ref.update({ status: "cancelled", updatedAt: admin.firestore.FieldValue.serverTimestamp() });
  return { ok: true };
});

export const getMyReservations = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;

  const snapshot = await db.collection("reservations")
    .where("userId", "==", uid)
    .orderBy("createdAt", "desc")
    .get();

  const items = snapshot.docs.map((doc) => {
    const d = doc.data();
    return {
      id: doc.id,
      lessonId: normalizeString(d.lessonId, 80),
      lessonTitle: normalizeString(d.lessonTitle, 160),
      lessonDate: normalizeString(d.lessonDate, 40),
      instructorName: normalizeString(d.instructorName, 120),
      status: normalizeString(d.status, 20) || "pending",
      createdAt: d.createdAt instanceof admin.firestore.Timestamp ? d.createdAt.toDate().toISOString() : "",
    };
  });

  return { items };
});

// ─── Horse Functions ───────────────────────────────────────────────────────

export const getMyHorses = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;

  const snapshot = await db.collection("horses").where("userId", "==", uid).get();
  const items = snapshot.docs.map((doc) => {
    const d = doc.data();
    return {
      id: doc.id,
      name: normalizeString(d.name, 80),
      breed: normalizeString(d.breed, 80),
      birthYear: typeof d.birthYear === "number" ? d.birthYear : 0,
      color: normalizeString(d.color, 40),
      gender: normalizeString(d.gender, 20),
      weightKg: typeof d.weightKg === "number" ? d.weightKg : 0,
      imageUrl: normalizeString(d.imageUrl, 500),
    };
  });

  return { items };
});

export const addHorse = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data || {};

  const name = parseRequiredId(data.name, "name");
  const breed = normalizeString(data.breed, 80);
  const birthYear = typeof data.birthYear === "number" ? Math.floor(data.birthYear) : 0;
  const color = normalizeString(data.color, 40);
  const gender = normalizeString(data.gender, 20);
  const weightKg = typeof data.weightKg === "number" ? Math.floor(data.weightKg) : 0;

  const ref = db.collection("horses").doc();
  await ref.set({
    userId: uid, name, breed, birthYear, color, gender, weightKg, imageUrl: "",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { id: ref.id, name, breed, birthYear, color, gender, weightKg, imageUrl: "" };
});

export const deleteHorse = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const horseId = parseRequiredId((request.data || {}).horseId, "horseId");

  const ref = db.collection("horses").doc(horseId);
  const doc = await ref.get();
  if (!doc.exists) throw new HttpsError("not-found", "Horse not found");
  if (doc.data()!.userId !== uid) throw new HttpsError("permission-denied", "Not your horse");

  await ref.delete();
  return { ok: true };
});

// ─── Review Functions ──────────────────────────────────────────────────────

export const submitReview = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data || {};

  const targetId = parseRequiredId(data.targetId, "targetId");
  const targetType = parseRequiredId(data.targetType, "targetType");
  const targetName = normalizeString(data.targetName, 160);
  const rating = typeof data.rating === "number" ? Math.min(5, Math.max(1, Math.floor(data.rating))) : 3;
  const comment = normalizeString(data.comment, 1000);

  const userDoc = await db.collection("users").doc(uid).get();
  const userData = userDoc.data() || {};
  const authorName = `${normalizeString(userData.firstName, 60)} ${normalizeString(userData.lastName, 60)}`.trim();

  const ref = db.collection("reviews").doc();
  await ref.set({
    userId: uid, targetId, targetType, targetName, rating, comment, authorName,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { id: ref.id, targetId, targetType, targetName, rating, comment, authorName, createdAt: new Date().toISOString() };
});

export const getMyReviews = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;

  const snapshot = await db.collection("reviews")
    .where("userId", "==", uid)
    .orderBy("createdAt", "desc")
    .get();

  const items = snapshot.docs.map((doc) => {
    const d = doc.data();
    return {
      id: doc.id,
      targetId: normalizeString(d.targetId, 80),
      targetType: normalizeString(d.targetType, 40),
      targetName: normalizeString(d.targetName, 160),
      rating: typeof d.rating === "number" ? d.rating : 0,
      comment: normalizeString(d.comment, 1000),
      authorName: normalizeString(d.authorName, 120),
      createdAt: d.createdAt instanceof admin.firestore.Timestamp ? d.createdAt.toDate().toISOString() : "",
    };
  });

  return { items };
});

// ─── User Settings ─────────────────────────────────────────────────────────

export const getUserSettings = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;

  const doc = await db.collection("users").doc(uid).collection("settings").doc("preferences").get();
  if (!doc.exists) {
    return { themeMode: "SYSTEM", language: "SYSTEM", notificationsEnabled: true, weightUnit: "kg", distanceUnit: "km" };
  }
  const d = doc.data()!;
  return {
    themeMode: normalizeString(d.themeMode, 20) || "SYSTEM",
    language: normalizeString(d.language, 20) || "SYSTEM",
    notificationsEnabled: typeof d.notificationsEnabled === "boolean" ? d.notificationsEnabled : true,
    weightUnit: normalizeString(d.weightUnit, 10) || "kg",
    distanceUnit: normalizeString(d.distanceUnit, 10) || "km",
  };
});

export const updateUserSettings = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const update: Record<string, unknown> = { updatedAt: admin.firestore.FieldValue.serverTimestamp() };
  if (typeof data.themeMode === "string") update.themeMode = normalizeString(data.themeMode, 20);
  if (typeof data.language === "string") update.language = normalizeString(data.language, 20);
  if (typeof data.notificationsEnabled === "boolean") update.notificationsEnabled = data.notificationsEnabled;
  if (typeof data.weightUnit === "string") update.weightUnit = normalizeString(data.weightUnit, 10);
  if (typeof data.distanceUnit === "string") update.distanceUnit = normalizeString(data.distanceUnit, 10);

  await db.collection("users").doc(uid).collection("settings").doc("preferences").set(update, { merge: true });
  return { success: true };
});

// ─── Horse Health Events ────────────────────────────────────────────────────

const VALID_HEALTH_EVENT_TYPES = new Set(["FARRIER", "VACCINATION", "DENTAL", "VET", "DEWORMING", "OTHER"]);

export const getHorseHealthEvents = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const horseId = parseRequiredId(request.data?.horseId, "horseId");

  const horseDoc = await db.collection("users").doc(uid).collection("horses").doc(horseId).get();
  if (!horseDoc.exists) throw new HttpsError("not-found", "Horse not found");

  const snapshot = await db.collection("users").doc(uid).collection("horses").doc(horseId)
    .collection("health_events").orderBy("date", "asc").get();

  const items = snapshot.docs.map((doc) => {
    const d = doc.data();
    return {
      id: doc.id,
      horseId,
      type: VALID_HEALTH_EVENT_TYPES.has(d.type) ? d.type as string : "OTHER",
      date: normalizeString(d.date, 10),
      notes: normalizeString(d.notes, 500),
      createdAt: d.createdAt instanceof admin.firestore.Timestamp ? d.createdAt.toDate().toISOString() : "",
    };
  });

  return { items };
});

export const addHorseHealthEvent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const horseId = parseRequiredId(data.horseId, "horseId");
  const type = typeof data.type === "string" && VALID_HEALTH_EVENT_TYPES.has(data.type) ? data.type : "OTHER";
  const date = normalizeString(data.date, 10);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new HttpsError("invalid-argument", "date must be yyyy-MM-dd");
  const notes = normalizeString(data.notes, 500);

  const horseDoc = await db.collection("users").doc(uid).collection("horses").doc(horseId).get();
  if (!horseDoc.exists) throw new HttpsError("not-found", "Horse not found");
  const horseName = normalizeString(horseDoc.data()?.name, 80);

  const ref = await db.collection("users").doc(uid).collection("horses").doc(horseId)
    .collection("health_events").add({ type, date, notes, createdAt: admin.firestore.FieldValue.serverTimestamp() });

  await maybeWriteHorseHealthReminder(uid, horseId, ref.id, horseName, type, date);

  return { id: ref.id, horseId, type, date, notes, createdAt: new Date().toISOString() };
});

export const updateHorseHealthEvent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const horseId = parseRequiredId(data.horseId, "horseId");
  const eventId = parseRequiredId(data.id, "id");

  const update: Record<string, unknown> = { updatedAt: admin.firestore.FieldValue.serverTimestamp() };
  if (typeof data.type === "string" && VALID_HEALTH_EVENT_TYPES.has(data.type)) update.type = data.type;
  if (typeof data.date === "string" && /^\d{4}-\d{2}-\d{2}$/.test(data.date)) update.date = data.date;
  if (typeof data.notes === "string") update.notes = normalizeString(data.notes, 500);

  const ref = db.collection("users").doc(uid).collection("horses").doc(horseId)
    .collection("health_events").doc(eventId);
  const doc = await ref.get();
  if (!doc.exists) throw new HttpsError("not-found", "Health event not found");

  await ref.update(update);
  const horseDoc = await db.collection("users").doc(uid).collection("horses").doc(horseId).get();
  const nextType = typeof update.type === "string" ? update.type : normalizeString(doc.data()?.type, 40) || "OTHER";
  const nextDate = typeof update.date === "string" ? update.date : normalizeString(doc.data()?.date, 10);
  await maybeWriteHorseHealthReminder(
    uid,
    horseId,
    eventId,
    normalizeString(horseDoc.data()?.name, 80),
    nextType,
    nextDate
  );
  return { success: true };
});

export const deleteHorseHealthEvent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const horseId = parseRequiredId(data.horseId, "horseId");
  const eventId = parseRequiredId(data.id, "id");

  const ref = db.collection("users").doc(uid).collection("horses").doc(horseId)
    .collection("health_events").doc(eventId);
  const doc = await ref.get();
  if (!doc.exists) throw new HttpsError("not-found", "Health event not found");

  await ref.delete();
  return { success: true };
});

export const syncHorseHealthReminders = onSchedule(
  {
    region: "us-central1",
    schedule: "every 6 hours",
    timeZone: "Europe/Istanbul",
    retryCount: 1,
  },
  async () => {
    const today = currentIstanbulDateKey(0);
    const tomorrow = currentIstanbulDateKey(1);
    const snapshot = await db.collectionGroup("health_events")
      .where("date", ">=", today)
      .where("date", "<=", tomorrow)
      .get();

    await mapWithConcurrency(snapshot.docs, 10, async (doc) => {
      const segments = doc.ref.path.split("/");
      const uid = segments[1] || "";
      const horseId = segments[3] || "";
      const eventId = segments[5] || doc.id;
      if (!uid || !horseId) return;

      const data = doc.data();
      const horseDoc = await db.collection("users").doc(uid).collection("horses").doc(horseId).get();
      await maybeWriteHorseHealthReminder(
        uid,
        horseId,
        eventId,
        normalizeString(horseDoc.data()?.name, 80),
        normalizeString(data.type, 40) || "OTHER",
        normalizeString(data.date, 10)
      );
    });
  }
);

export const sendGeneralNotification = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const title = normalizeString(data.title, 160);
  const body = normalizeString(data.body, 500);
  if (!title || !body) {
    throw new HttpsError("invalid-argument", "title and body are required");
  }

  const targetRoute = normalizeString(data.targetRoute, 160);
  const targetId = normalizeString(data.targetId, 120);
  const id = await writeUserNotification(uid, {
    type: "general",
    title,
    body,
    targetId: targetId || undefined,
    targetRoute: targetRoute || undefined,
  });

  return { success: true, id };
});

// ─────────────────────────────────────────────────────────────────────────────
// SAFETY FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────────

export const getSafetySettings = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;

  const settingsDoc = await db.collection("users").doc(uid).collection("safety").doc("settings").get();
  const isEnabled = settingsDoc.exists ? (settingsDoc.data()?.isEnabled ?? false) : false;
  const autoAlarmMinutes = settingsDoc.exists ? (settingsDoc.data()?.autoAlarmMinutes ?? 5) : 5;

  const contactsSnapshot = await db.collection("users").doc(uid).collection("safety")
    .doc("settings").collection("contacts").orderBy("createdAt", "asc").get();
  const contacts = contactsSnapshot.docs.map(doc => ({
    id: doc.id,
    name: (doc.data().name as string) ?? "",
    phone: (doc.data().phone as string) ?? ""
  }));

  return { isEnabled, autoAlarmMinutes, contacts };
});

export const updateSafetySettings = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const update: Record<string, unknown> = { updatedAt: admin.firestore.FieldValue.serverTimestamp() };
  if (typeof data.isEnabled === "boolean") update.isEnabled = data.isEnabled;
  if (typeof data.autoAlarmMinutes === "number") update.autoAlarmMinutes = Math.min(Math.max(1, data.autoAlarmMinutes), 30);

  await db.collection("users").doc(uid).collection("safety").doc("settings").set(update, { merge: true });
  return { success: true };
});

export const addSafetyContact = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const name = normalizeString(data.name, 80);
  const phone = normalizeString(data.phone, 20);
  if (!name) throw new HttpsError("invalid-argument", "name is required");
  if (!phone) throw new HttpsError("invalid-argument", "phone is required");

  // Max 5 contacts
  const existing = await db.collection("users").doc(uid).collection("safety")
    .doc("settings").collection("contacts").count().get();
  if (existing.data().count >= 5) throw new HttpsError("failed-precondition", "Max 5 contacts allowed");

  const ref = await db.collection("users").doc(uid).collection("safety")
    .doc("settings").collection("contacts").add({ name, phone, createdAt: admin.firestore.FieldValue.serverTimestamp() });

  return { id: ref.id, name, phone };
});

export const removeSafetyContact = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const contactId = parseRequiredId(data.contactId, "contactId");
  const ref = db.collection("users").doc(uid).collection("safety")
    .doc("settings").collection("contacts").doc(contactId);
  const doc = await ref.get();
  if (!doc.exists) throw new HttpsError("not-found", "Contact not found");

  await ref.delete();
  return { success: true };
});

export const triggerSafetyAlarm = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Authentication required");
  const uid = request.auth.uid;
  const data = request.data as Record<string, unknown>;

  const lat = typeof data.lat === "number" ? data.lat : null;
  const lng = typeof data.lng === "number" ? data.lng : null;

  // Build location link
  const locationLink = lat !== null && lng !== null
    ? `https://maps.google.com/maps?q=${lat},${lng}`
    : null;

  // Get user profile for display name
  const profileDoc = await db.collection("users").doc(uid).get();
  const firstName = profileDoc.exists ? (profileDoc.data()?.firstName ?? "A rider") : "A rider";

  // Get safety contacts
  const contactsSnapshot = await db.collection("users").doc(uid).collection("safety")
    .doc("settings").collection("contacts").get();

  // Get FCM tokens of user (self-notification for now; real implementation sends SMS/FCM to contacts)
  const tokenDoc = await db.collection("users").doc(uid).collection("tokens").doc("fcm").get();
  const fcmToken = tokenDoc.exists ? tokenDoc.data()?.token : null;

  if (fcmToken && admin.messaging) {
    const message = locationLink
      ? `${firstName} may need help! Last location: ${locationLink}`
      : `${firstName} may need help! No location available.`;
    await admin.messaging().send({
      token: fcmToken,
      notification: {
        title: "🛡️ Safety Alert",
        body: message
      },
      data: {
        type: "safety_alarm",
        lat: lat?.toString() ?? "",
        lng: lng?.toString() ?? "",
        locationLink: locationLink ?? ""
      }
    });
  }

  // Log alarm in Firestore
  await db.collection("users").doc(uid).collection("safety").doc("settings")
    .collection("alarms").add({
      lat,
      lng,
      locationLink,
      triggeredAt: admin.firestore.FieldValue.serverTimestamp(),
      contactCount: contactsSnapshot.size
    });

  return { success: true, locationLink };
});

// ─── Equestrian Agenda ───────────────────────────────────────────────────────

export const getEquestrianAnnouncements = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const cached = await readScrapeCache<{ items: EquestrianAnnouncementDto[] }>(EQUESTRIAN_ANNOUNCEMENTS_CACHE_KEY);
  return cached ?? { items: [] };
});

export const getEquestrianCompetitions = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const cached = await readScrapeCache<{ items: EquestrianCompetitionDto[] }>(EQUESTRIAN_COMPETITIONS_CACHE_KEY);
  return cached ?? { items: [] };
});

// ─── At Sağlık Takvimi ───────────────────────────────────────────────────────

export const getHealthEvents = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Login required");
  }
  const uid = request.auth.uid;
  const horseId = request.data?.horseId as string | undefined;

  let query: FirebaseFirestore.Query = db.collection("healthEvents").where("userId", "==", uid);
  if (horseId) {
    query = query.where("horseId", "==", horseId);
  }

  const snapshot = await query.orderBy("scheduledDate", "desc").get();
  return {
    events: snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }))
  };
});

export const saveHealthEvent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Login required");
  }
  const uid = request.auth.uid;
  const { id, horseId, horseName, type, scheduledDate, completedDate, notes, isCompleted } = request.data ?? {};

  if (!horseId) throw new HttpsError("invalid-argument", "horseId required");
  if (!type) throw new HttpsError("invalid-argument", "type required");
  if (scheduledDate == null) throw new HttpsError("invalid-argument", "scheduledDate required");

  const payload = {
    userId: uid,
    horseId,
    horseName: horseName ?? "",
    type,
    scheduledDate,
    completedDate: completedDate ?? null,
    notes: notes ?? "",
    isCompleted: isCompleted ?? false
  };

  if (id && id !== "") {
    await db.collection("healthEvents").doc(id as string).set(payload, { merge: true });
    return { id };
  } else {
    const ref = await db.collection("healthEvents").add(payload);
    return { id: ref.id };
  }
});

export const deleteHealthEvent = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Login required");
  }
  const { eventId } = request.data ?? {};
  if (!eventId) throw new HttpsError("invalid-argument", "eventId required");
  await db.collection("healthEvents").doc(eventId as string).delete();
  return { success: true };
});

export const markHealthEventCompleted = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Login required");
  }
  const { eventId, completedDate } = request.data ?? {};
  if (!eventId) throw new HttpsError("invalid-argument", "eventId required");
  await db.collection("healthEvents").doc(eventId as string).update({
    isCompleted: true,
    completedDate: completedDate ?? Date.now()
  });
  return { success: true };
});

// ─── Challenge / Badge System ────────────────────────────────────────────────

export const getActiveChallenges = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Login required");
  const userId = request.auth.uid;
  const now = Date.now();

  const globalSnapshot = await db.collection("challenges")
    .where("endDate", ">", now)
    .orderBy("endDate", "asc")
    .get();

  const userProgressSnapshot = await db.collection("userChallengeProgress")
    .where("userId", "==", userId)
    .get();

  const progressMap = new Map(
    userProgressSnapshot.docs.map(d => [d.data().challengeId as string, d.data().currentValue as number])
  );

  return {
    challenges: globalSnapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
      currentValue: progressMap.get(doc.id) ?? 0,
      isCompleted: (progressMap.get(doc.id) ?? 0) >= (doc.data().targetValue as number)
    }))
  };
});

export const getEarnedBadges = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Login required");
  const userId = request.auth.uid;

  const snapshot = await db.collection("userBadges")
    .where("userId", "==", userId)
    .orderBy("earnedDate", "desc")
    .get();

  return {
    badges: snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }))
  };
});

export const checkAndAwardBadges = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Login required");
  const userId = request.auth.uid;
  const { distanceMeters, durationSeconds, avgSpeedKph } = request.data ?? {};

  const newBadges: string[] = [];

  const ridesSnapshot = await db.collection("trainings").where("userId", "==", userId).get();
  const totalKm = ridesSnapshot.docs.reduce(
    (sum, d) => sum + ((d.data().distanceMeters as number) / 1000),
    0
  );

  const badgesToCheck = [
    { type: "FIRST_RIDE", condition: ridesSnapshot.size >= 1 },
    { type: "DISTANCE_10K", condition: totalKm >= 10 },
    { type: "DISTANCE_50K", condition: totalKm >= 50 },
    { type: "DISTANCE_100K", condition: totalKm >= 100 },
    { type: "SPEED_DEMON", condition: (avgSpeedKph as number) >= 20 },
  ];

  const existingBadges = await db.collection("userBadges").where("userId", "==", userId).get();
  const earnedTypes = new Set(existingBadges.docs.map(d => d.data().type as string));

  for (const badge of badgesToCheck) {
    if (badge.condition && !earnedTypes.has(badge.type)) {
      await db.collection("userBadges").add({
        userId,
        type: badge.type,
        earnedDate: Date.now()
      });
      newBadges.push(badge.type);
    }
  }

  return { newBadges };
});
