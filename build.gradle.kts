plugins {
    java
    id("org.sonarqube") version "4.3.1.3277"
    id("jacoco")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.sonarqube")
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
    }

    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // 통합 테스트 설정 분리 (시작)
    // 참고: https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations.implementation.get())
    }

    configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
    configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())

    val integrationTest = task<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter("test")
    }

    tasks.check { dependsOn(integrationTest) }
    // 통합 테스트 설정 분리 (끝)

    val buildDir = { path: String -> layout.buildDirectory.dir(path).get().asFile }

    sonar {
        properties {
            property("sonar.java.binaries", buildDir("classes"))
            property("sonar.coverage.jacoco.xmlReportPaths", buildDir("reports/jacoco.xml"))
        }
    }

    jacoco {
        toolVersion = "0.8.9"
    }

    tasks.jacocoTestReport {
        executionData(integrationTest)
        dependsOn(tasks.test, integrationTest)
        reports {
            html.required = true
            xml.required = true
            xml.outputLocation = file(buildDir("reports/jacoco.xml"))
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }
}

sonar {
    properties {
        val exclusions = listOf(
            "**/integrationTest/**",
            "**/test/**",
            "**/*Application*.java",
            "**/*Application*.kt",
            "**/*Config*.java",
            "**/*Config*.kt",
        )
        property("sonar.projectKey", "yellowsunn-home-server_employment-management_AYvjW9ddSRfkzjl51d1E")
        property("sonar.sources", "src")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.test.inclusions", "**/*Test.java, **/*Test.kt")
        property("sonar.exclusions", exclusions.joinToString(","))
        property("sonar.java.coveragePlugin", "jacoco")
    }
    isSkipProject = false
}
