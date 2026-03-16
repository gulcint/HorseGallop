package com.horsegallop.feature.auth.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Auth akışları için smoke testler.
 *
 * Kapsam:
 *  - LoginScreen: checkbox kontrolü → buton enable/disable
 *  - EmailLoginScreen (EmailFormContent): form doldurma → login butonu
 *  - ForgotPasswordScreen: email doldurma → gönder butonu
 *
 * Hilt / Firebase bağımlılığı yoktur — composable içerik fonksiyonları
 * doğrudan sahte UiState ile test edilir.
 */
@RunWith(AndroidJUnit4::class)
class AuthSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ─────────────────────────────────────────────
    // LoginScreen — agreement checkbox + buton gate
    // ─────────────────────────────────────────────

    @Test
    fun loginScreen_agreementUnchecked_emailButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                LoginScreenContent(
                    uiState = LoginUiState(agreementAccepted = false),
                    onGoogleClick = {},
                    onEmailClick = {},
                    onSignupClick = {},
                    onToggleAgreement = {}
                )
            }
        }
        composeRule.onNodeWithTag("email_login_button").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_agreementChecked_emailButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                LoginScreenContent(
                    uiState = LoginUiState(agreementAccepted = true),
                    onGoogleClick = {},
                    onEmailClick = {},
                    onSignupClick = {},
                    onToggleAgreement = {}
                )
            }
        }
        composeRule.onNodeWithTag("email_login_button").assertIsEnabled()
    }

    @Test
    fun loginScreen_checkboxToggle_enablesButtons() {
        var agreement = false
        composeRule.setContent {
            MaterialTheme {
                LoginScreenContent(
                    uiState = LoginUiState(agreementAccepted = agreement),
                    onGoogleClick = {},
                    onEmailClick = {},
                    onSignupClick = {},
                    onToggleAgreement = { agreement = !agreement }
                )
            }
        }
        // Başlangıçta checkbox kapalı — buton disabled
        composeRule.onNodeWithTag("agreement_checkbox").assertIsOff()
        composeRule.onNodeWithTag("email_login_button").assertIsNotEnabled()

        // Checkbox'a tıkla
        composeRule.onNodeWithTag("agreement_checkbox").performClick()

        // Artık buton enabled olmalı
        composeRule.onNodeWithTag("email_login_button").assertIsEnabled()
    }

    @Test
    fun loginScreen_emailButtonClick_triggersCallback() {
        var emailClicked = false
        composeRule.setContent {
            MaterialTheme {
                LoginScreenContent(
                    uiState = LoginUiState(agreementAccepted = true),
                    onGoogleClick = {},
                    onEmailClick = { emailClicked = true },
                    onSignupClick = {},
                    onToggleAgreement = {}
                )
            }
        }
        composeRule.onNodeWithTag("email_login_button").performClick()
        assertTrue("Email butonu tıklaması callback'i tetiklemeli", emailClicked)
    }

    // ─────────────────────────────────────────────
    // EmailLoginScreen — form dolumu + login butonu
    // ─────────────────────────────────────────────

    @Test
    fun emailLogin_emptyForm_loginButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(email = "", password = "", isFormValid = false),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }

    @Test
    fun emailLogin_filledForm_loginButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(
                        email = "test@horsegallop.com",
                        password = "Test1234!",
                        isFormValid = true
                    ),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun emailLogin_emailFieldInput_triggersCallback() {
        var typed = ""
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(),
                    onEmailChange = { typed = it },
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("email_input").performTextInput("test@horsegallop.com")
        assertTrue("Email girişi callback'e iletilmeli", typed.contains("test"))
    }

    @Test
    fun emailLogin_passwordFieldInput_triggersCallback() {
        var typed = ""
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(),
                    onEmailChange = {},
                    onPasswordChange = { typed = it },
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("password_input").performTextInput("Test1234!")
        assertTrue("Şifre girişi callback'e iletilmeli", typed.contains("Test"))
    }

    @Test
    fun emailLogin_loginButtonClick_triggersCallback() {
        var loginClicked = false
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(
                        email = "test@horsegallop.com",
                        password = "Test1234!",
                        isFormValid = true
                    ),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = { loginClicked = true },
                    onSignupClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("login_button").performClick()
        assertTrue("Login butonu tıklaması callback'i tetiklemeli", loginClicked)
    }

    @Test
    fun emailLogin_forgotPasswordLink_triggersCallback() {
        var forgotClicked = false
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = { forgotClicked = true }
                )
            }
        }
        // "Şifremi Unuttum" butonunu bul ve tıkla
        composeRule.onNodeWithTag("forgot_password_link").performClick()
        assertTrue("Şifremi Unuttum tıklaması callback'i tetiklemeli", forgotClicked)
    }

    // ─────────────────────────────────────────────
    // ForgotPasswordScreen içerik alanları
    // (ViewModel'siz content wrapper gerektirir)
    // ─────────────────────────────────────────────

    @Test
    fun forgotPassword_emptyEmail_sendButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordContent(
                    uiState = ForgotPasswordUiState(email = ""),
                    onEmailChange = {},
                    onSendClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("send_reset_button").assertIsNotEnabled()
    }

    @Test
    fun forgotPassword_filledEmail_sendButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordContent(
                    uiState = ForgotPasswordUiState(email = "test@horsegallop.com"),
                    onEmailChange = {},
                    onSendClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("send_reset_button").assertIsEnabled()
    }

    @Test
    fun forgotPassword_sendButtonClick_triggersCallback() {
        var sent = false
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordContent(
                    uiState = ForgotPasswordUiState(email = "test@horsegallop.com"),
                    onEmailChange = {},
                    onSendClick = { sent = true }
                )
            }
        }
        composeRule.onNodeWithTag("send_reset_button").performClick()
        assertTrue("Gönder butonu callback'i tetiklemeli", sent)
    }

    // ─────────────────────────────────────────────
    // EnrollmentScreen — kayıt formu
    // ─────────────────────────────────────────────

    @Test
    fun enrollment_emptyForm_signupButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                EnrollmentFormContent(
                    uiState = EnrollmentUiState(isFormValid = false),
                    onFirstNameChange = {},
                    onLastNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignUpClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("signup_button").assertIsNotEnabled()
    }

    @Test
    fun enrollment_filledForm_signupButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                EnrollmentFormContent(
                    uiState = EnrollmentUiState(
                        firstName = "Elif",
                        lastName = "Yılmaz",
                        email = "elif@horsegallop.com",
                        password = "Test1234!",
                        isFormValid = true
                    ),
                    onFirstNameChange = {},
                    onLastNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignUpClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("signup_button").assertIsEnabled()
    }

    @Test
    fun enrollment_firstNameInput_triggersCallback() {
        var typed = ""
        composeRule.setContent {
            MaterialTheme {
                EnrollmentFormContent(
                    uiState = EnrollmentUiState(),
                    onFirstNameChange = { typed = it },
                    onLastNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignUpClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("enrollment_first_name").performTextInput("Elif")
        assertTrue("Ad girişi callback'e iletilmeli", typed.contains("Elif"))
    }

    @Test
    fun enrollment_signupButtonClick_triggersCallback() {
        var signUpClicked = false
        composeRule.setContent {
            MaterialTheme {
                EnrollmentFormContent(
                    uiState = EnrollmentUiState(
                        firstName = "Elif",
                        lastName = "Yılmaz",
                        email = "elif@horsegallop.com",
                        password = "Test1234!",
                        isFormValid = true
                    ),
                    onFirstNameChange = {},
                    onLastNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignUpClick = { signUpClicked = true }
                )
            }
        }
        composeRule.onNodeWithTag("signup_button").performClick()
        assertTrue("Kayıt ol butonu callback'i tetiklemeli", signUpClicked)
    }

    // ─────────────────────────────────────────────
    // VerificationSentContent — e-posta doğrulama ekranı
    // ─────────────────────────────────────────────

    @Test
    fun verification_resendOnCooldown_resendButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                VerificationSentContent(
                    uiState = EnrollmentUiState(
                        email = "elif@horsegallop.com",
                        verificationSent = true,
                        resendCooldownRemaining = 45
                    ),
                    onResendClick = {},
                    onVerifiedCheck = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithTag("resend_verification_button").assertIsNotEnabled()
    }

    @Test
    fun verification_noCooldown_resendButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                VerificationSentContent(
                    uiState = EnrollmentUiState(
                        email = "elif@horsegallop.com",
                        verificationSent = true,
                        resendCooldownRemaining = 0
                    ),
                    onResendClick = {},
                    onVerifiedCheck = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithTag("resend_verification_button").assertIsEnabled()
    }

    @Test
    fun verification_confirmVerifiedButton_triggersCallback() {
        var verifiedClicked = false
        composeRule.setContent {
            MaterialTheme {
                VerificationSentContent(
                    uiState = EnrollmentUiState(
                        email = "elif@horsegallop.com",
                        verificationSent = true,
                        resendCooldownRemaining = 0
                    ),
                    onResendClick = {},
                    onVerifiedCheck = { verifiedClicked = true },
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithTag("confirm_verified_button").performClick()
        assertTrue("Doğrulandı butonu callback'i tetiklemeli", verifiedClicked)
    }

    @Test
    fun verification_resendButton_triggersCallback() {
        var resendClicked = false
        composeRule.setContent {
            MaterialTheme {
                VerificationSentContent(
                    uiState = EnrollmentUiState(
                        email = "elif@horsegallop.com",
                        verificationSent = true,
                        resendCooldownRemaining = 0
                    ),
                    onResendClick = { resendClicked = true },
                    onVerifiedCheck = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithTag("resend_verification_button").performClick()
        assertTrue("Tekrar gönder butonu callback'i tetiklemeli", resendClicked)
    }

    // ─────────────────────────────────────────────
    // ForgotPassword reset modu — yeni şifre girişi
    // ─────────────────────────────────────────────

    @Test
    fun forgotPasswordReset_emptyNewPassword_changeButtonDisabled() {
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordResetContent(
                    uiState = ForgotPasswordUiState(newPassword = "", isResetMode = true),
                    onNewPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onConfirmClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("change_password_button").assertIsNotEnabled()
    }

    @Test
    fun forgotPasswordReset_filledPassword_changeButtonEnabled() {
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordResetContent(
                    uiState = ForgotPasswordUiState(
                        newPassword = "NewPass123!",
                        confirmPassword = "NewPass123!",
                        isResetMode = true
                    ),
                    onNewPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onConfirmClick = {}
                )
            }
        }
        composeRule.onNodeWithTag("change_password_button").assertIsEnabled()
    }

    @Test
    fun forgotPasswordReset_changePasswordClick_triggersCallback() {
        var confirmClicked = false
        composeRule.setContent {
            MaterialTheme {
                ForgotPasswordResetContent(
                    uiState = ForgotPasswordUiState(
                        newPassword = "NewPass123!",
                        confirmPassword = "NewPass123!",
                        isResetMode = true
                    ),
                    onNewPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onConfirmClick = { confirmClicked = true }
                )
            }
        }
        composeRule.onNodeWithTag("change_password_button").performClick()
        assertTrue("Şifre değiştir butonu callback'i tetiklemeli", confirmClicked)
    }

    // ─────────────────────────────────────────────
    // EmailLoginScreen — tekrar doğrulama e-postası
    // ─────────────────────────────────────────────

    @Test
    fun emailLogin_resendVerificationShown_triggersCallback() {
        var resendClicked = false
        composeRule.setContent {
            MaterialTheme {
                EmailFormContent(
                    uiState = LoginUiState(
                        email = "test@horsegallop.com",
                        password = "Test1234!",
                        isFormValid = true,
                        showResendVerification = true
                    ),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onTogglePasswordVisibility = {},
                    onLoginClick = {},
                    onSignupClick = {},
                    onForgotPasswordClick = {},
                    onResendVerificationClick = { resendClicked = true }
                )
            }
        }
        composeRule.onNodeWithTag("resend_verification_button").assertIsDisplayed()
        composeRule.onNodeWithTag("resend_verification_button").performClick()
        assertTrue("Tekrar doğrulama butonu callback'i tetiklemeli", resendClicked)
    }
}
