USE script_booking;

-- 返回结果应为空；若有记录，说明 play_session.booked_count 与有效预约人数不一致。
SELECT
  ps.id,
  ps.session_code,
  ps.booked_count AS cached_booked_count,
  COALESCE(SUM(CASE WHEN b.status IN ('CONFIRMED','CHECKED_IN') THEN b.party_size ELSE 0 END), 0) AS actual_booked_count
FROM play_session ps
LEFT JOIN booking b ON b.session_id = ps.id
GROUP BY ps.id, ps.session_code, ps.booked_count
HAVING cached_booked_count <> actual_booked_count;

-- 仅在确认无并发写入并完成备份后用于修复。
-- UPDATE play_session ps
-- LEFT JOIN (
--   SELECT session_id, SUM(party_size) total
--   FROM booking
--   WHERE status IN ('CONFIRMED','CHECKED_IN')
--   GROUP BY session_id
-- ) x ON x.session_id = ps.id
-- SET ps.booked_count = COALESCE(x.total, 0);
