-- Additional relational data for the Vietnamese homestay demo set.
-- The goal is to keep the existing 20 sample homestays rich enough for manual
-- QA and automated tests by wiring them to amenities, nightly prices, rules,
-- facilities, and gallery assets.

-- Step 0: tune deposit percentages so the dataset is not stuck at 30%.
WITH deposit_policy AS (
    SELECT
        h.address_id,
        CASE
            WHEN h.category = 'Biệt thự' OR h.base_price >= 1600000 THEN 50
            WHEN h.base_price >= 1350000 THEN 45
            WHEN h.base_price >= 1100000 THEN 40
            WHEN h.base_price BETWEEN 900000 AND 1099999 THEN 35
            WHEN h.base_price BETWEEN 750000 AND 899999 THEN 30
            ELSE 25
        END AS deposit_percent
    FROM tbl_homestays h
    WHERE h.address_id BETWEEN 4001 AND 4020
)
UPDATE tbl_homestays AS h
SET advanced_payment = dp.deposit_percent,
    updated_at = CURRENT_TIMESTAMP
FROM deposit_policy dp
WHERE h.address_id = dp.address_id;

INSERT INTO tbl_amenities (id, name, description, image_url, created_at, updated_at)
VALUES
    (6001, 'Bữa sáng địa phương', 'Suất ăn sáng gồm bún, phở hoặc bánh mì được chuẩn bị với nguyên liệu địa phương mỗi sáng.', 'https://cdn.demo.vn/amenities/breakfast.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6002, 'Hướng dẫn viên bản địa', 'Nhân sự bản địa đồng hành khám phá ngõ nhỏ, quán ăn và văn hóa bản địa.', 'https://cdn.demo.vn/amenities/local-guide.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6003, 'Xe đạp miễn phí', 'Bộ đôi xe đạp địa hình để khách tự do khám phá khu vực xung quanh.', 'https://cdn.demo.vn/amenities/bicycle.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6004, 'Đưa đón sân bay', 'Xe 4-7 chỗ đưa đón sân bay/ga tàu hai chiều, tài xế đã được xác minh.', 'https://cdn.demo.vn/amenities/airport-transfer.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6005, 'Trải nghiệm nấu ăn', 'Lớp nấu ăn theo nhóm nhỏ với nguyên liệu sạch và chef địa phương.', 'https://cdn.demo.vn/amenities/cooking-class.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6006, 'Spa thảo mộc', 'Khu spa mini với tinh dầu và thảo mộc bản địa, phù hợp khách nghỉ dưỡng.', 'https://cdn.demo.vn/amenities/herbal-spa.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

WITH price_calendar AS (
    SELECT *
    FROM (VALUES
        (7201, DATE '2025-12-18', 520000::REAL, 0.95::NUMERIC, FALSE),
        (7202, DATE '2025-12-19', 580000::REAL, 1.05::NUMERIC, TRUE),
        (7203, DATE '2025-12-20', 660000::REAL, 1.18::NUMERIC, TRUE),
        (7204, DATE '2025-12-21', 610000::REAL, 1.12::NUMERIC, TRUE),
        (7205, DATE '2025-12-22', 470000::REAL, 0.90::NUMERIC, FALSE),
        (7206, DATE '2025-12-23', 490000::REAL, 0.94::NUMERIC, FALSE),
        (7207, DATE '2025-12-24', 620000::REAL, 1.02::NUMERIC, TRUE),
        (7208, DATE '2025-12-25', 720000::REAL, 1.28::NUMERIC, TRUE),
        (7209, DATE '2025-12-26', 590000::REAL, 1.08::NUMERIC, TRUE),
        (7210, DATE '2025-12-27', 640000::REAL, 1.15::NUMERIC, TRUE),
        (7211, DATE '2025-12-28', 600000::REAL, 1.10::NUMERIC, TRUE),
        (7212, DATE '2025-12-29', 530000::REAL, 0.96::NUMERIC, FALSE),
        (7213, DATE '2025-12-30', 560000::REAL, 1.02::NUMERIC, TRUE),
        (7214, DATE '2025-12-31', 750000::REAL, 1.30::NUMERIC, TRUE),
        (7215, DATE '2026-01-01', 680000::REAL, 1.18::NUMERIC, TRUE),
        (7216, DATE '2026-01-02', 500000::REAL, 0.97::NUMERIC, FALSE)
    ) AS t(price_day_id, nightly_date, reference_price, price_multiplier, is_peak)
)
INSERT INTO tbl_price_per_days (id, day, price, created_at, updated_at)
SELECT price_day_id, nightly_date, reference_price, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM price_calendar
ON CONFLICT (id) DO NOTHING;

WITH target_homestays AS (
    SELECT h.address_id,
           h.base_price,
           h.category,
           a.city
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
),
price_calendar AS (
    SELECT *
    FROM (VALUES
        (7201, DATE '2025-12-18', 0.95::NUMERIC, FALSE),
        (7202, DATE '2025-12-19', 1.05::NUMERIC, TRUE),
        (7203, DATE '2025-12-20', 1.18::NUMERIC, TRUE),
        (7204, DATE '2025-12-21', 1.12::NUMERIC, TRUE),
        (7205, DATE '2025-12-22', 0.90::NUMERIC, FALSE),
        (7206, DATE '2025-12-23', 0.94::NUMERIC, FALSE),
        (7207, DATE '2025-12-24', 1.02::NUMERIC, TRUE),
        (7208, DATE '2025-12-25', 1.28::NUMERIC, TRUE),
        (7209, DATE '2025-12-26', 1.08::NUMERIC, TRUE),
        (7210, DATE '2025-12-27', 1.15::NUMERIC, TRUE),
        (7211, DATE '2025-12-28', 1.10::NUMERIC, TRUE),
        (7212, DATE '2025-12-29', 0.96::NUMERIC, FALSE),
        (7213, DATE '2025-12-30', 1.02::NUMERIC, TRUE),
        (7214, DATE '2025-12-31', 1.30::NUMERIC, TRUE),
        (7215, DATE '2026-01-01', 1.18::NUMERIC, TRUE),
        (7216, DATE '2026-01-02', 0.97::NUMERIC, FALSE)
    ) AS t(price_day_id, nightly_date, price_multiplier, is_peak)
),
daily_rows AS (
    SELECT
        980000 + ROW_NUMBER() OVER (ORDER BY th.address_id, pc.price_day_id) AS synthetic_id,
        CAST(ROUND(th.base_price * pc.price_multiplier) AS REAL) AS nightly_price,
        CASE
            WHEN pc.is_peak AND th.base_price >= 1300000 THEN TRUE
            WHEN pc.is_peak AND th.base_price BETWEEN 900000 AND 1299999 THEN (th.address_id % 2 = 0)
            ELSE FALSE
        END AS is_booked,
        th.address_id AS homestay_id,
        pc.price_day_id AS price_per_day_id
    FROM target_homestays th
    CROSS JOIN price_calendar pc
)
INSERT INTO tbl_homestay_daily_prices (id, price, is_booked, homestay_id, price_per_day_id, created_at, updated_at)
SELECT synthetic_id, nightly_price, is_booked, homestay_id, price_per_day_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM daily_rows
ON CONFLICT (id) DO NOTHING;

WITH target_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
),
image_variants AS (
    SELECT *
    FROM (VALUES
        (1, 'exterior', TRUE),
        (2, 'living-room', FALSE),
        (3, 'bedroom', FALSE)
    ) AS v(variant_order, variant_key, is_primary)
),
gallery_rows AS (
    SELECT
        970000 + ROW_NUMBER() OVER (ORDER BY th.address_id, iv.variant_order) AS synthetic_id,
        CONCAT('https://cdn.demo.vn/homestays/', th.address_id, '/', iv.variant_key, '.jpg') AS image_url,
        iv.is_primary,
        th.address_id AS homestay_id
    FROM target_homestays th
    JOIN image_variants iv ON TRUE
)
INSERT INTO tbl_homestay_image (id, image_url, is_primary, homestay_id, created_at, updated_at)
SELECT synthetic_id, image_url, is_primary, homestay_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM gallery_rows
ON CONFLICT (id) DO NOTHING;

WITH target_homestays AS (
    SELECT h.address_id,
           a.city,
           h.category
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
),
rule_variants AS (
    SELECT *
    FROM (VALUES
        (1, 'quiet', 'NOT_ALLOW'),
        (2, 'kitchen', 'ALLOW'),
        (3, 'local', 'ALLOW')
    ) AS rv(variant_order, rule_key, rule_type)
),
rule_rows AS (
    SELECT
        940000 + ROW_NUMBER() OVER (ORDER BY th.address_id, rv.variant_order) AS synthetic_id,
        CASE rv.rule_key
            WHEN 'quiet' THEN CONCAT('Giữ yên tĩnh sau ', CASE WHEN th.city IN ('Hà Nội','Đà Nẵng','Huế','Cần Thơ') THEN '22h' ELSE '23h' END, ' để tôn trọng hàng xóm.')
            WHEN 'kitchen' THEN 'Sử dụng bếp chung và rửa sạch toàn bộ dụng cụ sau mỗi lần nấu.'
            WHEN 'local' THEN CONCAT('Liên hệ trước nếu muốn tham gia trải nghiệm bản địa tại ', th.city, '.')
        END AS description,
        rv.rule_type,
        th.address_id AS homestay_id
    FROM target_homestays th
    JOIN rule_variants rv ON TRUE
)
INSERT INTO tbl_homestay_rules (id, description, rule_type, home_id, created_at, updated_at)
SELECT synthetic_id, description, rule_type, homestay_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM rule_rows
ON CONFLICT (id) DO NOTHING;

WITH target_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
),
base_facilities AS (
    SELECT id
    FROM tbl_facilities
    WHERE name IN ('Wi-Fi miễn phí', 'Điều hòa', 'TV thông minh')
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT th.address_id, bf.id
FROM target_homestays th
CROSS JOIN base_facilities bf
ON CONFLICT DO NOTHING;

WITH target_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
      AND category IN ('Căn hộ', 'Nhà phố', 'Studio')
),
laundry_facilities AS (
    SELECT id
    FROM tbl_facilities
    WHERE name IN ('Máy giặt', 'Máy sấy')
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT th.address_id, lf.id
FROM target_homestays th
CROSS JOIN laundry_facilities lf
ON CONFLICT DO NOTHING;

WITH target_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
      AND (max_guest >= 6 OR category IN ('Biệt thự','Nhà phố'))
),
parking_facility AS (
    SELECT id
    FROM tbl_facilities
    WHERE name = 'Bãi đỗ xe riêng'
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT th.address_id, pf.id
FROM target_homestays th
JOIN parking_facility pf ON TRUE
ON CONFLICT DO NOTHING;

WITH target_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND h.category IN ('Căn hộ','Studio')
      AND a.city IN ('Hà Nội','Đà Nẵng','Huế','Cần Thơ')
),
desk_facility AS (
    SELECT id
    FROM tbl_facilities
    WHERE name = 'Bàn làm việc riêng'
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT th.address_id, df.id
FROM target_homestays th
JOIN desk_facility df ON TRUE
ON CONFLICT DO NOTHING;

WITH target_homestays AS (
    SELECT h.address_id,
           h.category,
           a.city
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND (
          (h.category IN ('Biệt thự','Căn hộ') AND a.city IN ('Đà Nẵng','Hội An','Nha Trang','Quy Nhơn','Phú Quốc'))
          OR h.category = 'Biệt thự'
      )
),
pool_facility AS (
    SELECT id
    FROM tbl_facilities
    WHERE name = 'Hồ bơi'
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT th.address_id, pf.id
FROM target_homestays th
JOIN pool_facility pf ON TRUE
ON CONFLICT DO NOTHING;

WITH mountain_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND a.city IN ('Đà Lạt','Sa Pa')
),
mountain_view AS (
    SELECT id
    FROM tbl_facilities
    WHERE name = 'View nhìn ra núi'
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT mh.address_id, mv.id
FROM mountain_homestays mh
JOIN mountain_view mv ON TRUE
ON CONFLICT DO NOTHING;

WITH pet_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
      AND category IN ('Nhà phố','Bungalow')
),
pet_facility AS (
    SELECT id
    FROM tbl_facilities
    WHERE name = 'Cho phép mang thú cưng'
)
INSERT INTO tbl_homestays_list_facilities (homestay_address_id, list_facilities_id)
SELECT ph.address_id, pf.id
FROM pet_homestays ph
JOIN pet_facility pf ON TRUE
ON CONFLICT DO NOTHING;

WITH target_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
),
breakfast AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6001
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT th.address_id, b.id
FROM target_homestays th
JOIN breakfast b ON TRUE
ON CONFLICT DO NOTHING;

WITH culture_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND a.city IN ('Hà Nội','Huế','Hội An','Đà Nẵng')
),
local_guides AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6002
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT ch.address_id, lg.id
FROM culture_homestays ch
JOIN local_guides lg ON TRUE
ON CONFLICT DO NOTHING;

WITH slow_travel_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND a.city IN ('Đà Lạt','Sa Pa','Huế')
),
free_bikes AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6003
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT sth.address_id, fb.id
FROM slow_travel_homestays sth
JOIN free_bikes fb ON TRUE
ON CONFLICT DO NOTHING;

WITH coastal_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND a.city IN ('Đà Nẵng','Hội An','Nha Trang','Quy Nhơn','Phú Quốc')
),
airport_transfer AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6004
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT ch.address_id, at.id
FROM coastal_homestays ch
JOIN airport_transfer at ON TRUE
ON CONFLICT DO NOTHING;

WITH culinary_homestays AS (
    SELECT h.address_id
    FROM tbl_homestays h
    JOIN tbl_addresses a ON a.id = h.address_id
    WHERE h.address_id BETWEEN 4001 AND 4020
      AND a.city IN ('Huế','Hội An','Đà Nẵng','Cần Thơ')
),
cooking_class AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6005
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT ch.address_id, cc.id
FROM culinary_homestays ch
JOIN cooking_class cc ON TRUE
ON CONFLICT DO NOTHING;

WITH premium_homestays AS (
    SELECT address_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
      AND base_price >= 1350000
),
herbal_spa AS (
    SELECT id
    FROM tbl_amenities
    WHERE id = 6006
)
INSERT INTO tbl_homestays_list_amenities (homestay_address_id, list_amenities_id)
SELECT ph.address_id, hs.id
FROM premium_homestays ph
JOIN herbal_spa hs ON TRUE
ON CONFLICT DO NOTHING;

-- Sample guest profiles used for realistic review data
INSERT INTO tbl_users (id, username, email, phone, is_online, avatar_url, age, name, created_at, updated_at)
VALUES
    (3101, 'tran.viet.anh', 'viet.anh@demo.vn', '0903111222', FALSE, 'https://cdn.demo.vn/avatar/customers/viet-anh.jpg', 33, 'Trần Việt Anh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3102, 'pham.hoang.yen', 'hoang.yen@demo.vn', '0904556677', FALSE, 'https://cdn.demo.vn/avatar/customers/hoang-yen.jpg', 29, 'Phạm Hoàng Yến', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3103, 'le.quynh.nhu', 'quynh.nhu@demo.vn', '0389123456', FALSE, 'https://cdn.demo.vn/avatar/customers/quynh-nhu.jpg', 31, 'Lê Quỳnh Như', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3104, 'nguyen.dang.khoa', 'dang.khoa@demo.vn', '0912456677', FALSE, 'https://cdn.demo.vn/avatar/customers/dang-khoa.jpg', 36, 'Nguyễn Đăng Khoa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3105, 'vu.thu.ha', 'thu.ha@demo.vn', '0967234455', FALSE, 'https://cdn.demo.vn/avatar/customers/thu-ha.jpg', 28, 'Vũ Thu Hà', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3106, 'bui.anh.dung', 'anh.dung@demo.vn', '0941223434', FALSE, 'https://cdn.demo.vn/avatar/customers/anh-dung.jpg', 34, 'Bùi Anh Dũng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3107, 'doan.minh.chau', 'minh.chau@demo.vn', '0976321456', FALSE, 'https://cdn.demo.vn/avatar/customers/minh-chau.jpg', 30, 'Đoàn Minh Châu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3108, 'ho.the.loc', 'the.loc@demo.vn', '0932567788', FALSE, 'https://cdn.demo.vn/avatar/customers/the-loc.jpg', 35, 'Hồ Thế Lộc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tbl_customer (user_id, status, date_of_birth, qr_code_url, last_booking, role, created_at, updated_at)
VALUES
    (3101, 'ACTIVE', '1992-05-11', 'https://cdn.demo.vn/qr/customers/3101.png', '2025-11-30', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3102, 'ACTIVE', '1995-08-22', 'https://cdn.demo.vn/qr/customers/3102.png', '2025-11-18', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3103, 'ACTIVE', '1993-02-14', 'https://cdn.demo.vn/qr/customers/3103.png', '2025-10-05', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3104, 'ACTIVE', '1989-09-04', 'https://cdn.demo.vn/qr/customers/3104.png', '2025-09-28', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3105, 'ACTIVE', '1996-11-19', 'https://cdn.demo.vn/qr/customers/3105.png', '2025-12-03', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3106, 'ACTIVE', '1991-07-07', 'https://cdn.demo.vn/qr/customers/3106.png', '2025-11-12', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3107, 'ACTIVE', '1994-04-30', 'https://cdn.demo.vn/qr/customers/3107.png', '2025-11-08', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3108, 'ACTIVE', '1988-03-15', 'https://cdn.demo.vn/qr/customers/3108.png', '2025-10-22', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Reviews capturing actual guest feedback for featured homestays
INSERT INTO tbl_reviews (id, created_at, updated_at, rating, comment, customer_id, homestay_id)
VALUES
    (86001, TIMESTAMP '2025-11-28 10:30:00', TIMESTAMP '2025-11-28 10:30:00', 5, 'Ban công rộng nhìn ra sông Hàn, chủ nhà chuẩn bị welcome drink rất dễ thương.', 3101, 4001),
    (86002, TIMESTAMP '2025-12-02 08:45:00', TIMESTAMP '2025-12-02 08:45:00', 4, 'Ngay trung tâm nên hơi ồn sau 22h nhưng tiện đi bộ ra cầu Rồng.', 3102, 4001),
    (86003, TIMESTAMP '2025-12-05 14:20:00', TIMESTAMP '2025-12-05 14:20:00', 5, 'Gia đình 8 người thoải mái tận hưởng hồ bơi và khu BBQ riêng.', 3103, 4003),
    (86004, TIMESTAMP '2025-11-18 19:10:00', TIMESTAMP '2025-11-18 19:10:00', 4, 'Thiết kế nhà phố đẹp, mất 5 phút đi bộ ra Mỹ Khê.', 3104, 4004),
    (86005, TIMESTAMP '2025-12-08 07:55:00', TIMESTAMP '2025-12-08 07:55:00', 5, 'Biệt thự Hội An nhiều cây xanh, bữa sáng địa phương ngon miệng.', 3105, 4008),
    (86006, TIMESTAMP '2025-11-25 21:05:00', TIMESTAMP '2025-11-25 21:05:00', 5, 'Căn hộ hồ Tây có bếp mở và view hoàng hôn siêu đẹp.', 3106, 4009),
    (86007, TIMESTAMP '2025-12-09 09:00:00', TIMESTAMP '2025-12-09 09:00:00', 4, 'Studio gọn gàng, chủ nhà chuẩn bị sẵn cà phê phin và guide food tour.', 3107, 4010),
    (86008, TIMESTAMP '2025-11-12 18:40:00', TIMESTAMP '2025-11-12 18:40:00', 5, 'Villa Đà Lạt ấm áp, vườn hồng sáng nào cũng thơm.', 3108, 4013),
    (86009, TIMESTAMP '2025-11-20 20:10:00', TIMESTAMP '2025-11-20 20:10:00', 4, 'Đêm xuống hơi lạnh, chủ nhà gửi thêm chăn ngay lập tức.', 3101, 4013),
    (86010, TIMESTAMP '2025-12-01 11:15:00', TIMESTAMP '2025-12-01 11:15:00', 5, 'Lodge Sa Pa nhìn thẳng ra núi, có set trà thảo mộc miễn phí.', 3102, 4017),
    (86011, TIMESTAMP '2025-11-27 16:20:00', TIMESTAMP '2025-11-27 16:20:00', 4, 'Căn hộ Quy Nhơn sát biển, bếp đầy đủ dụng cụ nấu ăn.', 3103, 4018),
    (86012, TIMESTAMP '2025-12-10 12:05:00', TIMESTAMP '2025-12-10 12:05:00', 5, 'Villa Phú Quốc có hồ bơi riêng và BBQ tối rất chill.', 3104, 4020),
    (86013, TIMESTAMP '2025-12-04 15:45:00', TIMESTAMP '2025-12-04 15:45:00', 4, 'Trần Phú Azure có hồ bơi vô cực, thang máy riêng tiện lợi.', 3105, 4015),
    (86014, TIMESTAMP '2025-11-30 17:35:00', TIMESTAMP '2025-11-30 17:35:00', 5, 'An Bàng Santorini trang trí tươi sáng, có xe đạp miễn phí cả ngày.', 3106, 4016),
    (86015, TIMESTAMP '2025-11-22 08:25:00', TIMESTAMP '2025-11-22 08:25:00', 4, 'Nhà ven sông Cần Thơ có hiên uống trà, phù hợp làm việc từ xa.', 3107, 4019),
    (86016, TIMESTAMP '2025-11-16 13:20:00', TIMESTAMP '2025-11-16 13:20:00', 5, 'Sông Hương Classic chuẩn bị sẵn nón bài thơ cho khách, rất đáng yêu.', 3108, 4005)
ON CONFLICT (id) DO NOTHING;

-- Align homestay ratings with the seeded review averages
WITH review_stats AS (
    SELECT homestay_id,
           ROUND(AVG(r.rating)::NUMERIC, 1)::FLOAT AS avg_rating
    FROM tbl_reviews r
    WHERE homestay_id BETWEEN 4001 AND 4020
    GROUP BY homestay_id
)
UPDATE tbl_homestays h
SET rating = rs.avg_rating,
    updated_at = CURRENT_TIMESTAMP
FROM review_stats rs
WHERE h.address_id = rs.homestay_id;
