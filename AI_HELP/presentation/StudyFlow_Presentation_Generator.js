/**
 * ==========================================================================================
 * STUDYFLOW - GENERADOR DE PRESENTACIÓN PARA GOOGLE SLIDES (VER. 2.0 - VISUAL PREMIUM)
 * ==========================================================================================
 * 
 * Este script automatiza la creación de una presentación moderna, altamente visual
 * e infográfica en Google Slides para la defensa de tu proyecto "StudyFlow" (2º DAM).
 * 
 * MEJORAS DE LA VERSIÓN 2.0 (MENOS TEXTO, BASADO EN EL PDF DE LA DEFENSA):
 * - Estructura Oficial de 9 Diapositivas: Alineada con tu defensa.
 * - Diagramación Programática con Formas: En lugar de viñetas aburridas, dibuja:
 *   * Hoja de Ruta: Una cuadrícula de 8 tarjetas visuales numeradas.
 *   * El Vacío Académico: Columnas comparativas con flechas de flujo SVG y tarjeta brillante.
 *   * Stack Tecnológico: 5 paneles verticales coloreados con marcas.
 *   * Arquitectura: Un diagrama de flujo funcional de capas de Spring Boot (Controllers ➔ Services ➔ Repositories).
 *   * Desarrollo Multiplataforma: 3 mockups visuales con la lista de incidencias resueltas de pre-entrega.
 *   * Viabilidad: Tabla de costes detallada ("factura") que calcula el presupuesto real de 4.515 €.
 *   * Roadmap Escalabilidad: Escalera diagonal ascendente dibujada con rectángulos flotantes.
 * - Sincronización de Notas: Inyecta el guion completo de 20-30 minutos en cada slide.
 * 
 * CÓMO USAR ESTE SCRIPT:
 * 1. Ve a https://script.google.com/
 * 2. Haz clic en "Nuevo proyecto" (New Project).
 * 3. Borra el código existente y pega todo este contenido.
 * 4. Haz clic en el botón "Ejecutar" (Run).
 * 5. Concede los permisos a tu Google Drive.
 * 6. Abre el enlace directo impreso en la consola para ver tu presentación visual premium.
 */

function crearPresentacionStudyFlow() {
  Logger.log("Iniciando la generación de la presentación 'StudyFlow' (Ver. 2.0 Visual)...");
  
  // 1. Crear la presentación
  var pres = SlidesApp.create("Presentación Proyecto Intermodular - StudyFlow (Visual)");
  
  // Limpiar diapositivas por defecto
  var slides = pres.getSlides();
  while (slides.length > 0) {
    slides[0].remove();
    slides = pres.getSlides();
  }
  
  // Paleta de Colores Corporativos
  var COLOR_FONDO = "#090d16";        // Dark slate azulado profundo
  var COLOR_TEXTO_TITULO = "#10b981";   // Emerald Green (Acento primario)
  var COLOR_TEXTO_CYAN = "#06b6d4";     // Cyan (Acento secundario)
  var COLOR_TEXTO_BLANCO = "#f3f4f6";   // Gris claro
  var COLOR_TEXTO_MUTED = "#9ca3af";    // Gris medio
  var COLOR_TARJETA = "#111827";        // Gris oscuro para tarjetas
  var COLOR_TARJETA_BORDER = "#1f2937"; // Borde sutil de tarjeta
  
  // Colores del Stack
  var COLOR_BACKEND = "#3b82f6";
  var COLOR_DATABASE = "#06b6d4";
  var COLOR_DESKTOP = "#eab308";
  var COLOR_PWA = "#ef4444";
  var COLOR_ANDROID = "#22c55e";

  // Helper para añadir la línea de acento de marca en el tope de cada slide
  function addAccentHeader(slide) {
    var line = slide.insertShape(SlidesApp.ShapeType.RECTANGLE, 0, 0, 720, 5);
    line.getFill().setSolidFill(COLOR_TEXTO_CYAN);
    line.getBorder().setTransparent();
  }
  
  // Helper para configurar el fondo del slide
  function setSlideBackground(slide) {
    slide.getBackground().setSolidFill(COLOR_FONDO);
  }

  // Helper para añadir título de diapositiva
  function addSlideHeader(slide, title, indexText) {
    addAccentHeader(slide);
    setSlideBackground(slide);
    
    // Título
    var titleBox = slide.insertTextBox(40, 25, 500, 45);
    var titleRange = titleBox.getText();
    titleRange.setText(title);
    titleRange.getTextStyle()
      .setFontFamily("Trebuchet MS")
      .setFontSize(26)
      .setBold(true)
      .setForegroundColor(COLOR_TEXTO_BLANCO);
    
    // Índice Categoría
    var indexBox = slide.insertTextBox(540, 25, 140, 45);
    var indexRange = indexBox.getText();
    indexRange.setText(indexText);
    indexRange.getTextStyle()
      .setFontFamily("Calibri")
      .setFontSize(10)
      .setBold(true)
      .setForegroundColor(COLOR_TEXTO_MUTED);
    indexBox.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  }
  
  // ========================================================================================
  // SLIDE 1: PORTADA
  // ========================================================================================
  Logger.log("Creando Diapositiva 1: Portada...");
  var slide1 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  setSlideBackground(slide1);
  addAccentHeader(slide1);
  
  // Logo Simulado
  var logoOuter = slide1.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 325, 30, 70, 70);
  logoOuter.getFill().setSolidFill(COLOR_TARJETA);
  logoOuter.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(2);
  
  var logoInner = slide1.insertShape(SlidesApp.ShapeType.DIAMOND, 345, 50, 30, 30);
  logoInner.getFill().setSolidFill(COLOR_TEXTO_TITULO);
  logoInner.getBorder().setTransparent();

  // Título Principal
  var titleBox1 = slide1.insertTextBox(40, 115, 640, 65);
  var titleRange1 = titleBox1.getText();
  titleRange1.setText("STUDYFLOW");
  titleRange1.getTextStyle()
    .setFontFamily("Trebuchet MS")
    .setFontSize(48)
    .setBold(true)
    .setForegroundColor(COLOR_TEXTO_TITULO);
  titleBox1.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  // Subtítulo
  var subtitleBox1 = slide1.insertTextBox(40, 175, 640, 45);
  var subtitleRange1 = subtitleBox1.getText();
  subtitleRange1.setText("Gestión Académica Multiplataforma");
  subtitleRange1.getTextStyle()
    .setFontFamily("Calibri")
    .setFontSize(16)
    .setItalic(true)
    .setForegroundColor(COLOR_TEXTO_MUTED);
  subtitleBox1.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  // Badge
  var badge1 = slide1.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 260, 225, 200, 28);
  badge1.getFill().setSolidFill("rgba(16, 185, 129, 0.15)");
  badge1.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(1);
  badge1.getText().setText("DEFENSA DE PROYECTO FINAL")
    .getTextStyle()
    .setFontFamily("Calibri")
    .setFontSize(10)
    .setBold(true)
    .setForegroundColor(COLOR_TEXTO_TITULO);
  badge1.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);

  // Metadatos
  var metaBox = slide1.insertTextBox(40, 275, 640, 80);
  var metaRange = metaBox.getText();
  metaRange.setText(
    "AUTOR: Juan Sebastián Valero Marulanda       |       CICLO: 2º DAM (Multiplataforma)\n" +
    "INSTITUCIÓN: IES Camp de Morvedre       |       MÓDULO: Proyecto Intermodular"
  );
  metaRange.getTextStyle()
    .setFontFamily("Calibri")
    .setFontSize(11)
    .setForegroundColor(COLOR_TEXTO_BLANCO);
  metaBox.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  var notes1 = 
    "Hola, buenos días/tardes. Mi nombre es Juan Sebastián Valero Marulanda, estudiante de 2º de DAM " +
    "(Desarrollo de Aplicaciones Multiplataforma), y hoy voy a presentar mi proyecto intermodular: StudyFlow.\n\n" +
    "Este proyecto representa la culminación del ciclo formativo y ha sido diseñado de principio a fin " +
    "como un ecosistema multiplataforma moderno, robusto y 100% orientado a optimizar la vida académica " +
    "del estudiante. A lo largo de esta defensa técnica, analizaremos los problemas de gestión actuales, " +
    "cómo la arquitectura multiplataforma orientada a servicios los resuelve de forma nativa, las " +
    "tecnologías involucradas, el modelado y persistencia de datos, y el riguroso pulido técnico " +
    "que hemos llevado a cabo en la versión 2.0 para garantizar un producto de nivel profesional.";
  slide1.getNotesPage().getNotesBody().setText(notes1);
  
  // ========================================================================================
  // SLIDE 2: HOJA DE RUTA (8 TARJETAS TIMELINE)
  // ========================================================================================
  Logger.log("Creando Diapositiva 2: Hoja de Ruta...");
  var slide2 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide2, "Hoja de Ruta del Proyecto", "01 / ESTRUCTURA");
  
  var timelineSteps = [
    { num: "1", title: "Planteamiento", desc: "Análisis de Mercado" },
    { num: "2", title: "Arquitectura", desc: "Core API REST" },
    { num: "3", title: "Stack Tecnológico", desc: "Java, Kotlin & Angular" },
    { num: "4", title: "Modelo de Datos", desc: "MySQL Nube (Aiven)" },
    { num: "5", title: "Calidad de Software", desc: "Auditoría Ver. 2.0" },
    { num: "6", title: "Viabilidad", desc: "Presupuesto & Horas" },
    { num: "7", title: "Roadmap y Futuro", desc: "Escalabilidad" },
    { num: "8", title: "Demo Técnica", desc: "Live Demo Activa" }
  ];
  
  // Dibujar cuadrícula de 4x2
  var cardWidth = 145;
  var cardHeight = 115;
  var startX = 40;
  var gapX = 20;
  
  for (var i = 0; i < timelineSteps.length; i++) {
    var row = Math.floor(i / 4);
    var col = i % 4;
    var x = startX + col * (cardWidth + gapX);
    var y = 90 + row * (cardHeight + 20);
    
    var card = slide2.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, x, y, cardWidth, cardHeight);
    card.getFill().setSolidFill(COLOR_TARJETA);
    if (i === 7) {
      card.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(2);
    } else {
      card.getBorder().setSolidFill(COLOR_TARJETA_BORDER).setWeight(1);
    }
    
    // Contenido
    var textRange = card.getText();
    textRange.setText(timelineSteps[i].num + ". " + timelineSteps[i].title + "\n\n" + timelineSteps[i].desc);
    textRange.getTextStyle().setFontFamily("Calibri").setFontSize(9).setForegroundColor(COLOR_TEXTO_MUTED);
    textRange.find(timelineSteps[i].num + ". " + timelineSteps[i].title).getTextStyle()
      .setBold(true)
      .setFontSize(11)
      .setForegroundColor(i === 7 ? COLOR_TEXTO_TITULO : COLOR_TEXTO_CYAN);
  }
  
  var notes2 = 
    "Para guiar esta exposición de la forma más estructurada posible, seguiremos la hoja de ruta oficial ilustrada en pantalla.\n\n" +
    "Comenzaremos exponiendo el planteamiento inicial de la idea y las carencias críticas que detectamos en el mercado académico. " +
    "Luego, nos detendremos en el diseño de arquitectura y la interoperabilidad de nuestro ecosistema SOA, detallando el stack " +
    "tecnológico elegido para cada plataforma cliente. A continuación, explicaremos el esquema relacional de base de datos normalizado " +
    "y daremos paso a la sección de Calidad del Software. Aquí demostraré detalladamente cómo he resuelto cada una de las " +
    "incidencias señaladas por los evaluadores en la pre-entrega. Finalmente, cerraremos con el balance financiero de costes y la " +
    "planificación futura, dando paso inmediato a la demostración práctica interactiva.";
  slide2.getNotesPage().getNotesBody().setText(notes2);

  // ========================================================================================
  // SLIDE 3: EL VACÍO EN LA GESTIÓN ACADÉMICA
  // ========================================================================================
  Logger.log("Creando Diapositiva 3: El Vacío...");
  var slide3 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide3, "El Vacío en la Gestión Académica", "02 / MOTIVACIÓN");
  
  // Columna Izquierda: Fragmentación
  var fragTitle = slide3.insertTextBox(40, 80, 290, 35);
  fragTitle.getText().setText("⚠️ Herramientas Fragmentadas").getTextStyle()
    .setFontFamily("Trebuchet MS").setFontSize(15).setBold(true).setForegroundColor("#ef4444");
    
  var fragItems = [
    { title: "Notion (Notas)", desc: "Datos Estáticos" },
    { title: "Todoist (Tareas)", desc: "Sin Contexto Escolar" },
    { title: "Excel (Calendario)", desc: "Flujo Rígido" }
  ];
  
  for (var j = 0; j < fragItems.length; j++) {
    var fy = 120 + j * 75;
    var fragCard = slide3.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 40, fy, 290, 60);
    fragCard.getFill().setSolidFill(COLOR_TARJETA);
    fragCard.getBorder().setSolidFill(COLOR_TARJETA_BORDER).setWeight(1);
    
    var fRange = fragCard.getText();
    fRange.setText(fragItems[j].title + "\n" + fragItems[j].desc);
    fRange.getTextStyle().setFontFamily("Calibri").setFontSize(9.5).setForegroundColor(COLOR_TEXTO_MUTED);
    fRange.find(fragItems[j].title).getTextStyle().setBold(true).setFontSize(11).setForegroundColor(COLOR_TEXTO_BLANCO);
  }
  
  // Flecha Central
  var arrow = slide3.insertShape(SlidesApp.ShapeType.RIGHT_ARROW, 345, 205, 30, 20);
  arrow.getFill().setSolidFill(COLOR_TEXTO_TITULO);
  arrow.getBorder().setTransparent();
  
  // Columna Derecha: Solución
  var solCard = slide3.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 390, 110, 290, 200);
  solCard.getFill().setSolidFill(COLOR_TARJETA);
  solCard.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(2);
  
  var solRange = solCard.getText();
  solRange.setText(
    "LA SOLUCIÓN INTEGRADA\n\n" +
    "StudyFlow All-in-One\n\n" +
    "Multiplataforma Inteligente"
  );
  solRange.getTextStyle().setFontFamily("Calibri").setFontSize(11).setForegroundColor(COLOR_TEXTO_BLANCO);
  solRange.find("LA SOLUCIÓN INTEGRADA").getTextStyle().setBold(true).setFontSize(13).setForegroundColor(COLOR_TEXTO_TITULO);
  solRange.find("StudyFlow All-in-One").getTextStyle().setBold(true).setFontSize(14).setForegroundColor(COLOR_TEXTO_CYAN);
  solCard.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  var notes3 = 
    "Cualquier alumno de secundaria, formación profesional o universidad se enfrenta a un desafío diario estresante: la desorganización.\n\n" +
    "La información académica se fragmenta de forma crítica en canales inconexos. Para resolver esto, los alumnos recurren a " +
    "herramientas generalistas: Notion sirve para almacenar notas estáticas pero no tiene concepto de planificación; Todoist gestiona " +
    "tareas pero ignora por completo el flujo escolar; y Excel o Google Calendar resultan tediosos de configurar diariamente y no " +
    "calculan medias ponderadas.\n\n" +
    "Por ello, StudyFlow nace para cubrir ese vacío crítico, integrando en una única solución multiplataforma intuitiva todo lo que un " +
    "estudiante necesita: asignaturas, entregas y cómputo automático de rendimiento en tiempo real.";
  slide3.getNotesPage().getNotesBody().setText(notes3);

  // ========================================================================================
  // SLIDE 4: STACK TECNOLÓGICO (5 COLUMNAS)
  // ========================================================================================
  Logger.log("Creando Diapositiva 4: Stack Tecnológico...");
  var slide4 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide4, "Stack Tecnológico Unificado", "03 / TECNOLOGÍAS");
  
  var techPanels = [
    { title: "Backend", label: "Core API", color: COLOR_BACKEND, bullets: "• Java 17 / 21\n• Spring Boot 3.x\n• Spring Data JPA\n• Hibernate ORM" },
    { title: "Database", label: "MySQL Nube", color: COLOR_DATABASE, bullets: "• MySQL 8.x\n• Aiven Cloud\n• Borrados Cascade\n• Índices de FK" },
    { title: "Escritorio", label: "Cliente JavaFX", color: COLOR_DESKTOP, bullets: "• JavaFX 19\n• Patrón MVC\n• Maven Build\n• Jackson JSON" },
    { title: "Web PWA", label: "Cliente Angular", color: COLOR_PWA, bullets: "• Angular 17\n• TypeScript\n• Service Workers\n• CSS Vanilla" },
    { title: "Android", label: "Móvil Nativo", color: COLOR_ANDROID, bullets: "• Kotlin / Java\n• Material Design 3\n• Retrofit 2 REST\n• Gson Sincro" }
  ];
  
  var pWidth = 118;
  var pHeight = 260;
  var pGap = 12;
  var pStartX = 40;
  
  for (var k = 0; k < techPanels.length; k++) {
    var px = pStartX + k * (pWidth + pGap);
    
    // Panel de fondo
    var panel = slide4.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, px, 95, pWidth, pHeight);
    panel.getFill().setSolidFill(COLOR_TARJETA);
    panel.getBorder().setSolidFill(techPanels[k].color).setWeight(2.5);
    
    // Contenido
    var pRange = panel.getText();
    pRange.setText(
      techPanels[k].title + "\n" +
      techPanels[k].label + "\n\n" +
      techPanels[k].bullets
    );
    pRange.getTextStyle().setFontFamily("Calibri").setFontSize(9).setForegroundColor(COLOR_TEXTO_MUTED);
    pRange.find(techPanels[k].title).getTextStyle().setBold(true).setFontSize(13).setForegroundColor(COLOR_TEXTO_BLANCO);
    pRange.find(techPanels[k].label).getTextStyle().setBold(true).setFontSize(8).setForegroundColor(techPanels[k].color);
  }
  
  var notes4 = 
    "StudyFlow demuestra el valor del desarrollo multiplataforma integrando tecnologías de vanguardia y entornos heterogéneos de forma robusta.\n\n" +
    "En el Backend, el núcleo lógico está desarrollado en Java 17/21 con Spring Boot 3 y persistencia Hibernate. " +
    "La base de datos es MySQL 8 alojada en la nube con Aiven Cloud, y la API se encuentra desplegada en producción en Render.\n\n" +
    "Para el cliente de Escritorio, usamos JavaFX 19 con patrón MVC y automatización mediante Maven. El cliente web es una PWA construida " +
    "en Angular 17 con Service Workers para soporte sin red local. Y en dispositivos móviles, optamos por desarrollo nativo en Android " +
    "con Kotlin/Java, consumo HTTP vía Retrofit 2 y maquetación basada en las directrices visuales modernas de Material Design 3.";
  slide4.getNotesPage().getNotesBody().setText(notes4);

  // ========================================================================================
  // SLIDE 5: ARQUITECTURA Y ESTRUCTURA (FLOWCHART CAPAS)
  // ========================================================================================
  Logger.log("Creando Diapositiva 5: Arquitectura...");
  var slide5 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide5, "Arquitectura y Estructura del Servidor", "04 / BACKEND");
  
  // Columna Izquierda: Texto
  var archTextBox = slide5.insertTextBox(40, 95, 290, 270);
  var archRange = archTextBox.getText();
  archRange.setText(
    "ESTRUCTURA MULTICAPA REST\n\n" +
    "API REST sin estado. Cómputo centralizado.\n\n" +
    "• Desacoplamiento: Clientes sin lógica de negocio.\n\n" +
    "• Aislamiento: Filtro obligatorio por userId."
  );
  archRange.getTextStyle().setFontFamily("Calibri").setFontSize(10.5).setForegroundColor(COLOR_TEXTO_BLANCO);
  archRange.find("ESTRUCTURA MULTICAPA REST").getTextStyle().setBold(true).setFontSize(13).setForegroundColor(COLOR_TEXTO_TITULO);
  archRange.find("Desacoplamiento:").getTextStyle().setBold(true);
  archRange.find("Aislamiento:").getTextStyle().setBold(true);
  
  // Columna Derecha: Flowchart
  var flowNodes = [
    { title: "1. Controllers", desc: "Endpoints & DTOs" },
    { title: "2. Services", desc: "Lógica & Medias Ponderadas" },
    { title: "3. Repositories", desc: "Spring Data JPA" },
    { title: "4. Base de Datos", desc: "MySQL Nube (Aiven)" }
  ];
  
  for (var m = 0; m < flowNodes.length; m++) {
    var ny = 95 + m * 68;
    
    // Nodo
    var node = slide5.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 370, ny, 310, 42);
    node.getFill().setSolidFill(COLOR_TARJETA);
    node.getBorder().setSolidFill(m === 3 ? COLOR_TEXTO_TITULO : COLOR_TEXTO_CYAN).setWeight(1);
    
    var nodeRange = node.getText();
    nodeRange.setText(flowNodes[m].title + "\n" + flowNodes[m].desc);
    nodeRange.getTextStyle().setFontFamily("Calibri").setFontSize(8.5).setForegroundColor(COLOR_TEXTO_MUTED);
    nodeRange.find(flowNodes[m].title).getTextStyle().setBold(true).setFontSize(10).setForegroundColor(COLOR_TEXTO_BLANCO);
    
    // Flecha (solo para los primeros 3)
    if (m < 3) {
      var fy = ny + 42 + 4;
      var fArrow = slide5.insertShape(SlidesApp.ShapeType.DOWN_ARROW, 515, fy, 20, 18);
      fArrow.getFill().setSolidFill(COLOR_TEXTO_CYAN);
      fArrow.getBorder().setTransparent();
    }
  }
  
  var notes5 = 
    "La solidez y consistencia matemática de StudyFlow se debe a su diseño de arquitectura distribuida.\n\n" +
    "No duplicamos lógica de cálculo en las interfaces de usuario. La API REST actúa como el verdadero cerebro sin estado (stateless). " +
    "El motor de negocio y validación opera exclusivamente en el servidor. Esto garantiza tres ventajas clave: consistencia matemática " +
    "absoluta de promedios ponderados entre todos los clientes; total aislamiento de datos gracias a que todas las consultas " +
    "filtran proactivamente por el userId activo; y un desacoplamiento limpio mediante la separación en capas ilustrada a la derecha: " +
    "controladores REST, servicios lógicos y repositorios Spring Data JPA mapeados físicamente sobre Aiven MySQL.";
  slide5.getNotesPage().getNotesBody().setText(notes5);

  // ========================================================================================
  // SLIDE 6: DESARROLLO MULTIPLATAFORMA E INCIDENCIAS CORREGIDAS
  // ========================================================================================
  Logger.log("Creando Diapositiva 6: Clientes y Calidad...");
  var slide6 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide6, "Clientes y Calidad del Software (Ver. 2.0)", "05 / DESARROLLO");
  
  var clientCards = [
    { 
      title: "Desktop Client", 
      tag: "JavaFX", 
      color: COLOR_BACKEND, 
      bugs: "• ComboBox: StringConverter\n• Bordes Pantone: Accesibilidad visual" 
    },
    { 
      title: "Web PWA Client", 
      tag: "Angular 17", 
      color: COLOR_TEXTO_TITULO, 
      bugs: "• Badges: ✓ HECHA / ✗ SIN HACER\n• Seguridad: Ocultación de IDs técnicos" 
    },
    { 
      title: "Android App", 
      tag: "Kotlin / Java", 
      color: COLOR_ANDROID, 
      bugs: "• Persistencia: Mapeo @JsonProperty\n• UI asíncrona: Material Design 3" 
    }
  ];
  
  var cWidth = 205;
  var cHeight = 260;
  var cGap = 15;
  var cStartX = 40;
  
  for (var n = 0; n < clientCards.length; n++) {
    var cx = cStartX + n * (cWidth + cGap);
    
    var card = slide6.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, cx, 95, cWidth, cHeight);
    card.getFill().setSolidFill(COLOR_TARJETA);
    card.getBorder().setSolidFill(clientCards[n].color).setWeight(1.5);
    
    // Contenido
    var cRange = card.getText();
    cRange.setText(
      clientCards[n].title + " (" + clientCards[n].tag + ")\n\n" +
      clientCards[n].bugs
    );
    cRange.getTextStyle().setFontFamily("Calibri").setFontSize(9).setForegroundColor(COLOR_TEXTO_MUTED);
    cRange.find(clientCards[n].title).getTextStyle().setBold(true).setFontSize(12).setForegroundColor(COLOR_TEXTO_BLANCO);
    cRange.find("(" + clientCards[n].tag + ")").getTextStyle().setFontSize(9).setForegroundColor(clientCards[n].color).setBold(true);
  }
  
  var notes6 = 
    "Este es el apartado del que más orgulloso me siento, ya que demuestra un control total sobre el código del proyecto " +
    "atendiendo exhaustivamente al pulido de calidad.\n\n" +
    "En primer lugar, mejoramos radicalmente la UX de la columna DONE en la Web PWA y móviles. Reemplazamos los booleanos crudos " +
    "true/false por bonitos indicadores coloreados de estado ✓ HECHA en verde y ✗ SIN HACER en gris. " +
    "Por seguridad, ocultamos todos los IDs técnicos autoincrementales en las tablas para mitigar vulnerabilidades de enumeración. " +
    "En la aplicación de escritorio, solucionamos la accesibilidad de colores de asignaturas: ya no se pintan fondos claros que " +
    "queman la vista; ahora usamos un sofisticado borde indicador izquierdo de 5px con el color Pantone correspondiente. " +
    "También implementamos un StringConverter para que los ComboBoxes muestren nombres limpios y corregimos la persistencia de " +
    "tareas Jackson bidireccional aplicando @JsonProperty de forma simétrica.";
  slide6.getNotesPage().getNotesBody().setText(notes6);

  // ========================================================================================
  // SLIDE 7: GESTIÓN DEL PROYECTO Y VIABILIDAD (INVOICE LEDGER)
  // ========================================================================================
  Logger.log("Creando Diapositiva 7: Gestión y Viabilidad...");
  var slide7 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide7, "Viabilidad del Proyecto y Costes", "06 / FINANZAS");
  
  // Columna Izquierda: Metodología
  var methTextBox = slide7.insertTextBox(40, 80, 290, 280);
  var methRange = methTextBox.getText();
  methRange.setText(
    "FASES DE LA METODOLOGÍA\n\n" +
    "• Q1: Requisitos, DB e Infraestructura\n\n" +
    "• Q2: API REST Spring Boot 3\n\n" +
    "• Q3: Escritorio JavaFX & MVC\n\n" +
    "• Q4: PWA Angular & Android Nativo\n\n" +
    "• Q5: Auditoría & Calidad de Software"
  );
  methRange.getTextStyle().setFontFamily("Calibri").setFontSize(10.5).setForegroundColor(COLOR_TEXTO_BLANCO);
  methRange.find("FASES DE LA METODOLOGÍA").getTextStyle().setBold(true).setFontSize(13).setForegroundColor(COLOR_TEXTO_TITULO);
  methRange.find("Q1").getTextStyle().setBold(true).setForegroundColor(COLOR_TEXTO_CYAN);
  methRange.find("Q2").getTextStyle().setBold(true).setForegroundColor(COLOR_TEXTO_CYAN);
  methRange.find("Q3").getTextStyle().setBold(true).setForegroundColor(COLOR_TEXTO_CYAN);
  methRange.find("Q4").getTextStyle().setBold(true).setForegroundColor(COLOR_TEXTO_CYAN);
  methRange.find("Q5").getTextStyle().setBold(true).setForegroundColor(COLOR_TEXTO_CYAN);
  
  // Columna Derecha: Invoice Ledger Shape
  var ledger = slide7.insertShape(SlidesApp.ShapeType.RECTANGLE, 360, 95, 320, 260);
  ledger.getFill().setSolidFill(COLOR_TARJETA);
  ledger.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(2);
  
  var ledgerRange = ledger.getText();
  ledgerRange.setText(
    "FACTURA DETALLADA DE INFRAESTRUCTURA\n" +
    "----------------------------------------------------\n" +
    "Ingeniería de Software (240 Horas / 18 €/h) 4.320,00 €\n" +
    "Infraestructura Render (Servidor API)         60,00 €\n" +
    "Infraestructura Aiven (MySQL)                110,00 €\n" +
    "Licencia Google Play Console                  25,00 €\n" +
    "Hosting Frontends (Vercel)                   Gratis\n" +
    "----------------------------------------------------\n" +
    "PRESUPUESTO TOTAL                           4.515,00 €"
  );
  ledgerRange.getTextStyle().setFontFamily("Consolas").setFontSize(9.5).setForegroundColor("#99f6e4");
  ledgerRange.find("FACTURA DETALLADA DE INFRAESTRUCTURA").getTextStyle().setBold(true).setFontSize(11).setForegroundColor(COLOR_TEXTO_BLANCO);
  ledgerRange.find("PRESUPUESTO TOTAL GENERAL").getTextStyle().setBold(true).setFontSize(11).setForegroundColor(COLOR_TEXTO_TITULO);
  ledgerRange.find("4.515,00 €").getTextStyle().setBold(true).setFontSize(12).setForegroundColor(COLOR_TEXTO_TITULO);
  ledger.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  var notes7 = 
    "Para validar la viabilidad económica real de StudyFlow, hemos realizado una estimación financiera rigurosa de costes de ingeniería e infraestructura.\n\n" +
    "El desarrollo del proyecto se estructuró a lo largo de 5 quincenas de trabajo incremental. Estimando un total de 240 horas de dedicación " +
    "de ingeniería con una tarifa estándar junior de 18 euros la hora, el coste de personal es de 4.320 euros. Adicionalmente, calculamos " +
    "los gastos de infraestructura en la nube: 60 euros anuales para el servidor Render API, 110 euros anuales para el alojamiento Aiven MySQL, " +
    "la licencia vitalicia de Google Play de 25 euros y hosting gratuito en Vercel para la PWA. El presupuesto de lanzamiento real se sitúa " +
    "en 4.515 euros, demostrando ser un producto sumamente viable y económicamente competitivo.";
  slide7.getNotesPage().getNotesBody().setText(notes7);

  // ========================================================================================
  // SLIDE 8: ROADMAP Y ESCALABILIDAD (STAIRCASE)
  // ========================================================================================
  Logger.log("Creando Diapositiva 8: Roadmap...");
  var slide8 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addSlideHeader(slide8, "Roadmap Tecnológico de Escalabilidad", "07 / FUTURO");
  
  var stairSteps = [
    { num: "1", title: "Notificaciones Push", desc: "Vencimientos y Alertas Móviles" },
    { num: "2", title: "Autenticación OAuth2", desc: "OAuth2 Google & GitHub" },
    { num: "3", title: "Entorno Colaborativo", desc: "Calendarios & Apuntes Compartidos" },
    { num: "4", title: "Exportación e IA", desc: "Planificación IA & Reportes PDF" }
  ];
  
  // Dibujar escalera diagonal de 4 peldaños
  var sWidth = 145;
  var sHeight = 80;
  
  for (var p = 0; p < stairSteps.length; p++) {
    // Calculo diagonal: Peldaño 1 está abajo-izquierda, Peldaño 4 está arriba-derecha
    var sx = 40 + p * 165;
    var sy = 270 - p * 60; // Peldaño sube a medida que p avanza
    
    var step = slide8.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, sx, sy, sWidth, sHeight);
    step.getFill().setSolidFill(COLOR_TARJETA);
    step.getBorder().setSolidFill(p === 3 ? COLOR_TEXTO_TITULO : COLOR_TEXTO_CYAN).setWeight(p === 3 ? 2 : 1);
    
    var sText = step.getText();
    sText.setText(stairSteps[p].num + ". " + stairSteps[p].title + "\n\n" + stairSteps[p].desc);
    sText.getTextStyle().setFontFamily("Calibri").setFontSize(8.5).setForegroundColor(COLOR_TEXTO_MUTED);
    sText.find(stairSteps[p].num + ". " + stairSteps[p].title).getTextStyle()
      .setBold(true)
      .setFontSize(10.5)
      .setForegroundColor(COLOR_TEXTO_BLANCO);
  }
  
  var notes8 = 
    "StudyFlow no se detiene en esta entrega; se ha diseñado con bases preparadas para una escalabilidad masiva y proyección a futuro.\n\n" +
    "Nuestra hoja de ruta tecnológica se compone de 4 peldaños ascendentes ilustrados en pantalla: en el primer escalón, implementaremos " +
    "notificaciones push nativas al móvil del estudiante. Subiendo en el roadmap, integraremos autenticación robusta mediante OAuth2 " +
    "con login social de Google y GitHub.\n\n" +
    "El tercer nivel dotará a la aplicación de un entorno colaborativo real, permitiendo a alumnos del mismo ciclo compartir tareas " +
    "y calendarios académicos. En la cumbre, integraremos un planificador predictivo con Inteligencia Artificial y exportación de " +
    "informes analíticos en PDF.";
  slide8.getNotesPage().getNotesBody().setText(notes8);

  // ========================================================================================
  // SLIDE 9: PORTADA DE CIERRE
  // ========================================================================================
  Logger.log("Creando Diapositiva 9: Cierre...");
  var slide9 = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  setSlideBackground(slide9);
  addAccentHeader(slide9);
  
  // Logo
  var logoOuter9 = slide9.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 325, 30, 70, 70);
  logoOuter9.getFill().setSolidFill(COLOR_TARJETA);
  logoOuter9.getBorder().setSolidFill(COLOR_TEXTO_TITULO).setWeight(2);
  
  var logoInner9 = slide9.insertShape(SlidesApp.ShapeType.DIAMOND, 345, 50, 30, 30);
  logoInner9.getFill().setSolidFill(COLOR_TEXTO_TITULO);
  logoInner9.getBorder().setTransparent();

  // Título Principal
  var titleBox9 = slide9.insertTextBox(40, 115, 640, 65);
  var titleRange9 = titleBox9.getText();
  titleRange9.setText("STUDYFLOW");
  titleRange9.getTextStyle()
    .setFontFamily("Trebuchet MS")
    .setFontSize(48)
    .setBold(true)
    .setForegroundColor(COLOR_TEXTO_TITULO);
  titleBox9.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  // Subtítulo
  var subtitleBox9 = slide9.insertTextBox(40, 175, 640, 45);
  var subtitleRange9 = subtitleBox9.getText();
  subtitleRange9.setText("¡Muchas gracias por su atención!");
  subtitleRange9.getTextStyle()
    .setFontFamily("Outfit")
    .setFontSize(22)
    .setBold(true)
    .setForegroundColor(COLOR_TEXTO_BLANCO);
  subtitleBox9.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  // Botón Acción
  var btn9 = slide9.insertShape(SlidesApp.ShapeType.ROUNDED_RECTANGLE, 220, 235, 280, 40);
  btn9.getFill().setSolidFill(COLOR_TEXTO_CYAN);
  btn9.getBorder().setTransparent();
  btn9.getText().setText("Demostración Técnica en Vivo ➔")
    .getTextStyle()
    .setFontFamily("Outfit")
    .setFontSize(14)
    .setBold(true)
    .setForegroundColor(COLOR_FONDO);
  btn9.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);

  // Metadatos
  var metaBox9 = slide9.insertTextBox(40, 305, 640, 60);
  var metaRange9 = metaBox9.getText();
  metaRange9.setText(
    "Juan Sebastián Valero Marulanda   -   2º DAM   -   Proyecto Intermodular"
  );
  metaRange9.getTextStyle()
    .setFontFamily("Calibri")
    .setFontSize(12)
    .setForegroundColor(COLOR_TEXTO_MUTED);
  metaBox9.setContentAlignment(SlidesApp.ContentAlignment.MIDDLE);
  
  var notes9 = 
    "Con esta diapositiva doy por concluida la síntesis teórica del proyecto.\n\n" +
    "Para demostrar la robustez, consistencia y sincronización en tiempo real multiplataforma de la que hemos estado hablando, " +
    "iniciaremos la demostración técnica práctica.\n\n" +
    "Procederé a abrir el cliente de escritorio en JavaFX para dar de alta asignaturas con colores representativos reales, " +
    "agregaré exámenes ponderados y tareas; seguidamente veremos cómo esa información se sincroniza instantáneamente tanto " +
    "en la Progressive Web App en el navegador como en el emulador de Android Nativo. Muchas gracias.";
  slide9.getNotesPage().getNotesBody().setText(notes9);
  
  pres.saveAndClose();
  Logger.log("¡Generación completada con éxito!");
  Logger.log("=========================================================================");
  Logger.log("ENLACE A LA PRESENTACIÓN:");
  Logger.log(pres.getUrl());
  Logger.log("=========================================================================");
}
