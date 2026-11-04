"""Reference feature extraction for the bleach SVM.

This is a faithful, self-contained copy of the feature pipeline used to train
`Deteksi Beras/bleach_model/svm_rbf.pkl` (see `train_rice_bleach.py`). It exists so
the golden vectors and the Kotlin port can be validated against one source of truth.

Feature layout (8202 total):
    [0:6]      GLCM props (contrast, dissimilarity, homogeneity, energy, correlation, ASM)
    [6:8106]   HOG (9 orientations, 16x16 cells, 2x2 blocks, L2-Hys)
    [8106:]    color histogram (32 bins x 3 BGR channels, L2-normalized)
"""

from __future__ import annotations

import cv2
import numpy as np
from skimage.feature import graycomatrix, graycoprops, hog

IMG_SIZE = (256, 256)
CLASSES = {"normal": 0, "pemutih": 1}


def resize_image(path: str, size=IMG_SIZE) -> np.ndarray:
    img = cv2.imread(str(path))
    if img is None:
        raise ValueError(f"gagal membaca: {path}")
    return cv2.resize(img, size, interpolation=cv2.INTER_AREA)


def to_gray(img: np.ndarray) -> np.ndarray:
    return cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)


def apply_clahe(gray: np.ndarray) -> np.ndarray:
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    return clahe.apply(gray)


def segment(gray: np.ndarray) -> np.ndarray:
    _, mask = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    kernel = np.ones((3, 3), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=1)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
    return mask


def extract_glcm(gray: np.ndarray) -> np.ndarray:
    glcm = graycomatrix(
        gray,
        distances=[1],
        angles=[0, np.pi / 4, np.pi / 2, 3 * np.pi / 4],
        levels=256,
        symmetric=True,
        normed=True,
    )
    props = ["contrast", "dissimilarity", "homogeneity", "energy", "correlation", "ASM"]
    return np.array([graycoprops(glcm, p).mean() for p in props])


def extract_hog(gray: np.ndarray) -> np.ndarray:
    return hog(
        gray,
        orientations=9,
        pixels_per_cell=(16, 16),
        cells_per_block=(2, 2),
        block_norm="L2-Hys",
        visualize=False,
        feature_vector=True,
    )


def extract_hist(img: np.ndarray, bins: int = 32) -> np.ndarray:
    feats = []
    for i in range(3):
        h = cv2.calcHist([img], [i], None, [bins], [0, 256])
        h = cv2.normalize(h, h).flatten()
        feats.extend(h)
    return np.array(feats)


def extract_features(path: str, use_clahe: bool = False) -> np.ndarray:
    img = resize_image(path)
    gray = to_gray(img)
    if use_clahe:
        gray = apply_clahe(gray)
    mask = segment(gray)
    segmented = cv2.bitwise_and(img, img, mask=mask)
    gray_seg = cv2.cvtColor(segmented, cv2.COLOR_BGR2GRAY)

    f_glcm = extract_glcm(gray_seg)
    f_hog = extract_hog(gray_seg)
    f_hist = extract_hist(segmented)
    return np.hstack([f_glcm, f_hog, f_hist])


def extract_features_from_bgr(img_bgr: np.ndarray, use_clahe: bool = False) -> np.ndarray:
    """Same pipeline as extract_features but starting from an in-memory BGR image."""
    img = cv2.resize(img_bgr, IMG_SIZE, interpolation=cv2.INTER_AREA)
    gray = to_gray(img)
    if use_clahe:
        gray = apply_clahe(gray)
    mask = segment(gray)
    segmented = cv2.bitwise_and(img, img, mask=mask)
    gray_seg = cv2.cvtColor(segmented, cv2.COLOR_BGR2GRAY)
    return np.hstack([extract_glcm(gray_seg), extract_hog(gray_seg), extract_hist(segmented)])
