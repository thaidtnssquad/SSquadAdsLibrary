# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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
-keep class com.android.vending.billing.**
-keep class com.vapp.admoblibrary.utils.SweetAlert.**

-keep public class com.google.android.gms.common.** { *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-keep public class com.android.installreferrer.** { *; }

-keep class com.reyun.** {*; }
-keep class route.**{*;}
-keep interface com.reyun.** {*; }
-keep interface route.**{*;}
-dontwarn com.reyun.**
-dontwarn org.json.**
-keep class org.json.**{*;}

-keep class com.tiktok.** { *; }
-keep class com.android.billingclient.api.** { *; }
-keep class androidx.lifecycle.** { *; }

-keep class com.tenjin.** { *; }
-keep class * extends java.util.ListResourceBundle {
    protected java.lang.Object[][] getContents();
}
