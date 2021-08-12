package com.jw.screw.storage.datax.job;

import java.util.HashMap;
import java.util.Map;

/**
 * strategy上下文类
 * @author jiangw
 * @date 2021/7/27 15:06
 * @since 1.1
 */
public class JobInfoSelector {

    private final Map<String, JobInfoStrategy> cache;

    public JobInfoSelector() {
        cache = new HashMap<>(3);
        cache.put(JobInfo.CONSTANT, new JobInfoConstant());
        cache.put(JobInfo.DATABASE, new JobInfoDatabase());
        cache.put(JobInfo.JEXL, new JobInfoJexl());
    }

    public String select(JobInfo jobInfo) {
        JobInfoStrategy strategy = cache.get(jobInfo.getType());
        if (strategy == null) {
            return jobInfo.getValue();
        }
        return strategy.execute(jobInfo);
    }
}
