package com.roche.rss.lannister.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roche.rss.lannister.domain.Task;
import com.roche.rss.lannister.exception.InvalidTaskException;
import com.roche.rss.lannister.utils.FileUtils;
import com.roche.rss.lannister.utils.JsonUtils;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class ArgsConfiguration {
    private final Logger log = LoggerFactory.getLogger(ArgsConfiguration.class);

    @Autowired
    private ApplicationArguments args;

    private String templatedConfigPath=null;

    @Bean
    public ArgsComponent argsComponent() throws Exception {
        verifyArguments();
        if(templatedConfigPath != null)
            return new ArgsComponent(args, templatedConfigPath);
        else
            return new ArgsComponent(args);
    }

    /**
     * Verify each of tasks defined in config-file before those tasks get registered to ThreadPoolTaskScheduler.
     * Especially, detect critical commands to delete all files recursively.
     *
     * @throws IOException during objectMapper.readValue
     * @throws InvalidTaskException customized exception to validate task described in config-json
     */
    private void verifyArguments() throws Exception {
        //Check if argument is present
        if(args.containsOption("config-file")) {
            String configFile = args.getOptionValues("config-file").get(0);
            // if config-file is templated
            if(args.containsOption("template-data")){
                configFile = getConfigFromTemplate();
                templatedConfigPath = configFile;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Task> taskList = objectMapper.readValue(new File(configFile), new TypeReference<List<Task>>(){});
            for(Task task: taskList) {
                //task.setCronSchedule();
                if(task.getAction().equalsIgnoreCase(Action.MOVE.getCmd()) || task.getAction().equalsIgnoreCase(Action.COPY.getCmd())){
                    // source should exist
                    if(!FileUtils.isDirectory(task.getSource())) throw new InvalidTaskException("Invalid source directory : " + task.getSource());
                    // target should exist
                    if(!FileUtils.isDirectory(task.getTarget())) throw new InvalidTaskException("Invalid target directory : " + task.getTarget());
                }else if(task.getAction().equalsIgnoreCase(Action.DELETE.getCmd()) || task.getAction().equalsIgnoreCase(Action.UPDATE_LIST.getCmd())){
                    // source should exist
                    if(!FileUtils.isDirectory(task.getSource())) throw new InvalidTaskException("Invalid source directory : " + task.getSource());
                    // file name should not be  * , */* , **/* and so on
                    Pattern pattern = Pattern.compile("^\\*+[\\.\\*/]*\\**$", Pattern.CASE_INSENSITIVE);
                    for(String fileName: task.getName()){
                        Matcher matcher = pattern.matcher(fileName);
                        if(matcher.find()) throw new InvalidTaskException("Invalid File Name or Regular Expression : " + fileName);
                    }
                }else throw new InvalidTaskException(String.format("Invalid action not in [%s] : %s", getAllActions(), task.getAction()));
            }
        }else throw new InvalidTaskException("Invalid config-file : File not found");

        // check if include-list file exists
        if(args.containsOption("include-list") ){
            if(!FileUtils.isDirectory(args.getOptionValues("include-list").get(0))) throw new InvalidTaskException("File not exist : " + args.getOptionValues("include-list").get(0) );
        }
        // check if excldue-list file exists
        if(args.containsOption("exclude-list") ){
            if(!FileUtils.isDirectory(args.getOptionValues("exclude-list").get(0))) throw new InvalidTaskException("File not exist : " + args.getOptionValues("exclude-list").get(0) );
        }
    }

    /**
     * mapping variables in config-file into values in template-data
     * @return template file path
     * @throws Exception
     */
    private String getConfigFromTemplate() throws Exception {
        String configFile = args.getOptionValues("config-file").get(0);
        String json = args.getOptionValues("template-data").get(0);
        HashMap map = JsonUtils.jsonToMap(json);

        File tpl = new File(configFile);
        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setDirectoryForTemplateLoading(new File(tpl.getParent()));
        Template temp = cfg.getTemplate(tpl.getName());
        String newCfgFilePath = tpl.getParent().concat("/").
                concat(FileUtils.getBaseName(tpl.getName())).
                concat(String.valueOf(FileUtils.getRandomInt())).concat(".json");
        FileWriter fileWriter = new FileWriter(newCfgFilePath);
        Writer out = new BufferedWriter(fileWriter);
        try {
            temp.process(map, out);
        }finally {
            if(out != null) out.close();
        }

        return newCfgFilePath;
    }

    /**
     * Join all of actions in Enum Action
     * @return all action string which is delimted by "|"
     */
    private String getAllActions(){
        StringJoiner allAct = new StringJoiner("|");
        for (Action action : Action.values()){
            allAct.add(action.getCmd());
        }
        return allAct.toString();
    }
}