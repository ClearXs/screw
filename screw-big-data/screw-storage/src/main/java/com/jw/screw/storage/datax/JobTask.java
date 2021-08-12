package com.jw.screw.storage.datax;

import com.alibaba.fastjson.JSON;
import com.zzht.patrol.screw.common.constant.StringPool;
import com.zzht.patrol.screw.common.parser.FormatParser;
import com.zzht.patrol.screw.common.util.FileUtils;
import com.zzht.patrol.screw.storage.datax.job.JobHandle;
import com.zzht.patrol.screw.storage.datax.properties.DataXProperties;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * 基于quartz进行定时离线数据同步
 * @author jiangw
 * @date 2021/7/27 15:13
 * @since 1.1
 */
public class JobTask {

    public JobTask() throws SchedulerException {
        DataXProperties properties = null;
        // 读取配置
        try {
            byte[] bytes =  FileUtils.readFileByNIO("classpath:screw-storage.yml");
            String config = new String(bytes);
            String propertiesStr = FormatParser.yamlToProperties(config);
            // 去除datax的前缀
            StringBuilder screwDataXProperties = new StringBuilder();
            for (String itemConfig : propertiesStr.split(StringPool.NEWLINE)) {
                if (itemConfig.startsWith("datax")) {
                    // + 1的目的是为了去除datax.的.
                    String segment = itemConfig.substring("datax".length() + 1);
                    screwDataXProperties.append(segment).append(StringPool.NEWLINE);
                }
            }
            String configJson = FormatParser.propertiesToJson(screwDataXProperties.toString());
            properties = JSON.parseObject(configJson, DataXProperties.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (properties == null) {
            throw new NullPointerException("dataX configuration reader error，please screw-storage.yml");
        }
        // 初始化quartz
        // 1.创建scheduler工厂
        StdSchedulerFactory factory = new StdSchedulerFactory();
        // 2.获取调度器
        Scheduler scheduler = factory.getScheduler();
        // 3.创建JobDetails
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("properties", properties);
        JobDetail jobDetail = JobBuilder
                .newJob(Synchronizer.class)
                // job 描述
                .withDescription("data synchronous")
                // job name 与 group
                .withIdentity("screw datax job", "screw")
                .setJobData(dataMap)
                .build();
        CronTrigger trigger = TriggerBuilder
                .newTrigger()
                .withDescription("")
                .withIdentity("screw datax trigger", "screw")
                .startAt(new Date())
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.getJobCorn()))
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }


    public static class Synchronizer implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            DataXProperties properties = (DataXProperties) dataMap.get("properties");
            if (!properties.isEnable()) {
                return;
            }
            JobHandle jobHandle = JobHandle.newInstance();
            try {
                jobHandle.handle(properties);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
