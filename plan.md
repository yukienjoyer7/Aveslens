# AvesLens — Development Plan

## Overview
Android bird observation journal app with automatic species classification via ML.
Built with Kotlin + Jetpack Compose. Backend: Supabase (Auth, Database, Storage).
Classification: Hugging Face Inference API.

---

## Tech Stack

| Layer | Library / Service |
|---|---|
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| Navigation | Navigation Compose (single Activity) |
| DI | Hilt |
| Backend | Supabase Kotlin SDK |
| Image Loading | Coil |
| ML | Custom FastAPI on HF Space (`crtal-aveslens-model-api`) |
| Database | Supabase (PostgreSQL) |
| Storage | Supabase Storage |
| Auth | Supabase Auth |
| Language | Kotlin |

**ML API base URL:** `https://crtal-aveslens-model-api.hf.space`
**Model:** Swin Tiny (ONNX, CPU), 525 bird species, ~97% val accuracy

---

## Project Structure

```
app/
└── src/main/
    ├── data/
    │   ├── model/          # Data classes (Observation, Species, Profile, AuditLog)
    │   ├── repository/     # Repository interfaces + implementations
    │   └── remote/
    │       ├── supabase/   # Supabase client + DAO-style functions
    │       └── aveslens/   # AvesLens model API service (OkHttp multipart)
    ├── di/                 # Hilt modules
    ├── ui/
    │   ├── theme/          # MaterialTheme, colors, typography
    │   ├── navigation/     # NavGraph, Routes, NavHost
    │   └── screens/
    │       ├── auth/       # AuthScreen + AuthViewModel
    │       ├── home/       # HomeScreen + HomeViewModel
    │       ├── form/       # FormScreen + FormViewModel
    │       ├── detail/     # DetailScreen + DetailViewModel
    │       ├── profile/    # ProfileScreen + ProfileViewModel
    │       └── explorer/   # ExplorerScreen + ExplorerViewModel
    └── util/               # Extensions, constants, helpers
```

---

## Database Schema (Summary)

Fully defined in `245150701111011_Dionisius_Seraf_Saputra.sql`. Tables:
- `profiles` — extends `auth.users`, stores public user info
- `species` — static lookup table for bird species
- `bird_observations` — main CRUD table, stores ML output + image URL
- `audit_logs` — auto-populated via PostgreSQL TRIGGER on `bird_observations`

Run the SQL file on Supabase SQL Editor before starting development.

---

## Build Phases

Each phase ends with a runnable app. Complete all checkboxes before moving on.

---

### Phase 1 — Project Scaffold
**Deliverable:** App launches to a blank screen with no crashes.

- [ ] Create Android project (Kotlin, minSdk 31, Jetpack Compose)
- [ ] Add Gradle dependencies: Hilt, Supabase Kotlin SDK, Coil, OkHttp, Navigation Compose, Google Fonts
- [ ] Configure `local.properties` + `build.gradle` to expose `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `AVESLENS_MODEL_URL` via `BuildConfig`
- [ ] Create `Color.kt`, `Type.kt` (Manrope + Inter via `GoogleFonts`), `Shape.kt` from design tokens
- [ ] Wire `MaterialTheme` in `MainActivity`
- [ ] Create Supabase client singleton (`SupabaseClient` Hilt module)
- [ ] Create `NavGraph` with all routes stubbed as empty `composable {}` blocks: `auth`, `home`, `form`, `detail/{id}`, `profile`, `explorer`
- [ ] Set up Hilt `@HiltAndroidApp` on `Application`, `@AndroidEntryPoint` on `MainActivity`

---

### Phase 2 — Auth Screen
**Deliverable:** User can register, log in, and be auto-navigated on reopen.

- [ ] Build `AuthScreen` UI: logo, tagline, login/register toggle, email + password fields, primary button
- [ ] `AuthViewModel`: `login()`, `register()`, session `StateFlow`
- [ ] Client-side validation: email format, password ≥ 8 chars
- [ ] On login success → navigate to `home`, clear back stack
- [ ] On app reopen with valid session → skip `auth`, go directly to `home`
- [ ] Error states: wrong credentials snackbar, email-already-registered message, network unavailable snackbar

---

### Phase 3 — Home Screen
**Deliverable:** Logged-in user sees their observation list (empty state if none).

- [ ] Build `HomeScreen` UI: `TopAppBar` (with `RenderEffect` backdrop blur), `BottomNavigationBar` (3 tabs), greeting header, search bar, `LazyColumn` of `ObservationCard`
- [ ] `ObservationCard` component: thumbnail (Coil), species name, location, date, optional RARE badge
- [ ] `HomeViewModel`: `loadObservations()` StateFlow, `searchObservations()` with 300ms debounce
- [ ] Empty state UI: illustration + "Record First Bird" CTA
- [ ] Skeleton shimmer loading state
- [ ] Pull-to-refresh
- [ ] FAB (+) → navigate to `form` (create mode)
- [ ] Tap card → navigate to `detail/{id}`

---

### Phase 4 — Form Screen (CRUD only, no ML)
**Deliverable:** User can create and edit observations with manual species entry.

- [ ] Build `FormScreen` UI: image picker area, species name field (with edit icon), location field, field notes textarea, Save + Discard buttons
- [ ] `AndroidManifest.xml`: `CAMERA` permission, `FileProvider` declaration + `res/xml/file_paths.xml`
- [ ] Image picker flow: `PickVisualMedia` (gallery, API 33+) + `TakePicture` (camera) with `CAMERA` permission request
- [ ] `compressImage()` utility in `util/`: iterative JPEG compress to ≤ 2MB
- [ ] `FormViewModel`: `pickImage()`, `saveObservation()` (upload to Supabase Storage → INSERT `bird_observations`), `loadObservation()` for edit mode
- [ ] Edit mode: pre-fill all fields from existing observation, reuse `image_url` if image not re-picked
- [ ] Save disabled when both `species_id` and `manual_species_name` are empty
- [ ] On save success → navigate back to `home`, trigger list refresh

---

### Phase 5 — Detail Screen
**Deliverable:** User can view full observation details and delete entries.

- [ ] Build `DetailScreen` UI: full-bleed hero image, RARE SIGHTING badge, species + scientific name, location card, date/time card, field notes, metadata chips, Edit FAB, Delete FAB
- [ ] `DetailViewModel`: `loadObservation()` (JOIN `species`), `deleteObservation()`
- [ ] Scientific name shown only when `species_id` non-null; fall back to `manual_species_name`
- [ ] Delete: confirmation dialog → DELETE → navigate back to `home`
- [ ] Edit FAB → navigate to `form?observationId={id}`
- [ ] Graceful handling if observation no longer exists (deleted from another session)

---

### Phase 6 — ML Integration
**Deliverable:** Picking an image auto-classifies the bird and fills the species field.

- [ ] Create `AvesLensModelService` in `data/remote/aveslens/`: OkHttp multipart POST to `/predict`, parse `PredictResponse` (`predictions[0].label` + `confidence`)
- [ ] Hilt module for `AvesLensModelService`
- [ ] Hook into `FormViewModel.pickImage()`: compress → classify → auto-fill `speciesName` (title-cased) + `confidenceScore`
- [ ] Progress bar UI: "ANALYZING SPECIMEN... X%" with animated green fill, skeleton placeholders below
- [ ] Species table lookup: case-insensitive match (`ILIKE`) → set `species_id` if found, else `manual_species_name`
- [ ] Low confidence (`< 0.5`) → auto-fill but show "Low confidence — please verify" warning
- [ ] HF Space cold start: retry once after 10s on 503, then show error card
- [ ] Error card UI: "Species not recognized" amber card + Retake / Enter Manually / Return actions
- [ ] Field photography tips section (shown while no image selected)

---

### Phase 7 — Profile Screen
**Deliverable:** User can view their profile, stats, and log out.

- [ ] Build `ProfileScreen` UI: avatar (128dp, edit badge), display name, location, stats cards (Birds Observed + Species Identified), Account Settings list, Logout button, version text
- [ ] `ProfileViewModel`: `loadProfile()`, `loadStats()` (COUNT + COUNT DISTINCT), `logout()`
- [ ] Logout → `supabase.auth.signOut()` → clear state → navigate to `auth`, clear back stack
- [ ] Version text pulled from `BuildConfig.VERSION_NAME`
- [ ] Profile row missing on first login → insert default row

---

### Phase 8 — Explorer Screen
**Deliverable:** User can browse and search all 525 recognizable bird species.

- [ ] Build `ExplorerScreen` UI: search bar, `LazyColumn` of species rows (common name + scientific name), species count header
- [ ] `ExplorerViewModel`: `loadSpecies()` (SELECT all `species` ORDER BY `common_name`), `searchSpecies()` with 300ms debounce
- [ ] Empty search result state: "No species found"
- [ ] Empty table state (seed not run): informational message
- [ ] Wire Explorer tab in `BottomNavigationBar`

---

### Phase 9 — Polish & Edge Cases
**Deliverable:** App is submission-ready with no rough edges.

- [ ] Audit all screens for missing error states and add retry buttons where absent
- [ ] Confirm Coil fallback placeholder on every image surface (cards, hero, avatar)
- [ ] Verify back stack is correct on all navigation paths (no double-back to auth, no orphaned screens)
- [ ] Test offline behaviour: no crash, snackbar shown on all network calls
- [ ] Confirm `chk_species_identified` DB constraint is never violated by the UI
- [ ] Test HF Space cold start path end-to-end
- [ ] Run through all acceptance criteria checkboxes in Screen Specifications below

---

## Screen Specifications

### 1. AuthScreen
**Route:** `auth`

**UI:**
- Login form: email + password fields, Login button, "Forgot Password?" link
- Toggle to Register form: email + full_name + username + password
- AvesLens logo + tagline

**ViewModel responsibilities:**
- `login(email, password)` → calls `supabase.auth.signInWith(Email) { ... }`
- `register(email, password, fullName, username)` → calls `supabase.auth.signUpWith()` then inserts into `profiles`
- Observe session state → navigate to HomeScreen on success

**Acceptance criteria:**
- [ ] Valid email format enforced client-side
- [ ] Password minimum 8 characters enforced
- [ ] Error message shown on wrong credentials
- [ ] On successful login, navigate to HomeScreen and clear back stack
- [ ] On app reopen, if session token still valid → skip AuthScreen directly to HomeScreen

**Edge cases:**
- Network unavailable → show snackbar "No internet connection"
- Email already registered → surface Supabase error message
- Username taken → validate uniqueness before insert or surface DB error

---

### 2. HomeScreen
**Route:** `home`

**UI:**
- Greeting header ("Good morning, Birder!")
- Search bar to filter observations by species name
- Recent Observations list (LazyColumn or LazyVerticalGrid)
  - Each item: thumbnail, species name, location, date, optional RARE badge
- Empty state: illustration + "Record First Bird" CTA button
- FAB (+) to navigate to FormScreen
- Bottom navigation bar: Journal | Observe | Explorer

**ViewModel responsibilities:**
- `loadObservations()` → SELECT from `bird_observations` WHERE `user_id = auth.uid()` ORDER BY `created_at DESC`
- `searchObservations(query)` → filter locally or re-query with ilike
- Expose `uiState: StateFlow<HomeUiState>` (Loading | Success | Empty | Error)

**Acceptance criteria:**
- [ ] List loads on screen entry
- [ ] Pull-to-refresh re-fetches from Supabase
- [ ] Search filters in real-time (debounced 300ms)
- [ ] Tapping an item navigates to DetailScreen with observation ID
- [ ] FAB navigates to FormScreen (create mode)
- [ ] Empty state shown when no observations exist

**Edge cases:**
- Supabase query fails → show error state with retry button
- Image URL broken/expired → show placeholder image via Coil fallback

---

### 3. FormScreen
**Route:** `form?observationId={id}` (id nullable — null = create, non-null = edit)

**UI:**
- Image picker area (camera or gallery)
- ML analysis progress bar + "Analyzing specimen..." label (during inference)
- Species name field (auto-filled from ML, editable)
- "Identification suggested by AvesLens AI" label below field
- Location field
- Field notes textarea
- Save Observation button
- Discard Draft button
- Error state: "Species not recognized" card + Retake / Enter Manually / Return options
- Field photography tips section (shown when no image selected)

**ViewModel responsibilities:**
- `pickImage(uri)` → store URI, trigger `classifyImage(uri)`
- `classifyImage(uri)` → send image bytes to HuggingFace API → parse response → auto-fill `speciesName` and `confidenceScore` state
- `saveObservation()`:
  1. Upload image to Supabase Storage → get public URL
  2. INSERT or UPDATE `bird_observations` with all fields
- `loadObservation(id)` → pre-fill fields for edit mode
- Expose `formUiState: StateFlow<FormUiState>` (Idle | AnalyzingImage | ImageError | Saving | Success | Error)

**AvesLens Model API call:**
```
POST https://crtal-aveslens-model-api.hf.space/predict
Content-Type: multipart/form-data
Body: form field "file" = image bytes (JPEG / PNG / WEBP)
No Authorization header required (public endpoint)

Response:
{
  "predictions": [
    { "label": "COMMON KINGFISHER", "confidence": 0.94 },
    ...  // top-5 results, descending confidence
  ]
}
```
Take `label` and `confidence` from `predictions[0]` (highest confidence).

**Label format note:** Labels are ALL CAPS (e.g., `"COMMON KINGFISHER"`). When matching against the `species` table, use a case-insensitive comparison (`ILIKE` or `UPPER()`) to avoid silent FK join failures.

**Android implementation (OkHttp / Ktor):**
```kotlin
// OkHttp multipart example
val requestBody = MultipartBody.Builder()
    .setType(MultipartBody.FORM)
    .addFormDataPart("file", "bird.jpg",
        imageBytes.toRequestBody("image/jpeg".toMediaType()))
    .build()

val request = Request.Builder()
    .url("https://crtal-aveslens-model-api.hf.space/predict")
    .post(requestBody)
    .build()
```

**Acceptance criteria:**
- [ ] Image picker opens gallery (and optionally camera)
- [ ] ML inference starts immediately after image selected
- [ ] Progress bar animates during inference
- [ ] On ML success: species name + confidence auto-filled (label displayed in title-case), field remains editable
- [ ] On ML failure: error card shown, user can enter manually
- [ ] `chk_species_identified` constraint respected — Save button disabled if both `species_id` and `manual_species_name` are empty
- [ ] In edit mode: existing data pre-filled, image shows current `image_url`
- [ ] On save success: navigate back to HomeScreen, list refreshes

**Edge cases:**
- Image too large → compress to JPEG before upload (max 2MB, use `Bitmap.compress()`)
- HF Space cold start (503 / slow first response) → retry once after 10s, then show error
- `confidence < 0.5` → still auto-fill but show warning label "Low confidence — please verify"
- Network drops mid-upload → show error, do not insert partial record
- Edit mode: if user does not re-pick image → reuse existing `image_url`, skip Storage upload
- Label not found in `species` table → set `species_id = null`, put title-cased label into `manual_species_name`

---

### 4. DetailScreen
**Route:** `detail/{observationId}`

**UI:**
- Full-resolution image (hero)
- RARE SIGHTING badge (conditional)
- Species common name (large heading)
- Scientific name (italic subtitle) — from `species` table if `species_id` non-null
- Location card with coordinates
- Date + time + weather (if available)
- Field notes section
- Tags: habitat, observation method, proximity
- Edit FAB → navigate to FormScreen (edit mode)
- Delete button → confirmation dialog → delete + navigate back

**ViewModel responsibilities:**
- `loadObservation(id)` → SELECT `bird_observations` JOIN `species` WHERE `id = observationId`
- `deleteObservation(id)` → DELETE from `bird_observations` (audit trigger fires automatically)

**Acceptance criteria:**
- [ ] All fields displayed correctly
- [ ] Scientific name shown only when `species_id` is non-null
- [ ] Edit FAB navigates to FormScreen with `observationId`
- [ ] Delete shows confirmation dialog before executing
- [ ] On delete success: navigate back to HomeScreen, item removed from list
- [ ] Image loads with Coil, shows loading placeholder

**Edge cases:**
- Observation deleted from another session → handle 404/empty result gracefully
- `species_id` null → hide scientific name, show `manual_species_name` instead

---

### 5. ProfileScreen
**Route:** `profile`

**UI:**
- Avatar (editable)
- Display name + location
- Stats: Birds Observed count, Species Identified count
- Account Settings: Edit Profile, Privacy, Help
- Logout button
- Version number

**ViewModel responsibilities:**
- `loadProfile()` → SELECT from `profiles` WHERE `id = auth.uid()`
- `loadStats()` → COUNT from `bird_observations`, COUNT DISTINCT `species_id`
- `logout()` → `supabase.auth.signOut()` → clear local state → navigate to AuthScreen

**Acceptance criteria:**
- [ ] Profile data loads on screen entry
- [ ] Stats reflect actual counts from DB
- [ ] Logout clears session and navigates to AuthScreen, clearing back stack
- [ ] Edit Profile navigates to edit form (or inline editing)

**Edge cases:**
- Profile row missing (edge case on first login) → insert default profile row
- Logout while offline → still clear local session, navigate to Auth

---

### 6. ExplorerScreen
**Route:** `explorer`
**Bottom nav tab:** Explorer (3rd tab)

**UI:**
- Search bar to filter species by name
- LazyColumn of species cards (common name + scientific name + optional thumbnail)
- Each item tappable → expands inline or navigates to a read-only species detail sheet
- Empty state for search with no results

**ViewModel responsibilities:**
- `loadSpecies()` → SELECT all from `species` ORDER BY `common_name ASC`
- `searchSpecies(query)` → filter locally with debounce (300ms) or `ilike` re-query
- Expose `uiState: StateFlow<ExplorerUiState>` (Loading | Success | Empty | Error)

**Acceptance criteria:**
- [ ] Full species list loads on first entry
- [ ] Search filters in real-time (debounced 300ms)
- [ ] Read-only — no create/edit/delete actions
- [ ] Species count shown in header (e.g., "525 Species")

**Edge cases:**
- `species` table empty (seed not run) → show empty state with note to admin
- No search results → show "No species found" message

---

## Camera & Media Permissions

Handle in `FormScreen` before opening picker. Required permissions vary by API level:

| API level | Gallery permission | Camera permission |
|---|---|---|
| 33+ (Android 13+) | `READ_MEDIA_IMAGES` | `CAMERA` |
| 29–32 | `READ_EXTERNAL_STORAGE` | `CAMERA` |
| ≤28 | `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE` | `CAMERA` |

**Implementation pattern (Compose):**
```kotlin
// Gallery — use PhotoPicker (API 33+, no permission needed) or fallback to GetContent
val galleryLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri -> uri?.let { viewModel.pickImage(it) } }

// Camera — write to FileProvider URI, requires CAMERA permission
val cameraLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.TakePicture()
) { success -> if (success) viewModel.pickImage(photoUri) }

val cameraPermission = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted -> if (granted) cameraLauncher.launch(photoUri) }
```

**AndroidManifest.xml entries required:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

<!-- FileProvider for camera output URI -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

> Prefer `PickVisualMedia` (Android Photo Picker, API 33+) for gallery — it requires zero permissions and Google recommends it. Fall back to `GetContent` on older APIs.

---

## Image Compression

Before uploading to Supabase Storage, compress in `FormViewModel`:

```kotlin
fun compressImage(context: Context, uri: Uri): ByteArray {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    var quality = 90
    var bytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        quality -= 10
    } while (bytes.size > 2 * 1024 * 1024 && quality > 40)
    return bytes
}
```

- Output format: **JPEG** (best size/quality ratio; WEBP is also accepted by the model API)
- Target: ≤ 2MB before both upload and ML inference call
- Use compressed bytes for the `/predict` call too — smaller payload, faster cold-start response

---

## Supabase Setup Checklist

Before running the app:
- [ ] Run `245150701111011_Dionisius_Seraf_Saputra.sql` on Supabase SQL Editor
- [ ] Enable Email Auth in Supabase Auth settings
- [ ] Create Storage bucket: `observation-images` (public)
- [ ] Add Storage policy: authenticated users can upload to their own folder (`user_id/*`)
- [ ] Populate `species` table with seed data — **labels must match the model's output** (see `labels.json` in the model repo; all-caps, 525 species)
- [ ] Add `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `AVESLENS_MODEL_URL` to `local.properties` (never commit)

---

## Environment Variables

In `local.properties`:
```
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
AVESLENS_MODEL_URL=https://crtal-aveslens-model-api.hf.space
```

Access in code via `BuildConfig` (configure in `build.gradle`).

> No API key required — the HF Space endpoint is public. If the Space is ever made private, add `HF_API_KEY` here and pass it as `Authorization: Bearer <key>`.

---

## Known Constraints & Decisions

| Decision | Rationale |
|---|---|
| `species_id` nullable FK | HuggingFace may fail to classify; `manual_species_name` is the fallback |
| Image stored as URL, not blob | Keeps DB lightweight; Supabase Storage handles file management |
| Audit log via PostgreSQL TRIGGER | Ensures logs cannot be bypassed by client-side bugs |
| `fn_set_updated_at()` shared by two triggers | Identical logic for `profiles` and `bird_observations` — DRY |
| SECURITY DEFINER on audit trigger function | Required to write to `audit_logs` despite RLS being active |
| Species table is admin-managed | Prevents user-submitted junk data from polluting the lookup table |
| `minSdk = 31` (Android 12) | Required to use `RenderEffect` for the TopAppBar `backdropBlur(12dp)` from Figma. API 31 covers ~85%+ of active devices (2025). If minSdk must go lower, replace with a semi-transparent `#FAF9F7` at 90% opacity — visually close enough. |
| Image compressed before upload AND inference | Avoids Supabase Storage quota waste and reduces HF Space payload; single compress pass reused for both calls. |
| Android Photo Picker preferred for gallery | `PickVisualMedia` (API 33+) needs no `READ_MEDIA_IMAGES` permission; fall back to `GetContent` on API 29–32. |
