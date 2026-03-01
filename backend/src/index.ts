import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

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

type TrainingTaskDto = {
  id: string;
  title: string;
  description: string;
  targetMinutes: number;
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "LOCKED";
};

type TrainingPlanDto = {
  id: string;
  title: string;
  summary: string;
  weeklyGoal: number;
  progressPercent: number;
  streakDays: number;
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "LOCKED";
  tasks: TrainingTaskDto[];
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

function defaultTrainingPlans(isPro: boolean): TrainingPlanDto[] {
  return [
    {
      id: "plan_foundation",
      title: "Foundation Control",
      summary: "Balance, posture and smooth transitions for new-season prep.",
      weeklyGoal: 3,
      progressPercent: 20,
      streakDays: 2,
      status: "IN_PROGRESS",
      tasks: [
        {
          id: "task_1",
          title: "Warm-up 20 min",
          description: "Walk/trot interval.",
          targetMinutes: 20,
          status: "COMPLETED",
        },
        {
          id: "task_2",
          title: "Core seat drills",
          description: "No-stirrup seated work.",
          targetMinutes: 25,
          status: "IN_PROGRESS",
        },
        {
          id: "task_3",
          title: "Cooldown",
          description: "Light walk and breathing.",
          targetMinutes: 10,
          status: "NOT_STARTED",
        },
      ],
    },
    {
      id: "plan_endurance_pro",
      title: "Endurance Pro 4W",
      summary: "Distance pacing and heart-zone control for competitions.",
      weeklyGoal: 4,
      progressPercent: 0,
      streakDays: 0,
      status: isPro ? "NOT_STARTED" : "LOCKED",
      tasks: [
        {
          id: "task_e1",
          title: "Zone 2 Ride",
          description: "Steady aerobic base work.",
          targetMinutes: 35,
          status: isPro ? "NOT_STARTED" : "LOCKED",
        },
        {
          id: "task_e2",
          title: "Hill Repeats",
          description: "Controlled effort climbs.",
          targetMinutes: 30,
          status: isPro ? "NOT_STARTED" : "LOCKED",
        },
      ],
    },
  ];
}

export const getTrainingPlans = onCall({ region: "us-central1" }, async (request) => {
  const auth = request.auth;
  if (!auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const entitlementDoc = await db.collection("user_entitlements").doc(auth.uid).get();
  const tier = (entitlementDoc.data()?.tier as string | undefined) ?? "FREE";
  const isPro = tier !== "FREE";
  return { plans: defaultTrainingPlans(isPro), tier };
});

export const completeTrainingTask = onCall({ region: "us-central1" }, async (request) => {
  const auth = request.auth;
  if (!auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }
  const planId = normalizeString(request.data?.planId, 64);
  const taskId = normalizeString(request.data?.taskId, 64);
  if (!planId || !taskId) {
    throw new HttpsError("invalid-argument", "planId and taskId are required");
  }

  await db.collection("users").doc(auth.uid).collection("trainingProgress").doc(planId).set(
    {
      [`tasks.${taskId}`]: "COMPLETED",
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  return { success: true };
});

export const getSubscriptionEntitlements = onCall({ region: "us-central1" }, async (request) => {
  const auth = request.auth;
  if (!auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }

  const entitlementDoc = await db.collection("user_entitlements").doc(auth.uid).get();
  const tier = (entitlementDoc.data()?.tier as string | undefined) ?? "FREE";
  const active = tier !== "FREE";
  return {
    tier,
    active,
  };
});
