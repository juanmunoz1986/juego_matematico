-- FASE 1: Esquema de Base de Datos Inicial (Supabase / PostgreSQL)

-- 1. Tabla de jugadores, ligada a la identidad de Supabase Auth
CREATE TABLE IF NOT EXISTS public.players (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  display_name TEXT NOT NULL CHECK (char_length(display_name) BETWEEN 1 AND 30),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. Tabla de puntajes
CREATE TABLE IF NOT EXISTS public.scores (
  id BIGSERIAL PRIMARY KEY,
  player_id UUID NOT NULL REFERENCES public.players(id) ON DELETE CASCADE,
  score INT NOT NULL CHECK (score >= 0),
  max_streak INT NOT NULL DEFAULT 0 CHECK (max_streak >= 0),
  difficulty TEXT NOT NULL CHECK (difficulty IN ('Bajo','Medio','Alto','Experto','Super Pro')),
  level_reached INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices optimizados para velocidad del Leaderboard
CREATE INDEX IF NOT EXISTS idx_scores_leaderboard ON public.scores (score DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_scores_player ON public.scores (player_id);

-- 3. Vista pública del leaderboard (la app lee SOLO esto, nunca las tablas base)
CREATE OR REPLACE VIEW public.leaderboard AS
SELECT s.id, p.display_name AS player_name, s.score, s.max_streak,
       s.difficulty, s.level_reached, s.created_at
FROM public.scores s
JOIN public.players p ON p.id = s.player_id
ORDER BY s.score DESC
LIMIT 100;

-- Otorgar permisos de lectura pública a la vista
GRANT SELECT ON public.leaderboard TO anon, authenticated;

-- 4. Row Level Security (RLS) - Seguridad de lectura/escritura obligatoria
ALTER TABLE public.players ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.scores ENABLE ROW LEVEL SECURITY;

-- Políticas para public.players: cada usuario gestiona solo su fila, lectura pública
DROP POLICY IF EXISTS players_insert ON public.players;
CREATE POLICY players_insert ON public.players FOR INSERT
  WITH CHECK (id = auth.uid());

DROP POLICY IF EXISTS players_update ON public.players;
CREATE POLICY players_update ON public.players FOR UPDATE
  USING (id = auth.uid());

DROP POLICY IF EXISTS players_select ON public.players;
CREATE POLICY players_select ON public.players FOR SELECT
  USING (true);

-- Políticas para public.scores: solo insertar puntajes propios; sin update/delete
DROP POLICY IF EXISTS scores_insert ON public.scores;
CREATE POLICY scores_insert ON public.scores FOR INSERT
  WITH CHECK (player_id = auth.uid());

DROP POLICY IF EXISTS scores_select ON public.scores;
CREATE POLICY scores_select ON public.scores FOR SELECT
  USING (true);

-- 5. Validación anti-trampa server-side mediante Trigger
CREATE OR REPLACE FUNCTION public.validate_score()
RETURNS TRIGGER AS $$
DECLARE
  max_plausible INT;
  recent_count INT;
BEGIN
  max_plausible := CASE NEW.difficulty
    WHEN 'Bajo'      THEN  50000
    WHEN 'Medio'     THEN 100000
    WHEN 'Alto'      THEN 150000
    WHEN 'Experto'   THEN 250000
    WHEN 'Super Pro' THEN 400000
    ELSE 50000
  END;
  IF NEW.score > max_plausible THEN
    RAISE EXCEPTION 'score_implausible';
  END IF;

  -- Rate limiting: máx 10 puntajes por jugador por minuto
  SELECT count(*) INTO recent_count FROM public.scores
  WHERE player_id = NEW.player_id AND created_at > now() - interval '1 minute';
  IF recent_count >= 10 THEN
    RAISE EXCEPTION 'rate_limit_exceeded';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_validate_score ON public.scores;
CREATE TRIGGER trg_validate_score
BEFORE INSERT ON public.scores
FOR EACH ROW EXECUTE FUNCTION public.validate_score();

-- 6. Trigger automático para sincronizar perfiles tras registro de auth.users
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.players (id, display_name)
  VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'display_name', 'Propietario'))
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
