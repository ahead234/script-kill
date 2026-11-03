package com.example.scriptbooking.interceptor;

import com.example.scriptbooking.model.User;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 登录校验、角色校验与 CSRF 校验的 Struts 拦截器。
 * 取代原先写在 SecurityFilter 里的认证逻辑，请求是否放行由 struts.xml 的拦截器栈决定。
 */
public class AuthInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = 1L;

    /** 允许访问的角色，逗号分隔；为空表示不限制登录状态。 */
    private String roles;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        HttpSession session = request.getSession();

        if ("POST".equalsIgnoreCase(request.getMethod())
                && !constantTimeEquals((String) session.getAttribute("csrfToken"), request.getParameter("csrfToken"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "forbidden";
        }

        if (roles == null || roles.trim().isEmpty()) {
            return invocation.invoke();
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "login";
        }
        for (String role : roles.split(",")) {
            if (user.hasRole(role.trim())) {
                return invocation.invoke();
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "forbidden";
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
