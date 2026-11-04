"""Generate YOLO golden vectors: source image + letterbox params + detections.

The detections come from Ultralytics `model.predict` (conf 0.25 / iou 0.45 / max_det 1000)
and are the reference the on-device TFLite pipeline is compared against.

Run with an env that has ultralytics + torch:
    python tools/make_yolo_golden.py
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import cv2
import numpy as np

VAL_DIR = Path("/home/rafli/Documents/Projek/Deteksi Beras/dataset_det_v3/images/val")
NAMES = ["beras_utuh", "beras_patah", "menir", "gabah", "batu"]


def letterbox(img: np.ndarray, new_shape=(1280, 1280), color=(114, 114, 114),
              scaleup=True, stride=32, auto=False):
    shape = img.shape[:2]
    r = min(new_shape[0] / shape[0], new_shape[1] / shape[1])
    if not scaleup:
        r = min(r, 1.0)
    new_unpad = (round(shape[1] * r), round(shape[0] * r))
    dw = new_shape[1] - new_unpad[0]
    dh = new_shape[0] - new_unpad[1]
    if auto:
        dw, dh = dw % stride, dh % stride
    dw /= 2
    dh /= 2
    if shape[::-1] != new_unpad:
        img = cv2.resize(img, new_unpad, interpolation=cv2.INTER_LINEAR)
    top, bottom = round(dh - 0.1), round(dh + 0.1)
    left, right = round(dw - 0.1), round(dw + 0.1)
    img = cv2.copyMakeBorder(img, top, bottom, left, right, cv2.BORDER_CONSTANT, value=color)
    return img, r, (left, top)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--weights", default="models/yolo11s_beras/best.pt")
    ap.add_argument("--out", default="app/src/test/resources/golden/yolo")
    ap.add_argument("--count", type=int, default=2)
    ap.add_argument("--imgsz", type=int, default=1280)
    args = ap.parse_args()

    from ultralytics import YOLO
    from ultralytics.data.augment import LetterBox

    root = Path(args.out)
    (root / "images").mkdir(parents=True, exist_ok=True)

    files = sorted(VAL_DIR.glob("*.jpg"))[: args.count]
    model = YOLO(args.weights)

    manifest = []
    for i, path in enumerate(files):
        name = f"det_{i:02d}"
        img = cv2.imread(str(path))
        h, w = img.shape[:2]

        # Validate our manual letterbox against Ultralytics'.
        lb = LetterBox((args.imgsz, args.imgsz), auto=False, scaleup=True, stride=32)
        ref = lb(image=img)
        mine, r, (pad_x, pad_y) = letterbox(img, (args.imgsz, args.imgsz))
        assert np.array_equal(ref.shape, mine.shape) and np.abs(ref.astype(int) - mine.astype(int)).max() <= 1, (
            f"letterbox mismatch for {path}"
        )

        res = model.predict(str(path), imgsz=args.imgsz, conf=0.25, iou=0.45, max_det=1000, verbose=False)[0]
        detections = []
        counts = {n: 0 for n in NAMES}
        for xyxy, conf, cls in zip(res.boxes.xyxy.cpu().numpy(), res.boxes.conf.cpu().numpy(), res.boxes.cls.cpu().numpy()):
            counts[NAMES[int(cls)]] += 1
            detections.append(
                {"xyxy": [float(v) for v in xyxy], "conf": float(conf), "cls": int(cls)}
            )

        (root / "images" / f"{name}.jpg").write_bytes(Path(path).read_bytes())

        manifest.append(
            {
                "name": name,
                "width": int(w),
                "height": int(h),
                "imgsz": args.imgsz,
                "scale": float(r),
                "pad_x": float(pad_x),
                "pad_y": float(pad_y),
                "counts": counts,
                "detections": detections,
            }
        )
        print(f"{name}: {w}x{h} scale={r:.4f} pad=({pad_x},{pad_y}) counts={counts}")

    (root / "manifest.json").write_text(json.dumps(manifest, indent=2))
    print(f"wrote {len(manifest)} cases to {root}")


if __name__ == "__main__":
    main()
