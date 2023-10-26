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
    // Pour JUnit 5, il est recommandé d'utiliser au moins Java 8. Cependant, si vous rencontrez des problèmes, vous pourriez envisager de revenir à JUnit 4.
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Hibernate 6 nécessite Java 11+. Vous devriez revenir à Hibernate 5 qui est compatible avec Java 8.
    implementation("org.hibernate:hibernate-core:6.1.7.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.1.7.Final")

    // SQLite JDBC est généralement compatible avec Java 8 pour les versions que vous utilisez.
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")

    // Jakarta Persistence et Jakarta Validation sont compatibles avec Java 8 pour les versions que vous utilisez.
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // FlatLaf 3.2.5 nécessite Java 9+. Vous devriez envisager de revenir à une version antérieure compatible avec Java 8.
    implementation("com.formdev:flatlaf:2.0.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "analyzor.controleur.ControleurPrincipal"
    }
}

