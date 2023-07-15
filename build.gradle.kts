plugins {
    kotlin("multiplatform") version "1.9.0"
}

group = "center.sciprog"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser()
    }
    sourceSets {
        val commonMain by getting{
            dependencies {
                implementation("space.kscience:kmath-stat:0.3.1")
                implementation("space.kscience:kmath-functions:0.3.1")
                implementation("space.kscience:kmath-for-real:0.3.1")
                implementation("space.kscience:kmath-optimization:0.3.1")
                implementation("space.kscience:plotlykt-core:0.5.3")


                implementation(kotlin("test"))
            }
        }
    }
}
