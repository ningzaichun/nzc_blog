package com;

import org.activiti.engine.*;
import org.junit.Test;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年07月15日 17:13
 */
public class nzc {
    public static class TestDemo {
        /**
         * 生成 activiti的数据库表
         */
        @Test
        public void testCreateDbTable() {
            //使用classpath下的activiti.cfg.xml中的配置创建processEngine
//            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//            System.out.println(processEngine);

            ProcessEngineConfiguration configuration =
                    ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");

            //通过ProcessEngineConfiguration创建ProcessEngine，此时会创建数据库
            ProcessEngine processEngine = configuration.buildProcessEngine();

            RuntimeService runtimeService = processEngine.getRuntimeService();
            RepositoryService repositoryService = processEngine.getRepositoryService();
            TaskService taskService = processEngine.getTaskService();

        }
    }


}
