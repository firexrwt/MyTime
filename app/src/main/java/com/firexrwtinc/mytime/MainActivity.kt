package com.firexrwtinc.mytime

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
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
    updateTitle: (LocalDate) -> Unit
) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
    }

    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("E", Locale.getDefault()) }
    val dayOfMonthFormatter = remember { DateTimeFormatter.ofPattern("dd", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayScreenHeader(
                dayOfWeek = currentDate.format(dayOfWeekFormatter).uppercase(),
                dayOfMonth = currentDate.format(dayOfMonthFormatter),
                pendingTasksCount = 2
            )

            HourTimeline(
                currentDate = currentDate,
                tasks = emptyList(), /* TODO: Передать реальный список задач */
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
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$pendingTasksCount ${getPendingTasksString(pendingTasksCount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    val hours = (0..23).toList()
    val timelineStartPadding = 56.dp

    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val currentTimeLineColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize()) {
        val calculatedHourHeight = 56.dp
        val calculatedHourHeightPx = with(LocalDensity.current) { calculatedHourHeight.toPx() }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                for (hour in hours) {
                    val y = hour * calculatedHourHeightPx
                    drawLine(
                        color = lineColor,
                        start = Offset(timelineStartPadding.toPx(), y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                if (currentDate == LocalDate.now()) {
                    val now = LocalTime.now()
                    val minutesFromMidnight = now.hour * 60 + now.minute
                    val currentY = (minutesFromMidnight / (24f * 60f)) * size.height

                    drawLine(
                        color = currentTimeLineColor,
                        start = Offset(timelineStartPadding.toPx() - 8.dp.toPx(), currentY),
                        end = Offset(size.width, currentY),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = currentTimeLineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(timelineStartPadding.toPx() - 8.dp.toPx(), currentY)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                HourLabelsColumn(
                    hours = hours,
                    hourHeight = calculatedHourHeight,
                    modifier = Modifier.width(timelineStartPadding)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(hours) { _, hour ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(calculatedHourHeight)
                        ) {
                            if (hour == 9 && tasks.isEmpty()) {
                                TaskEntry(
                                    title = "Занятия немецким",
                                    timeRange = "09:00 - 10:00",
                                    color = Color(0xFF4A90E2),
                                    modifier = Modifier.padding(start = 4.dp, top = (calculatedHourHeight / 4))
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
fun HourLabelsColumn(
    hours: List<Int>,
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        hours.forEach { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeight),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d:00", hour),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TaskEntry(title: String, timeRange: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.8f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = timeRange, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayScreenPreview() {
    MyTimeTheme {
        DayScreen(LocalDate.now(), {}, {})
    }
}


const val HOUR_HEIGHT_DP = 60 // Высота одного часа в dp
val totalDayHeightDp = HOUR_HEIGHT_DP * 24



@Composable
fun WeekScreen(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit, updateTitle: (LocalDate) -> Unit) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
    }
    // TODO: UI для отображения недели и кнопок < > для смены недели
    Text("Экран Недели (начало с): ${currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
}


@Composable
fun MonthScreen(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit, // Вызывается при смене месяца
    updateTitle: (LocalDate) -> Unit
) {
    val currentYearMonth = remember(currentDate) { YearMonth.from(currentDate) }

    LaunchedEffect(currentYearMonth) {
        updateTitle(currentYearMonth.atDay(1))
    }

    val daysOfWeek = remember { getDaysOfWeek() }
    val calendarDays = remember(currentYearMonth) { getDaysForMonthCalendar(currentYearMonth) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Панель навигации по месяцам
        MonthNavigationHeader(
            currentYearMonth = currentYearMonth,
            onPreviousMonth = {
                val previousMonth = currentYearMonth.minusMonths(1)
                onDateChange(previousMonth.atDay(1)) // Обновляем displayedDate в MainActivity
            },
            onNextMonth = {
                val nextMonth = currentYearMonth.plusMonths(1)
                onDateChange(nextMonth.atDay(1)) // Обновляем displayedDate в MainActivity
            }
        )

        // Заголовки дней недели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Сетка дней
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(calendarDays.size) { index ->
                val date = calendarDays[index]
                DayCell(
                    date = date,
                    isCurrentMonth = date?.month == currentYearMonth.month,
                    isToday = date == LocalDate.now()
                    // onClick = { if (date != null) { /* TODO: обработка нажатия на день */ } }
                )
            }
        }
        // TODO: Здесь можно будет добавить список задач для выбранного дня или другие элементы
    }
}

@Composable
fun YearScreen(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    updateTitle: (LocalDate) -> Unit,
    onMonthSelected: (YearMonth) -> Unit
) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
    }
    // TODO: UI для отображения года с месяцами (кликабельными - вызовут onMonthSelected)
    // TODO: Кнопки < > для смены года
    Text("Экран Года: ${currentDate.year}")
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
    val dayOfWeekOfFirstDay = firstDayOfMonth.dayOfWeek.value // 1 (Пн) - 7 (Вс)

    val calendarDays = mutableListOf<LocalDate?>()
    val leadingEmptyCells = (dayOfWeekOfFirstDay - DayOfWeek.MONDAY.value + 7) % 7
    for (i in 0 until leadingEmptyCells) {
        calendarDays.add(null)
    }
    for (day in 1..daysInMonth) {
        calendarDays.add(yearMonth.atDay(day))
    }
    while (calendarDays.size % 7 != 0) {
        calendarDays.add(null)
    }

    return calendarDays
}
@Composable
fun MonthNavigationHeader(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Предыдущий месяц")
        }
        Text(
            text = currentYearMonth.format(monthYearFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Следующий месяц")
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    // onClick: () -> Unit, // Пока не используем
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Делает ячейку квадратной
            .padding(2.dp)
            // .clickable(enabled = date != null, onClick = onClick) // Можно будет добавить обработчик
            .then(
                if (isToday) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) else Modifier
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp)
            )
            // TODO: Здесь будут маркеры задач под числом
            // Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)) {
            //     // TaskMarker(...)
            // }
        }
    }
}

data class Task(
    val id: String,
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate, // Дата, к которой относится задача
    val color: Color // Тип Color из androidx.compose.ui.graphics.Color
)