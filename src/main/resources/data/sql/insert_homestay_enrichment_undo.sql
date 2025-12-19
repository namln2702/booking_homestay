-- Undo script for src/main/resources/data/sql/insert_homestay_enrichment.sql
-- Removes all additional relationship data tied to the demo homestays (IDs 4001-4020).

DELETE FROM tbl_homestays_list_amenities
WHERE homestay_address_id BETWEEN 4001 AND 4020
  AND list_amenities_id BETWEEN 6001 AND 6006;

DELETE FROM tbl_homestays_list_facilities
WHERE homestay_address_id BETWEEN 4001 AND 4020
  AND list_facilities_id IN (
      SELECT id
      FROM tbl_facilities
      WHERE name IN (
          'Wi-Fi miễn phí',
          'Điều hòa',
          'TV thông minh',
          'Máy giặt',
          'Máy sấy',
          'Bãi đỗ xe riêng',
          'Bàn làm việc riêng',
          'Hồ bơi',
          'View nhìn ra núi',
          'Cho phép mang thú cưng'
      )
  );

DELETE FROM tbl_homestay_rules
WHERE id BETWEEN 940001 AND 940060;

DELETE FROM tbl_homestay_image
WHERE id BETWEEN 970001 AND 970060;

DELETE FROM tbl_reviews
WHERE id BETWEEN 86001 AND 86016;

DELETE FROM tbl_customer
WHERE user_id IN (3101,3102,3103,3104,3105,3106,3107,3108);

DELETE FROM tbl_users
WHERE id IN (3101,3102,3103,3104,3105,3106,3107,3108);

DELETE FROM tbl_homestay_daily_prices
WHERE id BETWEEN 980001 AND 980320;

DELETE FROM tbl_price_per_days
WHERE id BETWEEN 7201 AND 7216;

DELETE FROM tbl_amenities
WHERE id BETWEEN 6001 AND 6006;

WITH baseline_deposits(address_id, deposit_percent) AS (
    VALUES
        (4001, 30),
        (4002, 20),
        (4003, 35),
        (4004, 25),
        (4005, 15),
        (4006, 15),
        (4007, 20),
        (4008, 30),
        (4009, 40),
        (4010, 35),
        (4011, 25),
        (4012, 20),
        (4013, 30),
        (4014, 20),
        (4015, 35),
        (4016, 30),
        (4017, 25),
        (4018, 20),
        (4019, 15),
        (4020, 35)
)
UPDATE tbl_homestays AS h
SET advanced_payment = bd.deposit_percent,
    updated_at = CURRENT_TIMESTAMP
FROM baseline_deposits bd
WHERE h.address_id = bd.address_id;

WITH baseline_ratings(address_id, rating) AS (
    VALUES
        (4001, 4.8),
        (4002, 4.6),
        (4003, 4.9),
        (4004, 4.7),
        (4005, 4.5),
        (4006, 4.4),
        (4007, 4.6),
        (4008, 4.8),
        (4009, 4.9),
        (4010, 4.7),
        (4011, 4.6),
        (4012, 4.5),
        (4013, 4.9),
        (4014, 4.8),
        (4015, 4.9),
        (4016, 4.7),
        (4017, 4.8),
        (4018, 4.6),
        (4019, 4.5),
        (4020, 4.9)
)
UPDATE tbl_homestays AS h
SET rating = br.rating,
    updated_at = CURRENT_TIMESTAMP
FROM baseline_ratings br
WHERE h.address_id = br.address_id;
