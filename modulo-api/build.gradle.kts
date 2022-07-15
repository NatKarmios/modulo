plugins {
    java
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("api") {
            groupId = parent!!.group as String
            artifactId = project.name
            version = parent!!.version as String

            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Used to create Modulo plugins")
                url.set("https://github.com/NatKarmios/modulo")

                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("nat karmios")
                        name.set("Nat Karmios")
                        email.set("nat@karmios.com")
                    }
                }

                scm {
                    url.set("https://github.com/NatKarmios/modulo")
                    connection.set("scm:git:https://github.com/NatKarmios/modulo.git")
                    developerConnection.set("scm:git:https://github.com/NatKarmios/modulo.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/NatKarmios/modulo")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
        maven {
            name = "ossrhSnapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")

            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }

        maven {
            name = "ossrhStaging"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        publishing.publications.forEach { sign(it) }
    }
}
