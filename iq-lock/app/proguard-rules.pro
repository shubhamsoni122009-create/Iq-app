# proguard-rules.pro — ProGuard/R8 rules for the release build.
# Keep Room entity classes so their field names aren't obfuscated at runtime.

-keep class com.iqlock.app.data.entity.** { *; }
-keep class com.iqlock.app.data.dao.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep ViewBinding and data binding classes
-keep class **.databinding.** { *; }

# Keep accessibility service
-keep class com.iqlock.app.service.IQLockAccessibilityService { *; }

# Keep boot receiver
-keep class com.iqlock.app.receiver.BootReceiver { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
