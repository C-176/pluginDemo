package com.ryker.ones;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.ryker.ones.dto.TaskDTO;
import com.ryker.ones.util.HttpClientUtil;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TaskToolWindow {
    private final Project project;
    private final JPanel contentPanel;
    private final JBTable taskTable;
    private final JTextField searchField;
    private final JComboBox<String> statusFilter;

    // 映射Map
    public static final Map<String, String> STATUS_MAP = Map.of(
            "全部", "",
            "未开始", "to_do",
            "进行中", "in_progress",
            "已完成", "done"
    );
    private int hoveredRow = -1;
    private int hoveredCol = -1;

    public TaskToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.contentPanel = new SimpleToolWindowPanel(true);

        // 初始化表格
        String[] columns = {"任务id", "任务", "状态", "负责人"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑
            }
        };
        this.taskTable = new JBTable(model);
        taskTable.setAutoCreateRowSorter(true); // 启用排序

        // 为表格添加鼠标事件监听器
        taskTable.addMouseListener(new MouseInputAdapter() {


            @Override
            public void mouseEntered(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());
                if ((col == 0 || col == 1) && row != -1) {
                    hoveredRow = row;
                    hoveredCol = col;
                    taskTable.repaint(taskTable.getCellRect(row, col, false));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredRow != -1 && hoveredCol != -1) {
                    int oldRow = hoveredRow;
                    int oldCol = hoveredCol;
                    hoveredRow = -1;
                    hoveredCol = -1;
                    taskTable.repaint(taskTable.getCellRect(oldRow, oldCol, false));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());
                // 检查点击的是否是任务编码或任务名称列
                String key = String.valueOf(taskTable.getValueAt(row, 0));
                String name = String.valueOf(taskTable.getValueAt(row, 1));
                if (col == 0) {
                    String textToCopy = "func: #" + key + " " + name;
                    copyToClipboard(textToCopy);
                } else if (col == 1) {
                    String url = "http://ones.inspur.com/project/#/team/HvBrmPic/task/" + key.replace("task-", "");
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException ex) {
                        Notifications.Bus.notify(
                                new Notification(
                                        "ONES_Notification",
                                        "打开网页失败",
                                        ex.getMessage(),
                                        NotificationType.ERROR
                                ),
                                project
                        );
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());
                if ((col == 0 || col == 1) && row != -1) {
                    if (hoveredRow != row || hoveredCol != col) {
                        if (hoveredRow != -1 && hoveredCol != -1) {
                            taskTable.repaint(taskTable.getCellRect(hoveredRow, hoveredCol, false));
                        }
                        hoveredRow = row;
                        hoveredCol = col;
                        taskTable.repaint(taskTable.getCellRect(row, col, false));
                    }
                } else {
                    if (hoveredRow != -1 && hoveredCol != -1) {
                        taskTable.repaint(taskTable.getCellRect(hoveredRow, hoveredCol, false));
                        hoveredRow = -1;
                        hoveredCol = -1;
                    }
                }
            }
        });

//        // 自定义表格渲染器，实现下划线效果
//        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                if ((column == 0 || column == 1) && ((MouseInputAdapter) taskTable.getMouseListeners()[0]).hoveredRow == row && ((MouseInputAdapter) taskTable.getMouseListeners()[0]).hoveredCol == column) {
//                    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLUE));
//                } else {
//                    setBorder(null);
//                }
//                return c;
//            }
//        });

        // 初始化搜索组件
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        this.searchField = new JTextField();

        this.statusFilter = new JComboBox<>(STATUS_MAP.keySet().toArray(new String[0]));

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> refreshData());

        // 构建工具栏
        JPanel toolbar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(2);

        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        toolbar.add(new JLabel("状态:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        toolbar.add(statusFilter, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        toolbar.add(searchField, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.1;
        toolbar.add(searchButton, gbc);

        // 组装界面
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(toolbar, BorderLayout.NORTH);
        contentPanel.add(new JBScrollPane(taskTable), BorderLayout.CENTER);

        // 初始加载数据
        refreshData();
    }


    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        Notifications.Bus.notify(
                new Notification(
                        "ONES_Notification",
                        "复制成功",
                        "已复制内容: " + text,
                        NotificationType.INFORMATION
                ),
                project
        );
    }

    public void refreshData() {
        String searchText = searchField.getText().trim();

        String status = (String) statusFilter.getSelectedItem();

        try {
            JSONArray objects = new JSONArray();
            JSONObject filterGroup = JSONUtil.createObj().set("watchers_in", Collections.singletonList("${currentUser}"));
            objects.add(filterGroup);
            if (StrUtil.isNotBlank(status) && !status.equals("全部")) {
                filterGroup.set("statusCategory_in", Collections.singletonList(STATUS_MAP.get(status)));

            }
            List<TaskDTO> tasks = HttpClientUtil.queryTasks(searchField.getText(), objects);
            updateTable(tasks);
        } catch (Exception e) {
            Notifications.Bus.notify(
                    new Notification(
                            "ONES_Notification", // 使用注册的通知组ID
                            "请求失败",
                            e.getMessage(),
                            NotificationType.ERROR
                    ),
                    project
            );
        }
    }

//    private void handleTaskSelection(String key, String name) {
//        // 暂存代码
//        stashChanges();
//
//        // 新建分支
//        createNewBranch(key, name);
//    }
//
//    private void stashChanges() {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            AnAction stashAction = new AnAction() {
//                @Override
//                public void actionPerformed(@NotNull AnActionEvent e) {
//                    Project project = e.getData(PlatformDataKeys.PROJECT);
//                    if (project != null) {
//                        // 执行暂存操作
//                        VcsUtil.runVcsProcessWithProgress(() -> {
//                            // 这里可以使用 ChangeListManager 进行暂存操作
//                            // 示例代码仅为示意，实际使用时需要根据具体情况进行调整
//                            // ChangeListManager changeListManager = ChangeListManager.getInstance(project);
//                            // changeListManager.stashChanges();
//                        }, "Stashing changes", false, project);
//                    }
//                }
//            };
//            AnActionEvent event = AnActionEvent.createFromDataContext(
//                    ToolWindowManager.getInstance(project).getDataContext(),
//                    null,
//                    null
//            );
//            stashAction.actionPerformed(event);
//        });
//    }
//
//    private void createNewBranch(String key, String name) {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
//            List<GitRepository> repositories = repositoryManager.getRepositories();
//            if (!repositories.isEmpty()) {
//                GitRepository repository = repositories.get(0);
//                String branchName = "task-" + key;
//                GitNewBranchParams params = new GitNewBranchParams(branchName, true);
//                GitUtil.createNewBranch(repository, params);
//            }
//        });
//    }
//}

    private void updateTable(List<TaskDTO> tasks) {
        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
        model.setRowCount(0); // 清空旧数据

        for (TaskDTO task : tasks) {
            model.addRow(new Object[]{
//                    task.getKey(),
                    task.getNumber(),
                    task.getName(),
                    task.getStatus().getName(),
                    task.getImportantField().stream()
                            .filter(field -> "负责人".equals(field.getName()))
                            .findFirst()
                            .map(TaskDTO.Important::getValue).get()
            });
        }

        // 自动调整列宽
        taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public JComponent getContent() {
        return contentPanel;
    }
}