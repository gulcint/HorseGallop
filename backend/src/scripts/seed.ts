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
      date: "2026-04-05 10:00",
      title: "Beginner Ride",
      instructorName: "Alice",
      durationMin: 60,
      level: "Beginner",
      price: 1200,
    },
    {
      id: "lesson_2",
      date: "2026-04-06 14:00",
      title: "Trail Basics",
      instructorName: "Bob",
      durationMin: 75,
      level: "Intermediate",
      price: 1450,
    },
    {
      id: "lesson_3",
      date: "2026-04-08 09:00",
      title: "Dressaj Temelleri",
      instructorName: "Ayşe Kaya",
      durationMin: 60,
      level: "Beginner",
      price: 1300,
      barnId: "barn_adin_country",
      spotsTotal: 8,
      spotsAvailable: 5,
    },
    {
      id: "lesson_4",
      date: "2026-04-10 15:00",
      title: "Atlama Teknikleri",
      instructorName: "Mehmet Yıldız",
      durationMin: 90,
      level: "Advanced",
      price: 1800,
      barnId: "barn_sable_ranch",
      spotsTotal: 6,
      spotsAvailable: 2,
    },
    {
      id: "lesson_5",
      date: "2026-04-12 11:00",
      title: "Western Riding",
      instructorName: "Alice",
      durationMin: 60,
      level: "Intermediate",
      price: 1500,
      barnId: "barn_adin_country",
      spotsTotal: 10,
      spotsAvailable: 7,
    },
    {
      id: "lesson_6",
      date: "2026-04-15 14:00",
      title: "Endurance Training",
      instructorName: "Bob",
      durationMin: 120,
      level: "Advanced",
      price: 2200,
      barnId: "barn_sable_ranch",
      spotsTotal: 4,
      spotsAvailable: 4,
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

async function seedHorseTips() {
  const tips = [
    // Turkish tips
    {
      id: "tip_tr_1", locale: "tr", category: "breed",
      title: "Arap Atı: Eşsiz Anatomisi",
      body: "Arap atları diğer ırklardan farklı olarak 17 kuyruk omuru taşır (diğerleri 18-19). Bu yapı, zarif ve yüksek kuyruk taşıma pozisyonuna zemin hazırlar.",
    },
    {
      id: "tip_tr_2", locale: "tr", category: "physiology",
      title: "At Kalbi: Bir Motor Gibi",
      body: "Bir atın kalbi 3,6–4,5 kg ağırlığında olup egzersizde dakikada 38 litreye kadar kan pompalayabilir. Bu inanılmaz kapasite, sürat ve dayanıklılığın temelidir.",
    },
    {
      id: "tip_tr_3", locale: "tr", category: "vision",
      title: "360 Derece Görüş",
      body: "Atlar neredeyse 360 derecelik panoramik görüşe sahiptir; yalnızca burnun hemen önü ve tam arkaları kör noktadır. Bu avantaj, doğada avcılardan kaçmak için kritik öneme sahiptir.",
    },
    {
      id: "tip_tr_4", locale: "tr", category: "behavior",
      title: "Ayakta Uyuma Sanatı",
      body: "Atlar, bacaklarındaki 'kilitleme mekanizması' sayesinde ayakta uyuyabilir. Ancak REM uykusuna geçmek için kısa süreli yatmaları gerekir — günde 30 dakika yeterlidir.",
    },
    {
      id: "tip_tr_5", locale: "tr", category: "breed",
      title: "Ahal-Teke: Metalik Tüy",
      body: "Türkmenistan'a özgü Ahal-Teke ırkı, tüylerindeki yapısal parlaklık sayesinde altın metalik görünüm sergiler. Bu nadir ırk, dünyanın en eski kültür atları arasındadır.",
    },
    {
      id: "tip_tr_6", locale: "tr", category: "speed",
      title: "Safkan Hız Rekoru",
      body: "2008'de Winning Brew adlı Safkan at, saatte 70,76 km ile rekor kırdı. İngiliz Safkanı, 400 yıllık özel yetiştirme programıyla dünyanın en hızlı at ırkı unvanını taşıyor.",
    },
    {
      id: "tip_tr_7", locale: "tr", category: "care",
      title: "Atlar Neden Sürekli Otlar?",
      body: "Atlar birer 'arka bağırsak fermantörü'dür ve sindirim sağlığı için günde 16–18 saat otlanmaya ihtiyaç duyarlar. Boş kalan sindirim sistemi kolik riskini ciddi şekilde artırır.",
    },
    {
      id: "tip_tr_8", locale: "tr", category: "physiology",
      title: "Tay Doğar Doğmaz Ayağa Kalkar",
      body: "Taylar, doğumdan 1–2 saat içinde ayağa kalkabilir ve annesinin peşinden koşabilir. Bu, yaban hayatında avcılardan kaçmak için gelişmiş temel bir hayatta kalma içgüdüsüdür.",
    },
    {
      id: "tip_tr_9", locale: "tr", category: "anatomy",
      title: "At Toynaklarının Sırrı",
      body: "At toynaklarının ana bileşeni keratin proteinidir — insan tırnakları ve saçlarıyla aynı madde. Sağlıklı bir toynak, günde yaklaşık 0,6 cm uzar.",
    },
    {
      id: "tip_tr_10", locale: "tr", category: "breed",
      title: "Endülüs Atı ve Kraliyet Mirası",
      body: "Endülüs Atı (PRE), 15. yüzyılda İber Yarımadası'nda İspanyol soyluluğunun sembolü haline geldi. Bugün hâlâ Klasik Binicilikte en prestijli ırk olma özelliğini korumaktadır.",
    },
    // English tips
    {
      id: "tip_en_1", locale: "en", category: "breed",
      title: "Arabian: A Unique Skeleton",
      body: "Arabian horses have 17 tail vertebrae instead of the usual 18–19 in other breeds. This skeletal difference contributes to their characteristic high tail carriage.",
    },
    {
      id: "tip_en_2", locale: "en", category: "physiology",
      title: "The Horse Heart: A Powerhouse",
      body: "A horse's heart weighs 3.6–4.5 kg and can pump up to 38 litres per minute during intense exercise — making it one of the most efficient cardiovascular engines in the animal kingdom.",
    },
    {
      id: "tip_en_3", locale: "en", category: "vision",
      title: "Nearly 360° Vision",
      body: "Horses have almost panoramic vision with only two blind spots: directly in front of their nose and directly behind them. This wide-angle sight is vital for detecting predators in the wild.",
    },
    {
      id: "tip_en_4", locale: "en", category: "behavior",
      title: "Horses Sleep Standing Up",
      body: "Thanks to a passive 'stay apparatus' in their legs, horses can sleep lightly while standing. However, they still need brief periods of lying down — about 30 minutes per day — to reach REM sleep.",
    },
    {
      id: "tip_en_5", locale: "en", category: "breed",
      title: "Akhal-Teke: The Golden Horse",
      body: "The Akhal-Teke from Turkmenistan produces a natural metallic sheen on its coat due to the unique structure of its hair follicles. It is one of the oldest domesticated breeds in history.",
    },
    {
      id: "tip_en_6", locale: "en", category: "speed",
      title: "Thoroughbred Speed Record",
      body: "In 2008, a Thoroughbred named Winning Brew set the world speed record at 70.76 km/h. Thoroughbreds have been selectively bred for over 400 years for peak racing performance.",
    },
    {
      id: "tip_en_7", locale: "en", category: "care",
      title: "Horses Need to Graze Constantly",
      body: "As hindgut fermenters, horses require 16–18 hours of grazing daily for proper digestive health. An empty digestive tract significantly increases the risk of dangerous colic.",
    },
    {
      id: "tip_en_8", locale: "en", category: "physiology",
      title: "Foals Walk Within Hours",
      body: "Foals can stand and walk within 1–2 hours of birth and run alongside their mother within the day. This rapid development is an evolutionary survival adaptation against predators.",
    },
    {
      id: "tip_en_9", locale: "en", category: "anatomy",
      title: "Hooves Are Made of Keratin",
      body: "A horse's hoof wall is composed primarily of keratin — the same protein found in human nails and hair. A healthy hoof grows approximately 0.6 cm per month.",
    },
    {
      id: "tip_en_10", locale: "en", category: "breed",
      title: "Andalusian: A Royal Heritage",
      body: "The Andalusian (PRE) was the mount of Spanish royalty from the 15th century onward. Today it remains the most prestigious breed in Classical Dressage and is still bred at the Royal Andalusian School.",
    },
  ];

  await Promise.all(
    tips.map((tip) =>
      db.collection("horse_tips").doc(tip.id).set({
        ...tip,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true })
    )
  );
  console.log(`Seeded ${tips.length} horse tips.`);
}

async function seedBreeds() {
  const breeds = [
    { id: "breed_arabian",      nameEn: "Arabian",       nameTr: "Arap",          sortOrder: 1 },
    { id: "breed_thoroughbred", nameEn: "Thoroughbred",  nameTr: "İngiliz Safkanı", sortOrder: 2 },
    { id: "breed_holsteiner",   nameEn: "Holsteiner",    nameTr: "Holsteiner",    sortOrder: 3 },
    { id: "breed_kwpb",         nameEn: "KWPB",          nameTr: "KWPB",          sortOrder: 4 },
    { id: "breed_hanoverian",   nameEn: "Hanoverian",    nameTr: "Hannoveraner",  sortOrder: 5 },
    { id: "breed_trakehner",    nameEn: "Trakehner",     nameTr: "Trakehner",     sortOrder: 6 },
    { id: "breed_lusitano",     nameEn: "Lusitano",      nameTr: "Lusitano",      sortOrder: 7 },
    { id: "breed_andalusian",   nameEn: "Andalusian",    nameTr: "Endülüs (PRE)", sortOrder: 8 },
    { id: "breed_selle_fr",     nameEn: "Selle Français",nameTr: "Selle Français",sortOrder: 9 },
    { id: "breed_qh",           nameEn: "Quarter Horse", nameTr: "Quarter Horse", sortOrder: 10 },
    { id: "breed_mustang",      nameEn: "Mustang",       nameTr: "Mustang",       sortOrder: 11 },
    { id: "breed_akhalteke",    nameEn: "Akhal-Teke",    nameTr: "Ahal-Teke",     sortOrder: 12 },
    { id: "breed_turkish",      nameEn: "Turkish Horse", nameTr: "Türk Atı",      sortOrder: 13 },
    { id: "breed_morgan",       nameEn: "Morgan",        nameTr: "Morgan",        sortOrder: 14 },
    { id: "breed_friesian",     nameEn: "Friesian",      nameTr: "Frizyen",       sortOrder: 15 },
    { id: "breed_pony",         nameEn: "Pony",          nameTr: "Midilli / Pony",sortOrder: 16 },
    { id: "breed_other",        nameEn: "Other",         nameTr: "Diğer",         sortOrder: 99 },
  ];

  await Promise.all(
    breeds.map((breed) =>
      db.collection("horse_breeds").doc(breed.id).set({
        ...breed,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true })
    )
  );
  console.log(`Seeded ${breeds.length} horse breeds.`);
}

async function seedChallenges() {
  const now = Date.now();
  const oneMonth = 30 * 24 * 60 * 60 * 1000;
  const challenges = [
    {
      id: "challenge_first_ride",
      title: "İlk Sürüş",
      titleEn: "First Ride",
      description: "İlk sürüşünü tamamla",
      descriptionEn: "Complete your first ride",
      targetValue: 1,
      unit: "rides",
      icon: "🏇",
      startDate: now,
      endDate: now + oneMonth * 3,
    },
    {
      id: "challenge_10km",
      title: "10 km Yol",
      titleEn: "10 km Journey",
      description: "Toplam 10 km sürüş tamamla",
      descriptionEn: "Complete 10 km total distance",
      targetValue: 10,
      unit: "km",
      icon: "🗺️",
      startDate: now,
      endDate: now + oneMonth * 2,
    },
    {
      id: "challenge_weekly_5",
      title: "Haftalık 5 Sürüş",
      titleEn: "Weekly 5 Rides",
      description: "Bu hafta 5 sürüş tamamla",
      descriptionEn: "Complete 5 rides this week",
      targetValue: 5,
      unit: "rides",
      icon: "🎯",
      startDate: now,
      endDate: now + 7 * 24 * 60 * 60 * 1000,
    },
    {
      id: "challenge_speed_20",
      title: "Hız Ustası",
      titleEn: "Speed Master",
      description: "Ortalama 20 km/h hıza ulaş",
      descriptionEn: "Reach average speed of 20 km/h",
      targetValue: 20,
      unit: "km/h",
      icon: "⚡",
      startDate: now,
      endDate: now + oneMonth,
    },
  ];

  await Promise.all(
    challenges.map((c) =>
      db.collection("challenges").doc(c.id).set({
        ...c,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true })
    )
  );
  console.log(`Seeded ${challenges.length} challenges.`);
}

async function main() {
  await seedBarns();
  await seedLessons();
  await seedAppContent();
  await seedHorseTips();
  await seedBreeds();
  await seedChallenges();
  console.log("Seed completed: barns, lessons, app_content, horse_tips, horse_breeds, challenges");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("Seed failed", error);
    process.exit(1);
  });
