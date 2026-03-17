---
name: require-qa-verifier
enabled: true
event: stop
pattern: .*
---

🔍 **QA VERIFIER ZORUNLU**

Bir implementasyon tamamlandıysa `qa-verifier` agent çalıştırılmadan **"tamamlandı" denemez.**

**Kontrol:**
- Bu yanıt bir kod değişikliği / implementasyon içeriyor mu?
  - **Evet →** `qa-verifier` çalıştır, raporu bekle
  - **Hayır (soru/açıklama/tek satır fix) →** Bu hook geçerli değil, devam et

**qa-verifier çalıştırılmadan verilebilecek tek yanıt:**
> "qa-verifier'ı başlatıyorum..." veya zaten PASS/FAIL raporu mevcut

**PASS olmadan söylenemeyecekler:**
- "Tamamlandı"
- "Hazır"  
- "Implement edildi"
- "Commit edebilirsin"
