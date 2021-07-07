package com.jw.screw.monitor.core.mircometer;

import io.micrometer.core.instrument.*;

import java.util.List;

/**
 * 应用性能信息
 * @author jiangw
 * @date 2020/12/22 14:44
 * @since 1.0
 */
public class Metrics {

    private final String name;

    private final String description;

    private final String unit;

    private final List<Sample> measurements;

    private final List<Tag> availableTags;

    public Metrics(String name, String description, String unit, List<Sample> measurements, List<Tag> availableTags) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.measurements = measurements;
        this.availableTags = availableTags;
    }

    public static final class Sample {

        private final Statistic statistic;

        private final Double value;

        Sample(Statistic statistic, Double value) {
            this.statistic = statistic;
            this.value = value;
        }

        public Statistic getStatistic() {
            return this.statistic;
        }

        public Double getValue() {
            return this.value;
        }


        @Override
        public String toString() {
            return "MeasurementSample{statistic=" + this.statistic + ", value=" + this.value + '}';
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public List<Sample> getMeasurements() {
        return measurements;
    }

    public List<Tag> getAvailableTags() {
        return availableTags;
    }

    @Override
    public String toString() {
        return "MetricsBody{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", unit='" + unit + '\'' +
                ", measurements=" + measurements +
                ", availableTags=" + availableTags +
                '}';
    }

}
