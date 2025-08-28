package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.preferences.LanguagePreferences

/**
 * Language selector component for the app.
 * Allows users to choose between System Default, English, and Russian.
 */
@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val languagePreferences = remember { LanguagePreferences(context) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // System Default
            LanguageOption(
                label = stringResource(R.string.language_system_default),
                description = getSystemLanguageDescription(),
                isSelected = selectedLanguage == LanguagePreferences.LANGUAGE_SYSTEM,
                onClick = { onLanguageSelected(LanguagePreferences.LANGUAGE_SYSTEM) }
            )
            
            // English
            LanguageOption(
                label = stringResource(R.string.language_english),
                description = stringResource(R.string.language_selector_english),
                isSelected = selectedLanguage == LanguagePreferences.LANGUAGE_ENGLISH,
                onClick = { onLanguageSelected(LanguagePreferences.LANGUAGE_ENGLISH) }
            )
            
            // Russian
            LanguageOption(
                label = stringResource(R.string.language_russian),
                description = stringResource(R.string.language_selector_russian),
                isSelected = selectedLanguage == LanguagePreferences.LANGUAGE_RUSSIAN,
                onClick = { onLanguageSelected(LanguagePreferences.LANGUAGE_RUSSIAN) }
            )
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    description: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (description != null && description != label) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getSystemLanguageDescription(): String {
    val systemLanguage = java.util.Locale.getDefault().language
    return when (systemLanguage) {
        "en" -> stringResource(R.string.language_english)
        "ru" -> stringResource(R.string.language_russian)
        else -> systemLanguage.uppercase()
    }
}

/**
 * Compact language selector for use in profile menus or settings
 */
@Composable
fun CompactLanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        LanguagePreferences.LANGUAGE_SYSTEM to stringResource(R.string.language_system_default),
        LanguagePreferences.LANGUAGE_ENGLISH to stringResource(R.string.language_english),
        LanguagePreferences.LANGUAGE_RUSSIAN to stringResource(R.string.language_russian)
    )
    
    var expanded by remember { mutableStateOf(false) }
    val selectedLanguageLabel = languages.find { it.first == selectedLanguage }?.second 
        ?: stringResource(R.string.language_system_default)
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedLanguageLabel)
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    },
                    leadingIcon = if (code == selectedLanguage) {
                        { 
                            RadioButton(
                                selected = true,
                                onClick = null
                            )
                        }
                    } else null
                )
            }
        }
    }
}
