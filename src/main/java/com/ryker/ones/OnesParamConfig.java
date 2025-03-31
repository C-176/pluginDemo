package com.ryker.ones;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.ryker.ones.util.AuthConfig;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @功能名称: TODO
 * @功能描述: TODO
 * @作者 Ryker
 * @创建时间 2025/3/31 下午8:18
 */
public class OnesParamConfig implements Configurable {
    private JComponent component;
    private final OnesParam OnesParam = new OnesParam();
    private final AuthConfig.State state;
    private final AuthConfig authConfig;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "OnesParam";
    }

    public OnesParamConfig() {
        authConfig = ApplicationManager.getApplication().getService(AuthConfig.class);
        state = authConfig.getState();
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (component == null) {
            component = OnesParam.getMainPanel();

        }
        return component;
    }

    @Override
    public boolean isModified() {
        return state.getName() != null && !state.getName().equals(OnesParam.getName().getText()) || state.getPwd() != null && !state.getPwd().equals(OnesParam.getPwd().getText()) || state.getPythonExePath() != null && !state.getPythonExePath().equals(OnesParam.getPythonExePath().getText()) || state.getPythonScriptPath() != null && !state.getPythonScriptPath().equals(OnesParam.getPythonScriptPath().getText());

    }

    @Override
    public void apply() throws ConfigurationException {
        state.setName(OnesParam.getName().getText());
        state.setPwd(OnesParam.getPwd().getText());
        state.setPythonExePath(OnesParam.getPythonExePath().getText());
        state.setPythonScriptPath(OnesParam.getPythonScriptPath().getText());
        authConfig.saveConfig();  // 添加这行代码来保存配置
    }

    public void reset() {
        OnesParam.getName().setText(state.getName());
        OnesParam.getPwd().setText(state.getPwd());
        OnesParam.getPythonExePath().setText(state.getPythonExePath());
        OnesParam.getPythonScriptPath().setText(state.getPythonScriptPath());
    }
}
