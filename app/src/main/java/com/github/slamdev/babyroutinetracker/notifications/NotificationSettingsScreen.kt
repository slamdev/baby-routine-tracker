package com.github.slamdev.babyroutinetracker.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.model.NotificationPreferences
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.ui.components.ErrorStateComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    babyId: String,
    babyName: String,
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(babyId) {
        viewModel.initializePreferences(babyId)
    }

    // Show success message
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // Auto-clear success after a delay
            kotlinx.coroutines.delay(3000)
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings_title_full)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.partner_notifications_for, babyName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.configure_notifications_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Content based on state
            when (val preferencesState = uiState.preferences) {
                is OptionalUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is OptionalUiState.Error -> {
                    ErrorStateComponent(
                        errorMessage = preferencesState.message,
                        onRetry = { viewModel.initializePreferences(babyId) }
                    )
                }

                is OptionalUiState.Success -> {
                    NotificationPreferencesContent(
                        preferences = preferencesState.data,
                        uiState = uiState,
                        onPreferencesUpdate = viewModel::updatePreferences,
                        onSendTestNotification = { viewModel.sendTestNotification(babyId) },
                        onClearError = viewModel::clearSaveError,
                        onClearTestStatus = viewModel::clearTestStatus
                    )
                }

                is OptionalUiState.Empty -> {
                    // This shouldn't happen as we create default preferences
                    Text(stringResource(R.string.no_preferences_found))
                }
            }
        }
    }
}

@Composable
fun NotificationPreferencesContent(
    preferences: NotificationPreferences,
    uiState: NotificationSettingsUiState,
    onPreferencesUpdate: (NotificationPreferences) -> Unit,
    onSendTestNotification: () -> Unit,
    onClearError: () -> Unit,
    onClearTestStatus: () -> Unit
) {
    var currentPreferences by remember(preferences) { mutableStateOf(preferences) }

    // Error handling
    val saveError = uiState.saveError
    if (saveError != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.error),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = saveError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClearError) {
                        Text(stringResource(R.string.action_dismiss))
                    }
                }
            }
        }
    }

    // Success message
    if (uiState.saveSuccess) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.notification_preferences_saved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Main toggle
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.partner_notifications),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.receive_notifications_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = currentPreferences.enablePartnerNotifications,
                    onCheckedChange = { enabled ->
                        currentPreferences = currentPreferences.copy(enablePartnerNotifications = enabled)
                        onPreferencesUpdate(currentPreferences)
                    }
                )
            }
        }
    }

    // Activity type preferences (only if notifications are enabled)
    if (currentPreferences.enablePartnerNotifications) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.notify_me_for_activities),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Sleep notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.sleep_emoji), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.sleep_activities), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = currentPreferences.notifySleepActivities,
                        onCheckedChange = { enabled ->
                            currentPreferences = currentPreferences.copy(notifySleepActivities = enabled)
                            onPreferencesUpdate(currentPreferences)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feeding notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.feeding_emoji), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.feeding_activities), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = currentPreferences.notifyFeedingActivities,
                        onCheckedChange = { enabled ->
                            currentPreferences = currentPreferences.copy(notifyFeedingActivities = enabled)
                            onPreferencesUpdate(currentPreferences)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Diaper notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.diaper_emoji), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.diaper_changes), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = currentPreferences.notifyDiaperActivities,
                        onCheckedChange = { enabled ->
                            currentPreferences = currentPreferences.copy(notifyDiaperActivities = enabled)
                            onPreferencesUpdate(currentPreferences)
                        }
                    )
                }
            }
        }

        // Quiet hours
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.quiet_hours),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.dont_notify_during_hours),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = currentPreferences.quietHoursEnabled,
                        onCheckedChange = { enabled ->
                            currentPreferences = currentPreferences.copy(quietHoursEnabled = enabled)
                            onPreferencesUpdate(currentPreferences)
                        }
                    )
                }

                if (currentPreferences.quietHoursEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.quiet_hours_format, currentPreferences.quietHoursStart, currentPreferences.quietHoursEnd),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.time_picker_coming_soon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Test notification
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.test_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.test_notifications_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = onSendTestNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.send_test_notification))
                }

                // Test status
                val testStatus = uiState.testNotificationStatus
                if (testStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = testStatus,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onClearTestStatus) {
                                Text(stringResource(R.string.action_dismiss))
                            }
                        }
                    }
                }
            }
        }
    }

    // Loading overlay
    if (uiState.isSaving) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.saving_preferences))
                }
            }
        }
    }
}
