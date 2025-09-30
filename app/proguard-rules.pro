# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep AdInCountryApp class
-keep class com.example.adincountry.AdInCountryApp { *; }

# Keep all Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules** { *; }
-keep class **_HiltComponents** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# Keep all classes annotated with @HiltAndroidApp
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Keep all classes annotated with @AndroidEntryPoint
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
