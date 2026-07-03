# Respuestas para el Formulario de Seguridad de Datos (Data Safety) en Google Play Console

Este documento proporciona las respuestas exactas que debes ingresar al completar el formulario obligatorio de **Seguridad de Datos** en tu cuenta de Google Play Console para la aplicación **AcuMath**.

---

### 1. Recopilación y uso de datos
* **¿Tu app recopila o comparte alguno de los tipos de datos de usuario requeridos?**
  * Respuesta: **Sí**.
* **¿Todos los datos de usuario recopilados por tu app se cifran en tránsito?**
  * Respuesta: **Sí** (Toda la comunicación va cifrada por protocolo estricto HTTPS).
* **¿Proporcionas algún método para que los usuarios soliciten que se borren sus datos?**
  * Respuesta: **Sí** (Se indica en la política de privacidad que el usuario puede solicitar el borrado enviando un correo al desarrollador).

---

### 2. Tipos de datos recopilados
En la sección de selección de tipos de datos, marca únicamente las siguientes casillas:

#### Categoría: Identificadores personales y del dispositivo
* **Identificadores de usuario (User IDs):**
  * Selecciona: **Recopilados**.
  * **¿Se procesan estos datos de forma efímera?** -> **No**.
  * **¿Se requieren estos datos para tu app o los usuarios pueden elegir si se recopilan?** -> **Recopilación obligatoria** (Se requiere para asociar los puntajes del leaderboard con un usuario único de forma segura).
  * **¿Por qué se recopilan estos datos?** -> Marca: **Funcionalidad de la app** e **Identificación del usuario**.

#### Categoría: Información personal
* **Nombre de usuario o apodo (Name):**
  * Selecciona: **Recopilados** (para el nombre del jugador en el ranking).
  * **¿Se procesan estos datos de forma efímera?** -> **No**.
  * **¿Se requieren estos datos para tu app o los usuarios pueden elegir si se recopilan?** -> **Recopilación opcional / El usuario puede elegir** (Si no editan su nombre, figuran por defecto como "Propietario" o "Invitado", y pueden cambiarlo libremente).
  * **¿Por qué se recopilan estos datos?** -> Marca: **Funcionalidad de la app**.

#### Categoría: Rendimiento de la app (Opcional, solo si usas Analytics, si no, déjalo sin marcar)
* **Puntajes y rendimiento del juego:**
  * Si la plataforma lo pregunta de forma explícita en su categoría de datos:
  * Selecciona: **Recopilados**.
  * **¿Se procesan de forma efímera?** -> **No**.
  * **¿Es obligatorio u opcional?** -> **Obligatorio** (Es la mecánica principal del leaderboard).
  * **¿Por qué se recopila?** -> Marca: **Funcionalidad de la app**.

---

### 3. Compartir datos con terceros
* **¿Tu app comparte datos del usuario con otras empresas o instituciones?**
  * Respuesta: **No** (Los datos se transmiten únicamente a tu propio servidor backend de base de datos alojado en Supabase, lo cual cuenta como almacenamiento en el proveedor del servicio y no como "compartir con terceros").
