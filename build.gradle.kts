plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

group = "com.proxy.gshttp"
version = "1.0.4"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    jvm("desktop") {
        withJava()
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release.proguard.isEnabled.set(false)

        nativeDistributions {
            val currentOs = System.getProperty("os.name").lowercase()

            if (currentOs.contains("win")) {
                targetFormats(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
                )
            } else if (currentOs.contains("linux")) {
                targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            }

            packageName = "GS.HTTP"
            packageVersion = project.version.toString()
            vendor = "GS Ecosystem"

            includeAllModules = true

            windows {
                upgradeUuid = "8f4b9c2a-3d1e-4b7a-9c5f-1a2b3c4d5e6f"
                perUserInstall = true
                menu = true
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }

            linux {
                shortcut = true
                menuGroup = "Development"
                appCategory = "Utility"
                iconFile.set(project.file("src/desktopMain/resources/ic_gs_ht.png"))
            }
        }
    }
}

tasks.register<Tar>("packageTarGz") {
    archiveFileName.set("gs-http-${project.version}.tar.gz")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    compression = Compression.GZIP

    dependsOn(provider {
        tasks.matching { it.name.contains("Distributable", ignoreCase = true) }
    })

    from(layout.buildDirectory.dir("compose/binaries/main-release/app"))
}
