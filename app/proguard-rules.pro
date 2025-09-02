# Baby Routine Tracker - Production ProGuard Rules
# 
# These rules ensure proper obfuscation while maintaining compatibility
# with Firebase, Jetpack Compose, and other critical dependencies.

# Preserve line numbers for better crash reports in production
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# General Android optimizations
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Firebase Rules
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep Firebase model classes that are serialized/deserialized
-keep class com.github.slamdev.babyroutinetracker.data.model.** { *; }
-keepclassmembers class com.github.slamdev.babyroutinetracker.data.model.** {
    <init>(...);
}

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.** { *; }

# Firebase Functions
-keep class com.google.firebase.functions.** { *; }

# Jetpack Compose Rules
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    <methods>;
}

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }

# Navigation Compose
-keep class androidx.navigation.** { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep annotation classes
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.** { *; }

# Coil image loading library
-dontwarn coil.**
-keep class coil.** { *; }

# Vico charts library
-keep class com.patrykandpatrick.vico.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# OkHttp (used by Firebase)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson (if used indirectly)
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep custom exceptions for better crash reporting
-keep public class * extends java.lang.Exception

# R8 optimization settings
-allowaccessmodification
# TODO fails with R8: Unknown option "-optimizeaggressively"
# -optimizeaggressively

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception