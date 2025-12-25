-- Enables search extensions and creates trigram indexes for address.city and homestay.title
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Wrapper keeps the expression immutable without requiring superuser ALTER FUNCTION
CREATE OR REPLACE FUNCTION public.unaccent_immutable(text)
RETURNS text
LANGUAGE sql
IMMUTABLE
STRICT
AS $function$
SELECT public.unaccent($1);
$function$;

CREATE INDEX IF NOT EXISTS idx_address_city_trgm
ON tbl_addresses
USING gin (public.unaccent_immutable(lower(city)) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_homestay_title_trgm
ON tbl_homestays
USING gin (public.unaccent_immutable(lower(title)) gin_trgm_ops);
