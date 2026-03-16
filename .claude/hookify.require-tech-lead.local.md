---
name: require-tech-lead
enabled: true
event: prompt
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: (?i)(ekle|implement|yaz|düzelt|fix|feature|geliştir|refactor|yeni|ekran|screen|entegre|api|backend|fonksiyon|button|build|tamamla|yapı|oluştur)
---

🎼 **SENFONI KURALI: Tech-Lead Önce Çalışır**

Bu görev bir feature, fix veya refactor içeriyor. Direkt `android-feature`, `firebase-backend`, `ui-craft` veya `qa-verifier` çağırma.

**Zorunlu sıra:**
1. `tech-lead` → görevi analiz et, katmanlara böl, hangi agent'ların çalışacağını belirle
2. `android-feature` / `firebase-backend` / `ui-craft` → tech-lead dispatch edince çalışır
3. `qa-verifier` → her implementasyondan sonra PASS/FAIL raporu

**Tech-lead'i atlamak kaliteyi düşürür. İstisna yok.**
