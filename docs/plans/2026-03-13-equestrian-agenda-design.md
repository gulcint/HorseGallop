# Equestrian Agenda Design

**Date:** 2026-03-13

**Goal:** Replace the obsolete TJK race module with an equestrian-focused agenda powered by `binicilik.org.tr`, and route federated club data into the existing Barns flows.

## Product Decision

- Remove the `TJK` concept entirely from the app, backend, strings, navigation, and domain naming.
- Introduce `EquestrianAgenda` as the replacement surface.
- `EquestrianAgenda` will contain two tabs only:
  - `Duyurular`
  - `Yarışmalar`
- Federated club data from `https://www.binicilik.org.tr/Kulup/11/Federe-kulupler` will not live inside `EquestrianAgenda`.
- Federated clubs will feed the existing `Barns`, `BarnDetail`, `BarnsMapView`, and ride-time `Select Barn` flows.

## Why This Structure

- `Kulüpler` is discovery and selection data, not newsfeed data.
- Users looking for a barn should land in `Barns`, not navigate through an agenda screen.
- `Duyurular` and `Yarışmalar` belong together because both are timely federation content.
- Reusing the current barn stack reduces product churn and implementation risk.

## Source Data

### 1. Duyurular

Source:
- `https://www.binicilik.org.tr/Anasayfa/Duyuruarsiv`

Observed structure:
- Server-rendered HTML
- Repeating `article.post` cards
- Each card exposes title, detail URL, summary snippet, and date block

Planned payload:
- `id`
- `title`
- `summary`
- `detailUrl`
- `publishedAtLabel`
- `imageUrl`

### 2. Yarışmalar

Source:
- `https://www.binicilik.org.tr/Anasayfa/Yarislar`
- detail pages under `/Yarisma/{id}/...`

Observed structure:
- Main list page contains a table of competitions
- Rows expose competition title, date range, and place
- Detail pages expose organizer, place, start/end, and optional program/result file links

Planned payload:
- `id`
- `title`
- `location`
- `dateLabel`
- `detailUrl`
- `organizer`
- `startDateLabel`
- `endDateLabel`
- `programPdfUrl`
- `resultLinks`

### 3. Federe Kulüpler

Source:
- `https://www.binicilik.org.tr/Kulup/11/Federe-kulupler`

Observed structure:
- Grid cards with club names and images
- Modal-like detail markup embedded in the same HTML
- Detail blocks expose address, phone, email, website, manager/president, and image

Planned payload:
- `id`
- `name`
- `location`
- `description`
- `heroImageUrl`
- `phone`
- `email`
- `website`
- `address`
- `city`
- `district`
- `lat`
- `lng`

## Backend Design

Implementation stays inside Firebase Cloud Functions and uses `cheerio`.

### New callable functions

- `getEquestrianAnnouncements`
- `getEquestrianCompetitions`
- `getFederatedBarns`
- `getFederatedBarnDetail`

### Scraping approach

- Fetch HTML with browser-like headers
- Parse with `cheerio`
- Map stable selectors into DTOs
- Normalize relative URLs to absolute `https://www.binicilik.org.tr/...`
- Fail with safe empty states rather than crashing on partial parse

### Caching

First implementation should use a light Firestore-backed cache:
- store last successful scrape payload
- attach `fetchedAt`
- return cached data when live scrape fails

Reason:
- federation HTML may change temporarily
- this reduces user-facing failures
- avoids scraping the source on every app refresh

## Android App Design

### Rename scope

The following `TJK` concepts should be renamed or removed:
- route names
- screen and view model file names
- package names
- string resource ids
- cloud function method names in `AppFunctionsDataSource`
- DTO and domain model names

Target naming:
- `EquestrianAgenda`
- `EquestrianAnnouncement`
- `EquestrianCompetition`

### Agenda screen

`EquestrianAgendaScreen` will replace the current TJK screen.

Behavior:
- top bar title: `Binicilik Gündemi`
- segmented tab row or tab row with:
  - `Duyurular`
  - `Yarışmalar`
- independent loading and empty states
- pull-to-refresh or explicit refresh action
- item tap opens external detail link if no native detail screen is added in the first pass

### Barns integration

The existing barn repository will switch from generic placeholder backend data to federated club data from Cloud Functions.

Affected flows:
- `BarnListScreen`
- `BarnDetailScreen`
- `BarnsMapViewScreen`
- ride `BarnSelector`
- favorite barn references on home/recent activity

## Mapping Decisions

### Barn descriptions

Federation club pages do not provide a clean marketing description.

First-pass fallback:
- build a concise description from city, district, address, phone, and website availability

### Barn tags and reviews

Federation source does not expose product-like amenities and reviews.

First-pass policy:
- keep tags empty unless inferable
- keep instructors/reviews empty
- avoid fake generated review content

### Coordinates

Federation source does not expose coordinates directly.

First-pass policy:
- if coordinates are unavailable in current backend data model, store sentinel-safe values and surface clubs in list/search/select flows first
- map experience can degrade gracefully until geocoding is introduced

## Error Handling

- If scrape succeeds: return fresh payload and refresh cache
- If scrape fails but cache exists: return cached payload with stale flag
- If both fail: return empty payload and user-safe error message
- Android UI should show empty-state copy specific to announcements, competitions, or barns

## Risks

- HTML structure on `binicilik.org.tr` may change without notice
- club pages have rich but inconsistent detail markup
- map features may be weak until reliable coordinates exist
- competition result files are sometimes missing, so result CTA must tolerate absent documents

## Out of Scope For First Pass

- Apify integration
- headless browser scraping
- automatic geocoding for all clubs
- native deep competition detail screen
- background scheduled refresh jobs beyond simple callable + cache flow

## Success Criteria

- No `TJK` branding or naming remains in the shipped app
- Home quick action opens `Binicilik Gündemi`
- `Binicilik Gündemi` loads federation `Duyurular` and `Yarışmalar`
- `Barns` and ride barn selection show federation club data from backend
- Empty and error states are usable when the federation source is unavailable
