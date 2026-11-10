# Examy Proguard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep ZXing & Barcode Scanner
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.journeyapps.barcodescanner.**

# Keep App Models & Classes
-keep class com.examy.app.ExamConfig { *; }
-keep class com.examy.app.** { *; }
