package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.service.ScriptService;
import com.example.scriptbooking.util.Sets;
import org.apache.struts2.ServletActionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * 后台剧本管理：列表筛选、新增/编辑、封面上传、上下架与审核状态变更。
 */
public class AdminScriptAction extends BaseAction {
    private static final Set<String> COVER_TYPES = Sets.of("jpg", "jpeg", "png", "webp", "gif", "svg");

    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final ScriptService scriptService = new ScriptService();

    private String keyword;
    private String genre;
    private Long id;

    private String scriptCode;
    private String title;
    private String difficulty;
    private String tags;
    private String description;
    private String coverPath;
    private String status;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer durationMinutes;

    private File coverFile;
    private String coverFileFileName;

    private Object result;
    private ScriptBook book;

    public String list() {
        result = scriptDao.findPage(keyword, genre, status, pageNo(), 10, false);
        setPageTitle("剧本管理");
        return SUCCESS;
    }

    public String showForm() throws Exception {
        ScriptBook target = new ScriptBook();
        // 管理员新增直接上架；店长新增进入待审核，审核通过后前台才可见。
        target.setStatus(currentUser().hasRole("ADMIN") ? "ACTIVE" : "PENDING");
        target.setDifficulty("MEDIUM");
        if (id != null) {
            target = scriptDao.findById(id).orElse(null);
            if (target == null) {
                return notFound();
            }
        }
        book = target;
        setPageTitle(target.getId() == 0 ? "新增剧本" : "编辑剧本");
        return SUCCESS;
    }

    public String save() throws Exception {
        ScriptBook submitted = null;
        try {
            submitted = fromRequest();
            String uploaded = saveCover();
            if (uploaded != null) {
                submitted.setCoverPath(uploaded);
            }
            long savedId = scriptService.save(submitted, currentUser(), clientIp());
            flash("success", "剧本保存成功");
            return redirectTo("/admin/scripts/form?id=" + savedId);
        } catch (BusinessException e) {
            setError(e.getMessage());
            try {
                book = fromRequest();
            } catch (RuntimeException ignored) {
                // 表单参数不完整时保留空表单，错误提示已经写入 error
            }
            setPageTitle("剧本表单");
            return INPUT;
        }
    }

    public String changeStatus() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        try {
            scriptService.changeStatus(requiredLong(id, "id"), status, currentUser(), clientIp());
            flash("success", "剧本状态已更新");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/admin/scripts");
    }

    private ScriptBook fromRequest() {
        ScriptBook target = new ScriptBook();
        if (id != null) {
            target.setId(id);
        }
        target.setScriptCode(scriptCode);
        target.setTitle(title);
        target.setGenre(genre);
        target.setMinPlayers(requiredInt(minPlayers, "minPlayers"));
        target.setMaxPlayers(requiredInt(maxPlayers, "maxPlayers"));
        target.setDurationMinutes(requiredInt(durationMinutes, "durationMinutes"));
        target.setDifficulty(difficulty);
        target.setTags(tags);
        target.setDescription(description);
        target.setCoverPath(coverPath);
        target.setStatus(status);
        return target;
    }

    /**
     * 保存上传的封面图片到部署目录 /uploads/covers/，返回站点内相对路径。
     * 文件名由服务端重新生成，不使用用户上传的原始文件名。
     */
    private String saveCover() {
        if (coverFile == null) return null;
        String ext = extOf(coverFileFileName);
        if (!COVER_TYPES.contains(ext)) {
            throw new BusinessException("封面仅支持 jpg / png / webp / gif / svg 图片");
        }
        File folder = new File(ServletActionContext.getServletContext().getRealPath("/uploads/covers"));
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new BusinessException("封面目录创建失败");
        }
        String name = "script-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "." + ext;
        try {
            Files.copy(coverFile.toPath(), new File(folder, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("封面保存失败，请重试");
        }
        return "/uploads/covers/" + name;
    }

    private String extOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    public Object getResult() {
        return result;
    }

    public ScriptBook getBook() {
        return book;
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

    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setCoverFile(File coverFile) {
        this.coverFile = coverFile;
    }

    public void setCoverFileFileName(String coverFileFileName) {
        this.coverFileFileName = coverFileFileName;
    }
}
