# Guía de Publicación en Google Play Console para AcuMath

Esta guía describe el paso a paso detallado para configurar tu ficha, rellenar los cuestionarios obligatorios y subir el archivo App Bundle (`.aab`) a Google Play Console.

---

## Paso 1: Crear la Aplicación en Play Console
1. Entra a tu cuenta en [Google Play Console](https://play.google.com/console/).
2. Haz clic en **Crear aplicación** (Create app).
3. Configura los siguientes datos básicos:
   * **Nombre de la aplicación:** AcuMath
   * **Idioma predeterminado:** Español (España) o Español (América Latina)
   * **Tipo de aplicación:** Juego (Game)
   * **¿Es gratis o de pago?:** Gratis (Free)
4. Acepta las declaraciones de políticas obligatorias y haz clic en **Crear aplicación**.

---

## Paso 2: Configurar la Ficha de Play Store
Dirígete a la barra lateral izquierda en **Presencia en Google Play Store** -> **Ficha de Play Store principal** e ingresa la información de marketing que preparamos:
* **Título (título.txt):** `AcuMath`
* **Descripción corta (descripcion_corta.txt):** `Entrena tu cerebro y mejora tu cálculo mental matemático con desafíos dinámicos.`
* **Descripción larga (descripcion_larga.md):** Copia todo el contenido del archivo `store/descripcion_larga.md` (hasta 4,000 caracteres, bien estructurado y formateado con viñetas).

---

## Paso 3: Gráficos de la Ficha de Play Store
Sube los recursos visuales en la misma pantalla de configuración de la ficha:
1. **Ícono de la aplicación:** Un archivo PNG de **512 x 512 píxeles**, de hasta 1 MB, con fondo opaco.
2. **Gráfico de funciones (Feature Graphic):** Un archivo PNG de **1024 x 500 píxeles**, de hasta 1 MB. Es la imagen de cabecera en Play Store.
3. **Capturas de pantalla del teléfono (Phone Screenshots):** Sube al menos 2 a 4 capturas de pantalla de alta calidad tomadas directamente de tu juego en ejecución (menú, pantalla de juego activa, pantalla de game over y leaderboard).

---

## Paso 4: Responder Cuestionarios de Políticas (Dashboard)
Completa las tareas del panel de control de la app (App Setup Checklist) una por una:
1. **Acceso a apps (App Access):** Selecciona "Todas las funciones están disponibles sin restricciones de acceso" (ya que la autenticación anónima se realiza en segundo plano sin requerir credenciales manuales).
2. **Anuncios (Ads):** Selecciona "No, mi app no contiene anuncios".
3. **Clasificación de contenido (Content Rating):**
   * Tipo de categoría: Juego (Game).
   * Responde honestamente (no contiene violencia, apuestas, drogas ni lenguaje fuerte).
   * Te asignará la clasificación apta para todo público (**PEGI 3** o **IARC 3+**).
4. **Público objetivo (Target Audience):**
   * Selecciona el rango de edad sugerido: **13-15 años, 16-17 años, y 18 años o más**.
   * ⚠️ **Recomendación crítica:** **NO** marques rangos menores de 13 años (ej. niños pequeños) para evitar caer en las regulaciones de la "Política de Familias de Google" (Families Policy), que requiere SDKs certificados adicionales, procesos estrictos de consentimiento parental y mayor burocracia de publicación.
   * Responde "No" a la pregunta "¿Se diseñó para niños?".
5. **Apps de noticias (News Apps):** Selecciona "No, mi app no es una aplicación de noticias".
6. **Seguridad de datos (Data Safety):** Sigue detalladamente el archivo de guía rápida `store/data_safety.md` y completa el formulario.
7. **Política de privacidad (Privacy Policy):** Introduce la URL pública donde hayas publicado el archivo `store/politica_privacidad.md` (puedes publicarla fácilmente usando GitHub Pages, un repositorio público de GitHub en modo Raw, o cualquier hosting estático gratuito).

---

## Paso 5: Subir tu Primer Bundle (.aab)
1. Ve a la barra lateral izquierda y selecciona **Pruebas cerradas (Closed testing)**.
2. Haz clic en **Crear nueva versión (Create release)**.
3. Asegúrate de habilitar **Firma de apps de Google Play** (Play App Signing).
4. Arrastra y suelta tu archivo `.aab` generado mediante `./gradlew bundleRelease` (ubicado en `app/build/outputs/bundle/release/app-release.aab`).
5. Añade notas de la versión breves (ej. `Versión inicial de AcuMath con leaderboard seguro y sincronización local offline.`).
6. Haz clic en **Guardar** y luego en **Revisar versión**.

---

## Paso 6: Cumplir con el Requisito de los 12 Probadores (Closed Testing)
> ⚠️ **REGLA OBLIGATORIA DE GOOGLE:** Para las cuentas personales de desarrollador registradas después de noviembre de 2023, Google exige que ejecutes una prueba cerrada con al menos **12 evaluadores independientes que hayan optado por participar activamente durante 14 días consecutivos** antes de poder solicitar la publicación en producción.

1. En la pestaña de tu pista de pruebas cerradas, ve a la sección **Probadores (Testers)**.
2. Crea una lista de correo con las direcciones de Gmail de tus 12 testers (amigos, familiares, compañeros de trabajo).
3. Compárteles el **enlace de participación web** o de **Android** que te suministra Play Console.
4. Pídeles que descarguen el juego y lo abran diariamente o con frecuencia durante los 14 días requeridos.
5. Al cumplir los 14 días, la opción de **Solicitar publicación en producción** se desbloqueará automáticamente en tu consola de control.
