package com.movil.saferescue.navigation

/**
 * Clase sellada (sealed class) para eventos que el ViewModel puede emitir
 * para que la Activity (o NavHost) realice una acción de navegación.
 */
sealed class NavigationEvent {
    // Comando para navegar a una ruta específica. Permite limpiar la pila (popUpTo).
    data class NavigateTo(
        val route: Route,
        val popUpToRoute: Route? = null,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    // Comando para volver atrás (ej: de un detalle a la lista).
    data object PopBackStack : NavigationEvent()
}