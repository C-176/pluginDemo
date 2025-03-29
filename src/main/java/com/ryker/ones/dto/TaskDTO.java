package com.ryker.ones.dto;


import java.util.List;

public class TaskDTO {
    private String key;
    private String name;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    private Integer number;
    private Status status;


    public static class Status {
        private String name;
        private String category;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    public List<Important> getImportantField() {
        return importantField;
    }

    public void setImportantField(List<Important> importantField) {
        this.importantField = importantField;
    }

    // "importantField":[{"bgColor":"#e0ecfb","color":"#307fe2","fieldUUID":"field012","name":"优先级","value":"普通"},{"bgColor":"","color":"","fieldUUID":"field004","name":"负责人","value":"陈乐"}]
    private List<Important> importantField;

    public static class Important {
        // 只要负责人名字
        private String name;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBgColor() {
            return bgColor;
        }

        public void setBgColor(String bgColor) {
            this.bgColor = bgColor;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String value;
        public String bgColor;
        public String color;
    }
}