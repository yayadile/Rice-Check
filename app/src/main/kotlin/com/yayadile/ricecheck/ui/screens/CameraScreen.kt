package com.yayadile.ricecheck.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.RiceSecondaryButton
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.ui.theme.ViewfinderShape
import com.yayadile.ricecheck.viewmodel.CameraViewModel
import com.yayadile.ricecheck.viewmodel.GalleryImportState
import com.yayadile.ricecheck.viewmodel.cameraViewModelFactory
import kotlinx.coroutines.launch

/**
 * Route: camera — price input, CameraX preview, frame guide, shutter, gallery picker.
 */
@Composable
fun CameraScreen(
    onCaptured: (photoPath: String, price: Int?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val viewModel: CameraViewModel = viewModel(
        factory = remember { cameraViewModelFactory(container.scanFileStore) }
    )
    val priceInput by viewModel.priceInput.collectAsState()
    val galleryState by viewModel.galleryState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var requestedOnce by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        requestedOnce = true
    }

    // Ask on first entry only; a denial lands on the explanation screen.
    LaunchedEffect(Unit) {
        if (!hasPermission && !requestedOnce) {
            requestedOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Coming back from system settings: re-check without asking again.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasCameraPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.importFromGallery(uri)
    }

    LaunchedEffect(galleryState) {
        when (val state = galleryState) {
            is GalleryImportState.Imported -> {
                viewModel.consumeGalleryState()
                onCaptured(state.photoPath, viewModel.priceOrNull())
            }

            is GalleryImportState.Failed -> {
                viewModel.consumeGalleryState()
                snackbarHostState.showSnackbar(state.message)
            }

            else -> Unit
        }
    }

    if (!hasPermission) {
        PermissionRationale(
            onBack = onBack,
            onOpenSettings = { context.openAppSettings() },
            onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
        return
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val view = LocalView.current

    fun capture() {
        // Keep the JPEG upright so the classifier and thumbnails agree with the preview.
        view.display?.rotation?.let { imageCapture.targetRotation = it }
        val destination = viewModel.newCaptureFile()
        val options = ImageCapture.OutputFileOptions.Builder(destination).build()
        imageCapture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onCaptured(destination.absolutePath, viewModel.priceOrNull())
                }

                override fun onError(exception: ImageCaptureException) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Gagal mengambil foto. Coba lagi.")
                    }
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        CameraContent(
            priceInput = priceInput,
            onPriceChange = viewModel::onPriceChange,
            onCapture = { capture() },
            onGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onBack = onBack,
            cameraPreview = {
                CameraPreview(
                    imageCapture = imageCapture,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(ScreenPadding)
        )
    }
}

/**
 * Stateless viewfinder UI. [cameraPreview] is the only part that needs CameraX; previews
 * pass a neutral placeholder instead.
 */
@Composable
fun CameraContent(
    priceInput: String,
    onPriceChange: (String) -> Unit,
    onCapture: () -> Unit,
    onGallery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    cameraPreview: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        cameraPreview()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Pindai Beras",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(2.dp, Color.White, ViewfinderShape)
            )

            Spacer(Modifier.size(Spacing.sm))
            Text(
                text = "Jarak 10–15 cm · cahaya rata",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )

            Spacer(Modifier.weight(1f))

            OutlinedTextField(
                value = priceInput,
                onValueChange = onPriceChange,
                label = { Text("Harga per kg (Rp)") },
                placeholder = { Text("Boleh dikosongkan") },
                singleLine = true,
                shape = CardShape,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = RiceColors.Surface,
                    unfocusedContainerColor = RiceColors.Surface,
                    focusedBorderColor = RiceColors.Primary,
                    unfocusedBorderColor = RiceColors.Outline,
                    focusedTextColor = RiceColors.Text,
                    unfocusedTextColor = RiceColors.Text,
                    focusedLabelColor = RiceColors.Primary,
                    unfocusedLabelColor = RiceColors.TextSecondary,
                    cursorColor = RiceColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.size(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onGallery,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = "Pilih dari galeri",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, RiceColors.Primary, CircleShape)
                        .clickable { onCapture() }
                        .semantics { contentDescription = "Ambil foto" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(RiceColors.Primary)
                    )
                }

                // Keeps the shutter centred.
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.size(Spacing.sm))
        }
    }
}

@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner, imageCapture) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        providerFuture.addListener(
            {
                val provider = runCatching { providerFuture.get() }.getOrNull()
                if (provider != null) {
                    val preview = CameraPreviewUseCase.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    runCatching {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    }
                }
            },
            executor
        )

        onDispose {
            if (providerFuture.isDone) {
                runCatching { providerFuture.get().unbindAll() }
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(RiceColors.SurfaceGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_photo_camera),
                contentDescription = null,
                tint = RiceColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.size(Spacing.lg))
        Text(
            text = "Izin kamera diperlukan",
            style = MaterialTheme.typography.titleLarge,
            color = RiceColors.Text
        )
        Spacer(Modifier.size(Spacing.sm))
        Text(
            text = "RiceCheck memakai kamera untuk memindai beras. Kamu bisa mengizinkan lewat pengaturan.",
            style = MaterialTheme.typography.bodyMedium,
            color = RiceColors.TextSecondary
        )
        Spacer(Modifier.size(Spacing.lg))
        RicePrimaryButton(text = "Buka pengaturan", onClick = onOpenSettings)
        Spacer(Modifier.size(Spacing.sm))
        RiceSecondaryButton(text = "Coba lagi", onClick = onRetry)
        Spacer(Modifier.size(Spacing.sm))
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text(text = "Kembali", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}

// PREVIEW ONLY — the viewfinder area is a flat placeholder; no CameraX is started.
@Preview(showBackground = true, name = "Pindai Beras — viewfinder", heightDp = 900)
@Composable
private fun CameraContentPreview() {
    PreviewSurface {
        CameraContent(
            priceInput = "16000",
            onPriceChange = {},
            onCapture = {},
            onGallery = {},
            onBack = {},
            cameraPreview = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF5A6B57))
                )
            }
        )
    }
}

// PREVIEW ONLY — camera permission denied state.
@Preview(showBackground = true, name = "Pindai Beras — izin ditolak", heightDp = 800)
@Composable
private fun PermissionRationalePreview() {
    PreviewSurface {
        PermissionRationale(
            onBack = {},
            onOpenSettings = {},
            onRetry = {}
        )
    }
}
