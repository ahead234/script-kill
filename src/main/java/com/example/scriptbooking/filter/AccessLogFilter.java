package com.example.scriptbooking.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.example.scriptbooking.util.WebUtil;
import java.util.logging.Logger;

/**
 * 访问日志过滤器（实验三新增）。
 *
 * 放在 securityFilter 之后、struts2 之前：先由 securityFilter 统一请求编码与安全响应头，
 * 再由本过滤器记录“谁、用什么方法、访问了哪个路径、返回什么状态码、耗时多少、是否已登录”，
 * 最后才交给 Struts 的 Action。过滤器链的顺序由 web.xml 中 filter-mapping 的声明顺序决定。
 *
 * 放行规则：/assets/ 下的静态资源（CSS、JS、图片）只透传不记录，避免日志被静态请求淹没；
 * 其余请求（页面、表单提交、注销）全部记录，用于回归时核对“未登录是否被拦截”。
 */
public class AccessLogFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AccessLogFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        long begin = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            boolean signedIn = WebUtil.isSignedIn(request);
            LOGGER.info(String.format("[access] %s %s -> %d, %d ms, session=%s",
                    request.getMethod(), path, response.getStatus(),
                    System.currentTimeMillis() - begin, signedIn ? "登录" : "匿名"));
        }
    }

    @Override
    public void destroy() {
    }
}
