package com.example.healthypettracker.ui.screens.cats

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.canhub.cropper.CropImageView
import com.example.healthypettracker.ui.navigation.BottomBarConfig
import com.example.healthypettracker.ui.navigation.BottomBarState
import java.io.File
import java.io.FileOutputStream

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri {
    val filename = "cat_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

@Composable
fun EditPhotoScreen(
    originalUri: Uri,
    bottomBarState: BottomBarState,
    viewModel: EditPhotoViewModel = hiltViewModel()
) {
    val cropViewRef = remember { mutableStateOf<CropImageView?>(null) }
    val composeContext = LocalContext.current

    LaunchedEffect(Unit) {
        bottomBarState.updateConfig(
            BottomBarConfig.SaveCancel(
                onSave = {
                    val cropped = cropViewRef.value?.getCroppedImage()
                    if (cropped != null) {
                        val uri = saveBitmapToInternalStorage(
                            context = composeContext,
                            bitmap = cropped
                        )
                        viewModel.savePhoto(uri)
                    }
                },
                onCancel = { },
                saveText = "Save"
            )
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            cropViewRef.value = null
            bottomBarState.clear()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            CropImageView(ctx).apply {
                setImageUriAsync(originalUri)
                setAspectRatio(1, 1)
                setFixedAspectRatio(true)
                cropViewRef.value = this
            }
        }
    )
}
