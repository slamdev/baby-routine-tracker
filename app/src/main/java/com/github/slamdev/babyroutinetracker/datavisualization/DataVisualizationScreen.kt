package com.github.slamdev.babyroutinetracker.datavisualization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.github.slamdev.babyroutinetracker.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.datavisualization.components.DateRangeSelector
import com.github.slamdev.babyroutinetracker.datavisualization.components.FeedingChart
import com.github.slamdev.babyroutinetracker.datavisualization.components.SleepChart
import com.github.slamdev.babyroutinetracker.datavisualization.components.DiaperChart
import com.github.slamdev.babyroutinetracker.ui.components.ErrorStateComponent

@Composable
fun DataVisualizationScreen(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: DataVisualizationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize data loading when screen is first displayed
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }
    
    // Responsive layout that adapts to screen orientation
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight
        val padding = if (isLandscape) 16.dp else 24.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = stringResource(R.string.data_visualization_title),
                fontSize = if (isLandscape) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Error handling
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                ErrorStateComponent(
                    errorMessage = errorMessage,
                    onRetry = { viewModel.retryLoading(babyId) },
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            
            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.loading_data),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // Date range selector
                DateRangeSelector(
                    selectedRange = uiState.selectedDateRange,
                    onRangeSelected = { range ->
                        viewModel.onDateRangeChanged(babyId, range)
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Sleep chart
                SleepChart(
                    data = uiState.sleepData,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Feeding chart
                FeedingChart(
                    data = uiState.feedingData,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Diaper chart
                DiaperChart(
                    data = uiState.diaperData,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Summary statistics
                if (uiState.sleepData.isNotEmpty() || uiState.feedingData.isNotEmpty() || uiState.diaperData.isNotEmpty()) {
                    DataSummaryCard(
                        sleepData = uiState.sleepData,
                        feedingData = uiState.feedingData,
                        diaperData = uiState.diaperData,
                        dateRange = uiState.selectedDateRange,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DataSummaryCard(
    sleepData: List<DailySleepData>,
    feedingData: List<DailyFeedingData>,
    diaperData: List<DailyDiaperData>,
    dateRange: DateRange,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val rangeLabel = when(dateRange) {
                DateRange.LAST_WEEK -> stringResource(R.string.range_last_week)
                DateRange.LAST_TWO_WEEKS -> stringResource(R.string.range_last_2_weeks)
                DateRange.LAST_MONTH -> stringResource(R.string.range_last_month)
            }
            Text(
                text = stringResource(R.string.summary_title, rangeLabel),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Sleep summary
            if (sleepData.isNotEmpty()) {
                val avgSleepHours = sleepData.map { it.totalHours }.average()
                val totalSleepSessions = sleepData.sumOf { it.sleepSessions }
                val maxSleepDay = sleepData.maxByOrNull { it.totalHours }
                
                SummaryRow(
                    icon = "😴",
                    title = stringResource(R.string.summary_section_sleep),
                    stats = listOf(
                        stringResource(R.string.summary_sleep_avg_hours_per_day, avgSleepHours),
                        stringResource(R.string.summary_sleep_sessions, totalSleepSessions),
                        stringResource(R.string.summary_sleep_best_day, maxSleepDay?.totalHours ?: 0f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Feeding summary
            if (feedingData.isNotEmpty()) {
                val avgFeedings = feedingData.map { it.totalFeedings }.average()
                val totalBreastFeedings = feedingData.sumOf { it.breastFeedings }
                val totalBottleFeedings = feedingData.sumOf { it.bottleFeedings }
                
                SummaryRow(
                    icon = "🍼",
                    title = stringResource(R.string.summary_section_feeding),
                    stats = listOf(
                        stringResource(R.string.summary_feeding_avg_per_day, avgFeedings),
                        stringResource(R.string.summary_feeding_breast_count, totalBreastFeedings),
                        stringResource(R.string.summary_feeding_bottle_count, totalBottleFeedings)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Poop summary
            if (diaperData.isNotEmpty()) {
                val avgPoops = diaperData.map { it.poopDiapers }.average()
                val totalPoopDiapers = diaperData.sumOf { it.poopDiapers }
                
                SummaryRow(
                    icon = "💩",
                    title = stringResource(R.string.summary_section_poops),
                    stats = listOf(
                        stringResource(R.string.summary_poops_total, totalPoopDiapers),
                        stringResource(R.string.summary_poops_avg_per_day, avgPoops)
                    )
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: String,
    title: String,
    stats: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            stats.forEach { stat ->
                Text(
                    text = stringResource(R.string.summary_bullet_format, stat),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
