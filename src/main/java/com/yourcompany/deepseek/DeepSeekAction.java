package com.yourcompany.deepseek;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

public class DeepSeekAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getProject();
        if (editor == null || project == null) return;

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            Messages.showErrorDialog("请先选择代码", "错误");
            return;
        }


        new Thread(() -> {
            try {
                String modifiedCode = callDeepSeekApi(selectedText, "优化这段代码");
                WriteCommandAction.runWriteCommandAction(project, () ->
                        editor.getDocument().replaceString(
                                editor.getSelectionModel().getSelectionStart(),
                                editor.getSelectionModel().getSelectionEnd(),
                                modifiedCode
                        )
                );
            } catch (Exception ex) {
                Messages.showErrorDialog("API 调用失败: " + ex.getMessage(), "错误");
            }
        }).start();
    }

    private String callDeepSeekApi(String code, String instruction) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        String jsonBody = "{ \"code\": " + GsonUtil.toJson(code) + ", \"instruction\": \"" + instruction + "\" }";
        RequestBody body = RequestBody.create(jsonBody, JSON);


        Request request = new Request.Builder()
                .url("https://api.deepseek.com/v1/code")
                .header("Authorization", "Bearer " + System.getenv("DEEPSEEK_API_KEY"))
                .post(body)
                .build();


//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("HTTP 错误: " + response.code());
//            return GsonUtil.fromJson(response.body().string(), DeepSeekResponse.class).modifiedCode;
//        }
        return "xxxxxxxxxxxx";
    }


    private static class DeepSeekResponse {
        public String modifiedCode;
    }


    private static class GsonUtil {
        private static final com.google.gson.Gson gson = new com.google.gson.Gson();

        public static String toJson(Object obj) {
            return gson.toJson(obj);
        }

        public static <T> T fromJson(String json, Class<T> classOfT) {
            return gson.fromJson(json, classOfT);
        }
    }
}