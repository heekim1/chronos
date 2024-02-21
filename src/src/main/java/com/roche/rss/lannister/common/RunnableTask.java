package com.roche.rss.lannister.common;

import com.roche.rss.lannister.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


/**
 * Create scheduled task.
 * Since RunnableTask implements Runnable,
 * the instance will be registered and executed by threadPoolTaskScheduler
 * in TaskSchedulerService along with cronTrigger or by executorService with --immediate.
 */
public class RunnableTask implements Runnable {
    private final Logger log = LoggerFactory.getLogger(RunnableTask.class);

    static private boolean isActionTaskIsRunning = false;
    static private String nameoftheRunningTask = "";
    static private String nameofFile = "";
    private String message;
    private Task task;
    private ArgsComponent args;
    private String listFile;

    /**
     * construct a runnable object
     *
     * @param message
     * @param task instance of Task from json object form config-file
     */
    public RunnableTask(String message, Task task) {
        this.message = message;
        this.task = task;
    }

    public RunnableTask(String message, Task task, ArgsComponent args) {
        this.message = message;
        this.task = task;
        this.args = args;
    }

    @Override
    /**
     * automatically executed by threadPoolTaskScheduler or executorService
     */
    public void run() {
        if(!task.getAction().equalsIgnoreCase("updateList")){
            nameoftheRunningTask = task.toString();
            isActionTaskIsRunning = true;
        }
        if(task.getAction().equalsIgnoreCase("move")) {
            log.info("Move task started");
            move();
            log.info("Move task finished");
        }else if(task.getAction().equalsIgnoreCase("delete")){
            delete();
        }else if(task.getAction().equalsIgnoreCase("copy")) {
            log.info("Copy task started.isRunning="+isActionTaskIsRunning);
            copy();
            log.info("Copy task finished. isRunning="+isActionTaskIsRunning);

        }else if(task.getAction().equalsIgnoreCase("updateList")) {
            if (!isActionTaskIsRunning) updateList(); else log.info(String.format("The task to update the list was skipped to prevent the file from being updated while another task was running. The running task: %s, filename: %s. ",nameoftheRunningTask,nameofFile));
        }
    }

    /**
     * add grep to get all in or not in a list of pattern
     * @return
     */
    private String getGrepFilter(){
        String filter = "";
        try {
            if (args.getArgs().containsOption("include-list")) {
                filter = String.format("| grep -F -f %s", args.getArgs().getOptionValues("include-list").get(0));
            } else if (args.getArgs().containsOption("exclude-list")) {
                filter = String.format("| grep -Fv -f %s", args.getArgs().getOptionValues("exclude-list").get(0));
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        return filter;
    }

    /**
     * add grep to get all in or not in a list of pattern
     * @return
     */
    private String getFilter(){
        try {
            if (args.getArgs().containsOption("include-list")) {
                return String.format("-a \\( %s \\)", getIncFilter());
            } else if (args.getArgs().containsOption("exclude-list")) {
                return String.format("%s", getExcFilter());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        return "";
    }

    /**
     *
     * @return
     */
    private String getIncFilter(){
        StringJoiner path = new StringJoiner(" -o ");
        try{
            File f = new File(args.getArgs().getOptionValues("include-list").get(0));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = br.readLine()) != null) {
                path.add(String.format("-path '*%s*'",readLine));
            }
        }catch (Exception e) {
            log.info("Exception while generating Include filter", e);
            System.exit(1);
        }

        if(path.toString() != null && !path.toString().trim().isEmpty()) {
            return path.toString();
        }else {
            // generate a random file name not to apply all of directories or files
            return String.format("-path '*%s*'", ThreadLocalRandom.current().nextInt());
        }
    }

    /**
     *
     * @return
     */
    private String getExcFilter(){
        StringJoiner path = new StringJoiner(" ");
        try{
            File f = new File(args.getArgs().getOptionValues("exclude-list").get(0));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = br.readLine()) != null) {
                path.add(String.format("! -path '*%s*'",readLine));
            }
        }catch (Exception e) {
            log.info("Exception while generating Exclude filter", e);
            System.exit(1);
        }

        return path.toString();
    }

    /**
     *
     */
    private void updateList(){

        StringJoiner name = new StringJoiner(" -o ");
        for(String fileName: task.getName()) {
            name.add(String.format("-name '%s'",fileName));
        }
        if (args.getArgs().containsOption("include-list")) {
            listFile = args.getArgs().getOptionValues("include-list").get(0);
        }else if (args.getArgs().containsOption("exclude-list")) {
            listFile = args.getArgs().getOptionValues("exclude-list").get(0);
        }else {
            log.info("Either --include-list or --exclude-list should be defined.");
            System.exit(1);
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "-c",
                String.format("(cd %s && find . %s %s \\( %s \\) %s -mmin +%s %s -exec sh -c 'basename `dirname {}`'  \\; > %s)",
                        task.getSource(),
                        (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                        (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                        name.toString(),
                        (task.getType() != null ? "-type " + task.getType() : ""),
                        convert2min(task.getRetention()),
                        (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                        listFile
                        ));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        startAndCheckProcess(processBuilder);
    }


    /**
     * perform move which runs mv in shell
     */
    private void move() {
        for(String fileName: task.getName()) {
            nameofFile = fileName.toString();
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(task.getMirroring()) {
                processBuilder.command("/bin/bash", "-c",
                        String.format("(cd %s && find . -depth %s %s \\( -name '%s' %s \\) %s -mmin +%s %s -exec sh -c 'mkdir -p `dirname %s/{}`' \\; -exec sh -c 'mv -n %s%s/{} `dirname %s/{}`' \\; )",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                fileName,
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getType() != null ? "-type " + task.getType() : ""),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                                task.getTarget(),
                                (task.getVerbose() ? "-v " : ""),
                                task.getSource(),
                                task.getTarget()));
            }else{
                processBuilder.command("/bin/bash", "-c",
                        String.format("find %s/* %s %s \\( -name '%s' %s \\) %s -mmin +%s %s | xargs -n1 -I \'{}\' mv -n %s \'{}\' %s ",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                fileName,
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getType() != null ? "-type " + task.getType() : ""),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                                (task.getVerbose() ? "-v " : ""),
                                task.getTarget()));
            }
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            startAndCheckProcess(processBuilder);
        }
    }

    /**
     * perform rm in shell
     */
    private void delete() {
        for(String fileName: task.getName()) {
            nameofFile = fileName.toString();
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(task.getEmptyOnly()){
                processBuilder.command("/bin/bash", "-c",
                        String.format("(cd %s && find . %s %s \\( -empty %s \\) %s -mmin +%s %s -delete)",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getType() != null ? "-type " + task.getType() : ""),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : "")));

            }else {
                processBuilder.command("/bin/bash", "-c",
                        String.format("find %s/* %s %s \\( -name '%s' %s \\) %s -mmin +%s %s | xargs rm -rf%s ",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                fileName,
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getType() != null ? "-type " + task.getType() : ""),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                                (task.getVerbose() ? "v " : "")
                                ));
            }
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            startAndCheckProcess(processBuilder);
        }
    }

    /**
     * perform cp in shell
     * using -n, cp copies only if file not exists
     */
    private void copy() {
        for (String fileName : task.getName()) {
            nameofFile = fileName.toString();
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(task.getMirroring()) {
                processBuilder.command("/bin/bash", "-c",
                        String.format("(cd %s && find . %s %s \\( -name '%s' %s \\) %s -mmin +%s %s -exec sh -c 'mkdir -p `dirname %s/{}`' \\; -exec sh -c 'cp -n %s%s %s/{} `dirname %s/{}`' \\; )",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                fileName,
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getSoftLink() ? String.format("-type l -xtype %s", task.getType()) : String.format("-type %s", task.getType())),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                                task.getTarget(),
                                (task.getVerbose() ? "-v " : ""),
                                (task.getType().startsWith("d") ? "-r" : ""),
                                task.getSource(),
                                task.getTarget()));
            }else{
                processBuilder.command("/bin/bash", "-c",
                        String.format("find %s/* %s %s \\( -name '%s'  %s \\) %s -mmin +%s %s | xargs -n1 -I \'{}\' cp -n %s%s \'{}\' %s ",
                                task.getSource(),
                                (task.getMaxDepth() != null ? String.format("-maxdepth %s", task.getMaxDepth()) : ""),
                                (task.getMinDepth() != null ? String.format("-mindepth %s", task.getMinDepth()) : ""),
                                fileName,
                                (task.getFiltering() ? getFilter() : ""),
                                (task.getSoftLink() ? String.format("-type l -xtype %s", task.getType())  : String.format("-type %s", task.getType()) ),
                                convert2min(task.getRetention()),
                                (task.getMaxRetention() != null ? "-mmin -" + convert2min(task.getMaxRetention()) : ""),
                                (task.getVerbose() ? "-v " : ""),
                                (task.getType().startsWith("d") ? "-r" : ""),
                                task.getTarget()));
            }
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            startAndCheckProcess(processBuilder);
        }
    }

    private void startAndCheckProcess(ProcessBuilder processBuilder){
        try {
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            isActionTaskIsRunning = true;
            if ((task.getExpectedExecutionTime() == -1) && (task.getListOfRecipients().isEmpty() == false)){
                log.error("Task with request to send email if expected execution time exceed found, but parameter expectedExecutionTime is not defined.");
            }
            if ((task.getExpectedExecutionTime() > -1) && (task.getListOfRecipients().isEmpty())){
                log.error("Task with request to send email if expected execution time exceed found, but parameter listOfRecipients is not defined.");
            }
            if ((task.getExpectedExecutionTime() > -1) && (task.getListOfRecipients().isEmpty() == false)){
                process.waitFor(task.getExpectedExecutionTime(), TimeUnit.MINUTES);
                checkIfAlive(process, task.getAction());
            }
            // Clearing the buffer to let the task be finished
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) if (task.getVerbose()) log.info("Result: "+line);

            BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = readerError.readLine()) != null)  if (task.getVerbose()) log.info("Error: "+line);

            logStdOut(processBuilder, process);
            isActionTaskIsRunning = false;

        } catch (Exception e) {
            log.info("Exception while " + processBuilder.command(), e);
            System.exit(1);
        }
    }


        } catch (Exception e) {
            log.info("Exception while " + processBuilder.command(), e);
            System.exit(1);
        }
    }

    /**
     * concat STDOUT/STDERR
     * @param process
     * @param taskType
     * @throws IOException
     */
    public void checkIfAlive(Process process, String taskType) throws IOException {
        if (process.isAlive()) {
            log.info(String.format("After %s min. %s process is not finished",task.getExpectedExecutionTime(),taskType));
            String command = String.format("echo \"Chronos task execution exceeded an expected time. " +
                    "After %s min. %s process is not finished. Task description: %s. Path to full log file: %s\" |mail -s \"Chronos task execution warning.\" %s"
                    ,task.getExpectedExecutionTime(),taskType,task.gettaskDescriptionForEmailNotification(),
                    System.getProperty("user.dir")+"/logs/chronos.log", task.getListOfRecipients());
            log.info(String.format("WARNING EMAIL SENT: %s",task.getListOfRecipients().replace(" ",", ")));
            ProcessBuilder processMailSend = new ProcessBuilder();
            processMailSend.command("/bin/bash", "-c", command).start();
        }
    }
    /**
     * concat STDOUT/STDERR
     * @param process
     * @param taskType
     * @throws IOException
     */
    public void checkIfAlive(Process process, String taskType) throws IOException {
        if (process.isAlive()) {
            log.info(String.format("After %s min. %s process is not finished",task.getExpectedExecutionTime(),taskType));
            String command = String.format("echo \"Chronos task execution exceeded an expected time. " +
                    "After %s min. %s process is not finished. Task description: %s. Path to full log file: %s\" |mail -s \"Chronos task execution warning.\" %s"
                    ,task.getExpectedExecutionTime(),taskType,task.gettaskDescriptionForEmailNotification(),
                    System.getProperty("user.dir")+"/logs/chronos.log", task.getListOfRecipients());
            log.info(String.format("WARNING EMAIL SENT: %s",task.getListOfRecipients().replace(" ",", ")));
            ProcessBuilder processMailSend = new ProcessBuilder();
            processMailSend.command("/bin/bash", "-c", command).start();
        }
    }
    /**
     * convert days to min
     * @param days
     * @return
     */
    public String convert2min(String days){
        Float fDays = Float.valueOf(days);
        return String.valueOf((int) (fDays * 24 * 60));
    }

    /**
     * concat STDOUT/STDERR
     * @param process
     * @return
     * @throws IOException
     */
    private String getStdOut(Process process) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    /**
     * log STDOUT/STDERR if exitCode is not zero
     * @param processReporter
     * @param processtoReport
     * @throws IOException
     */

    private void logStdOut(ProcessBuilder processReporter, Process processtoReport) {
        try {
            int i = 0;
            while (processtoReport.isAlive() && i < 1000){ i++; Thread.sleep(100); }

            if (!processtoReport.isAlive()){
                if(processtoReport.exitValue() != 0)
                    log.info(String.valueOf(processReporter.command()) + ", exitCode: " + processtoReport.exitValue() + ", stderr: " + getStdOut(processtoReport));
                else
                    log.info(String.valueOf(processReporter.command()) + ", exitCode: 0");
            } else {
                log.info("The process is still alive after "+ i +" attempts to kill it. "+processReporter.command());
            }
            }
        catch (Exception e) {
            log.info("JAVA failed to provide the process exit code. ", e);
        }
}

}