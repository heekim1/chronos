package com.roche.rss.lannister.domain;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The mapper of task defined in config-file
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private String action;
    private String source;
    private String target;
    private String type = "f";
    private String [] name;
    private String cronSchedule;
    private String retention;
    private String maxRetention;
    private Boolean softLink = false;
    private String maxDepth;
    private String minDepth;
    private Boolean emptyOnly = false;
    private Boolean mirroring = true;
    private Boolean filtering = true;
    private String comment;
    private String expectedExecutionTime = "-1";
    private String listOfRecipients="";
    private String taskDescriptionForEmailNotification="";
    private Boolean verbose = false;
    public Task(){
    }

    public Task(String action, String source, String target, String type, String[] name, String cronSchedule, String retention) {
        this.action = action;
        this.source = source;
        this.target = target;
        this.type = type;
        this.name = name;
        this.cronSchedule = cronSchedule;
        this.retention = retention;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public String getRetention() {
        return retention;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    public String getMaxRetention() { return maxRetention; }

    public Boolean getVerbose() { return verbose;  }
    public void setVerbose(boolean verbose) { this.verbose = verbose;  }

    public void setMaxRetention(String maxRetention) {
        this.maxRetention = maxRetention;
    }

    public Boolean getSoftLink() { return softLink; }

    public void setSoftLink(Boolean softLink) { this.softLink = softLink; }

    public String getMaxDepth() { return maxDepth; }

    public void setMaxDepth(String maxDepth) { this.maxDepth = maxDepth; }

    public String getMinDepth() { return minDepth; }

    public void setMinDepth(String minDepth) { this.minDepth = minDepth; }

    public Boolean getEmptyOnly() { return emptyOnly; }

    public void setEmptyOnly(Boolean emptyOnly) { this.emptyOnly = emptyOnly; }

    public Boolean getMirroring() { return mirroring; }

    public void setMirroring(Boolean mirroring) { this.mirroring = mirroring; }

    public Boolean getFiltering() { return filtering; }

    public void setFiltering(Boolean filtering) { this.filtering = filtering; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public int getExpectedExecutionTime() { return Integer.valueOf(expectedExecutionTime); }

    public void setExpectedExecutionTime(String expectedExecutionTime) { this.expectedExecutionTime = expectedExecutionTime; }

    public String gettaskDescriptionForEmailNotification() { return taskDescriptionForEmailNotification; }

    public void setTaskDescriptionForEmailNotification(String taskDescriptionForEmailNotification) { this.taskDescriptionForEmailNotification = taskDescriptionForEmailNotification; }

    public String getListOfRecipients() { return listOfRecipients.replace(", "," ").replace(","," "); }

    public void setListOfRecipients(String listOfRecipients) { this.listOfRecipients = listOfRecipients; }

    @Override
    public String toString() {
        return "Task{" +
                "type='" + action + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", fileType='" + type + '\'' +
                ", name=" + Arrays.toString(name) +
                ", cronSchedule='" + cronSchedule + '\'' +
                ", retention='" + retention + '\'' +
                ", maxRetention='" + maxRetention + '\'' +
                ", expectedExecutionTime='" + expectedExecutionTime + '\'' +
                ", softLink='" + softLink + '\'' +
                ", maxDepth='" + maxDepth + '\'' +
                ", minDepth='" + minDepth + '\'' +
                ", emptyOnly='" + emptyOnly + '\'' +
                ", mirroring='" + mirroring + '\'' +
                ", filtering='" + filtering + '\'' +
                ", comment='" + comment + '\'' +
                ", verbose='" + verbose + '\'' +
                '}';
    }
}
