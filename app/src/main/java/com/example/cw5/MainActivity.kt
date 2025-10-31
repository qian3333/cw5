package com.example.cw5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cw5.ui.theme.Cw5Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cw5Theme {
                CityTourApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityTourApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(getScreenTitle(currentRoute)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    // Show back button only if not on home screen
                    if (currentRoute != Screen.Home.route) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // Show home button on all screens except home
                    if (currentRoute != Screen.Home.route) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        CityTourNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// Navigation Graph
@Composable
fun CityTourNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                }
            )
        }

        // Categories Screen
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onCategoryClick = { category ->
                    navController.navigate(Screen.List.createRoute(category))
                }
            )
        }

        // List Screen (with String argument)
        composable(
            route = Screen.List.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ListScreen(
                category = category,
                onLocationClick = { locationId ->
                    navController.navigate(Screen.Detail.createRoute(category, locationId))
                }
            )
        }

        // Detail Screen (with String and Int arguments)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("locationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val locationId = backStackEntry.arguments?.getInt("locationId") ?: 0
            DetailScreen(
                category = category,
                locationId = locationId
            )
        }
    }
}

// Sealed class for Screen routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object List : Screen("list/{category}") {
        fun createRoute(category: String) = "list/$category"
    }
    object Detail : Screen("detail/{category}/{locationId}") {
        fun createRoute(category: String, locationId: Int) = "detail/$category/$locationId"
    }
}

// Helper function to get screen title
fun getScreenTitle(route: String?): String {
    return when {
        route == null -> "City Tour"
        route == Screen.Home.route -> "Welcome to Boston"
        route == Screen.Categories.route -> "Explore Categories"
        route.startsWith("list/") -> {
            val category = route.substringAfter("list/")
            category.replaceFirstChar { it.uppercase() }
        }
        route.startsWith("detail/") -> "Location Details"
        else -> "City Tour"
    }
}

// Data Models
data class Category(
    val name: String,
    val description: String,
    val icon: String
)

data class Location(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val address: String,
    val highlights: List<String>
)

// Sample Data
object TourData {
    val categories = listOf(
        Category("Museums", "Explore world-class museums and exhibitions", "🏛️"),
        Category("Parks", "Discover beautiful parks and green spaces", "🌳"),
        Category("Restaurants", "Taste the finest cuisine in Boston", "🍽️"),
        Category("Landmarks", "Visit iconic landmarks and historical sites", "🗽")
    )

    val locations = listOf(
        // Museums
        Location(1, "MIT Museum", "Museums", "Explore science and technology exhibits", "265 Massachusetts Ave", listOf("Robotics", "Holography", "Innovation")),
        Location(2, "Museum of Fine Arts", "Museums", "One of the most comprehensive art museums", "465 Huntington Ave", listOf("Ancient Art", "Contemporary", "Impressionism")),
        Location(3, "Boston Children's Museum", "Museums", "Interactive exhibits for kids", "308 Congress St", listOf("Science", "Culture", "Art Activities")),
        
        // Parks
        Location(4, "Boston Common", "Parks", "America's oldest public park", "139 Tremont St", listOf("Walking Trails", "Frog Pond", "Public Garden")),
        Location(5, "Charles River Esplanade", "Parks", "Scenic riverfront park", "Along Charles River", listOf("Biking", "Jogging", "River Views")),
        Location(6, "Arnold Arboretum", "Parks", "Historic botanical garden", "125 Arborway", listOf("Plant Collections", "Hiking", "Photography")),
        
        // Restaurants
        Location(7, "Union Oyster House", "Restaurants", "Historic seafood restaurant since 1826", "41 Union St", listOf("Oysters", "Clam Chowder", "Historic Setting")),
        Location(8, "Neptune Oyster", "Restaurants", "Award-winning seafood spot", "63 Salem St", listOf("Raw Bar", "Lobster Roll", "Craft Cocktails")),
        Location(9, "Oleana", "Restaurants", "Mediterranean cuisine with local ingredients", "134 Hampshire St", listOf("Farm-to-Table", "Mezze", "Garden Patio")),
        
        // Landmarks
        Location(10, "Freedom Trail", "Landmarks", "2.5-mile path through historic sites", "Boston Common", listOf("16 Historical Sites", "Guided Tours", "Revolutionary History")),
        Location(11, "Faneuil Hall", "Landmarks", "Historic marketplace and meeting hall", "1 Faneuil Hall Sq", listOf("Shopping", "Street Performers", "Food Court")),
        Location(12, "Boston Harbor", "Landmarks", "Historic waterfront and harbor", "Atlantic Ave", listOf("Harbor Cruises", "Islands", "Seafood"))
    )

    fun getLocationsByCategory(category: String): List<Location> {
        return locations.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getLocationById(id: Int): Location? {
        return locations.find { it.id == id }
    }
}

// Screen Composables

@Composable
fun HomeScreen(
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏙️",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Welcome to Boston City Tour",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Discover the best attractions, dining, and experiences in historic Boston",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onNavigateToCategories,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Start Exploring", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "What would you like to explore?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(TourData.categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category.name) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${category.icon} ${category.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ListScreen(
    category: String,
    onLocationClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val locations = TourData.getLocationsByCategory(category)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All $category",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "${locations.size} locations found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (locations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No locations found in this category",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(locations) { location ->
                    LocationCard(
                        location = location,
                        onClick = { onLocationClick(location.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = location.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "📍 ${location.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DetailScreen(
    category: String,
    locationId: Int,
    modifier: Modifier = Modifier
) {
    val location = TourData.getLocationById(locationId)

    if (location == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Location not found",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = location.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Category: $category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Location ID: $locationId",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = location.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item {
            Column {
                Text(
                    text = "📍 Address",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Column {
                Text(
                    text = "✨ Highlights",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                location.highlights.forEach { highlight ->
                    Text(
                        text = "• $highlight",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}