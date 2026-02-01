package com.example.healthypettracker.ui.screens.cats

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canhub.cropper.CropImageView
import com.example.healthypettracker.di.AppContainer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPhotoScreen(
    originalUri: Uri,
    catId: Long,
    container: AppContainer,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    viewModel: EditPhotoViewModel = viewModel(
        factory = EditPhotoViewModel.Factory(container.catRepository)
    )
) {
    val cropViewRef = remember { mutableStateOf<CropImageView?>(null) }
    val composeContext = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            factory = { ctx ->
                CropImageView(ctx).apply {
                    setImageUriAsync(originalUri)
                    setAspectRatio(1, 1)
                    setFixedAspectRatio(true)
                    cropViewRef.value = this
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }

            TextButton(onClick = {
                val cropped = cropViewRef.value?.getCroppedImage()
                if (cropped != null) {
                    val uri = saveBitmapToInternalStorage(
                        context = composeContext,
                        bitmap = cropped
                    )
                    viewModel.savePhoto(catId, uri)
                    onSave()
                }
            }) {
                Text("Save")
            }
        }
    }
}
