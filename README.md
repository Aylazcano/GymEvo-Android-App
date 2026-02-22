# GymEvo Android App

## Architecture

Offline-first Android app (Java 17, minSdk 26). Room is the single source of truth; no external sync dependencies are active until a server is configured.

## Offline exercise catalog

- Exercises are seeded from `assets/free_exercise_db/exercises.json` via `FreeExerciseDbSeeder` — idempotent, missing exercises only.
- Catalog images are bundled under `assets/free_exercise_db/images/` and served as `file:///android_asset/...` paths.
- Room `exercise` table stores full free-exercise-db metadata (`sourceId`, `force`, `level`, `mechanic`, `equipment`, `category`, `primaryMuscles`, `secondaryMuscles`, `instructions`), propagated to `exercise_in_workout`.

## Custom exercise images

- When a user picks images for a custom exercise, `ExerciseImageStore` compresses and saves them once to `files/free_exercise_db/images/{name}/0.jpg` and `1.jpg` (~750 px max side, JPEG 82%).
- `ExerciseImageStore.persistIfNeeded()` returns a `PersistResult(imageA, imageB)` and never mutates its argument.
- Room stores `file://` URIs so images remain available offline even if the cache is cleared.

## Server sync (gated — not yet active)

All sync infrastructure is built and production-safe. Nothing runs until `IMAGE_SYNC_ENABLED=true` and `IMAGE_SYNC_BASE_URL` are set in `build.gradle`.

**Sync package — 6 classes:**

| Class | Role |
|---|---|
| `ExerciseImageSyncConfig` | Reads `BuildConfig` flags; `isValid()` is the single gate for all sync behavior |
| `ExerciseImageSyncScheduler` | Enqueues unique `OneTimeWorkRequest` (network required, exponential backoff 30 s) |
| `ExerciseImageSyncWorker` | Batches queue items; `PENDING→UPLOADING→DONE/FAILED`; retries while retriable items remain |
| `ExerciseImageUploader` | Interface: `boolean upload(item)` |
| `StubExerciseImageUploader` | Returns `false` — used in tests only |
| `HttpExerciseImageUploader` | Validates config + local file; reads optional Bearer token from `SharedPreferences`; TODO: multipart body |

**Room queue (`exercise_image_sync_queue`):**
- FK `exerciseId → exercise.id ON DELETE CASCADE` — no orphan rows possible.
- Statuses: `PENDING → UPLOADING → DONE / FAILED`; retry budget capped at 10.

**To activate when server is ready:**
1. In `app/build.gradle`:
   ```groovy
   buildConfigField "boolean", "IMAGE_SYNC_ENABLED", "true"
   buildConfigField "String",  "IMAGE_SYNC_BASE_URL",        '"https://api.yourserver.com"'
   buildConfigField "String",  "IMAGE_SYNC_AUTH_PREFS_NAME", '"gymevo_prefs"'
   buildConfigField "String",  "IMAGE_SYNC_AUTH_TOKEN_KEY",  '"auth_token"'
   ```
2. Implement multipart upload in `HttpExerciseImageUploader.upload()` — return `true` on HTTP 2xx only.

## Developer utilities

- `scripts/download_free_exercise_db_images.ps1` — optional script to refresh bundled catalog images. Not required at runtime.

## Validation tests (instrumented)

Run with a connected device/emulator:
```
./gradlew connectedDebugAndroidTest
```

| Test | Covers |
|---|---|
| `ExerciseImageStoreInstrumentedTest` | Images written to `0.jpg`/`1.jpg`; `PersistResult` returned; no sidecar written |
| `ExerciseImageSyncQueueDaoInstrumentedTest` | Queue retrieval; cascade delete removes queue rows when exercise is deleted |
| `ExerciseImageSyncWorkerInstrumentedTest` | Worker returns success immediately when sync config is disabled |
