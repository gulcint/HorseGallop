# Codebase Cleanup Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Gereksiz dosyaları sil, agent/context dosyalarını token verimli hale getir, docs çiftlerini birleştir.

**Architecture:** Saf silme/kısaltma işlemi — kod değişikliği yok, derleme gerekmez. Her task bağımsız, paralel çalıştırılabilir.

**Tech Stack:** Markdown, bash (git, rm, gitignore)

---

## Chunk 1: CLAUDE.md Agresif Kısaltma

**Files:**
- Modify: `CLAUDE.md`

**Hedef:** ~268 satır → ~140 satır. Kaldırılacaklar:
- "Implemented Features" listesi (MEMORY.md'de zaten var)
- "Context Topology" bölümü (agent-contracts.md'de zaten var, CLAUDE.md'de tekrar gerek yok)
- Agent Dispatch Protokolü örnek blokları (agent MD'lerinde zaten var)
- Agent Orchestration karar ağacı verbose açıklama kısmı → kompakt tut

- [ ] **Step 1: CLAUDE.md yeni halini yaz**

Aşağıdaki içerikle `CLAUDE.md`'yi tamamen yeniden yaz:

```markdown
# CLAUDE.md

## Session Start

- Read `memory.md` first, then this file.
- Multi-agent work: use `.claude/context/shared/agent-contracts.md` + task files under `.claude/context/tasks/<task-id>/`.
- Agent model: `Conductor (tech-lead) → Researcher / Builder / Operator → Reviewer (qa-verifier)`

## Build Commands

```bash
bash scripts/setup-hooks.sh        # one-time git hook setup
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew androidQualityConventions
bash scripts/pr-pipeline-merge.sh  # run local gate + open/update PR
```

Git hooks: `pre-commit` → conflict/syntax check | `pre-push` → lint + tests
Claude hooks: `PreToolUse` blocks dangerous commands | `PostToolUse` auto-runs Gradle | `SubagentStart` injects context paths

`enforceSemanticSurfaceTokens` runs on every `preBuild` — **build fails** on direct color usage in `feature/`, `core/`, `navigation/`. Use `LocalSemanticColors.current` tokens only.

## Agent Orchestration

**When to use agents:**
- Direct answer: question / debug / 1-2 file change
- Agents (tech-lead): new feature (3+ layers), cross-cutting refactor, ambiguous scope

**QA Gate (mandatory after every implementation):**
- `qa-verifier` PASS required before saying "done" / "ready" / "commit"
- FAIL → fix → re-run QA → then commit

**New multi-step task:**
```bash
python3 scripts/init_claude_task_context.py --task-id T-123 --title "Task Title"
```

## Architecture

**Single-module monolith** — only `:app`. All code under `com.horsegallop`.

```
domain/{feature}/model/         → Pure Kotlin data classes
domain/{feature}/repository/    → Interfaces
domain/{feature}/usecase/       → Single-responsibility business logic
data/{feature}/repository/      → Implements domain interfaces
data/remote/functions/          → AppFunctionsDataSource (all Firebase calls)
data/remote/dto/                → FunctionsDtos.kt
data/di/                        → DataModule, FirebaseModule, NetworkModule
feature/{feature}/presentation/ → Screen + ViewModel pairs
core/components/                → Shared Composables
navigation/AppNav.kt            → All routes + NavHost
ui/theme/                       → SemanticColors, Type, Theme
```

All remote calls: `AppFunctionsDataSource` → Firebase Cloud Functions.

DI: `@Binds @Singleton` in `DataModule`. ViewModels: `@HiltViewModel` + `@Inject constructor`.

Routes: sealed class `Dest` in `AppNav.kt`. Bottom nav: Home, Barns, Ride, Schedule, Profile.

## Design System

**Never use direct colors in `feature/`, `core/`, `navigation/`, `MainActivity.kt`.**

```kotlin
val semantic = LocalSemanticColors.current
// semantic.screenBase, .cardElevated, .cardSubtle, .cardStroke,
// .success, .warning, .destructive, .ratingStar, .panelOverlay
```

ViewModel state: one `UiState` data class, `StateFlow`, collect with `collectAsStateWithLifecycle()`.

## Firebase

- Functions region: `us-central1`
- App Check: `DebugAppCheckProviderFactory` (debug) / `PlayIntegrityAppCheckProviderFactory` (release)
- FCM channels: `general`, `reservation`, `lesson`
- Auth: Email/password + Google. Password reset deep link: `horsegallop.page.link/reset-password`

## Google Play Billing

`BillingManager` is `@Singleton`. Billing flow requires Activity — call `billingManager.launchBillingFlow(activity, productId)`. Product IDs: `horsegallop_pro_monthly`, `horsegallop_pro_yearly`.

## Compose Rules

1. `LazyColumn`/`LazyRow` — always `key()` + `contentType()`
2. Expensive calc → `remember()`
3. Derived state → `derivedStateOf()`
4. Lambdas → hoist or `remember { {} }`
5. Data classes → `@Stable`/`@Immutable` where possible
6. `contentDescription` — all interactive elements via `stringResource()`

## Key Constraints

- `stringResource()` cannot be called inside `LaunchedEffect` — pre-compute outside
- `@OptIn(ExperimentalMaterial3Api::class)` for `CenterAlignedTopAppBar`, `ModalBottomSheet`
- Turkish strings with apostrophes: `Pro\'ya Geç`
- Min SDK 24, Target SDK 34, JVM 17
```

- [ ] **Step 2: Dosyayı kaydet ve satır sayısını doğrula**

```bash
wc -l CLAUDE.md
```
Beklenen: ~130-145 satır (268'den ~%50 azalma)

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "chore: compress CLAUDE.md ~50% — remove redundant sections (features list, context topology, verbose dispatch examples)"
```

---

## Chunk 2: MEMORY.md Kısaltma

**Files:**
- Modify: `/Users/gulcintas/.claude/projects/-Users-gulcintas-HorseGallopProject-horsegallop/memory/MEMORY.md`

**Hedef:** Büyük "Recently Completed" ve "Implemented Features" detay listelerini özetle. ~40% azalma.

- [ ] **Step 1: Mevcut MEMORY.md'yi oku**

```bash
wc -l /Users/gulcintas/.claude/projects/-Users-gulcintas-HorseGallopProject-horsegallop/memory/MEMORY.md
```

- [ ] **Step 2: "Recently Completed" bölümünü kısalt**

`## Recently Completed (feat/elevation-chart-ride-autodetect-barn-fixes branch → PR #81)` bölümündeki bullet listesini tek paragrafla değiştir:

```markdown
## Recently Completed (merged to main)

PR #81 tamamlandı. At Sağlık Takvimi, Challenge/Rozet sistemi, Settings backend entegrasyonu,
B2B Ahır Yönetim Modu, Türkçe AI Koç (AiCoachScreen + Gemini 1.5 Flash), TJK→TBF rename,
Onboarding/Login UI düzeltmeleri, Robo test desteği, CodeRabbit kritik düzeltmeleri, BarnList hata düzeltmeleri.
```

- [ ] **Step 3: "Implemented Features (merged to main)" verbose alt-listesini kısalt**

`## Implemented Features (merged to main — includes worktree work)` bölümündeki tüm bullet listesini sil. Yerine:

```markdown
## Implemented Features (merged to main)

Gait color-coded polyline, speed smoothing, elevation tracking, horse calorie display,
gait distribution stats, all Cloud Functions (15+), BarnDetail reservation (real data),
Notifications (Firestore realtime), ride history pathPoints, Lesson/Barn DTOs, HorseTip categories,
Safety Tracking feature, ride auto-detect.
```

- [ ] **Step 4: Commit**

```bash
git add /Users/gulcintas/.claude/projects/-Users-gulcintas-HorseGallopProject-horsegallop/memory/MEMORY.md
git commit -m "chore: compress project memory — collapse verbose feature lists to summaries"
```

---

## Chunk 3: Agent MD Dosyaları Sadeleştirme

**Files:**
- Modify: `.claude/agents/conductor.md`
- Modify: `.claude/agents/ui-craft.md`
- Modify: `.claude/agents/firebase-backend.md`
- Modify: `.claude/agents/researcher.md`
- Modify: `.claude/agents/operator.md`

**Strateji:**
- `conductor.md` — `tech-lead.md` ile neredeyse aynı. Sadece header + "tech-lead kullan" referansına indirge.
- `firebase-backend.md` — `safetyContacts` koleksiyon referansını kaldır (Safety feature silindi).
- `ui-craft.md` — Empty State şablonunu kısalt, dark mode preview tekrarını kaldır.
- `researcher.md` + `operator.md` — zaten kısa, sadece context contract tekrarını kaldır.

### conductor.md → Redirect to tech-lead

- [ ] **Step 1: conductor.md'yi sadeleştir**

```markdown
---
name: conductor
description: |
  HorseGallop görev orkestrasyon agenti. tech-lead ile aynı role sahiptir.
  Çok adımlı görevlerde tech-lead agent'ını kullan.
tools:
  - Bash
  - Read
  - Edit
  - Write
  - Glob
  - Grep
  - Task
  - TodoWrite
---

Bu agent `tech-lead` ile eşdeğerdir. `.claude/agents/tech-lead.md` protokolünü uygula.

Context Contract: `.claude/context/shared/agent-contracts.md` dosyasını oku.
```

### firebase-backend.md → safetyContacts satırını kaldır

- [ ] **Step 2: firebase-backend.md'den safetyContacts satırını kaldır**

`| \`safetyContacts/{uid}/contacts\` | Güvenlik kişileri |` satırını sil.

### ui-craft.md → Empty State şablonunu kısalt

- [ ] **Step 3: ui-craft.md Empty State bölümünü kısalt**

`## Boş Durum (Empty State) Standardı` bölümündeki verbose Kotlin şablonunu kaldır. Yerine:

```markdown
## Boş Durum (Empty State) Standardı

`semantic.cardElevated` Surface içinde: 60dp icon daire + title (titleMedium/SemiBold) + subtitle (bodyMedium/onSurfaceVariant). Şablon için `core/components/` içindeki mevcut empty state composable'ları kullan.
```

- [ ] **Step 4: ui-craft.md dark mode preview tekrarını kaldır**

`@Preview(showBackground = true, uiMode = ...)` dark mode preview bloğunu kaldır. Açıklamayı tek satırla bırak:
```kotlin
// Opsiyonel: uiMode = UI_MODE_NIGHT_YES ile dark mode preview ekle
```

### researcher.md + operator.md → Context Contract tekrarını kaldır

- [ ] **Step 5: researcher.md context contract satırını sadeleştir**

`## Context Contract` bölümünü şuna indirge:
```markdown
## Context Contract
- `.claude/context/shared/agent-contracts.md` oku → dispatch mesajındaki handoff ve artifact path'lerini kullan.
- Kod yazma. Artifact dosyasına yaz.
```

- [ ] **Step 6: operator.md context contract satırını sadeleştir**

Aynı şekilde:
```markdown
## Context Contract
- `.claude/context/shared/agent-contracts.md` oku → dispatch mesajındaki handoff ve artifact path'lerini kullan.
- Operational değişikliklerde audit ve rollback göz önünde bulundur.
```

- [ ] **Step 7: Commit**

```bash
git add .claude/agents/
git commit -m "chore: simplify agent MDs — remove redundant context contracts, safety refs, verbose templates"
```

---

## Chunk 4: Docs Birleştirme

**Files:**
- Delete: `docs/plans/2026-03-13-equestrian-agenda-design.md`
- Delete: `docs/plans/2026-03-14-realtime-notifications-design.md`
- Delete: `docs/plans/2026-03-14-ride-detail-polyline-design.md`
- Modify: `docs/plans/2026-03-13-equestrian-agenda.md`
- Modify: `docs/plans/2026-03-14-realtime-notifications.md`
- Modify: `docs/plans/2026-03-14-ride-detail-polyline.md`

**Strateji:** Her implementation plan dosyasının başına "Status: COMPLETED" ekle ve gereksiz detayları kısalt. Design dosyalarını sil (implementation dosyası yeterli).

- [ ] **Step 1: Her implementation plan dosyasının başına COMPLETED header ekle**

`docs/plans/2026-03-13-equestrian-agenda.md` dosyasının en başına ekle:
```markdown
> **Status: COMPLETED** — Merged to main. Bu doküman referans amaçlıdır.
```

Aynısını şu dosyalara da ekle:
- `docs/plans/2026-03-14-realtime-notifications.md`
- `docs/plans/2026-03-14-ride-detail-polyline.md`

- [ ] **Step 2: Design dosyalarını sil**

```bash
rm docs/plans/2026-03-13-equestrian-agenda-design.md
rm docs/plans/2026-03-14-realtime-notifications-design.md
rm docs/plans/2026-03-14-ride-detail-polyline-design.md
```

- [ ] **Step 3: product-logic-fixes.md'i arşivle**

```bash
mkdir -p docs/archive
mv docs/superpowers/plans/2026-03-17-product-logic-fixes.md docs/archive/
```

- [ ] **Step 4: Commit**

```bash
git add docs/
git commit -m "chore: archive completed plan docs — delete design duplicates, add COMPLETED headers"
```

---

## Chunk 5: Genel Temizlik

**Files:**
- Modify: `.gitignore`
- Delete: `.claude/worktrees/competent-lamarr/` (stale)
- Truncate: `.claude/logs/hooks.jsonl`

- [ ] **Step 1: .gitignore'a .DS_Store ekle**

`.gitignore` dosyasını aç ve şu satırların var olduğunu kontrol et, yoksa ekle:
```
# macOS
.DS_Store
**/.DS_Store
```

- [ ] **Step 2: Var olan .DS_Store dosyalarını git tracking'den kaldır**

```bash
find . -name ".DS_Store" -not -path "./.git/*" | head -5
git rm --cached $(find . -name ".DS_Store" -not -path "./.git/*") 2>/dev/null || true
```

- [ ] **Step 3: Stale worktree state'i sil**

```bash
ls .claude/worktrees/
rm -rf .claude/worktrees/competent-lamarr/
```

- [ ] **Step 4: Hook loglarını truncate et (son 5 entry tut)**

```bash
tail -5 .claude/logs/hooks.jsonl > .claude/logs/hooks.jsonl.tmp
mv .claude/logs/hooks.jsonl.tmp .claude/logs/hooks.jsonl
```

- [ ] **Step 5: Commit**

```bash
git add .gitignore
git commit -m "chore: add .DS_Store to gitignore, remove stale worktree state, trim hook logs"
```

---

## Özet Metrikleri (Beklenen)

| Dosya | Önce | Sonra | Tasarruf |
|-------|------|-------|----------|
| CLAUDE.md | ~268 satır | ~140 satır | ~48% |
| MEMORY.md | ~200 satır | ~120 satır | ~40% |
| .claude/agents/ (8 dosya) | ~630 satır | ~480 satır | ~24% |
| docs/plans/ | 6 dosya | 3 dosya | 3 dosya silindi |
| .DS_Store | 40+ dosya | 0 | temizlendi |
| hooks.jsonl | 11 KB | <1 KB | truncated |
