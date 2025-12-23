-- Migration script để thêm cột deleted cho các bảng đã có dữ liệu
-- Chạy script này trước khi start ứng dụng nếu bảng đã có dữ liệu

-- 1. Thêm cột deleted cho tbl_amenities (cho phép NULL tạm thời)
ALTER TABLE tbl_amenities ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 2. Update tất cả các record cũ với giá trị false
UPDATE tbl_amenities SET deleted = false WHERE deleted IS NULL;

-- 3. Set default value và NOT NULL constraint
ALTER TABLE tbl_amenities ALTER COLUMN deleted SET DEFAULT false;
ALTER TABLE tbl_amenities ALTER COLUMN deleted SET NOT NULL;

-- 4. Thêm cột deleted cho tbl_preferences (cho phép NULL tạm thời)
ALTER TABLE tbl_preferences ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 5. Update tất cả các record cũ với giá trị false
UPDATE tbl_preferences SET deleted = false WHERE deleted IS NULL;

-- 6. Set default value và NOT NULL constraint
ALTER TABLE tbl_preferences ALTER COLUMN deleted SET DEFAULT false;
ALTER TABLE tbl_preferences ALTER COLUMN deleted SET NOT NULL;

-- 7. Thêm cột deleted cho tbl_facilities (cho phép NULL tạm thời)
ALTER TABLE tbl_facilities ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 8. Update tất cả các record cũ với giá trị false
UPDATE tbl_facilities SET deleted = false WHERE deleted IS NULL;

-- 9. Set default value và NOT NULL constraint
ALTER TABLE tbl_facilities ALTER COLUMN deleted SET DEFAULT false;
ALTER TABLE tbl_facilities ALTER COLUMN deleted SET NOT NULL;

