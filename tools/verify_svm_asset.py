"""Read back bleach_svm.bin and reproduce the golden decisions.

This validates the exact layout the Kotlin reader uses, independently of scikit-learn.
    python tools/verify_svm_asset.py
"""

from __future__ import annotations

import json
import struct
from pathlib import Path

import numpy as np

ASSET = Path("app/src/main/assets/bleach_svm.bin")
GOLDEN = Path("app/src/test/resources/golden/svm")


def load_asset(path: Path):
    raw = path.read_bytes()
    assert raw[:4] == b"RSVM", "bad magic"
    version, n_features, n_sv, n_classes, kernel = struct.unpack_from("<IIIII", raw, 4)
    gamma, bias = struct.unpack_from("<ff", raw, 24)
    off = 32
    mean = np.frombuffer(raw, "<f4", n_features, off); off += 4 * n_features
    scale = np.frombuffer(raw, "<f4", n_features, off); off += 4 * n_features
    support = np.frombuffer(raw, "<f4", n_sv * n_features, off).reshape(n_sv, n_features); off += 4 * n_sv * n_features
    dual = np.frombuffer(raw, "<f4", n_sv, off)
    return dict(version=version, kernel=kernel, gamma=gamma, bias=bias, mean=mean, scale=scale, support=support, dual=dual)


def decision(a, raw: np.ndarray) -> float:
    z = ((raw.astype(np.float64) - a["mean"]) / a["scale"]).astype(np.float32)
    if a["kernel"] == 0:  # rbf
        d2 = ((z[None, :] - a["support"]) ** 2).sum(axis=1)
        return float((a["dual"] * np.exp(-a["gamma"] * d2)).sum() + a["bias"])
    # linear
    dots = a["support"] @ z
    return float((a["dual"] * dots).sum() + a["bias"])


def main() -> None:
    a = load_asset(ASSET)
    kernel_name = {0: "rbf", 1: "linear"}.get(int(a["kernel"]), str(a["kernel"]))
    print(f"asset: version={a['version']} kernel={kernel_name} gamma={a['gamma']:.9g} "
          f"bias={a['bias']:.9g} n_sv={a['support'].shape[0]} n_features={a['mean'].shape[0]}")

    manifest = json.loads((GOLDEN / "manifest.json").read_text())
    ok = True
    for case in manifest:
        raw = np.fromfile(GOLDEN / "features" / f"{case['name']}.f32", "<f4")
        d = decision(a, raw)
        cls = 1 if d > 0 else 0
        exp_d, exp_c = case["decision"], case["class"]
        match = cls == exp_c and abs(d - exp_d) < 1e-3
        ok &= match
        print(f"{case['name']:14s} decision={d:+.6f} (exp {exp_d:+.6f}) class={cls} "
              f"(exp {exp_c}) {'OK' if match else 'MISMATCH'}")

    print("PASS" if ok else "FAIL")


if __name__ == "__main__":
    main()
