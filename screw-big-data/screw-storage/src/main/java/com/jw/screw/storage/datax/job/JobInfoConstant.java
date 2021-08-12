package com.jw.screw.storage.datax.job;

public class JobInfoConstant implements JobInfoStrategy {

    @Override
    public String execute(JobInfo jobInfo) {
        return jobInfo.getValue();
    }
}
