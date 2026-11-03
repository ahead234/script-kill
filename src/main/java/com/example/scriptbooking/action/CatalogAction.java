package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.ReviewDao;
import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.model.Review;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.util.Page;

import java.util.List;

/**
 * 前台剧本与场次浏览：发现剧本、剧本详情、下一场故事、场次详情。
 */
public class CatalogAction extends BaseAction {
    /** 与前台四列卡片栅格对齐，保证分页时首页先铺满整行再进入下一页。 */
    private static final int PAGE_SIZE = 8;

    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final SessionDao sessionDao = new SessionDao();
    private final ReviewDao reviewDao = new ReviewDao();

    private String keyword;
    private String genre;
    private Long id;

    private Page<ScriptBook> result;
    private ScriptBook book;
    private PlaySession playSession;
    private List<PlaySession> sessions;
    private List<Review> reviews;

    public String list() {
        result = scriptDao.findPage(keyword, genre, "ACTIVE", pageNo(), PAGE_SIZE, true);
        setPageTitle("发现剧本");
        return SUCCESS;
    }

    public String scriptDetail() throws Exception {
        try {
            long scriptId = requiredLong(id, "id");
            ScriptBook found = scriptDao.findById(scriptId).orElse(null);
            if (found == null || !"ACTIVE".equals(found.getStatus())) {
                return notFound();
            }
            book = found;
            sessions = sessionDao.findAvailableByScript(scriptId);
            reviews = reviewDao.findPublishedByScript(scriptId);
            setPageTitle(found.getTitle());
            return SUCCESS;
        } catch (BusinessException e) {
            return badRequest(e.getMessage());
        }
    }

    public String upcoming() {
        sessions = sessionDao.findUpcoming(24);
        setPageTitle("下一场故事");
        return SUCCESS;
    }

    public String sessionDetail() throws Exception {
        try {
            playSession = sessionDao.findById(requiredLong(id, "id")).orElse(null);
            if (playSession == null) {
                return notFound();
            }
            setPageTitle("场次详情");
            return SUCCESS;
        } catch (BusinessException e) {
            return badRequest(e.getMessage());
        }
    }

    public Page<ScriptBook> getResult() {
        return result;
    }

    public ScriptBook getBook() {
        return book;
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    public List<PlaySession> getSessions() {
        return sessions;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
