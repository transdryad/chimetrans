plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "org.hazelv"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(files("libs/chime-lang-1.0.1-all.jar"))
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            resources.srcDir("src/main/resources")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "org.hazelv.chimetrans.Main")
    }
}