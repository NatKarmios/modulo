import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("com.github.breadmoirai.github-release") version "2.3.7"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    with(rootProject.libs) {
        implementation(klaxon)
        implementation(fuel)
        implementation(fuel.coroutines)
        implementation(classgraph)
        implementation(exposed.jdbc)
        implementation(postgres)
        implementation(sqlite)
        kapt(pf4j)
    }

    implementation(project(":modulo-api"))
}

tasks {
    withType<KotlinCompile> { kotlinOptions { jvmTarget = "1.8" } }
}

application {
    mainClass.set("com.karmios.modulo.core.MainKt")
}

githubRelease {
    token(System.getenv("TOKEN") ?: "")

    owner("NatKarmios")
    repo("modulo")
    tagName("v${parent!!.version}")
    releaseName("v${parent!!.version}")
//    body("## CHANGELOG\n${changelog().call()}")
    releaseAssets("build/libs/scromble") // TODO change
    prerelease(System.getenv("IS_PRERELEASE") == "1")
}
