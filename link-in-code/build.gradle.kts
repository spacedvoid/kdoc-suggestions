plugins {
	kotlin("jvm") version "2.2.20"
}

group = "io.github.spacedvoid"
version = "0.1.0"

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("reflect"))
	compileOnly("org.jetbrains.dokka:dokka-core:2.0.0")
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(21)
}
