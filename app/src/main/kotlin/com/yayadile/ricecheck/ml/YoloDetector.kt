package com.yayadile.ricecheck.ml

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import java.io.Closeable

/**
 * LiteRT wrapper for the YOLO11s rice detector (`app/src/main/assets/yolo11s_beras.tflite`).
 *
 * Input: NHWC `[1, 1280, 1280, 3]` float32 RGB /255. Output: NCHW `[1, 9, 33600]`.
 */
class YoloDetector(
    context: Context,
    assetName: String = ASSET_NAME,
) : Closeable {

    private val model: CompiledModel = CompiledModel.create(context.applicationContext.assets, assetName)
    private val input: TensorBuffer = model.createInputBuffers()[0]
    private val output: TensorBuffer = model.createOutputBuffers()[0]

    fun detect(argb: IntArray, width: Int, height: Int): List<YoloPost.Detection> {
        val letterbox = YoloPost.letterbox(width, height)
        val tensor = YoloPost.toInputTensor(argb, width, height)
        input.writeFloat(tensor)
        model.run(listOf(input), listOf(output))
        val raw = output.readFloat()
        val decoded = YoloPost.decode(raw)
        val kept = YoloPost.nms(decoded)
        return YoloPost.toOriginal(kept, letterbox, width, height)
    }

    override fun close() {
        runCatching { model.close() }
    }

    companion object {
        const val ASSET_NAME = "yolo11s_beras.tflite"
    }
}
