package com.horsegallop.domain.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    private val validator = AuthValidator()

    @Test
    fun `validateEmail accepts standard emails`() {
        assertTrue(validator.validateEmail("rider@example.com"))
        assertTrue(validator.validateEmail("first.last+tag@sub.domain.co"))
    }

    @Test
    fun `validateEmail rejects invalid emails`() {
        assertFalse(validator.validateEmail(""))
        assertFalse(validator.validateEmail("missing-at-symbol"))
        assertFalse(validator.validateEmail("user@"))
        assertFalse(validator.validateEmail("user@invalid"))
    }

    @Test
    fun `isPasswordStrongEnough enforces length and mix`() {
        assertFalse(validator.isPasswordStrongEnough("short1A"))
        assertFalse(validator.isPasswordStrongEnough("alllowercase123"))
        assertFalse(validator.isPasswordStrongEnough("ALLUPPERCASE123"))
        assertFalse(validator.isPasswordStrongEnough("NoDigitsHere"))
        assertTrue(validator.isPasswordStrongEnough("StrongPass1"))
    }
}
