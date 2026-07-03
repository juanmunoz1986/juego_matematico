# AcuMath - Migración a Arquitectura Segura en la Nube (Supabase + PostgreSQL)

Este documento detalla la implementación y los pasos necesarios para configurar el backend externo con base de datos PostgreSQL, garantizando una arquitectura moderna y segura de tipo **App → API REST (HTTPS + JWT) → PostgreSQL**, manteniendo **Room** como caché local offline (SQLite).

---

## 🛠️ Fase 1: Esquema de Base de Datos (Supabase / PostgreSQL)

Para inicializar tu base de datos en Supabase, abre el **SQL Editor** en el panel de control de Supabase y ejecuta las siguientes consultas SQL:

### 1. Tabla de Jugadores (`players`)
Esta tabla almacena la identidad de cada dispositivo autenticado anónimamente, impidiendo la suplantación de nombres.

```sql
CREATE TABLE public.players (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- Habilitar Row Level Security (RLS)
ALTER TABLE public.players ENABLE ROW LEVEL SECURITY;

-- Política: Permitir lectura pública de los perfiles
CREATE POLICY "Permitir lectura pública de perfiles" 
ON public.players FOR SELECT 
TO public 
USING (true);

-- Política: Permitir que los usuarios editen su propio perfil
CREATE POLICY "Permitir actualización del propio perfil" 
ON public.players FOR UPDATE 
TO authenticated 
USING (auth.uid() = id) 
WITH CHECK (auth.uid() = id);

-- Política: Permitir que los usuarios inserten su propio perfil inicial
CREATE POLICY "Permitir inserción del propio perfil" 
ON public.players FOR INSERT 
TO authenticated 
WITH CHECK (auth.uid() = id);
```

### 2. Tabla de Puntajes (`scores`)
Almacena los registros históricos de las puntuaciones alcanzadas.

```sql
CREATE TABLE public.scores (
    id BIGSERIAL PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES public.players(id) ON DELETE CASCADE,
    score INT NOT NULL CHECK (score >= 0),
    max_streak INT DEFAULT 0 CHECK (max_streak >= 0),
    difficulty TEXT NOT NULL,
    level INT NOT NULL CHECK (level >= 1),
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- Índice optimizado para el Leaderboard
CREATE INDEX idx_scores_leaderboard ON public.scores (score DESC);

-- Habilitar Row Level Security (RLS)
ALTER TABLE public.scores ENABLE ROW LEVEL SECURITY;

-- Política: Permitir inserción solo para puntajes propios de usuarios autenticados
CREATE POLICY "Permitir insertar puntajes propios" 
ON public.scores FOR INSERT 
TO authenticated 
WITH CHECK (auth.uid() = player_id);

-- Política: Denegar updates y deletes para proteger la integridad del Leaderboard
-- Al no crear políticas de UPDATE o DELETE, estas acciones quedan denegadas por defecto por RLS.
```

### 3. Vista Pública para el Leaderboard (`leaderboard`)
Para evitar exponer IDs internos y cumplir con la regla de seguridad server-side, la aplicación consume el leaderboard exclusivamente a través de esta vista de solo lectura.

```sql
CREATE VIEW public.leaderboard AS
SELECT 
    s.id,
    s.score,
    s.max_streak,
    s.difficulty,
    s.level,
    p.display_name,
    s.created_at
FROM public.scores s
JOIN public.players p ON s.player_id = p.id
ORDER BY s.score DESC;

-- Dar permisos de lectura pública a la vista
GRANT SELECT ON public.leaderboard TO anon, authenticated;
```

### 4. Automatización de Creación de Perfil de Jugador (Trigger)
Para agilizar el proceso y asegurar la creación del perfil al registrarse un usuario anónimo de manera segura, puedes añadir este Trigger en Postgres:

```sql
-- Función que se ejecuta tras registrar un usuario
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.players (id, display_name)
  VALUES (new.id, COALESCE(new.raw_user_meta_data->>'display_name', 'Propietario'));
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger asociado a auth.users
CREATE OR REPLACE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
```

---

## 🔑 Fase 2: Configuración en Google AI Studio (Secrets)

Para que tu aplicación se conecte correctamente a Supabase, **no debes harcodear las llaves en el código fuente**. En su lugar:

1. Ve a la interfaz de **Google AI Studio** en tu navegador.
2. Abre el panel de **Secrets** (Secretos).
3. Agrega las siguientes dos variables de entorno con tus credenciales de Supabase:
   - `SUPABASE_URL`: La URL de tu proyecto de Supabase (ej. `https://zqwpxkahdfksjdhfks.supabase.co`)
   - `SUPABASE_ANON_KEY`: Tu llave pública anónima de API (`anon` key).

Estas variables se inyectarán de forma segura en tiempo de compilación a través del plugin **Secrets Gradle Plugin** y se expondrán mediante la clase `com.example.BuildConfig`.

---

## 📱 Fase 3: Detalle de Cambios en la App Android

1. **Eliminación Total de Firebase:**
   - Se removió el archivo `google-services.json`.
   - Se eliminaron los plugins y librerías de Firebase de los archivos `build.gradle.kts`.
2. **Capa de Red basada en Retrofit + Moshi:**
   - Creado `ApiService.kt`: interfaz limpia con los 4 endpoints esenciales mapeados (Autenticación Anónima, Envío de Puntuaciones, Leaderboard Público y Actualización de Perfiles).
   - Creado `SupabaseManager.kt`: singleton robusto que gestiona:
     - Persistencia y caché local de la sesión JWT y el ID de jugador en SharedPreferences.
     - Registro anónimo directo en el primer arranque de la app.
     - Sincronización automática del nombre del jugador con la nube.
     - Mapeo de DTOs a la entidad `UserScore` consumida por el UI.
3. **Mantenimiento del Flujo Offline-First:**
   - `MainViewModel` conserva intacta la lógica de caché offline local (Room).
   - Los registros que no pudieron enviarse por falta de conexión se guardan con `isSynced = false` en Room. Al activar la conexión online, la función `syncOfflineScores()` reintenta la subida secuencialmente a la base de datos externa de Supabase.

---

## 🚀 Fase 4: Preparación para Google Play Console

1. **Namespace & ApplicationID:**
   - El `applicationId` definitivo configurado en Gradle es `com.aistudio.acumath.zqwpxk`. Es inmutable para esta versión del proyecto de compilación.
2. **Prueba Cerrada (12 Testers):**
   - Para cuentas personales creadas después de noviembre de 2023, Google Play exige que realices una prueba cerrada con al menos **12 evaluadores que hayan optado por participar durante 14 días seguidos** de forma voluntaria antes de poder solicitar el acceso a producción.
