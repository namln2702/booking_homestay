-- Seed script for Vietnamese homestay demo data.
-- Creates base person types, five sample hosts, 20 addresses, and
-- 20 homestays with default capacity rows.

INSERT INTO tbl_persons (id, type, created_at, updated_at)
VALUES
    (9101, 'ADULTS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9102, 'CHILDREN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9103, 'BABY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tbl_users (id, username, email, phone, is_online, avatar_url, age, name, created_at, updated_at)
VALUES
    (3001, 'nguyen.minh.khoa', 'khoa.nguyen@demo.vn', '0905123456', FALSE, 'https://cdn.demo.vn/avatar/khoa.jpg', 34, 'Nguyễn Minh Khoa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3002, 'tran.thi.lan', 'lan.tran@demo.vn', '0938456123', FALSE, 'https://cdn.demo.vn/avatar/lan.jpg', 31, 'Trần Thị Lan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3003, 'pham.gia.bao', 'bao.pham@demo.vn', '0978456789', FALSE, 'https://cdn.demo.vn/avatar/bao.jpg', 37, 'Phạm Gia Bảo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3004, 'le.hoang.phuc', 'phuc.le@demo.vn', '0963344556', FALSE, 'https://cdn.demo.vn/avatar/phuc.jpg', 40, 'Lê Hoàng Phúc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3005, 'vo.my.linh', 'linh.vo@demo.vn', '0912345678', FALSE, 'https://cdn.demo.vn/avatar/linh.jpg', 29, 'Võ Mỹ Linh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tbl_hosts (user_id, business_name, qr_code_url, role, status, created_at, updated_at)
VALUES
    (3001, 'Khoa House Đà Nẵng', 'https://cdn.demo.vn/qr/khoa.png', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3002, 'Lan Retreat Việt', 'https://cdn.demo.vn/qr/lan.png', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3003, 'Bảo Saigon Stay', 'https://cdn.demo.vn/qr/bao.png', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3004, 'Phúc Highland Homes', 'https://cdn.demo.vn/qr/phuc.png', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3005, 'Linh Coastal Villas', 'https://cdn.demo.vn/qr/linh.png', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO tbl_addresses (id, address_line, city, state, latitude, longitude, created_at, updated_at)
VALUES
    (4001, '12 Nguyễn Văn Linh', 'Đà Nẵng', 'Hải Châu', '16.0678', '108.2208', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4002, '48 Bạch Đằng', 'Đà Nẵng', 'Thanh Khê', '16.0670', '108.2145', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4003, '21 Võ Nguyên Giáp', 'Đà Nẵng', 'Sơn Trà', '16.0790', '108.2428', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4004, '05 Lê Quang Đạo', 'Đà Nẵng', 'Ngũ Hành Sơn', '16.0290', '108.2450', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4005, '15 Nguyễn Huệ', 'Huế', 'Phú Hội', '16.4679', '107.5909', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4006, '92 Trần Quốc Toản', 'Huế', 'Vĩnh Ninh', '16.4560', '107.5852', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4007, '08 Phan Đình Phùng', 'Huế', 'Kim Long', '16.4800', '107.5650', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4008, '66 Bạch Đằng', 'Hội An', 'Minh An', '15.8770', '108.3290', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4009, '25 Từ Hoa Công Chúa', 'Hà Nội', 'Tây Hồ', '21.0680', '105.8220', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4010, '36 Hàng Bè', 'Hà Nội', 'Hoàn Kiếm', '21.0285', '105.8542', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4011, '11 Đội Cấn', 'Hà Nội', 'Ba Đình', '21.0333', '105.8142', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4012, '85 Nguyễn Văn Cừ', 'Hà Nội', 'Long Biên', '21.0460', '105.9130', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4013, '18 Phan Đình Phùng', 'Đà Lạt', 'Phường 1', '11.9465', '108.4419', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4014, '02 Lê Hồng Phong', 'Đà Lạt', 'Phường 3', '11.9400', '108.4500', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4015, '40 Trần Phú', 'Nha Trang', 'Lộc Thọ', '12.2433', '109.1925', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4016, '22 Nguyễn Phan Vinh', 'Đà Nẵng', 'An Bàng', '15.8950', '108.3450', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4017, '12 Fansipan', 'Sa Pa', 'Trung Tâm', '22.3350', '103.8420', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4018, '55 Xuân Diệu', 'Quy Nhơn', 'Ngô Mây', '13.7676', '109.2198', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4019, '88 Nguyễn Thái Học', 'Cần Thơ', 'Ninh Kiều', '10.0320', '105.7830', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4020, '07 Trần Hưng Đạo', 'Phú Quốc', 'Dương Đông', '10.2270', '103.9590', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tbl_homestays (
    address_id,
    created_at,
    updated_at,
    advanced_payment,
    base_price,
    category,
    description,
    max_guest,
    min_guest,
    num_bathrooms,
    num_bedrooms,
    num_beds,
    num_kitchen,
    rating,
    status,
    title,
    warning_count,
    host_id
)
VALUES
    (4001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 950000, 'Căn hộ', 'Căn hộ view sông Hàn với ban công rộng và nhiều ánh sáng tự nhiên.', 6, 2, 2, 3, 3, 1, 4.8, 'ACTIVE', 'Sông Hàn Breeze', 0, 3001),
    (4002, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 780000, 'Studio', 'Không gian ấm áp ngay trung tâm thành phố, phù hợp cặp đôi.', 3, 1, 1, 1, 1, 1, 4.6, 'ACTIVE', 'Bạch Đằng Loft', 0, 3001),
    (4003, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 35, 1350000, 'Biệt thự', 'Biệt thự biển với hồ bơi riêng và khu BBQ.', 10, 4, 3, 4, 5, 1, 4.9, 'ACTIVE', 'Sea Pearl Villa', 0, 3001),
    (4004, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 1100000, 'Nhà phố', 'Nhà phố hiện đại gần bãi biển Mỹ Khê.', 8, 3, 3, 3, 4, 1, 4.7, 'ACTIVE', 'Mỹ Khê Urban Stay', 0, 3001),
    (4005, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 650000, 'Căn hộ', 'Căn hộ cổ điển ngắm sông Hương lãng mạn.', 4, 2, 1, 2, 2, 1, 4.5, 'ACTIVE', 'Sông Hương Classic', 0, 3002),
    (4006, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 720000, 'Nhà phố', 'Không gian xanh mát với sân vườn nhỏ giữa lòng Huế.', 5, 2, 2, 2, 3, 1, 4.4, 'ACTIVE', 'Vườn Nhỏ Huế House', 0, 3002),
    (4007, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 880000, 'Bungalow', 'Nhà gỗ hướng sông thơ mộng phù hợp gia đình.', 6, 2, 2, 3, 3, 1, 4.6, 'ACTIVE', 'Kim Long Riverside', 0, 3002),
    (4008, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 1250000, 'Biệt thự', 'Biệt thự Hội An với sân vườn nhiệt đới và hồ bơi.', 9, 4, 3, 4, 5, 1, 4.8, 'ACTIVE', 'An Bàng Garden Villa', 0, 3002),
    (4009, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 40, 1800000, 'Căn hộ', 'Căn hộ cao cấp bên hồ Tây với bếp mở và quầy bar.', 6, 2, 2, 3, 3, 1, 4.9, 'ACTIVE', 'Hồ Tây Premier Loft', 0, 3003),
    (4010, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 35, 1500000, 'Studio', 'Studio phong cách Đông Dương giữa phố cổ.', 4, 2, 1, 1, 2, 1, 4.7, 'ACTIVE', 'Hoàn Kiếm Heritage', 0, 3003),
    (4011, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 980000, 'Nhà phố', 'Nhà phố rộng rãi có phòng làm việc riêng.', 5, 2, 2, 3, 3, 1, 4.6, 'ACTIVE', 'Ba Đình Executive Home', 0, 3003),
    (4012, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 820000, 'Căn hộ', 'Căn hộ sáng sủa nhìn ra cầu Chương Dương.', 4, 2, 2, 2, 2, 1, 4.5, 'ACTIVE', 'Long Biên Light', 0, 3003),
    (4013, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 1400000, 'Biệt thự', 'Biệt thự thông hai tầng với vườn hồng Đà Lạt.', 8, 3, 3, 4, 5, 1, 4.9, 'ACTIVE', 'Đồi Hồng Villa', 0, 3004),
    (4014, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 900000, 'Bungalow', 'Nhà gỗ nhỏ nhìn ra thung lũng, có lò sưởi.', 4, 2, 1, 2, 2, 1, 4.8, 'ACTIVE', 'Thông Xanh Cabin', 0, 3004),
    (4015, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 35, 1550000, 'Căn hộ', 'Căn hộ hướng biển Nha Trang với hồ bơi vô cực.', 6, 2, 2, 3, 3, 1, 4.9, 'ACTIVE', 'Trần Phú Azure', 0, 3004),
    (4016, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 1200000, 'Nhà phố', 'Nhà phong cách Santorini tại làng An Bàng.', 6, 2, 2, 3, 3, 1, 4.7, 'ACTIVE', 'An Bàng Santorini', 0, 3004),
    (4017, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 1050000, 'Bungalow', 'Lodge gỗ ấm áp nhìn ra đồi chè Sa Pa.', 5, 2, 2, 2, 3, 1, 4.8, 'ACTIVE', 'Fansipan Lodge', 0, 3005),
    (4018, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 890000, 'Căn hộ', 'Căn hộ biển Quy Nhơn có bếp mở và bàn ăn dài.', 5, 2, 2, 2, 3, 1, 4.6, 'ACTIVE', 'Xuân Diệu Azure', 0, 3005),
    (4019, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 760000, 'Nhà phố', 'Nhà ven sông Cần Thơ với hiên rộng uống trà.', 5, 2, 2, 2, 3, 1, 4.5, 'ACTIVE', 'Gió Sông Ninh Kiều', 0, 3005),
    (4020, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 35, 1650000, 'Biệt thự', 'Biệt thự nhiệt đới với hồ bơi và vườn dừa Phú Quốc.', 9, 3, 3, 4, 5, 1, 4.9, 'ACTIVE', 'Dương Đông Palm Villa', 0, 3005)
ON CONFLICT (address_id) DO NOTHING;

WITH target_homestays AS (
    SELECT address_id AS homestay_id
    FROM tbl_homestays
    WHERE address_id BETWEEN 4001 AND 4020
),
capacity_template (person_id, quantity) AS (
    VALUES
        (9101, 4),
        (9102, 2),
        (9103, 1)
)
INSERT INTO tbl_person_homestay (person_id, homestay_id, quantity, created_at, updated_at)
SELECT
    ct.person_id,
    th.homestay_id,
    ct.quantity,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM capacity_template ct
JOIN target_homestays th ON 1 = 1
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_person_homestay existing
    WHERE existing.person_id = ct.person_id
      AND existing.homestay_id = th.homestay_id
);
