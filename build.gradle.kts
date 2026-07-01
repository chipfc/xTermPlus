import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("application")
    id("org.beryx.runtime") version "2.0.1"
}

group = "org.chipfc"
version = "1.0.0"

val vendor = "ChipFC"
val copyright = "© 2026 ChipFC"
val repos = "https://github.com/chipfc/xTermPlus"
val osArch = System.getProperty("os.arch")

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    // --- LOMBOK (Annotation Processing) ---
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    // --- UI & LOOK AND FEEL (FlatLaf & Icons) ---
    val flatlafVersion = "3.7.1"
    implementation("com.formdev:flatlaf:$flatlafVersion")
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")
    implementation("com.formdev:flatlaf-swingx:$flatlafVersion")
    implementation("com.formdev:flatlaf-fonts-inter:4.1")
    implementation("com.formdev:flatlaf-fonts-jetbrains-mono:2.304")
    implementation("com.formdev:flatlaf-fonts-roboto:2.137")
    runtimeOnly("com.formdev:flatlaf-intellij-themes:$flatlafVersion")

    implementation("com.github.weisj:jsvg:2.1.0")

    implementation("com.miglayout:miglayout-swing:11.4.3")
    implementation(":modal-dialog-2.6.1") // https://github.com/DJ-Raven/swing-modal-dialog

    // --- SERIAL COMMUNICATION ---
    implementation("com.fazecast:jSerialComm:2.11.4")

    // --- TERMINAL WIDGET (JediTerm) ---
    val jeditermVersion = "3.72"
    implementation("org.jetbrains.jediterm:jediterm-core:$jeditermVersion")
    implementation("org.jetbrains.jediterm:jediterm-ui:$jeditermVersion")

    // --- UTILITIES & DATA ---
    implementation("ch.qos.logback:logback-classic:1.5.36")
    implementation("com.google.code.gson:gson:2.14.0")

    // --- TESTING ---
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

if (JavaVersion.current() < JavaVersion.VERSION_21)
    throw RuntimeException("compile required Java ${JavaVersion.VERSION_21}, current Java ${JavaVersion.current()}")

println()
println("--------------------------------------------------")
println("${name}-${version}")
println("***")
println("Project Path  : $projectDir")
println("Java Version  : ${System.getProperty("java.version")}")
println("Gradle Version: ${gradle.gradleVersion} at ${gradle.gradleHomeDir}")
println("OS Arch       : ${osArch}")
println("Current Date  : ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
println("--------------------------------------------------")
println()

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.chipfc.Main"
        attributes["Implementation-Vendor"] = "${vendor}"
        attributes["Implementation-Copyright"] = "${copyright}"
        attributes["Implementation-Version"] = "${version}"
        attributes["Implementation-Commit"] = "SNAPSHOT"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

application {
    mainClass.set("org.chipfc.Main")
}

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    jpackage {
        appVersion = "${version}"
        imageName = "${name}"
        installerName = "${name}-${osArch}"

        val commonMetadata = listOf(
            "--vendor", "${vendor}",
            "--copyright", "${copyright}",
            "--description", "xTerm+ Serial Terminal"
        )

        val imageOps = mutableListOf(
            "--java-options", "--enable-native-access=ALL-UNNAMED",
            "--verbose"
        ).apply { addAll(commonMetadata) }

        val installerOps = mutableListOf(
            "--about-url", "${repos}"
        ).apply { addAll(commonMetadata) }

        val os = org.gradle.internal.os.OperatingSystem.current()
        when {
            os.isWindows -> {
                installerType = "exe"
                val winIcon = "src/main/resources/icons/Windows/icon.ico"

                imageOps.addAll(listOf("--icon", winIcon))
                installerOps.addAll(listOf(
                    "--win-dir-chooser",
                    "--win-shortcut",
                    "--win-shortcut-prompt",
                    "--win-menu",
                    "--icon", winIcon
                ))
            }

            os.isMacOsX -> {
                installerType = "dmg"
                imageOps.addAll(listOf("--icon", "src/main/resources/icons/macOS/icon.icns"))
            }

            os.isLinux -> {
                installerType = "deb"
                imageOps.addAll(listOf("--icon", "src/main/resources/icons/Linux/icon.png"))
                installerOps.addAll(listOf(
                    "--linux-shortcut",
                    "--linux-menu-group", "Development"
                ))
            }
        }

        imageOptions = imageOps
        installerOptions = installerOps
    }
}
