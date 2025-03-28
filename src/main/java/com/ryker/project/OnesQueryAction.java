package com.ryker.project;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class OnesQueryAction extends AnAction {

    private static final String ONES_API_URL = "http://ones.inspur.com/project/#/workspace/team/HvBrmPic/filter/view/ft-t-002/task/EiiEaJiCGMMIAftS";
    private static final String ONES_USERNAME = "chenle02-";
    private static final String ONES_PASSWORD = "Nevergiveup123456";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new Thread(() -> {
            try {
                String response = callOnesApi();
// 使用 SwingUtilities.invokeLater 在 EDT 中显示消息对话框
                SwingUtilities.invokeLater(() -> {
                    Messages.showInfoMessage(response, "ONES 查询结果");
                });
            } catch (IOException ex) {
// 使用 SwingUtilities.invokeLater 在 EDT 中显示错误对话框
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("查询失败: " + ex.getMessage(), "错误");
                });
            }
            SwingUtilities.invokeLater(OnesQueryUI::new);
        }).start();
    }


    public String callOnesApi() throws IOException {
//        OkHttpClient client = new OkHttpClient();
        return "query result";
//        Request request = new Request.Builder()
//               .url(ONES_API_URL)
//               .addHeader("Authorization", Credentials.basic(ONES_USERNAME, ONES_PASSWORD))
//               .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (response.isSuccessful() && response.body() != null) {
//                return response.body().string();
//            } else {
//                throw new IOException("Unexpected code " + response);
//            }
//        }
    }
}
