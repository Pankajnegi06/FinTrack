package com.fintrack.model;

public enum Category {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    EDUCATION("Education"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Category fromString(String text) {
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.label.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}
