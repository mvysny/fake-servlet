dependencies {
    api(libs.javax.servletapi)
    implementation(libs.slf4j.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureBintray("fake-servlet")
