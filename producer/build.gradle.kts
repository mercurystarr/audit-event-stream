plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":proto"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.spring.kafka)
    implementation(libs.protobuf.java)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // not read yet
    // testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
}