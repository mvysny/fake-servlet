# Fake Servlet

A fake implementation of both `javax.servlet` and `javaee.servlet` APIs. Requires Java 8+. Written in Kotlin.

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    testImplementation("com.github.mvysny.fake-servlet:fake-servlet:1.0") // for javax.servlet
    testImplementation("com.github.mvysny.fake-servlet:fake-servlet5:1.0") // for javaee.servlet
}
```

> Note: obtain the newest version from the tag name above

For Maven it's really easy: the jar is published on Maven Central, so all you need to do is to add the dependency
to your `pom.xml`:

```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.fake-servlet</groupId>
			<artifactId>fake-servlet</artifactId>
			<version>1.0</version>
			<scope>test</scope>
		</dependency>
    </dependencies>
</project>
```

## Quickstart

Use the following code to obtain the mock/fake instances:

```kotlin
val context: ServletContext = FakeContext()
val servletConfig: ServletConfig = FakeServletConfig(context)
val session: HttpSession = FakeHttpSession.create(context)
val request: HttpServletRequest = FakeRequest(session)
val response: HttpServletResponse = FakeResponse()
```

See the `MockHttpEnvironment` class to tune the values returned by the `MockRequest` class.
