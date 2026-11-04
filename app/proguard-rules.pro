# Proguard/R8 rules for RiceCheck

# LiteRT (JNI entry points and native-backed types must not be renamed/stripped).
-keep class com.google.ai.edge.litert.** { *; }
-dontwarn com.google.ai.edge.litert.**

# Room generated code and entities.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep line numbers for readable release crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
