package com.ryker.ones;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import javax.swing.*;

public class QueryAction extends AnAction {
    @Override
public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) return;

    ToolWindow toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("ONES Tasks");

    if (toolWindow != null) {
        toolWindow.show(() -> {
            Content content = toolWindow.getContentManager().getContent(0);
            if (content != null) {
                JComponent component = content.getComponent();
                if (component instanceof SimpleToolWindowPanel) {
                    // 创建一个 Key 实例
                    Key<TaskToolWindow> TASK_TOOL_WINDOW_KEY = Key.create("TaskToolWindowKey");
                    // 使用 Key 实例来获取 TaskToolWindow 实例
                    TaskToolWindow taskToolWindow = content.getUserData(TASK_TOOL_WINDOW_KEY);
                    if (taskToolWindow != null) {
                        taskToolWindow.refreshData();
                    }
                }
            }
        });
    }
}


    private void refreshData(JPanel panel) {
        // 这里可以添加刷新数据的逻辑
        System.out.println("Refreshing data...");
    }
}