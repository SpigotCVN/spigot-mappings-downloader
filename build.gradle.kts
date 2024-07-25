plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
}

group = "io.github.spigotcvn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // This is required for jgit, we never actually use slf4j
    implementation("org.slf4j:slf4j-simple:1.7.2")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.eclipse.jgit:org.eclipse.jgit:4.6.0.201612231935-r")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.guava:guava:33.2.0-jre")
}

tasks.jar {
    finalizedBy(tasks.shadowJar)
    manifest {
        attributes("Main-Class" to "io.github.spigotcvn.smdownloader.Main")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "cvn-repo"
            url = uri("https://maven.radsteve.net/cvn")

            credentials {
                username = System.getenv("CVN_MAVEN_USER")
                password = System.getenv("CVN_MAVEN_TOKEN")
            }
        }
    }
}
