package com.firexrwtinc.mytime.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firexrwtinc.mytime.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    actualViewModel: SettingsViewModel? = null
) {
    val context = LocalContext.current
    val actualViewModel: SettingsViewModel = actualViewModel ?: viewModel { 
        SettingsViewModel(context.applicationContext as Application) 
    }
    val uiState by actualViewModel.uiState.collectAsState()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationSoundDialog by remember { mutableStateOf(false) }
    var showDefaultDurationDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_section_language)) {
                SettingsItem(
                    title = stringResource(R.string.settings_language),
                    subtitle = when (uiState.selectedLanguage) {
                        Language.ENGLISH -> stringResource(R.string.language_english)
                        Language.RUSSIAN -> stringResource(R.string.language_russian)
                        Language.SYSTEM -> stringResource(R.string.language_system)
                    },
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                Column {
                    SettingsItem(
                        title = stringResource(R.string.settings_notifications_enabled),
                        subtitle = if (uiState.notificationsEnabled) {
                            stringResource(R.string.settings_enabled)
                        } else {
                            stringResource(R.string.settings_disabled)
                        },
                        icon = Icons.Default.Notifications,
                        trailingContent = {
                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { actualViewModel.updateNotificationsEnabled(it) }
                            )
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = uiState.notificationsEnabled,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Column {
                            SettingsItem(
                                title = stringResource(R.string.settings_notification_sound),
                                subtitle = when (uiState.notificationSound) {
                                    NotificationSound.DEFAULT -> stringResource(R.string.notification_sound_default)
                                    NotificationSound.BELL -> stringResource(R.string.notification_sound_bell)
                                    NotificationSound.CHIME -> stringResource(R.string.notification_sound_chime)
                                    NotificationSound.NONE -> stringResource(R.string.notification_sound_none)
                                },
                                icon = Icons.Default.MusicNote,
                                onClick = { showNotificationSoundDialog = true }
                            )
                            
                            SettingsItem(
                                title = stringResource(R.string.settings_vibration),
                                subtitle = if (uiState.vibrationEnabled) {
                                    stringResource(R.string.settings_enabled)
                                } else {
                                    stringResource(R.string.settings_disabled)
                                },
                                icon = Icons.Default.Vibration,
                                trailingContent = {
                                    Switch(
                                        checked = uiState.vibrationEnabled,
                                        onCheckedChange = { actualViewModel.updateVibrationEnabled(it) }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsItem(
                    title = stringResource(R.string.settings_theme),
                    subtitle = when (uiState.selectedTheme) {
                        AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                        AppTheme.LIGHT -> stringResource(R.string.theme_light)
                        AppTheme.DARK -> stringResource(R.string.theme_dark)
                    },
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_tasks)) {
                Column {
                    SettingsItem(
                        title = stringResource(R.string.settings_default_task_duration),
                        subtitle = stringResource(
                            R.string.settings_duration_minutes,
                            uiState.defaultTaskDurationMinutes
                        ),
                        icon = Icons.Default.Schedule,
                        onClick = { showDefaultDurationDialog = true }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.settings_default_reminder),
                        subtitle = when (uiState.defaultReminderHours) {
                            0 -> stringResource(R.string.reminder_option_no)
                            1 -> stringResource(R.string.reminder_option_hours_1)
                            else -> stringResource(R.string.reminder_option_generic_hours, uiState.defaultReminderHours)
                        },
                        icon = Icons.Default.NotificationImportant,
                        trailingContent = {
                            DropdownMenuBox(
                                selectedValue = uiState.defaultReminderHours,
                                options = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                                onValueChange = { actualViewModel.updateDefaultReminderHours(it) },
                                displayText = { hours ->
                                    when (hours) {
                                        0 -> context.getString(R.string.reminder_option_no)
                                        1 -> context.getString(R.string.reminder_option_hours_1)
                                        else -> context.getString(R.string.reminder_option_generic_hours, hours)
                                    }
                                }
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.settings_show_weekends),
                        subtitle = if (uiState.showWeekends) {
                            stringResource(R.string.settings_enabled)
                        } else {
                            stringResource(R.string.settings_disabled)
                        },
                        icon = Icons.Default.Weekend,
                        trailingContent = {
                            Switch(
                                checked = uiState.showWeekends,
                                onCheckedChange = { actualViewModel.updateShowWeekends(it) }
                            )
                        }
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_data)) {
                Column {
                    SettingsItem(
                        title = stringResource(R.string.settings_backup_data),
                        subtitle = stringResource(R.string.settings_backup_description),
                        icon = Icons.Default.Backup,
                        onClick = { 
                            // TODO: Implement backup functionality
                        }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.settings_restore_data),
                        subtitle = stringResource(R.string.settings_restore_description),
                        icon = Icons.Default.Restore,
                        onClick = { 
                            // TODO: Implement restore functionality
                        }
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                Column {
                    SettingsItem(
                        title = stringResource(R.string.settings_version),
                        subtitle = "0.1", // Get from BuildConfig.VERSION_NAME if needed
                        icon = Icons.Default.Info
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.settings_about_description),
                        icon = Icons.Default.Help,
                        onClick = { 
                            // TODO: Show about dialog or navigate to about screen
                        }
                    )
                }
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.selectedLanguage,
            onLanguageSelected = { language ->
                actualViewModel.updateLanguage(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Notification Sound Dialog
    if (showNotificationSoundDialog) {
        NotificationSoundDialog(
            currentSound = uiState.notificationSound,
            onSoundSelected = { sound ->
                actualViewModel.updateNotificationSound(sound)
                showNotificationSoundDialog = false
            },
            onDismiss = { showNotificationSoundDialog = false }
        )
    }

    // Default Duration Dialog
    if (showDefaultDurationDialog) {
        DefaultDurationDialog(
            currentDuration = uiState.defaultTaskDurationMinutes,
            onDurationSelected = { duration ->
                actualViewModel.updateDefaultTaskDuration(duration)
                showDefaultDurationDialog = false
            },
            onDismiss = { showDefaultDurationDialog = false }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.selectedTheme,
            onThemeSelected = { theme ->
                actualViewModel.updateTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        trailingContent?.invoke()
    }
}

@Composable
private fun DropdownMenuBox(
    selectedValue: Int,
    options: List<Int>,
    onValueChange: (Int) -> Unit,
    displayText: (Int) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = displayText(selectedValue),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayText(option)) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                Language.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (language == currentLanguage),
                                onClick = { onLanguageSelected(language) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (language == currentLanguage),
                            onClick = null
                        )
                        Text(
                            text = when (language) {
                                Language.ENGLISH -> stringResource(R.string.language_english)
                                Language.RUSSIAN -> stringResource(R.string.language_russian)
                                Language.SYSTEM -> stringResource(R.string.language_system)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun NotificationSoundDialog(
    currentSound: NotificationSound,
    onSoundSelected: (NotificationSound) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_notification_sound)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                NotificationSound.values().forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (sound == currentSound),
                                onClick = { onSoundSelected(sound) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (sound == currentSound),
                            onClick = null
                        )
                        Text(
                            text = when (sound) {
                                NotificationSound.DEFAULT -> stringResource(R.string.notification_sound_default)
                                NotificationSound.BELL -> stringResource(R.string.notification_sound_bell)
                                NotificationSound.CHIME -> stringResource(R.string.notification_sound_chime)
                                NotificationSound.NONE -> stringResource(R.string.notification_sound_none)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun DefaultDurationDialog(
    currentDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val durations = listOf(15, 30, 45, 60, 90, 120, 180, 240)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_default_task_duration)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                durations.forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (duration == currentDuration),
                                onClick = { onDurationSelected(duration) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (duration == currentDuration),
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.settings_duration_minutes, duration),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                AppTheme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null
                        )
                        Text(
                            text = when (theme) {
                                AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                                AppTheme.LIGHT -> stringResource(R.string.theme_light)
                                AppTheme.DARK -> stringResource(R.string.theme_dark)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}