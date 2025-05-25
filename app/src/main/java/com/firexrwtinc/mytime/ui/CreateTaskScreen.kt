package com.firexrwtinc.mytime.ui

// TopAppBar не импортируем, так как он управляется из MainActivity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EuroSymbol
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.firexrwtinc.mytime.R
import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.hexToColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    selectedDateArg: LocalDate,
    taskIdToEdit: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var taskTitle by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf(selectedDateArg) }
    var taskStartTime by remember { mutableStateOf(LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1)) }
    var taskEndTime by remember { mutableStateOf(LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(2)) }
    var taskLocation by remember { mutableStateOf("") }
    var taskEquipment by remember { mutableStateOf("") }
    var taskPriceString by remember { mutableStateOf("") }
    var selectedReminderHours by remember { mutableStateOf<Int?>(null) }
    var selectedColorHex by remember { mutableStateOf("#82B1FF") } // Default Blue A100

    val isEditing = taskIdToEdit != 0L
    val existingTaskState by taskViewModel.selectedTask.observeAsState()

    // Этот LaunchedEffect будет реагировать на изменение taskIdToEdit
    // и загружать задачу, если мы в режиме редактирования.
    LaunchedEffect(taskIdToEdit) {
        if (isEditing) {
            taskViewModel.loadTaskById(taskIdToEdit)
        } else {
            // Сброс полей для новой задачи, установка переданной даты
            taskTitle = ""
            taskDate = selectedDateArg
            taskStartTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1)
            taskEndTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(2)
            taskLocation = ""
            taskEquipment = ""
            taskPriceString = ""
            selectedReminderHours = null
            selectedColorHex = "#82B1FF"
            taskViewModel.clearSelectedTask() // Очищаем selectedTask во ViewModel
        }
    }

    // Этот LaunchedEffect будет реагировать на изменение existingTaskState
    // и заполнять поля, если задача была успешно загружена для редактирования.
    LaunchedEffect(existingTaskState) {
        if (isEditing && existingTaskState != null) {
            existingTaskState?.let { task ->
                taskTitle = task.title
                taskDate = task.date
                taskStartTime = task.startTime
                taskEndTime = task.endTime
                taskLocation = task.location ?: ""
                taskEquipment = task.equipment ?: ""
                taskPriceString = task.price?.toString() ?: ""
                selectedReminderHours = task.reminderHoursBefore
                selectedColorHex = task.colorHex
            }
        }
    }


    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            taskDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        taskDate.year,
        taskDate.monthValue - 1,
        taskDate.dayOfMonth
    )

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            taskStartTime = LocalTime.of(hourOfDay, minute)
        },
        taskStartTime.hour,
        taskStartTime.minute,
        true // 24-hour format
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            taskEndTime = LocalTime.of(hourOfDay, minute)
        },
        taskEndTime.hour,
        taskEndTime.minute,
        true // 24-hour format
    )

    val reminderOptions = listOf(
        null to stringResource(R.string.reminder_option_no),
        1 to stringResource(R.string.reminder_option_hours_1),
        2 to stringResource(R.string.reminder_option_hours_2),
        3 to stringResource(R.string.reminder_option_hours_3),
        4 to stringResource(R.string.reminder_option_hours_4),
        5 to stringResource(R.string.reminder_option_hours_5),
        6 to stringResource(R.string.reminder_option_hours_6),
        7 to stringResource(R.string.reminder_option_hours_7),
        8 to stringResource(R.string.reminder_option_hours_8),
        9 to stringResource(R.string.reminder_option_hours_9)
    )
    var reminderDropdownExpanded by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        "#FF8A80", "#FF80AB", "#EA80FC", "#B388FF", "#8C9EFF", "#82B1FF", "#80D8FF", "#84FFFF",
        "#A7FFEB", "#B9F6CA", "#CCFF90", "#F4FF81", "#FFFF8D", "#FFE57F", "#FFD180", "#FF9E80"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // TopAppBar управляется из MainActivity
        floatingActionButton = {
            Button(
                onClick = {
                    if (taskTitle.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_title_empty)) }
                        return@Button
                    }
                    if (taskEndTime.isBefore(taskStartTime)) {
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_end_time_before_start)) }
                        return@Button
                    }

                    val taskToSave = Task(
                        id = if (isEditing) taskIdToEdit else 0L,
                        title = taskTitle.trim(),
                        date = taskDate,
                        startTime = taskStartTime,
                        endTime = taskEndTime,
                        location = taskLocation.takeIf { it.isNotBlank() },
                        equipment = taskEquipment.takeIf { it.isNotBlank() },
                        price = taskPriceString.toDoubleOrNull(),
                        reminderHoursBefore = selectedReminderHours,
                        colorHex = selectedColorHex,
                        isCompleted = if(isEditing) existingTaskState?.isCompleted ?: false else false
                    )

                    if (isEditing) {
                        taskViewModel.updateTask(taskToSave)
                    } else {
                        taskViewModel.insertTask(taskToSave)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Отступы для кнопки
            ) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.button_save_task))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.button_save_task))
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp) // Дополнительные отступы для контента
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Уменьшил немного расстояние между элементами
        ) {
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text(stringResource(R.string.label_task_title)) },
                leadingIcon = { Icon(Icons.Outlined.Title, contentDescription = null)},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = taskDate.format(dateFormatter),
                onValueChange = { /* Read-only */ },
                label = { Text(stringResource(R.string.label_date)) },
                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null)},
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                readOnly = true,
                // Чтобы поле выглядело неактивным для прямого ввода, но кликабельным
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = taskStartTime.format(timeFormatter),
                    onValueChange = { /* Read-only */ },
                    label = { Text(stringResource(R.string.label_start_time)) },
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = null)},
                    modifier = Modifier.weight(1f).clickable { startTimePickerDialog.show() },
                    readOnly = true,
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                OutlinedTextField(
                    value = taskEndTime.format(timeFormatter),
                    onValueChange = { /* Read-only */ },
                    label = { Text(stringResource(R.string.label_end_time)) },
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = null)},
                    modifier = Modifier.weight(1f).clickable { endTimePickerDialog.show() },
                    readOnly = true,
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }

            OutlinedTextField(
                value = taskLocation,
                onValueChange = { taskLocation = it },
                label = { Text(stringResource(R.string.label_location)) },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null)},
                placeholder = { Text(stringResource(R.string.hint_location)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                // TODO: Implement Google Maps Place Picker for location
            )

            OutlinedTextField(
                value = taskEquipment,
                onValueChange = { taskEquipment = it },
                label = { Text(stringResource(R.string.label_equipment)) },
                leadingIcon = { Icon(Icons.Outlined.ListAlt, contentDescription = null)},
                placeholder = { Text(stringResource(R.string.hint_equipment)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                // TODO: Implement equipment presets loading and management
                // TODO: Implement resizable input field for task description/equipment
            )

            OutlinedTextField(
                value = taskPriceString,
                onValueChange = { taskPriceString = it.filter { char -> char.isDigit() || char == '.' || char == ',' } },
                label = { Text(stringResource(R.string.label_price)) },
                leadingIcon = { Icon(Icons.Outlined.EuroSymbol, contentDescription = null)},
                placeholder = { Text(stringResource(R.string.hint_price)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )

            ExposedDropdownMenuBox(
                expanded = reminderDropdownExpanded,
                onExpandedChange = { reminderDropdownExpanded = !reminderDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = reminderOptions.find { it.first == selectedReminderHours }?.second ?: stringResource(R.string.reminder_option_no),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.label_reminder)) },
                    leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null)},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors( // Чтобы выглядело кликабельным
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                ExposedDropdownMenu(
                    expanded = reminderDropdownExpanded,
                    onDismissRequest = { reminderDropdownExpanded = false }
                ) {
                    reminderOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.second) },
                            onClick = {
                                selectedReminderHours = selectionOption.first
                                reminderDropdownExpanded = false
                                // TODO: Implement actual alarm scheduling using AlarmManager
                            }
                        )
                    }
                }
            }

            Text(stringResource(R.string.label_task_color), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly // Равномерное распределение
            ) {
                colorOptions.take(colorOptions.size / 2).forEach { colorHex ->
                    ColorDot(color = hexToColor(colorHex), isSelected = selectedColorHex == colorHex) {
                        selectedColorHex = colorHex
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorOptions.drop(colorOptions.size / 2).forEach { colorHex ->
                    ColorDot(color = hexToColor(colorHex), isSelected = selectedColorHex == colorHex) {
                        selectedColorHex = colorHex
                    }
                }
            }
            // TODO: Добавить более продвинутый Color Picker, если нужно

            Spacer(modifier = Modifier.height(72.dp)) // Отступ для плавающей кнопки сохранения
        }
    }
}

@Composable
fun ColorDot(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp) // Немного уменьшил размер
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape) // Используем outline для рамки
                else Modifier
            )
            .padding(4.dp), // Небольшой внутренний отступ, если нужен
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected Color",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(18.dp) // Уменьшил галочку
            )
        }
    }
}

// Расширение для определения светлости цвета (простое)
fun Color.luminance(): Float {
    // Используем стандартный метод toArgb() для получения int представления цвета
    val colorInt = this.toArgb()
    val red = android.graphics.Color.red(colorInt) / 255f
    val green = android.graphics.Color.green(colorInt) / 255f
    val blue = android.graphics.Color.blue(colorInt) / 255f
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}
