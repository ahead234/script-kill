<%@ include file="../common/header.jspf" %>
<section class="error-page">
    <div>
        <div class="error-code">403</div>
        <h1>没有访问权限</h1>
        <p>当前账号无权访问此页面或执行此操作。</p>
        <a class="button button-primary" href="${pageContext.request.contextPath}/scripts">返回首页</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
