package com.horsegallop.data.uitext

import com.horsegallop.domain.uitext.LocaleCode
import com.horsegallop.domain.uitext.ScreenId
import com.horsegallop.domain.uitext.TextType
import com.horsegallop.domain.uitext.UiText
import com.horsegallop.domain.uitext.UiTextKey

object FakeUiTextData {
  val all: List<UiText> = buildList {
    // Splash
    add(UiText(UiTextKey(ScreenId.SPLASH, TextType.TITLE, "hero_title"), LocaleCode.EN_US, "Track Every Gallop"))
    add(UiText(UiTextKey(ScreenId.SPLASH, TextType.SUBTITLE, "hero_subtitle"), LocaleCode.EN_US, "Your journey starts here"))
    add(UiText(UiTextKey(ScreenId.SPLASH, TextType.TITLE, "hero_title"), LocaleCode.TR_TR, "Her Dörtnalı Takip Et"))
    add(UiText(UiTextKey(ScreenId.SPLASH, TextType.SUBTITLE, "hero_subtitle"), LocaleCode.TR_TR, "Yolculuğun burada başlıyor"))

    // Login
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.TITLE, "login_title_brand"), LocaleCode.EN_US, "Horse Gallop"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.SUBTITLE, "login_subtitle"), LocaleCode.EN_US, "Your horse riding experience starts here"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.LABEL, "or_label"), LocaleCode.EN_US, "or"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.DESCRIPTION, "terms_consent"), LocaleCode.EN_US, "By continuing you agree to the Terms of Use and Privacy Policy"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_google"), LocaleCode.EN_US, "Continue with Google"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_apple"), LocaleCode.EN_US, "Continue with Apple"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_email"), LocaleCode.EN_US, "Continue with Email"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.TITLE, "login_title_brand"), LocaleCode.TR_TR, "Horse Gallop"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.SUBTITLE, "login_subtitle"), LocaleCode.TR_TR, "At binme deneyiminiz burada başlıyor"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.LABEL, "or_label"), LocaleCode.TR_TR, "veya"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.DESCRIPTION, "terms_consent"), LocaleCode.TR_TR, "Devam ederek Kullanım Koşulları ve Gizlilik Politikasını kabul etmiş olursunuz"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_google"), LocaleCode.TR_TR, "Google ile devam et"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_apple"), LocaleCode.TR_TR, "Apple ile devam et"))
    add(UiText(UiTextKey(ScreenId.LOGIN, TextType.BUTTON, "continue_with_email"), LocaleCode.TR_TR, "E‑posta ile devam et"))

    // Ride Tracking
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.TITLE, "ride_title"), LocaleCode.EN_US, "Ready to Ride?"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.SUBTITLE, "ride_subtitle"), LocaleCode.EN_US, "Start tracking your adventure"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.BUTTON, "start_ride"), LocaleCode.EN_US, "Start Your Ride"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "total_distance"), LocaleCode.EN_US, "Total Distance"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "total_time"), LocaleCode.EN_US, "Total Time"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "avg_speed"), LocaleCode.EN_US, "Avg Speed"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "this_week"), LocaleCode.EN_US, "This Week"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.TITLE, "ride_title"), LocaleCode.TR_TR, "Sürüşe Hazır mısın?"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.SUBTITLE, "ride_subtitle"), LocaleCode.TR_TR, "Maceranı takip etmeye başla"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.BUTTON, "start_ride"), LocaleCode.TR_TR, "Binişe Başla"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "total_distance"), LocaleCode.TR_TR, "Toplam Mesafe"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "total_time"), LocaleCode.TR_TR, "Toplam Süre"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "avg_speed"), LocaleCode.TR_TR, "Ortalama Hız"))
    add(UiText(UiTextKey(ScreenId.RIDE_TRACKING, TextType.LABEL, "this_week"), LocaleCode.TR_TR, "Bu Hafta"))

    // Common/Errors
    add(UiText(UiTextKey(ScreenId.HOME, TextType.ERROR, "error_unknown"), LocaleCode.EN_US, "Unknown error occurred"))
    add(UiText(UiTextKey(ScreenId.HOME, TextType.ERROR, "error_network"), LocaleCode.EN_US, "Network error"))
    add(UiText(UiTextKey(ScreenId.HOME, TextType.ERROR, "error_unknown"), LocaleCode.TR_TR, "Bilinmeyen bir hata oluştu"))
    add(UiText(UiTextKey(ScreenId.HOME, TextType.ERROR, "error_network"), LocaleCode.TR_TR, "Ağ hatası"))
  }
}


