package com.firexrwtinc.mytime

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

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

                // Форматтеры для заголовков
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
fun DayScreen(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit, updateTitle: (LocalDate) -> Unit) {
    LaunchedEffect(currentDate) {
        updateTitle(currentDate)
    }
    // TODO: UI для отображения дня, задач и кнопок < > для смены дня (вызовут onDateChange)
    Text("Экран Дня: ${currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
}

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