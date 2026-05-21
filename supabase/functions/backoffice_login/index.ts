import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
);

serve(async (req) => {
  try {
    if (req.method === "OPTIONS") {
      return new Response("ok", { headers: corsHeaders });
    }

    if (req.method !== "POST") {
      return json({ success: false, message: "Method not allowed" }, 405);
    }

    const { login_type, identifier, password } = await req.json();

    if (!login_type || !identifier || !password) {
      return json({ success: false, message: "Missing required fields" }, 400);
    }

    if (login_type === "outlet") {
      const { data, error } = await supabase
        .from("outlet_logins")
        .select("id, outlet_id, username, password, role, is_active")
        .eq("username", String(identifier).trim())
        .single();

      if (error || !data || !data.is_active || data.password !== password) {
        return invalidCredentials();
      }

      return json({
        success: true,
        message: "Login successful",
        session: {
          type: "outlet",
          outlet_id: data.outlet_id,
          username: data.username,
          role: data.role,
        },
      });
    }

    if (login_type === "employee") {
      const value = String(identifier).trim();
      const numericId = Number(value);
      const filter = Number.isFinite(numericId)
        ? `contact.eq.${value},id.eq.${numericId}`
        : `contact.eq.${value}`;

      const { data, error } = await supabase
        .from("employee")
        .select(`
          id,
          outlet_id,
          designation_id,
          password,
          designations:designation_id (
            permissions
          )
        `)
        .or(filter)
        .single();

      if (error || !data || data.password !== password) {
        return invalidCredentials();
      }

      return json({
        success: true,
        message: "Login successful",
        session: {
          type: "employee",
          emp_id: data.id,
          outlet_id: data.outlet_id,
          designation_id: data.designation_id,
          permissions: data.designations?.permissions ?? [],
        },
      });
    }

    return json({ success: false, message: "Invalid login type" }, 400);
  } catch (error) {
    return json(
      {
        success: false,
        message: error?.message || "Internal server error",
      },
      500,
    );
  }
});

function invalidCredentials() {
  return json({ success: false, message: "Invalid credentials" }, 401);
}

function json(body: unknown, status = 200) {
  return Response.json(body, {
    status,
    headers: corsHeaders,
  });
}
