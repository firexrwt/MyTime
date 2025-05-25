package com.firexrwtinc.mytime

import android.annotation.SuppressLint
import android.app.Application // Необходим для TaskViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.viewModels // Этот импорт больше не нужен, используем viewModel() из compose
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Для получения ViewModel в Composable
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.ui.CreateTaskScreen // Убедитесь, что этот файл создан и импорт корректен
import com.firexrwtinc.mytime.ui.TaskViewModel
import com.firexrwtinc.mytime.ui.theme.MyTimeTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import java.time.format.TextStyle as JavaTextStyle

/* TODO: Доделать WeekScreen
*  TODO: Сделать переключение на следующий день в DayScreen
*  TODO: При двойном нажатии на день в Month или WeekScreen открыть DayScreen соответствующего дня(в соответствии с логикой YearScreen)
*  TODO: Надо сделать везде красивые анимации
*  TODO: Надо сделать возможность создания шаблонов для задач
*  TODO: Надо сделать возможность редактирования шаблонов
*  TODO: Надо сделать возможность удаления шаблонов
*  TODO: Надо сделать возможность для загрузки шаблонов на странице создания задач
*  TODO: Надо сделать интеграцию с базой данных(для шаблонов и самих задач)
*  TODO: Надо сделать логику для уведомлений за час до будильника(будильник должен звонить от 0 до 9 часов до задачи в зависимости от выбора пользователя на экране создания задач)
*  TODO: Добавить опцию редактирования уже существующих задач
*  TODO: Надо сделать опцию удаления существующих задач
*  TODO: Надо сделать настройки в сэндвич меню */

fun hexToColor(hex: String): Color {
    val colorString = if (hex.startsWith("#")) hex else "#$hex"
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}

sealed class Screen(val route: String, val resourceId: Int? = null, val icon: ImageVector? = null) {
    object DayView : Screen("day", R.string.screen_day, Icons.Filled.CalendarViewDay)
    object WeekView : Screen("week", R.string.screen_week, Icons.Filled.CalendarViewWeek)
    object MonthView : Screen("month", R.string.screen_month, Icons.Filled.CalendarMonth)
    object YearView : Screen("year", R.string.screen_year, Icons.Filled.CalendarToday)
    object CreateTask : Screen("create_task")
}

val navItems = listOf(
    Screen.DayView,
    Screen.WeekView,
    Screen.MonthView,
    Screen.YearView
)

const val BASE_HOUR_HEIGHT_DP = 60
val MIN_HOUR_HEIGHT = 20.dp
val MAX_HOUR_HEIGHT = 240.dp
const val MIN_SCALE_FACTOR = 0.4f
const val MAX_SCALE_FACTOR = 4.0f

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTimeTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val taskViewModel: TaskViewModel = viewModel(
                    factory = TaskViewModel.TaskViewModelFactory(application)
                )

                var displayedDate by remember { mutableStateOf(LocalDate.now()) }
                var currentTopBarTitle by remember { mutableStateOf("") }
                val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.getDefault()) }
                val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM uuuu", Locale.getDefault()) }
                val yearFormatter = remember { DateTimeFormatter.ofPattern("uuuu", Locale.getDefault()) }

                // Эта лямбда ожидает ДВА аргумента
                val updateTitle: (Screen, LocalDate) -> Unit = { screen, date ->
                    currentTopBarTitle = when (screen) {
                        is Screen.DayView -> date.format(dayFormatter)
                        is Screen.WeekView -> {
                            val calendar = Calendar.getInstance(); calendar.set(date.year, date.monthValue -1, date.dayOfMonth)
                            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                            "Неделя $weekOfYear, ${date.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())} ${date.year}"
                        }
                        is Screen.MonthView -> YearMonth.from(date).format(monthFormatter)
                        is Screen.YearView -> date.format(yearFormatter)
                        is Screen.CreateTask -> {
                            // Логика определения "Новая" или "Редактировать" находится здесь,
                            // на основе состояния taskViewModel.selectedTask.value
                            val editingTask = taskViewModel.selectedTask.value
                            if (editingTask != null && editingTask.id != 0L) {
                                "Редактировать задачу"
                            } else {
                                "Новая задача"
                            }
                        }
                    }
                }

                // Этот LaunchedEffect будет реагировать на изменение маршрута, даты ИЛИ выбранной задачи в ViewModel
                LaunchedEffect(navController.currentBackStackEntryAsState().value, displayedDate, taskViewModel.selectedTask.value) {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route

                    if (currentRoute?.startsWith(Screen.CreateTask.route.split("?").first()) == true) {
                        // Для экрана CreateTask, дата не так важна для заголовка, как сам факт, что это этот экран.
                        // Логика "Новая/Редактировать" уже внутри updateTitle
                        updateTitle(Screen.CreateTask, LocalDate.now())
                    } else {
                        val screen = navItems.find { it.route == currentRoute } ?: Screen.DayView
                        updateTitle(screen, displayedDate)
                    }
                }

                ModalNavigationDrawer(drawerState = drawerState, drawerContent = { ModalDrawerSheet {
                    Text("Элемент меню 1 (пример)", modifier = Modifier.padding(16.dp))
                    Text("Элемент меню 2 (пример)", modifier = Modifier.padding(16.dp))
                } }) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(currentTopBarTitle) },
                                navigationIcon = {
                                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                                    if (currentRoute?.startsWith(Screen.CreateTask.route.split("?").first()) == true ||
                                        (navController.previousBackStackEntry != null && navItems.none { it.route == currentRoute })) {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(Icons.Filled.ArrowBackIosNew, stringResource(R.string.desc_back))
                                        }
                                    } else {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Filled.Menu, stringResource(R.string.desc_open_drawer))
                                        }
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                            if (currentRoute?.startsWith(Screen.CreateTask.route.split("?").first()) != true) {
                                NavigationBar {
                                    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                                    navItems.forEach { screen ->
                                        NavigationBarItem(
                                            icon = { Icon(screen.icon!!, contentDescription = stringResource(screen.resourceId!!)) },
                                            label = { Text(stringResource(screen.resourceId!!)) },
                                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        AppNavHost(
                            navController = navController,
                            taskViewModel = taskViewModel,
                            innerPadding = innerPadding,
                            displayedDate = displayedDate,
                            onDateChange = { displayedDate = it },
                            updateTitle = updateTitle // Передаем (Screen, LocalDate) -> Unit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    innerPadding: PaddingValues,
    displayedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    updateTitle: (Screen, LocalDate) -> Unit // Ожидаем (Screen, LocalDate) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DayView.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.DayView.route) {
            DayScreen(currentDate = displayedDate, onDateChange = onDateChange,
                updateTitle = { date -> updateTitle(Screen.DayView, date) }, // Вызываем с двумя аргументами
                taskViewModel = taskViewModel, navController = navController
            )
        }
        composable(Screen.WeekView.route) {
            WeekScreen(currentDate = displayedDate, onDateChange = onDateChange,
                updateTitle = { date -> updateTitle(Screen.WeekView, date) } // Вызываем с двумя аргументами
            )
        }
        composable(Screen.MonthView.route) {
            MonthScreen(currentDate = displayedDate, onDateChange = onDateChange,
                updateTitle = { date -> updateTitle(Screen.MonthView, date) } // Вызываем с двумя аргументами
            )
        }
        composable(Screen.YearView.route) {
            YearScreen(currentDate = displayedDate, onDateChange = onDateChange,
                updateTitle = { date -> updateTitle(Screen.YearView, date) }, // Вызываем с двумя аргументами
                onMonthSelected = { yearMonth ->
                    onDateChange(yearMonth.atDay(1)); navController.navigate(Screen.MonthView.route) { popUpTo(Screen.MonthView.route) { inclusive = true } }
                }
            )
        }
        composable(Screen.CreateTask.route + "?date={date}&taskId={taskId}") { backStackEntry ->
            val selectedDateStr = backStackEntry.arguments?.getString("date")
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
            val selectedDate = selectedDateStr?.let { LocalDate.parse(it) } ?: displayedDate

            LaunchedEffect(taskId) {
                // Этот LaunchedEffect теперь НЕ вызывает updateTitle напрямую.
                // Он только загружает/очищает задачу.
                // Главный LaunchedEffect в MainActivity отреагирует на изменение taskViewModel.selectedTask.value и обновит заголовок.
                if (taskId != 0L) {
                    taskViewModel.loadTaskById(taskId)
                } else {
                    taskViewModel.clearSelectedTask()
                }
            }
            CreateTaskScreen( // Убедитесь, что импорт com.firexrwtinc.mytime.ui.CreateTaskScreen есть
                navController = navController, taskViewModel = taskViewModel,
                selectedDateArg = selectedDate, taskIdToEdit = taskId
            )
        }
    }
}

@Composable
fun DayScreen(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    updateTitle: (LocalDate) -> Unit,
    taskViewModel: TaskViewModel,
    navController: NavHostController
) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
        taskViewModel.loadTasksForDate(currentDate)
    }
    val tasksForDay: List<Task> by taskViewModel.tasksForDateLiveData.observeAsState(initial = emptyList())
    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("E", Locale.getDefault()) }
    val dayOfMonthFormatter = remember { DateTimeFormatter.ofPattern("dd", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayScreenHeader(
                dayOfWeek = currentDate.format(dayOfWeekFormatter).uppercase(Locale.getDefault()),
                dayOfMonth = currentDate.format(dayOfMonthFormatter),
                pendingTasksCount = tasksForDay.count { task -> !task.isCompleted && task.endTime.isAfter(LocalTime.now()) }
            )
            HourTimeline(currentDate = currentDate, tasks = tasksForDay, modifier = Modifier.weight(1f))
        }
        FloatingActionButton(
            onClick = { navController.navigate(Screen.CreateTask.route + "?date=${currentDate}&taskId=0") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, stringResource(id = R.string.desc_add_task))
        }
    }
}

// ... (Остальной код: DayScreenHeader, getPendingTasksString, HourTimeline, HourLabelsColumn, TaskEntry, DayScreenPreview, WeekScreen, MonthScreen, YearScreen, MonthCellForYearView, getDaysOfWeek, getDaysForMonthCalendar, MonthNavigationHeader, DayCell)
// ... ОНИ НЕ ИЗМЕНИЛИСЬ И НЕ ДОЛЖНЫ ВЫЗЫВАТЬ ПРОБЛЕМ С updateTitle ...

@Composable
fun DayScreenHeader(dayOfWeek: String, dayOfMonth: String, pendingTasksCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dayOfWeek, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = dayOfMonth, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "$pendingTasksCount ${getPendingTasksString(pendingTasksCount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun getPendingTasksString(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "ожидающая задача"
        count % 10 in 2..4 && (count % 100 < 10 || count % 100 >= 20) -> "ожидающие задачи"
        else -> "ожидающих задач"
    }
}

@Composable
fun HourTimeline(
    currentDate: LocalDate,
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    var scaleFactor by remember { mutableStateOf(1f) }
    val scaledHourHeight = remember(scaleFactor) {
        (BASE_HOUR_HEIGHT_DP * scaleFactor).dp.coerceIn(MIN_HOUR_HEIGHT, MAX_HOUR_HEIGHT)
    }

    val hours = (0..23).toList()
    val timelineStartPadding = 56.dp
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val currentTimeLineColor = MaterialTheme.colorScheme.primary
    val totalContentHeight = remember(scaledHourHeight) { scaledHourHeight * hours.size }

    val displayTasks = if (tasks.isEmpty()) {
        listOf(
            Task(title = "Пример: Занятия", date = currentDate, startTime = LocalTime.of(9,0), endTime = LocalTime.of(10,0), colorHex = "#4A90E2", price = null, equipment = null, location = null, reminderHoursBefore = null),
            Task(title = "Пример: Обед", date = currentDate, startTime = LocalTime.of(13,0), endTime = LocalTime.of(13,45), colorHex = "#2E8B57", price = null, equipment = null, location = null, reminderHoursBefore = null)
        )
    } else {
        tasks
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, _, gestureZoom, _ ->
                    val oldScaleFactor = scaleFactor
                    scaleFactor = (oldScaleFactor * gestureZoom).coerceIn(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR)
                    if (oldScaleFactor != scaleFactor) {
                        val effectiveZoomDelta = scaleFactor / oldScaleFactor
                        val currentScrollPx = scrollState.value.toFloat()
                        val contentYAtCentroidPx = currentScrollPx + centroid.y
                        val newScrollPx = (contentYAtCentroidPx * effectiveZoomDelta) - centroid.y
                        coroutineScope.launch {
                            scrollState.scrollTo(newScrollPx.roundToInt())
                        }
                    }
                }
            }
    ) {
        HourLabelsColumn(
            scaledHourHeight = scaledHourHeight,
            modifier = Modifier
                .width(timelineStartPadding)
                .fillMaxHeight()
                .verticalScroll(scrollState)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.height(totalContentHeight).fillMaxWidth()) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val hourHeightPx = with(density) { scaledHourHeight.toPx() }
                    (0..23).forEach { hour ->
                        val y = hour * hourHeightPx
                        val lineAlpha = when {
                            scaledHourHeight < 30.dp && hour % 4 != 0 -> 0.05f
                            scaledHourHeight < 30.dp && hour % 4 == 0 -> 0.2f
                            scaledHourHeight < 45.dp && hour % 2 != 0 -> 0.08f
                            scaledHourHeight < 45.dp && hour % 2 == 0 -> 0.2f
                            else -> 0.15f
                        }
                        val strokeWidthFactor = when {
                            scaledHourHeight < 30.dp && hour % 4 == 0 -> 1.5f
                            scaledHourHeight < 45.dp && hour % 2 == 0 -> 1.5f
                            else -> 1f
                        }
                        drawLine(color = lineColor.copy(alpha = lineAlpha), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokeWidthFactor)
                        if (scaledHourHeight >= 75.dp) {
                            val y30 = y + hourHeightPx / 2
                            drawLine(color = lineColor.copy(alpha = 0.07f), start = Offset(0f, y30), end = Offset(size.width, y30), strokeWidth = 0.8f)
                        }
                    }
                    if (currentDate == LocalDate.now()) {
                        val now = LocalTime.now()
                        val minutesFromMidnight = now.hour * 60 + now.minute
                        val totalDayMinutes = 24f * 60f
                        val currentY = (minutesFromMidnight / totalDayMinutes) * (hourHeightPx * 24)
                        drawLine(color = currentTimeLineColor, start = Offset(with(density) { (-8).dp.toPx() }, currentY), end = Offset(size.width, currentY), strokeWidth = 2f)
                        drawCircle(color = currentTimeLineColor, radius = with(density) { 4.dp.toPx() }, center = Offset(with(density) { (-8).dp.toPx() }, currentY))
                    }
                }

                displayTasks.forEach { task ->
                    val taskStartMinutes = task.startTime.hour * 60 + task.startTime.minute
                    val taskEndMinutes = task.endTime.hour * 60 + task.endTime.minute
                    val durationMinutes = taskEndMinutes - taskStartMinutes
                    if (durationMinutes > 0) {
                        val hourHeightPx = with(density) { scaledHourHeight.toPx() }
                        val taskYOffsetPx = (taskStartMinutes / 60f) * hourHeightPx
                        val taskHeightPx = (durationMinutes / 60f) * hourHeightPx
                        TaskEntry(
                            title = task.title,
                            timeRange = "${task.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${task.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            color = hexToColor(task.colorHex),
                            modifier = Modifier
                                .padding(start = 4.dp, end = 4.dp)
                                .offset(y = with(density) { taskYOffsetPx.toDp() })
                                .height(with(density) { taskHeightPx.toDp() })
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HourLabelsColumn(
    scaledHourHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        (0..23).forEach { hour ->
            Box(
                modifier = Modifier.fillMaxWidth().height(scaledHourHeight),
                contentAlignment = Alignment.TopEnd
            ) {
                val showThisHourLabel = when {
                    scaledHourHeight < 30.dp -> hour % 4 == 0
                    scaledHourHeight < 45.dp -> hour % 2 == 0
                    else -> true
                }
                if (showThisHourLabel) {
                    Text(text = String.format(Locale.getDefault(), "%02d:00", hour), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 8.dp, top = if (scaledHourHeight < 25.dp) 0.dp else 2.dp))
                }
                if (scaledHourHeight >= 150.dp) {
                    Box(modifier = Modifier.fillMaxWidth().height(scaledHourHeight / 2).align(Alignment.BottomEnd).padding(end = 8.dp), contentAlignment = Alignment.TopEnd) {
                        Text(text = String.format(Locale.getDefault(), "%02d:30", hour), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)), modifier = Modifier.padding(top = 0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEntry(title: String, timeRange: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .border(1.dp, Color.Black.copy(alpha = 0.2f), MaterialTheme.shapes.small)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = timeRange, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DayScreenPreview() {
    MyTimeTheme {
        val sampleTasks = listOf(
            Task(id = 1L, title = "Превью: Встреча", date = LocalDate.now(), startTime = LocalTime.of(9,0), endTime = LocalTime.of(10,30), colorHex = "#5EABF2", price = 50.0, equipment = null, location = null, reminderHoursBefore = null),
            Task(id = 2L, title = "Превью: Обед", date = LocalDate.now(), startTime = LocalTime.of(13,0), endTime = LocalTime.of(14,0), colorHex = "#6BCB77", reminderHoursBefore = 1, price = null, equipment = null, location = null)
        )
        val app = LocalContext.current.applicationContext as Application
        DayScreen(
            currentDate = LocalDate.now(),
            onDateChange = {},
            updateTitle = {},
            taskViewModel = TaskViewModel(app),
            navController = rememberNavController()
        )
    }
}

@Composable
fun WeekScreen(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit, updateTitle: (LocalDate) -> Unit) {
    LaunchedEffect(currentDate) { updateTitle(currentDate) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Экран Недели", style = MaterialTheme.typography.headlineMedium)
        Text("(Начало недели с): ${currentDate.with(DayOfWeek.MONDAY).format(DateTimeFormatter.ISO_LOCAL_DATE)}")
        Text("В разработке...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun MonthScreen(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit, updateTitle: (LocalDate) -> Unit) {
    val currentYearMonth = remember(currentDate) { YearMonth.from(currentDate) }
    LaunchedEffect(currentYearMonth) { updateTitle(currentYearMonth.atDay(1)) }
    val daysOfWeek = remember { getDaysOfWeek() }
    val calendarDays = remember(currentYearMonth) { getDaysForMonthCalendar(currentYearMonth) }
    Column(modifier = Modifier.fillMaxSize()) {
        MonthNavigationHeader(currentYearMonth = currentYearMonth,
            onPreviousMonth = { onDateChange(currentYearMonth.minusMonths(1).atDay(1)) },
            onNextMonth = { onDateChange(currentYearMonth.plusMonths(1).atDay(1)) }
        )
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { dayLabel -> Text(text = dayLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)) {
            items(calendarDays.size) { index ->
                val date = calendarDays[index]
                DayCell(date = date, isCurrentMonth = date?.month == currentYearMonth.month, isToday = date == LocalDate.now(),
                    onClick = { if (date != null) { onDateChange(date) } }
                )
            }
        }
    }
}

@Composable
fun YearScreen(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit, updateTitle: (LocalDate) -> Unit, onMonthSelected: (YearMonth) -> Unit) {
    val currentYear = remember(currentDate) { currentDate.year }
    LaunchedEffect(currentYear) { updateTitle(LocalDate.of(currentYear, 1, 1)) }
    val months = remember(currentYear) { (1..12).map { YearMonth.of(currentYear, it) } }
    val monthNameFormatter = remember { DateTimeFormatter.ofPattern("LLLL", Locale.getDefault()) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { onDateChange(LocalDate.of(currentYear - 1, currentDate.monthValue,1).withDayOfMonth(1)) }) { Icon(Icons.Filled.ArrowBackIosNew, contentDescription = stringResource(R.string.desc_previous_year)) }
            Text(text = currentYear.toString(), style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { onDateChange(LocalDate.of(currentYear + 1, currentDate.monthValue,1).withDayOfMonth(1)) }) { Icon(Icons.Filled.ArrowForwardIos, contentDescription = stringResource(R.string.desc_next_year)) }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            items(months.size) { index ->
                val yearMonth = months[index]
                MonthCellForYearView(yearMonth = yearMonth, monthName = yearMonth.format(monthNameFormatter).replaceFirstChar { it.titlecase(Locale.getDefault()) }, isCurrentMonth = yearMonth == YearMonth.now(),
                    onClick = { onMonthSelected(yearMonth) }
                )
            }
        }
    }
}

@Composable
fun MonthCellForYearView(yearMonth: YearMonth, monthName: String, isCurrentMonth: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .background(if (isCurrentMonth) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
            .border(1.dp, if (isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = monthName, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
    }
}

fun getDaysOfWeek(locale: Locale = Locale.getDefault()): List<String> {
    val daysOfWeek = mutableListOf<String>()
    var day = DayOfWeek.MONDAY
    for (i in 0 until 7) {
        daysOfWeek.add(day.getDisplayName(JavaTextStyle.SHORT, locale).uppercase(locale))
        day = day.plus(1)
    }
    return daysOfWeek
}

fun getDaysForMonthCalendar(yearMonth: YearMonth): List<LocalDate?> {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeekOfFirstDayValue = firstDayOfMonth.dayOfWeek.value
    val leadingEmptyCells = (dayOfWeekOfFirstDayValue - DayOfWeek.MONDAY.value + 7) % 7
    val calendarDays = mutableListOf<LocalDate?>()
    for (i in 0 until leadingEmptyCells) { calendarDays.add(null) }
    for (day in 1..daysInMonth) { calendarDays.add(yearMonth.atDay(day)) }
    while (calendarDays.size % 7 != 0) { calendarDays.add(null) }
    return calendarDays
}

@Composable
fun MonthNavigationHeader(currentYearMonth: YearMonth, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM uuuu", Locale.getDefault()) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPreviousMonth) { Icon(Icons.Filled.ArrowBackIosNew, contentDescription = stringResource(R.string.desc_previous_month)) }
        Text(text = currentYearMonth.format(monthYearFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNextMonth) { Icon(Icons.Filled.ArrowForwardIos, contentDescription = stringResource(R.string.desc_next_month)) }
    }
}

@Composable
fun DayCell(date: LocalDate?, isCurrentMonth: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable(enabled = date != null, onClick = onClick)
            .then(
                if (date != null && isToday) Modifier
                    .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                else if (date != null && isCurrentMonth) Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
                else Modifier
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}