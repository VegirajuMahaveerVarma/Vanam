# Vanam
Offline AI field guide. A field notebook, not a chatbot. Nothing is uploaded; GPS stays on the device; the manifest has no INTERNET permission.

## Architecture
- `domain/` interfaces (ImageIdentifier, AudioIdentifier, FieldWriter), `ConfidencePolicy` (0.75 / 0.4 honesty rule), Fakes, `Real.kt` (BirdNET, TFLite classifier, Gemma writer), `InferenceLog`.
- `data/` Room (species, trails, discoveries, challenges), seed data, `Repo`.
- `ui/` Compose screens, `TrailViewModel` (GPS, mic, camera results, pocket mode, screen-time tracking), hidden `DebugScreen`.
- `di/Modules.kt` is the only place models are chosen.
Pipeline: capture -> identify -> ConfidencePolicy -> FieldWriter (grounded in DB facts only) -> save to trail.

## Models (all optional; each falls back automatically)
Put files in `app/src/main/assets/`, then rebuild.
- **BirdNET**: `birdnet.tflite` (BirdNET V2.4 TFLite from https://zenodo.org/records/15050749). `birdnet_labels.txt` is already bundled. License is CC BY-NC-SA 4.0 (non-commercial).
- **Photo classifier**: `species_classifier.tflite` + `species_labels.txt`. No pretrained model for these 15 species exists, so train one with `tools/train_classifier.py`.
- **Gemma**: `gemma.task` (a Gemma .task bundle for MediaPipe LLM Inference) in the app's external files folder. Output is rejected and replaced by the template if it is slow (6 s), too long, or contains numbers.
Swap models by changing the three `@Binds` lines in `di/Modules.kt`.

## Debug
Debug builds only: long-press the "Vanam" title on Home. Shows which model is active, latency per call, and a right/wrong tally.

## Known limitations (honest)
- Not compiled or tested by the author; expect small fixes. No Android SDK was available when written.
- Without model files, identifications are random fakes.
- BirdNET confidences are usually lower than the 0.75 / 0.4 thresholds in noisy field audio; calibrate with the debug screen. Audio scores all ~6,500 BirdNET bird labels; the 8 seeded birds have full field notes, every other bird gets a names-only entry (no invented facts). Plants come from the photo classifier only.
- BirdNET, the classifier and Gemma code paths are unverified against real model files.
- The "map" plots saved coordinates on a grid; there are no map tiles. Trail path is not stored.
- Nature Pack download is simulated; the data is bundled. The isolated downloader is not built.
- Inference log is in memory only. Back during a trail asks before ending.
- Fonts are variable fonts (Fraunces, Inter, SIL OFL); labels use the system monospace.
