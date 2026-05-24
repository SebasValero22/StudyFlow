# 🎙️ Guion de Presentación Profesional - StudyFlow (Defensa de Proyecto)

> [!TIP]
> ### 🚀 ¡NUEVO: LECTOR DE GUIÓN INTERACTIVO Y TELEPROMPTER!
> Hemos diseñado y programado una aplicación web de telelectura e interactividad premium para ti. 
> Puedes abrirla haciendo doble clic en: [**`script_reader.html` (Lector de Guion & Teleprompter)**](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/script_reader.html)
> **¿Qué incluye?**
> *   **Modo Teleprompter:** Texto ultra-legible de gran formato con desplazamiento automático (Autoplay), velocidad ajustable en palabras por minuto (WPM) y tamaño de fuente regulable.
> *   **Cronómetro de sesión en tiempo real:** Te avisa visualmente si estás en tiempo (20-30 minutos) con transiciones de color.
> *   **Panel de Chuletas de Código (Cheat Sheet):** Copia directa de las correcciones de pre-evaluación (`ExamFormController.java` y `Task.java`) para el bloque de la demostración.
> *   **Navegación ágil por teclado:** Usa `←` / `→` para cambiar de sección, `Espacio` para pausar y `N` para cambiar de modo de vista.

Este documento es tu guía definitiva y detallada para la defensa en vídeo de **StudyFlow** (2º de DAM). Está estructurado diapositiva por diapositiva (alineado al 100% con tu presentador interactivo local [`presentation.html`](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/presentation.html) y tu Google Presentation), indicándote qué mostrar en pantalla, qué decir palabra por palabra de forma profesional y natural, y cómo llevar a cabo una demostración técnica en vivo espectacular.

---

## ⏱️ Distribución Estratégica del Tiempo (Total: 22 - 25 Minutos)

Para cumplir con la rúbrica oficial y asegurar una defensa madura y fluida, dividiremos el vídeo en dos partes principales:

```
┌───────────────────────────────────────────────┬───────────────────────────────────────────────┐
│     Parte 1: Presentación de Diapositivas     │        Parte 2: Demostración Práctica         │
│            (Aprox. 10 - 12 Minutos)           │            (Aprox. 12 - 13 Minutos)           │
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ • Slide 1: Portada y Saludo (1:30)            │ • Login seguro y aislamiento multiusuario     │
│ • Slide 2: Hoja de Ruta y Agenda (1:00)       │ • Alta de asignaturas con colores Pantone     │
│ • Slide 3: El Vacío y Solución (1:30)         │ • Gestión de tareas y prioridad               │
│ • Slide 4: Stack Tecnológico Unificado (2:00) │ • Examen con ComboBox StringConverter FIXED   │
│ • Slide 5: Arquitectura REST Capas (1:30)     │ • Checkbox Done con badges reactivas FIXED    │
│ • Slide 6: Clientes y Calidad 2.0 (2:00)      │ • Persistencia y Jackson isCompleted FIXED    │
│ • Slide 7: Viabilidad y Presupuesto (1:00)    │ • Cálculo de promedios ponderados al vuelo    │
│ • Slide 8: Escalabilidad y Roadmap (1:00)      │ • Sincronización en Angular PWA y Android      │
│ • Slide 9: Cierre de la Teoría (0:30)         │ • Muestra rápida de 2 ficheros de código      │
└───────────────────────────────────────────────┴───────────────────────────────────────────────┘
```

---

## 🖥️ PARTE 1: DEFENSA CON DIAPOSITIVAS (PASO A PASO)

> [!NOTE]
> **Recomendación para la grabación:** Abre [`presentation.html`](file:///f:/proyecto%20intermodular/StudyFlow-main/AI_HELP/presentation/presentation.html) en tu navegador. Configura OBS Studio o MS Teams para capturar la pantalla en pantalla completa y tu cámara web en una esquina inferior pequeña. Habla con un tono calmado, profesional y seguro.

---

### 🟪 Slide 1: Portada de la Exposición
*   **Visual en Pantalla:** Logo corporativo esmeralda brillante de StudyFlow con tus metadatos personales (Ciclo, Autor, Institución).
*   **Acción del Presentador:** Mantén contacto visual con la cámara, sonríe y saluda con seguridad.
*   **Discurso del Orador:**
    > *"Hola, buenos días/tardes a los miembros del tribunal evaluador. Mi nombre es Juan Sebastián Valero Marulanda, estudiante de segundo año del Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma en el IES Camp de Morvedre. Hoy tengo el honor de presentarles la defensa técnica de mi proyecto intermodular: **StudyFlow**.*
    > 
    > *Este proyecto no es solo una aplicación aislada, sino un ecosistema multiplataforma completo, diseñado bajo estándares profesionales de ingeniería de software, cuyo propósito es modernizar, centralizar e integrar la gestión de la vida académica del estudiante."*

---

### 🟪 Slide 2: Hoja de Ruta del Proyecto
*   **Visual en Pantalla:** Una cuadrícula interactiva de 8 tarjetas visuales numeradas que guiarán la exposición teórica y práctica.
*   **Acción del Presentador:** Haz un breve resumen de la agenda para estructurar el vídeo.
*   **Discurso del Orador:**
    > *"Para guiar esta exposición de la manera más clara y estructurada posible, seguiremos la hoja de ruta que ven en pantalla. Comenzaremos justificando la necesidad del proyecto analizando las carencias del mercado académico. Luego, profundizaremos en la infraestructura tecnológica del servidor REST y las decisiones técnicas de diseño multiplataforma en los clientes.*
    > 
    > *A continuación, nos detendremos en el bloque de Calidad del Software (Versión 2.0), donde les detallaré cómo he resuelto y validado cada una de las incidencias de pre-entrega. Analizaremos también la viabilidad económica real, la planificación futura del roadmap, y daremos paso inmediato a la demostración práctica interactiva en tiempo real consumiendo los servicios en nube."*

---

### 🟪 Slide 3: El Vacío en la Gestión Académica
*   **Visual en Pantalla:** Diagrama comparativo. A la izquierda, 3 tarjetas grises que representan la desorganización de Notion, Todoist y Excel; a la derecha, la tarjeta brillante "StudyFlow All-in-One".
*   **Acción del Presentador:** Usa un tono empático. Enfatiza el problema y cómo tu aplicación aporta valor diferencial.
*   **Discurso del Orador:**
    > *"Cualquier estudiante se enfrenta hoy en día a un gran desafío: la dispersión y desorganización de su información. Para resolverlo, solemos recurrir a herramientas fragmentadas: Notion para tomar notas estáticas sin ningún dinamismo; Todoist para apuntar tareas simples pero sin contexto escolar; y Excel o calendarios rígidos que requieren una configuración diaria tediosa y que no nos ayudan a computar nuestras calificaciones ponderadas de forma automatizada.*
    > 
    > *StudyFlow nace precisamente para llenar ese vacío crítico, integrando en una única solución inteligente, nativa y multiplataforma todo lo que el estudiante del siglo XXI necesita: sus asignaturas, control de exámenes, pesos de tareas y cálculo de rendimiento académico en tiempo real."*

---

### 🟪 Slide 4: Stack Tecnológico Unificado
*   **Visual en Pantalla:** 5 columnas de colores representativos que segmentan el ecosistema tecnológico (Java/Spring Boot, MySQL/Aiven, JavaFX/MVC, Angular PWA, Android Nativo).
*   **Acción del Presentador:** Destaca la complejidad tecnológica al integrar múltiples lenguajes e interoperabilidad JSON.
*   **Discurso del Orador:**
    > *"La verdadera potencia de StudyFlow reside en su capacidad para unificar entornos tecnológicos heterogéneos mediante un estándar de comunicación robusto. En el Backend, el cerebro lógico está construido en **Java 17/21 con Spring Boot 3.x**, utilizando **Spring Data JPA y Hibernate** para la persistencia.*
    > 
    > *La base de datos es **MySQL 8.x** administrada en la nube con **Aiven Cloud**, lo que nos garantiza alta disponibilidad. En la capa cliente, tenemos tres frontends independientes:*
    > * *Primero, el cliente de escritorio desarrollado en **JavaFX 19** bajo patrón MVC e hilos asíncronos para un uso intensivo.*
    > * *Segundo, el cliente web responsive, que es una **Progressive Web App construida en Angular 17** con Service Workers para almacenamiento offline.*
    > * *Y tercero, el cliente móvil nativo en **Android con Kotlin/Java** y consumo de servicios mediante **Retrofit 2 REST**, adaptado a las pautas estéticas modernas de **Material Design 3**."*

---

### 🟪 Slide 5: Arquitectura y Estructura del Servidor
*   **Visual en Pantalla:** Diagrama de flujo vertical que ilustra la arquitectura multicapa (Controllers ➔ Services ➔ Repositories ➔ DB).
*   **Acción del Presentador:** Explica la lógica en el servidor para evitar duplicidades de código en clientes.
*   **Discurso del Orador:**
    > *"Como ingenieros multiplataforma, no podemos cometer el error de duplicar la lógica de negocio en cada cliente. Por ello, StudyFlow implementa una **Arquitectura orientada a servicios (SOA)**. Nuestro API REST actúa como el motor centralizado y sin estado (stateless) de toda la aplicación.*
    > 
    > *Toda lógica compleja —como el cálculo matemático de medias ponderadas de asignaturas— se ejecuta exclusivamente en el servidor. Esto nos garantiza consistencia absoluta de datos sin importar desde qué cliente los consultemos. Asimismo, como ven en el flujo de la derecha, estructuramos el backend en capas perfectamente desacopladas: controladores encargados de exponer los endpoints REST y mapear DTOs; servicios que ejecutan la lógica de negocio; repositorios JPA que aíslan el acceso físico; y la base de datos MySQL en la nube."*

---

### 🟪 Slide 6: Clientes y Calidad de Software (Ver. 2.0)
*   **Visual en Pantalla:** 3 mockups minimalistas que muestran los frontends e indican los bugs corregidos de la pre-entrega.
*   **Acción del Presentador:** Muestra orgullo por el pulido técnico y la calidad del código, preparando el terreno para la demo.
*   **Discurso del Orador:**
    > *"El desarrollo de la Versión 2.0 se ha centrado rigurosamente en garantizar un producto de nivel profesional, abordando exhaustivamente el informe de calidad y feedback de la pre-entrega. En primer lugar, mejoramos la interfaz de usuario en la Web PWA y móvil sustituyendo los booleanos crudos por etiquetas de estado elegantes: **✓ HECHA** en verde y **✗ SIN HACER** en gris.*
    > 
    > *Por seguridad, ocultamos todos los identificadores autoincrementales técnicos de la base de datos para mitigar ataques de enumeración. En la aplicación de escritorio, solucionamos un problema de contraste visual: ya no pintamos molestos fondos claros para las asignaturas, sino que aplicamos un sofisticado borde indicador izquierdo de 5 píxeles con el color Pantone seleccionado. Además, solventamos un bug de Jackson que provocaba que las tareas no mantuvieran su estado en base de datos al crearse nuevas incidencias, y refinamos los ComboBox mediante un `StringConverter` para que representen limpiamente los nombres en la UI. Todo esto lo veremos ahora funcionando."*

---

### 🟪 Slide 7: Viabilidad del Proyecto y Costes
*   **Visual en Pantalla:** Línea temporal de 5 quincenas de desarrollo a la izquierda, y factura de infraestructura en consola monospaciada con un presupuesto final de 4.515,00 € a la derecha.
*   **Acción del Presentador:** Explica la estimación financiera de forma realista.
*   **Discurso del Orador:**
    > *"Para validar la viabilidad económica real de StudyFlow, hemos realizado una estimación financiera rigurosa de costes de ingeniería y operación. El desarrollo del proyecto se estructuró a lo largo de 5 quincenas de trabajo incremental. Estimando un total de 240 horas de dedicación de ingeniería con una tarifa estándar junior de 18 euros la hora, el coste de personal asciende a **4.320 euros**.*
    > 
    > *Adicionalmente, calculamos los costes de infraestructura y licencias en la nube: 60 euros anuales para el servidor en Render API, 110 euros anuales para el alojamiento Aiven MySQL, la licencia vitalicia de Google Play Console de 25 euros y hosting gratuito en Vercel para el frontend. El presupuesto de lanzamiento real se sitúa en **4.515,00 euros**, demostrando ser un producto altamente viable, robusto y económicamente competitivo."*

---

### 🟪 Slide 8: Roadmap Tecnológico de Escalabilidad
*   **Visual en Pantalla:** Escalera diagonal interactiva de 4 peldaños ascendentes (Push, OAuth2, Colaborativo, IA).
*   **Acción del Presentador:** Explica la proyección del negocio a futuro.
*   **Discurso del Orador:**
    > *"StudyFlow no se detiene en esta entrega; se ha diseñado con bases preparadas para una escalabilidad masiva y proyección a futuro. Nuestra hoja de ruta técnica se compone de 4 peldaños ascendentes:*
    > * *En el primer escalón, implementaremos notificaciones push nativas al móvil del estudiante para recordarles exámenes.*
    > * *En el segundo peldaño, integraremos autenticación robusta mediante OAuth2 con login social de Google y GitHub.*
    > * *El tercer nivel dotará a la aplicación de un entorno colaborativo real, permitiendo a alumnos del mismo ciclo compartir tareas y apuntes.*
    > * *En la cumbre del roadmap, integraremos un planificador predictivo con Inteligencia Artificial y exportación de informes en PDF."*

---

### 🟪 Slide 9: Cierre y Transición a la Demo
*   **Visual en Pantalla:** Portada de cierre formal con tus datos de contacto y un botón de "Demostración Técnica en Vivo".
*   **Acción del Presentador:** Agradece la atención teórica y cambia de pantalla de forma limpia hacia el IDE / Aplicaciones en ejecución.
*   **Discurso del Orador:**
    > *"Con esto doy por concluida la síntesis teórica del proyecto. Para demostrar la robustez, consistencia y sincronización en tiempo real multiplataforma de la que hemos estado hablando, daremos paso a la demostración técnica práctica. Muchas gracias por su atención y pasemos a la acción."*

---

## 💻 PARTE 2: GUION DE LA DEMOSTRACIÓN TÉCNICA (EN VIVO)

> [!IMPORTANT]
> **Preparativos previos:** 
> 1. Ten abiertas en tu pantalla las tres aplicaciones: el cliente de Escritorio JavaFX, el navegador web con la PWA en localhost o Vercel, y el Emulador de Android.
> 2. Asegúrate de tener una cuenta de pruebas limpia, pero con datos reales. Por ejemplo, la asignatura *"Acceso a Datos"* ya precargada con algunos exámenes reales de tu ciclo.
> 3. Abre tu entorno de desarrollo (IntelliJ o VS Code) en segundo plano con dos archivos clave listos para mostrar: `ExamFormController.java` y `Task.java`.

---

### 🟦 1. Login Seguro y Aislamiento Multiusuario
*   **Acción:** Enfoca el cliente JavaFX. Introduce tu usuario y contraseña de pruebas y haz clic en *Login*.
*   **Guión:**
    > *"Comenzamos en la aplicación de escritorio JavaFX. Como pueden observar, nos recibe una pantalla de acceso segura. Iniciamos sesión con mis credenciales de prueba. El sistema valida los datos contra el API REST en la nube y nos da paso a nuestra área de trabajo personal.*
    > 
    > *Un aspecto de seguridad crucial implementado es que toda la lógica de persistencia y consumo en el backend aplica un aislamiento absoluto: cada consulta SQL en la API REST inyecta dinámicamente el `userId` en sesión. Esto garantiza que un estudiante jamás pueda visualizar o modificar las tareas o calificaciones de otro usuario, cumpliendo con los estándares OWASP de control de acceso seguro."*

### 🟦 2. Creación de Asignatura y Accesibilidad (Corrección de Colores)
*   **Acción:** Haz clic en la sección *Subjects* (Asignaturas). Haz clic en *Añadir Asignatura*. Introduce el nombre *"Sistemas de Gestión Empresarial"* y selecciona un color corporativo (por ejemplo, verde esmeralda o azul marino).
*   **Guión:**
    > *"Nos dirigimos a la pestaña de Asignaturas. Aquí he precargado algunas asignaturas reales de nuestro ciclo de DAM para darle total realismo a la demostración. Vamos a dar de alta una nueva asignatura haciendo clic en el botón Añadir. La llamaremos 'Sistemas de Gestión Empresarial' y le asociaremos el año académico actual.*
    > 
    > *Fíjense en un detalle de usabilidad: he seleccionado un color oscuro corporativo con excelente contraste. Siguiendo el feedback de diseño de la pre-entrega, hemos reemplazado el color rosa pastel por defecto por colores Pantone oscuros y sólidos. En lugar de pintar un fondo de tarjeta chillón que quema la vista y reduce la accesibilidad del texto, ahora pintamos un sofisticado y limpio borde izquierdo indicador de 5 píxeles con el color representativo, logrando una estética ultra-premium y profesional."*

### 🟦 3. Alta de Tareas y Programación de Exámenes (¡Destaca el Fix del ComboBox!)
*   **Acción:** 
    1. Ve a la sección de *Tasks* (Tareas). Añade una tarea llamada *"Mapeo Entidades JPA"*, selecciónale prioridad Alta, y asígnale la nueva asignatura.
    2. Ve a la sección de *Exams* (Exámenes). Añade un examen llamado *"Examen JPA y Hibernate"*. **Abre el ComboBox de asignaturas de forma pausada en pantalla.**
*   **Guión:**
    > *"Ahora crearemos una tarea asociada. Vamos a la vista de Tareas, hacemos clic en Nueva Tarea y rellenamos la información: 'Mapeo Entidades JPA', prioridad Alta, y le asociamos nuestra nueva asignatura de 'Sistemas de Gestión Empresarial'.*
    > 
    > *A continuación, programaremos un examen para esta materia. Vamos a la vista de Exámenes y hacemos clic en el botón de creación. **Les ruego presten especial atención al selector desplegable de asignaturas.** Como pueden ver, el ComboBox nos representa con total limpieza exclusivamente el nombre de la asignatura:* **'Sistemas de Gestión Empresarial'**. 
    > 
    > *Hemos solucionado de raíz la incidencia señalada en la pre-entrega, donde por defecto JavaFX volcaba la representación interna String del objeto Lombok `Subject(subjectId=4, nameSubject=...)` en lugar del nombre legible. Implementando de forma nativa una celda personalizada `cellFactory` y una `buttonCell` de JavaFX basadas en un `StringConverter`, la experiencia visual del usuario es perfecta y sumamente elegante."*

### 🟦 4. Marcado DONE de Tareas y Persistencia Simétrica (¡Destaca el Fix de Jackson!)
*   **Acción:** 
    1. Regresa a la tabla de Tareas en JavaFX. Haz clic en la columna de verificación para marcar la tarea como Completada. 
    2. Muestra cómo cambia visualmente el estado.
    3. Haz clic en el botón *Refrescar* de la tabla (o abre la ventana de añadir una nueva tarea) para demostrar que el estado **no se resetea a FALSE**.
*   **Guión:**
    > *"Volvemos a nuestra lista de tareas. Al completar nuestro trabajo, podemos marcarlo directamente en la columna de verificación de la tabla. Al hacerlo, verán que la interfaz de usuario cambia de forma totalmente dinámica y reactiva, pintando una etiqueta formateada que dice* **'✓ HECHA'** *en verde negrita, o* **'✗ SIN HACER'** *en gris.*
    > 
    > *Pero lo más importante está detrás del telón: **hemos corregido por completo el bug crítico de reseteo de tareas**. En la versión preliminar, al marcar una tarea como completada y refrescar la tabla o crear una tarea posterior, el estado del checkbox se reseteaba a FALSE debido a una inconsistencia de deserialización JSON de Jackson con el campo boolean `isCompleted` de Lombok en Spring Boot y JavaFX.*
    > 
    > *Hemos reestructurado el modelo serializando manualmente el getter y setter mediante la propiedad `@JsonProperty("isCompleted")`, a la vez que corregimos el formato del campo `due_date` entre las plataformas. Ahora, la persistencia es 100% simétrica y permanente en la base de datos MySQL."*

### 🟦 5. Autocalculación de Calificaciones ponderadas
*   **Acción:** Ve a la sección *Grades* (Calificaciones). Introduce dos notas para *"Sistemas de Gestión Empresarial"*: un 8.0 en el Examen Teórico (peso 60%) y un 9.5 en la Práctica (peso 40%). Muestra cómo el promedio se calcula automáticamente como **8.6**.
*   **Guión:**
    > *"Para probar nuestro motor matemático, nos dirigimos a la pestaña de Calificaciones. Para nuestra asignatura recién creada, vamos a registrar dos notas con diferentes pesos: un examen teórico con una nota de 8.0 y un peso del 60%, y una práctica de laboratorio con un 9.5 y un peso del 40%.*
    > 
    > *Al instante, el sistema calcula de forma exacta la media ponderada del alumno, arrojando un promedio exacto de 8.6. Toda esta computación matemática se ejecuta en caliente en el API REST, eliminando cualquier discrepancia de cálculo entre interfaces y garantizando un control riguroso de su progreso."*

### 🟦 6. Sincronización Multiplataforma en Tiempo Real (Angular PWA y Android)
*   **Acción:** 
    1. Abre tu navegador y muestra la aplicación web **Angular PWA** abierta en localhost o producción. Loguéate con la misma cuenta. Muestra que la asignatura *"Sistemas de Gestión Empresarial"*, la tarea con su badge de completada y la nota promedio calculada de **8.6** ya están ahí sincronizadas.
    2. Enfoca el **Emulador de Android** en tu pantalla. Entra con el mismo usuario y muestra exactamente la misma información.
*   **Guión:**
    > *"Para constatar el verdadero valor de nuestra arquitectura distribuida orientada a servicios, abrimos la aplicación web **Progressive Web App construida en Angular 17**. Al acceder con la misma cuenta, verán cómo la asignatura 'Sistemas de Gestión Empresarial' y las notas que acabamos de registrar en la aplicación de escritorio ya aparecen aquí sincronizadas en tiempo real.*
    > 
    > *Y no solo eso, aquí pueden observar en el **Emulador de Android Nativo** cómo nuestra app nativa móvil en Kotlin también refleja las tareas pendientes al instante, demostrando que toda la información fluye con absoluta coherencia bidireccional mediante nuestro API centralizado. Es un ecosistema multiplataforma real y listo para el mercado escolar."*

### 🟦 7. Demostración Rápida de Código Fuente
*   **Acción:** Abre IntelliJ IDEA / VS Code en pantalla. Muestra brevemente y con letra grande dos bloques de código:
    1. El `StringConverter` configurado en `ExamFormController.java` (donde se corrigen los ComboBoxes).
    2. Los getters y setters anotados con `@JsonProperty("isCompleted")` en `Task.java`.
*   **Guión:**
    > *"Antes de concluir, quiero mostrarles rápidamente en el entorno de desarrollo cómo se ve reflejada esta calidad de código en la base física. *
    > * *Aquí pueden ver en* `ExamFormController.java` *cómo configuramos la celda personalizada y el botón de visualización del selector, utilizando el converter para desvincular Lombok de la interfaz visual.*
    > * *Y en este otro fragmento del modelo* `Task.java` *en el cliente, pueden apreciar los getters y setters explícitos serializados con* `@JsonProperty("isCompleted")` *que solucionaron el comportamiento asimétrico de Jackson.*
    > * *Esto demuestra que detrás de la interfaz de usuario de StudyFlow hay un desarrollo limpio, maduro y sólidamente estructurado."*

---

## 🟪 FINAL DEL VÍDEO (CONCLUSIÓN)

*   **Acción:** Vuelve a enfocar tu cámara web en grande si es posible, o mantente en la diapositiva de cierre formal de la presentación.
*   **Discurso del Orador:**
    > *"Con esto doy por concluida la demostración práctica y la defensa de StudyFlow. Este proyecto intermodular me ha permitido consolidar de forma integral todos los conocimientos adquiridos a lo largo del ciclo formativo de DAM, capacitándome para resolver problemas reales de sincronización de datos y arquitectura distribuida multiplataforma.*
    > 
    > *Agradezco enormemente su atención y quedo a su entera disposición para responder cualquier pregunta o aclarar cualquier aspecto técnico del código o la base de datos. Muchas gracias."*
