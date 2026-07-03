# Política de Privacidad de AcuMath

**Última actualización:** 2 de julio de 2026

En **AcuMath** (en adelante, "la Aplicación"), nos tomamos muy en serio la privacidad de nuestros usuarios. Esta Política de Privacidad describe los tipos de información que recopilamos, cómo la utilizamos y las medidas que tomamos para garantizar que tus datos estén protegidos de acuerdo con las políticas de Google Play Store.

Al descargar y utilizar la Aplicación, aceptas los términos descritos en esta política.

---

## 1. Información que Recopilamos

Para proporcionarte una experiencia de juego óptima, un sistema de puntuación global y sincronización, la Aplicación puede recopilar los siguientes datos:

1. **Identificadores Anónimos de Usuario:** Al abrir la aplicación por primera vez, se genera automáticamente una cuenta anónima segura en nuestro servidor de bases de datos externas (Supabase Auth). Esto genera un ID único (UUID) que no está asociado con tu nombre real, correo electrónico, número de teléfono ni ninguna otra información de identidad del mundo real.
2. **Nombre de Jugador (Display Name):** Puedes elegir libremente un apodo o nombre de pantalla visible públicamente para que aparezca en la tabla de clasificación mundial (Leaderboard). Este nombre no necesita ser tu nombre real.
3. **Puntajes y Estadísticas de Juego:** Registramos tus puntuaciones más altas, la racha máxima de respuestas correctas, el nivel de dificultad seleccionado y el nivel alcanzado durante tus partidas.

---

## 2. Cómo Utilizamos la Información

La información recopilada se utiliza de manera exclusiva para las siguientes finalidades:

* **Tabla de Clasificación (Leaderboard) Global:** Mostrar los mejores puntajes de los jugadores del juego en la tabla de clasificación de la nube para fomentar la competencia amistosa.
* **Sincronización Offline-to-Online:** Guardar temporalmente las puntuaciones en el almacenamiento local de tu dispositivo (SQLite) y subirlas a la nube cuando tu conexión a Internet se restablezca.
* **Seguridad y Prevención de Abuso:** Validar en el lado del servidor que las puntuaciones enviadas sean físicamente posibles y no producto de alteraciones ilegítimas de la Aplicación.

**Nota fundamental:** No vendemos, alquilamos ni compartimos tus datos bajo ninguna circunstancia con terceros ni agencias de publicidad. Tu información se almacena de forma segura en tránsito mediante cifrado HTTPS y se protege mediante políticas RLS (Row Level Security) en nuestra base de datos.

---

## 3. Almacenamiento y Seguridad de los Datos

* **Cifrado en Tránsito:** Toda la transferencia de datos entre la Aplicación y nuestro backend en Supabase se realiza bajo el protocolo criptográfico seguro HTTPS (SSL/TLS).
* **Control de Acceso Riguroso:** El servidor de base de datos implementa **Row Level Security (RLS)** en PostgreSQL. Esto significa que un usuario autenticado anónimamente solo puede editar o actualizar su propio perfil y puntuaciones, imposibilitando la suplantación de identidad o la alteración de puntuaciones de terceros.

---

## 4. Retención y Eliminación de Datos

Conservamos la información del jugador mientras la cuenta permanezca activa. Si deseas solicitar la eliminación completa de tus puntajes y de tu cuenta anónima del sistema, puedes ponerte en contacto con el desarrollador propietario a través del correo electrónico de soporte. Todos tus datos asociados se eliminarán de manera permanente en un plazo no mayor a 15 días hábiles.

---

## 5. Menores de Edad

La Aplicación está diseñada para usuarios de todas las edades. Aunque es un juego educativo de matemáticas, **no está dirigido específicamente a niños menores de 13 años** para evitar la recopilación innecesaria de información regulada por COPPA. Alentamos a los padres y tutores a supervisar la actividad en línea de los menores.

---

## 6. Cambios en esta Política de Privacidad

Nos reservamos el derecho de actualizar esta Política de Privacidad periódicamente para reflejar cambios en la aplicación o requisitos legales. Te recomendamos revisar esta página de vez en cuando para estar informado.

---

## 7. Contacto

Si tienes preguntas o inquietudes sobre esta Política de Privacidad o deseas solicitar la eliminación de tus datos de la tabla de clasificación, por favor contáctanos:

* **Desarrollador Propietario:** Juan Muñoz
* **Correo Electrónico de Soporte:** juanospina876@gmail.com
