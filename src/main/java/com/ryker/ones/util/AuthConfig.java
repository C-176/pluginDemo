package com.ryker.ones.util;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
@State(
        name = "AuthConfig",
        storages = @Storage("ones_config.xml")
)
public final class AuthConfig implements PersistentStateComponent<AuthConfig.State> {

    private State myState = new State();

    public static class State {
        private String token = "";
        private String pythonScriptPath = "D:\\code\\work\\pluginDemo\\src\\main\\resources\\scripts\\get_token.py";
        private String pythonExePath = "D:\\code\\selnium\\.venv\\Scripts\\python.exe";
        private String scriptArgs = "--no_log";

        // 添加构造函数，确保所有字段都被初始化
        public State() {
            this.token = "";
            this.pythonScriptPath = "D:\\code\\work\\pluginDemo\\src\\main\\resources\\scripts\\get_token.py";
            this.pythonExePath = "D:\\code\\selnium\\.venv\\Scripts\\python.exe";
            this.scriptArgs = "--no_log";
        }

        // Getter/Setter 方法
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getPythonScriptPath() {
            return pythonScriptPath;
        }

        public void setPythonScriptPath(String pythonScriptPath) {
            this.pythonScriptPath = pythonScriptPath;
        }

        public String getPythonExePath() {
            return pythonExePath;
        }

        public void setPythonExePath(String pythonExePath) {
            this.pythonExePath = pythonExePath;
        }

        public String getScriptArgs() {
            return scriptArgs;
        }

        public void setScriptArgs(String scriptArgs) {
            this.scriptArgs = scriptArgs;
        }
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        saveConfig();  // 加载状态后立即持久化
    }

    public static AuthConfig getInstance() {
        AuthConfig config = ApplicationManager.getApplication().getService(AuthConfig.class);
        config.ensureConfigExists();
        return config;
    }

    // 添加新的 get/set 方法
    public String getToken() {
        return myState.getToken();
    }

    public void setToken(String token) {
        myState.setToken(token);
        saveConfig();
    }

    public String getPythonScriptPath() {
        return myState.getPythonScriptPath();
    }

    public void setPythonScriptPath(String pythonScriptPath) {
        myState.setPythonScriptPath(pythonScriptPath);
        saveConfig();
    }

    public String getPythonExePath() {
        return myState.getPythonExePath();
    }

    public void setPythonExePath(String pythonExePath) {
        myState.setPythonExePath(pythonExePath);
        saveConfig();
    }

    public String getScriptArgs() {
        return myState.getScriptArgs();
    }

    public void setScriptArgs(String scriptArgs) {
        myState.setScriptArgs(scriptArgs);
        saveConfig();
    }

    public void saveConfig() {
        ApplicationManager.getApplication().saveSettings();
    }

    public void clearToken() {
        myState.setToken("");
        saveConfig();
    }

    public void ensureConfigExists() {
        boolean modified = false;
        if (myState.token == null) {
            myState.token = "";
            modified = true;
        }
        if (myState.pythonScriptPath == null) {
            myState.pythonScriptPath = "script/get_token.py";
            modified = true;
        }
        if (myState.pythonExePath == null) {
            myState.pythonExePath = "python";
            modified = true;
        }
        if (myState.scriptArgs == null) {
        if (modified) {
            saveConfig();
        }
            modified = true;
            myState.scriptArgs = "";
        }
        saveConfig();
    }
}
