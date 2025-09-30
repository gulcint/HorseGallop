# Screenshot Testing Guide

Bu proje **Paparazzi** kullanarak otomatik screenshot testing yapmaktadır.

## 🎯 Amaç

Her feature push edilmeden önce UI'ın değişmediğinden emin olmak için screenshot testleri kullanılır. Bu sayede:

- ✅ Yanlışlıkla UI değişikliklerini önleriz
- ✅ Visual regression'ları yakalarız
- ✅ Main branch ile feature branch'leri karşılaştırırız
- ✅ PR review sürecinde UI değişikliklerini görselleştiririz

## 📸 Screenshot Test Çalıştırma

### İlk Kez Snapshot Oluşturma

```bash
./gradlew recordPaparazziDebug
```

Bu komut tüm screenshot testlerini çalıştırır ve baseline snapshot'ları `src/test/snapshots/` klasörüne kaydeder.

### Screenshot Testlerini Doğrulama

```bash
./gradlew verifyPaparazziDebug
```

Bu komut mevcut UI'ı baseline snapshot'larla karşılaştırır. Eğer fark varsa test fail olur.

### Snapshot'ları Güncelleme

Eğer UI değişikliği kasıtlıysa (örneğin yeni bir feature eklediyseniz):

```bash
./gradlew recordPaparazziDebug
```

Yeni snapshot'ları commit etmeyi unutmayın!

## 🔄 Workflow

### Yeni Feature Eklerken

1. **Feature branch oluştur**
   ```bash
   git checkout -b feat/my-new-feature
   ```

2. **Kodu yaz ve screenshot test ekle**
   ```kotlin
   @Test
   fun myNewScreen_defaultState() {
       paparazzi.snapshot {
           MaterialTheme {
               MyNewScreen()
           }
       }
   }
   ```

3. **İlk snapshot'ı kaydet**
   ```bash
   ./gradlew recordPaparazziDebug
   ```

4. **Testleri çalıştır**
   ```bash
   ./gradlew verifyPaparazziDebug
   ```

5. **Commit ve push**
   ```bash
   git add .
   git commit -m "feat: add my new feature with screenshot tests"
   git push origin feat/my-new-feature
   ```

### Pull Request Öncesi

PR oluşturmadan önce MUTLAKA:

```bash
# Main branch'i pull et
git checkout main
git pull origin main

# Main'deki screenshot'ları kaydet (baseline)
./gradlew recordPaparazziDebug
mkdir -p /tmp/main-snapshots
cp -r */src/test/snapshots /tmp/main-snapshots/

# Feature branch'e dön
git checkout feat/my-new-feature

# Feature branch'teki screenshot'ları test et
./gradlew verifyPaparazziDebug
```

Eğer testler fail olursa:
- ✅ Değişiklik kasıtlıysa: `./gradlew recordPaparazziDebug` ile güncelle
- ❌ Değişiklik istenmediyse: Kodu düzelt

## 📁 Test Dosyaları

Screenshot testleri şu konumlarda:

```
feature_auth/src/test/java/com/example/feature_auth/ProfessionalLoginScreenTest.kt
feature_home/src/test/java/com/example/feature_home/HomeScreenTest.kt
```

## 🤖 GitHub Actions

Her PR'da otomatik olarak screenshot testleri çalışır:

- ✅ **Success**: UI değişikliği yok
- ❌ **Failure**: UI değişikliği var
  - Artifacts'dan screenshot diff'leri indir
  - İnceleyip kasıtlı değişiklik mi kontrol et
  - Kasıtlıysa: snapshot'ları güncelle ve commit et

## 📝 Best Practices

1. **Her önemli ekran için test yaz**
   - Login screen ✅
   - Home screen ✅
   - Detail screens
   - Dialog'lar
   - Bottom sheets

2. **Farklı state'ler için testler**
   ```kotlin
   @Test fun screen_loadingState()
   @Test fun screen_successState()
   @Test fun screen_errorState()
   @Test fun screen_emptyState()
   ```

3. **Snapshot'ları version control'e commit et**
   ```bash
   git add */src/test/snapshots/
   git commit -m "test: update screenshots"
   ```

4. **PR'da değişen screenshot'ları açıkla**
   PR description'da hangi ekranların değiştiğini ve neden değiştiğini belirt.

## 🐛 Troubleshooting

### Test çalışmıyor

```bash
./gradlew clean
./gradlew recordPaparazziDebug
```

### Snapshot'lar gitignore'da

`*/src/test/snapshots/` klasörünün `.gitignore`'da OLMADIĞINDAN emin olun.

### CI'da farklı sonuçlar

Paparazzi JVM'de çalıştığı için deterministik olmalı. Eğer CI'da farklı sonuç alıyorsanız:

1. Lokal Java versiyonunuzu kontrol edin (17 olmalı)
2. `./gradlew clean` yapın
3. Snapshot'ları yeniden kaydedin

## 📚 Daha Fazla Bilgi

- [Paparazzi Documentation](https://github.com/cashapp/paparazzi)
- [Screenshot Testing Best Practices](https://developer.android.com/training/testing/screenshot-testing)
