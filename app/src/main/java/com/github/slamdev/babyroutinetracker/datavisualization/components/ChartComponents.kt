/**
 * Chart components for data visualization using Material 3 theme colors.
 * All colors are derived from MaterialTheme.colorScheme to ensure consistency
 * with the app's theming system and proper support for light/dark themes.
 */
package com.github.slamdev.babyroutinetracker.datavisualization.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.github.slamdev.babyroutinetracker.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.slamdev.babyroutinetracker.datavisualization.DailySleepData
import com.github.slamdev.babyroutinetracker.datavisualization.DailyFeedingData
import com.github.slamdev.babyroutinetracker.datavisualization.DailyDiaperData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleepChart(
    data: List<DailySleepData>,
    modifier: Modifier = Modifier
) {
    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    
    ChartContainer(
        title = "😴 Daily Sleep Hours",
        subtitle = "Total sleep time per day",
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState("No sleep data available")
        } else {
            AndroidView(
                modifier = Modifier.height(200.dp),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                        
                        // X-axis configuration
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = data.size
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        // Y-axis configuration
                        axisLeft.apply {
                            setDrawGridLines(true)
                            gridColor = outlineColor.copy(alpha = 0.3f).toArgb()
                            axisMinimum = 0f
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                    }
                },
                update = { chart ->
                    val entries = data.mapIndexed { index, sleepData ->
                        BarEntry(index.toFloat(), sleepData.totalHours)
                    }
                    
                    val dataSet = BarDataSet(entries, "Sleep Hours").apply {
                        color = primaryColor.toArgb()
                        valueTextColor = onSurfaceColor.copy(alpha = 0.7f).toArgb()
                        valueTextSize = 9f
                    }
                    
                    val barData = BarData(dataSet)
                    barData.barWidth = 0.7f
                    
                    chart.data = barData
                    
                    // Set labels for x-axis
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val labels = data.map { dateFormat.format(it.date) }
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    
                    chart.invalidate()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    "📊 Average: ${String.format("%.1f", data.map { it.totalHours }.average())} hours",
                    "🔢 Total sessions: ${data.sumOf { it.sleepSessions }}"
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    
    ChartContainer(
        title = "🍼 Daily Feedings",
        subtitle = "Number of feedings per day",
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState("No feeding data available")
        } else {
            AndroidView(
                modifier = Modifier.height(200.dp),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                        
                        // X-axis configuration
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = data.size
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        // Y-axis configuration
                        axisLeft.apply {
                            setDrawGridLines(true)
                            gridColor = outlineColor.copy(alpha = 0.3f).toArgb()
                            axisMinimum = 0f
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        
                        // Configure legend colors to match theme
                        legend.apply {
                            textColor = onSurfaceColor.copy(alpha = 0.8f).toArgb()
                            textSize = 10f
                        }
                    }
                },
                update = { chart ->
                    val breastEntries = data.mapIndexed { index, feedingData ->
                        BarEntry(index.toFloat(), feedingData.breastFeedings.toFloat())
                    }
                    
                    val bottleEntries = data.mapIndexed { index, feedingData ->
                        BarEntry(index.toFloat(), feedingData.bottleFeedings.toFloat())
                    }
                    
                    val breastDataSet = BarDataSet(breastEntries, "Breast").apply {
                        color = primaryColor.toArgb()
                        valueTextColor = onSurfaceColor.copy(alpha = 0.7f).toArgb()
                        valueTextSize = 9f
                    }
                    
                    val bottleDataSet = BarDataSet(bottleEntries, "Bottle").apply {
                        color = tertiaryColor.toArgb()
                        valueTextColor = onSurfaceColor.copy(alpha = 0.7f).toArgb()
                        valueTextSize = 9f
                    }
                    
                    val barData = BarData(breastDataSet, bottleDataSet)
                    barData.barWidth = 0.35f
                    
                    chart.data = barData
                    
                    // Group bars together
                    chart.groupBars(-0.5f, 0.3f, 0.05f)
                    
                    // Set labels for x-axis
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val labels = data.map { dateFormat.format(it.date) }
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    
                    chart.invalidate()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    "🤱 Breast: ${data.sumOf { it.breastFeedings }} total",
                    "🍼 Bottle: ${data.sumOf { it.bottleFeedings }} total",
                    "📊 Average: ${String.format("%.1f", data.map { it.totalFeedings }.average())} per day"
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    
    ChartContainer(
        title = "💩 Daily Poops",
        subtitle = "Number of poop diapers per day",
        modifier = modifier
    ) {
        if (data.isEmpty()) {
            EmptyChartState("No poop data available")
        } else {
            AndroidView(
                modifier = Modifier.height(200.dp),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                        
                        // X-axis configuration
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = data.size
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        // Y-axis configuration
                        axisLeft.apply {
                            setDrawGridLines(true)
                            gridColor = outlineColor.copy(alpha = 0.3f).toArgb()
                            axisMinimum = 0f
                            textColor = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10f
                        }
                        
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        
                        // Configure legend colors to match theme
                        legend.apply {
                            textColor = onSurfaceColor.copy(alpha = 0.8f).toArgb()
                            textSize = 10f
                        }
                    }
                },
                update = { chart ->
                    val poopEntries = data.mapIndexed { index, diaperData ->
                        BarEntry(index.toFloat(), diaperData.poopDiapers.toFloat())
                    }
                    
                    val poopDataSet = BarDataSet(poopEntries, "Poop Diapers").apply {
                        color = errorColor.toArgb() // Using error color for poop (brownish/red)
                        valueTextColor = onSurfaceColor.copy(alpha = 0.7f).toArgb()
                        valueTextSize = 9f
                    }
                    
                    val barData = BarData(poopDataSet)
                    chart.data = barData
                    
                    // Set labels for x-axis
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val labels = data.map { dateFormat.format(it.date) }
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    
                    chart.invalidate()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartLegend(
                items = listOf(
                    "💩 Total: ${data.sumOf { it.poopDiapers }} poops",
                    "📊 Average: ${String.format("%.1f", data.map { it.poopDiapers }.average())} per day"
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
