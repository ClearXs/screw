package com.jw.screw.storage.datax.job;

/**
 * info value对应的策略类型
 * @author jiangw
 * @date 2021/7/27 14:57
 * @since 1.1
 */
public interface JobInfoStrategy {

    /**
     * 根据type不同类型，计算不同的记过
     * @param jobInfo {@link JobInfo}
     * @return 计算的记过
     */
    String execute(JobInfo jobInfo);
}
