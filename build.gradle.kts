plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.ryker"
version = "1.0.0"

repositories {
    // 阿里云镜像（主镜像源）
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    // 华为云镜像（备用）
    maven { url = uri("https://repo.huaweicloud.com/repository/maven") }

    // 腾讯云镜像（特殊依赖）
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public") }

    // 配置优先级：先检查上述镜像，最后回退到原始仓库
    mavenCentral()
    google()  // 如果需要 Android 相关依赖
}
java {
}

// 强制指定编译任务 JDK 路径（兼容旧版本）
tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.forkOptions.javaHome =
        file("C:\\Software\\jdk\\OpenJDK17U-jdk_x64_windows_hotspot_17.0.14_7\\jdk-17.0.14+7")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
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
        sinceBuild.set("241.1234")
        untilBuild.set("241.*")
    }


//    runIde {
////        ideDir.set(file("C:\\Software\\Toolbox\\IntelliJ IDEA Ultimate"))
//    }
}