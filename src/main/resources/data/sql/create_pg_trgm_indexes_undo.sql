-- Drops trigram search indexes for rollback purposes
DROP INDEX IF EXISTS idx_address_city_trgm;
DROP INDEX IF EXISTS idx_homestay_title_trgm;
DROP FUNCTION IF EXISTS public.unaccent_immutable(text);
