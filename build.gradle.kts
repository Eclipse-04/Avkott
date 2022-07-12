import io.github.liplum.mindustry.importMindustry
import io.github.liplum.mindustry.mindustry
import io.github.liplum.mindustry.mindustryRepo

plugins {
    kotlin("jvm") version "1.7.10"
    id("io.github.liplum.mgpp") version "1.0.13"
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
        mindustry mirror "dbc2aa4cb7"
        arc on "0af9cd477b"
    }
    client {
        mindustry be latest
    }
    server {
        mindustry be latest
    }
    deploy {
        baseName = project.name
    }
    run {
        setDataDefault()
    }
    io.github.liplum.mindustry.MindustryPlugin.Anuken
}
mindustryAssets {
    root at "$projectDir/assets"
}