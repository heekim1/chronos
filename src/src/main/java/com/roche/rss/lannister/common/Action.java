package com.roche.rss.lannister.common;

public enum Action {
    COPY("copy"),
    MOVE("move"),
    DELETE("delete"),
    UPDATE_LIST("updateList")
    ;

    private final String cmd;

    Action(String cmd) {
        this.cmd = cmd;
    }

    String getCmd(){
        return cmd;
    }
}
