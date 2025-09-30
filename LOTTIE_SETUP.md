# 🐴 Lottie Animation Setup

## 📁 Horse JSON Dosyası Ekleme

Lütfen at animasyonu için Lottie JSON dosyasını şu klasöre ekleyin:

```
app/src/main/res/raw/horse.json
```

### Alternatif Kaynaklar

Eğer kendi `horse.json` dosyanız yoksa, şu kaynaklardan ücretsiz at animasyonu indirebilirsiniz:

1. **LottieFiles** (Önerilen)
   - https://lottiefiles.com/
   - Arama: "horse animation"
   - JSON formatında indirin

2. **Popüler At Animasyonları**
   - https://lottiefiles.com/animations/horse
   - https://lottiefiles.com/animations/galloping-horse
   - https://lottiefiles.com/animations/cute-horse

### Manuel Ekleme Adımları

1. Lottie JSON dosyasını indirin
2. Dosya adını `horse.json` olarak değiştirin
3. `app/src/main/res/raw/` klasörüne kopyalayın
4. Projeyi yeniden build edin

```bash
./gradlew clean :app:assembleDebug
```

## 🎨 Animasyon Özellikleri

- **Süre**: 2 saniye (ayarlanabilir)
- **Tekrar**: Sonsuz döngü
- **Boyut**: Ekranı doldurur
- **Konum**: Merkez

## 🔧 Özelleştirme

### Animasyon Süresini Değiştirme

`MainActivity.kt` dosyasında:

```kotlin
LaunchedEffect(Unit) {
    delay(2000) // 2 saniye -> 3000 için 3 saniye
    showLottie = false
}
```

### Tek Seferlik Oynatma

```kotlin
val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = 1 // Tek sefer
)
```

### Animasyon Hızını Değiştirme

```kotlin
val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever,
    speed = 1.5f // 1.5x hızlı
)
```

## ✅ Test

1. APK'yı build edin
2. Emülatörde çalıştırın
3. Uygulama açılır açılmaz at animasyonu gösterilmeli
4. 2 saniye sonra ana ekrana geçmeli
5. Freeze olmadan düzgün çalışmalı

---

**Not**: Bu branch (`fix/splash-screen-freeze`) splash screen freeze sorununu çözmek için oluşturulmuştur.
