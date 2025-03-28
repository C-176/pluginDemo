package com.ryker.project;

import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 模拟任务类
class Task {
    String status;
    String assignee;
    String title;

    public Task(String status, String assignee, String title) {
        this.status = status;
        this.assignee = assignee;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Title: " + title + ", Status: " + status + ", Assignee: " + assignee;
    }
}

public class OnesQueryUI extends JFrame {
    private JComboBox<String> statusComboBox;
    private JComboBox<String> assigneeComboBox;
    private JButton queryButton;
    private JTextArea resultTextArea;

    private List<Task> tasks;
        private OnesQueryAction onesQueryAction;

    public OnesQueryUI() {
    try {
        onesQueryAction = new OnesQueryAction();
        initComponents();
        setTitle("ONES 任务查询");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        String[] statusOptions = {"All", "In Progress", "Completed", "Pending"};
        statusComboBox = new JComboBox<>(statusOptions);
        panel.add(new JLabel("状态: "));
        panel.add(statusComboBox);

        String[] assigneeOptions = {"All", "John Doe", "Jane Smith", "Bob Johnson"};
        assigneeComboBox = new JComboBox<>(assigneeOptions);
        panel.add(new JLabel("负责人: "));
        panel.add(assigneeComboBox);

        queryButton = new JButton("查询");
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行查询逻辑
            }
        });
        panel.add(queryButton);

        resultTextArea = new JTextArea(10, 50);
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

     private void performQuery() {
        String selectedStatus = (String) statusComboBox.getSelectedItem();
        String selectedAssignee = (String) assigneeComboBox.getSelectedItem();

        try {
            // 这里需要修改 OnesQueryAction 来支持筛选条件
            String response = onesQueryAction.callOnesApi();
            resultTextArea.setText(response);
        } catch (IOException ex) {
            Messages.showErrorDialog("查询失败: " + ex.getMessage(), "错误");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OnesQueryUI::new);
    }
}
