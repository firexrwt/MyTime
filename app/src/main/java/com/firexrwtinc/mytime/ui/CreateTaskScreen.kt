package com.firexrwtinc.mytime.ui

// TopAppBar не импортируем, так как он управляется из MainActivity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EuroSymbol
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.firexrwtinc.mytime.R
import com.firexrwtinc.mytime.Screen
import com.firexrwtinc.mytime.data.model.LocationType
import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.hexToColor
import com.firexrwtinc.mytime.ui.TaskViewModel
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
    val lifecycleOwner = LocalLifecycleOwner.current

    // Exact alarms can be revoked by the system/user at any time; re-check on every resume,
    // so returning from the system settings screen immediately hides the warning.
    var canScheduleExact by remember { mutableStateOf(taskViewModel.canScheduleExactAlarms()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canScheduleExact = taskViewModel.canScheduleExactAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // formInitialized persists through child navigation (MapPickerScreen) but resets when
    // this back stack entry is destroyed (user navigates back from CreateTask).
    var formInitialized by rememberSaveable { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf(selectedDateArg) }
    var taskStartTime by remember { mutableStateOf(LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1)) }
    var taskEndTime by remember { mutableStateOf(LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(2)) }
    var taskLocation by remember { mutableStateOf("") }
    var taskLocationType by remember { mutableStateOf(LocationType.NONE) }
    var taskLocationLat by remember { mutableStateOf<Double?>(null) }
    var taskLocationLng by remember { mutableStateOf<Double?>(null) }
    var taskEquipment by remember { mutableStateOf("") }
    var taskPriceString by remember { mutableStateOf("") }
    var selectedReminderHours by remember { mutableStateOf<Int?>(null) }
    var selectedColorHex by remember { mutableStateOf("#82B1FF") }

    val isEditing = taskIdToEdit != 0L
    val existingTaskState by taskViewModel.selectedTask.observeAsState()

    // Initialize form fields only once per navigation instance.
    // formInitialized persists through child navigation (e.g. MapPickerScreen) but resets
    // when this back stack entry is destroyed (navigating back from CreateTask).
    LaunchedEffect(taskIdToEdit) {
        if (formInitialized) return@LaunchedEffect
        formInitialized = true
        if (isEditing) {
            taskViewModel.loadTaskById(taskIdToEdit)
        } else {
            taskTitle = ""
            taskDescription = ""
            taskDate = selectedDateArg
            taskStartTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1)
            taskEndTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(2)
            taskLocation = ""
            taskLocationType = LocationType.NONE
            taskLocationLat = null
            taskLocationLng = null
            taskEquipment = ""
            taskPriceString = ""
            selectedReminderHours = null
            selectedColorHex = "#82B1FF"
            taskViewModel.clearSelectedTask()
        }
    }

    // Этот LaunchedEffect будет реагировать на изменение existingTaskState
    // и заполнять поля, если задача была успешно загружена для редактирования.
    LaunchedEffect(existingTaskState) {
        if (isEditing && existingTaskState != null) {
            existingTaskState?.let { task ->
                taskTitle = task.title
                taskDescription = task.description ?: ""
                taskDate = task.date
                taskStartTime = task.startTime
                taskEndTime = task.endTime
                taskLocation = task.location ?: ""
                taskLocationType = task.locationType
                taskLocationLat = task.locationLat
                taskLocationLng = task.locationLng
                taskEquipment = task.equipment ?: ""
                taskPriceString = task.price?.toString() ?: ""
                selectedReminderHours = task.reminderHoursBefore
                selectedColorHex = task.colorHex
            }
        }
    }


    // Получаем результат из MapPickerScreen через savedStateHandle
    val mapResultLat by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Double>("location_lat")
        ?.observeAsState() ?: remember { mutableStateOf(null) }
    val mapResultLng by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Double>("location_lng")
        ?.observeAsState() ?: remember { mutableStateOf(null) }
    val mapResultAddress by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("location_address")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(mapResultLat, mapResultLng, mapResultAddress) {
        val lat = mapResultLat
        val lng = mapResultLng
        val address = mapResultAddress
        if (lat != null && lng != null && address != null) {
            taskLocationLat = lat
            taskLocationLng = lng
            taskLocation = address
            taskLocationType = LocationType.ADDRESS
            navController.currentBackStackEntry?.savedStateHandle?.apply {
                remove<Double>("location_lat")
                remove<Double>("location_lng")
                remove<String>("location_address")
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
                        description = taskDescription.trim().takeIf { it.isNotBlank() },
                        date = taskDate,
                        startTime = taskStartTime,
                        endTime = taskEndTime,
                        location = taskLocation.takeIf { it.isNotBlank() },
                        locationType = taskLocationType,
                        locationLat = taskLocationLat,
                        locationLng = taskLocationLng,
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
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text(stringResource(R.string.label_task_description)) },
                placeholder = { Text(stringResource(R.string.hint_task_description)) },
                leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null)},
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = taskDate.format(dateFormatter),
                onValueChange = { },
                label = { Text(stringResource(R.string.label_date)) },
                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null)},
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                readOnly = true,
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    datePickerDialog.show()
                                }
                            }
                        }
                    }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = taskStartTime.format(timeFormatter),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.label_start_time)) },
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = null)},
                    modifier = Modifier.weight(1f).clickable { startTimePickerDialog.show() },
                    readOnly = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        startTimePickerDialog.show()
                                    }
                                }
                            }
                        }
                )
                OutlinedTextField(
                    value = taskEndTime.format(timeFormatter),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.label_end_time)) },
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = null)},
                    modifier = Modifier.weight(1f).clickable { endTimePickerDialog.show() },
                    readOnly = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        endTimePickerDialog.show()
                                    }
                                }
                            }
                        }
                )
            }

            // Выбор типа локации
            Text(
                stringResource(R.string.label_location),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LocationType.entries.filter { it != LocationType.NONE }.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        onClick = {
                            taskLocationType = if (taskLocationType == type) LocationType.NONE else type
                            if (taskLocationType == LocationType.URL) {
                                taskLocationLat = null
                                taskLocationLng = null
                            }
                        },
                        selected = taskLocationType == type,
                        label = {
                            Text(
                                when (type) {
                                    LocationType.ADDRESS -> stringResource(R.string.location_type_address)
                                    LocationType.URL -> stringResource(R.string.location_type_url)
                                    else -> ""
                                }
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (type) {
                                    LocationType.ADDRESS -> Icons.Outlined.LocationOn
                                    LocationType.URL -> Icons.Outlined.Link
                                    else -> Icons.Outlined.LocationOn
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Поле адреса или ссылки в зависимости от типа
            when (taskLocationType) {
                LocationType.ADDRESS -> {
                    OutlinedTextField(
                        value = taskLocation,
                        onValueChange = { taskLocation = it },
                        label = { Text(stringResource(R.string.location_type_address)) },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                        placeholder = { Text(stringResource(R.string.hint_location)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        readOnly = taskLocationLat != null
                    )
                    OutlinedButton(
                        onClick = {
                            navController.navigate(
                                Screen.MapPicker.route +
                                    "?lat=${taskLocationLat ?: 0.0}&lng=${taskLocationLng ?: 0.0}"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_pick_on_map))
                    }
                }
                LocationType.URL -> {
                    OutlinedTextField(
                        value = taskLocation,
                        onValueChange = { taskLocation = it },
                        label = { Text(stringResource(R.string.location_type_url)) },
                        leadingIcon = { Icon(Icons.Outlined.Link, contentDescription = null) },
                        placeholder = { Text(stringResource(R.string.hint_location_url)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                LocationType.NONE -> { /* поле скрыто */ }
            }

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
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
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
                            }
                        )
                    }
                }
            }

            // Без разрешения на точные будильники напоминание может не прийти вовремя -
            // предупреждаем сразу, а не когда уведомление уже не сработало.
            if (selectedReminderHours != null && !canScheduleExact) {
                val exactAlarmIntent = taskViewModel.getExactAlarmPermissionIntent()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.exact_alarm_warning),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (exactAlarmIntent != null) {
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(exactAlarmIntent)
                                    } catch (e: Exception) {
                                        // На некоторых прошивках такого системного экрана нет -
                                        // молча остаёмся на месте, приложение не роняем.
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.exact_alarm_warning)
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text(stringResource(R.string.exact_alarm_allow))
                            }
                        }
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
                contentDescription = stringResource(R.string.selected_color),
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
