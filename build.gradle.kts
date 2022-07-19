import io.github.liplum.mindustry.importMindustry
import io.github.liplum.mindustry.mindustry
import io.github.liplum.mindustry.mindustryRepo

plugins {
    kotlin("jvm") version "1.7.10"
    id("io.github.liplum.mgpp") version "1.1.4"
}
sourceSets {
    main {
        java.srcDirs("src")
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
repositories {
    mindustryRepo()
    mavenCentral()
}
dependencies {
    importMindustry()
}
mindustry {
    dependency {
        mindustry on "v136"
        arc on "v136"
    }
    client {
        mindustry official "v136.1"
    }
    server {
        mindustry official "v136.1"
    }
    deploy {
        baseName = project.name
    }
    run {
        useDefaultDataDir
        keepOtherMods
    }
}
mindustryAssets {
    root at "$projectDir/assets"
}