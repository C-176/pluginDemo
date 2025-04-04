package com.ryker.ones;

import cn.hutool.core.map.MapUtil;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
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

    // 分页状态
    public static boolean hasNextPage = true;
    public static String endCursor = "";
    public static int totalCount = 0;

    // 映射Map
    public static final Map<String, String> STATUS_MAP = MapUtil.newHashMap(true);

    static {
        STATUS_MAP.put("全部", "");
        STATUS_MAP.put("未开始", "to_do");
        STATUS_MAP.put("进行中", "in_progress");
        STATUS_MAP.put("已完成", "done");
    }


    public TaskToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.contentPanel = new SimpleToolWindowPanel(true);

        // 设置窗口图标
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Icons/Bell.ico"));
            if (icon != null && icon.getImage() != null) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(contentPanel);
                if (parentFrame != null) {
                    parentFrame.setIconImage(icon.getImage());
                }
            }
        } catch (Exception e) {
           System.err.println("无法加载图标: " + e.getMessage());

        }

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
        // 设置列宽比例
        taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumnModel columnModel = taskTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(15);  // 任务id列占15%
        columnModel.getColumn(1).setPreferredWidth(50);  // 任务名称列占50%
        columnModel.getColumn(2).setPreferredWidth(20);  // 状态列占20%
        columnModel.getColumn(3).setPreferredWidth(15);  // 负责人列占15%

        // 为表格添加鼠标事件监听器
        taskTable.addMouseListener(new MouseInputAdapter() {

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

        });



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
//         添加状态变化监听器
        statusFilter.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refreshData();
            }
        });

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        toolbar.add(searchField, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.1;
        toolbar.add(searchButton, gbc);

        // 组装界面
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(toolbar, BorderLayout.NORTH);
        JBScrollPane jbScrollPane = new JBScrollPane(taskTable);
        contentPanel.add(jbScrollPane, BorderLayout.CENTER);
        contentPanel.setMinimumSize(new Dimension(400, 200));

        // 添加滚动事件监听器
        jbScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar scrollBar = (JScrollBar) e.getSource();
            int extent = scrollBar.getModel().getExtent();
            int maximum = scrollBar.getModel().getMaximum();
            int value = scrollBar.getValue();

            // 判断是否滚动到底部并且hasNextPage为true
            if ((value + extent) == maximum && hasNextPage) {
                try {
                    JSONArray objects = new JSONArray();
                    JSONObject filterGroup = JSONUtil.createObj().set("assign_in", Collections.singletonList("${currentUser}"));
                    objects.add(filterGroup);
                    if (StrUtil.isNotBlank((String) statusFilter.getSelectedItem()) && !statusFilter.getSelectedItem().equals("全部")) {
                        filterGroup.set("statusCategory_in", Collections.singletonList(STATUS_MAP.get(statusFilter.getSelectedItem())));
                    }
                    List<TaskDTO> tasks = HttpClientUtil.queryTasks(searchField.getText(), objects, endCursor);
                    updateTable(tasks,true);
                } catch (Exception ex) {
                    Notifications.Bus.notify(
                            new Notification(
                                    "ONES_Notification", // 使用注册的通知组ID
                                    "请求失败",
                                    ex.getMessage(),
                                    NotificationType.ERROR
                            ),
                            project
                    );
                }
            }
        });


        // 初始加载数据
        refreshData();
    }

     // 添加分页状态重置方法
    private void resetPagination() {
        hasNextPage = true;
        endCursor = "";
        totalCount = 0;
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        Notifications.Bus.notify(
                new Notification(
                        "ONES_Notification",
                        "复制成功",
                        text,
                        NotificationType.INFORMATION
                ),
                project
        );
    }

    public void refreshData() {
        // 重置分页状态
        resetPagination();
        String searchText = searchField.getText().trim();

        String status = (String) statusFilter.getSelectedItem();

        try {
            // assign_in 负责人
            // statusCategory_in 状态
            // watchers_in 关注人
            JSONArray objects = new JSONArray();
            JSONObject filterGroup = JSONUtil.createObj().set("assign_in", Collections.singletonList("${currentUser}"));
            objects.add(filterGroup);
            if (StrUtil.isNotBlank(status) && !status.equals("全部")) {
                filterGroup.set("statusCategory_in", Collections.singletonList(STATUS_MAP.get(status)));
            }
            List<TaskDTO> tasks = HttpClientUtil.queryTasks(searchField.getText(), objects,null);
            updateTable(tasks,false);
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

    private void updateTable(List<TaskDTO> tasks,boolean firstPage) {
        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
        if(!firstPage) model.setRowCount(0); // 清空旧数据

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