import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader

plugins {
    idea
    `java-library`
    kotlin("jvm") version "2.0.0"
    id("net.neoforged.moddev.legacyforge") version "2.0.80"
    id("com.hypherionmc.modutils.modpublisher") version "2.1.6"
}

val modId: String by project
val modVersion: String by project
val modGroupId: String by project
val modName: String by project
val mcVersion: String by project
val minecraftVersionRange: String by project
val forgeVersion: String by project
val forgeVersionRange: String by project
val loaderVersionRange: String by project
val parchmentMappingsVersion: String by project
val parchmentMinecraftVersion: String by project

version = modVersion
group = modGroupId

base {
    archivesName = modId
}

// Mojang ships Java 21 to end users in 1.20.1, so mods should target Java 17.
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
kotlin.jvmToolchain(17)

legacyForge {
    // Specify the version of MinecraftForge to use.
    version = "$mcVersion-$forgeVersion"

    parchment {
        mappingsVersion = parchmentMappingsVersion
        minecraftVersion = parchmentMinecraftVersion
    }

    // This line is optional. Access Transformers are automatically detected
    // accessTransformers = project.files("src/main/resources/META-INF/accesstransformer.cfg")

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        create("client") {
            client()

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()

            // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
            // gameDirectory = project.file("run-data")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                "--mod", modId, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath
            )
        }

        // applies to all the run configs above
        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            gameDirectory.set(file("run-$name"))
            systemProperty("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            // logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // mostly optional in a single mod project
        // but multi mod projects should define one per mod
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val localRuntime: Configuration by configurations.creating

configurations {
    configurations.named("runtimeClasspath") {
        extendsFrom(localRuntime)
    }
}

obfuscation {
    createRemappingConfiguration(localRuntime)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
    maven("https://maven.createmod.net")
    maven("https://modmaven.dev")
}

dependencies {
    val kotlinForForgeVersion = "4.11.0"
    val mixinExtraVersion = "0.4.1"
    val mixinVersion = "0.8.5"

    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtraVersion")!!)
    implementation("io.github.llamalad7:mixinextras-forge:$mixinExtraVersion")
    annotationProcessor("org.spongepowered:mixin:$mixinVersion:processor")
}

// Uncomment the lines below if you wish to configure mixin. The mixin file should be named modid.mixins.json.

/*
mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
}

tasks.jar {
    manifest.attributes(mapOf(
        "MixinConfigs" to "${modId}.mixins.json"
    ))
}
*/

publisher {
    apiKeys {
        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
        modrinth(System.getenv("MODRINTH_API_KEY"))
    }

    curseID.set("")
    modrinthID.set("")
    versionType.set("release")
    changelog.set(file("changelog.md"))
    version.set(project.version.toString())
    displayName.set("$modName $modVersion")
    setGameVersions(mcVersion)
    setLoaders(ModLoader.FORGE)
    setCurseEnvironment(CurseEnvironment.BOTH)
    artifact.set("build/libs/${base.archivesName.get()}-${project.version}.jar")

    curseDepends {
        required("kotlin-for-forge")
    }
    modrinthDepends {
        required("kotlin-for-forge")
    }
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val modLicense: String by project
    val modAuthors: String by project
    val modDescription: String by project

    val replaceProperties = mapOf(
        "minecraftVersion" to mcVersion,
        "minecraftVersionRange" to minecraftVersionRange,
        "forgeVersion" to forgeVersion,
        "forgeVersionRange" to forgeVersionRange,
        "loaderVersionRange" to loaderVersionRange,
        "modId" to modId,
        "modName" to modName,
        "modLicense" to modLicense,
        "modVersion" to modVersion,
        "modAuthors" to modAuthors,
        "modDescription" to modDescription,
    )

    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main.get().resources.srcDir("src/generated/resources")
sourceSets.main.get().resources.srcDir(generateModMetadata)
legacyForge.ideSyncTask(generateModMetadata)

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
