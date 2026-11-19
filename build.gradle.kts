// AGP 9 enables built-in Kotlin with a runtime dependency on an older Kotlin
// Gradle plugin. The official AGP 9 mechanism to raise the Kotlin and KSP
// versions is this buildscript classpath block (see docs/VERSIONS.md).
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.14")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
