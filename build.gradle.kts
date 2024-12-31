plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "cc.raynet"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "cc.raynet.worldsharing"
    addonInfo {
        namespace = "worldsharing"
        displayName = "World Sharing"
        author = "Raynet Team"
        description = "Play on your Singleplayer world with your friends!"
        minecraftVersion = "1.8<1.21.4"
        version = rootProject.version.toString()
    }

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
//                    devLogin = true
                }
            }

            val file = file("./game-runner/src/${this.sourceSetName}/resources/worldsharing-${versionId}.accesswidener")
            accessWidener.set(file)


        }

    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")
    group = rootProject.group
    version = rootProject.version
}