val springVersion: String = libs.versions.spring.bom.get()

dependencies {
    api(project(":jpa-cache-api"))

    compileOnly(platform("org.springframework:spring-framework-bom:$springVersion"))
    compileOnly("org.springframework:spring-jdbc")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-tx")

    compileOnly(libs.jackson.databind)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.slf4j.simple)

    testImplementation(platform("org.springframework:spring-framework-bom:$springVersion"))
    testImplementation("org.springframework:spring-jdbc")
    testImplementation("org.springframework:spring-context")
    testImplementation("org.springframework:spring-tx")
    testImplementation("org.springframework:spring-tx")
    testImplementation("org.springframework:spring-tx")

    testImplementation(libs.jackson.databind)
}

configureMavenPublication(
    artifactId = "jpa-cache-core",
    descriptionText = "Spring Cache JPA core"
)
configureSigning()
