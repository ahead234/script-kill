package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.RoomDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Room;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 房间业务层。
 * 除字段校验外有两条真实业务规则：
 * 1. 房间编号、房间名全局唯一（数据库唯一键兜底）；
 * 2. 还有未结束排期的房间不能转检修或停用，必须先处理场次。
 */
public class RoomService {
    private static final Set<String> STATUSES = Sets.of("ACTIVE", "MAINTENANCE", "DISABLED");
    private static final Pattern ROOM_CODE = Pattern.compile("^[A-Z]{1,3}[0-9]{2,4}$");

    private final RoomDao roomDao = new RoomDao();
    private final SessionDao sessionDao = new SessionDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public long save(Room room, User operator, String ip) {
        validate(room);
        boolean creating = room.getId() == 0;
        if (creating) {
            if (roomDao.codeExists(room.getRoomCode(), 0)) throw new BusinessException("房间编号已存在");
            if (roomDao.nameExists(room.getRoomName(), 0)) throw new BusinessException("房间名称已存在");
        } else {
            Room current = roomDao.findById(room.getId()).orElseThrow(() -> new BusinessException("房间不存在"));
            // 房间名与编号是场次排期的关联依据，本阶段只读，避免改名后历史排期指向错误房间
            room.setRoomCode(current.getRoomCode());
            room.setRoomName(current.getRoomName());
            if (!current.getStatus().equals(room.getStatus())) {
                ensureNoUnfinishedSession(room.getRoomName(), room.getStatus());
            }
        }
        long id;
        if (creating) {
            id = roomDao.insert(room);
        } else {
            roomDao.update(room);
            id = room.getId();
        }
        logDao.insert(operator.getId(), creating ? "ROOM_CREATE" : "ROOM_UPDATE", "ROOM", id,
                "房间=" + room.getRoomCode() + "/" + room.getRoomName() + "，容量=" + room.getCapacity(), ip);
        return id;
    }

    public void changeStatus(long roomId, String status, User operator, String ip) {
        Room room = roomDao.findById(roomId).orElseThrow(() -> new BusinessException("房间不存在"));
        String target = Validators.oneOf(status, STATUSES, "房间状态");
        if (room.getStatus().equals(target)) throw new BusinessException("房间已处于该状态");
        ensureNoUnfinishedSession(room.getRoomName(), target);
        roomDao.updateStatus(roomId, target);
        logDao.insert(operator.getId(), "ROOM_STATUS", "ROOM", roomId,
                "房间=" + room.getRoomCode() + "，" + room.getStatus() + "→" + target, ip);
    }

    /** 非可用状态（检修/停用）下，房间仍有未结束排期时拒绝变更。 */
    private void ensureNoUnfinishedSession(String roomName, String targetStatus) {
        if ("ACTIVE".equals(targetStatus)) return;
        long unfinished = sessionDao.countUnfinishedByRoom(roomName);
        if (unfinished > 0) {
            String label = "MAINTENANCE".equals(targetStatus) ? "检修" : "停用";
            throw new BusinessException("该房间还有 " + unfinished + " 场未结束的排期，处理完才能转为" + label);
        }
    }

    private void validate(Room room) {
        String code = Validators.required(room.getRoomCode(), "房间编号", 20).toUpperCase();
        if (!ROOM_CODE.matcher(code).matches()) {
            throw new BusinessException("房间编号需为 1-3 位大写字母加 2-4 位数字，例如 R06");
        }
        room.setRoomCode(code);
        room.setRoomName(Validators.required(room.getRoomName(), "房间名称", 60));
        Validators.range(room.getCapacity(), 2, 30, "房间容量");
        room.setLocation(Validators.optional(room.getLocation(), "位置", 80));
        room.setNote(Validators.optional(room.getNote(), "备注", 255));
        room.setStatus(Validators.oneOf(room.getStatus(), STATUSES, "房间状态"));
    }
}
