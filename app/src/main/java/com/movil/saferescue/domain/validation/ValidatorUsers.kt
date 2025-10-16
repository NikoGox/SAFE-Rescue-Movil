package com.movil.saferescue.domain.validation

import android.util.Patterns // Usamos el patrón estándar de Android para emails

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {                            // Retorna String? (mensaje) o null si está OK
    if (email.isBlank()) return "El email es obligatorio"              // Regla 1: no vacío
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()           // Regla 2: coincide con patrón de email
    return if (!ok) "Formato de email inválido" else null              // Si no cumple, devolvemos mensaje
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {                   // Valida nombre
    if (name.isBlank()) return "El nombre es obligatorio"              // Regla 1: no vacío
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 2: solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null// Mensaje si falla
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {                  // Valida teléfono
    if (phone.isBlank()) return "El teléfono es obligatorio"           // Regla 1: no vacío
    if (!phone.all { it.isDigit() }) return "Solo números"             // Regla 2: todos dígitos
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" // Regla 3: tamaño razonable
    return null                                                        // OK
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {                    // Requisitos mínimos de seguridad
    if (pass.isBlank()) return "La contraseña es obligatoria"          // No vacío
    if (pass.length < 8) return "Mínimo 8 caracteres"                  // Largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // Al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // Al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"         // Al menos 1 número
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // Al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios"          // Sin espacios
    return null                                                         // OK
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {          // Confirmación de contraseña
    if (confirm.isBlank()) return "Confirma tu contraseña"             // No vacío
    return if (pass != confirm) "Las contraseñas no coinciden" else null // Deben ser iguales
}

// Valida el nombre de usuario (mínimo 3 caracteres, sin espacios)
fun validateUsername(username: String): String? {
    if (username.isBlank()) return "El nombre de usuario es obligatorio"
    if (username.length < 3) return "Mínimo 3 caracteres"
    if (username.contains(' ')) return "No debe contener espacios"
    return null
}

// Valida que el RUN no esté vacío (solo para feedback en tiempo real)
fun validateRun(run: String): String? {
    if (run.isBlank()) return "El RUN es obligatorio"
    if (!run.all { it.isDigit() }) return "El RUN debe contener solo números"
    if (run.length !in 7..8) return "El RUN debe tener 7 u 8 dígitos"
    return null
}

// Valida que el DV no esté vacío (solo para feedback en tiempo real)
fun validateDv(dv: String): String? {
    if (dv.isBlank()) return "El DV es obligatorio"
    if (dv.length != 1) return "El DV debe tener 1 caracter"
    if (!dv.first().isDigit() && dv.first().uppercaseChar() != 'K') return "Dígito verificador inválido"
    return null
}


// Valida que el RUN sea un número válido y que el DV corresponda
fun validateChileanRUN(run: String, dv: String): String? {
    if (run.isBlank()) return "El RUN es obligatorio"
    if (dv.isBlank()) return "El dígito verificador es obligatorio"
    if (!run.all { it.isDigit() }) return "El RUN debe contener solo números"
    if (run.length !in 7..8) return "El RUN debe tener 7 u 8 dígitos"
    if (dv.length != 1) return "El dígito verificador debe tener 1 caracter"
    if (!dv.first().isDigit() && dv.first().uppercaseChar() != 'K') return "Dígito verificador inválido"

    // Algoritmo de validación de RUN chileno
    var runValue = run.toIntOrNull() ?: return "RUN inválido"
    var m = 0
    var s = 1
    while (runValue != 0) {
        s = (s + runValue % 10 * (9 - m++ % 6)) % 11
        runValue /= 10
    }
    val calculatedDv = if (s != 0) (s - 1).toString() else "K"

    return if (calculatedDv.equals(dv, ignoreCase = true)) null else "El RUN o dígito verificador es incorrecto"
}

// Valida que la URL no esté vacía y tenga un formato válido
fun validateUrl(url: String): String? {
    if (url.isBlank()) return "La URL de la foto es obligatoria"
    if (!Patterns.WEB_URL.matcher(url).matches()) return "Formato de URL inválido"
    return null
}

// Valida que la fecha tenga el formato YYYY-MM-DD
fun validateDate(date: String): String? {
    if (date.isBlank()) return "La fecha de subida es obligatoria"
    // Regex para validar formato YYYY-MM-DD, se puede ajustar si es necesario
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    if (!regex.matches(date)) return "Formato de fecha inválido (debe ser YYYY-MM-DD)"
    return null
}

// Valida que el nombre del rol no esté vacío y contenga solo letras
fun validateRolName(rol: String): String? {
    if (rol.isBlank()) return "El nombre del rol es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(rol)) "El rol solo debe contener letras y espacios" else null
}

// Valida que el detalle del rol no esté vacío
fun validateRolDetalle(detalle: String): String? {
    if (detalle.isBlank()) return "El detalle del rol es obligatorio"
    return null
}
