package com.yayadile.ricecheck.ml

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Helpers to load the golden vectors from `app/src/test/resources`. */
internal object GoldenResources {

    fun resourceBytes(path: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream(path)) { "missing test resource $path" }.use { it.readBytes() }

    fun resourceText(path: String): String = resourceBytes(path).toString(Charsets.UTF_8)

    fun jsonArray(path: String): JsonArray = JsonParser.parseString(resourceText(path)).asJsonArray

    fun floats(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / 4) { buffer.float }
    }

    fun floats(path: String): FloatArray = floats(resourceBytes(path))

    /** Assets live outside the test classpath; resolve from the module directory. */
    fun assetBytes(name: String): ByteArray {
        val candidates = listOf(
            File("src/main/assets/$name"),
            File("app/src/main/assets/$name"),
        )
        val file = candidates.firstOrNull { it.exists() }
        requireNotNull(file) { "missing asset $name (tried ${candidates.joinToString()})" }
        return file.readBytes()
    }
}
