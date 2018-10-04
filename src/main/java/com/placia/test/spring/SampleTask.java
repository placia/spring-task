package com.placia.test.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeEvent;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.cloud.task.listener.TaskExecutionListener;
import org.springframework.cloud.task.listener.annotation.AfterTask;
import org.springframework.cloud.task.listener.annotation.BeforeTask;
import org.springframework.cloud.task.listener.annotation.FailedTask;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableTask
@Slf4j
public class SampleTask {
    @Bean
    public CommandLineRunner commandLineRunner() {
        return new HelloWorldCommandLineRunner();
    }

    @Bean
    ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            if (exception.getCause() instanceof IllegalAccessException) {
                return 100;
            } else {
                return -1;
            }
        };
    }

    @Bean
    DemoListener demoListenerBean() {
        return new DemoListener();
    }

    @Bean
    public CommandLineRunner commandLineRunnerCustom() {
        return new CustomCommandLineRunner();
    }

//    @Bean
//    public TaskExecutionListener taskExecutionListener() {
//        return new SampleTaskExecutionListener();
//    }

    @Bean
    public TaskExecutionListenerByAnnotation taskExecutionListenerByAnnotation() {
        return new TaskExecutionListenerByAnnotation();
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleTask.class, args);
    }

    public static class HelloWorldCommandLineRunner implements CommandLineRunner {

        @Override
        public void run(String... strings) throws Exception {
            log.info("Hello, World!");
        }
    }

    public static class CustomCommandLineRunner implements CommandLineRunner {

        @Override
        public void run(String... args) throws Exception {
            log.info("custom commandLineRunner");
            throw new IllegalAccessException();
        }
    }

    public static class DemoListener {
        @EventListener
        public void exitEvent(ExitCodeEvent event) {
            log.info("Received exit code: {}", event.getExitCode());
        }
    }

//    public static class SampleTaskExecutionListener implements TaskExecutionListener {
//
//        @Override
//        public void onTaskStartup(TaskExecution taskExecution) {
//            log.info("SampleTaskExecutionListener startUp: {}", taskExecution);
//        }
//
//        @Override
//        public void onTaskEnd(TaskExecution taskExecution) {
//            log.info("SampleTaskExecutionListener taskEnd: {}", taskExecution);
//        }
//
//        @Override
//        public void onTaskFailed(TaskExecution taskExecution, Throwable throwable) {
//            log.info("SampleTaskExecutionListener taskFailed: {}", taskExecution);
//        }
//    }

    public static class TaskExecutionListenerByAnnotation {
        @BeforeTask
        public void beforTask(TaskExecution taskExecution) {
            taskExecution.setTaskName("change taskName"); //but at afterTask, failedTask, it is not changed
            taskExecution.setExitMessage("startTask");
            log.info("Annotaion before Listener:{}", taskExecution);
        }

        @AfterTask
        public void afterTask(TaskExecution taskExecution) {
            taskExecution.setExitMessage("despite of failed, it is always called");
            log.info("Annotaion after Listener:{}", taskExecution);
        }

        @FailedTask
        public void failedTask(TaskExecution taskExecution, Throwable throwable) {
            taskExecution.setExitMessage("task failed");
            log.info("Annotaion failed Listener:{}, ", taskExecution, throwable);
        }
    }
}
