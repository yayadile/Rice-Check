"""Generate SVM golden vectors for the Kotlin port.

For each selected image we export:
  - resized/<name>.bin : the exact 256x256 BGR uint8 image (INTER_AREA) fed to features
  - features/<name>.f32: 8202 float32 raw features
  - scaled/<name>.f32  : 8202 float32 after StandardScaler
and a manifest.json with the expected decision value and class.

Run with the training env:
    python tools/make_svm_golden.py
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import cv2
import joblib
import numpy as np

import bleach_features as bf

TRAIN_DIR = Path("/home/rafli/Documents/Projek/Deteksi Beras/train_rice_bleach")


def select_images(per_class: int) -> list[tuple[str, Path]]:
    out: list[tuple[str, Path]] = []
    for label in ("normal", "pemutih"):
        files = sorted((TRAIN_DIR / label).glob("*.jpg"))[:per_class]
        for i, p in enumerate(files):
            out.append((f"{label}_{i:02d}", p))
    return out


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="app/src/test/resources/golden/svm")
    ap.add_argument("--per-class", type=int, default=6)
    args = ap.parse_args()

    root = Path(args.out)
    for sub in ("resized", "features", "scaled"):
        (root / sub).mkdir(parents=True, exist_ok=True)

    bundle = joblib.load("models/bleach_svm/svm_rbf.pkl")
    model, scaler = bundle["model"], bundle["scaler"]

    manifest = []
    for name, path in select_images(args.per_class):
        img = cv2.resize(cv2.imread(str(path)), bf.IMG_SIZE, interpolation=cv2.INTER_AREA)
        feats = bf.extract_features(str(path)).astype("<f4")
        scaled = scaler.transform(feats.reshape(1, -1))[0].astype("<f4")
        decision = float(model.decision_function(scaled.reshape(1, -1))[0])
        pred = int(model.predict(scaled.reshape(1, -1))[0])

        (root / "resized" / f"{name}.bin").write_bytes(img.tobytes())
        (root / "features" / f"{name}.f32").write_bytes(feats.tobytes())
        (root / "scaled" / f"{name}.f32").write_bytes(scaled.tobytes())

        manifest.append(
            {
                "name": name,
                "label": path.parent.name,
                "decision": decision,
                "class": pred,
            }
        )
        print(f"{name:14s} label={path.parent.name:8s} decision={decision:+.4f} class={pred}")

    (root / "manifest.json").write_text(json.dumps(manifest, indent=2))
    print(f"wrote {len(manifest)} cases to {root}")


if __name__ == "__main__":
    main()
