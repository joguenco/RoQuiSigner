plugins {
    application
    id("com.diffplug.spotless") version "8.11.0"
    id("maven-publish")
}

spotless {
    java {
        googleJavaFormat().aosp()

        // Optionally, add other steps
        formatAnnotations()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()

        // Specify target files (e.g., all .java files except those in build directory)
        target("src/**/*.java")
        targetExclude("build/**")
    }
}

version = "2.0.0"
group = "dev.joguenco.signer"

repositories {
    mavenCentral()
}

dependencies {
    implementation("eu.europa.ec.joinup.sd-dss:dss-xades:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils-apache-commons:6.5.1")
    compileOnly("org.projectlombok:lombok:1.18.48")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.joguenco.roqui.signer.Main"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("RoQuiSigner-$version.jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "RoQuiSigner"
            from(components["java"])
            pom {
                name.set("RoQuiSigner")
                description.set("XML Digital Signature Application")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
