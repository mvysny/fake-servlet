dependencies {
    api(libs.javax.servletapi)
    api(kotlin("stdlib-jdk8"))
    implementation(libs.slf4j.api)

    testImplementation(libs.dynatest)
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
