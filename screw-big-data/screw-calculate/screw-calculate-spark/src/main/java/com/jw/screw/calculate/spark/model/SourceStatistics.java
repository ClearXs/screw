package com.jw.screw.calculate.spark.model;

import com.jw.screw.common.util.IdUtils;

import java.io.Serializable;
import java.util.Date;

public class SourceStatistics implements Serializable {

    private String id;

    private Date statisticTime;

    private String source;

    private int count;

    private Date startTime;

    private Date endTime;

    public SourceStatistics() {
        id = IdUtils.getNextIdAsString();
        statisticTime = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStatisticTime() {
        return statisticTime;
    }

    public void setStatisticTime(Date statisticTime) {
        this.statisticTime = statisticTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
