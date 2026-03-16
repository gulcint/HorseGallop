---
name: require-qa-verifier
enabled: true
event: stop
pattern: .*
---

🔍 **QA VERIFIER ÇALIŞTI MI?**

İmplementasyon tamamlanmadan önce `qa-verifier` agent'ı çalıştırılmalı.

**Kontrol listesi:**
- [ ] `qa-verifier` agent çalıştırıldı mı?
- [ ] Build SUCCESSFUL mu? (`./gradlew lintDebug testDebugUnitTest`)
- [ ] SemanticColors ihlali yok mu?
- [ ] Gereksinimler karşılandı mı?

**qa-verifier çalıştırılmadan "tamamlandı" denilemez.**
