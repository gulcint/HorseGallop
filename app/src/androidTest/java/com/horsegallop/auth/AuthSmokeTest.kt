package com.horsegallop.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.horsegallop.MainActivity
import org.junit.Rule
import org.junit.Test

class AuthSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationToEnrollment() {
        // Wait for splash to finish and onboarding/login to show
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("signup_text_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Click signup
        composeTestRule.onNodeWithTag("signup_text_button").performClick()

        // Verify Enrollment screen
        composeTestRule.onNodeWithTag("first_name_input").assertExists()
        composeTestRule.onNodeWithTag("last_name_input").assertExists()
        composeTestRule.onNodeWithTag("email_input").assertExists()
        composeTestRule.onNodeWithTag("enroll_button").assertExists()
    }

    @Test
    fun testLoginButtonDisabledInitially() {
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("login_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Button should be disabled when empty
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }
}
