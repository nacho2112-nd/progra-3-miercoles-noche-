// Genera la documentación técnica del TPO en .docx
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, Table, TableRow, TableCell,
  WidthType, ShadingType, BorderStyle, ImageRun, PageBreak, Header, Footer, PageNumber,
  TableOfContents, LevelFormat, PageOrientation, VerticalAlign,
} = require('docx');

const IMG = path.join(__dirname, '..', 'img');
const SALIDA = process.argv[2] || path.join(__dirname, '..', 'TPO-AdivinaQuien-Documentacion.docx');

const AZUL = '1E3A8F', TINTA = '131720', SUAVE = '5B6475', COBRE = 'A9541F', CELESTE = 'E3E9F7', BORDE = 'C9CFDA';
const ANCHO = 9638;           // ancho útil en DXA (A4 con márgenes de 2 cm)
const ANCHO_PX = 640;         // ancho útil aproximado en píxeles
const ANCHO_APAISADO_PX = 960;

// ---------------------------------------------------------------- helpers de texto
function runs(texto, base = {}) {
  const partes = texto.split(/(\*\*[^*]+\*\*|`[^`]+`|\[COMPLETAR[^\]]*\])/g).filter(s => s.length);
  return partes.map(p => {
    if (p.startsWith('**')) return new TextRun({ ...base, text: p.slice(2, -2), bold: true });
    if (p.startsWith('`')) return new TextRun({ ...base, text: p.slice(1, -1), font: 'Consolas', size: 20, color: '0B6B74' });
    if (p.startsWith('[COMPLETAR')) return new TextRun({ ...base, text: p, bold: true, highlight: 'yellow' });
    return new TextRun({ ...base, text: p });
  });
}
const P = (t, opts = {}) => new Paragraph({ children: runs(t, opts.run || {}), spacing: { after: 100, line: 259 }, alignment: opts.align || AlignmentType.JUSTIFIED, ...opts.par });
const H1 = t => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(t)] });
const H1salto = t => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(t)], pageBreakBefore: true });
const H1c = t => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(t)] });
const H2 = t => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(t)] });
const B = t => new Paragraph({ children: runs(t), numbering: { reference: 'vinetas', level: 0 }, spacing: { after: 60, line: 259 }, alignment: AlignmentType.JUSTIFIED });
const F = t => new Paragraph({ children: [new TextRun({ text: t, font: 'Consolas', size: 21, color: TINTA })], alignment: AlignmentType.CENTER,
  spacing: { before: 120, after: 160 }, shading: { type: ShadingType.CLEAR, fill: 'F3F5F9', color: 'auto' } });
const salto = () => new Paragraph({ children: [new PageBreak()] });

let figura = 0;
function tamPng(f) {
  const b = fs.readFileSync(f);
  return { w: b.readUInt32BE(16), h: b.readUInt32BE(20), data: b };
}
function imagen(archivo, anchoPx, epigrafe, nuevaPagina = false) {
  const f = path.join(IMG, archivo);
  const { w, h, data } = tamPng(f);
  const alto = Math.round(h * anchoPx / w);
  const out = [new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120, after: 60 }, keepNext: true, pageBreakBefore: nuevaPagina,
    children: [new ImageRun({ type: 'png', data, transformation: { width: anchoPx, height: alto } })] })];
  if (epigrafe) {
    figura++;
    out.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 },
      children: [new TextRun({ text: `Figura ${figura}. `, bold: true, size: 19, color: SUAVE }), ...runs(epigrafe, { size: 19, color: SUAVE, italics: true })] }));
  }
  return out;
}

// ---------------------------------------------------------------- tablas
const borde = { style: BorderStyle.SINGLE, size: 4, color: BORDE };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };
function celda(texto, ancho, { cabecera = false, align = AlignmentType.LEFT, fill } = {}) {
  return new TableCell({
    width: { size: ancho, type: WidthType.DXA }, borders: bordes, verticalAlign: VerticalAlign.CENTER,
    margins: { top: 40, bottom: 40, left: 90, right: 90 },
    shading: cabecera ? { type: ShadingType.CLEAR, fill: CELESTE, color: 'auto' } : (fill ? { type: ShadingType.CLEAR, fill, color: 'auto' } : undefined),
    children: [new Paragraph({ alignment: align, spacing: { after: 0, line: 252 },
      children: runs(texto, { size: 18, bold: cabecera || undefined, color: cabecera ? AZUL : TINTA }) })],
  });
}
function tabla(filas, proporciones, { numericas = [], resaltar = [] } = {}) {
  const total = proporciones.reduce((a, b) => a + b, 0);
  const anchos = proporciones.map(p => Math.floor(ANCHO * p / total));
  anchos[anchos.length - 1] += ANCHO - anchos.reduce((a, b) => a + b, 0);
  return new Table({
    width: { size: ANCHO, type: WidthType.DXA }, columnWidths: anchos,
    rows: filas.map((fila, i) => new TableRow({
      tableHeader: i === 0, cantSplit: true,
      children: fila.map((t, j) => celda(t, anchos[j], {
        cabecera: i === 0, align: (i > 0 && numericas.includes(j)) ? AlignmentType.RIGHT : AlignmentType.LEFT,
        fill: resaltar.includes(i) ? 'FFF6E5' : undefined })),
    })),
  });
}
const espacio = () => new Paragraph({ spacing: { after: 80 }, children: [] });

// ---------------------------------------------------------------- contenido
const portada = [
  new Paragraph({ spacing: { before: 1800, after: 120 }, children: [new TextRun({ text: 'UADE · PROGRAMACIÓN III · DISEÑO Y ANÁLISIS DE ALGORITMOS', bold: true, size: 20, color: AZUL })] }),
  new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: 'Adivina Quién', bold: true, size: 72, color: TINTA })] }),
  new Paragraph({ spacing: { after: 600 }, children: [new TextRun({ text: 'Documentación técnica · Trabajo práctico obligatorio 1', size: 32, color: SUAVE })] }),
  tabla([
    ['Dato', 'Detalle'],
    ['Asignatura', 'Programación III — Diseño y Análisis de Algoritmos'],
    ['Docente', 'López, Juan Ignacio'],
    ['Comisión', 'Miércoles, turno noche [COMPLETAR: número de comisión]'],
    ['Integrantes', '[COMPLETAR: apellido, nombre y legajo de cada integrante]'],
    ['Fecha de entrega', '[COMPLETAR]'],
    ['Código fuente', 'Carpeta ZIP adjunta y repositorio github.com/nacho2112-nd/progra-3-miercoles-noche- (carpeta tpo-1-adivina-quien)'],
  ], [1, 3]),
  new Paragraph({ spacing: { before: 400 }, children: runs('Los campos marcados como [COMPLETAR] los completa el equipo antes de imprimir.', { size: 18, color: SUAVE, italics: true }) }),
  salto(),
  new TableOfContents('Índice', { hyperlink: true, headingStyleRange: '1-2' }),
];

const introduccion = [
  H1c('1. Introducción'),
  P('Adivina Quién es un juego de dos jugadores. Cada uno elige en secreto un personaje de un tablero común y, turno a turno, trata de descubrir el del rival: hace preguntas de sí o no, o arriesga un nombre. Nuestra implementación en Java tiene dos modos, **Jugador vs Máquina** y **Máquina vs Máquina**. En el segundo, la pantalla muestra todo el razonamiento de cada máquina.'),
  P('Dividimos el problema en cuatro subsistemas y resolvimos cada uno con una estrategia distinta:'),
  B('**Armado del tablero (Divide y Conquista).** El catálogo llega agrupado sólo por género. La máquina valida las precondiciones, ordena los personajes por nombre con MergeSort y los agrega a una lista que les asigna un ID autoincremental. Como el tablero queda ordenado por nombre, cada arriesgue se resuelve con búsqueda binaria.'),
  B('**Motor de turnos.** La clase `Partida` alterna los turnos. A cada jugador le entrega a su rival sólo como un `Oraculo`: una interfaz que responde SÍ o NO y nunca devuelve el personaje secreto.'),
  B('**Estrategia de la máquina (Greedy).** En cada turno, la máquina evalúa los filtros sin resolver y elige el de mayor descarte garantizado, min(SÍ, NO). Una base de conocimiento aplica la dependencia lógica entre filtros (por ejemplo, un pelado no tiene color de pelo). Un criterio de parada decide cuándo conviene arriesgar.'),
  B('**Visualización y verificación.** La interfaz Swing muestra los tableros, la evaluación de cada turno, la traza de MergeSort, el árbol de decisión completo y la comparación de tiempos. Hay además 54 pruebas automáticas y una herramienta que compara la estrategia con el óptimo exacto.'),
  P('Las reglas, tal como las implementamos:'),
  tabla([
    ['Regla', 'Decisión'],
    ['Personajes', '23, los mismos para los dos jugadores.'],
    ['Filtros', 'Los de la consigna: género, calvicie (en el juego, "¿Es pelado?"), lentes y pelo colorado, negro o amarillo. Agregamos barba y sombrero (ver abajo).'],
    ['Turno', 'Una sola acción: preguntar un filtro o arriesgar un personaje.'],
    ['Arriesgue', 'Si acierta, gana. Si falla, pierde el turno y ese personaje queda descartado.'],
    ['Respuestas', 'Automáticas: salen del secreto de cada jugador, a través del Oraculo. El humano no contesta a mano y no puede mentir.'],
    ['Secretos', 'El humano elige el suyo haciendo clic en una carta. La máquina elige el suyo al azar.'],
    ['Quién empieza', 'El humano en Jugador vs Máquina; la Máquina A en Máquina vs Máquina.'],
  ], [1, 4]),
  espacio(),
  P('**Por qué barba y sombrero.** Con los filtros de la consigna hay 2 × 4 × 2 = 16 combinaciones posibles: género, una de cuatro opciones de pelo (pelado, colorado, negro o amarillo) y lentes. Con 23 personajes, al menos 7 quedarían idénticos a otro y ninguna pregunta podría separarlos. Con barba (sólo en hombres) y sombrero hay 48 perfiles posibles, y los 23 personajes son distinguibles, como pide la consigna.'),
];

const uml = [
  H1('2. Diagrama de clases (UML)'),
  H2('2.1 Estructura general del proyecto'),
  P('El código está en cinco paquetes, más una carpeta de pruebas. Las dependencias van en un solo sentido y no hay ciclos. La interfaz depende del motor; el motor, de los algoritmos y los datos; y todos, del modelo. Así se puede probar el motor sin abrir ninguna ventana, y cambiar la interfaz sin tocar la lógica.'),
  ...imagen('uml-paquetes.png', ANCHO_PX, 'Paquetes del proyecto y sus dependencias.'),
  tabla([
    ['Paquete', 'Responsabilidad', 'Clases principales'],
    ['modelo', 'Las entidades del juego, sin lógica de partida.', 'Personaje, Tablero, Filtro, Genero, ColorPelo'],
    ['datos', 'El catálogo de 23 personajes y sus precondiciones.', 'CatalogoPersonajes, ValidadorCatalogo'],
    ['algoritmos', 'Ordenamientos, búsqueda binaria y medición de tiempos.', 'MergeSort, QuickSort, OrdenamientoInsercion, OrdenamientoBurbuja, BusquedaBinaria, Benchmark'],
    ['motor', 'Reglas, turnos y estrategia de la máquina.', 'Oraculo, Jugador, JugadorHumano, JugadorMaquina, Partida, EstrategiaGreedy, BaseConocimiento, ArbolDecision, OrganizadorTablero'],
    ['ui', 'Ventana Swing y pantallas.', 'VentanaPrincipal, PanelJugadorVsMaquina, PanelMaquinaVsMaquina, PanelOrdenamiento, PanelArbolDecision, TarjetaPersonaje'],
    ['test', 'Pruebas y herramientas de análisis. No forman parte del juego.', 'PruebasAutomaticas, AnalisisOptimalidad, MedicionTiempos, GeneradorCapturas'],
  ], [1.1, 2.4, 3.5]),
  H2('2.2 Relaciones entre clases'),
  P('Los dos diagramas de las páginas siguientes muestran las clases con sus atributos y métodos principales. El primero cubre el motor del juego; el segundo, los datos, los ordenamientos y el armado del tablero. En la notación, + es público, - privado y ~ de paquete. Las flechas son las de UML: triángulo para herencia (línea llena) y realización (línea punteada), rombo lleno para composición, rombo vacío para agregación, flecha simple para asociación y línea punteada para dependencia.'),
];

const umlApaisado = [
  ...imagen('uml-motor.png', ANCHO_APAISADO_PX - 60, 'Diagrama de clases del motor del juego.'),
  ...imagen('uml-datos.png', ANCHO_APAISADO_PX, 'Diagrama de clases de datos, ordenamiento y armado del tablero.', true),
];

const relaciones = [
  P('Las relaciones más importantes y por qué están modeladas así:', { par: { spacing: { before: 0, after: 120 } } }),
  tabla([
    ['Relación', 'Tipo', 'Por qué'],
    ['Jugador → Oraculo', 'Realización', 'Cada jugador responde preguntas sobre su propio secreto. Es la única cara que ve el rival.'],
    ['JugadorHumano, JugadorMaquina → Jugador', 'Herencia', 'Comparten el secreto, los candidatos, la base de conocimiento y la mecánica de preguntar y arriesgar. Sólo cambia quién decide: la interfaz o la estrategia.'],
    ['Partida → Jugador (2)', 'Asociación', 'La partida conoce a los dos jugadores y controla de quién es el turno.'],
    ['Partida ◆→ ResultadoTurno (*)', 'Composición', 'El historial pertenece a la partida y no existe sin ella.'],
    ['Jugador ◆→ BaseConocimiento', 'Composición', 'Lo que un jugador sabe del rival es suyo. Hacia afuera se entrega sólo una copia.'],
    ['JugadorMaquina ◆→ EstrategiaGreedy', 'Composición', 'La estrategia es parte de la máquina.'],
    ['Jugador → Personaje', 'Asociación', 'Un secreto y un conjunto de 0 a 23 candidatos.'],
    ['Tablero ◇→ Personaje (23)', 'Agregación', 'El tablero ordena y numera personajes que se crean en el catálogo.'],
    ['JugadorMaquina ⇢ Oraculo', 'Dependencia', 'La máquina recibe al rival como parámetro en cada turno y no lo guarda en ningún campo. Esa es la garantía de que no accede al secreto.'],
    ['OrganizadorTablero ⇢ ValidadorCatalogo, MergeSort, Tablero', 'Dependencia', 'Los usa para armar el tablero y no guarda referencias.'],
    ['MergeSort, QuickSort, Inserción, Burbujeo → AlgoritmoOrdenamiento', 'Realización', 'El Benchmark mide los cuatro con el mismo código, por polimorfismo.'],
  ], [2.6, 1.2, 4.2]),
  espacio(),
  P('En el diagrama se ven dos principios SOLID de la materia. **Inversión de dependencias:** la máquina depende de la abstracción `Oraculo`, no del jugador concreto. **Abierto/cerrado:** para agregar un ordenamiento basta con implementar `AlgoritmoOrdenamiento`, sin tocar `Benchmark`.'),
  H2('2.3 Justificación del modelo de datos'),
  tabla([
    ['Estructura', 'Dónde', 'Por qué', 'Costo'],
    ['List<Personaje> (ArrayList)', 'Tablero', 'Hace falta orden (alfabético) y acceso por posición: el personaje con ID i está en la posición i − 1.', 'Por ID O(1); agregar al final O(1) amortizado; búsqueda binaria O(log n).'],
    ['Arreglo auxiliar T[]', 'MergeSort', 'La combinación necesita un arreglo del mismo tamaño para intercalar.', 'Θ(n) de memoria.'],
    ['Set<Personaje> (LinkedHashSet)', 'Jugador.candidatos', 'Los candidatos son un conjunto: no hay repetidos y se pregunta pertenencia. LinkedHashSet conserva el orden del tablero, así las trazas son reproducibles.', 'contains y remove O(1); recorrido O(c).'],
    ['Map<Filtro, Boolean> (EnumMap)', 'BaseConocimiento', 'Asocia cada filtro resuelto con su valor. EnumMap es un arreglo indexado por el ordinal del enum: más compacto y rápido que un HashMap.', 'get, put y containsKey O(1).'],
    ['Set<Filtro> (EnumSet)', 'BaseConocimiento', 'El grupo exclusivo {pelado, colorado, negro, amarillo}. Internamente es una máscara de bits.', 'O(1).'],
    ['HashSet<String>, HashMap<Integer, Personaje>', 'ValidadorCatalogo', 'Detectar nombres y perfiles repetidos en una sola pasada.', 'O(1) por personaje, O(n) en total.'],
    ['List<ResultadoTurno>', 'Partida', 'Registro cronológico de la partida.', 'Agregar O(1).'],
    ['enum Filtro', 'modelo', 'Los filtros son un conjunto cerrado. Cada constante implementa evaluar(Personaje), así que agregar uno no obliga a tocar ningún switch.', 'evaluar O(1).'],
  ], [2.2, 1.5, 3.6, 2.2]),
  espacio(),
  P('Además, `Personaje.perfil()` codifica los 8 filtros de un personaje en un entero de 8 bits. Dos personajes con el mismo perfil serían indistinguibles. El validador usa ese número como clave de un `HashMap` para detectarlos, y la herramienta de análisis lo usa para representar conjuntos de candidatos como máscaras de bits.'),
];

const snippets = JSON.parse(fs.readFileSync(path.join(IMG, 'snippets.json'), 'utf-8'));
function fragmento(nombre, explicacion) {
  const s = snippets[nombre];
  return [...imagen(nombre + '.png', ANCHO_PX - 20, `${s.titulo} (${s.archivo.split('/').pop()}). Complejidad: ${s.bigO}.`), P(explicacion)];
}

const bitacora = [
  H1('3. Bitácora de desarrollo'),
  H2('3.1 Etapas'),
  tabla([
    ['Fecha', 'Etapa', 'Qué se hizo'],
    ['26/08/2026', 'Primer borrador', 'Proyecto en IntelliJ con las clases Personajes y Acciones: generación aleatoria de personajes con muchos atributos (profesión, mascota, música). No llegó a compilar y se descartó, porque la consigna pide personajes fijos y filtros definidos.'],
    ['29/08/2026', 'Versión 1', 'Adivina Quién por consola. El jugador escribía preguntas en lenguaje natural y un filtro de palabras clave las traducía a un atributo. Sólo adivinaba el humano: la máquina no jugaba.'],
    ['26/09/2026', 'Versión 2 (la que se entrega)', 'Con la documentación formal del TP rehicimos el proyecto desde cero, en este orden: modelo y catálogo; validaciones; MergeSort y los ordenamientos de comparación; motor de turnos con Oraculo; estrategia Greedy y base de conocimiento; árbol de decisión; pruebas automáticas; interfaz Swing; benchmark y análisis de optimalidad; ajuste del criterio de parada; este documento.'],
    ['[COMPLETAR]', '[COMPLETAR]', '[COMPLETAR: revisión del equipo, pruebas, ajustes posteriores]'],
  ], [1.2, 1.6, 5.2]),
  espacio(),
  P('La versión 2 se desarrolló en una jornada de trabajo intensivo con asistencia de IA (ver 3.3).'),
  H2('3.2 Herramientas'),
  B('**Java 17**, compilado con el JDK 25 y la opción `--release 17`, para que corra en cualquier JDK desde el 17. No usa librerías externas.'),
  B('**IntelliJ IDEA 2025.3** como entorno de desarrollo.'),
  B('**Swing** para la interfaz. Las caras de los personajes se dibujan con Graphics2D a partir de sus atributos, sin imágenes.'),
  B('**Git y GitHub** para versionar el proyecto.'),
  B('**Claude Code** (Anthropic) como asistente de IA; ver 3.3.'),
  B('**Microsoft Word** para este documento y **Google Chrome** para exportar los diagramas a imagen.'),
  H2('3.3 Uso de inteligencia artificial'),
  P('Sí, usamos IA. Trabajamos con Claude (modelo Claude Opus 5.5, de Anthropic) a través de Claude Code, un asistente que lee y escribe los archivos del proyecto y ejecuta comandos en la computadora.'),
  B('**Para qué.** A partir de la consigna, la IA propuso el diseño, escribió el código Java, las pruebas y las herramientas de análisis, generó los diagramas UML y las capturas, y redactó el borrador de este documento.'),
  B('**Qué decidió el equipo.** Rehacer el proyecto desde cero en lugar de continuar la versión 1; usar una interfaz Swing; agregar atributos para que los 23 personajes sean distinguibles; decir "pelado" en lugar de "calvo"; entregar el documento en Word. [COMPLETAR: otras decisiones y revisiones del equipo.]'),
  B('**Cómo se verificó.** Todo el código compila y pasa 54 pruebas automáticas. Los tiempos, las cantidades de turnos y los contraejemplos de este documento salen de ejecutar el programa, no de estimaciones.'),
  B('**Lo que no reemplaza.** En la defensa, cada integrante tiene que poder explicar cualquier parte del código. [COMPLETAR: cómo se repartió el estudio del código.]'),
  H2('3.4 División del trabajo'),
  tabla([
    ['Integrante', 'Tareas'],
    ['[COMPLETAR]', '[COMPLETAR]'],
    ['[COMPLETAR]', '[COMPLETAR]'],
    ['[COMPLETAR]', '[COMPLETAR]'],
  ], [1, 3]),
  H2('3.5 Problemas encontrados y cómo se resolvieron'),
  tabla([
    ['Problema', 'Solución'],
    ['Con los filtros de la consigna, 23 personajes no entran en 16 perfiles: 7 quedaban idénticos a otro.', 'Agregar barba y sombrero (48 perfiles), y una precondición que rechaza el catálogo si dos personajes tienen el mismo perfil.'],
    ['La versión 1 no cumplía la consigna: no tenía Máquina vs Máquina, ni ordenamiento, ni estrategia.', 'Reescritura completa. Sólo se conservó la idea de un tablero de atributos.'],
    ['Encapsular el secreto sin perder la posibilidad de mostrarlo al final.', 'El secreto es private en Jugador y no tiene getter; hacia afuera, el jugador sólo es un Oraculo. Partida.revelarSecreto() funciona únicamente cuando la partida terminó.'],
    ['Un ordenamiento de 23 elementos tarda alrededor de un microsegundo: menos de lo que el reloj mide con precisión.', 'Repetir cada ordenamiento 20.000 veces sobre copias preparadas de antemano, calentar el JIT antes de medir y reportar la mediana de 7 corridas.'],
    ['Para probar que la máquina siempre adivina hacía falta que el rival no le ganara antes.', 'Una prueba que hace jugar a la máquina sola contra cada uno de los 23 secretos.'],
    ['Empates en la función de selección: en el primer turno, ¿Es mujer? y ¿Usa lentes? descartan 11 cada uno.', 'Desempate determinista por el orden del enum. La máquina es predecible y el árbol de decisión es el mismo en cada ejecución.'],
    ['El análisis de optimalidad mostró que, con 3 candidatos, la máquina preguntaba cuando le convenía arriesgar.', 'El umbral de parada pasó de 2 a 3 candidatos. Con ese cambio, en el tablero completo la estrategia empata al óptimo exacto (sección 5.4).'],
    ['Las capturas de la interfaz salían con textos cortados al dibujarlas en alta resolución.', 'Generarlas con la escala de interfaz de Java en 2 (-Dsun.java2d.uiScale=2), para que los textos midan lo mismo al armar la pantalla y al dibujarla.'],
  ], [1, 1.2]),
  H2('3.6 Fragmentos de código y su complejidad'),
  P('Estas son capturas del código real, con sus números de línea. En la esquina de cada una va su complejidad temporal. La notación es: n, la cantidad de personajes del tablero (23); c, los candidatos que le quedan al jugador (c ≤ n); f, la cantidad de filtros (8); y h, la altura del árbol de decisión.'),
  ...fragmento('cod-mergesort', '**MergeSort.** Cada nivel de la recursión combina n elementos en total y hay ⌈log₂ n⌉ niveles. La recurrencia es T(n) = 2T(n/2) + Θ(n), que por el caso 2 del teorema maestro (a = 2, b = 2, f(n) = n = n^(log_b a)) da Θ(n log n). La comparación estricta en `combinar` hace que el algoritmo sea estable.'),
  ...fragmento('cod-tablero', '**Tablero.agregar.** Compara con el último personaje y agrega al final: O(1). El contador asigna el ID autoincremental. La verificación de orden hace que la lista quede ordenada por construcción.'),
  ...fragmento('cod-organizador', '**OrganizadorTablero.organizar.** Validar es O(n), ordenar Θ(n log n) y agregar O(n). En total, Θ(n log n).'),
  ...fragmento('cod-binaria', '**Búsqueda binaria.** T(n) = T(n/2) + O(1), es decir, O(log n). Con 23 personajes hace a lo sumo 5 comparaciones.'),
  ...fragmento('cod-greedy', '**Evaluación y selección del Greedy.** evaluar recorre f filtros × c candidatos: O(f · c). elegir recorre las evaluaciones: O(f). Como f está fijo en 8, un turno es lineal en la cantidad de candidatos.'),
  ...fragmento('cod-maquina', '**Turno de la máquina.** Decide entre arriesgar (O(c) para elegir) y preguntar (O(f · c) para evaluar, más O(c) para descartar). Nunca recibe ni guarda el secreto del rival: sólo el Oraculo, como parámetro.'),
  ...fragmento('cod-descarte', '**Descarte de candidatos.** Un recorrido del conjunto con un iterador que elimina en O(1): O(c) en total. Registrar la respuesta en la base de conocimiento es O(1).'),
  ...fragmento('cod-dependencia', '**Dependencia lógica.** Revisa el grupo exclusivo del pelo (4 filtros) y la regla de la barba. Con 8 filtros en total, es O(1).'),
  ...fragmento('cod-arbol', '**Árbol de decisión.** En cada nivel del árbol, entre todos los nodos, se evalúan los f filtros sobre los n candidatos: O(f · n) por nivel, O(f · n · h) en total. Con divisiones parejas, h ≈ log₂ n.'),
  ...fragmento('cod-oraculo', '**El secreto.** Responder una pregunta o verificar un arriesgue es O(1). Los métodos son final: ninguna subclase puede cambiar cómo se responde.'),
  ...fragmento('cod-validador', '**Precondiciones del catálogo.** Una sola pasada con HashSet y HashMap: O(n).'),
];

const dyc = [
  H1('4. Justificación algorítmica de Divide y Conquista'),
  H2('4.1 Algoritmo de ordenamiento inicial'),
  P('El catálogo llega como pide la consigna: primero las 11 mujeres, después los 12 hombres, sin ningún otro orden dentro de cada grupo. La máquina lo ordena con MergeSort, que aplica los tres pasos de Divide y Conquista:'),
  B('**Dividir:** parte el rango [inicio..fin] por la mitad.'),
  B('**Conquistar:** ordena cada mitad recursivamente. El caso base es un rango de 0 o 1 elemento, que ya está ordenado.'),
  B('**Combinar:** intercala las dos mitades ordenadas en un solo recorrido, en O(n).'),
  F('T(n) = 2·T(n/2) + Θ(n)   ⇒   T(n) = Θ(n log n)'),
  P('Por el teorema maestro, a = 2, b = 2 y f(n) = Θ(n) = Θ(n^(log₂ 2)): es el caso 2, y el costo es Θ(n log n) en el mejor caso, en el promedio y en el peor. Con n = 23, MergeSort hace 22 divisiones, 22 combinaciones y 76 comparaciones. La pantalla "Ordenamiento del tablero" muestra la traza completa.'),
  P('Después de ordenar, la máquina agrega los personajes al `Tablero` de a uno. `Tablero.agregar()` les asigna el ID con un contador que se incrementa en cada agregado: Ana recibe el 1, Bruno el 2, y así hasta Valentina, que recibe el 23. El tablero verifica además que cada personaje nuevo no vaya antes que el último. La lista queda ordenada por construcción, y un agregado fuera de orden lanza una excepción.'),
  H2('4.2 Elección entre MergeSort y QuickSort'),
  tabla([
    ['Criterio', 'MergeSort', 'QuickSort'],
    ['Peor caso', 'Θ(n log n)', 'O(n²), si el pivote cae siempre en un extremo'],
    ['Estabilidad', 'Estable', 'No estable'],
    ['Memoria extra', 'Θ(n)', 'O(log n) de pila en promedio'],
    ['Comparaciones con n = 23', '76', '118'],
    ['Tiempo con n = 23 (mediana)', '0,00110 ms', '0,00071 ms'],
  ], [2, 2, 3]),
  espacio(),
  P('Elegimos MergeSort por tres motivos:'),
  B('**Garantiza Θ(n log n) aun en el peor caso.** No depende de la elección del pivote ni de cómo venga la entrada.'),
  B('**Es estable.** Dos personajes con la misma clave conservan su orden relativo. Si el criterio cambiara (por ejemplo, ordenar por color de pelo), los empates seguirían agrupados por género, como en el catálogo.'),
  B('**Su traza se sigue fácil en pantalla**, porque las divisiones siempre son por la mitad.'),
  P('Su desventaja, la memoria extra Θ(n), no importa con 23 personajes. QuickSort quedó implementado sólo para la comparación de tiempos.'),
  H2('4.3 Criterio de ordenamiento'),
  P('Ordenamos por nombre, en orden alfabético, sin distinguir mayúsculas ni tildes. Cada personaje calcula una sola vez su clave de ordenamiento: `Personaje.normalizar` descompone el texto (forma NFD), borra las marcas diacríticas y lo pasa a minúsculas. Así, "Lucía" y "lucia" comparan igual, y cada comparación es una comparación de cadenas común.'),
  P('El criterio no es arbitrario: hace que el arriesgue también sea Divide y Conquista. Cuando el jugador escribe un nombre, `BusquedaBinaria` lo busca en el tablero ordenado en O(log n): con 23 personajes, 5 comparaciones como máximo. En un tablero ordenado sólo por género habría que buscar de forma secuencial, en O(n).'),
  H2('4.4 Estrategia de selección del filtro'),
  P('En cada turno, la máquina tiene un conjunto de candidatos C (los personajes que todavía pueden ser el secreto del rival) y un conjunto de filtros sin resolver. Cada filtro divide a C en dos grupos: los que responderían SÍ y los que responderían NO. La máquina no sabe con qué grupo se va a quedar, porque la respuesta depende del rival. Lo que sí sabe es que, conteste lo que conteste, descarta por lo menos al grupo más chico. La estrategia elige el filtro que maximiza ese **descarte garantizado**.'),
  P('Es un criterio **minimax**: minimiza el grupo que puede quedar en el peor caso, max(SÍ, NO), lo que equivale a maximizar min(SÍ, NO). El filtro ideal parte a los candidatos por la mitad, como la búsqueda binaria parte un arreglo.'),
  H2('4.5 Función de evaluación del filtro'),
  F('evaluación(f, C) = min( |{p ∈ C : f(p) = SÍ}| , |{p ∈ C : f(p) = NO}| )'),
  P('`EstrategiaGreedy.evaluar()` recorre los filtros sin resolver y, para cada uno, cuenta los candidatos que cumplen: O(f · c). `EstrategiaGreedy.elegir()` se queda con el de mayor evaluación. Si hay empate, gana el primero en el orden del enum. Un filtro con evaluación 0 no separa a nadie (todos los candidatos contestarían lo mismo) y nunca se elige. Así evalúa la máquina su primer turno:'),
  tabla([
    ['Filtro', 'SÍ', 'NO', 'Descarte garantizado'],
    ['¿Es mujer?', '11', '12', '11  ← elegido'],
    ['¿Es pelado?', '4', '19', '4'],
    ['¿Usa lentes?', '11', '12', '11'],
    ['¿Tiene pelo colorado?', '6', '17', '6'],
    ['¿Tiene pelo negro?', '7', '16', '7'],
    ['¿Tiene pelo amarillo?', '6', '17', '6'],
    ['¿Tiene barba?', '6', '17', '6'],
    ['¿Usa sombrero?', '8', '15', '8'],
  ], [3, 1, 1, 2], { numericas: [1, 2, 3], resaltar: [1] }),
  espacio(),
  P('¿Es mujer? y ¿Usa lentes? empatan en 11. Gana ¿Es mujer? porque está primero en el enum.'),
  H2('4.6 Relación con el árbol de decisión del juego'),
  P('Cada partida de la máquina es un recorrido por un árbol binario de decisión. Los nodos internos son preguntas, las ramas son las respuestas SÍ y NO, y las hojas son los arriesgues. La clase `ArbolDecision` construye ese árbol de antemano, con la misma estrategia que usa el juego, y lo hace por Divide y Conquista:'),
  B('**Dividir:** elige el mejor filtro para los candidatos y los separa en dos grupos.'),
  B('**Conquistar:** construye recursivamente el subárbol de cada grupo.'),
  B('**Combinar:** une los dos subárboles en un nodo.'),
  P('Una de las pruebas automáticas verifica que la máquina, en partida, hace exactamente las preguntas del árbol.'),
  P('Para los 23 personajes, el árbol tiene altura 4: la máquina nunca necesita más de 4 preguntas. Contando los arriesgues finales, adivina en 6 turnos en el peor caso y en 5,04 en promedio, si los 23 secretos son igual de probables. La pantalla "Árbol de decisión" lo muestra completo (figura siguiente).'),
  ...imagen('app/08-arbol.png', ANCHO_PX, 'Pantalla "Árbol de decisión": el árbol que genera el Greedy y los turnos que necesita la máquina para cada secreto.'),
  P('La relación con la búsqueda binaria es directa. Si cada pregunta partiera a los candidatos exactamente por la mitad, harían falta ⌈log₂ 23⌉ = 5 preguntas para aislar a cualquier personaje, y ningún método puede hacerlo con menos: es la cota inferior de cualquier árbol binario con 23 hojas. Nuestros filtros no siempre parten por la mitad, así que la estrategia se acerca a ese ideal sin alcanzarlo siempre. Por eso conviene arriesgar al final en lugar de seguir preguntando (sección 4.8).'),
  H2('4.7 Cómo se descartan los candidatos en cada turno'),
  P('`Jugador.preguntar()` recibe la respuesta del Oraculo, recorre los candidatos y elimina a los que contestarían distinto: O(c). Después, la base de conocimiento registra la respuesta y aplica las reglas de dependencia (sección 4.9), que pueden resolver otros filtros sin preguntarlos.'),
  P('Si el turno es un arriesgue fallido, se descarta sólo al personaje arriesgado. En los dos casos se verifica un invariante: el secreto del rival sigue entre los candidatos, así que el conjunto nunca queda vacío. Si quedara vacío, significaría que hubo respuestas contradictorias, y el programa lanza una excepción en lugar de seguir con datos imposibles.'),
  P('En la interfaz, los descartados del último turno se marcan con borde cobre y los de turnos anteriores quedan tachados.'),
  H2('4.8 Criterio de parada y suposición final'),
  P('Preguntar hasta que quede un solo candidato no siempre es lo mejor. Con k candidatos, arriesgar de a uno cuesta, en promedio:'),
  F('E_arriesgar(k) = (1 + 2 + … + k) / k = (k + 1) / 2 turnos'),
  P('Si un arriesgue falla, ese personaje queda descartado y se arriesga el siguiente. La tabla compara esa opción con preguntar primero, usando la mejor división posible:'),
  tabla([
    ['Candidatos (k)', 'Arriesgar de a uno: promedio / peor', 'Preguntar primero: promedio / peor', 'Conviene'],
    ['1', '1 / 1', '—', 'Arriesgar'],
    ['2', '1,5 / 2', '2 / 2', 'Arriesgar'],
    ['3', '2 / 3', '2,33 / 3', 'Arriesgar'],
    ['4', '2,5 / 4', '2,5 / 3', 'Preguntar: empatan en promedio y el peor caso es mejor'],
  ], [1.2, 2, 2, 2.6], { numericas: [] }),
  espacio(),
  P('Por eso la máquina arriesga cuando le quedan **3 candidatos o menos** (`EstrategiaGreedy.UMBRAL_ARRIESGUE = 3`), y elige al azar el orden de los arriesgues. Esta regla no estaba en el primer diseño. La agregamos cuando la herramienta de análisis mostró que, con umbral 2, la máquina promediaba 5,17 turnos contra 5,04 del óptimo, y que la diferencia venía toda de los grupos de 3 candidatos.'),
  P('Hay además un caso de seguridad: si ningún filtro separa a los candidatos, la máquina arriesga. Con un catálogo válido no pasa nunca, porque los perfiles son únicos.'),
  H2('4.9 Manejo de la dependencia lógica'),
  P('Los filtros no son independientes. La clase `BaseConocimiento` guarda en un `EnumMap` cada filtro resuelto con su valor, y aplica dos reglas hasta que no se deduce nada nuevo:'),
  B('**Regla 1: grupo exclusivo del pelo.** Cada personaje cumple exactamente una de cuatro condiciones: es pelado, o tiene pelo colorado, negro o amarillo. Si una es SÍ, las otras tres son NO: un pelado no tiene color de pelo, y quien tiene pelo negro no es pelado ni tiene otro color. Si tres son NO, la cuarta es SÍ.'),
  B('**Regla 2: mujer implica sin barba.** Si ¿Es mujer? es SÍ, ¿Tiene barba? es NO. Por el contrarrecíproco, si ¿Tiene barba? es SÍ, ¿Es mujer? es NO.'),
  P('Los filtros resueltos por inferencia no se vuelven a evaluar. La máquina no gasta turnos en preguntas cuya respuesta ya conoce, y para el humano esos botones se deshabilitan. En Máquina vs Máquina, cada inferencia aparece en la traza con su motivo, por ejemplo: "¿Tiene pelo negro? = NO (porque ¿Es pelado? es SÍ)".'),
  P('Las reglas son O(1): hay 8 filtros, cada pasada revisa a lo sumo 6 condiciones y la propagación termina en pocas pasadas. Si dos respuestas se contradicen, `BaseConocimiento` lanza una excepción. El `ValidadorCatalogo` garantiza que los datos cumplen las mismas reglas.'),
  H2('4.10 Precondiciones y validaciones implementadas'),
  tabla([
    ['Dónde', 'Qué se valida', 'Si falla'],
    ['ValidadorCatalogo', 'Hay exactamente 23 personajes y no hay nombres repetidos (sin distinguir tildes ni mayúsculas).', 'No se arma el tablero'],
    ['ValidadorCatalogo', 'Pelado ⇔ sin color de pelo; quien no es pelado tiene exactamente un color. Ninguna mujer tiene barba.', 'No se arma el tablero'],
    ['ValidadorCatalogo', 'No hay dos personajes con el mismo perfil: todos son distinguibles.', 'No se arma el tablero'],
    ['ValidadorCatalogo', 'El catálogo llega agrupado por género, como pide la consigna.', 'No se arma el tablero'],
    ['Personaje', 'El nombre no está vacío. El ID se asigna una sola vez.', 'Excepción'],
    ['Tablero', 'Cada personaje agregado va después del último. obtenerPorId recibe un ID entre 1 y 23.', 'Excepción'],
    ['Jugador', 'El secreto es un personaje del tablero. No se pregunta un filtro ya resuelto. Los candidatos nunca quedan vacíos.', 'Excepción'],
    ['BaseConocimiento', 'Dos respuestas sobre el mismo filtro no se contradicen.', 'Excepción'],
    ['Partida', 'Cada jugador juega sólo en su turno y nadie juega después del final. Sólo se arriesgan personajes del tablero. Los secretos se revelan recién al terminar.', 'Excepción'],
    ['Interfaz', 'El nombre arriesgado existe (búsqueda binaria). Los botones de filtros resueltos y los controles fuera de turno están deshabilitados.', 'Aviso al usuario'],
  ], [1.6, 4.6, 1.4]),
  espacio(),
  P('Las 54 pruebas automáticas cubren todas estas validaciones.'),
  H2('4.11 Tabla comparativa de tiempos'),
  P('Medimos los cuatro ordenamientos sobre la misma lista de 23 personajes, en el orden del catálogo. Cada valor es el tiempo promedio de un ordenamiento, calculado así:'),
  B('Cada ordenamiento se repitió 20.000 veces, sobre copias de la lista preparadas de antemano.'),
  B('Antes de medir hubo una tanda de calentamiento, para que el compilador JIT de Java optimizara el código.'),
  B('Se reporta la mediana de 7 corridas y, al lado, el mínimo y el máximo.'),
  P('Equipo: procesador AMD Ryzen 7 8845HS, Windows 11, JDK 25.'),
  tabla([
    ['Algoritmo', 'Complejidad', 'Tiempo (ms)', 'Rango (ms)', 'Comparaciones'],
    ['MergeSort', 'Θ(n log n)', '0,00110', '0,00104 – 0,00187', '76'],
    ['QuickSort', 'O(n log n) promedio', '0,00071', '0,00070 – 0,00118', '118'],
    ['Inserción', 'O(n²)', '0,00064', '0,00063 – 0,00080', '146'],
    ['Burbujeo', 'O(n²)', '0,00118', '0,00094 – 0,00167', '232'],
  ], [1.4, 1.8, 1.3, 1.9, 1.4], { numericas: [2, 3, 4] }),
  espacio(),
  P('**Con 23 personajes, la diferencia no es significativa.** Los cuatro tardan entre 0,6 y 1,2 microsegundos, e Inserción es incluso el más rápido. La diferencia entre el mejor y el peor es de medio microsegundo: miles de veces menos que lo que tarda la ventana en dibujar un solo cuadro (unos 16 ms). Además, el tablero se ordena una sola vez por partida.'),
  P('La explicación está en las constantes que la notación O descarta. MergeSort hace menos comparaciones (76 contra 146 de Inserción), pero paga llamadas recursivas, copias al arreglo auxiliar y la conversión entre lista y arreglo. Con n = 23, n·log₂ n ≈ 104 y n² = 529: la diferencia en cantidad de operaciones es chica, y el costo fijo de cada una pesa más. Inserción, además, trabaja en el lugar y aprovecha que la entrada no es completamente aleatoria.'),
  P('La ventaja asintótica aparece cuando n crece. Estos son los mismos algoritmos sobre personajes al azar (mediana de 7 corridas, en ms):'),
  tabla([
    ['n', 'MergeSort', 'QuickSort', 'Inserción', 'Burbujeo'],
    ['23', '0,0011', '0,0007', '0,0006', '0,0012'],
    ['1.000', '0,091', '0,063', '1,047', '5,588'],
    ['10.000', '2,43', '2,16', '138,4', '692,4'],
  ], [1, 1.5, 1.5, 1.5, 1.5], { numericas: [1, 2, 3, 4] }),
  espacio(),
  P('Con 10.000 elementos, Inserción ya tarda 57 veces más que MergeSort, y Burbujeo 285 veces más: 24,8 y 50 millones de comparaciones contra 120 mil. Elegimos MergeSort no por su velocidad con 23 personajes, que en la práctica es la misma, sino por la garantía de peor caso, la estabilidad y porque el diseño no depende del tamaño del tablero. Con 1.000 personajes, el juego seguiría funcionando igual de bien.'),
];

const greedy = [
  H1('5. Justificación de la estrategia Greedy utilizada'),
  H2('5.1 Por qué elegimos esta variante'),
  P('Los cinco elementos de la técnica, como los vimos en clase:'),
  tabla([
    ['Elemento', 'En nuestro juego'],
    ['Conjunto de candidatos', 'Los filtros sin resolver.'],
    ['Función de selección', 'El filtro con mayor descarte garantizado, min(SÍ, NO).'],
    ['Función de factibilidad', 'El filtro tiene que separar a los candidatos (descarte > 0) y no puede estar resuelto por la base de conocimiento.'],
    ['Función objetivo', 'Adivinar el secreto en la menor cantidad de turnos.'],
    ['Función solución', 'Quedan 3 candidatos o menos: a partir de ahí, se arriesga.'],
  ], [1, 2.6]),
  espacio(),
  P('Consideramos otras funciones de selección:'),
  B('**Maximizar los SÍ**, es decir, preguntar por la característica más común. Descarta mucho cuando la respuesta es NO y casi nada cuando es SÍ: el resultado depende de la suerte.'),
  B('**Elegir al azar.** No aprovecha la información de los candidatos.'),
  B('**Máxima ganancia de información (entropía)**, como el algoritmo ID3 para árboles de decisión. Con preguntas de sí o no y candidatos igual de probables, también prefiere las divisiones más parejas, así que elige casi siempre lo mismo que nuestro criterio. Pero necesita logaritmos, y su resultado es más difícil de interpretar.'),
  P('Elegimos el descarte garantizado por tres razones: protege contra la respuesta menos favorable, se calcula con conteos enteros y se explica en una frase: "pase lo que pase, esta pregunta descarta por lo menos a tantos".'),
  H2('5.2 Cómo se calcula el descarte'),
  P('Para cada filtro sin resolver se recorren los c candidatos y se cuentan los que cumplen (SÍ). Los que no cumplen son c − SÍ. El descarte garantizado es el menor de los dos números. Después de la respuesta, el descarte real es el grupo que no coincidió, que es mayor o igual al garantizado.'),
  H2('5.3 Por qué es eficiente'),
  P('Un turno cuesta O(f · c): con f = 8 filtros y c ≤ 23 candidatos, menos de 200 evaluaciones de un atributo. La estrategia no mira hacia adelante ni vuelve atrás: decide con la información del turno y no guarda estados.'),
  P('Una estrategia óptima, en cambio, tiene que considerar todas las secuencias posibles de preguntas y respuestas. Construir el árbol de decisión óptimo es un problema NP-completo en general (Hyafil y Rivest, 1976). Nuestra herramienta de verificación lo resuelve de forma exacta sólo porque 23 es un número chico: explora hasta 2²³ ≈ 8,4 millones de subconjuntos de candidatos.'),
  H2('5.4 ¿La estrategia es óptima?'),
  P('**En nuestro tablero, sí.** La herramienta `AnalisisOptimalidad` calcula la mejor estrategia posible con las mismas reglas (cualquier secuencia de preguntas y arriesgues), por búsqueda exhaustiva con memoria, y la compara con la nuestra:'),
  tabla([
    ['Estrategia', 'Peor caso', 'Promedio'],
    ['Greedy (la del juego, umbral de parada 3)', '6 turnos', '5,04 turnos'],
    ['Óptima (búsqueda exhaustiva)', '6 turnos', '5,04 turnos'],
    ['Greedy con umbral de parada 2 (primera versión)', '6 turnos', '5,17 turnos'],
  ], [4, 1.5, 1.5], { numericas: [1, 2] }),
  espacio(),
  P('**En general, no.** El Greedy mira un solo turno hacia adelante. La herramienta buscó subconjuntos del tablero donde pierde y encontró este, con 6 personajes:'),
  tabla([
    ['Personaje', 'Características'],
    ['Ana', 'Mujer, pelo amarillo, lentes'],
    ['Elena', 'Mujer, pelada, lentes'],
    ['Florencia', 'Mujer, pelo colorado, lentes'],
    ['Hernán', 'Hombre, pelado, lentes, sombrero'],
    ['Paula', 'Mujer, pelo negro, lentes, sombrero'],
    ['Tomás', 'Hombre, pelo negro, barba'],
  ], [1, 3]),
  espacio(),
  P('En el primer turno empatan tres filtros con descarte garantizado 2: ¿Es mujer? (4 SÍ, 2 NO), ¿Es pelado? (2 SÍ, 4 NO) y ¿Tiene pelo negro? (2 SÍ, 4 NO). El Greedy elige ¿Es mujer? por el orden de desempate. Si la respuesta es SÍ, quedan cuatro mujeres. ¿Es pelado? separa sólo a Elena, y quedan tres (Ana, Florencia y Paula) que se arriesgan de a uno: **5 turnos** en el peor caso.'),
  P('La estrategia óptima pregunta primero ¿Es pelado?. Deja dos grupos, {Elena, Hernán} y {Ana, Florencia, Paula, Tomás}, que las preguntas siguientes parten por la mitad, y adivina siempre en **4 turnos**.'),
  P('**Por qué pasa.** La función de selección mide cuánto se descarta ahora, pero no qué tan fácil de dividir es lo que queda. Tres preguntas que valen lo mismo en este turno dejan grupos que valen distinto en el siguiente. Para evitarlo habría que evaluar dos o más turnos hacia adelante, y eso es justamente dejar de ser Greedy.'),
  P('Hay un segundo caso, con 4 personajes (Florencia, Gustavo, Julieta y Valentina), donde ningún filtro los parte en 2 y 2: todos dejan 3 de un lado y 1 del otro. Si pregunta, la máquina promedia 2,75 turnos; si arriesga de a uno desde el principio, 2,5. El criterio de parada cuenta candidatos, pero no tiene en cuenta que las preguntas disponibles sean malas.'),
];

const noUsados = [
  H1('6. Algoritmos no utilizados'),
  tabla([
    ['Algoritmo visto en clase', 'Por qué no lo usamos'],
    ['QuickSort', 'Está implementado, pero sólo para comparar. No lo usamos para el tablero porque su peor caso es O(n²) y no es estable (sección 4.2).'],
    ['Inserción y Burbujeo', 'Son cuadráticos. Están en el proyecto sólo como referencia para la tabla de tiempos.'],
    ['Dijkstra, Prim y Kruskal', 'Resuelven problemas sobre grafos con pesos: caminos mínimos y árboles de recubrimiento mínimo. En el juego no hay grafo ni costos entre personajes.'],
    ['Código de Huffman', 'Construye el árbol de prefijos de largo promedio mínimo cuando cualquier división de los símbolos está permitida. Es muy cercano a nuestro problema: si se pudiera preguntar "¿está en este grupo?" por cualquier grupo de personajes, Huffman daría el árbol de preguntas óptimo en promedio. No aplica porque sólo se puede preguntar por los 8 filtros, que dividen a los candidatos de formas fijas.'],
    ['Mochila fraccionaria y problema del cambio', 'Optimizan una suma de valores con una capacidad o un monto. En el juego no hay pesos, valores ni montos que repartir.'],
    ['Matrimonios estables', 'Empareja dos grupos según sus preferencias. En el juego no hay nada que emparejar.'],
    ['Otros ejemplos de Divide y Conquista (torres de Hanoi, elemento mayoritario, suma parcial máxima, fixture de torneo, potencia rápida)', 'Resuelven problemas que el juego no tiene. De esa familia sí usamos la búsqueda binaria, para el arriesgue.'],
    ['Programación dinámica', 'Todavía no la vimos en la materia. La herramienta de verificación usa una búsqueda exhaustiva con memorización, que es la idea de la programación dinámica, para calcular la estrategia óptima. No está en el juego: necesita memoria para 2²³ estados, y con más personajes se vuelve impracticable, mientras que el Greedy decide cada turno en O(f · c).'],
  ], [2.2, 5]),
];

const bigO = [
  H1('7. Notación Big O para nuestra app'),
  tabla([
    ['Operación', 'Clase', 'Complejidad'],
    ['Validar el catálogo', 'ValidadorCatalogo', 'O(n)'],
    ['Ordenar el tablero', 'MergeSort', 'Θ(n log n)'],
    ['Asignar IDs y agregar', 'Tablero', 'O(1) cada uno, O(n) en total'],
    ['Buscar un personaje por ID', 'Tablero', 'O(1)'],
    ['Buscar un personaje por nombre (arriesgue)', 'BusquedaBinaria', 'O(log n)'],
    ['Evaluar los filtros y elegir uno', 'EstrategiaGreedy', 'O(f · c)'],
    ['Descartar candidatos', 'Jugador', 'O(c)'],
    ['Inferir por dependencia lógica', 'BaseConocimiento', 'O(1)'],
    ['Responder una pregunta', 'Oraculo (Jugador)', 'O(1)'],
    ['Construir el árbol de decisión', 'ArbolDecision', 'O(f · n · h)'],
  ], [3, 2, 2]),
  espacio(),
  P('Una partida tiene dos partes. La **preparación** valida y ordena el tablero: O(n) + Θ(n log n) = Θ(n log n). Después vienen los **turnos**: cada turno de la máquina cuesta O(f · c) ≤ O(f · n), y la cantidad de turnos t está acotada por la altura del árbol de decisión más los arriesgues finales.'),
  F('T(partida) = Θ(n log n) + t · O(f · n)'),
  P('¿Cuánto vale t? En el peor caso teórico, con filtros que separaran a un solo personaje por vez, cada pregunta descartaría uno y t crecería como n: la partida sería O(f · n²). Ese caso no puede darse en nuestro juego. La cantidad de filtros es fija (f = 8) y cada uno se pregunta una vez como mucho, así que la máquina hace a lo sumo 8 preguntas y 3 arriesgues. Y con filtros que dividen parejo, como los nuestros, t crece como log₂ n: en el tablero real, la máquina adivina en 6 turnos como máximo.'),
  P('Con f constante y t proporcional a log n, los turnos cuestan O(n log n), igual que la preparación. Por eso la notación más certera para el juego es **O(n log n)**: el ordenamiento inicial y el recorrido del árbol de decisión crecen al mismo ritmo, y ninguna parte del programa es cuadrática. La interfaz no cambia el análisis, porque dibujar el tablero es O(n) por cuadro.'),
];

const reflexion = [
  H1('8. Reflexión sobre el TP'),
  H2('Logros'),
  B('La máquina adivina a cualquiera de los 23 personajes en 6 turnos como máximo y en 5,04 en promedio. En este tablero, eso es exactamente el óptimo.'),
  B('El secreto del humano está protegido por diseño: la máquina no tiene ninguna forma de leerlo.'),
  B('Todo lo que afirma este documento se puede reproducir, porque las pruebas, las mediciones y el análisis de optimalidad son programas del proyecto.'),
  B('El modo Máquina vs Máquina muestra cada decisión con sus números, así que el algoritmo se puede seguir y explicar turno por turno.'),
  H2('Dificultades'),
  B('Los filtros de la consigna no alcanzaban para distinguir a 23 personajes.'),
  B('Medir de forma confiable tiempos de un microsegundo.'),
  B('Encontrar un contraejemplo concreto para el Greedy. En el tablero completo empata al óptimo, así que hubo que buscarlo en subconjuntos.'),
  B('La ventaja de empezar: en 200 partidas Máquina vs Máquina con secretos al azar, la máquina que empieza ganó 130 (65 %).'),
  H2('Propuestas de mejora'),
  B('Desempatar la función de selección mirando un turno más adelante, para evitar el contraejemplo de la sección 5.4.'),
  B('Un criterio de parada que compare el costo esperado de preguntar y de arriesgar con los candidatos concretos, en lugar de un umbral fijo.'),
  B('Niveles de dificultad; por ejemplo, una máquina "fácil" que elija filtros al azar.'),
  B('Cargar el catálogo desde un archivo, para jugar con otros personajes sin recompilar.'),
  B('Un modo en el que el humano conteste a mano, con detección de respuestas contradictorias (la base de conocimiento ya las detecta).'),
  B('Pasar las pruebas a JUnit.'),
  P('[COMPLETAR: reflexión personal del equipo]'),
];

const biblio = [
  H1('9. Bibliografía, fuentes y ayudas'),
  B('Consigna "TP – Adivina Quién" y "Documentación técnica – Trabajo práctico obligatorio". Diseño y Análisis de Algoritmos, prof. Juan Ignacio López, UADE, 2026.'),
  B('Diapositivas de la cátedra de Programación III (UADE): notación Big O, Divide y Conquista y algoritmos Greedy.'),
  B('Cormen, T. H., Leiserson, C. E., Rivest, R. L. y Stein, C. Introduction to Algorithms, 3.ª ed. MIT Press, 2009. Capítulos 2 (MergeSort), 4 (teorema maestro), 7 (QuickSort) y 16 (algoritmos greedy y Huffman).'),
  B('Hyafil, L. y Rivest, R. L. "Constructing optimal binary decision trees is NP-complete". Information Processing Letters 5(1), 1976, pp. 15–17.'),
  B('Quinlan, J. R. "Induction of decision trees". Machine Learning 1, 1986, pp. 81–106 (algoritmo ID3).'),
  B('Documentación oficial de Java SE 17: java.util (ArrayList, LinkedHashSet, EnumMap, EnumSet) y javax.swing.'),
  B('Claude (Anthropic), a través de Claude Code: asistente de IA usado para el diseño, el código, las pruebas y la redacción (ver 3.3).'),
  B('[COMPLETAR: otras fuentes que haya usado el equipo]'),
];

const anexo = [
  H1salto('Anexo · Capturas de la aplicación'),
  P('Capturas de las pantallas del juego. No cuentan para la extensión del documento.'),
  ...imagen('app/01-inicio.png', ANCHO_PX, 'Menú principal.'),
  ...imagen('app/02-jvm-eleccion.png', ANCHO_PX, 'Jugador vs Máquina: el humano elige su personaje haciendo clic en una carta.'),
  ...imagen('app/03-jvm-partida.png', ANCHO_PX, 'Jugador vs Máquina en juego. A la izquierda, los candidatos del humano; abajo a la derecha, los de la máquina (el personaje del humano, en dorado). Los botones de filtros resueltos se deshabilitan.'),
  ...imagen('app/04-mvm.png', ANCHO_PX, 'Máquina vs Máquina: cada tablero muestra los candidatos de una máquina. Abajo, la traza con la tabla de evaluación del Greedy en cada turno.'),
  ...imagen('app/05-orden-tablas.png', ANCHO_PX, 'Del catálogo agrupado por género al tablero ordenado por MergeSort, con ID autoincremental.'),
  ...imagen('app/06-orden-traza.png', ANCHO_PX, 'Traza de MergeSort: cada división y cada combinación.'),
  ...imagen('app/07-orden-tiempos.png', ANCHO_PX, 'Comparación de tiempos en la aplicación. Son los de una sola corrida; la tabla de la sección 4.11 usa la mediana de 7.'),
];

// ---------------------------------------------------------------- documento
const encabezado = new Header({ children: [new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: 'TPO 1 · Adivina Quién · Programación III', size: 16, color: SUAVE })] })] });
const pie = new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ size: 18, color: SUAVE, children: ['Página ', PageNumber.CURRENT, ' de ', PageNumber.TOTAL_PAGES] })] })] });
const margen = { top: 1134, bottom: 1134, left: 1134, right: 1134, header: 567, footer: 567 };
const vertical = { page: { size: { width: 11906, height: 16838 }, margin: margen } };
const apaisada = { page: { size: { width: 11906, height: 16838, orientation: PageOrientation.LANDSCAPE }, margin: margen } };

const doc = new Document({
  creator: 'Equipo TPO Programación III', title: 'Adivina Quién — Documentación técnica',
  styles: {
    default: { document: { run: { font: 'Calibri', size: 21, color: TINTA } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 32, bold: true, color: AZUL, font: 'Calibri' }, paragraph: { spacing: { before: 360, after: 160 }, outlineLevel: 0, keepNext: true } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 26, bold: true, color: TINTA, font: 'Calibri' }, paragraph: { spacing: { before: 220, after: 100 }, outlineLevel: 1, keepNext: true } },
    ],
  },
  numbering: { config: [{ reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
    style: { paragraph: { indent: { left: 540, hanging: 270 } } } }] }] },
  sections: [
    { properties: vertical, children: portada },
    { properties: vertical, headers: { default: encabezado }, footers: { default: pie }, children: [...introduccion, ...uml] },
    { properties: apaisada, headers: { default: encabezado }, footers: { default: pie }, children: umlApaisado },
    { properties: vertical, headers: { default: encabezado }, footers: { default: pie },
      children: [...relaciones, ...bitacora, ...dyc, ...greedy, ...noUsados, ...bigO, ...reflexion, ...biblio, ...anexo] },
  ],
});

Packer.toBuffer(doc).then(buf => { fs.writeFileSync(SALIDA, buf); console.log('ok', SALIDA, figura, 'figuras'); });
