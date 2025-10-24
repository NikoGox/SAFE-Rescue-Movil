# SAFE Rescue

*Gestión eficiente y rápida de incidentes en situaciones de emergencia.*

<p align="center">
  <img src="./README/SRCover.png" alt="Portada SAFE Rescue" width="900px">
</p>

SAFE Rescue es una aplicación diseñada para la gestión eficiente y rápida de incidentes en situaciones de emergencia. Esta herramienta permite a los equipos de respuesta coordinar recursos, monitorear el desarrollo de incidentes y tomar decisiones informadas en tiempo real. Con un enfoque en la seguridad y la comunicación efectiva, SAFE Rescue optimiza la respuesta a emergencias y ayuda a mitigar los riesgos en situaciones críticas.

---

## Últimos cambios

### ❚❙❘ VERSIÓN 1.6

> <br>• Se cambió la funcionalidad de "Recordarme": ahora permite mantener la sesión iniciada al reabrir la app (no solo recordar el usuario).
> <br>• Se ajustó AuthViewModel y NavGraph para que la restauración de sesión y la navegación funcionen sin bucles ni conflictos.
> <br>• Se corrigió un Scaffold que provocaba duplicado del AppTopBar y del BottomNavigationBar en el NavGraph.
> <br>• Se mejoró el estilo de la pantalla de inicio de sesión: colores, espaciado y presentación para un inicio más agradable.
> <br>• Se actualizaron los colores del TopBar y del BottomNavigationBar a los colores de la marca.
> <br>• Se unificó la instancia del AuthViewModel entre NavGraph y la pantalla de login para evitar estados desincronizados.
> <br>• Archivos modificados verificados; prueba: login SIN “Recordarme” (debe volver al login al reiniciar) y CON “Recordarme” (debe mantenerse la sesión).
> <br> > > > Terminar de desarrollar la función de Incidentes.
> <br> > > > Mejorar barra de notificaciones y agregar logica de creacion de notificaciones solo para el administrador.

---

## Características Principales

### Coordinación y Comunicación Centralizada
La comunicación oportuna hace la diferencia. SAFE Rescue proporciona un canal de comunicación unificado que permite a las centrales de alarma y a los equipos en terreno estar perfectamente sincronizados, asegurando que la información crítica llegue a quien la necesita sin demoras.

<p align="center">
  <img src="./README/350_central_alarmas_osorno.jpg" alt="Central de Alarmas Osorno" width="500px">
</p>

### Gestión de Recursos en Terreno
La valentía y la preparación salvan vidas. La aplicación equipa a los bomberos y personal de emergencia con herramientas para visualizar la ubicación de los recursos, actualizar el estado de los incidentes y recibir instrucciones claras, optimizando cada segundo de la operación.

<p align="center">
  <img src="./README/bomberos_en_accion.jpg" alt="Bomberos en acción" width="500px">
</p>

## Tecnologías Utilizadas
* **Kotlin**
* **Jetpack Compose**
* **SQLite/Room**

<br>

<p align="center">
  <img src="./README/SafeRescueLogo.png" alt="Safe Rescue Logo" width="400px">
</p>
