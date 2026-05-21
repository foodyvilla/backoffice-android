# FoodyVilla Backoffice Supabase Functions

## Deploy login function

```bash
supabase functions deploy backoffice_login
```

If you still see `invalid JWT format`, deploy with JWT verification disabled:

```bash
supabase functions deploy backoffice_login --no-verify-jwt
```

This repo also includes `supabase/config.toml` with:

```toml
[functions.backoffice_login]
verify_jwt = false
```

The function uses the built-in `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` secrets available in Supabase Edge Functions.

## Password storage

This project is currently configured for plain text backoffice passwords because that is what the deployed function expects.

Run the migration, then set passwords in Supabase SQL editor:

```sql
update public.outlet_logins
set password = 'your-password'
where username = 'kora_owner';

update public.employee
set password = 'your-password'
where contact = '+919911223344';
```

The same migration also creates a `public.products` compatibility view over `outlet_menu_items` + `product_catalog`. That fixes older app screens that still call `/rest/v1/products`.

## Expected request body

Outlet login:

```json
{
  "login_type": "outlet",
  "identifier": "kora_owner",
  "password": "your-password"
}
```

Employee login:

```json
{
  "login_type": "employee",
  "identifier": "+919911223344",
  "password": "your-password"
}
```
