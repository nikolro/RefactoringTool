plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2023.3.3")
    type.set("IC")
    plugins.set(listOf("java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnit()
        testLogging {
            events(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }

    processTestResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("src/test/resources") {
            include("**/*.java")
        }
    }

    val createClasspathIndex by registering {
        val outputFile = file("build/classes/java/test/classpath.index")
        outputs.file(outputFile)
        doLast {
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
        }
    }

    named<JavaCompile>("compileTestJava") {
        dependsOn(processTestResources, createClasspathIndex)
    }

    named("classpathIndexCleanup") {
        dependsOn(createClasspathIndex, processTestResources)
        mustRunAfter(compileTestJava)
    }

    named("instrumentTestCode") {
        // This task is provided by the IntelliJ plugin
    }

    test {
        dependsOn(compileTestJava, createClasspathIndex, named("classpathIndexCleanup"), named("instrumentTestCode"))
    }
}