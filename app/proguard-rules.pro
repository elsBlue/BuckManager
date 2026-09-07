# Buck Manager — ProGuard / R8 rules

-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exception
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Kotlin Serialization
-keep,includedescriptorclasses class com.buckmanager.app.**783550serializer { *; }
-keepclassmembers class com.buckmanager.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.buckmanager.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep data models used via reflection / JSON
-keep class com.buckmanager.app.model.** { *; }
-keep class com.buckmanager.app.data.** { *; }

# Google / Drive / Auth clients
-dontwarn com.google.api.**
-dontwarn com.google.api.client.**
-dontwarn org.apache.http.**
-keep class com.google.api.** { *; }

# Widgets / manifests components are kept by default; ensure provider
-keep class com.buckmanager.app.widget.** { *; }
-keep class com.buckmanager.app.MainActivity { *; }

# Play Billing
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }

