-- Undo script for src/main/resources/data/sql/insert_homestay_seed.sql
-- Removes the deterministic seed rows while keeping other data intact.

BEGIN;

DELETE FROM tbl_person_homestay
WHERE person_id IN (9101, 9102, 9103)
  AND homestay_id BETWEEN 4001 AND 4020;

DELETE FROM tbl_homestays
WHERE address_id BETWEEN 4001 AND 4020;

DELETE FROM tbl_addresses
WHERE id BETWEEN 4001 AND 4020;

DELETE FROM tbl_hosts
WHERE user_id IN (3001, 3002, 3003, 3004, 3005);

DELETE FROM tbl_users
WHERE id IN (3001, 3002, 3003, 3004, 3005);

DELETE FROM tbl_persons
WHERE id IN (9101, 9102, 9103);

COMMIT;
