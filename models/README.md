# Model artifacts

Source model files copied from the training project `Deteksi Beras`. They are **integrated and
shipping**: the runtime assets are `app/src/main/assets/yolo11s_beras.tflite` (converted from
`best.onnx`) and `app/src/main/assets/bleach_svm_rbf.bin` (exported from `svm_rbf.pkl`). The
Android pipeline is in `app/src/main/kotlin/com/yayadile/ricecheck/ml/`.

The files in this folder are the canonical sources for regeneration, not the runtime assets.

## `yolo11s_beras/`

Source: `Deteksi Beras/runs2/beras_yolo11s_v1/` (Ultralytics YOLO11s, trained imgsz 1280).

| File | Description |
|---|---|
| `best.pt` | PyTorch weights (19 MB). |
| `best.onnx` | ONNX export (37 MB), input `1x3x1280x1280`, output `1x9x33600` (4 box coords + 5 class scores, **no NMS**). Re-exported from `best.pt` (Ultralytics 8.4.194, `dataset_det_v3`). |
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

## Runtime integration

- Runtime YOLO asset: `app/src/main/assets/yolo11s_beras.tflite`
  (input `1x1280x1280x3` NHWC RGB/255, output `1x9x33600` NCHW; pad 114, class-aware NMS
  conf 0.25 / IoU 0.45 / max 1000).
- Runtime SVM asset: `app/src/main/assets/bleach_svm_rbf.bin` (little-endian float32: magic
  `RSVM`, version, shapes, gamma, bias, scaler mean/scale, 198x8202 support vectors, dual coefs).
- Kotlin: `ml/YoloDetector.kt`, `ml/YoloPost.kt`, `ml/FeatureExtractor.kt`,
  `ml/BleachClassifier.kt`, `ml/QualityMapping.kt`, orchestrated by `ml/RiceClassifier.kt`.
- The verdict stays in `VerdictCalculator`; models only produce quality metrics.
- 0 detections or a missing/corrupt asset raises `AnalysisException` (the UI shows a failure);
  no stub numbers are ever produced.

## Regeneration and verification

Requires an env with `onnx2tf[tensorflow]==2.6.9` (Python 3.12) and an env with
`ultralytics`/`scikit-learn`/`scikit-image`/`opencv-python` for the SVM export and goldens.

```bash
# 1. canonical ONNX from the ml-trained weights
yolo export model=models/yolo11s_beras/best.pt format=onnx imgsz=1280 opset=12 simplify=True

# 2. ONNX -> TFLite
onnx2tf -i models/yolo11s_beras/best.onnx -o build/model_export/yolo -osd -coion

# 3. TFLite vs ONNX numeric check (writes nothing)
python tools/verify_tflite.py --onnx models/yolo11s_beras/best.onnx \
  --tflite build/model_export/yolo/best_float32.tflite --image <photo.jpg>

# 4. SVM asset + golden vectors
python tools/export_svm_asset.py
python tools/verify_svm_asset.py
python tools/make_svm_golden.py
python tools/make_yolo_golden.py
```

Copy `build/model_export/yolo/best_float32.tflite` to `app/src/main/assets/yolo11s_beras.tflite`
after verification. Golden vectors are written to `app/src/test/resources/golden/` and consumed by
`FeatureExtractorGoldenTest`, `BleachClassifierGoldenTest`, `YoloPostTest`, `QualityMappingTest`.
On-device parity is checked by `RiceClassifierInstrumentedTest` (`app/src/androidTest/`).

## Porting notes (verified parity)

- OpenCV 5.0 `BGR2GRAY` uses `(9798R + 19235G + 3735B + 16384) >> 15`; OpenCV -xs 4.x uses
  the 14-bit variant. Use the 5.0 coefficients to match this training env.
- skimage `graycomatrix(symmetric=True)` is `P + P.T`, so diagonal entries are **doubled**.
- OpenCV `MORPH_CLOSE, iterations=2` is a single **5x5** kernel (iterations grow the kernel),
  while `MORPH_OPEN, iterations=1` is 3x3.
- skimage HOG on 256x256 with cells 16x16 and blocks 2x2 yields 8100 values; bins are
  nearest-angle (no linear interpolation). Feature order is GLCM(6), HOG(8100), hist(96).

## License

The YOLO11s weights derive from Ultralytics and are **AGPL-3.0**. Distributing the app with these
assets has AGPL implications; verify before any release.

GitHub note: all files are under the 100 MB per-file limit (largest `best.onnx` ~37 MB),
so they can be committed without Git LFS. Total is ~69 MB.
