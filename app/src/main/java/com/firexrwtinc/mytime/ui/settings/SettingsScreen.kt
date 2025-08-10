package com.firexrwtinc.mytime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firexrwtinc.mytime.data.model.ThemeMode
import com.firexrwtinc.mytime.hexToColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Инициализация настроек при первом запуске
    LaunchedEffect(Unit) {
        viewModel.initializeSettings()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            
            Text(
                text = "Настройки",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Отображение ошибки
        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Индикатор загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Настройки времени
        SettingsSection(title = "Время и дата") {
            SettingsItem(
                title = "Формат времени",
                subtitle = if (settings.timeFormat24Hour) "24-часовой" else "12-часовой",
                content = {
                    Switch(
                        checked = settings.timeFormat24Hour,
                        onCheckedChange = viewModel::updateTimeFormat
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Настройки темы
        SettingsSection(title = "Внешний вид") {
            SettingsItem(
                title = "Тема приложения",
                subtitle = when (settings.themeMode) {
                    ThemeMode.LIGHT -> "Светлая"
                    ThemeMode.DARK -> "Темная"
                    ThemeMode.SYSTEM -> "Системная"
                },
                content = {
                    ThemeModeSelector(
                        currentMode = settings.themeMode,
                        onModeSelected = viewModel::updateThemeMode
                    )
                }
            )
            
            Divider()
            
            Column {
                SettingsItem(
                    title = "Акцентный цвет",
                    subtitle = settings.accentColor,
                    content = {}
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AccentColorSelector(
                    currentColor = settings.accentColor,
                    onColorSelected = viewModel::updateAccentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Настройки уведомлений
        SettingsSection(title = "Уведомления") {
            SettingsItem(
                title = "Включить уведомления",
                subtitle = if (settings.enableNotifications) "Включено" else "Выключено",
                content = {
                    Switch(
                        checked = settings.enableNotifications,
                        onCheckedChange = viewModel::updateNotificationsEnabled
                    )
                }
            )
            
            Divider()
            
            SettingsItem(
                title = "Уведомлять за",
                subtitle = "${settings.defaultNotificationMinutes} минут",
                content = {
                    NotificationTimeSelector(
                        currentMinutes = settings.defaultNotificationMinutes,
                        onMinutesSelected = viewModel::updateDefaultNotificationMinutes
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Настройки задач
        SettingsSection(title = "Задачи") {
            SettingsItem(
                title = "Показывать выполненные задачи",
                subtitle = if (settings.showCompletedTasks) "Включено" else "Выключено",
                content = {
                    Switch(
                        checked = settings.showCompletedTasks,
                        onCheckedChange = viewModel::updateShowCompletedTasks
                    )
                }
            )
            
            Divider()
            
            SettingsItem(
                title = "Автоудаление выполненных задач",
                subtitle = if (settings.autoDeleteCompletedTasksAfterDays == 0) {
                    "Выключено"
                } else {
                    "Через ${settings.autoDeleteCompletedTasksAfterDays} дней"
                },
                content = {
                    AutoDeleteSelector(
                        currentDays = settings.autoDeleteCompletedTasksAfterDays,
                        onDaysSelected = viewModel::updateAutoDeleteCompletedTasksAfterDays
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = when (currentMode) {
                ThemeMode.LIGHT -> "Светлая"
                ThemeMode.DARK -> "Темная"
                ThemeMode.SYSTEM -> "Системная"
            },
            onValueChange = { },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(when (mode) {
                            ThemeMode.LIGHT -> "Светлая"
                            ThemeMode.DARK -> "Темная"
                            ThemeMode.SYSTEM -> "Системная"
                        })
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AccentColorSelector(
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    val predefinedColors = listOf(
        "#FF6200EA", "#FF3700B3", "#FF018786", "#FF03DAC6",
        "#FFF44336", "#FFE91E63", "#FF9C27B0", "#FF673AB7",
        "#FF2196F3", "#FF00BCD4", "#FF009688", "#FF4CAF50"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(predefinedColors) { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(hexToColor(color))
                    .border(
                        width = if (currentColor == color) 3.dp else 0.dp,
                        color = if (currentColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTimeSelector(
    currentMinutes: Int,
    onMinutesSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 15, 30, 60, 120, 240, 480, 1440) // 0 мин - 24 часа
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (currentMinutes == 0) "Выключено" else if (currentMinutes < 60) "$currentMinutes мин" else "${currentMinutes/60} ч",
            onValueChange = { },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = {
                        Text(if (minutes == 0) "Выключено" else if (minutes < 60) "$minutes мин" else "${minutes/60} ч")
                    },
                    onClick = {
                        onMinutesSelected(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoDeleteSelector(
    currentDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 7, 14, 30, 60, 90) // Выключено, 1 неделя - 3 месяца
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (currentDays == 0) "Выключено" else "$currentDays дней",
            onValueChange = { },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { days ->
                DropdownMenuItem(
                    text = {
                        Text(if (days == 0) "Выключено" else "$days дней")
                    },
                    onClick = {
                        onDaysSelected(days)
                        expanded = false
                    }
                )
            }
        }
    }
}