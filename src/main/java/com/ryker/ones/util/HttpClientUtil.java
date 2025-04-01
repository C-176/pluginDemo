package com.ryker.ones.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.ryker.ones.dto.TaskDTO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HttpClientUtil {
    private static final String BASE_URL = "http://ones.inspur.com/project/api/project/team/HvBrmPic/items/graphql";
    private static final int MAX_RETRIES = 3;
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 30000; // 30 seconds

    public static final String SEARCH_PARAM = "search: $search\n    \n";
    private static final String YOUR_GRAPHQL_QUERY_STRING = "{\n    buckets (\n      groupBy: $groupBy\n      orderBy: $groupOrderBy\n      pagination: $pagination\n      filter: $groupFilter\n    ) {\n      key\n      \n      tasks (\n        filterGroup: $filterGroup\n        orderBy: $orderBy\n        limit: 2000\n        \n      includeAncestors:{pathField:\"path\"}\n      orderByPath: \"path\"\n    \n      search: $search\n    \n       ) {\n        \n    key\n    name\n    uuid\n    serverUpdateStamp\n    number\n    path\n    subTaskCount\n    subTaskDoneCount\n    position\n    status {\n      uuid\n      name\n      category\n    }\n    deadline(unit: ONESDATE)\n    subTasks {\n      uuid\n    }\n    issueType {\n      uuid\n      manhourStatisticMode\n    }\n    subIssueType {\n      uuid\n      manhourStatisticMode\n    }\n    project {\n      uuid\n    }\n    parent {\n      uuid\n    }\n    estimatedHours\n    remainingManhour\n    totalEstimatedHours\n    totalRemainingHours\n    issueTypeScope {\n      uuid\n    }\n\n        \n      importantField{\n        bgColor\n        color\n        name\n        value\n        fieldUUID\n      }\n      issueTypeScope {\n        uuid\n        currentLayout {\n          uuid\n          hasViewManhourTab\n        }\n      }\n    \n      }\n      pageInfo {\n        count\n        totalCount\n        startPos\n        startCursor\n        endPos\n        endCursor\n        hasNextPage\n        preciseCount\n      }\n    }\n    __extensions\n  }";

    public static List<TaskDTO> queryTasks(String searchKey, JSONArray filterGroup) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                // 获取Token（自动检查缓存或调用Python脚本）
                String token = AuthUtil.getToken().get();
                if (token != null) {
                    System.out.println("Token: " + token);
                } else {
                    Notifications.Bus.notify(
                            new Notification("ONES_Notification", "Token获取失败",
                                    "请检查配置文件或重新登录", NotificationType.ERROR));

                    System.out.println("Failed to get token.");
                }

                JSONObject variables = JSONUtil.createObj(new JSONConfig().setIgnoreError(true).setIgnoreNullValue(false))
                        .set("groupBy", JSONUtil.createObj().set("tasks", JSONUtil.createObj()))
                        .set("orderBy", JSONUtil.createObj()
                                .set("position", "ASC")
                                .set("createTime", "DESC"))
                        .set("filterGroup", filterGroup)
                        .set("search", StrUtil.isBlank(searchKey) ? null : JSONUtil.createObj().set("aliases", Collections.EMPTY_LIST).set("keyword", searchKey))
                        .set("pagination", JSONUtil.createObj()
                                .set("limit", 50)
                                .set("preciseCount", false));

                String result = HttpRequest.post(BASE_URL + "?t=group-task-data")
                        .setConnectionTimeout(CONNECT_TIMEOUT)  // 新增连接超时设置
                        .timeout(READ_TIMEOUT)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .header("referer", "http://ones.inspur.com/project/")
                        .body(JSONUtil.createObj()
                                .set("query", StrUtil.isBlank(searchKey) ? YOUR_GRAPHQL_QUERY_STRING.replace(SEARCH_PARAM, "") : YOUR_GRAPHQL_QUERY_STRING)
                                .set("variables", variables)
                                .toString())
                        .execute()
                        .body();
                return parseResult(result);
            } catch (Exception e) {
                retryCount++;
                System.err.println("请求失败，重试次数：" + retryCount + "，错误信息：" + e.getMessage());
                if (retryCount >= MAX_RETRIES) {
                    Notifications.Bus.notify(
                            new Notification("ONES_Notification", "请求失败",
                                    "重试次数已达上限：" + e.getMessage(), NotificationType.ERROR));
                    AuthUtil.getConfig().clearToken();
                    return Collections.emptyList();
                }
                try {
                    Thread.sleep(1000 * retryCount * retryCount); // 修改为平方退避策略
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<TaskDTO> parseResult(String json) {

        JSONObject entries = JSONUtil.parseObj(json);
        if (entries != null && entries.get("errcode") != null) {
            if ("401".equals(entries.get("code").toString())) {
                AuthUtil.getConfig().getState().setToken("");
                AuthUtil.getConfig().saveConfig();
                Notifications.Bus.notify(
                        new Notification("ONES_Notification", "Token过期",
                                "请重新登录", NotificationType.ERROR));
                return Collections.emptyList();
            }
            throw new RuntimeException(entries.getOrDefault("reason", "请求失败").toString());
        }
        return entries
                .getJSONObject("data")
                .getJSONArray("buckets")
                .getJSONObject(0)
                .getJSONArray("tasks")
                .toList(TaskDTO.class);
    }
}