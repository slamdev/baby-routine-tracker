/**
 * Chart components for data visualization using Vico with Material 3 theme colors.
 * All colors are derived from MaterialTheme.colorScheme to ensure consistency
 * with the app's theming system and proper support for light/dark themes.
 */
package com.github.slamdev.babyroutinetracker.datavisualization.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.datavisualization.DailyDiaperData
import com.github.slamdev.babyroutinetracker.datavisualization.DailyFeedingData
import com.github.slamdev.babyroutinetracker.datavisualization.DailySleepData

@Composable
fun SleepChart(
    data: List<DailySleepData>,
    modifier: Modifier = Modifier
) {
    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    
    ChartContainer(
        title = stringResource(R.string.chart_sleep_title_emoji),
        subtitle = stringResource(R.string.chart_subtitle_sleep_time),
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState(stringResource(R.string.chart_no_sleep_data))
        } else {
            val modelProducer = remember { CartesianChartModelProducer() }
            
            LaunchedEffect(data) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(data.map { it.totalHours })
                    }
                }
            }
            
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier.height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    stringResource(R.string.chart_average_hours, data.map { it.totalHours }.average()),
                    stringResource(R.string.chart_total_sessions, data.sumOf { it.sleepSessions })
                )
            )
        }
    }
}

@Composable
fun FeedingChart(
    data: List<DailyFeedingData>,
    modifier: Modifier = Modifier
) {
    // Get theme colors
    val feedingColor = MaterialTheme.colorScheme.secondary
    
    ChartContainer(
        title = stringResource(R.string.chart_feeding_title_emoji),
        subtitle = stringResource(R.string.chart_subtitle_feeding_count),
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState(stringResource(R.string.chart_no_feeding_data))
        } else {
            val modelProducer = remember { CartesianChartModelProducer() }
            
            LaunchedEffect(data) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(data.map { it.breastFeedings })
                        series(data.map { it.bottleFeedings })
                    }
                }
            }
            
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier.height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    stringResource(R.string.chart_total_feedings, data.sumOf { it.totalFeedings }),
                    stringResource(R.string.chart_avg_daily_feedings, data.map { it.totalFeedings }.average())
                )
            )
        }
    }
}

@Composable
fun DiaperChart(
    data: List<DailyDiaperData>,
    modifier: Modifier = Modifier
) {
    // Get theme colors
    val diaperColor = MaterialTheme.colorScheme.tertiary
    
    ChartContainer(
        title = stringResource(R.string.chart_diaper_title_emoji),
        subtitle = stringResource(R.string.chart_subtitle_diaper_count),
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState(stringResource(R.string.chart_no_diaper_data))
        } else {
            val modelProducer = remember { CartesianChartModelProducer() }
            
            LaunchedEffect(data) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(data.map { it.poopDiapers })
                    }
                }
            }
            
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier.height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    stringResource(R.string.chart_total_diapers, data.sumOf { it.totalDiapers }),
                    stringResource(R.string.chart_avg_daily_diapers, data.map { it.totalDiapers }.average())
                )
            )
        }
    }
}

@Composable
private fun ChartContainer(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
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
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun EmptyChartState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.chart_icon),
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ChartLegend(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            Text(
                text = item,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
