# Keep the IME service entry point
-keep public class com.ajay.prokeyboard.inputMethodService { *; }

# Keep all keyboard view classes (custom View used directly, not via reflection)
-keep public class com.ajay.prokeyboard.keyboard.** { *; }

# Keep all Activity/Service/Dialog subclasses referenced in the manifest
-keep public class com.ajay.prokeyboard.MainActivity { *; }
-keep public class com.ajay.prokeyboard.settings { *; }
-keep public class com.ajay.prokeyboard.about { *; }
-keep public class com.ajay.prokeyboard.attribution { *; }
-keep public class com.ajay.prokeyboard.dialogBox { *; }
-keep public class com.ajay.prokeyboard.SlideAdapter { *; }

# Keep line numbers in stack traces for easier crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
