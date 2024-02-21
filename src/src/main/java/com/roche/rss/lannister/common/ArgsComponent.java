package com.roche.rss.lannister.common;

import org.springframework.boot.ApplicationArguments;

public class ArgsComponent {

    private ApplicationArguments args;

    private String templatedConfigPath;

    public ArgsComponent(ApplicationArguments args) {
        this.args = args;
    }

    public ArgsComponent(ApplicationArguments args, String templatedConfigPath) {
        this.args = args;
        this.templatedConfigPath = templatedConfigPath;
    }

    public ApplicationArguments getArgs() {
        return args;
    }

    public String getTemplatedConfigPath() {
        return templatedConfigPath;
    }

    public void setTemplatedConfigPath(String templatedConfigPath) {
        this.templatedConfigPath = templatedConfigPath;
    }
}