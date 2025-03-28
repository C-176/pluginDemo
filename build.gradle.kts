plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.yourcompany"
version = "1.0.0"

repositories {
    // 阿里云镜像
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    // 华为云镜像
    maven { url = uri("https://repo.huaweicloud.com/repository/maven") }
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

intellij {
    localPath = "C:\\Software\\Toolbox\\IntelliJ IDEA Ultimate"
    type = "IU"
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("242.1234")
        untilBuild.set("242.*")
    }

    runIde {
//        ideDir.set(file("C:\\Software\\Toolbox\\IntelliJ IDEA Ultimate"))
    }
}