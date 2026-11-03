<%@ include file="../common/header.jspf" %>
<div class="page-header report-header">
    <div>
        <h1>数据报表</h1>
        <p>预约、排期与审核的核心指标概览。</p>
    </div>
    <div class="report-actions">
        <input class="input report-date" type="date" data-report-from aria-label="开始日期">
        <span class="report-range-sep">至</span>
        <input class="input report-date" type="date" data-report-to aria-label="结束日期">
        <button class="button button-primary" type="button" data-report-refresh>
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M16.2 10a6.2 6.2 0 1 1-1.9-4.5"/><path d="M16.2 3.4v2.7h-2.7"/></svg>
            刷新
        </button>
    </div>
</div>
<%@ include file="../common/messages.jspf" %>

<div class="summary-grid">
    <article class="card summary-card">
        <span class="summary-label">开放场次</span>
        <strong class="summary-value">${stats.openSessions}</strong>
        <span class="summary-meta">全部剧本 ${stats.scriptCount} 部</span>
    </article>
    <article class="card summary-card">
        <span class="summary-label">待到店预约</span>
        <strong class="summary-value">${stats.confirmedBookings}</strong>
        <span class="summary-meta">预约总数 ${stats.totalBookings} 条</span>
    </article>
    <article class="card summary-card">
        <span class="summary-label">已到店</span>
        <strong class="summary-value">${stats.checkedInBookings}</strong>
        <span class="summary-meta">到店待完成</span>
    </article>
    <article class="card summary-card">
        <span class="summary-label">待审核评价</span>
        <strong class="summary-value">${stats.pendingReviews}</strong>
        <span class="summary-meta">等待审核处理</span>
    </article>
</div>

<section class="card report-panel">
    <div class="panel-header">
        <h2>预约统计</h2>
        <span class="muted small">状态分布与新增趋势</span>
    </div>
    <div class="report-split">
        <div class="report-block">
            <div class="report-block-head">
                <h3>预约状态分布</h3>
                <span class="report-block-note">共 ${stats.totalBookings} 条预约</span>
            </div>
            <div class="chart-box" data-chart="bookingStatus"></div>
        </div>
        <div class="report-block">
            <div class="report-block-head">
                <h3>预约趋势</h3>
                <span class="report-block-note" data-trend-range>近 7 日</span>
            </div>
            <div class="chart-box" data-chart="trend"></div>
        </div>
    </div>
</section>

<section class="card report-panel">
    <div class="panel-header">
        <h2>运营明细</h2>
        <span class="muted small">剧本热度与审核 / 排期状态</span>
    </div>
    <div class="report-split">
        <div class="report-block">
            <div class="report-block-head">
                <h3>热门剧本</h3>
                <span class="report-block-note">按累计预约量排序</span>
            </div>
            <div class="chart-box" data-chart="scriptDemand"></div>
        </div>
        <div class="report-block">
            <div class="report-block-head">
                <h3>审核与排期状态</h3>
                <span class="report-block-note">评价与场次数量对比</span>
            </div>
            <div class="chart-box" data-chart="moderation"></div>
        </div>
    </div>
</section>

<script src="${ctx}/assets/vendor/echarts.min.js"></script>
<script src="${ctx}/assets/js/admin-charts.js?v=2"></script>
<%@ include file="../common/footer.jspf" %>
