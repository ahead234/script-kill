package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.service.AuthService;

import javax.servlet.http.HttpSession;

/**
 * 登录与退出。GET /login 展示表单，POST /login 校验账号，POST /logout 注销会话。
 */
public class AuthAction extends BaseAction {
    private final AuthService authService = new AuthService();
    private final OperationLogDao logDao = new OperationLogDao();

    private String username;
    private String password;

    @Override
    public String execute() throws Exception {
        if (isPost()) {
            return login();
        }
        if (!isGet()) {
            return methodNotAllowed();
        }
        User user = currentUser();
        if (user != null) {
            return redirectTo(user.homePath());
        }
        setPageTitle("登录");
        return SUCCESS;
    }

    public String login() throws Exception {
        try {
            User user = authService.authenticate(username, password);
            request().changeSessionId();
            HttpSession session = request().getSession();
            session.setAttribute("currentUser", user);
            session.removeAttribute("csrfToken");
            logDao.insert(user.getId(), "LOGIN", "USER", user.getId(), "用户登录", clientIp());
            return redirectTo(user.homePath());
        } catch (BusinessException e) {
            setError(e.getMessage());
            setPageTitle("登录");
            return INPUT;
        }
    }

    public String logout() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        User user = currentUser();
        if (user != null) {
            logDao.insert(user.getId(), "LOGOUT", "USER", user.getId(), "用户退出", clientIp());
        }
        HttpSession session = request().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return redirectTo("/login");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
