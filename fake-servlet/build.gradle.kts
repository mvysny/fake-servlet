dependencies {
    api("javax.servlet:javax.servlet-api:4.0.1")
    api(kotlin("stdlib-jdk8"))
    implementation(libs.slf4j.api)

    testImplementation(libs.dynatest)
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureBintray("mock-servlet-environment")
