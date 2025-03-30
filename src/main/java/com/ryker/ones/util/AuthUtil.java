package com.ryker.ones.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.Messages;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthUtil {
    private static final String LOGIN_URL = "http://ones.inspur.com/auth/third_party/login/ldap?bindedOrgUUID=MfXXggQm&redirect_uri=http%3A%2F%2Fones.inspur.com%2Fauth%2Fthird_party%2Fresult%3FbindedOrgUUID%3DMfXXggQm%26lang%3Dzh%26ones_from%3D%26scene%3D%26third_party_type%3D3%26third_party_value%3Dldap";
    public static AuthConfig config;

    static {
        config = AuthConfig.getInstance();
    }

    public static CompletableFuture<String> getToken() {

        String token = config.getState().token;
        if (StrUtil.isBlank(token)) {
            return executePythonScript()
                    .thenApply(result -> {
                        if (StrUtil.isNotBlank(result)) {
                            config.getState().token = result;
                            config.saveConfig();
                            return result;
                        }
                        return null;
                    });
        }
        return CompletableFuture.completedFuture(token);
    }

    private static CompletableFuture<String> executePythonScript() {
        return CompletableFuture.supplyAsync(() -> {
            try {

                ProcessBuilder processBuilder = new ProcessBuilder(config.getState().pythonExePath, config.getState().pythonScriptPath);
                // 获取脚本的父目录
                File scriptFile = new File(config.getState().pythonScriptPath);
                processBuilder.directory(scriptFile.getParentFile());
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return output.toString();
                } else {
                    System.err.println("Python script execution failed with exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });
    }


}