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
        public String token;
        public String pythonScriptPath = "C:\\Code\\pluginDemo\\src\\main\\resources\\scripts\\get_token.py"; // 默认路径
        public String pythonExePath = "C:\\Software\\Anaconda\\envs\\zz\\python.exe"; // 默认路径
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public static AuthConfig getInstance() {
        AuthConfig config = ApplicationManager.getApplication().getService(AuthConfig.class);

        config.ensureConfigExists();
        return config;
    }

    public void saveConfig() {
        ApplicationManager.getApplication().saveSettings();
    }

    public void ensureConfigExists() {
        if (myState.token == null) {
            myState.token = "";
        }
        if (myState.pythonScriptPath == null) {
            myState.pythonScriptPath = "script/get_token.py";
        }
        if (myState.pythonExePath == null) {
            myState.pythonExePath = "python";
        }
        saveConfig();
    }
}
