import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    // quic
    addonMavenDependency("tech.kwik:kwik:0.8.13")
    addonMavenDependency("tech.kwik:agent15:2.0")
    addonMavenDependency("at.favre.lib:hkdf:2.0.0")

    // asn.1
    addonMavenDependency("org.bouncycastle:bcprov-jdk18on:1.78.1")

    compileOnly("io.netty:netty-all:4.1.115.Final")

    addonMavenDependency("io.sentry:sentry:7.14.0")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}