import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();

async function seedBarns() {
  const barns = [
    {
      id: "barn_adin_country",
      name: "Adin Country",
      description: "Beginner to Pro rides",
      location: "Istanbul, TR",
      lat: 41.0082,
      lng: 28.9784,
      tags: ["cafe", "indoor_arena", "parking", "lessons", "open_now"],
      amenities: ["cafe", "indoor_arena", "parking", "lessons", "open_now"],
      rating: 4.7,
      reviewCount: 124,
    },
    {
      id: "barn_sable_ranch",
      name: "Sable Ranch",
      description: "Trail and endurance",
      location: "Sariyer, TR",
      lat: 41.0151,
      lng: 29.0037,
      tags: ["outdoor_arena", "trail", "parking", "boarding"],
      amenities: ["outdoor_arena", "trail", "parking", "boarding"],
      rating: 4.5,
      reviewCount: 89,
    },
  ];

  await Promise.all(
    barns.map((barn) =>
      db.collection("barns").doc(barn.id).set({
        ...barn,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true })
    )
  );
}

async function seedLessons() {
  const lessons = [
    {
      id: "lesson_1",
      date: "2026-03-03 10:00",
      title: "Beginner Ride",
      instructorName: "Alice",
      durationMin: 60,
      level: "Beginner",
      price: 1200,
    },
    {
      id: "lesson_2",
      date: "2026-03-04 14:00",
      title: "Trail Basics",
      instructorName: "Bob",
      durationMin: 75,
      level: "Intermediate",
      price: 1450,
    },
  ];

  await Promise.all(
    lessons.map((lesson) =>
      db.collection("lessons").doc(lesson.id).set({
        ...lesson,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true })
    )
  );
}

async function seedAppContent() {
  await db.collection("app_content").doc("tr").set(
    {
      home: {
        heroTitle: "HorseGallop Pro ile Antrenmanını Geliştir",
        heroSubtitle: "Günlük performansını takip et, hedeflerini yükselt.",
      },
      common: {
        offlineHelp: "Bağlantını kontrol et ve tekrar dene.",
      },
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  await db.collection("app_content").doc("en").set(
    {
      home: {
        heroTitle: "Train Smarter with HorseGallop Pro",
        heroSubtitle: "Track your daily performance and level up.",
      },
      common: {
        offlineHelp: "Check your connection and retry.",
      },
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
}

async function main() {
  await seedBarns();
  await seedLessons();
  await seedAppContent();
  console.log("Seed completed: barns, lessons, app_content");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("Seed failed", error);
    process.exit(1);
  });
