package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.RoomDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Room;
import com.example.scriptbooking.service.RoomService;
import com.example.scriptbooking.util.Sets;

import java.util.Set;

/**
 * 后台房间管理：列表筛选、新增/编辑、状态变更。
 * 房间与场次通过名称关联，因此编辑时编号与名称只读，只允许调整容量、位置、备注与状态。
 */
public class AdminRoomAction extends BaseAction {
    private static final Set<String> STATUSES = Sets.of("ACTIVE", "MAINTENANCE", "DISABLED");

    private final RoomDao roomDao = new RoomDao();
    private final RoomService roomService = new RoomService();

    private String keyword;
    private String status;
    private Long id;

    private String roomCode;
    private String roomName;
    private Integer capacity;
    private String location;
    private String note;

    private Object result;
    private Room room;

    public String list() {
        result = roomDao.findPage(keyword, status, pageNo(), 10);
        setPageTitle("房间管理");
        return SUCCESS;
    }

    public String showForm() throws Exception {
        if (id == null || id == 0) {
            Room target = new Room();
            target.setStatus("ACTIVE");
            target.setCapacity(6);
            room = target;
            setPageTitle("新增房间");
        } else {
            Room target = roomDao.findById(id).orElse(null);
            if (target == null) return notFound();
            room = target;
            setPageTitle("编辑房间");
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        Room submitted = new Room();
        submitted.setId(id == null ? 0 : id);
        try {
            bindForm(submitted);
            long savedId = roomService.save(submitted, currentUser(), clientIp());
            flash("success", "房间保存成功");
            return redirectTo("/admin/rooms/form?id=" + savedId);
        } catch (BusinessException e) {
            setError(e.getMessage());
            // 只回显已成功解析的字段，不再二次解析一遍表单
            room = submitted;
            setPageTitle(submitted.getId() == 0 ? "新增房间" : "编辑房间");
            return INPUT;
        }
    }

    public String changeStatus() throws Exception {
        if (!isPost()) return methodNotAllowed();
        try {
            roomService.changeStatus(requiredLong(id, "id"), status, currentUser(), clientIp());
            flash("success", "房间状态已更新");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/admin/rooms");
    }

    /** 边解析边写入目标对象，某个字段不合法时前面已解析的字段仍可用于回显。 */
    private void bindForm(Room target) {
        target.setRoomCode(roomCode);
        target.setRoomName(roomName);
        target.setLocation(location);
        target.setNote(note);
        target.setStatus(status);
        if (capacity == null) throw new BusinessException("房间容量不能为空");
        target.setCapacity(capacity);
    }

    public Object getResult() {
        return result;
    }

    public Room getRoom() {
        return room;
    }

    public Set<String> getStatuses() {
        return STATUSES;
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

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
