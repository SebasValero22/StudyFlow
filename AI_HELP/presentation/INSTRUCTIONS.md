# 📖 Guía de Uso del Kit de Presentación - StudyFlow

Este kit ha sido diseñado específicamente para la defensa pública de tu proyecto intermodular **StudyFlow** (2º DAM, IES Camp de Morvedre). Te proporciona un flujo de trabajo profesional para automatizar tu presentación en Google Slides y para ensayar tu discurso con un presentador interactivo de última generación.

---

## ⚡ Paso 1: Generar tu Google Slides en 1 Minuto

Hemos creado un script automatizado en **Google Apps Script** que interactúa con la API de Google Slides para construir tu presentación con diseño oscuro premium, la estructura recomendada por los evaluadores y **tus notas de orador integradas automáticamente en cada diapositiva**.

### Instrucciones para la generación:
1. Abre tu navegador y ve a la consola oficial de Google Apps Script: [script.google.com](https://script.google.com/) (asegúrate de iniciar sesión con tu cuenta de Google preferida).
2. Haz clic en el botón azul **"Nuevo proyecto"** (New Project) en la esquina superior izquierda.
3. Se abrirá un editor de código. Borra cualquier código por defecto (como `function myFunction() {}`).
4. Abre localmente el archivo [`StudyFlow_Presentation_Generator.js`](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/StudyFlow_Presentation_Generator.js), copia todo su contenido y pégalo en el editor de Google Apps Script.
5. Haz clic en el icono del **disco (Guardar)** en la barra de herramientas o presiona `Ctrl + S`. Puedes cambiar el nombre del proyecto de script arriba a *"Generador Presentación StudyFlow"*.
6. Asegúrate de tener seleccionada la función `crearPresentacionStudyFlow` en la barra superior y haz clic en el botón **"Ejecutar"** (Run ▶).
7. **Autorización de Permisos:**
   * Google te solicitará permiso para administrar tus diapositivas en Google Drive. Esto es completamente seguro y normal, ya que el script necesita crear un archivo de Google Slides.
   * Haz clic en *"Revisar permisos"* (Review Permissions), selecciona tu cuenta de Google.
   * Si te aparece la advertencia *"Google no ha verificado esta aplicación"*, haz clic abajo a la izquierda en **"Configuración avanzada"** (Advanced) y luego haz clic en **"Ir a Generador Presentación StudyFlow (no seguro)"**.
   * Haz clic en **"Permitir"** (Allow).
8. Espera unos segundos a que finalice la ejecución. En la consola inferior verás los registros de ejecución y, finalmente, aparecerá impreso el **enlace directo de tu presentación**.
9. Abre el enlace o ve directamente a tu **Google Drive** o [slides.google.com](https://slides.google.com/). ¡Allí estará tu presentación perfectamente creada con todo el contenido, colores corporativos y notas de orador listas!

---

## 🖥️ Paso 2: Ensayar tu Defensa con el Visor Interactivo Web

Para que puedas ensayar tu exposición cómodamente y de forma local sin requerir de conexión a internet o de herramientas externas, hemos programado un presentador web de alta fidelidad estética (Dark Slate Blue + Neon Emerald) y con soporte completo para notas.

### Cómo usar el visor local:
1. Ve a la carpeta [`AI_HELP/presentation/`](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/) y haz doble clic sobre el archivo **`presentation.html`** para abrirlo en Google Chrome, Microsoft Edge o tu navegador de confianza.
2. **Navegación por Diapositivas:**
   * **Avanzar:** Presiona la tecla **Flecha Derecha**, **Espacio**, **Enter** o haz clic en el botón *Siguiente ▶*.
   * **Retroceder:** Presiona la tecla **Flecha Izquierda** o haz clic en el botón *◀ Anterior*.
3. **Notas del Orador Integradas:**
   * Haz clic en el botón **"📝 Notas del Orador"** en la esquina inferior derecha o simplemente presiona la tecla **`N`** de tu teclado.
   * Se abrirá un panel lateral derecho interactivo que cargará dinámicamente tu guión específico de exposición palabra por palabra para la diapositiva que estás visualizando.
   * Esto te permite ensayar los tiempos frente a la pantalla sabiendo exactamente qué decir y cuándo pasar de slide.

---

## 📺 Paso 3: Usar el Lector de Guión e Instrumento de Telelectura Activa

Para aquellos momentos de ensayo de voz o durante la propia grabación del vídeo donde desees leer el guión sin despegar la vista de la cámara, hemos creado **[`script_reader.html`](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/script_reader.html)**. Es una aplicación web companion con funcionalidades premium para el presentador:

### Características clave del Lector Activo:
1. **Modo Guión Estructurado:** Muestra de forma segregada por cada sección:
   * **Visual:** Qué se debe proyectar en pantalla.
   * **Acción:** Cues físicos o clicks del ratón (especialmente críticos en el Live Demo).
   * **Voz:** El guión detallado palabra por palabra con resaltados visuales en términos técnicos.
2. **Modo Teleprónter Auto-scrolling:**
   * Convierte la pantalla en una interfaz de telelectura.
   * Ajusta la velocidad de avance en **palabras por minuto (WPM)** presionando `+` / `-`.
   * Regula el **tamaño de letra (px)** para que lo leas con total comodidad desde la distancia.
   * Presiona **Espacio** o haz clic en *Autoplay* para iniciar/detener el avance de texto.
3. **Cronómetro Inteligente Integrado:**
   * Haz clic en `▶` para iniciar el reloj al empezar tu vídeo.
   * El indicador te alertará cambiando de color (blanco ➔ amarillo ➔ rojo) según te acerques o superes el límite de 20-30 minutos, ayudándote a controlar perfectamente la velocidad de exposición.
4. **Cheat Sheets de Código (Chuleta de Código de Incidencias):**
   * Panel lateral derecho con botones de copia instantánea para los trozos de código clave corregidos de la pre-entrega (`ExamFormController.java` y `Task.java`).
5. **Atajos de Teclado:**
   * `←` / `→` para saltar de diapositiva.
   * `N` para alternar instantáneamente entre el Modo Guión Estructurado y el Modo Teleprónter.
   * `Espacio` para arrancar y detener el Teleprónter.

---

## 🎤 Consejos Clave para una Exposición de Calificación 10

1. **Balance de los Tiempos (Duración: 20-30 minutos):**
   * El profesor especifica que la distribución debe estar compensada. Te aconsejamos reservar **entre 10 y 12 minutos** para explicar las diapositivas teóricas y la arquitectura de sistemas.
   * Dedica los siguientes **12 a 15 minutos** a la demostración práctica e interacciones en tiempo real.
2. **Higiene de Datos en la Demo Técnica:**
   * Evita introducir datos ficticios vacíos o sucios en las tablas (ej: *"asdasd"*, *"prueba1"*, *"aaa"*).
   * Registra asignaturas reales de tu ciclo (*Acceso a Datos*, *Desarrollo de Interfaces*, *Sistemas de Gestión Empresarial*).
   * Registra calificaciones realistas para lucir el cálculo automático de medias ponderadas en el servidor.
3. **Calidad Audiovisual:**
   * Grábate con buena iluminación usando herramientas como OBS Studio, Teams o Google Meet donde se te vea tanto a ti en la webcam como a la presentación/pantallas de demostración en todo momento.
   * Utiliza auriculares con micrófono para mitigar el eco del ambiente y los sonidos del teclado durante la demostración en vivo.
