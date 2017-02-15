# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\OaKKuB\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# -------------------------------------------
# Icepick
-dontwarn icepick.**
-keep class icepick.** { *; }
-keep class **$$Icepick { *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}

# -------------------------------------------
# Green EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# -------------------------------------------
# Parcel library
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep class org.parceler.Parceler$$Parcels

# -------------------------------------------
# Material Sheet Fab
-keep class io.codetail.animation.arcanimator.** { *; }

# -------------------------------------------
# Firebase
-keepattributes Signature
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.**
-keep class com.oakkub.chat.models.** { *; }

# -------------------------------------------
# Jackson inside Firebase
-keepnames class com.fasterxml.jackson.annotation.** { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
-keepattributes Annotation,EnclosingMethod,Signature

# -------------------------------------------
# Android Support Library
#-keep public class android.support.v7.widget.** { *; }
#-keep public class android.support.v7.internal.widget.** { *; }
#-keep public class android.support.v7.internal.view.menu.** { *; }
#-keep public class * extends android.support.v4.view.ActionProvider {
#    public <init>(android.content.Context);
#}
-keep class android.support.v7.widget.SearchView { *; }

# -------------------------------------------
# Parcelable Object
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

