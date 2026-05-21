alter table public.outlet_logins
add column if not exists password text;

alter table public.employee
add column if not exists password text;

create index if not exists employee_contact_idx on public.employee(contact);
create index if not exists outlet_logins_username_idx on public.outlet_logins(username);

create or replace view public.products as
select
  omi.id,
  omi.created_at,
  pc.name,
  pc.description,
  omi.price,
  omi.discount,
  omi.image,
  pc.category,
  omi.rating,
  omi.reviews_count as "reviewsCount",
  pc."prepTime",
  pc.review,
  pc."nutritionalInfo",
  pc."isVeg",
  pc."isVegan",
  pc."isBestSeller",
  omi.outlet_id,
  omi.product_id,
  omi.is_available,
  omi.is_out_of_stock
from public.outlet_menu_items omi
join public.product_catalog pc on pc.id = omi.product_id;

comment on column public.outlet_logins.password is
  'Plain text backoffice outlet password, used by backoffice_login.';

comment on column public.employee.password is
  'Plain text backoffice employee password, used by backoffice_login.';

comment on view public.products is
  'Compatibility view for older app screens. Uses outlet_menu_items ids as product ids and joins product_catalog metadata.';
