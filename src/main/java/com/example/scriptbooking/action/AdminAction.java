package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.UserDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.OperationLog;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.service.AdminService;
import com.example.scriptbooking.util.Page;

import java.util.Map;

/**
 * 管理后台：数据报表页、用户管理、操作日志。
 */
public class AdminAction extends BaseAction {
    private final AdminService adminService = new AdminService();
    private final UserDao userDao = new UserDao();
    private final OperationLogDao logDao = new OperationLogDao();

    private String keyword;
    private String status;
    private Long id;

    private Map<String, Object> stats;
    private Page<?> result;

    public String dashboard() {
        stats = adminService.dashboard();
        setPageTitle("管理工作台");
        return SUCCESS;
    }

    public String users() {
        Page<User> pageData = userDao.findPage(keyword, status, pageNo(), 12);
        result = pageData;
        setPageTitle("用户管理");
        return SUCCESS;
    }

    public String logs() {
        Page<OperationLog> pageData = logDao.findPage(keyword, pageNo(), 15);
        result = pageData;
        setPageTitle("操作日志");
        return SUCCESS;
    }

    public String changeUserStatus() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        try {
            adminService.changeUserStatus(requiredLong(id, "id"), status, currentUser(), clientIp());
            flash("success", "用户状态已更新");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/admin/users");
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public Page<?> getResult() {
        return result;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
