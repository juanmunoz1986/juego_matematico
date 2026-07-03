# Configuración del Backend (Supabase + PostgreSQL)

Este directorio contiene las migraciones y configuraciones requeridas en Supabase para habilitar un backend seguro de puntuación en línea para **AcuMath**.

## Paso 1: Crear un proyecto en Supabase
1. Ingresa a [Supabase](https://supabase.com/) y crea un proyecto nuevo.
2. Anota la **URL del proyecto** y la clave **Anon public API key** (clave anónima). Las necesitarás para configurar los secretos en la app.

## Paso 2: Ejecutar el Esquema de Base de Datos
1. En el panel lateral de Supabase, ve a **SQL Editor** y haz clic en **New query**.
2. Copia y pega el contenido del archivo `backend/migrations/001_initial.sql`.
3. Haz clic en **Run** para aplicar las tablas, la vista, los índices, las políticas de seguridad (RLS) y los triggers anti-trampas.

## Paso 3: Habilitar Autenticación Anónima
Para que la app asigne a cada usuario una identidad única y segura (usando JWT de Supabase), habilita la autenticación anónima:
1. Ve a **Authentication** -> **Providers**.
2. Busca **Anonymous** en la lista de proveedores.
3. Actívalo (**Enable Anonymous Sign-ins**) y guarda los cambios.

## Paso 4: Configurar los Secretos de la Aplicación Android
En el panel de control de tu entorno de desarrollo o en la consola de Google AI Studio, añade los siguientes secretos de forma segura:
- `SUPABASE_URL`: La URL de tu proyecto de Supabase (ej. `https://your-project-id.supabase.co`)
- `SUPABASE_ANON_KEY`: La clave de API anónima de tu proyecto (anon public key).

La aplicación leerá estas credenciales de forma segura mediante `BuildConfig` para establecer la conexión en tránsito HTTPS + JWT.
