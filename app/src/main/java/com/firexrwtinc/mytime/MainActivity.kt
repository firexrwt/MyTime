package com.firexrwtinc.mytime

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

/* TODO: Надо сделать везде красивые анимации */

sealed class Screen(val route: String, val resourceId: Int, val icon: ImageVector) {
    object DayView : Screen("day", R.string.screen_day, Icons.Filled.CalendarViewDay)
    object WeekView : Screen("week", R.string.screen_week, Icons.Filled.CalendarViewWeek)
    object MonthView : Screen("month", R.string.screen_month, Icons.Filled.CalendarMonth)
    object YearView : Screen("year", R.string.screen_year, Icons.Filled.CalendarToday)
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

                var displayedDate by remember { mutableStateOf(LocalDate.now()) }

                var currentTopBarTitle by remember { mutableStateOf("") }
                val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
                val weekFormatterPattern = remember { "w" }
                val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
                val yearFormatter = remember { DateTimeFormatter.ofPattern("yyyy", Locale.getDefault()) }

                val updateTitle: (Screen, LocalDate) -> Unit = { screen, date ->
                    currentTopBarTitle = when (screen) {
                        is Screen.DayView -> date.format(dayFormatter)
                        is Screen.WeekView -> {
                            val calendar = Calendar.getInstance()
                            calendar.set(date.year, date.monthValue -1, date.dayOfMonth)
                            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
                            val monthName = date.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
                            "Неделя $weekOfMonth, $monthName ${date.year}"
                        }
                        is Screen.MonthView -> YearMonth.from(date).format(monthFormatter)
                        is Screen.YearView -> date.format(yearFormatter)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("Элемент меню 1", modifier = Modifier.padding(16.dp))
                            Text("Элемент меню 2", modifier = Modifier.padding(16.dp))
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(currentTopBarTitle) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.apply { if (isClosed) open() else close() } }
                                    }) {
                                        Icon(Icons.Filled.Menu, stringResource(R.string.desc_open_drawer))
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                navItems.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, null) },
                                        label = { Text(stringResource(screen.resourceId)) },
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
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.MonthView.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.DayView.route) {
                                DayScreen(
                                    currentDate = displayedDate,
                                    onDateChange = { newDate -> displayedDate = newDate },
                                    updateTitle = { date -> updateTitle(Screen.DayView, date) }
                                )
                            }
                            composable(Screen.WeekView.route) {
                                WeekScreen(
                                    currentDate = displayedDate,
                                    onDateChange = { newDate -> displayedDate = newDate },
                                    updateTitle = { date -> updateTitle(Screen.WeekView, date) }
                                )
                            }
                            composable(Screen.MonthView.route) {
                                MonthScreen(
                                    currentDate = displayedDate,
                                    onDateChange = { newDate -> displayedDate = newDate },
                                    updateTitle = { date -> updateTitle(Screen.MonthView, date) }
                                )
                            }
                            composable(Screen.YearView.route) {
                                YearScreen(
                                    currentDate = displayedDate,
                                    onDateChange = { newDate -> displayedDate = newDate },
                                    updateTitle = { date -> updateTitle(Screen.YearView, date) },
                                    onMonthSelected = { yearMonth ->
                                        displayedDate = yearMonth.atDay(1)
                                        navController.navigate(Screen.MonthView.route) {
                                            popUpTo(Screen.MonthView.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayScreen(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    updateTitle: (LocalDate) -> Unit,
    tasks: List<Task> = emptyList()
) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
    }

    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("E", Locale.getDefault()) }
    val dayOfMonthFormatter = remember { DateTimeFormatter.ofPattern("dd", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayScreenHeader(
                dayOfWeek = currentDate.format(dayOfWeekFormatter).uppercase(Locale.getDefault()),
                dayOfMonth = currentDate.format(dayOfMonthFormatter),
                pendingTasksCount = tasks.count { it.date == currentDate && it.endTime.isAfter(LocalTime.now()) }
            )

            HourTimeline(
                currentDate = currentDate,
                tasks = tasks,
                modifier = Modifier.weight(1f)
            )
        }

        FloatingActionButton(
            onClick = { /* TODO: Навигация на экран добавления задачи */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить задачу")
        }
    }
}

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
    // scaledHourHeight будет вычисляться на основе scaleFactor
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

    val displayTasks = if (tasks.filter { it.date == currentDate }.isEmpty()) {
        // Примеры задач, если список пуст
        listOf(
            Task("placeholder-1", "Пример: Занятия немецким", LocalTime.of(9,0), LocalTime.of(10,0), currentDate, Color(0xFF4A90E2)),
            Task("placeholder-2", "Пример: Обед", LocalTime.of(13,0), LocalTime.of(13,45), currentDate, Color(0xFF2E8B57)),
            Task("placeholder-3", "Пример: Встреча (перекрытие)", LocalTime.of(9,30), LocalTime.of(11,0), currentDate, Color(0xFFFFA500))
        )
    } else {
        tasks.filter { it.date == currentDate }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, _, gestureZoom, _ ->
                    val oldScaleFactor = scaleFactor
                    // Обновляем scaleFactor, применяя изменение от жеста и ограничивая его
                    scaleFactor = (oldScaleFactor * gestureZoom).coerceIn(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR)

                    // Если масштаб действительно изменился, корректируем прокрутку
                    if (oldScaleFactor != scaleFactor) {
                        val effectiveZoomDelta = scaleFactor / oldScaleFactor
                        val currentScrollPx = scrollState.value.toFloat()
                        // Y-координата контента, которая была под центром жеста
                        val contentYAtCentroidPx = currentScrollPx + centroid.y
                        // Новая позиция прокрутки, чтобы contentYAtCentroidPx остался под centroid.y
                        val newScrollPx = (contentYAtCentroidPx * effectiveZoomDelta) - centroid.y

                        coroutineScope.launch {
                            // Прокручиваем к новой позиции, убедившись, что она не выходит за пределы
                            // Максимальное значение прокрутки: totalContentHeightPx - viewportHeightPx
                            // viewportHeightPx здесь неизвестен точно, поэтому scrollTo может обрезать значение.
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
                        // Определяем, насколько заметной должна быть линия часа
                        val lineAlpha = when {
                            scaledHourHeight < 30.dp && hour % 4 != 0 -> 0.05f // Очень тусклая
                            scaledHourHeight < 30.dp && hour % 4 == 0 -> 0.2f  // Основная для этого зума
                            scaledHourHeight < 45.dp && hour % 2 != 0 -> 0.08f // Тусклая
                            scaledHourHeight < 45.dp && hour % 2 == 0 -> 0.2f  // Основная
                            else -> 0.15f // Стандартная
                        }
                        val strokeWidth = when {
                            scaledHourHeight < 30.dp && hour % 4 == 0 -> 1.5f
                            scaledHourHeight < 45.dp && hour % 2 == 0 -> 1.5f
                            else -> 1f
                        }

                        drawLine(
                            color = lineColor.copy(alpha = lineAlpha),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )

                        // Рисуем 30-минутные отметки, если достаточно увеличено
                        if (scaledHourHeight >= 75.dp) {
                            val y30 = y + hourHeightPx / 2
                            drawLine(
                                color = lineColor.copy(alpha = 0.07f), // Очень тусклые
                                start = Offset(0f, y30),
                                end = Offset(size.width, y30),
                                strokeWidth = 0.8f
                            )
                        }
                    }

                    if (currentDate == LocalDate.now()) {
                        val now = LocalTime.now()
                        val minutesFromMidnight = now.hour * 60 + now.minute
                        val totalDayMinutes = 24f * 60f
                        // Позиция Y относительно общей высоты контента
                        val currentY = (minutesFromMidnight / totalDayMinutes) * (hourHeightPx * 24)

                        drawLine(
                            color = currentTimeLineColor,
                            start = Offset(with(density) { (-8).dp.toPx() }, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 2f
                        )
                        drawCircle(
                            color = currentTimeLineColor,
                            radius = with(density) { 4.dp.toPx() },
                            center = Offset(with(density) { (-8).dp.toPx() }, currentY)
                        )
                    }
                }

                // Размещение задач
                // TODO: Реализовать алгоритм корректного размещения перекрывающихся задач
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
                            color = task.color,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaledHourHeight), // Высота каждой ячейки часа соответствует масштабу
                contentAlignment = Alignment.TopEnd
            ) {
                // Определяем, нужно ли отображать метку для этого часа
                val showThisHourLabel = when {
                    scaledHourHeight < 30.dp -> hour % 4 == 0 // Каждые 4 часа
                    scaledHourHeight < 45.dp -> hour % 2 == 0 // Каждые 2 часа
                    else -> true // Каждый час
                }

                if (showThisHourLabel) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:00", hour),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 8.dp, top = if (scaledHourHeight < 25.dp) 0.dp else 2.dp) // Уменьшаем отступ при сильном сжатии
                    )
                }

                // Если очень сильно увеличено, можно добавить метки для 30 минут
                if (scaledHourHeight >= 150.dp) { // Порог для отображения метки ":30"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(scaledHourHeight / 2)
                            .align(Alignment.BottomEnd) // Позиционируем относительно середины часа
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.TopEnd // Текст ":30" будет вверху этой половины
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:30", hour),
                            style = MaterialTheme.typography.labelSmall.copy( // Меньший шрифт для :30
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(top = 0.dp) // Ближе к линии середины часа
                        )
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DayScreenPreview() {
    MyTimeTheme {
        val sampleTasks = listOf(
            Task("1", "Утренняя встреча", LocalTime.of(9,0), LocalTime.of(10,30), LocalDate.now(), Color(0xFF5EABF2)),
            Task("2", "Обед", LocalTime.of(13,0), LocalTime.of(14,0), LocalDate.now(), Color(0xFF6BCB77)),
            Task("3", "Работа над проектом Alpha", LocalTime.of(10,45), LocalTime.of(12,30), LocalDate.now(), Color(0xFFFFD966)),
            Task("4", "Звонок клиенту", LocalTime.of(15,0), LocalTime.of(15,30), LocalDate.now(), Color(0xFFF28B82))
        )
        DayScreen(currentDate = LocalDate.now(), onDateChange = {}, updateTitle = {}, tasks = sampleTasks)
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
    val months = remember(currentYear) { (1..12).map { YearMonth.of(currentYear, it) } } // Обновляем месяцы при смене года
    val monthNameFormatter = remember { DateTimeFormatter.ofPattern("LLLL", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { onDateChange(LocalDate.of(currentYear - 1, currentDate.monthValue, 1).withDayOfMonth(1)) }) { Icon(Icons.Filled.ArrowBackIosNew, "Предыдущий год") }
            Text(text = currentYear.toString(), style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { onDateChange(LocalDate.of(currentYear + 1, currentDate.monthValue, 1).withDayOfMonth(1)) }) { Icon(Icons.Filled.ArrowForwardIos, "Следующий год") }
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
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPreviousMonth) { Icon(Icons.Filled.ArrowBackIosNew, "Предыдущий месяц") }
        Text(text = currentYearMonth.format(monthYearFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNextMonth) { Icon(Icons.Filled.ArrowForwardIos, "Следующий месяц") }
    }
}

@Composable
fun DayCell(date: LocalDate?, isCurrentMonth: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp) // Уменьшил padding для более плотной сетки
            // TODO: Реализовать .clickable(enabled = date != null, onClick = onClick)
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

data class Task(val id: String, val title: String, val startTime: LocalTime, val endTime: LocalTime, val date: LocalDate, val color: Color)
