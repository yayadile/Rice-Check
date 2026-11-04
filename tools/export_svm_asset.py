"""Export the bleach SVM to a compact little-endian float32 asset.

Layout (all little-endian):

    offset  type       field
    0       bytes[4]   magic = b"RSVM"
    4       uint32     version = 2
    8       uint32     n_features
    12      uint32     n_sv
    16      uint32     n_classes (2)
    20      uint32     kernel (0 = rbf, 1 = linear)
    24      float32    gamma (unused for linear, written as 0)
    28      float32    bias (intercept for the positive class)
    32      float32[n_features]        scaler mean_
    32+4n   float32[n_features]        scaler scale_
    ...     float32[n_sv*n_features]  support vectors (scaled space, row-major)
    ...     float32[n_sv]             dual coefficients

Usage:
    python tools/export_svm_asset.py \
        [--bundle models/bleach_svm/svm_linear.pkl] \
        [--out app/src/main/assets/bleach_svm.bin]
"""

from __future__ import annotations

import argparse
import struct
from pathlib import Path

import joblib
import numpy as np

MAGIC = b"RSVM"
VERSION = 2
KERNELS = {"rbf": 0, "linear": 1}


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--bundle", default="models/bleach_svm/svm_linear.pkl")
    ap.add_argument("--out", default="app/src/main/assets/bleach_svm.bin")
    args = ap.parse_args()

    bundle = joblib.load(args.bundle)
    model = bundle["model"]
    scaler = bundle["scaler"]

    kernel_name = model.kernel
    if kernel_name not in KERNELS:
        raise SystemExit(f"unsupported kernel: {kernel_name}")
    kernel_id = KERNELS[kernel_name]

    mean = np.asarray(scaler.mean_, dtype="<f4")
    scale = np.asarray(scaler.scale_, dtype="<f4")
    support = np.asarray(model.support_vectors_, dtype="<f4")
    dual = np.asarray(model.dual_coef_[0], dtype="<f4")
    gamma = float(model._gamma) if kernel_name == "rbf" else 0.0
    bias = float(model.intercept_[0])

    n_features = mean.shape[0]
    n_sv = support.shape[0]

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("wb") as f:
        f.write(MAGIC)
        f.write(struct.pack("<IIIII", VERSION, n_features, n_sv, 2, kernel_id))
        f.write(struct.pack("<ff", gamma, bias))
        f.write(mean.tobytes())
        f.write(scale.tobytes())
        f.write(support.tobytes())
        f.write(dual.tobytes())

    print(f"wrote {out} ({out.stat().st_size:,} bytes)")
    print(f"kernel={kernel_name} n_features={n_features} n_sv={n_sv} gamma={gamma:.9g} bias={bias:.9g}")


if __name__ == "__main__":
    main()
