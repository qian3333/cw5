***AI Usage Documentation***


**Where AI helped:**
- AI was really useful for setting up the basic navigation structure with NavHost and sealed classes
- It helped me quickly generate the sample data for all 12 Boston locations
- The Material 3 styling and card layouts mostly use ai suggestions

**Where AI got confused:**
- Initially, AI suggested using BottomNavigation instead of TopAppBar, which didn't fit the requirements for a hierarchical tour app
- The biggest issue was with navigation arguments - AI first tried to pass objects directly instead of using String and Int types properly through the route
- AI also struggled with the back stack management. It kept suggesting `popUpTo(0)` instead of the correct `popUpTo(Screen.Home.route) { inclusive = true }` and users couldn't properly return home
- The lambda syntax for navigation callbacks confused the AI sometimes
