package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.util.Set;

public class ScriptService {
    private static final Set<String> DIFFICULTIES = Sets.of("EASY", "MEDIUM", "HARD");
    private static final Set<String> STATUSES = Sets.of("PENDING", "ACTIVE", "INACTIVE");
    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public long save(ScriptBook book, User operator, String ip) {
        boolean creating = book.getId() == 0;
        if (!operator.hasRole("ADMIN")) {
            // 店长新增剧本须经过管理员审核；编辑时不能自行改变审核/上下架状态。
            if (creating) {
                book.setStatus("PENDING");
            } else {
                book.setStatus(scriptDao.findById(book.getId())
                        .map(ScriptBook::getStatus)
                        .orElseThrow(() -> new BusinessException("剧本不存在")));
            }
        }
        validate(book);
        if (scriptDao.codeExists(book.getScriptCode(), book.getId())) throw new BusinessException("剧本编码已存在");
        long id;
        if (creating) {
            id = scriptDao.insert(book);
        } else {
            scriptDao.update(book);
            id = book.getId();
        }
        logDao.insert(operator.getId(), creating ? "SCRIPT_CREATE" : "SCRIPT_UPDATE", "SCRIPT", id,
                "剧本编码=" + book.getScriptCode() + ", 名称=" + book.getTitle(), ip);
        return id;
    }

    public void changeStatus(long id, String status, User operator, String ip) {
        if (!operator.hasRole("ADMIN")) throw new BusinessException("仅管理员可以审核或变更剧本状态");
        Validators.oneOf(status, STATUSES, "剧本状态");
        scriptDao.updateStatus(id, status);
        logDao.insert(operator.getId(), "SCRIPT_STATUS_" + status, "SCRIPT", id, "剧本状态变更为" + status, ip);
    }

    private void validate(ScriptBook book) {
        book.setScriptCode(Validators.required(book.getScriptCode(), "剧本编码", 30));
        book.setTitle(Validators.required(book.getTitle(), "剧本名称", 120));
        book.setGenre(Validators.required(book.getGenre(), "题材", 50));
        Validators.positive(book.getMinPlayers(), "最少人数");
        if (book.getMaxPlayers() < book.getMinPlayers()) throw new BusinessException("最多人数不能小于最少人数");
        Validators.positive(book.getDurationMinutes(), "预计时长");
        book.setDifficulty(Validators.oneOf(book.getDifficulty(), DIFFICULTIES, "难度"));
        book.setStatus(Validators.oneOf(book.getStatus(), STATUSES, "状态"));
        book.setTags(Validators.optional(book.getTags(), "标签", 255));
        book.setDescription(Validators.optional(book.getDescription(), "简介", 5000));
        book.setCoverPath(Validators.optional(book.getCoverPath(), "封面路径", 255));
    }
}
