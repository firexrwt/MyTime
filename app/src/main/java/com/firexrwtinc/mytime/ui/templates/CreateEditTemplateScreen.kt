package com.firexrwtinc.mytime.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firexrwtinc.mytime.data.model.TaskTemplate
import com.firexrwtinc.mytime.hexToColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTemplateScreen(
    templateId: Long? = null, // null для создания, не null для редактирования
    onNavigateBack: () -> Unit,
    viewModel: TaskTemplateViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Состояние формы
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF6200EA") }
    var durationMinutes by remember { mutableStateOf("60") }
    var notificationMinutes by remember { mutableStateOf("60") }
    
    var isInitialized by remember { mutableStateOf(templateId == null) }
    
    // Загрузка существующего шаблона для редактирования
    LaunchedEffect(templateId) {
        if (templateId != null && !isInitialized) {
            viewModel.getTemplateById(templateId)?.let { template ->
                name = template.name
                description = template.description
                location = template.location
                equipment = template.equipment
                selectedColor = template.colorHex
                durationMinutes = template.defaultDurationMinutes.toString()
                notificationMinutes = template.notificationMinutesBefore.toString()
                isInitialized = true
            }
        }
    }
    
    // Предустановленные цвета
    val predefinedColors = listOf(
        "#FF6200EA", "#FF3700B3", "#FF018786", "#FF03DAC6",
        "#FFF44336", "#FFE91E63", "#FF9C27B0", "#FF673AB7",
        "#FF2196F3", "#FF00BCD4", "#FF009688", "#FF4CAF50",
        "#FF8BC34A", "#FFCDDC39", "#FFFFC107", "#FFFF9800",
        "#FFFF5722", "#FF795548", "#FF607D8B", "#FF9E9E9E"
    )
    
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
                text = if (templateId == null) "Создать шаблон" else "Редактировать шаблон",
                fontSize = 20.sp,
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
        
        // Название шаблона
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название шаблона") },
            modifier = Modifier.fillMaxWidth(),
            isError = name.isBlank()
        )
        if (name.isBlank()) {
            Text(
                text = "Название обязательно",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Описание
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Местоположение
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Местоположение (необязательно)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Оборудование
        OutlinedTextField(
            value = equipment,
            onValueChange = { equipment = it },
            label = { Text("Оборудование (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Продолжительность по умолчанию
        OutlinedTextField(
            value = durationMinutes,
            onValueChange = { 
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    durationMinutes = it
                }
            },
            label = { Text("Продолжительность (минуты)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("мин") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Уведомления за минут до
        OutlinedTextField(
            value = notificationMinutes,
            onValueChange = { 
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    notificationMinutes = it
                }
            },
            label = { Text("Уведомление за (минуты)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("мин") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Выбор цвета
        Text(
            text = "Цвет шаблона",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(predefinedColors) { color ->
                ColorOption(
                    color = color,
                    isSelected = selectedColor == color,
                    onSelect = { selectedColor = color }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Кнопка сохранения
        Button(
            onClick = {
                val template = if (templateId == null) {
                    // Создание нового шаблона
                    TaskTemplate(
                        name = name.trim(),
                        description = description.trim(),
                        location = location.trim(),
                        equipment = equipment.trim(),
                        colorHex = selectedColor,
                        defaultDurationMinutes = durationMinutes.toIntOrNull() ?: 60,
                        notificationMinutesBefore = notificationMinutes.toIntOrNull() ?: 60
                    )
                } else {
                    // Обновление существующего шаблона
                    TaskTemplate(
                        id = templateId,
                        name = name.trim(),
                        description = description.trim(),
                        location = location.trim(),
                        equipment = equipment.trim(),
                        colorHex = selectedColor,
                        defaultDurationMinutes = durationMinutes.toIntOrNull() ?: 60,
                        notificationMinutesBefore = notificationMinutes.toIntOrNull() ?: 60
                    )
                }
                
                scope.launch {
                    if (templateId == null) {
                        viewModel.createTemplate(template)
                    } else {
                        viewModel.updateTemplate(template)
                    }
                    if (!isLoading && errorMessage == null) {
                        onNavigateBack()
                    }
                }
            },
            enabled = name.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (templateId == null) "Создать шаблон" else "Сохранить изменения")
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(hexToColor(color))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}