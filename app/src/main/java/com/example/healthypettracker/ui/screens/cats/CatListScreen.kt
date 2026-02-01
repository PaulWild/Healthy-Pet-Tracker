package com.example.healthypettracker.ui.screens.cats

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.di.AppContainer
import com.example.healthypettracker.ui.components.CatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatListScreen(
    container: AppContainer,
    onNavigateToAddCat: () -> Unit,
    onNavigateToCatDetail: (Long) -> Unit,
    onNavigateToEditPhoto: (Long, Uri) -> Unit,
    viewModel: CatListViewModel = viewModel(
        factory = CatListViewModel.Factory(container.catRepository)
    )
) {
    val cats by viewModel.cats.collectAsState()
    var catToDelete by remember { mutableStateOf<Cat?>(null) }

    var selectedCatId by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && selectedCatId != null) {

            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onNavigateToEditPhoto(selectedCatId!!, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cats") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCat) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add cat"
                )
            }
        }
    ) { paddingValues ->
        if (cats.isEmpty()) {
            EmptyCatList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cats, key = { it.id }) { cat ->
                    CatCard(
                        cat = cat,
                        onClick = { onNavigateToCatDetail(cat.id) },
                        onSelectCatImage = {
                            selectedCatId = cat.id
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onDeleteClick = { catToDelete = cat }
                    )
                }
            }
        }
    }

    catToDelete?.let { cat ->
        DeleteCatDialog(
            catName = cat.name,
            onConfirm = {
                viewModel.deleteCat(cat)
                catToDelete = null
            },
            onDismiss = { catToDelete = null }
        )
    }
}

@Composable
private fun EmptyCatList(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No cats yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add your first cat",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DeleteCatDialog(
    catName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $catName?") },
        text = {
            Text("This will delete all data associated with $catName, including medicine schedules, weight entries, and diary notes. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
