# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\DevelopeSoft\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-keep class com.tencent.stat.* { ;}
#-keep class com.tencent.mid.* { ;}

-keep class com.tencent.stat.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
-keep class com.tencent.mta.track.**

-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**

#okhttp
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

#retrofit
-dontwarn okio.**
-dontwarn javax.annotation.**

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# ProGuard configurations for Bonree-Agent
-keep public class com.bonree.**{*;}
-keep public class bonree.**{*;}
-dontwarn com.bonree.** -dontwarn bonree.**
# End Bonree-Agent

-dontwarn javax.servlet.**
-dontwarn org.apache.thrift.transport.**
-dontwarn org.slf4j.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-dontwarn rx.internal.util.unsafe.*

