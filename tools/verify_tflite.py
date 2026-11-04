"""Verify that the converted TFLite model matches the source ONNX model.

Runs both models on the same input and reports the maximum absolute difference.
Usage:
    python tools/verify_tflite.py \
        --onnx models/yolo11s_beras/best.onnx \
        --tflite build/model_export/yolo/best_float32.tflite \
        [--image path/to/photo.jpg]
"""

from __future__ import annotations

import argparse

import cv2
import numpy as np
import onnxruntime as ort


def make_interpreter(model_path: str):
    try:
        from ai_edge_litert.interpreter import Interpreter  # type: ignore

        return Interpreter(model_path=model_path)
    except ImportError:
        import tensorflow as tf  # type: ignore

        return tf.lite.Interpreter(model_path=model_path)


def load_input(image: str | None, imgsz: int = 1280) -> np.ndarray:
    if image is None:
        rng = np.random.default_rng(0)
        return rng.random((1, 3, imgsz, imgsz), dtype=np.float32)
    bgr = cv2.imread(image)
    if bgr is None:
        raise SystemExit(f"cannot read image: {image}")
    resized = cv2.resize(bgr, (imgsz, imgsz), interpolation=cv2.INTER_LINEAR)
    rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB).astype(np.float32) / 255.0
    return np.transpose(rgb, (2, 0, 1))[np.newaxis, ...].copy()


def align(a: np.ndarray, b: np.ndarray) -> tuple[np.ndarray, np.ndarray]:
    if a.shape == b.shape:
        return a, b
    if a.ndim == 3 and b.ndim == 3 and a.shape[0] == 1 and b.shape[0] == 1:
        # Handle possible NCHW/NHWC transposition of the second output.
        if a.shape[1:] == b.shape[1:][::-1]:
            b = np.transpose(b, (0, 3, 1, 2))
    return a, b


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--onnx", required=True)
    ap.add_argument("--tflite", required=True)
    ap.add_argument("--image", default=None)
    ap.add_argument("--tol", type=float, default=1e-2)
    args = ap.parse_args()

    inp = load_input(args.image)
    print(f"input shape : {inp.shape}")

    sess = ort.InferenceSession(args.onnx, providers=["CPUExecutionProvider"])
    in_name = sess.get_inputs()[0].name
    onnx_out = sess.run(None, {in_name: inp})[0]
    print(f"onnx output : {onnx_out.shape}")

    interp = make_interpreter(args.tflite)
    interp.allocate_tensors()
    in_detail = interp.get_input_details()[0]
    tflite_in = inp
    expected = tuple(int(x) for x in in_detail["shape"])
    if expected == (1, inp.shape[2], inp.shape[3], inp.shape[1]):
        tflite_in = np.transpose(inp, (0, 2, 3, 1))
    print(f"tflite input : {expected}")
    interp.set_tensor(in_detail["index"], tflite_in)
    interp.invoke()
    out_detail = interp.get_output_details()[0]
    tflite_out = interp.get_tensor(out_detail["index"])
    print(f"tflite output: {tflite_out.shape} ({out_detail['name']})")

    a, b = align(onnx_out, tflite_out)
    if a.shape != b.shape:
        raise SystemExit(f"shape mismatch after align: {a.shape} vs {b.shape}")

    diff = np.abs(a - b)
    rel = diff.max() / (np.abs(a).max() + 1e-9)
    print(f"max abs diff : {diff.max():.6e}")
    print(f"mean abs diff: {diff.mean():.6e}")
    print(f"relative diff: {rel:.6e}")
    print("PASS" if diff.max() <= args.tol else "FAIL")


if __name__ == "__main__":
    main()
