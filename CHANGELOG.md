# Changelog

## [Unreleased] - 2025-09-30

### ✨ Yeni Özellikler

#### 🏗️ Proje Yapısı
- **Multi-modül Clean Architecture** yapısı kuruldu
  - `:app` - Ana uygulama modülü
  - `:core` - Paylaşılan utility ve UI bileşenleri
  - `:domain` - İş mantığı ve use case'ler
  - `:data` - Repository implementasyonları ve API servisleri
  - `:feature_auth` - Kimlik doğrulama özellikleri
  - `:feature_home` - Ana sayfa ve slider
  - `:feature_schedule` - Ders programı
  - `:feature_profile` - Kullanıcı profili
  - `:feature_settings` - Uygulama ayarları
  - `:feature_reservation`, `:feature_orders`, `:feature_reviews`, `:feature_admin` - Gelecek özellikler

#### 🎨 UI/UX
- **Material 3** teması entegre edildi
- **Bottom Navigation** ile ana ekranlar arası geçiş
  - Ana Sayfa
  - Program
  - Profil
  - Ayarlar
- **Splash Screen** animasyonlu at logosu ile
- **Profil Ekranı**
  - Kullanıcı bilgileri gösterimi
  - Avatar desteği (Coil ile)
  - Çıkış yapma fonksiyonu
- **Ayarlar Ekranı**
  - Dil seçimi (Türkçe/English)
  - Bildirim ayarları
  - Hakkında bilgileri

#### 🌍 Çoklu Dil Desteği
- `strings.xml` ile Türkçe ve İngilizce dil desteği
- `Accept-Language` header ile backend'den dinamik çeviri
- Backend'den gelen tüm metinler cihaz diline göre

#### 🔧 Teknik Alt Yapı
- **Hilt** dependency injection
- **Jetpack Compose** ile modern UI
- **Navigation Component** ile sayfa yönetimi
- **Kotlin Coroutines** ve **Flow** ile asenkron işlemler
- **Retrofit** ve **OkHttp** ile API iletişimi
- **Room** ile local veritabanı hazırlığı
- **Firebase** entegrasyonu hazırlığı
- **Gradle Version Catalogs** ile merkezi dependency yönetimi

#### 📦 Build ve Deployment
- **Gradle 8.7** ile build yapılandırması
- **Kotlin 1.9.25** versiyonu
- **Java 17** uyumluluğu
- **Debug APK** başarıyla oluşturuluyor (12MB)

### 🐛 Düzeltmeler
- Gradle build hatalarının tümü giderildi
- AndroidManifest dosyaları tüm modüllere eklendi
- Material3 icon uyumluluk sorunları düzeltildi
- JVM target uyumsuzlukları giderildi
- Hilt DI bağlantı sorunları çözüldü

### 📝 Dokümantasyon
- `LOCALIZATION.md` - Çoklu dil stratejisi
- `BUILD_INSTRUCTIONS.md` - Build talimatları
- `FEATURES_TODO.md` - Gelecek özellikler listesi

### 🔄 Sonraki Adımlar
- [ ] Firebase Authentication entegrasyonu (Google/Apple Sign-In)
- [ ] API servislerinin tamamlanması
- [ ] Restaurant sipariş sistemi
- [ ] Ders rezervasyon sistemi
- [ ] Push notification entegrasyonu
- [ ] Admin paneli
- [ ] Unit ve UI testleri
- [ ] CI/CD pipeline (GitHub Actions)

---

## Teknoloji Stack

### Frontend
- Kotlin 1.9.25
- Jetpack Compose (Material 3)
- Navigation Component
- Hilt (Dependency Injection)
- Coil (Image Loading)
- Coroutines & Flow

### Backend Hazırlığı
- Retrofit 2.9.0
- OkHttp 4.12.0
- Moshi (JSON parsing)
- Room (Local DB)

### Build & Tools
- Gradle 8.7
- Android Gradle Plugin 8.5.2
- Kotlin Compiler 1.9.25
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Java 17

### Firebase
- Firebase Auth
- Firebase Cloud Messaging (FCM)

---

## Git Branch Yapısı

### feat/dynamic-strings-xml
- Tüm temel özellikler bu branch'te geliştirildi
- Build başarılı, APK oluşturuldu
- Production-ready base structure

---

## Katkıda Bulunanlar

Geliştirici: @gulcint
Proje: adincountry - At Binicilik Uygulaması
