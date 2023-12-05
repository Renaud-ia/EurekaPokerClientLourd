import org.gradle.api.tasks.bundling.Jar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

group = "com.eureka"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    implementation("org.apache.logging.log4j:log4j-api:2.22.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.0")

    implementation("org.apache.commons:commons-math:2.2")

    implementation("org.hibernate:hibernate-hikaricp:6.1.7.Final")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.hibernate:hibernate-core:6.1.7.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.1.7.Final")

    implementation("org.xerial:sqlite-jdbc:3.43.0.0")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    implementation("com.formdev:flatlaf:3.2.5")
    implementation("com.miglayout:miglayout-swing:11.0");
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "analyzor.controleur.ControleurPrincipal"
    }
}

