package com.yayadile.ricecheck.ui.navigation

import android.net.Uri

/**
 * Routes from docs/IA.md §2.
 *
 * The `analyzing` route additionally carries `price` (see docs/architecture.md §5:
 * "navigasi ke analyzing dengan photoUri + price") so the price entered on the Camera
 * screen survives the hop to Result.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CAMERA = "camera"
    const val HISTORY = "history"

    const val ARG_PHOTO_URI = "photoUri"
    const val ARG_PRICE = "price"
    const val ANALYZING = "analyzing?photoUri={photoUri}&price={price}"

    fun analyzing(photoUri: String, price: Int? = null): String =
        "analyzing?photoUri=${Uri.encode(photoUri)}&price=${price?.toString().orEmpty()}"

    const val ARG_SCAN_ID = "scanId"
    const val RESULT = "result/{scanId}"

    fun result(scanId: Long): String = "result/$scanId"

    const val HISTORY_DETAIL = "historyDetail/{scanId}"

    fun historyDetail(scanId: Long): String = "historyDetail/$scanId"
}
