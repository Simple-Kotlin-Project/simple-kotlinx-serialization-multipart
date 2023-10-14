plugins {
    alias(libs.plugins.simple.kotlin.gradle)
    alias(libs.plugins.kotlin.serialization.plugin)
}

group = "io.github.edmondantes"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.simple.kotlin.serialization.utils)
                implementation(libs.simple.kotlin.multipart)
                implementation(libs.kotlin.serialization)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

licenses {
    apache2()
}

developers {
    developer {
        name = "Ilia Loginov"
        email = "masaqaz40@gmail.com"
        organizationName("github")
        role("Maintainer")
        role("Developer")
    }
}

simplePom {
    any {
        title = "Simple kotlin Telegram API for Kotlin"
        description = "Library with entities for Telegram API"
        url = "#github::Simple-Kotlin-Project::${project.name}"
        scm {
            url = "#github::Simple-Kotlin-Project::${project.name}::master"
            connection = "#github::Simple-Kotlin-Project::${project.name}"
            developerConnection = "#github::Simple-Kotlin-Project::${project.name}"
        }
    }
}