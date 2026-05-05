# App ProGuard rules
-dontobfuscate

-optimizations !class/merging/*

# Keep ALL Kotlin stdlib (including internal classes like CollectionsKt__IterablesKt)
-keep class kotlin.** { *; }
-keepnames class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }
-keep class kotlin.collections.** { *; }
-keepnames class kotlin.collections.** { *; }
-keepclassmembers class kotlin.collections.** {
    public static *** *(...);
    public *** *(...);
}

-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

-keep class com.kingzcheung.xime.plugin.** { *; }
-keepclassmembers class com.kingzcheung.xime.plugin.** { *; }

-keep class com.kingzcheung.xime.rime.** { *; }
-keep class com.kingzcheung.xime.**Jni** { *; }

-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** {
    <fields>;
    <methods>;
}

-keepattributes SourceFile,LineNumberTable

-processkotlinnullchecks remove