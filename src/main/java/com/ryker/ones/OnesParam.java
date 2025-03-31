package com.ryker.ones;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @功能名称: TODO
 * @功能描述: TODO
 * @作者 Ryker
 * @创建时间 2025/3/30 下午8:59
 */
public class OnesParam {
    private JTextField name;
    private JTextField pwd;
    private long lastFocusTime = 0; // 添加最后焦点时间戳
    private static final long DEBOUNCE_TIME = 3000; // 防抖时间100ms

    private JPanel createFileChooserField(JTextField textField, String title, String fileExtension) {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建选择文件按钮
        JButton button = new JButton("上传");
        button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
            descriptor.setTitle(title);
            VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, null, null);
            if (virtualFiles != null) {
                for (VirtualFile virtualFile : virtualFiles) {
                    if (virtualFile.getPath().endsWith(fileExtension)) {
                        textField.setText(virtualFile.getPath());
                    }
                }
            }
        });

        // 添加输入框和按钮
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    public OnesParam() {
        // 确保所有组件都已初始化
        if (mainPanel == null) {
            mainPanel = new JPanel();
            // 设置布局管理器
            mainPanel.setLayout(new GridLayoutManager(4, 2));
        }

        // 确保所有组件都已添加到面板
        if (pythonScriptPath != null) {
            mainPanel.add(createFileChooserField(pythonScriptPath, "选择 Python 脚本", ".py"));
        }
        if (pythonExePath != null) {
            mainPanel.add(createFileChooserField(pythonExePath, "选择 Python 解释器", ".exe"));
        }

        pythonScriptPath.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {

                long currentTime = System.currentTimeMillis();
                System.out.println(currentTime - lastFocusTime + "," + DEBOUNCE_TIME);
                if (currentTime - lastFocusTime < DEBOUNCE_TIME) {
                    name.requestFocusInWindow();
                    return; // 如果时间间隔小于100ms，直接返回
                }


                super.focusGained(e);
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
                descriptor.setTitle("选择 Python 脚本");
                VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, null, null);
                lastFocusTime = currentTime; // 更新最后焦点时间
                name.requestFocusInWindow();
                if (virtualFiles != null) {
                    for (VirtualFile virtualFile : virtualFiles) {
                        if (virtualFile.getPath().endsWith(".py")) {
                            pythonScriptPath.setText(virtualFile.getPath());
                        }
                    }
                }
            }
        });

        pythonExePath.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                long currentTime = System.currentTimeMillis();
                System.out.println(currentTime - lastFocusTime + "," + DEBOUNCE_TIME);
                if (currentTime - lastFocusTime < DEBOUNCE_TIME) {
                    name.requestFocusInWindow();
                    return; // 如果时间间隔小于100ms，直接返回
                }
                super.focusGained(e);
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
                descriptor.setTitle("选择 Python 解释器");
                VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, null, null);
                lastFocusTime = currentTime; // 更新最后焦点时间
                name.requestFocusInWindow();
                if (virtualFiles != null) {
                    for (VirtualFile virtualFile : virtualFiles) {
                        if (virtualFile.getPath().endsWith(".exe")) {
                            pythonExePath.setText(virtualFile.getPath());
                        }
                    }
                }
            }
        });
    }

    public JTextField getName() {
        return name;
    }

    public JTextField getPwd() {
        return pwd;
    }

    public void setName(JTextField name) {
        this.name = name;
    }

    public void setPwd(JTextField pwd) {
        this.pwd = pwd;
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel mainPanel;

    public JTextField getPythonScriptPath() {
        return pythonScriptPath;
    }

    public void setPythonScriptPath(JTextField pythonScriptPath) {
        this.pythonScriptPath = pythonScriptPath;
    }

    public JTextField getPythonExePath() {
        return pythonExePath;
    }

    public void setPythonExePath(JTextField pythonExePath) {
        this.pythonExePath = pythonExePath;
    }

    private JTextField pythonScriptPath;
    private JTextField pythonExePath;
}



