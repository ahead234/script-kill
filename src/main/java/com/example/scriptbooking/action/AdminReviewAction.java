package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.ReviewDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.service.ReviewService;

/**
 * 后台评价审核：待审核列表与发布/隐藏处理。
 */
public class AdminReviewAction extends BaseAction {
    private final ReviewDao reviewDao = new ReviewDao();
    private final ReviewService reviewService = new ReviewService();

    private String status;
    private String reason;
    private Long id;

    private Object result;

    public String list() {
        result = reviewDao.findPage(status, pageNo(), 10);
        setPageTitle("评价审核");
        return SUCCESS;
    }

    public String moderate() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        try {
            reviewService.moderate(requiredLong(id, "id"), status, reason, currentUser(), clientIp());
            flash("success", "评价审核结果已保存");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/admin/reviews");
    }

    public Object getResult() {
        return result;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
