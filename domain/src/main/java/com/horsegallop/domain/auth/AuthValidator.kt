package com.horsegallop.domain.auth

import javax.inject.Inject

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errorIds: List<Int>) : ValidationResult()
}

class AuthValidator @Inject constructor() {

    fun validateName(firstName: String, lastName: String): Boolean {
        return firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email.trim())
    }

    fun calculatePasswordStrength(password: String): Int {
        val hasLen = password.length >= 10
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        return listOf(hasLen, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    }

    fun isPasswordStrongEnough(password: String): Boolean {
        val hasLen = password.length >= 10
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasLen && hasUpper && hasLower && hasDigit
    }
}
