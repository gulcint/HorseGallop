# Equestrian Agenda Implementation Plan

> **Status: COMPLETED** — Merged to main. Bu doküman referans amaçlıdır.

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the legacy TJK flow with a two-tab equestrian agenda backed by federation scraping, and feed federated club data into existing Barns flows.

**Architecture:** Keep scraping inside Firebase Cloud Functions using `cheerio`, expose normalized callable APIs, and update Android data/domain/UI layers to consume the new agenda and barn payloads. Reuse the existing barn architecture where possible, but fully remove `TJK` naming to avoid long-term technical debt.

**Tech Stack:** Firebase Cloud Functions, TypeScript, cheerio, Firestore cache, Kotlin, Hilt, Jetpack Compose, Firebase Functions client

---

### Task 1: Add shared federation scraping helpers in backend

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/backend/src/index.ts`
- Test: `/Users/gulcintas/HorseGallopProject/horsegallop/backend/package.json`

**Step 1: Write the helper signatures**

Add small helpers for:
- absolute URL normalization
- safe text extraction
- cache read/write

**Step 2: Run backend typecheck baseline**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

Expected:
- current backend compiles before scraper changes

**Step 3: Implement the minimal helpers**

Keep helpers local to `index.ts` unless file extraction is clearly useful.

**Step 4: Run backend build again**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

Expected:
- TypeScript build passes

### Task 2: Implement `getEquestrianAnnouncements`

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/backend/src/index.ts`

**Step 1: Add DTO/interface definitions**

Define:
- `EquestrianAnnouncementEntry`
- response wrapper if needed

**Step 2: Implement the announcement scraper**

Parse:
- title
- summary
- detail link
- image
- date label

**Step 3: Add Firestore cache fallback**

Cache key example:
- `federation_cache/announcements`

**Step 4: Export callable**

Export:
- `getEquestrianAnnouncements`

**Step 5: Run backend build**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

Expected:
- build passes

### Task 3: Implement `getEquestrianCompetitions`

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/backend/src/index.ts`

**Step 1: Add competition DTO/interface definitions**

Define:
- `EquestrianCompetitionEntry`
- `EquestrianCompetitionResultLink`

**Step 2: Implement list page scraper**

Parse rows from `Anasayfa/Yarislar`:
- id
- title
- date label
- location
- detail link

**Step 3: Implement detail enrichment**

From detail pages parse:
- organizer
- place
- start/end labels
- program pdf
- result files if present

**Step 4: Add cache fallback**

Cache key example:
- `federation_cache/competitions`

**Step 5: Export callable**

Export:
- `getEquestrianCompetitions`

**Step 6: Run backend build**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

Expected:
- build passes

### Task 4: Replace barn backend with federated club data

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/backend/src/index.ts`

**Step 1: Inspect current barn callable outputs**

Confirm current shape of:
- `getBarns`
- `getBarnDetail`

**Step 2: Implement federated club scraper**

Parse from `Kulup/11/Federe-kulupler`:
- id
- name
- image
- phone
- address
- email
- website

**Step 3: Map scraped clubs into existing barn response shape**

Keep Android churn low by preserving current barn DTO contract where possible.

**Step 4: Replace or adapt `getBarns` and `getBarnDetail`**

Decision:
- keep callable names stable for Android
- change data source underneath

**Step 5: Add stale cache fallback**

Cache keys:
- `federation_cache/barns`
- `federation_cache/barn_details`

**Step 6: Run backend build**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

Expected:
- build passes

### Task 5: Remove TJK DTOs and add agenda DTOs in Android data layer

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/remote/dto/FunctionsDtos.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/remote/functions/AppFunctionsDataSource.kt`

**Step 1: Add new DTOs**

Create DTOs for:
- announcements
- competitions

**Step 2: Add callable client methods**

Add:
- `getEquestrianAnnouncements()`
- `getEquestrianCompetitions()`

**Step 3: Delete TJK client methods only after replacements compile**

Remove:
- `getTjkRaceDay`
- `getTjkCities`

**Step 4: Run app compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- compile may fail in downstream call sites until later tasks finish

### Task 6: Replace TJK domain layer with agenda domain layer

**Files:**
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/equestrian/model/EquestrianModels.kt`
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/equestrian/repository/EquestrianAgendaRepository.kt`
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/equestrian/usecase/GetEquestrianAnnouncementsUseCase.kt`
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/equestrian/usecase/GetEquestrianCompetitionsUseCase.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/tjk/model/TjkModels.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/tjk/repository/TjkRepository.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/tjk/usecase/GetTjkCitiesUseCase.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/domain/tjk/usecase/GetTjkRaceDayUseCase.kt`

**Step 1: Add minimal agenda domain models**

**Step 2: Add repository interface**

**Step 3: Add two use cases**

**Step 4: Remove TJK domain files**

**Step 5: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- remaining errors only in repository/UI wiring

### Task 7: Replace TJK repository implementation with agenda repository

**Files:**
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/equestrian/repository/EquestrianAgendaRepositoryImpl.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/di/DataModule.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/tjk/repository/TjkRepositoryImpl.kt`

**Step 1: Map data DTOs to domain models**

**Step 2: Bind repository in Hilt**

**Step 3: Remove TJK binding**

**Step 4: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- repository layer compiles

### Task 8: Build `EquestrianAgendaViewModel`

**Files:**
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/equestrian/presentation/EquestrianAgendaViewModel.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/tjk/presentation/TjkRacesViewModel.kt`

**Step 1: Define UI state**

Include:
- selected tab
- loading announcements
- loading competitions
- lists
- error state per tab or shared error message

**Step 2: Load both feeds**

Keep refreshable and independently resilient.

**Step 3: Add tab switching and refresh actions**

**Step 4: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- view model compiles, screen still pending

### Task 9: Replace TJK screen with `EquestrianAgendaScreen`

**Files:**
- Create: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/equestrian/presentation/EquestrianAgendaScreen.kt`
- Delete: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/tjk/presentation/TjkRacesScreen.kt`

**Step 1: Build scaffold and tab row**

Tabs:
- announcements
- competitions

**Step 2: Build announcements list**

Show:
- title
- summary
- date
- open-link action if available

**Step 3: Build competitions list**

Show:
- title
- location
- date
- organizer when present
- program/result CTA links when present

**Step 4: Add loading, empty, and error states**

**Step 5: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- agenda screen compiles

### Task 10: Rename navigation and home entry point

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/navigation/AppNav.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/home/presentation/HomeScreen.kt`

**Step 1: Replace `Dest.TjkRaces` with `Dest.EquestrianAgenda`**

**Step 2: Replace imports and composable destination**

**Step 3: Rename quick action strings and click action**

**Step 4: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- no unresolved TJK navigation references remain

### Task 11: Update strings and remove TJK copy

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values/strings_core.xml`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-tr/strings.xml`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-en/strings.xml`

**Step 1: Replace quick action and screen strings**

**Step 2: Remove onboarding TJK references**

Replace with:
- federation announcements
- competition calendar

**Step 3: Add agenda empty/error strings**

**Step 4: Validate XML**

Run:
- `xmllint --noout /Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values/strings_core.xml`
- `xmllint --noout /Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-tr/strings.xml`
- `xmllint --noout /Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-en/strings.xml`

Expected:
- XML files parse cleanly

### Task 12: Feed federated clubs into Barn repository

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/barn/repository/BarnRepositoryImpl.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/remote/functions/AppFunctionsDataSource.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/data/remote/dto/FunctionsDtos.kt`

**Step 1: Preserve barn contract while mapping federated fields**

Map:
- name
- address into `location`
- image
- phone/email/site into description or metadata

**Step 2: Keep empty arrays for reviews/instructors**

**Step 3: Ensure ride selector still receives valid items**

**Step 4: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- barn and ride flows compile against federated data

### Task 13: Polish Barn UI for federated club content

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/barn/presentation/BarnListScreen.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/barn/presentation/BarnDetail.kt`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingScreen.kt`

**Step 1: Ensure list cards read well with federation names**

**Step 2: Make detail screen degrade gracefully when reviews/tags are empty**

**Step 3: Ensure select-barn dialog uses real federated names and locations**

**Step 4: Run compile check**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

Expected:
- UI compiles without relying on fake barn metadata

### Task 14: Remove dead TJK references from memory and docs

**Files:**
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/memory.md`
- Modify: `/Users/gulcintas/HorseGallopProject/horsegallop/CLAUDE.md`

**Step 1: Replace TJK backlog references with federation/equestrian agenda wording**

**Step 2: Remove stale onboarding/product notes that mention TJK**

**Step 3: Verify search is clean**

Run: `rg -n \"\\bTJK\\b|\\btjk\\b\" /Users/gulcintas/HorseGallopProject/horsegallop`

Expected:
- only acceptable historical references remain, or none

### Task 15: Full verification pass

**Files:**
- Verify only

**Step 1: Backend build**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop/backend && npm run build`

**Step 2: Android compile**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew :app:compileDebugKotlin`

**Step 3: Android gate if environment is ready**

Run: `cd /Users/gulcintas/HorseGallopProject/horsegallop && ./gradlew lintDebug testDebugUnitTest`

Expected:
- pass if required local secrets and config files are present

**Step 4: Manual grep verification**

Run:
- `rg -n \"\\bTJK\\b|\\btjk\\b\" /Users/gulcintas/HorseGallopProject/horsegallop`
- `rg -n \"EquestrianAgenda|Binicilik Gündemi|Binicilik Gundemi\" /Users/gulcintas/HorseGallopProject/horsegallop`

Expected:
- TJK references removed
- new agenda references present
