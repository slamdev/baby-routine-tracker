package com.github.slamdev.babyroutinetracker.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
import com.github.slamdev.babyroutinetracker.ui.components.ErrorStateComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDeletionScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDeletionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFinalConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_delete_account)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Warning header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.warning_icon),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.account_deletion_warning_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // What will be deleted section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.what_will_be_deleted),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val deletionItems = listOf(
                        stringResource(R.string.deletion_item_account),
                        stringResource(R.string.deletion_item_baby_profiles),
                        stringResource(R.string.deletion_item_activities),
                        stringResource(R.string.deletion_item_sleep_plans),
                        stringResource(R.string.deletion_item_invitations),
                        stringResource(R.string.deletion_item_settings),
                        stringResource(R.string.deletion_item_shared_access)
                    )
                    
                    deletionItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.bullet_point),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = item,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Important notes section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.important_notes),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val importantNotes = listOf(
                        stringResource(R.string.note_cannot_undo),
                        stringResource(R.string.note_partner_retains_access),
                        stringResource(R.string.note_multiple_parents),
                        stringResource(R.string.note_takes_time),
                        stringResource(R.string.note_auto_signout)
                    )
                    
                    importantNotes.forEach { note ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.warning_emoji),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = note,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // GDPR compliance note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.data_protection),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.gdpr_compliance),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            // Error display
            uiState.errorMessage?.let { errorMessage ->
                CompactErrorDisplay(
                    errorMessage = errorMessage,
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Delete account button
                Button(
                    onClick = { showFinalConfirmation = true },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.delete_my_account),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Cancel button
                OutlinedButton(
                    onClick = onNavigateBack,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }

    // Final confirmation dialog
    if (showFinalConfirmation) {
        var confirmationText by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { 
                if (!uiState.isLoading) {
                    showFinalConfirmation = false 
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.final_confirmation),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.account_deletion_confirmation))
                    Text(
                        text = stringResource(R.string.permanent_action_warning),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(stringResource(R.string.type_delete_confirmation))
                    
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        placeholder = { Text(stringResource(R.string.placeholder_type_delete)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalConfirmation = false
                        viewModel.deleteAccount()
                    },
                    enabled = confirmationText == "DELETE" && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete_account_button),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!uiState.isLoading) {
                            showFinalConfirmation = false 
                        }
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Handle successful deletion
    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onAccountDeleted()
        }
    }
}
