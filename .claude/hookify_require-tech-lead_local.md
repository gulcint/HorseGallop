---
name: require-tech-lead
enabled: true
event: prompt
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: (?i)(yeni ekran|new screen|yeni feature|yeni özellik|entegre et|entegrasyon|refactor|yeniden yaz|rewrite|migrasyón|tüm katman|full implementation|sprint)
---

🎼 **SENFONI KURALI — Tech-Lead Önce Çalışır**

Bu görev bir **feature, entegrasyon veya refactor** içeriyor.

**Önce complexity gate'i değerlendir:**
- Tek dosya / küçük değişiklik mi? → Direkt yap, sprint açma
- Birden fazla katman etkileniyorsa → Aşağıdaki sırayı takip et

**Zorunlu sprint sırası:**
1. `tech-lead` → analiz, dekompoze, dispatch
2. `android-feature` / `firebase-backend` / `ui-craft` → tech-lead'den Task alarak çalışır
3. `qa-verifier` → PASS/FAIL raporu — PASS olmadan commit önerilmez

**Not:** Soru, açıklama, tek satır fix → bu hook geçerli değil, direkt cevap ver.
