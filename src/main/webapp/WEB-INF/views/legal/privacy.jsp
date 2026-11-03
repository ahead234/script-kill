<%@ include file="../common/header.jspf" %>
<article class="panel legal-page">
    <h1>隐私政策</h1>
    <p class="muted small">最后更新：2026-11-03</p>

    <h2>一、我们收集的信息</h2>
    <p>注册时收集用户名与手机号；预约时收集联系人姓名、联系电话与备注；评价时收集你提交的评分与文字内容。</p>

    <h2>二、信息用途</h2>
    <p>上述信息仅用于账号识别、场次预约、到店核销、评价审核与门店运营统计，不会用于与剧本杀服务无关的用途。</p>

    <h2>三、密码与安全</h2>
    <p>密码使用 BCrypt 单向加密存储，系统不会保存明文密码，也无法查看你的原始密码。所有表单提交均带 CSRF 校验令牌。</p>

    <h2>四、信息共享</h2>
    <p>除法律法规要求外，我们不会向第三方共享你的个人信息。公开展示的评价仅包含显示名称与评价内容。</p>

    <h2>五、你的权利</h2>
    <p>你可以联系门店修改或停用账号，或申请删除已提交的评价内容。</p>

    <p><a class="button button-secondary" href="${ctx}/register">返回注册</a></p>
</article>
<%@ include file="../common/footer.jspf" %>
