# Agent Contracts

## Canonical Handoff Model

Task dispatch mesajlari kisa olur. Gercek baglam her zaman dosya path'lerinde yasar.

Her subagent su sirayla hareket eder:

1. `.claude/context/shared/agent-contracts.md` dosyasini oku
2. Dispatch mesajinda verilen task-specific path'leri oku
3. Yalnizca ilgili kod path'lerinde calis
4. Sonucunu dispatch mesajinda verilen artifact veya report path'ine yaz

## Task Dizini

Her gorev icin:

- `.claude/context/tasks/<task-id>/brief.md`
- `.claude/context/tasks/<task-id>/state.json`
- `.claude/context/tasks/<task-id>/decision-log.md`
- `.claude/context/tasks/<task-id>/open-questions.md`
- `.claude/context/tasks/<task-id>/handoffs/<agent-name>.md`
- `.claude/context/tasks/<task-id>/artifacts/<agent-name>.md`
- `.claude/context/tasks/<task-id>/artifacts/index.md`
- `.claude/context/tasks/<task-id>/reports/qa.md`

## Yazma Kurallari

- `tech-lead`: handoff dosyalari ve `state.json`
- `conductor`/`tech-lead`: handoff dosyalari, `state.json`, `decision-log.md`, `open-questions.md`, `artifacts/index.md`
- `researcher`: kendi artifact dosyasi
- `android-feature`: kendi artifact dosyasi
- `ui-craft`: kendi artifact dosyasi
- `firebase-backend`: kendi artifact dosyasi
- `operator`: kendi artifact dosyasi
- `qa-verifier`: `reports/qa.md`

## Rol Modeli

- `Conductor`: isi alir, planlar, dekompoze eder, sira ve bagimliliklari yonetir
- `Researcher`: repo, docs, issue, API ve web arastirmasi toplar
- `Builder`: kod veya workflow uretir (`android-feature`, `firebase-backend`, `ui-craft`)
- `Reviewer`: uretimi dogrular (`qa-verifier`)
- `Operator`: hook, deploy, notification, retry, logging gibi operational yuzeyi kurar
- `Memory/State`: task klasorundeki state ve karar kayitlari

## Yasaklar

- Baska agent handoff dosyasini dogrudan degistirmek
- Dispatch mesajinda verilmemis path'leri task state yerine kullanmak
- Sohbet gecmisine guvenip missing context uydurmak
