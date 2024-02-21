package com.roche.rss.lannister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;



@SpringBootApplication
@EnableScheduling
/**
 * Chronos is built on Spring Boot framework.
 * The @EnableScheduling annotation enables ThreadPoolTaskScheduler
 * RunnableTask will be registered to ThreadPoolTaskScheduler with CronTrigger instance.
 * Along with CronTrigger, RunnableTask will be executed.
 */
public class Chronos {

	private static final Logger logger = LoggerFactory.getLogger(Chronos.class);
	/**
	 * registers all the configurations such as ArgsConfiguration, CronTrigger and ThreadPoolTaskScheduler
	 * executes all the tasks registered in ThreadPoolTaskScheduler
	 *
	 * @param args --config-file to define a list of task in json
	 */
	public static void main(String[] args) {
		if (Arrays.asList("-h", "/h", "--help", "-help", "--h").contains(args[0])) {
			System.out.println("Usage for cronSchedule mode (default) : java -jar chronos.jar --config-file=your_config.txt");
			System.out.println("Usage for immediate mode: java -jar chronos.jar --config-file=your_config.txt --immediate");
			System.out.println("Usage for include list: java -jar chronos.jar --config-file=your_config.txt --include-list=your_list_file");
			System.out.println("Usage for exclude list: java -jar chronos.jar --config-file=your_config.txt --exclude-list=your_list_file");
			System.out.println("Usage for templating: java -jar chronos.jar --config-file=your_config_template.tpl --template-data=\"{\\\"source\\\":\\\"/Users/kimh89/chronos_demo/MainOutput\\\",\\\"target\\\":\\\"/Users/kimh89/chronos_demo/PermaOutput\\\"}");
			System.exit(0);
		}

		try{
			SpringApplication.run(Chronos.class, args);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
