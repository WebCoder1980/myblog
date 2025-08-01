val javaProjects = listOf(
    project(":users-service")
)

plugins {
    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("jacoco")
}

configure(javaProjects) {
    println(name)

    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "jacoco")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    version = "1.0.0"

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa:3.5.4")
        "implementation"("org.springframework.boot:spring-boot-starter-security:3.5.4")
        "implementation"("org.springframework.boot:spring-boot-starter-web:3.5.4")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test:3.5.4")
        "testImplementation"("org.springframework.security:spring-security-test:6.5.2")

        "runtimeOnly"("org.postgresql:postgresql:42.7.7")

        "compileOnly"("org.projectlombok:lombok:1.18.38")
        "annotationProcessor"("org.projectlombok:lombok:1.18.38")

        "testRuntimeOnly"("org.junit.platform:junit-platform-commons:1.13.4")

        "implementation"("jakarta.validation:jakarta.validation-api:3.0.2")
        "implementation"("org.hibernate.validator:hibernate-validator:9.0.1.Final")

        "implementation"("io.jsonwebtoken:jjwt-api:0.11.5")
        "runtimeOnly"("io.jsonwebtoken:jjwt-impl:0.11.5")
        "runtimeOnly"("io.jsonwebtoken:jjwt-jackson:0.11.5")

        "implementation"("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

        "implementation"("org.apache.logging.log4j:log4j-core:2.25.1")
        "implementation"("org.apache.logging.log4j:log4j-api:2.25.1")
    }

    jacoco {
        toolVersion = "0.8.13"
        reportsDirectory.set(layout.buildDirectory.dir("jacocoReport"))
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}