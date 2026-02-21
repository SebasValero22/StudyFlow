# Proyecto Final: Desarrollo de una PWA con Angular
## StudyFlow
**Tu compañero de estudio personal. Organiza, aprende y progresa.**
**Autor:** Sebastian Valero
**Centro:** IES Camp de Morvedre - DESARROLLO DE INTERFACES

---

## ÍNDICE
1. [1- QUE ES STUDYFLOW](#1--que-es-studyflow)
2. [2- Instrucciones de Lanzamiento y Despliegue](#2--instrucciones-de-lanzamiento-y-despliegue)
3. [3- Justificación de Resultados de Aprendizaje (RAs)](#3--justificacion-de-resultados-de-aprendizaje-ras)

---

## 1- QUE ES STUDYFLOW

StudyFlow es una Progressive Web App (PWA) desarrollada con Angular diseñada para la gestión académica. Funciona como una nube personal donde el frontend se comunica de forma asíncrona con una API RESTful en Spring Boot, conectada a una base de datos MySQL en la nube.

La aplicación es una aplicación individual la cual está hecha para que cada usuario pueda darle un uso el cual se acople a cada uno. Razón por la cual no hay más usuarios.

**Enlaces de Producción:**
* **Frontend (PWA - Netlify):** https://studyflow-pwa.netlify.app
* **Backend (API - Render):** https://studyflow-nuux.onrender.com
* **Base de Datos (Aiven MySQL):** mysql://avnadmin:AVNS_L1WanryGE2wj84BUFmS@mysql-24d1d64a-studyflowddbb.g.aivencloud.com:27865/defaultdb?ssl-mode=REQUIRED
* **Repositorio (GitHub):** https://github.com/SebasValero22/StudyFlow

*(La aplicación se ha hecho en ingles completamente dado que por recomendaciones de profesores debe seguir la estructura del código. Todo el código se intenta que este en el mismo idioma, llame las mismas clases para evitar confusiones y hacerlo de la forma adecuada. Se pretende añadir a futuro una opción de cambio de idioma y una de cambio de contraste)*

---

## 2- Instrucciones de Lanzamiento y Despliegue

1. **Base de Datos:** El backend se conecta automáticamente a la instancia de Aiven MySQL mediante variables de entorno (SPRING_DATASOURCE_URL, USERNAME, PASSWORD).
2. **Backend (Spring Boot):**
   a. El Código se compila e inicializa mediante el Dockerfile ubicado en la ruta StudyFlow/StudyFlow_api/Dockerfile del repositorio.
   b. Expone el puerto 8888 y permite peticiones gracias a la configuración de CORS (@CrossOrigin).
3. **Frontend (Angular PWA):**
   a. Para compilar en local: `ng build --configuration production`
   b. El despliegue en Netlify requiere subir la carpeta `dist/studyflow-pwa/browser`.
   c. Crucial: Se ha implementado un archivo `_redirects` en la raíz con la regla `/* /index.html 200` para evitar errores HTTP 404 al recargar rutas manejadas por el enrutador de Angular.

---

## 3- Justificación de Resultados de Aprendizaje (RAs)

A continuación, se detalla el cumplimiento de los criterios de evaluación exigidos:

**RA1. Genera interfaces de usuario utilizando editores visuales**
* **Criterio 1:** Estructura modular clara. El proyecto frontend de Angular está dividido en al menos 5 módulos funcionales (Overview, Subjects, Tasks, Exams, Grades) ubicados dentro de la carpeta `src/app/features`.
* **Criterio 2:** Separación de responsabilidades. Existe una separación estricta entre la interfaz (plantillas HTML/CSS en los componentes), la lógica de negocio (archivos TypeScript) y el acceso a datos (centralizado en el ApiService mediante inyección de dependencias).

**RA2. Genera interfaces naturales utilizando herramientas visuales**
* **Criterio 1:** Coherencia de navegación. Se ha implementado una barra lateral (Sidebar) consistente que permite cambiar entre vistas usando routerLink. El logotipo "SF" (StudyFlow) actúa de manera visual e intuitiva como punto de anclaje.
* **Criterio 2:** Gestión de estados. La interfaz reacciona de forma natural a la comunicación con la API. Si la API tarda, Angular espera la resolución de los Observables. Además, toda la UI se ha unificado en inglés para mantener un estándar profesional.

**RA3. Crea componentes visuales empleando herramientas específicas**
* **Criterio 1:** Componentes reutilizables. Se ha hecho uso intensivo del Angular CLI para generar servicios y componentes (ej. `ng generate service core/services/api`). El diseño de las tarjetas de asignaturas y tareas son bloques reutilizables de HTML y SCSS.
* **Criterio 2:** Enlace de datos (Data Binding). Las propiedades visuales se vinculan dinámicamente a los datos de la base de datos. Por ejemplo, el uso de directivas como `[ngStyle]` para pintar los bordes de las tareas con el color hexadecimal exacto asignado a su asignatura asociada.

**RA4. Diseña interfaces con criterios de usabilidad y accesibilidad**
* **Criterio 1:** Prevención de errores. A nivel de interfaz, no se muestran jamás los IDs de la base de datos (como taskId o examId) al usuario final, protegiendo la integridad y mejorando la usabilidad.
* **Criterio 2:** Identidad visual accesible. Se ha limitado la paleta de colores de las asignaturas a colores pastel específicos y contrastados, asegurando que el texto superpuesto sea siempre legible.

**RA5. Crea informes utilizando herramientas gráficas**
* **Criterio 1:** Filtrado dinámico de información. El módulo Overview funciona como un informe o panel de control ("Dashboard") dinámico. Combina tareas y exámenes incompletos y los filtra mediante un selector de rango temporal (próximas 1, 2, 3 o 4 semanas).
* **Criterio 2:** Cálculo en tiempo real. El sistema incluye una herramienta que recopila todas las calificaciones (Grades) a través del ApiService y calcula la media ponderada general de forma automática y visual.

**RA6. Documenta aplicaciones utilizando herramientas específicas**
* **Criterio 1:** Uso de estándares. Esta documentación se ha estructurado como texto/Markdown, el estándar profesional para repositorios en la industria tecnológica, detallando flujos, URL y arquitecturas.
* **Criterio 2:** Organización por RAs. El documento justifica técnica y referencialmente el cumplimiento de todos los RAs exigidos en la rúbrica.

**RA7. Prepara las aplicaciones para su distribución**
* **Criterio 1:** Implementación PWA. La aplicación cumple con el objetivo principal del proyecto: es instalable. Cuenta con un manifest.webmanifest personalizado con iconos de alta calidad (512x512) y colores de tema (#0d1b2a), junto con un Service Worker activo (ngsw-worker.js) generado mediante `@angular/pwa`.
* **Criterio 2:** Gestión de configuraciones externas. El proyecto está distribuido. El backend maneja las credenciales mediante variables de entorno en Render (SPRING_DATASOURCE_URL), ocultando la lógica local y permitiendo que el contenedor Docker conecte limpiamente con la nube de Aiven.

**RA8. Evalúa el comportamiento de la aplicación diseñando pruebas**
* **Criterio 1:** Pruebas de conexión e integración. Se han llevado a cabo pruebas manuales verificando la comunicación cruzada (CORS). Se resolvieron bloqueos de acceso permitiendo orígenes cruzados (`@CrossOrigin`) en los Controladores Java tras verificar errores 404 y fallos de tipo handshake en la consola del navegador.
* **Criterio 2:** Manejo de rutas. Se probó el comportamiento del servidor web estático (Netlify) ante recargas de página en rutas internas, resolviendo la caída de la aplicación mediante la inyección del archivo de reglas de enrutamiento `_redirects` en el directorio de construcción final.