plugins {
    id("java")
}

/* ===== Project Properties ===== */
val modGroup    = project.property("mod_group")     as String
val modVersion  = project.property("mod_version")   as String
/* ============================== */

group = modGroup
version = modVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    compileOnly(libs.hytale)
}
