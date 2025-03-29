package com.ryker.ones.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import com.intellij.openapi.application.PathManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;


public class AuthUtil {

    private static final String SETTING_PATH = "ones_config.setting";
    private static final String TOKEN_KEY = "token";
    private static final String PYTHON_SCRIPT_PATH = "/script/get_token.py"; // 替换为实际的 Python 脚本路径


    private static final Setting SETTING = new Setting(
        PathManager.getOptionsPath() + "/ones_config.setting",
        true
    );

    public static CompletableFuture<String> getToken() {
        Setting setting = SETTING;
        String token = setting.getStr(TOKEN_KEY);
        if (StrUtil.isBlank(token)) {
            return executePythonScript()
                   .thenApply(result -> {
                        if (StrUtil.isNotBlank(result)) {
                            setting.set(TOKEN_KEY, result);
                            setting.store();
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
                ProcessBuilder processBuilder = new ProcessBuilder("python", PYTHON_SCRIPT_PATH);
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