import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("org.bouncycastle:bcprov-jdk18on:1.78.1")
    addonMavenDependency("io.sentry:sentry:7.14.0")

    compileOnly("io.netty:netty-all:4.1.115.Final")
    addonMavenDependency("io.netty.incubator:netty-incubator-codec-classes-quic:0.0.74.Final.natives") {
        exclude("io.netty")
    }

    addonMavenDependency("tech.kwik:kwik:0.10.3")
    addonMavenDependency("tech.kwik:agent15:2.0")
    addonMavenDependency("at.favre.lib:hkdf:2.0.0")
}

repositories {
    maven("https://maven.mxha.de")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}
