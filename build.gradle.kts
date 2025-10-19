import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader

plugins {
    idea
    `java-library`
    kotlin("jvm") version "2.0.0"
    id("net.neoforged.moddev.legacyforge") version "2.0.95"
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

java.toolchain.languageVersion = JavaLanguageVersion.of(17)
kotlin.jvmToolchain(17)

legacyForge {
    version = "$mcVersion-$forgeVersion"

    parchment {
        mappingsVersion = parchmentMappingsVersion
        minecraftVersion = parchmentMinecraftVersion
    }

    runs {
        create("client") {
            client()
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()

            programArguments.addAll(
                "--mod", modId, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            gameDirectory.set(file("run-$name"))
            systemProperty("forge.logging.markers", "REGISTRIES")
        }
    }

    mods {
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
    maven("https://api.modrinth.com/maven")
    maven("https://cursemaven.com")
    maven("https://maven.tterrag.com/")
}

dependencies {
    val kotlinForForgeVersion = "4.11.0"
    val mixinExtraVersion = "0.4.1"
    val mixinVersion = "0.8.5"

    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtraVersion")!!)
    implementation("io.github.llamalad7:mixinextras-forge:$mixinExtraVersion")
    annotationProcessor("org.spongepowered:mixin:$mixinVersion:processor")

    modImplementation("maven.modrinth:sophisticated-core:1.20.1-1.2.105.1230")
    modImplementation("maven.modrinth:sophisticated-backpacks:1.20.1-3.24.9.1391")

    modImplementation("maven.modrinth:mine-and-slash:6.3.7")
    modImplementation("curse.maven:library-of-exile-398780:7103648")
    modImplementation("curse.maven:playeranimator-658587:4587214")
    modImplementation("maven.modrinth:curios:5.14.1+1.20.1")
    modImplementation("curse.maven:dungeon-realm-1200770:7103646")
    modImplementation("maven.modrinth:the-harvest:1.1.1")

    modImplementation("com.tterrag.registrate:Registrate:MC1.20-1.3.3")
}

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
        required("sophisticated-core")
        required("sophisticated-backpacks")
        required("mine-and-slash-reloaded")
    }
    modrinthDepends {
        required("kotlin-for-forge")
        required("sophisticated-core")
        required("sophisticated-backpacks")
        required("mine-and-slash")
    }
}

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

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
