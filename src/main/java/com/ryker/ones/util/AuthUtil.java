package com.ryker.ones.util;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.ui.Messages;

import java.awt.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class AuthUtil {
    private static final String LOGIN_URL = "http://ones.inspur.com/auth/third_party/login/ldap?bindedOrgUUID=MfXXggQm&redirect_uri=http%3A%2F%2Fones.inspur.com%2Fauth%2Fthird_party%2Fresult%3FbindedOrgUUID%3DMfXXggQm%26lang%3Dzh%26ones_from%3D%26scene%3D%26third_party_type%3D3%26third_party_value%3Dldap";
    private static AuthConfig config;

    public  static AuthConfig getConfig() {
        if (config == null) {
            config = AuthConfig.getInstance();
        }
        return config;
    }

    public static CompletableFuture<String> getToken() {
        String token = getConfig().getState().getToken();
        if (StrUtil.isBlank(token)) {
            // 提示用户是否要登录
            int result = Messages.showYesNoDialog("Token 已失效，是否立即登录？", "登录提示", Messages.getQuestionIcon());
            if (result == Messages.YES) {
                return executePythonScript()
                        .thenApply(x -> {
                            if (StrUtil.isNotBlank(x)) {
                                config.getState().setToken(x);
                                config.saveConfig();
                                return x;
                            }
                            return null;
                        });
            } else {
                return CompletableFuture.completedFuture(null);
            }
        }
        return CompletableFuture.completedFuture(token);
    }

    private static CompletableFuture<String> executePythonScript() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String pythonExePath = getConfig().getState().getPythonExePath();
                String pythonScriptPath = getConfig().getState().getPythonScriptPath();
                String scriptArgs = getConfig().getState().getScriptArgs(); // 获取脚本参数

                // 获取脚本所在目录
                File scriptFile = new File(pythonScriptPath);
                String workingDir = scriptFile.getParentFile().toString();
                String[] cmds = new String[]{"cmd", "/c", "cd", workingDir, "&&", pythonExePath, pythonScriptPath, scriptArgs};
                // 使用 Hutool 执行 Python 脚本，并指定工作目录和参数
                String s = RuntimeUtil.execForStr(cmds);
                // 去除返回结果中的换行符和空格
                String result = s.replaceAll("\\s+", "");
                // 去除参数list，是包含在[]中的参数
                String[] split = result.split("\\[");
                if (split.length > 1) {
                    return split[1].replace("]", "");
                }
                return result;

            } catch (Exception e) {
                return null;
            }
        });
    }

}