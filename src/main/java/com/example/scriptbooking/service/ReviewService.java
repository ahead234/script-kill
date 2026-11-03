package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.ReviewDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.model.Review;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class ReviewService {
    private static final Set<String> MODERATION_STATUSES = Sets.of("PUBLISHED", "HIDDEN");
    private final ReviewDao reviewDao = new ReviewDao();
    private final BookingDao bookingDao = new BookingDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public long save(Review review, User player, String ip) {
        Booking booking = bookingDao.findById(review.getBookingId()).orElseThrow(() -> new BusinessException("预约不存在"));
        if (booking.getUserId() != player.getId()) throw new BusinessException("不能评价他人的预约");
        if (!"COMPLETED".equals(booking.getStatus())) throw new BusinessException("只有已完成预约可以评价");
        validate(review);
        review.setUserId(player.getId());
        Optional<Review> existing = reviewDao.findByBooking(review.getBookingId());
        long id;
        if (existing.isPresent()) {
            if (review.getId() == 0 || review.getId() != existing.get().getId()) throw new BusinessException("该预约已经评价过");
            reviewDao.update(review);
            id = review.getId();
            logDao.insert(player.getId(), "REVIEW_UPDATE", "REVIEW", id, "玩家修改待审核评价", ip);
        } else {
            id = reviewDao.insert(review);
            logDao.insert(player.getId(), "REVIEW_CREATE", "REVIEW", id, "玩家提交评价", ip);
        }
        return id;
    }

    public void moderate(long reviewId, String status, String reason, User operator, String ip) {
        Validators.oneOf(status, MODERATION_STATUSES, "审核状态");
        String moderationReason = Validators.required(reason, "审核原因", 500);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                reviewDao.moderate(connection, reviewId, status, moderationReason, operator.getId());
                logDao.insert(connection, operator.getId(), "REVIEW_" + status, "REVIEW", reviewId,
                        "审核结果=" + status + ", 原因=" + moderationReason, ip);
                connection.commit();
            } catch (Exception e) {
                try { connection.rollback(); } catch (SQLException ignored) { }
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("评价审核失败", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("评价审核失败", e);
        }
    }

    private void validate(Review review) {
        Validators.range(review.getRating(), 1, 5, "总评分");
        Validators.optionalRating(review.getStoryRating(), "剧情评分");
        Validators.optionalRating(review.getDmRating(), "DM评分");
        Validators.optionalRating(review.getAtmosphereRating(), "氛围评分");
        review.setContent(Validators.required(review.getContent(), "评价内容", 2000));
    }
}
