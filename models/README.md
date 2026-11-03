# Model artifacts

Raw model files copied from the training project `Deteksi Beras`. These are **not used
at runtime yet** — `app/src/main/kotlin/com/yayadile/ricecheck/ml/RiceClassifier.kt` still
returns a stub `QualityReport`. The Android app expects a single LiteRT asset at
`app/src/main/assets/ricecheck_v1.tflite`; the files here need conversion before integration.

## `yolo11s_beras/`

Source: `Deteksi Beras/runs2/beras_yolo11s_v1/` (Ultralytics YOLO11s, trained imgsz 1280).

| File | Description |
|---|---|
| `best.pt` | PyTorch weights (19 MB). |
| `best.onnx` | ONNX export (37 MB), input `1x3x1280x1280`, output `1x9x33600` (4 box coords + 5 class scores, **no NMS**). |
| `classes.txt` | Class names in output index order. |
| `model_artifacts.json` | Ultralytics metadata (`nc`, names, architecture, version). |

Classes (index order): `0 beras_utuh`, `1 beras_patah`, `2 menir`, `3 gabah`, `4 batu`.

Training config: `epochs 150`, `batch 2`, `imgsz 1280`, dataset `dataset_det_v3`, Ultralytics `8.3.40`.

Detection config used by the training project: `conf 0.25`, `iou 0.45`, `max_det 1000`.

## `bleach_svm/`

Source: `Deteksi Beras/bleach_model/` (scikit-learn `SVC`, kernel RBF, `C=10`, `gamma=scale`, `probability=True`).
This is the best non-CLAHE variant (accuracy 94%, F1 0.9388).

| File | Description |
|---|---|
| `svm_rbf.pkl` | joblib bundle: `model` (SVC), `scaler` (`StandardScaler`), `kernel`, `clahe=False`. |
| `metrics.json` | Evaluation metrics for the RBF and linear kernels. |

Input: 8202 features per image, computed as:
1. Resize to 256x256 (BGR), convert to gray.
2. Otsu threshold + morphological open/close to segment grains.
3. GLCM (distances 1, 4 angles, 256 levels) -> 6 props (`contrast`, `dissimilarity`,
   `homogeneity`, `energy`, `correlation`, `ASM`).
4. HOG: orientations 9, pixels_per_cell 16x16, cells_per_block 2x2, `L2-Hys` -> 8100.
5. Color histogram: 32 bins per BGR channel -> 96.

Classes: `0 normal`, `1 pemutih`. Output is `predict_proba` for the `pemutih` class.

## Integration notes (not done yet)

- YOLO: export `.pt` -> TensorFlow Lite (or ONNX -> TFLite), add Box/NMS decoding in Kotlin,
  and map detections to `QualityReport` (`wholeGrainPct` from `beras_utuh`, `brokenPct` from
  `beras_patah` + `menir`, etc.). Rename/point `RiceClassifier.ASSET_NAME` accordingly.
- SVM: either export to ONNX/TFLite or port the 198 RBF support vectors + scaler to Kotlin.
  The GLCM/HOG/color-histogram preprocessing must be ported to match training exactly.
- Preprocessing must match training (input size, channel order BGR/RGB, normalization) or
  results will be wrong even if the model loads.
- The verdict stays in `VerdictCalculator`; models only produce quality metrics.

GitHub note: all files are under the 100 MB per-file limit (largest `best.onnx` ~37 MB),
so they can be committed without Git LFS. Total is ~69 MB.
