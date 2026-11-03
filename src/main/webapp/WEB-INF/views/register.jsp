<%@ include file="common/header.jspf" %>
<div class="login-shell">
    <section class="panel login-card register-card">
        <h1>创建账号</h1>
        <p>注册后即可预约场次、发表评价。</p>
        <%@ include file="common/messages.jspf" %>
        <form method="post" action="${ctx}/register" data-register-form novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="formToken" value="<c:out value='${formToken}'/>">

            <div class="field" data-field="username">
                <label class="required" for="username">用户名</label>
                <input class="input" id="username" name="username" maxlength="20" autocomplete="username"
                       required aria-describedby="username-hint" data-validate="username"
                       value="<c:out value='${username}'/>">
                <span class="help" id="username-hint">4-20 位字母、数字或下划线</span>
                <span class="field-error" data-error-for="username"><c:out value='${formErrors.username}'/></span>
            </div>

            <div class="field" data-field="phone">
                <label class="required" for="phone">手机号</label>
                <input class="input" id="phone" name="phone" type="tel" inputmode="numeric" maxlength="11"
                       autocomplete="tel" required aria-describedby="phone-hint" data-validate="phone"
                       placeholder="请输入 11 位手机号" value="<c:out value='${phone}'/>">
                <span class="help" id="phone-hint">11 位手机号，用于预约联系</span>
                <span class="field-error" data-error-for="phone"><c:out value='${formErrors.phone}'/></span>
            </div>

            <div class="field" data-field="password">
                <label class="required" for="password">密码</label>
                <div class="password-wrap">
                    <input class="input" id="password" name="password" type="password" maxlength="50"
                           autocomplete="new-password" required aria-describedby="password-hint" data-validate="password">
                    <button class="password-toggle" type="button" data-password-toggle="password" aria-label="显示密码">显示</button>
                </div>
                <div class="strength" data-strength hidden>
                    <span class="strength-track"><span class="strength-fill" data-strength-fill></span></span>
                    <span class="strength-text" data-strength-text>密码强度：弱</span>
                </div>
                <span class="help" id="password-hint">至少 8 位，需同时包含字母和数字</span>
                <span class="field-error" data-error-for="password"><c:out value='${formErrors.password}'/></span>
            </div>

            <div class="field" data-field="confirmPassword">
                <label class="required" for="confirmPassword">确认密码</label>
                <div class="password-wrap">
                    <input class="input" id="confirmPassword" name="confirmPassword" type="password" maxlength="50"
                           autocomplete="new-password" required data-validate="confirmPassword">
                    <button class="password-toggle" type="button" data-password-toggle="confirmPassword" aria-label="显示密码">显示</button>
                </div>
                <span class="field-error" data-error-for="confirmPassword"><c:out value='${formErrors.confirmPassword}'/></span>
            </div>

            <div class="field" data-field="agreed">
                <label class="checkbox" for="agreed">
                    <input id="agreed" type="checkbox" name="agreed" value="true" data-validate="agreed" ${agreed ? 'checked' : ''}>
                    <span>我已阅读并同意
                        <a href="${ctx}/terms" target="_blank" rel="noopener">《用户协议》</a>与
                        <a href="${ctx}/privacy" target="_blank" rel="noopener">《隐私政策》</a>
                    </span>
                </label>
                <span class="field-error" data-error-for="agreed"><c:out value='${formErrors.agreed}'/></span>
            </div>

            <button class="button button-primary button-block" type="submit" data-submit>注册账号</button>
            <p class="login-alt">已有账号？<a href="${ctx}/login">返回登录</a></p>
        </form>
    </section>
</div>
<script src="${ctx}/assets/js/register.js"></script>
<%@ include file="common/footer.jspf" %>
