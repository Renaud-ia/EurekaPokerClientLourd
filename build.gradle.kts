import org.gradle.api.tasks.bundling.Jar

plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.hibernate:hibernate-core:6.3.1.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.1.6.Final")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-M1.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("myJar") {
    from(configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) project.fileTree(it) else zipTree(it) })
    manifest {
        attributes["Main-Class"] = "analyzor.modele.ControleurPrincipal"
    }
    archiveFileName.set("analyzor.jar")
}

