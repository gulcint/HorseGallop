# 🌿 Branch Stratejisi

## Ana Branch'ler

### `main`
- Production-ready kod
- Merge edilen tüm feature'lar
- Her zaman çalışır durumda

### `develop` (Opsiyonel)
- Geliştirilmekte olan özellikler
- Feature branch'lerinin merge hedefi

## Feature Branch Yapısı

Her yeni özellik için ayrı branch:

### Mevcut Branch'ler

1. **`feat/dynamic-strings-xml`** ✅ Tamamlandı
   - Dynamic localization
   - Strings.xml yapısı
   - Accept-Language header

2. **`fix/splash-screen-freeze`** 🔄 Aktif
   - Splash screen freeze sorunu
   - Lottie animasyon entegrasyonu
   - 2 saniye splash delay

### Gelecek Feature Branch'ler

3. **`feat/bottom-navigation`**
   - Bottom navigation bar
   - Profile screen
   - Settings screen
   - 4 ana sekme

4. **`feat/firebase-auth`**
   - Google Sign-In
   - Apple Sign-In
   - Email/Password auth
   - JWT token yönetimi

5. **`feat/home-slider`**
   - Image slider component
   - Auto-scroll
   - Indicator dots
   - API entegrasyonu

6. **`feat/lesson-schedule`**
   - Ders listesi
   - Filtreleme
   - Detay sayfası
   - Rezervasyon butonu

7. **`feat/reservation-system`**
   - Rezervasyon formu
   - Tarih/saat seçici
   - Eğitmen seçimi
   - Onay ekranı

8. **`feat/restaurant-orders`**
   - Menü listesi
   - Sepet yönetimi
   - Sipariş takibi
   - Ödeme entegrasyonu

9. **`feat/reviews-ratings`**
   - Yorum listesi
   - Rating sistemi
   - Yorum ekleme
   - Like/Dislike

10. **`feat/admin-panel`**
    - Kullanıcı yönetimi
    - İçerik yönetimi
    - İstatistikler
    - Bildirim gönderimi

11. **`feat/push-notifications`**
    - FCM entegrasyonu
    - Notification permissions
    - Background handler
    - Deep linking

12. **`feat/offline-mode`**
    - Room database
    - Caching stratejisi
    - Sync mechanism
    - Offline UI

## Branch İsimlendirme Kuralları

### Feature
```
feat/feature-name
Örnek: feat/google-sign-in
```

### Bug Fix
```
fix/bug-description
Örnek: fix/splash-screen-freeze
```

### Refactor
```
refactor/what-changed
Örnek: refactor/repository-pattern
```

### Documentation
```
docs/doc-name
Örnek: docs/api-documentation
```

### Performance
```
perf/improvement
Örnek: perf/image-loading-optimization
```

## Workflow

### 1. Yeni Feature Başlatma

```bash
git checkout main
git pull origin main
git checkout -b feat/new-feature
```

### 2. Geliştirme

```bash
# Değişiklikleri commit et
git add .
git commit -m "feat: add new feature description"
```

### 3. Push ve PR

```bash
# Branch'i push et
git push origin feat/new-feature

# GitHub'da Pull Request oluştur
# main <- feat/new-feature
```

### 4. Merge Sonrası

```bash
# Main'e geri dön
git checkout main
git pull origin main

# Eski branch'i sil
git branch -d feat/new-feature
git push origin --delete feat/new-feature
```

## Commit Message Kuralları

### Format
```
<type>: <short description>

[optional body]
[optional footer]
```

### Types

- `feat`: Yeni özellik
- `fix`: Bug fix
- `docs`: Dokümantasyon
- `style`: Formatting, semicolons
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `test`: Test ekleme
- `chore`: Build, dependencies

### Örnekler

```bash
feat: add Google Sign-In authentication
fix: resolve splash screen freeze issue
docs: update README with setup instructions
refactor: reorganize repository structure
perf: optimize image loading with Coil
test: add unit tests for HomeViewModel
```

## Pull Request Template

```markdown
## Açıklama
Bu PR ne yapıyor?

## Değişiklikler
- [ ] Yeni feature eklendi
- [ ] Bug düzeltildi
- [ ] Dokümantasyon güncellendi

## Test
Nasıl test edildi?

## Screenshots
(Varsa ekran görüntüleri)

## Checklist
- [ ] Kod lint'ten geçti
- [ ] Build başarılı
- [ ] Test edildi
- [ ] Dokümantasyon güncellendi
```

---

**Not**: Her feature bağımsız olarak geliştirilebilir ve test edilebilir olmalı.
