import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    java
    id("io.spring.dependency-management") version libs.versions.spring.dependency.management.plugin.get()
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.fasterxml.jackson:jackson-bom:2.19.0")
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

dependencies {
    implementation(project(":jpa-cache-core"))
    implementation(project(":jpa-cache-api"))

    // JPA support
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

    compileOnly(libs.jackson.databind)
    testImplementation(libs.jackson.databind)

    // Spring Framework 6.x
    testImplementation("org.springframework:spring-jdbc")
    testImplementation("org.springframework:spring-context")
    testImplementation("org.liquibase:liquibase-core:4.20.0")

    // Spring Boot 3.x test support
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

    // Testcontainers
    testImplementation("org.testcontainers:postgresql:1.21.1")
    testImplementation("org.testcontainers:oracle-xe:1.21.1")
    testImplementation("org.testcontainers:mssqlserver:1.21.1")
    testImplementation("org.testcontainers:junit-jupiter:1.21.1")

    // Database drivers
    testImplementation("org.postgresql:postgresql:42.7.7")
    testImplementation("com.oracle.database.jdbc:ojdbc8:23.8.0.25.04")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:12.10.0.jre8")

    testImplementation("ch.qos.logback:logback-classic:1.4.11")
    testImplementation("ch.qos.logback:logback-core:1.4.11")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events = setOf(FAILED, SKIPPED, PASSED, STANDARD_OUT)
    }
}

tasks.register<Test>("testPostgres") {
    useJUnitPlatform()
    systemProperty("jdbc.test.db", "postgres")
}

tasks.register<Test>("testOracle") {
    useJUnitPlatform()
    systemProperty("jdbc.test.db", "oracle")
}

tasks.register<Test>("testMsSql") {
    useJUnitPlatform()
    systemProperty("jdbc.test.db", "mssql")
}

tasks.register("integrationTestAll") {
    dependsOn("testPostgres", "testOracle", "testMsSql")
}
