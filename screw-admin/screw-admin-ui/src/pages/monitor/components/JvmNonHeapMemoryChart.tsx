import React, { useEffect, useState } from 'react';
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/dataZoom';
import 'echarts/lib/component/tooltip';
import { BarSeriesOption } from 'echarts/lib/chart/bar/BarSeries';
import { EChartsFullOption } from 'echarts/lib/option';
import { EChartsType } from 'echarts/lib/echarts';
import _ from 'lodash';

const JvmNonHeapMemoryChart: React.FC<any> = ( props ) => {

    const [xAxis, setXAxis] = useState<string[]>([]);

    const [series, setSeries] = useState<BarSeriesOption[]>();

    const [isInit, setIsInit] = useState<boolean>(true);

    const [nonHeapMemoryChart, setNonHeapMemoryChart] = useState<EChartsType>();

    const initChart = () => {
        let element = document.getElementById('jvmNonHeapMemory');
        let chart = echarts.init(element as HTMLDivElement);
        setNonHeapMemoryChart(chart);
        setIsInit(false);
    }

    const buildMemorySeries = () => {
        if (_.isEmpty(series)) {
            return;
        }
        xAxis.push(props.xNonHeapMemoryAxis);
        const nonHeapMemorySeries = props.nonHeapMemorySeries;
        nonHeapMemorySeries.forEach((o) => {
            const filter: BarSeriesOption[] = series.filter((item) => {
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
                data: ['Max Metaspace', 'Used Metaspace', 'Committed Metaspace', 'Max Compressed Class Space', 'Used Compressed Class Space', 'Committed Compressed Class Space', 'Max Code Cache', 'Used Code Cache', 'Committed Code Cache']
            },
            dataZoom: [{
                type: 'slider',
            }, {
                type: 'inside',
            }],
            xAxis: [
                {
                    type: 'category',
                    data: xAxis
                }
            ],
            yAxis: {
                type: 'value',
                axisLabel: {
                    formatter: '{value}/M'
                }
            },
            series: series
        };
        nonHeapMemoryChart?.setOption(option);
        setXAxis(xAxis);
        setSeries(series);
    }

    useEffect(() => {
        if (!_.isEmpty(series)) {
            buildMemorySeries();
        }
        if (isInit) {
            setSeries([{
                    name: 'Max Metaspace',
                    type: 'bar',
                    stack: 'Metaspace',
                    data: []
                }, {
                    name: 'Used Metaspace',
                    type: 'bar',
                    stack: 'Metaspace',
                    data: []
                }, {
                    name: 'Committed Metaspace',
                    type: 'bar',
                    stack: 'Metaspace',
                    data: []
                }, {
                    name: 'Max Compressed Class Space',
                    type: 'bar',
                    stack: 'Compressed Class Space',
                    data: []
                }, {
                    name: 'Used Compressed Class Space',
                    type: 'bar',
                    stack: 'Compressed Class Space',
                    data: []
                }, {
                    name: 'Committed Compressed Class Space',
                    type: 'bar',
                    stack: 'Compressed Class Space',
                    data: []
                }, {
                    name: 'Max Code Cache',
                    type: 'bar',
                    stack: 'Code Cache',
                    data: []
                }, {
                    name: 'Used Code Cache',
                    type: 'bar',
                    stack: 'Code Cache',
                    data: []
                }, {
                    name: 'Committed Code Cache',
                    type: 'bar',
                    stack: 'Code Cache',
                    data: []
                }
            ])
            buildMemorySeries();
            initChart();
        }
        return () => {
            setIsInit(false);
        }
    })

    return (
        <div id="jvmNonHeapMemory" style={{ height: 400, width: 1000 }} />
    )
}

export default JvmNonHeapMemoryChart;