package com.movil.saferescue.navigation

sealed class Route( //Clase sellada
    val path: String
){
    data object Home:Route("home")
    data object Register:Route("register")
    data object Login:Route("login")
    object Notification:Route("notification")
    object Profile:Route("profile")

    object Incidente:Route("incidente")

    object Chat:Route("chat")

}