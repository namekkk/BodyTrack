# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.bodytrack.app.data.local.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
