package com.ryker.ones;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

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
        panel.add(new JLabel(title), BorderLayout.WEST);
        panel.add(new JTextField(), BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    public OnesParam() {
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


    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel mainPanel;

    public JTextField getPythonScriptPath() {
        return pythonScriptPath;
    }

    public JTextField getPythonExePath() {
        return pythonExePath;
    }

    private JTextField pythonScriptPath;
    private JTextField pythonExePath;
    private JTextPane a1如果需要脚本自动获取token的需要准备好python环境TextPane;
}



