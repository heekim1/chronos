package com.roche.rss.lannister.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roche.rss.lannister.common.ArgsComponent;
import com.roche.rss.lannister.common.RunnableTask;
import com.roche.rss.lannister.domain.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
/**
 * Read a json file, which includes a list of tasks from the args.config-file --config-file.
 *   {
 *     "action": "move",
 *     "source": "/Users/kimh89/test",
 *     "target": "/Users/kimh89/test_target",
 *     "name": ["test*",
 *       "freq*"],
 *     "type": "d",
 *     "cronSchedule" : "0 43 20 * * ?",
 *     "retention": "0"
 *   }
 * Each of tasks maps to the Task class.
 * Each of those mapped tasks is going to be registered into scheduler as a RunnableTask instance.
 * Each of those RunnableTask instances will be triggered along with its cronSchedule.
 */
public class TaskSchedulerService {
    private final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ArgsComponent args;

    private CronTrigger cronTrigger;

    @PostConstruct
    private void execute() throws Exception {
        String configFile = args.getArgs().getOptionValues("config-file").get(0);
        // if config-file is templated
        if(args.getTemplatedConfigPath() != null){
            configFile = args.getTemplatedConfigPath();
        }
        log.info("The config-file : " + configFile);

        if(args.getArgs().containsOption("immediate")){
            log.info("Executing tasks in immediate mode ...");
            runExecutorService(configFile);
        }else{
            log.info("Executing scheduled tasks ...");
            scheduleRunnableWithCronTrigger(configFile);
        }
    }

    /**
     * Execute tasks without cronTrigger
     * @throws IOException
     */
    private void runExecutorService(String configFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Task> taskList = objectMapper.readValue(new File(configFile), new TypeReference<List<Task>>() {});
        for (Task task : taskList) {
            try {
                executorService.submit(new RunnableTask(task.toString(), task, args));
                log.info(task.toString() + " has been registered successfully.");
            }catch (Exception e){
                e.printStackTrace();
                log.error("ExecutorService faild : " + e);
                System.exit(1);
            }
        }
        executorService.shutdown();
        shutdownAndAwaitTermination(executorService);
    }


    /**
     * Execute tasks with cronTrigger
     * @throws IOException
     */
    private void scheduleRunnableWithCronTrigger(String configFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Task> taskList = objectMapper.readValue(new File(configFile), new TypeReference<List<Task>>() {});
        for (Task task : taskList) {
            cronTrigger = new CronTrigger(task.getCronSchedule());
            try {
                taskScheduler.schedule(new RunnableTask(task.toString(), task, args), cronTrigger);
                log.info(task.toString() + " has been registered successfully.");
            }catch (Exception e){
                e.printStackTrace();
                log.error("TaskScheduler faild : " + e);
                System.exit(1);
            }
        }
    }

    /**
     * Shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming tasks, and then calling
     * shutdownNow only if any thread is running more than timeout
     * @param es ExecutorService object that creates a pool of threads
     */
    private void shutdownAndAwaitTermination(ExecutorService es) {
        try {
            if (!es.awaitTermination(2, TimeUnit.HOURS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
