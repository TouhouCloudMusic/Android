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
# ==================== THCDB API 规则 ====================

# 保留所有数据模型类及其成员
-keep class net.hearnsoft.thcdb_api.model.** { *; }
-keepclassmembers class net.hearnsoft.thcdb_api.model.** { *; }
-dontwarn net.hearnsoft.thcdb_api.model.**

# 保留 API 接口
-keep interface net.hearnsoft.thcdb_api.api.** { *; }
-keepclassmembers interface net.hearnsoft.thcdb_api.api.** { *; }

# 保留所有使用 @SerializedName 注解的字段
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# 保留 Gson TypeAdapter
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 BaseResponse 的内部类和枚举
-keep class net.hearnsoft.thcdb_api.model.BaseResponse$** { *; }
-keepclassmembers class net.hearnsoft.thcdb_api.model.BaseResponse$** { *; }

# 保留泛型签名
-keepattributes Signature

# 保留注解
-keepattributes *Annotation*

# 保留源文件和行号信息（用于调试）
-keepattributes SourceFile,LineNumberTable

# Retrofit 相关规则
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# 保留 Retrofit 接口方法参数名称
-keepattributes MethodParameters

# OkHttp 相关规则
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Kotlin 相关规则
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}