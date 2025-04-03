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
        private String pythonScriptPath = "";
        private String pythonExePath = "";
        private String name = "";
        private String pwd = "";

        private String scriptArgs = "--no_log";

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        // 添加构造函数，确保所有字段都被初始化
        public State() {
            this.token = "";
            this.pythonScriptPath = "";
            this.pythonExePath = "";
            this.name = "";
            this.pwd = "";
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
        if (myState.token == null) {
            myState.token = "";
        }
        if (myState.pythonScriptPath == null) {
            myState.pythonScriptPath = "";
        }
        if (myState.pythonExePath == null) {
            myState.pythonExePath = "";
        }
        if (myState.name == null) {
            myState.name = "";
        }
        if (myState.pwd == null) {
            myState.pwd = "";
        }

        saveConfig();
    }
}
