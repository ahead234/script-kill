<% response.setStatus(500); %>
<%@ include file="../common/header.jspf" %>
<section class="error-page">
    <div>
        <div class="error-code">500</div>
        <h1>系统暂时无法处理请求</h1>
        <p>请稍后重试；系统不会向页面暴露内部异常信息。</p>
        <a class="button button-primary" href="${pageContext.request.contextPath}/scripts">返回首页</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
