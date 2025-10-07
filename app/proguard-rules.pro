# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes
-keep class com.hindustani.pitchdetector.data.** { *; }
-keep class com.hindustani.pitchdetector.music.HindustaniNoteConverter$HindustaniNote { *; }
-keep class com.hindustani.pitchdetector.audio.PYINDetector$PitchResult { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Uncomment if you encounter issues with reflection
-keepattributes Signature
-keepattributes *Annotation*
