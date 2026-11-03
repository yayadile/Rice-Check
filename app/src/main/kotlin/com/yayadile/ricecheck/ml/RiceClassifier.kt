package com.yayadile.ricecheck.ml

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import java.io.Closeable

/**
 * Wrapper class that loads a .tflite model from assets and runs inference using LiteRT.
 */
class RiceClassifier(
    private val context: Context,
    private val modelPath: String = "model.tflite"
) : Closeable {

    private var compiledModel: CompiledModel? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        runCatching {
            compiledModel = CompiledModel.create(context.assets, modelPath)
        }.onFailure {
            // Model file will be placed in assets when trained/ready
            compiledModel = null
        }
    }

    fun isModelLoaded(): Boolean = compiledModel != null

    /**
     * Runs inference on raw float input buffer and returns class prediction probabilities.
     */
    fun classify(inputData: FloatArray): FloatArray {
        val model = compiledModel ?: return floatArrayOf()
        val inputBuffers: List<TensorBuffer> = model.createInputBuffers()
        val outputBuffers: List<TensorBuffer> = model.createOutputBuffers()

        if (inputBuffers.isNotEmpty()) {
            inputBuffers[0].writeFloat(inputData)
        }

        model.run(inputBuffers, outputBuffers)

        return if (outputBuffers.isNotEmpty()) {
            outputBuffers[0].readFloat()
        } else {
            floatArrayOf()
        }
    }

    override fun close() {
        compiledModel = null
    }
}

