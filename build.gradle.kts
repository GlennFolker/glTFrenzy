import java.io.*

plugins{
    `java-library`
    `maven-publish`
}

val arcVersion: String by project
val mindustryVersion: String by project
val entVersion: String by project
val javapoetVersion: String by project

fun arc(module: String): String{
    return "com.github.Anuken.Arc$module:$arcVersion"
}

fun mindustry(module: String): String{
    return "com.github.Anuken.Mindustry$module:$mindustryVersion"
}

fun entity(module: String): String{
    return "com.github.GlennFolker.EntityAnno$module:$entVersion"
}

fun javapoet(): String{
    return "com.squareup:javapoet:$javapoetVersion"
}

allprojects{
    apply(plugin = "java-library")

    sourceSets["main"].java.setSrcDirs(listOf(layout.projectDirectory.file("src")))
    dependencies{
        annotationProcessor(entity(":downgrader"))
    }

    repositories{
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
        maven("https://raw.githubusercontent.com/GlennFolker/EntityAnnoMaven/main")

        maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        maven("https://jitpack.io")
    }
}

project(":annotations"){
    sourceSets["main"].resources.setSrcDirs(listOf(layout.projectDirectory.file("assets")))

    dependencies{
        implementation(arc(":arc-core"))
        implementation(arc(":g3d"))
        implementation(javapoet())
    }

    tasks.withType<JavaCompile>().configureEach{
        options.apply{
            isIncremental = true
            encoding = "UTF-8"
            compilerArgs.add("-Xlint:-options")
        }

        sourceCompatibility = "17"
        targetCompatibility = "8"

        doFirst{
            sourceCompatibility = "8"
        }
    }
}

project(":"){
    apply(plugin = "maven-publish")

    group = "com.github.GlennFolker"

    java{
        withJavadocJar()
        withSourcesJar()
    }

    dependencies{
        annotationProcessor(project(":annotations"))
        compileOnly(project(":annotations"))

        compileOnlyApi(arc(":arc-core"))
        compileOnlyApi(arc(":g3d"))
    }

    tasks.withType<Jar>().configureEach{
        exclude("gltfrenzy/spec/**")
        metaInf{
            from(layout.projectDirectory.file("LICENSE"))
        }
    }

    tasks.withType<JavaCompile>().configureEach{
        sourceCompatibility = "17"
        options.apply{
            release = 8
            compilerArgs.add("-Xlint:-options")

            isIncremental = true
            encoding = "UTF-8"
        }
    }

    tasks.withType<Javadoc>().configureEach{
        options{
            encoding = "UTF-8"

            val exports = (project.property("org.gradle.jvmargs") as String)
                .split(Regex("\\s+"))
                .filter{it.startsWith("--add-opens")}
                .map{"--add-exports ${it.substring("--add-opens=".length)}"}
                .reduce{accum, arg -> "$accum $arg"}

            val opts = File(temporaryDir, "exports.options")
            BufferedWriter(FileWriter(opts, Charsets.UTF_8, false)).use{it.write("-Xdoclint:none $exports")}
            optionFiles(opts)
        }
    }

    publishing.publications.register<MavenPublication>("maven"){
        from(components["java"])
        pom{
            name = "glTFrenzy"
            description = "A glTF loader for Arc-based projects."
            url = "https://github.com/GlennFolker/glTFrenzy"
            inceptionYear = "2024"

            licenses{
                license{
                    name = "GPL-3.0-or-later"
                    url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    distribution = "repo"
                }
            }

            issueManagement{
                system = "GitHub Issue Tracker"
                url = "https://github.com/GlennFolker/EntityAnno/issues"
            }
        }
    }
}
