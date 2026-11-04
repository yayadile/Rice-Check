package com.yayadile.ricecheck.ml

/** Raised when a photo cannot be analysed (model missing, 0 grains detected, ...). */
class AnalysisException(message: String) : Exception(message)
