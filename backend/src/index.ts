import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
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

  const snapshot = await db.collection("barns").get();
  const items = snapshot.docs.map((doc) => normalizeBarnDto(doc.id, doc.data()));
  return { items };
});

export const getBarnDetail = onCall({ region: "us-central1" }, async (request): Promise<BarnDto> => {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const id = parseRequiredId((request.data || {}).id, "id");
  const [doc, instructorSnap, reviewSnap] = await Promise.all([
    db.collection("barns").doc(id).get(),
    db.collection("barns").doc(id).collection("instructors").orderBy("rating", "desc").limit(5).get(),
    db.collection("reviews")
      .where("targetId", "==", id)
      .where("targetType", "==", "barn")
      .orderBy("createdAt", "desc")
      .limit(5)
      .get(),
  ]);
  if (!doc.exists) {
    throw new HttpsError("not-found", "Barn not found");
  }
  const instructors: BarnInstructorDto[] = instructorSnap.docs.map((d) => {
    const x = d.data();
    return {
      id: d.id,
      name: normalizeString(x.name, 120),
      photoUrl: typeof x.photoUrl === "string" ? x.photoUrl : "",
      specialty: normalizeString(x.specialty, 80),
      rating: typeof x.rating === "number" ? x.rating : 0,
    };
  });
  const reviews: BarnReviewDto[] = reviewSnap.docs.map((d) => {
    const x = d.data();
    return {
      id: d.id,
      authorName: normalizeString(x.authorName, 120),
      rating: typeof x.rating === "number" ? x.rating : 0,
      comment: normalizeString(x.comment, 500),
      dateLabel: normalizeString(x.dateLabel, 40),
    };
  });
  return normalizeBarnDto(doc.id, doc.data() || {}, instructors, reviews);
});

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

  const ref = await db.collection("users").doc(uid).collection("horses").doc(horseId)
    .collection("health_events").add({ type, date, notes, createdAt: admin.firestore.FieldValue.serverTimestamp() });

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

// ─── TJK Yarış Entegrasyonu ──────────────────────────────────────────────────

// City ID → name mapping from tjk.org
const TJK_CITIES: Record<number, string> = {
  1: "Adana",
  2: "İzmir",
  3: "İstanbul",
  4: "Bursa",
  5: "Ankara",
  6: "Urfa",
  7: "Elazığ",
  8: "Diyarbakır",
  9: "Kocaeli",
};

interface TjkRaceResultEntry {
  position: string;
  horseName: string;
  jockey: string;
  trainer: string;
  weight: string;
  time: string;
}

interface TjkRaceEntry {
  raceNo: number;
  raceTitle: string;
  distance: string;
  surface: string;
  startTime: string;
  results: TjkRaceResultEntry[];
}

interface TjkRaceDayResponse {
  date: string;
  cityId: number;
  cityName: string;
  races: TjkRaceEntry[];
}

async function scrapeTjkRaceDay(dateStr: string, cityId: number): Promise<TjkRaceDayResponse> {
  // Fetch the AJAX endpoint for a specific city — this returns the full race data for that city.
  // Verified URL pattern from tjk.org inspection (13/03/2026).
  const cityName = TJK_CITIES[cityId] ?? "";
  const url =
    `https://www.tjk.org/TR/YarisSever/Info/Sehir/GunlukYarisSonuclari` +
    `?SehirId=${cityId}` +
    `&QueryParameter_Tarih=${encodeURIComponent(dateStr)}` +
    `&SehirAdi=${encodeURIComponent(cityName)}` +
    `&Era=today`;

  const response = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36",
      "Accept": "text/html,application/xhtml+xml",
      "Accept-Language": "tr-TR,tr;q=0.9,en;q=0.8",
      "Referer": "https://www.tjk.org/",
    },
  } as Record<string, unknown>);

  if (!response.ok) {
    throw new HttpsError("unavailable", `TJK returned HTTP ${response.status}`);
  }

  const html = await response.text();
  const $ = cheerio.load(html);
  const races: TjkRaceEntry[] = [];

  // ── Confirmed HTML structure (verified 13/03/2026) ──────────────────────────
  //
  // <div class="races-panes races-panes{SehirId}">
  //   <div>   ← one div per race
  //     <h3 class="race-no">
  //       <a href="#223638" id="anc223638">1. Koşu 14.30</a>
  //     </h3>
  //     <table summary="Kosular" class="tablesorter">
  //       <thead><tr>
  //         <th>Forma</th><th>S</th><th>At İsmi</th><th>Yaş</th>
  //         <th>Orijin</th><th>Sıklet</th><th>Jokey</th><th>Sahip</th>
  //         <th>Antrenör</th><th>Derece</th>...
  //       </tr></thead>
  //       <tbody>
  //         <tr class="odd|even">
  //           <td class="gunluk-GunlukYarisSonuclari-FormaKodu">…</td>
  //           <td class="gunluk-GunlukYarisSonuclari-SONUCNO">1</td>
  //           <td class="gunluk-GunlukYarisSonuclari-AtAdi3"><a>DİLŞAHKAYA(1)</a></td>
  //           <td class="gunluk-GunlukYarisSonuclari-Yas">4y k k</td>
  //           <td class="gunluk-GunlukYarisSonuclari-Baba">…</td>
  //           <td class="gunluk-GunlukYarisSonuclari-Kilo">58</td>
  //           <td class="gunluk-GunlukYarisSonuclari-JokeAdi"><a>Y.GÖKÇE</a></td>
  //           <td class="gunluk-GunlukYarisSonuclari-SahipAdi"><a>ELİF KAYA</a></td>
  //           <td class="gunluk-GunlukYarisSonuclari-AntronorAdi"><a>RAM. KAYA</a></td>
  //           <td class="gunluk-GunlukYarisSonuclari-Derece">1.34.43</td>
  //           …
  //         </tr>
  //       </tbody>
  //     </table>
  //   </div>
  // </div>

  // Find the races-panes container (class contains "races-panes")
  const racesPanes = $(`[class*="races-panes${cityId}"], .races-panes`).first();
  const container = racesPanes.length ? racesPanes : $("body");

  // Each direct child div of races-panes is one race
  container.children("div").each((_idx, raceDiv) => {
    const $raceDiv = $(raceDiv);

    // ── Race header: "1. Koşu 14.30" ──────────────────────────────────────
    const headingText = $raceDiv.find("h3.race-no a").first().text().trim();
    // Matches "1. Koşu 14.30" or "1. Koşu: 14.30"
    const headingMatch = headingText.match(/(\d+)\.\s*Ko[şs]u[:\s]+(\d{1,2}[.:]\d{2})/i);
    const raceNo = headingMatch ? parseInt(headingMatch[1], 10) : (_idx + 1);
    const startTime = headingMatch ? headingMatch[2].replace(".", ":") : "";

    // ── Race details: look for sibling/child elements with distance & surface ──
    // Some pages show these in a <ul> or <p> near the heading.
    // We'll try common patterns and fall back to empty string.
    const detailsText = $raceDiv.find(".race-details, .kosu-bilgi, .race-info-row, ul.race-info li")
      .text().trim();
    // Try to extract distance (e.g., "1400 m") and surface ("Kum", "Çim", "Sentetik")
    const distanceMatch = detailsText.match(/(\d{3,5})\s*m/i);
    const surfaceMatch = detailsText.match(/\b(Kum|Çim|Sentetik|Turf|Sand|Grass)\b/i);
    const distance = distanceMatch ? `${distanceMatch[1]} m` : "";
    const surface = surfaceMatch ? surfaceMatch[1] : "";

    // Race title: the link text may include a name after the "Koşu" label
    // e.g., "1. Koşu — Uğur Koşusu 14.30" — extract the named part if present
    const raceTitleMatch = headingText.match(/Ko[şs]u[:\s—–-]+(.+?)\s+\d{1,2}[.:]\d{2}/i);
    const raceTitle = raceTitleMatch ? raceTitleMatch[1].trim() : "";

    // ── Result rows ────────────────────────────────────────────────────────
    const results: TjkRaceResultEntry[] = [];
    $raceDiv.find("table.tablesorter tbody tr").each((_ri, row) => {
      const $row = $(row);

      // Skip rows without enough cells (e.g., spacer rows)
      const position = $row.find("td.gunluk-GunlukYarisSonuclari-SONUCNO").text().trim();
      if (!position) return;

      // Horse name: strip "(1)", "(2)" starting-number suffix if present
      const rawHorse = $row.find("td.gunluk-GunlukYarisSonuclari-AtAdi3 a").first().text().trim();
      const horseName = rawHorse.replace(/\(\d+\)\s*$/, "").trim();

      // Weight: first text node (before any <sup> bonus/penalty info)
      const weightRaw = $row.find("td.gunluk-GunlukYarisSonuclari-Kilo").contents().first().text().trim();

      // Jockey: link text
      const jockey = $row.find("td.gunluk-GunlukYarisSonuclari-JokeAdi a").first().text().trim();

      // Trainer
      const trainer = $row.find("td.gunluk-GunlukYarisSonuclari-AntronorAdi a").first().text().trim();

      // Finishing time (e.g., "1.34.43")
      const time = $row.find("td.gunluk-GunlukYarisSonuclari-Derece").text().trim();

      results.push({ position, horseName, jockey, trainer, weight: weightRaw, time });
    });

    // Only include races that have at least one result row
    if (results.length > 0 || headingMatch) {
      races.push({ raceNo, raceTitle, distance, surface, startTime, results });
    }
  });

  return {
    date: dateStr,
    cityId,
    cityName: cityName || `City ${cityId}`,
    races,
  };
}

/**
 * getTjkRaceDay — Scrapes tjk.org for daily race results.
 *
 * Input: { date: "DD/MM/YYYY", cityId: number }
 * Output: { date, cityId, cityName, races: [ { raceNo, raceTitle, distance, surface, startTime, results: [...] } ] }
 */
export const getTjkRaceDay = onCall({ region: "us-central1" }, async (request) => {
  const data = request.data as Record<string, unknown>;
  const dateStr = typeof data.date === "string" ? data.date.trim() : "";
  const cityId = typeof data.cityId === "number" ? data.cityId : 3; // default: İstanbul

  if (!dateStr || !/^\d{2}\/\d{2}\/\d{4}$/.test(dateStr)) {
    throw new HttpsError("invalid-argument", "date must be in DD/MM/YYYY format");
  }
  if (!TJK_CITIES[cityId]) {
    throw new HttpsError("invalid-argument", `Unknown cityId: ${cityId}`);
  }

  const result = await scrapeTjkRaceDay(dateStr, cityId);
  return result;
});

/**
 * getTjkCities — Returns the list of TJK cities with their IDs.
 */
export const getTjkCities = onCall({ region: "us-central1" }, async (_request) => {
  return Object.entries(TJK_CITIES).map(([id, name]) => ({
    id: parseInt(id, 10),
    name,
  }));
});
