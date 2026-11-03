<%@ include file="common/header.jspf" %>
<div class="login-shell">
    <section class="panel login-card">
        <h1>欢迎回来</h1>
        <p>登录后管理预约，或继续探索下一场故事。</p>
        <%@ include file="common/messages.jspf" %>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <div class="field">
                <label class="required" for="username">账号</label>
                <input class="input" id="username" name="username" maxlength="50" autocomplete="username" required value="<c:out value='${username}'/>">
            </div>
            <div class="field">
                <label class="required" for="password">密码</label>
                <input class="input" id="password" type="password" name="password" maxlength="200" autocomplete="current-password" required>
            </div>
            <button class="button button-primary" type="submit">登录</button>
        </form>
        <p class="login-alt">还没有账号？<a href="${ctx}/register">立即注册</a></p>
    </section>
</div>
<%@ include file="common/footer.jspf" %>
