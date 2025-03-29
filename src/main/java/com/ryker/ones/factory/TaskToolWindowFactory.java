package com.ryker.ones.factory;

// TaskToolWindowFactory.java

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.ryker.ones.TaskToolWindow;
import org.jetbrains.annotations.NotNull;

public class TaskToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        TaskToolWindow window = new TaskToolWindow(project, toolWindow);
        toolWindow.getComponent().add(window.getContent());
    }
}