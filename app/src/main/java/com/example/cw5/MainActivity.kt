package com.example.cw5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cw5.ui.theme.Cw5Theme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch


// Sealed class for navigation routes
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object AddRecipe : Routes("add_recipe")
    object Settings : Routes("settings")
    object RecipeDetail : Routes("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Int) = "recipe_detail/$recipeId"
    }
}

// Data class for Recipe
data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>
)

// ViewModel for managing recipes (in-memory persistence)
class RecipeViewModel : ViewModel() {
    private val _recipes = mutableStateListOf<Recipe>()
    val recipes: List<Recipe> = _recipes

    private var nextId = 1

    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _recipes.add(recipe.copy(id = nextId++))
        }
    }

    fun getRecipeById(id: Int): Recipe? {
        return _recipes.find { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cw5Theme {
                val navController = rememberNavController()
                val recipeViewModel: RecipeViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(topBarTitleForRoute(currentRoute))
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.Home.route) {
                            HomeScreen(
                                recipes = recipeViewModel.recipes,
                                onRecipeClick = { recipeId ->
                                    navController.navigate(Routes.RecipeDetail.createRoute(recipeId)) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(Routes.AddRecipe.route) {
                            AddRecipeScreen(
                                onAddRecipe = { recipe ->
                                    recipeViewModel.addRecipe(recipe)
                                    navController.navigate(Routes.Home.route) {
                                        popUpTo(Routes.Home.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onCancel = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Routes.Settings.route) {
                            SettingsScreen()
                        }
                        composable(Routes.RecipeDetail.route) { backStackEntry ->
                            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                            val recipe = recipeId?.let { recipeViewModel.getRecipeById(it) }

                            if (recipe != null) {
                                RecipeDetailScreen(recipe = recipe)
                            } else {
                                ErrorScreen(onBackClick = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun topBarTitleForRoute(route: String?): String {
    return when {
        route == Routes.Home.route -> "Recipe Book"
        route == Routes.AddRecipe.route -> "Add New Recipe"
        route == Routes.Settings.route -> "Settings"
        route?.startsWith("recipe_detail") == true -> "Recipe Details"
        else -> "Recipe App"
    }
}

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

// Bottom Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Routes.Home.route, "Home", Icons.Default.Home),
        BottomNavItem(Routes.AddRecipe.route, "Add", Icons.Default.Add),
        BottomNavItem(Routes.Settings.route, "Settings", Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Home Screen - List of Recipes
@Composable
fun HomeScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recipes yet! Add your first recipe.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recipes) { recipe ->
                    RecipeListItem(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }
            }
        }
    }
}

// Recipe List Item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListItem(recipe: Recipe, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Add Recipe Screen - Form with state management
@Composable
fun AddRecipeScreen(
    onAddRecipe: (Recipe) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") } // Comma-separated
    var stepsText by remember { mutableStateOf("") } // Line-separated
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = ingredientsText,
            onValueChange = { ingredientsText = it },
            label = { Text("Ingredients (comma-separated)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = stepsText,
            onValueChange = { stepsText = it },
            label = { Text("Steps (line-separated)") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            maxLines = Int.MAX_VALUE
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val ingredients = ingredientsText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        val steps = stepsText.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        if (title.isNotBlank() && ingredients.isNotEmpty() && steps.isNotEmpty()) {
                            onAddRecipe(Recipe(id = 0, title = title, ingredients = ingredients, steps = steps))
                        }
                    }
                },
                enabled = title.isNotBlank() && ingredientsText.isNotBlank() && stepsText.isNotBlank()
            ) {
                Text("Save Recipe")
            }
        }
    }
}

// Recipe Detail Screen
@Composable
fun RecipeDetailScreen(recipe: Recipe, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Ingredients", style = MaterialTheme.typography.headlineSmall)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(recipe.ingredients) { ingredient ->
                    Text(text = "• $ingredient", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Steps", style = MaterialTheme.typography.headlineSmall)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(recipe.steps) { index, step ->
                    Text(text = "${index + 1}. $step", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// Settings Screen (placeholder)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Settings Screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// Error Screen (for invalid recipe ID)
@Composable
fun ErrorScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Recipe Not Found",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}