<%@ include file="../common/header.jspf" %>
<section class="error-page">
    <div>
        <div class="error-code">404</div>
        <h1>页面没有找到</h1>
        <p>链接可能已失效，或对应内容已经不存在。</p>
        <a class="button button-primary" href="${pageContext.request.contextPath}/scripts">返回首页</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
