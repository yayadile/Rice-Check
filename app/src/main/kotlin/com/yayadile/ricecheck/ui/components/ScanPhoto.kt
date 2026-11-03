package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.util.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decodes a scan photo off the main thread (no image-loading dependency for v1). */
@Composable
fun rememberScanPhoto(path: String?, targetSizePx: Int = 512): ImageBitmap? {
    val photo by produceState<ImageBitmap?>(initialValue = null, path, targetSizePx) {
        value = if (path == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                ImageDecoder.decode(path, targetSizePx)?.asImageBitmap()
            }
        }
    }
    return photo
}

/** Square, rounded thumbnail with a neutral fallback when the file is gone. */
@Composable
fun ScanThumbnail(
    path: String?,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val photo = rememberScanPhoto(path, targetSizePx = 256)
    val shape = RoundedCornerShape(12.dp)
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = "Foto beras",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_image),
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(RiceColors.SurfaceGreen)
                .padding(16.dp)
        )
    }
}

// PREVIEW ONLY — path is null, so this shows the fallback used when a photo is gone.
@Preview(showBackground = true, name = "ScanThumbnail")
@Composable
private fun ScanThumbnailPreview() {
    PreviewSurface {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(Spacing.md)
        ) {
            ScanThumbnail(path = null, size = 56.dp)
        }
    }
}
