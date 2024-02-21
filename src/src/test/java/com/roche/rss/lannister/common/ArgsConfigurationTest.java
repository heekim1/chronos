package com.roche.rss.lannister.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roche.rss.lannister.domain.Task;
import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(args = "--config-file=src/test/resources/config.json")
public class ArgsConfigurationTest {

    @Autowired
    private ArgsConfiguration argsConfiguration;

    /**
     * The source and the target directories are supposed to exist during the test.
     */
    @Test
    public void readConfigJson() throws Exception {
        argsConfiguration.argsComponent();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Task> taskList = objectMapper.readValue(new File("src/test/resources/config.json"), new TypeReference<List<Task>>(){});
        Assert.assertTrue(taskList != null);
    }

    @Test
    public void readConfigWithExtraProperties() throws Exception {
        argsConfiguration.argsComponent();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Task> taskList = objectMapper.readValue(new File("src/test/resources/configExtra.json"), new TypeReference<List<Task>>(){});
        Assert.assertTrue(taskList != null);
    }
}
