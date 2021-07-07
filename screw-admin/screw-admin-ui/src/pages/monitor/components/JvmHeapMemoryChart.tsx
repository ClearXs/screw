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

const JvmHeapMemoryChart: React.FC<any> = ( props ) => {

    const [xAxis, setXAxis] = useState<string[]>([]);

    const [series, setSeries] = useState<BarSeriesOption[]>();

    const [isInit, setIsInit] = useState<boolean>(true);

    const [heapMemoryChart, setHeapMemoryChart] = useState<EChartsType>();

    const initChart = () => {
        let element = document.getElementById('jvmHeapMemory');
        let chart = echarts.init(element as HTMLDivElement);
        setHeapMemoryChart(chart);
        setIsInit(false);
    }

    const buildMemorySeries = () => {
        if (_.isEmpty(series)) {
            return;
        }
        xAxis.push(props.xHeapMemoryAxis);
        const heapMemorySeries = props.heapMemorySeries;
        heapMemorySeries.forEach((o) => {
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
                data: ['Max Survivor Space', 'Used Survivor Space', 'Committed Survivor Space', 'Max Eden Space', 'Used Eden Space', 'Committed Eden Space', 'Max Old Gen', 'Used Old Gen', 'Committed Old Gen']
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
        heapMemoryChart?.setOption(option);
        setXAxis(xAxis);
        setSeries(series);
    }

    useEffect(() => {
        if (!_.isEmpty(series)) {
            buildMemorySeries();
        }
        if (isInit) {
            setSeries([{
                    name: 'Max Survivor Space',
                    type: 'bar',
                    stack: 'Survivor Space',
                    data: []
                }, {
                    name: 'Used Survivor Space',
                    type: 'bar',
                    stack: 'Survivor Space',
                    data: []
                }, {
                    name: 'Committed Survivor Space',
                    type: 'bar',
                    stack: 'Survivor Space',
                    data: []
                }, {
                    name: 'Max Eden Space',
                    type: 'bar',
                    stack: 'Eden Space',
                    data: []
                }, {
                    name: 'Used Eden Space',
                    type: 'bar',
                    stack: 'Eden Space',
                    data: []
                }, {
                    name: 'Committed Eden Space',
                    type: 'bar',
                    stack: 'Eden Space',
                    data: []
                }, {
                    name: 'Max Old Gen',
                    type: 'bar',
                    stack: 'Old Gen',
                    data: []
                }, {
                    name: 'Used Old Gen',
                    type: 'bar',
                    stack: 'Old Gen',
                    data: []
                }, {
                    name: 'Committed Old Gen',
                    type: 'bar',
                    stack: 'Old Gen',
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
        <div id="jvmHeapMemory" style={{ height: 400, width: 1000 }} />
    )
}

export default JvmHeapMemoryChart;