package com.example.cw5

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentScope

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cw5.ui.theme.Cw5Theme


sealed class Screen(val route: String) {
    object Notes : Screen("notes")
    object Tasks : Screen("tasks")
    object Calendar : Screen("calendar")
}


class NotesViewModel : ViewModel() {
    val notes = mutableStateListOf<String>()
    fun addNote(note: String) {
        if (note.isNotBlank()) {
            notes.add(note)
        }
    }
}

class TasksViewModel : ViewModel() {
    class Task(val id: Int, val text: String, isCompleted: Boolean = false) {
        var isCompleted by mutableStateOf(isCompleted)
    }

    private var nextId = 0
    val tasks = mutableStateListOf<Task>()

    fun addTask(taskText: String) {
        if (taskText.isNotBlank()) {
            tasks.add(Task(id = nextId++, text = taskText))
        }
    }

    fun toggleTaskCompletion(taskId: Int) {
        val task = tasks.find { it.id == taskId }
        if (task != null) {
            task.isCompleted = !task.isCompleted
        }
    }
}


@Composable
fun AppTopBar(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}


@Composable
fun AppBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Notes Item
        NavigationBarItem(
            icon = { androidx.compose.material3.Icon(Icons.Default.Note, contentDescription = "Notes") },
            label = { Text("Notes") },
            selected = currentRoute == Screen.Notes.route,
            onClick = {
                navController.navigate(Screen.Notes.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Tasks Item
        NavigationBarItem(
            icon = { androidx.compose.material3.Icon(Icons.Default.Task, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = currentRoute == Screen.Tasks.route,
            onClick = {
                navController.navigate(Screen.Tasks.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Calendar Item
        NavigationBarItem(
            icon = { androidx.compose.material3.Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") },
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                navController.navigate(Screen.Calendar.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    var newNoteText by rememberSaveable { mutableStateOf("") }
    val notes = viewModel.notes

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = "Notes")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Add Note TextField
            TextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                label = { Text("Enter new note") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.addNote(newNoteText)
                    newNoteText = "" // Clear input after adding
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Add Note")
            }

            // Notes List
            if (notes.isNotEmpty()) {
                Text("Your Notes:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(notes) { note ->
                        Text(
                            text = note,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes yet. Add your first note!")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier
) {
    var newTaskText by rememberSaveable { mutableStateOf("") }
    val tasks = viewModel.tasks

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = "Tasks")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Add Task TextField
            TextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                label = { Text("Enter new task") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.addTask(newTaskText)
                    newTaskText = "" // Clear input after adding
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Add Task")
            }

            // Tasks List
            if (tasks.isNotEmpty()) {
                Text("Your Tasks:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(tasks) { task ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = task.text,
                                    style = if (task.isCompleted) {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge
                                    }
                                )
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks yet. Add your first task!")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    // Static calendar with current month
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showAnimation = true
    }

    // Get current date info
    val currentMonth = remember { java.util.Calendar.getInstance() }
    val monthName = remember {
        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            .format(currentMonth.time)
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = "Calendar")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showAnimation,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Month header
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Days of week header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Generate calendar grid
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
                    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)

                    // Calculate total cells needed
                    val totalCells = firstDayOfWeek + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        for (week in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (dayOfWeek in 0..6) {
                                    val cellIndex = week * 7 + dayOfWeek
                                    val dayNumber = cellIndex - firstDayOfWeek + 1

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cellIndex >= firstDayOfWeek && dayNumber <= daysInMonth) {
                                            // Valid day
                                            val isToday = dayNumber == today
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isToday) {
                                                    // Highlight today
                                                    androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(40.dp)
                                                    ) {
                                                        drawCircle(
                                                            color = androidx.compose.ui.graphics.Color(0xFF6200EE),
                                                            radius = size.minDimension / 2
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isToday) {
                                                        androidx.compose.ui.graphics.Color.White
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Footer info
                    Text(
                        text = "Today: ${java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200))
        }
    ) { entry ->
        content(entry)
    }
}


@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Animated screens
            animatedComposable(Screen.Notes.route) {
                val viewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                NotesScreen(viewModel = viewModel)
            }

            animatedComposable(Screen.Tasks.route) {
                val viewModel: TasksViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                TasksScreen(viewModel = viewModel)
            }

            animatedComposable(Screen.Calendar.route) {
                CalendarScreen()
            }
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cw5Theme {
                MainApp()
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun NotesScreenPreview() {
    Cw5Theme {
        val viewModel = NotesViewModel().apply {
            addNote("First preview note")
            addNote("Second preview note")
        }
        NotesScreen(viewModel = viewModel)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun TasksScreenPreview() {
    Cw5Theme {
        val viewModel = TasksViewModel().apply {
            addTask("Buy groceries")
            addTask("Finish homework")
        }
        TasksScreen(viewModel = viewModel)
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun CalendarScreenPreview() {
    Cw5Theme {
        CalendarScreen()
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun MainAppPreview() {
    Cw5Theme {
        MainApp()
    }
}