package com.github.slamdev.babyroutinetracker.invitation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.ui.theme.extended
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBabyProfileScreen(
    babyId: String,
    onNavigateBack: () -> Unit,
    onUpdateSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvitationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val birthDatePickerState = rememberDatePickerState()
    val dueDatePickerState = rememberDatePickerState()
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var includeDueDate by remember { mutableStateOf(false) }

    // Load baby data when screen starts
    LaunchedEffect(babyId) {
        viewModel.loadBabyForEditing(babyId)
    }

    // Get the baby from the editing state or babies list
    val baby = uiState.editingBaby ?: uiState.babies.find { it.id == babyId }

    // Show loading while baby is being loaded
    if (baby == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Initialize due date state when baby is loaded
    LaunchedEffect(baby) {
        includeDueDate = baby.dueDate != null
    }

    // Handle successful update
    LaunchedEffect(uiState.baby) {
        if (uiState.editingBaby == null && uiState.successMessage != null) {
            onUpdateSuccess()
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Baby Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelEditingBaby()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit ${baby.name}'s Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Update your baby's information and age calculation settings.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Current age display
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.extended.successContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Age Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Real Age: ${baby.getFormattedRealAge()}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    baby.getFormattedAdjustedAge()?.let { adjustedAge ->
                        Text(
                            text = "Corrected Age: $adjustedAge",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    
                    if (baby.wasBornEarly()) {
                        baby.getGestationWeeks()?.let { weeks ->
                            Text(
                                text = "Born at approximately $weeks weeks gestation",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Form
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Baby name input
                    OutlinedTextField(
                        value = uiState.babyName,
                        onValueChange = { viewModel.updateBabyName(it) },
                        label = { Text("Baby's Name") },
                        placeholder = { Text("Enter baby's name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        isError = uiState.errorMessage != null && uiState.babyName.isEmpty(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true
                    )

                    // Birth date input
                    OutlinedTextField(
                        value = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                            .format(uiState.babyBirthDate.toDate()),
                        onValueChange = { },
                        label = { Text("Birth Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showBirthDatePicker = true }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Birth Date")
                            }
                        }
                    )

                    // Due date checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeDueDate,
                            onCheckedChange = { 
                                includeDueDate = it
                                if (!it) {
                                    viewModel.updateBabyDueDate(null)
                                }
                            },
                            enabled = !uiState.isLoading
                        )
                        Text(
                            text = "Include due date (for corrected age calculation)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Due date input (conditional)
                    if (includeDueDate) {
                        OutlinedTextField(
                            value = uiState.babyDueDate?.let { 
                                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(it.toDate())
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Due Date") },
                            placeholder = { Text("Select due date") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDueDatePicker = true }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Due Date")
                                }
                            }
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.cancelEditingBaby()
                        onNavigateBack()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = { 
                        viewModel.updateBabyProfile(
                            baby.id,
                            uiState.babyName, 
                            uiState.babyBirthDate,
                            if (includeDueDate) uiState.babyDueDate else null
                        )
                    },
                    enabled = !uiState.isLoading && uiState.babyName.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isLoading) "Updating..." else "Update")
                }
            }

            // Error message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text(
                                "Dismiss",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Success message
            if (uiState.successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.extended.successContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.successMessage!!,
                            color = MaterialTheme.colorScheme.extended.onSuccessContainer,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Birth date picker dialog
        if (showBirthDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showBirthDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            birthDatePickerState.selectedDateMillis?.let { millis ->
                                viewModel.updateBabyBirthDate(Timestamp(Date(millis)))
                            }
                            showBirthDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBirthDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = birthDatePickerState,
                    showModeToggle = false,
                    title = { Text("Select Birth Date") }
                )
            }
        }

        // Due date picker dialog
        if (showDueDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDueDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dueDatePickerState.selectedDateMillis?.let { millis ->
                                viewModel.updateBabyDueDate(Timestamp(Date(millis)))
                            }
                            showDueDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDueDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = dueDatePickerState,
                    showModeToggle = false,
                    title = { Text("Select Due Date") }
                )
            }
        }
    }
}
