import React, { useEffect, useState } from 'react';
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/line';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/dataZoom';
import 'echarts/lib/component/tooltip';
import { LineSeriesOption } from 'echarts/lib/chart/line/LineSeries';
import { EChartsFullOption } from 'echarts/lib/option';
import { EChartsType } from 'echarts/lib/echarts';
import _ from 'lodash';

const JvmThreadChart: React.FC<any> = ( props ) => {

    const [xAxis, setXAxis] = useState<string[]>([]);

    const [isInit, setIsInit] = useState<boolean>(true);

    const [series, setSeries] = useState<LineSeriesOption[]>();

    const [threadChart, setThreadChart] = useState<EChartsType>();

    let initChart = () => {
        let element = document.getElementById('jvmThreads');
        let chart = echarts.init(element as HTMLDivElement);
        setThreadChart(chart);
        setIsInit(false);
    };

    const buildThreadSeries = () => {
        if (_.isEmpty(series)) {
            return;
        }
        xAxis.push(props.xThreadAxis);
        const threadSeries = props.threadSeries;
        threadSeries.forEach((o) => {
            const filter: LineSeriesOption[] = series.filter((item) => {
                return item.name === o.name;
            });
            if (!_.isEmpty(filter)) {
                filter[0].data.push(o.value);
            }
        });
        const option: EChartsFullOption = {
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                orient: 'horizontal',
                align: 'left',
                data: ['terminated', 'blocked', 'new', 'runnable', 'waiting', 'timed-waiting', 'live', 'peak']
            },
            dataZoom: [{
                type: 'slider',
            }, {
                type: 'inside',
            }],
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: xAxis
            },
            yAxis: {
                type: 'value'
            },
            series: series
        };
        threadChart?.setOption(option);
        setXAxis(xAxis);
        setSeries(series);
    }

    useEffect(() => {
        if (!_.isEmpty(series)) {
            buildThreadSeries();
        }
        if (isInit) {
            setSeries([{
                name: 'terminated',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'blocked',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'new',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'runnable',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'waiting',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'timed-waiting',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'live',
                type: 'line',
                step: 'start',
                data: []
            }, {
                name: 'peak',
                type: 'line',
                step: 'start',
                data: []
            }])
            buildThreadSeries();
            initChart();
        }
        return () => {
            setIsInit(false);
        }
    })

    return (
        <div id="jvmThreads" style={{ height: 400, width: 1000 }} />
    )
}

export default JvmThreadChart;
