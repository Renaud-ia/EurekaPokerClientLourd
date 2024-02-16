import org.gradle.api.tasks.bundling.Jar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "com.eureka-poker"
version = "0.3.0-BETA"

repositories {
    mavenCentral()
}

dependencies {
    // tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    // logging
    implementation("org.apache.logging.log4j:log4j-api:2.22.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.0")

    // maths et probas
    implementation("org.apache.commons:commons-math3:3.6.1")

    // bdd centrale et orm
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")

    implementation("org.hibernate:hibernate-hikaricp:6.1.7.Final")
    implementation("org.hibernate:hibernate-core:6.4.1.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.1.7.Final")

    implementation("com.h2database:h2:2.2.224")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // bdd calculs
    implementation("berkeleydb:je:3.2.76")


    // interface graphique
    implementation("com.formdev:flatlaf:3.2.5")
    implementation("com.miglayout:miglayout-swing:11.0")

    // todo pour excel à supprimer
    implementation("org.apache.poi:poi-ooxml:5.2.4")
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

