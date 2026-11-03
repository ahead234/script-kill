package com.example.scriptbooking.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * HTTP 层通用处理：请求编码、安全响应头，以及 /uploads 下上传封面的登录保护。
 * 登录校验、角色校验与 CSRF 校验已迁移到 Struts 的 AuthInterceptor。
 */
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        request.setCharacterEncoding("UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; form-action 'self'; frame-ancestors 'none'");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        /*
         * 静态文件不要设置响应字符集：一旦设置了字符集，Tomcat 的 DefaultServlet 会把 text/* 资源
         * 按平台默认编码（GBK）解码后再按 UTF-8 写出，JS/CSS 里的中文会变成乱码。
         * 动态页面（JSP/Servlet）仍需要 UTF-8。
         */
        if (!path.startsWith("/assets/") && !path.startsWith("/uploads/")) {
            response.setCharacterEncoding("UTF-8");
        }

        if (path.startsWith("/uploads/") && !signedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean signedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("currentUser") != null;
    }

    @Override
    public void destroy() {
    }
}
