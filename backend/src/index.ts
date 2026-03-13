import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import type { BarnDto, BarnListDto, LessonDto, LessonListDto, HorseTipDto, HorseTipListDto, BreedDto, BreedListDto } from "./contracts";
import { buildHomeDashboard } from "./home-service";
import {
  parseLimit,
  parseOptionalDate,
  parseOptionalNumber,
  parseOptionalString,
  parseRequiredId,
} from "./validators";

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

function normalizeBarnDto(id: string, data: FirebaseFirestore.DocumentData): BarnDto {
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
  const doc = await db.collection("barns").doc(id).get();
  if (!doc.exists) {
    throw new HttpsError("not-found", "Barn not found");
  }
  return normalizeBarnDto(doc.id, doc.data() || {});
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
