package com.roche.rss.lannister.common;

import com.roche.rss.lannister.domain.Task;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * The class performs setUp() to create temporary directories and files that are required for the test cases such as move, delete, and copy.
 * Each of test cases will be executed.
 * Finally, the class calls shutDown() to remove those temporary directories and files.
 */
public class RunnableTaskTest {

    Task moveTask, deleteTask, copyTask;
    File sourceDir, targetDir, testDir1, testDir2, testFile1;

    @Before
    public void setUp() throws IOException, InterruptedException {
        System.out.println("setting up test ...");
        // create source and target dirs
        sourceDir = new File("test_source");
        sourceDir.mkdir();
        targetDir = new File("test_target");
        targetDir.mkdir();

        // create a test dir for move
        testDir1 =  new File (sourceDir.getPath() + "/test_move");
        testDir1.mkdir();

        // create a test file for delete
        testFile1 =  new File (sourceDir.getPath() + "/test_delete.txt");
        testFile1.createNewFile();

        // create a test dir for copy
        testDir2 =  new File (sourceDir.getPath() + "/test_copy");
        testDir2.mkdir();

        // it is required because each of test case asynchronously
        Thread.sleep(1000);

    }

    @After
    public void tearDown() throws IOException {
        System.out.println("clean test ...");
        // remove test dirs recursively
        removeDir(sourceDir.toPath());
        removeDir(targetDir.toPath());

    }

    @Test
    public void move() throws InterruptedException, IOException {
        moveTask = new Task("move", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath(),"d", new String[] {"test_move"}, "0 56 13 * * ?", "0");
        moveTask.setFiltering(false);
        moveTask.setMaxRetention("1");
        moveTask.setMaxDepth("2");
        RunnableTask runnableTask = new RunnableTask(moveTask.toString(),moveTask);
        runnableTask.run();

        Assert.assertFalse("File Should Not Exist", Files.exists(new File(sourceDir.getAbsolutePath() + "/test_move").toPath()));
        Assert.assertTrue("File should exist", Files.exists(new File(targetDir.getAbsolutePath() + "/test_move").toPath()));
    }

    @Test
    public void delete() throws IOException, InterruptedException {
        deleteTask = new Task("delete",sourceDir.getAbsolutePath(),null,"f", new String[] {"test_delete.txt"}, "0 56 13 * * ?", "0");
        deleteTask.setFiltering(false);
        deleteTask.setMaxRetention("1");
        RunnableTask runnableTask = new RunnableTask(deleteTask.toString(), deleteTask);
        runnableTask.run();

        Assert.assertFalse("File Should Not Exist", Files.exists(new File(sourceDir.getAbsolutePath() + "/test_delete.txt").toPath()));
    }

    @Test
    public void copy() throws InterruptedException {
        copyTask = new Task("copy",sourceDir.getAbsolutePath(), targetDir.getAbsolutePath(), "d", new String[] {"test_copy"}, "0 56 13 * * ?", "0");
        copyTask.setFiltering(false);
        copyTask.setMaxRetention("1");
        RunnableTask runnableTask = new RunnableTask(copyTask.toString(), copyTask);
        runnableTask.run();

        Assert.assertTrue("File should exist", Files.exists(new File(sourceDir.getAbsolutePath() + "/test_copy").toPath()));
        Assert.assertTrue("File should exist", Files.exists(new File(targetDir.getAbsolutePath() + "/test_copy").toPath()));
    }

    @Test
    public void testCheckIfAlive() throws IOException {
            moveTask = new Task("move", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath(),"d", new String[] {"test_move"}, "0 56 13 * * ?", "0");
            moveTask.setFiltering(false);
            moveTask.setMaxRetention("1");
            moveTask.setMaxDepth("2");
            moveTask.setExpectedExecutionTime("2");
            moveTask.setListOfRecipients("hee.kim@roche.com, andrii.savchenko@roche.com");
            moveTask.setTaskDescriptionForEmailNotification("AOA Pipeline, delete files configuration");

            Runnable runnable = new RunnableTask("",moveTask);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("/bin/bash", "-c", "sleep 3");
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            try {
                Process process = processBuilder.start();
                process.waitFor(1, TimeUnit.SECONDS);
                ((RunnableTask) runnable).checkIfAlive(process, moveTask.getAction());
                process.waitFor();
            } catch (Exception e){
                System.exit(1);
            }
        }


    public void removeDir(Path dir) throws IOException {
        Files.walk(dir)
                .map(Path::toFile)
                .forEach(File::delete);
        dir.toFile().delete();
    }
}