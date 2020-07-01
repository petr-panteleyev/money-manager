/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
import org.gradle.internal.os.OperatingSystem
import java.time.LocalDateTime

group = "org.panteleyev.money"
version = "20.5.3"
description = "Money Manager"

val moneyTestMysqlHost: String? by project
val moneyTestMysqlUser: String? by project
val moneyTestMysqlPassword: String? by project
val moneyTestMysqlDatabase: String? by project

project.ext["buildTimestamp"] = LocalDateTime.now().toString()

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("org.panteleyev.jpackageplugin") version "0.0.2"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.panteleyev:java-api-for-mysql:1.3.0")
    implementation("org.panteleyev:java-fx-helpers:1.3.0")
    implementation("org.panteleyev:ofx-parser:0.1.0")
    implementation("org.controlsfx:controlsfx:11.0.1")
    implementation("mysql:mysql-connector-java:8.0.16")
    implementation("org.jsoup:jsoup:1.12.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("org.freemarker:freemarker:2.3.29")
    testImplementation("org.testng:testng:6.14.3")
}

plugins.withType<JavaPlugin>().configureEach {
    configure<JavaPluginExtension> {
        modularity.inferModulePath.set(true)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

javafx {
    version = "14.0.1"
    modules(
        "javafx.base",
        "javafx.controls",
        "javafx.web",
        "javafx.swing",
        "javafx.media",
        "javafx.graphics"
    )
}

application {
    mainModule.set("org.panteleyev.money")
    mainClass.set("org.panteleyev.money.MoneyApplication")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs(
        "--enable-preview",
        "--add-exports", "javafx.base/com.sun.javafx.event=org.controlsfx.controls",
        "--add-opens", "javafx.graphics/javafx.scene=org.controlsfx.controls",
        "--add-reads", "org.panteleyev.mysqlapi=org.panteleyev.money"
    )
    useTestNG()

    systemProperty("mysql.host", moneyTestMysqlHost ?: "localhost")
    systemProperty("mysql.database", moneyTestMysqlDatabase ?: "moneytestdb")
    if (moneyTestMysqlUser != null && moneyTestMysqlPassword != null) {
        systemProperty("mysql.user", moneyTestMysqlUser as String)
        systemProperty("mysql.password", moneyTestMysqlPassword as String)
    }
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--enable-preview",
        "-Dfile.encoding=UTF-8",
        "--add-exports",
        "javafx.base/com.sun.javafx.event=org.controlsfx.controls"
    )
    systemProperty("money.profile", System.getProperty("money.profile"))
}

tasks.withType<Jar> {
    destinationDirectory.set(File("$buildDir/jmods"))
}

tasks.processResources {
    filesMatching("**/*.properties") {
        expand(project.properties)
    }
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jmods")
}

tasks.withType<org.panteleyev.jpackage.JPackageTask> {
    dependsOn("build")
    dependsOn("copyDependencies")

    appName = "Money Manager"
    appVersion = project.version as String
    vendor = "panteleyev.org"
    copyright = "Copyright (c) 2016, 2020, Petr Panteleyev"
    runtimeImage = System.getProperty("java.home")
    module = "org.panteleyev.money/org.panteleyev.money.MoneyApplication"
    modulePath = "$buildDir/jmods"
    destination = "$buildDir/dist"
    javaOptions = listOf(
        "--enable-preview",
        "-Dfile.encoding=UTF-8",
        "--add-exports",
        "javafx.base/com.sun.javafx.event=org.controlsfx.controls"
    )

    mac {
        icon = "icons/icons.icns"
    }

    windows {
        icon = "icons/icons.ico"
        winMenu = true
        winDirChooser = true
        winUpgradeUuid = "38dac4b6-91d2-4ca8-aacb-2e0cfd54127a"
        winMenuGroup = "panteleyev.org"
    }
}
