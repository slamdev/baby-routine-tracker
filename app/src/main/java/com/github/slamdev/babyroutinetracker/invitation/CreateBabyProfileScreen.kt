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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.ui.theme.extended
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBabyProfileScreen(
    onNavigateBack: () -> Unit,
    onCreateSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvitationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val birthDatePickerState = rememberDatePickerState()
    val dueDatePickerState = rememberDatePickerState()
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var includeDueDate by remember { mutableStateOf(false) }

    // Handle successful creation
    LaunchedEffect(uiState.baby) {
        if (uiState.baby != null && uiState.successMessage != null) {
            onCreateSuccess()
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
                title = { Text(stringResource(R.string.baby_profile_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                        text = stringResource(R.string.baby_profile_create_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.baby_profile_create_description),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
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
                        label = { Text(stringResource(R.string.baby_profile_name)) },
                        placeholder = { Text(stringResource(R.string.enter_baby_name)) },
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
                        label = { Text(stringResource(R.string.baby_profile_birth_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showBirthDatePicker = true }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.content_desc_select_birth_date))
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
                            text = stringResource(R.string.baby_profile_due_date_checkbox),
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
                            label = { Text(stringResource(R.string.baby_profile_due_date)) },
                            placeholder = { Text(stringResource(R.string.select_due_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDueDatePicker = true }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.content_desc_select_due_date))
                                }
                            }
                        )
                    }
                }
            }

            // Create button
            Button(
                onClick = { 
                    viewModel.createBabyProfile(
                        uiState.babyName, 
                        uiState.babyBirthDate,
                        if (includeDueDate) uiState.babyDueDate else null
                    )
                },
                enabled = !uiState.isLoading && uiState.babyName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (uiState.isLoading) 
                        stringResource(R.string.create_baby_creating)
                    else 
                        stringResource(R.string.create_baby_create_profile)
                )
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
                                stringResource(R.string.action_dismiss),
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBirthDatePicker = false }
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DatePicker(
                    state = birthDatePickerState,
                    showModeToggle = false,
                    title = { Text(stringResource(R.string.select_birth_date)) }
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDueDatePicker = false }
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DatePicker(
                    state = dueDatePickerState,
                    showModeToggle = false,
                    title = { Text(stringResource(R.string.select_due_date_title)) }
                )
            }
        }
    }
}
