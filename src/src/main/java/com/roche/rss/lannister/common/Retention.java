package com.roche.rss.lannister.common;

public enum Retention {
    Unused("0"),
    Transient("+42"),
    Internal("+90"),
    Raw("+365"),
    Intermediate("+2555"),
    Report("+7300");

    private final String days;

    Retention(String days) {
        this.days = days;
    }

    public String getDays() {
        return days;
    }

}