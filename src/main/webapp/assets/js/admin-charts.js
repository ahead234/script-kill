/* 管理后台报表图表：ECharts 精简样式，直角柱体、细环、细网格线，不使用渐变与圆角色块。
   日期区间通过 /admin/api/stats 的 from/to 参数传给后端，只影响"预约趋势"，其余指标为当前快照。 */
(function () {
    var INK = '#3f6578';
    var GREEN = '#2f6558';
    var AXIS_LINE = '#d6ded9';
    var AXIS_TEXT = '#6e7b76';
    var SPLIT_LINE = '#eef2ef';

    var fromInput = document.querySelector('[data-report-from]');
    var toInput = document.querySelector('[data-report-to]');
    var rangeNote = document.querySelector('[data-trend-range]');
    var refreshButton = document.querySelector('[data-report-refresh]');
    var instances = [];

    function pad(value) { return value < 10 ? '0' + value : String(value); }

    function toDateString(date) {
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
    }

    function seedRange() {
        if (!fromInput || !toInput) return;
        var today = new Date();
        if (!toInput.value) toInput.value = toDateString(today);
        if (!fromInput.value) {
            var start = new Date(today.getTime());
            start.setDate(start.getDate() - 6);
            fromInput.value = toDateString(start);
        }
    }

    function readRange() {
        seedRange();
        if (!fromInput || !toInput) return { from: '', to: '' };
        if (fromInput.value && toInput.value && fromInput.value > toInput.value) {
            var swap = fromInput.value;
            fromInput.value = toInput.value;
            toInput.value = swap;
        }
        return { from: fromInput.value, to: toInput.value };
    }

    function categoryAxis(data) {
        return {
            type: 'category',
            data: data,
            axisLine: { lineStyle: { color: AXIS_LINE } },
            axisTick: { show: false },
            axisLabel: { color: AXIS_TEXT, fontSize: 11 }
        };
    }

    function valueAxis() {
        return {
            type: 'value',
            minInterval: 1,
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { lineStyle: { color: SPLIT_LINE } },
            axisLabel: { color: AXIS_TEXT, fontSize: 11 }
        };
    }

    function trendOption(data) {
        return {
            grid: { left: 8, right: 16, top: 16, bottom: 6, containLabel: true },
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            xAxis: categoryAxis(data.map(function (item) { return item.label; })),
            yAxis: valueAxis(),
            series: [{
                type: 'bar',
                barMaxWidth: 26,
                data: data.map(function (item) { return item.count; }),
                itemStyle: { color: INK }
            }]
        };
    }

    function bookingStatusOption(data) {
        return {
            tooltip: { trigger: 'item', formatter: '{b}：{c} 条（{d}%）' },
            legend: {
                bottom: 0,
                itemWidth: 10,
                itemHeight: 10,
                itemGap: 16,
                textStyle: { color: AXIS_TEXT, fontSize: 12 }
            },
            series: [{
                type: 'pie',
                radius: ['46%', '62%'],
                center: ['50%', '45%'],
                avoidLabelOverlap: true,
                label: {
                    show: true,
                    position: 'outside',
                    color: AXIS_TEXT,
                    fontSize: 11,
                    formatter: '{b}'
                },
                labelLine: { length: 8, length2: 8, lineStyle: { color: AXIS_LINE } },
                itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
                data: data.map(function (item) {
                    return { name: item.label, value: item.count, itemStyle: { color: item.color } };
                })
            }]
        };
    }

    function scriptDemandOption(data) {
        var rows = data.slice().reverse();
        return {
            grid: { left: 8, right: 32, top: 8, bottom: 6, containLabel: true },
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            xAxis: valueAxis(),
            yAxis: categoryAxis(rows.map(function (item) { return item.label; })),
            series: [{
                type: 'bar',
                barMaxWidth: 16,
                data: rows.map(function (item) { return item.count; }),
                itemStyle: { color: GREEN },
                label: { show: true, position: 'right', color: AXIS_TEXT, fontSize: 11 }
            }]
        };
    }

    function moderationOption(reviewStatus, sessionStatus) {
        var labels = [];
        var reviewValues = [];
        var sessionValues = [];
        reviewStatus.forEach(function (item) {
            labels.push(item.label);
            reviewValues.push(item.count);
            sessionValues.push('-');
        });
        sessionStatus.forEach(function (item) {
            labels.push(item.label);
            reviewValues.push('-');
            sessionValues.push(item.count);
        });
        return {
            grid: { left: 8, right: 16, top: 28, bottom: 6, containLabel: true },
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: {
                top: 0,
                itemWidth: 10,
                itemHeight: 10,
                itemGap: 14,
                textStyle: { color: AXIS_TEXT, fontSize: 12 }
            },
            xAxis: categoryAxis(labels),
            yAxis: valueAxis(),
            series: [
                { name: '评价状态', type: 'bar', barMaxWidth: 18, data: reviewValues, itemStyle: { color: GREEN } },
                { name: '场次状态', type: 'bar', barMaxWidth: 18, data: sessionValues, itemStyle: { color: INK } }
            ]
        };
    }

    function optionFor(type, stats) {
        if (type === 'trend') return trendOption(stats.trend || []);
        if (type === 'bookingStatus') return bookingStatusOption(stats.bookingStatus || []);
        if (type === 'scriptDemand') return scriptDemandOption(stats.scriptDemand || []);
        if (type === 'moderation') return moderationOption(stats.reviewStatus || [], stats.sessionStatus || []);
        return null;
    }

    function chartFor(element) {
        var chart = echarts.getInstanceByDom(element);
        if (!chart) {
            chart = echarts.init(element);
            instances.push(chart);
        }
        return chart;
    }

    function render(stats) {
        document.querySelectorAll('[data-chart]').forEach(function (element) {
            var option = optionFor(element.getAttribute('data-chart'), stats);
            if (option) chartFor(element).setOption(option, true);
        });
    }

    function setLoading(loading) {
        if (refreshButton) refreshButton.disabled = loading;
    }

    function load() {
        var range = readRange();
        if (rangeNote) {
            rangeNote.textContent = range.from && range.to ? range.from + ' 至 ' + range.to : '近 7 日';
        }
        setLoading(true);
        fetch('api/stats?from=' + encodeURIComponent(range.from) + '&to=' + encodeURIComponent(range.to),
            { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (!response.ok) throw new Error('stats request failed');
                return response.json();
            })
            .then(function (stats) { render(stats); })
            .catch(function () {
                if (rangeNote) rangeNote.textContent = '数据加载失败，请刷新重试';
            })
            .then(function () { setLoading(false); });
    }

    if (refreshButton) {
        refreshButton.addEventListener('click', load);
    }
    window.addEventListener('resize', function () {
        instances.forEach(function (chart) { chart.resize(); });
    });

    load();
})();
