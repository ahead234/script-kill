package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.ReviewDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.model.Review;
import com.example.scriptbooking.service.ReviewService;

import java.util.List;

/**
 * 玩家评价：我的评价、提交或修改待审核评价。
 */
public class ReviewAction extends BaseAction {
    private final ReviewDao reviewDao = new ReviewDao();
    private final BookingDao bookingDao = new BookingDao();
    private final ReviewService reviewService = new ReviewService();

    private Long bookingId;
    private Long id;
    private Integer rating;
    private Integer storyRating;
    private Integer dmRating;
    private Integer atmosphereRating;
    private String content;

    private List<Review> reviews;
    private Review review;
    private Booking booking;

    public String list() {
        reviews = reviewDao.findForUser(currentUser().getId());
        setPageTitle("我的评价");
        return SUCCESS;
    }

    public String create() throws Exception {
        return isPost() ? save() : showForm();
    }

    private String showForm() throws Exception {
        long targetBookingId = requiredLong(bookingId, "bookingId");
        Booking owned = ownedBooking(targetBookingId);
        if (!"COMPLETED".equals(owned.getStatus())) {
            flash("danger", "只有已完成预约可以评价");
            return redirectTo("/bookings/detail?id=" + targetBookingId);
        }
        Review existing = reviewDao.findByBooking(targetBookingId).orElseGet(Review::new);
        if (existing.getId() != 0 && !"PENDING".equals(existing.getStatus())) {
            flash("danger", "仅待审核评价允许修改");
            return redirectTo("/my/reviews");
        }
        existing.setBookingId(targetBookingId);
        booking = owned;
        review = existing;
        setPageTitle(existing.getId() == 0 ? "提交评价" : "修改评价");
        return SUCCESS;
    }

    private String save() throws Exception {
        Review submitted = null;
        try {
            submitted = fromRequest();
            reviewService.save(submitted, currentUser(), clientIp());
            flash("success", "评价已提交，等待审核");
            return redirectTo("/my/reviews");
        } catch (BusinessException e) {
            setError(e.getMessage());
            if (submitted == null) {
                try {
                    submitted = fromRequest();
                } catch (RuntimeException ignored) {
                    submitted = new Review();
                }
            }
            review = submitted;
            try {
                booking = ownedBooking(submitted.getBookingId());
            } catch (RuntimeException ignored) {
                // 预约参数无效时只保留评价表单，错误提示已经写入 error
            }
            setPageTitle("提交评价");
            return INPUT;
        }
    }

    private Booking ownedBooking(long targetBookingId) {
        Booking owned = bookingDao.findById(targetBookingId).orElseThrow(() -> new BusinessException("预约不存在"));
        if (owned.getUserId() != currentUser().getId()) {
            throw new BusinessException("不能操作他人的预约");
        }
        return owned;
    }

    private Review fromRequest() {
        Review submitted = new Review();
        if (id != null) {
            submitted.setId(id);
        }
        submitted.setBookingId(requiredLong(bookingId, "bookingId"));
        submitted.setRating(requiredInt(rating, "rating"));
        submitted.setStoryRating(storyRating);
        submitted.setDmRating(dmRating);
        submitted.setAtmosphereRating(atmosphereRating);
        submitted.setContent(content);
        submitted.setUserId(currentUser().getId());
        return submitted;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Review getReview() {
        return review;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setStoryRating(Integer storyRating) {
        this.storyRating = storyRating;
    }

    public void setDmRating(Integer dmRating) {
        this.dmRating = dmRating;
    }

    public void setAtmosphereRating(Integer atmosphereRating) {
        this.atmosphereRating = atmosphereRating;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
