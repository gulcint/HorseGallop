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
      auth: {
        loginTitle: "HorseGallop'a Hoş Geldin",
        loginSubtitle: "Sürüşlerini takip et, antrenman hedeflerini yükselt.",
        emailLoginTitle: "E-posta ile giriş",
        emailLoginSubtitle: "Hesap bilgilerini girerek güvenli şekilde devam et.",
        enrollTitle: "HorseGallop topluluğuna katıl",
        enrollSubtitle: "Profilini tamamla ve ilk sürüşünü planla.",
        forgotPasswordSubtitle: "E-posta adresini gir, sıfırlama bağlantısını hemen gönderelim.",
      },
      onboarding: {
        heroTitle: "Atçılık yolculuğuna profesyonel başla",
        heroSubtitle: "Ahır, eğitim ve sürüş verilerini tek panelde yönet.",
        helpText: "Özellikleri keşfetmek için kaydır, dilediğinde başlayabilirsin.",
      },
      ride: {
        liveTitle: "Canlı Sürüş Paneli",
        liveSubtitleIdle: "Sürüş türünü ve ahırı seç, hazır olduğunda başlat.",
        liveSubtitleActive: "Rota, hız ve kaloriler canlı takip ediliyor.",
        permissionTitle: "Konum izni gerekli",
        permissionHint: "Canlı rota ve mesafe için konum iznini etkinleştir.",
        grantLocationCta: "Konum izni ver",
      },
      settings: {
        themeSubtitle: "Uygulama görünümünü tercihine göre ayarla.",
        languageSubtitle: "Uygulama dilini hızlıca değiştir.",
        notificationsSubtitle: "Ders ve sürüş hatırlatmalarını kontrol et.",
        privacySubtitle: "Veri dışa aktarma ve hesap silme işlemlerini yönet.",
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
      auth: {
        loginTitle: "Welcome to HorseGallop",
        loginSubtitle: "Track rides and level up your training goals.",
        emailLoginTitle: "Sign in with email",
        emailLoginSubtitle: "Use your account credentials to continue securely.",
        enrollTitle: "Join the HorseGallop community",
        enrollSubtitle: "Complete your profile and plan your first ride.",
        forgotPasswordSubtitle: "Enter your email and we will send a reset link right away.",
      },
      onboarding: {
        heroTitle: "Start your riding journey like a pro",
        heroSubtitle: "Manage barns, lessons, and ride analytics from one place.",
        helpText: "Swipe to explore features, then start when ready.",
      },
      ride: {
        liveTitle: "Live Ride Panel",
        liveSubtitleIdle: "Choose ride type and barn, then start your session.",
        liveSubtitleActive: "Route, speed, and calories are being tracked live.",
        permissionTitle: "Location permission required",
        permissionHint: "Enable location permission for live route and distance tracking.",
        grantLocationCta: "Grant location access",
      },
      settings: {
        themeSubtitle: "Adjust the app appearance to your preference.",
        languageSubtitle: "Quickly switch app language.",
        notificationsSubtitle: "Control lesson and ride reminders.",
        privacySubtitle: "Manage data export and account deletion actions.",
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
