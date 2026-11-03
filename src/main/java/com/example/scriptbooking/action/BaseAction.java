package com.example.scriptbooking.action;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.WebUtil;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Struts Action 基类。
 * 统一提供会话访问、当前登录用户、CSRF 令牌、Flash 提示以及由 struts.xml 渲染的跳转结果。
 */
public abstract class BaseAction extends ActionSupport implements Preparable {
    /** 与 struts.xml 中 global-results 的 redirect 结果对应。 */
    public static final String REDIRECT = "redirect";

    private static final SecureRandom RANDOM = new SecureRandom();

    private String redirectUrl;
    private String pageTitle;
    private String error;
    private String flashMessage;
    private String flashType;
    private Integer page;

    /**
     * 在参数绑定之前执行：准备 CSRF 令牌，并把上一个请求留下的提示消息取到当前 Action。
     */
    @Override
    public void prepare() {
        HttpSession session = session();
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", newToken());
        }
        Object message = session.getAttribute("flashMessage");
        if (message != null) {
            flashMessage = String.valueOf(message);
            Object type = session.getAttribute("flashType");
            flashType = type == null ? "info" : String.valueOf(type);
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashType");
        }
    }

    /** 生成随机令牌，用于 CSRF 与一次性表单令牌。 */
    protected String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    protected HttpServletRequest request() {
        return ServletActionContext.getRequest();
    }

    protected HttpServletResponse response() {
        return ServletActionContext.getResponse();
    }

    protected HttpSession session() {
        return request().getSession();
    }

    protected User currentUser() {
        return (User) session().getAttribute("currentUser");
    }

    protected String clientIp() {
        return WebUtil.clientIp(request());
    }

    /** 写入一次性提示，下一次请求由 prepare() 读出。 */
    protected void flash(String type, String message) {
        HttpSession session = session();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    /** 返回 redirect 结果，最终地址由 struts.xml 的 result 配置完成跳转。 */
    protected String redirectTo(String url) {
        this.redirectUrl = url;
        return REDIRECT;
    }

    protected String notFound() throws IOException {
        response().sendError(HttpServletResponse.SC_NOT_FOUND);
        return NONE;
    }

    protected String badRequest(String message) throws IOException {
        response().sendError(HttpServletResponse.SC_BAD_REQUEST, message);
        return NONE;
    }

    protected String methodNotAllowed() throws IOException {
        response().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return NONE;
    }

    protected String forbid() throws IOException {
        response().sendError(HttpServletResponse.SC_FORBIDDEN);
        return NONE;
    }

    protected boolean isPost() {
        return "POST".equalsIgnoreCase(request().getMethod());
    }

    protected boolean isGet() {
        return "GET".equalsIgnoreCase(request().getMethod());
    }

    protected long requiredLong(Long value, String name) {
        if (value == null) throw new BusinessException("参数 " + name + " 不正确");
        return value;
    }

    protected int requiredInt(Integer value, String name) {
        if (value == null) throw new BusinessException("参数 " + name + " 不正确");
        return value;
    }

    /** 与服务端原有的分页参数规则保持一致：缺省 1，范围 1~100000。 */
    protected int pageNo() {
        return page == null ? 1 : Math.max(1, Math.min(100000, page));
    }

    /** 解析表单提交的 datetime-local 值。 */
    protected java.time.LocalDateTime dateTimeParam(String value, String name) {
        try {
            return java.time.LocalDateTime.parse(value);
        } catch (java.time.format.DateTimeParseException | NullPointerException e) {
            throw new BusinessException("时间参数 " + name + " 不正确");
        }
    }

    /** 解析可选的日期筛选参数。 */
    protected java.time.LocalDate optionalDateParam(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return java.time.LocalDate.parse(value.trim());
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException("日期参数 date 不正确");
        }
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    protected void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

    public String getFlashMessage() {
        return flashMessage;
    }

    public String getFlashType() {
        return flashType;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
