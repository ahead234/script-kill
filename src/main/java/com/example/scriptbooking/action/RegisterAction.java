package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.service.AuthService;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注册账号。
 * GET /register 渲染表单，POST /register 完成校验与注册。
 * 表单带一次性令牌（registerFormToken），成功后立即作废，避免刷新或重复点击造成重复注册。
 */
public class RegisterAction extends BaseAction {
    private static final String FORM_TOKEN = "registerFormToken";

    private final AuthService authService = new AuthService();
    private final OperationLogDao logDao = new OperationLogDao();

    private String username;
    private String phone;
    private String password;
    private String confirmPassword;
    private boolean agreed;
    private String formToken;
    private final Map<String, String> formErrors = new LinkedHashMap<String, String>();

    @Override
    public void prepare() {
        super.prepare();
        HttpSession session = session();
        if (session.getAttribute(FORM_TOKEN) == null) {
            session.setAttribute(FORM_TOKEN, newToken());
        }
        formToken = (String) session.getAttribute(FORM_TOKEN);
    }

    @Override
    public String execute() throws Exception {
        if (!isPost()) {
            setPageTitle("注册账号");
            return SUCCESS;
        }
        if (!validFormToken()) {
            rotateFormToken();
            setError("表单已过期或已提交，请重新填写后再试");
            setPageTitle("注册账号");
            return INPUT;
        }
        if (!agreed) {
            rotateFormToken();
            formErrors.put("agreed", "请先阅读并同意用户协议和隐私政策");
            setPageTitle("注册账号");
            return INPUT;
        }
        try {
            User user = authService.register(username, phone, password, confirmPassword);
            logDao.insert(user.getId(), "REGISTER", "USER", user.getId(), "新用户注册", clientIp());
            session().removeAttribute(FORM_TOKEN);
            flash("success", "注册成功，请使用「" + user.getUsername() + "」登录");
            return redirectTo("/login");
        } catch (BusinessException e) {
            rotateFormToken();
            setError(e.getMessage());
            assignFieldError(e.getMessage());
            setPageTitle("注册账号");
            return INPUT;
        }
    }

    private boolean validFormToken() {
        Object expected = session().getAttribute(FORM_TOKEN);
        String submitted = request().getParameter("formToken");
        if (expected == null || submitted == null) return false;
        return MessageDigest.isEqual(
                expected.toString().getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }

    private void rotateFormToken() {
        formToken = newToken();
        session().setAttribute(FORM_TOKEN, formToken);
    }

    /** 把服务端错误落到具体输入框，方便页面就地提示。 */
    private void assignFieldError(String message) {
        if (message.contains("两次")) {
            formErrors.put("confirmPassword", message);
        } else if (message.contains("用户名")) {
            formErrors.put("username", message);
        } else if (message.contains("手机号")) {
            formErrors.put("phone", message);
        } else if (message.contains("密码")) {
            formErrors.put("password", message);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isAgreed() {
        return agreed;
    }

    public void setAgreed(boolean agreed) {
        this.agreed = agreed;
    }

    public String getFormToken() {
        return formToken;
    }

    public Map<String, String> getFormErrors() {
        return formErrors;
    }
}

