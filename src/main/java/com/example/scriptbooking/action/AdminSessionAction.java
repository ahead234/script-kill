package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.dao.SessionQuery;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.service.SessionService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台场次排期：列表筛选、新增/编辑、取消场次。
 */
public class AdminSessionAction extends BaseAction {
    private final SessionDao sessionDao = new SessionDao();
    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final SessionService sessionService = new SessionService();

    private String keyword;
    private String status;
    private String date;
    private Long id;

    private String sessionCode;
    private Long scriptId;
    private String startTime;
    private String endTime;
    private Integer capacity;
    private String roomName;
    private String dmName;
    private String unitPrice;
    private String remark;

    private Object result;
    private PlaySession playSession;
    private List<ScriptBook> scripts;

    public String list() {
        result = sessionDao.findPage(new SessionQuery(optionalDateParam(date), keyword, status), pageNo(), 10);
        setPageTitle("场次管理");
        return SUCCESS;
    }

    public String showForm() throws Exception {
        PlaySession target = new PlaySession();
        target.setStatus("DRAFT");
        target.setCapacity(6);
        target.setUnitPrice(BigDecimal.ZERO);
        if (id != null) {
            target = sessionDao.findById(id).orElse(null);
            if (target == null) {
                return notFound();
            }
        }
        prepareForm(target);
        return SUCCESS;
    }

    public String save() throws Exception {
        // 重构前：try 里 fromRequest() 一次，catch 里为了回显又解析一次；
        // 第二次解析在原字段（如结束时间格式）上同样抛错，结果整张表单被清空。
        // 重构后：只解析一次，bindForm 边解析边写入实体，失败时已解析的字段仍可回显。
        PlaySession submitted = new PlaySession();
        try {
            bindForm(submitted);
            long savedId = sessionService.save(submitted, currentUser(), clientIp());
            flash("success", "场次保存成功");
            return redirectTo("/admin/sessions/form?id=" + savedId);
        } catch (BusinessException | NumberFormatException e) {
            setError(e instanceof BusinessException ? e.getMessage() : "数字参数格式不正确");
            prepareForm(submitted);
            return INPUT;
        }
    }

    public String cancel() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        try {
            sessionService.cancel(requiredLong(id, "id"), currentUser(), clientIp());
            flash("success", "场次及其有效预约已取消");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/admin/sessions");
    }

    private void prepareForm(PlaySession target) {
        playSession = target;
        scripts = scriptDao.findActiveOptions();
        setPageTitle(target.getId() == 0 ? "新增场次" : "编辑场次");
    }

    /** 把表单字段逐项写入实体：解析失败的字段抛异常，已写入的字段保留用于页面回显。 */
    private void bindForm(PlaySession target) {
        if (id != null) {
            target.setId(id);
        }
        target.setSessionCode(sessionCode);
        target.setScriptId(requiredLong(scriptId, "scriptId"));
        target.setStartTime(dateTimeParam(startTime, "startTime"));
        target.setEndTime(dateTimeParam(endTime, "endTime"));
        target.setCapacity(requiredInt(capacity, "capacity"));
        target.setRoomName(roomName);
        target.setDmName(dmName);
        target.setUnitPrice(unitPrice == null || unitPrice.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(unitPrice.trim()));
        target.setStatus(status);
        target.setRemark(remark);
    }

    public Object getResult() {
        return result;
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    public List<ScriptBook> getScripts() {
        return scripts;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setDmName(String dmName) {
        this.dmName = dmName;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
