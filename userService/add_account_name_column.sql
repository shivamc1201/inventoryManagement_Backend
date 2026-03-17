-- Add the missing account_name column to the distributors table
ALTER TABLE public.distributors ADD COLUMN account_name VARCHAR(100) NOT NULL DEFAULT '';

-- If you want to make it non-nullable without a default, you can:
-- 1. First add the column as nullable
-- ALTER TABLE public.distributors ADD COLUMN account_name VARCHAR(100);
-- 2. Update existing records with appropriate values
-- UPDATE public.distributors SET account_name = 'Default Account Name' WHERE account_name IS NULL;
-- 3. Then make it non-nullable
-- ALTER TABLE public.distributors ALTER COLUMN account_name SET NOT NULL;