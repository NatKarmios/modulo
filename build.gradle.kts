import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group "com.karmios.modulo"
version "0.1.1"

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }


    repositories {
        mavenCentral()
        jcenter()
    }

    val implementation by configurations

    dependencies {
        with(rootProject.libs) {
            implementation(slf4j.api)
            implementation(slf4j.log4j12)
            implementation(diskord)
            implementation(exposed)
            implementation(pf4j)
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
