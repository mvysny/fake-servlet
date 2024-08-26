import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    `maven-publish`
    signing
}

defaultTasks("clean", "build")

allprojects {
    group = "com.github.mvysny.fake-servlet"
    version = "1.2-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {

    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("org.gradle.signing")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the exceptions of failed tests in CI console.
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }

    // creates a reusable function which configures proper deployment to Maven Central
    ext["configureMavenCentral"] = { artifactId: String ->

        java {
            withJavadocJar()
            withSourcesJar()
        }

        tasks.withType<Javadoc> {
            isFailOnError = false
        }

        publishing {
            repositories {
                maven {
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = project.properties["ossrhUsername"] as String? ?: "Unknown user"
                        password = project.properties["ossrhPassword"] as String? ?: "Unknown user"
                    }
                }
            }
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description = "Fake Servlet"
                        name = artifactId
                        url = "https://github.com/mvysny/fake-servlet"
                        licenses {
                            license {
                                name = "The MIT License (MIT)"
                                url = "https://opensource.org/licenses/MIT"
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "mavi"
                                name = "Martin Vysny"
                                email = "mavi@vaadin.com"
                            }
                        }
                        scm {
                            url = "https://github.com/mvysny/fake-servlet"
                        }
                    }

                    from(components["java"])
                }
            }
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}

if (JavaVersion.current() > JavaVersion.VERSION_11 && gradle.startParameter.taskNames.contains("publish")) {
    throw GradleException("Release fake-servlet 1.x with JDK 11 or lower; current JDK is ${JavaVersion.current()}")
    // otherwise Kotlin will use Stream.toList() from JDK 15+, which will cause fake-servlet not working on JDK 14-
}

