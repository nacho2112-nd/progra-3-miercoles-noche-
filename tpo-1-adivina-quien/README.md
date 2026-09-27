# Adivina Quién — TPO 1 de Programación III (UADE)

Juego de adivinanzas en Java con interfaz Swing. Tiene dos modos: **Jugador vs Máquina** y **Máquina vs Máquina**. En el segundo se ve todo el proceso de la máquina, turno por turno.

- 23 personajes. El catálogo llega agrupado sólo por género.
- La máquina ordena el catálogo con **MergeSort** por nombre y asigna un **ID autoincremental** al agregar cada personaje al tablero.
- En cada turno la máquina elige la pregunta con una estrategia **Greedy**: el filtro con mayor *descarte garantizado*, `min(SÍ, NO)`.
- Filtros: género, pelado, lentes, pelo colorado / negro / amarillo, barba y sombrero.
- La máquina no puede leer el personaje del humano. Sólo le hace preguntas a través de la interfaz `Oraculo`.

## Requisitos

JDK 17 o superior. No usa librerías externas.

## Cómo correrlo

**Desde IntelliJ IDEA**: abrir la carpeta del proyecto y ejecutar `adivinaquien.App`.

**Desde la consola**, en la carpeta del proyecto:

```
javac -encoding UTF-8 -d out src/adivinaquien/*.java src/adivinaquien/*/*.java
java -cp out adivinaquien.App
```

En Windows PowerShell, el comando de compilación es:

```
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

## Pruebas y herramientas (carpeta `test/`)

Para compilarlas, agregar `test` a la lista de fuentes del `javac`.

| Clase | Qué hace |
|---|---|
| `adivinaquien.motor.PruebasAutomaticas` | 54 verificaciones: catálogo, ordenamientos, IDs, búsqueda binaria, dependencia lógica, estrategia, turnos y encapsulamiento del secreto. |
| `adivinaquien.AnalisisOptimalidad` | Compara Greedy con la estrategia óptima (búsqueda exhaustiva) y busca contraejemplos. Correr con `-Xmx1g`. |
| `adivinaquien.MedicionTiempos` | Mediana de 7 corridas del benchmark de ordenamientos. |
| `adivinaquien.GeneradorCapturas` | Genera las capturas de pantalla del documento. |

## Documentación técnica

- [`docs/TPO-AdivinaQuien-Documentacion.docx`](docs/TPO-AdivinaQuien-Documentacion.docx): el documento que se imprime para la defensa. Antes de imprimir, completar los campos resaltados `[COMPLETAR]`: integrantes, comisión, fecha, división del trabajo y reflexión del equipo.
- [`docs/TPO-AdivinaQuien-Documentacion.pdf`](docs/TPO-AdivinaQuien-Documentacion.pdf): la misma versión, en PDF.
- [`docs/img/`](docs/img): diagramas UML, capturas de código y capturas de la aplicación.
- [`docs/generador/`](docs/generador): scripts que regeneran las imágenes y el documento cuando cambia el código.

## Estructura

```
src/adivinaquien/
  App.java       punto de entrada
  modelo/        Personaje, Genero, ColorPelo, Filtro, Tablero
  datos/         CatalogoPersonajes, ValidadorCatalogo
  algoritmos/    MergeSort, QuickSort, Inserción, Burbujeo, BusquedaBinaria, Benchmark
  motor/         Oraculo, Jugador, JugadorHumano, JugadorMaquina, EstrategiaGreedy,
                 BaseConocimiento, ArbolDecision, Partida, OrganizadorTablero
  ui/            ventana y pantallas Swing
test/adivinaquien/  pruebas y herramientas de análisis
docs/               documentación técnica, imágenes y generador
```

## Dónde tocar para cambiar algo

| Quiero cambiar… | Archivo |
|---|---|
| Los personajes | `src/adivinaquien/datos/CatalogoPersonajes.java`. El validador rechaza el catálogo si dos personajes quedan iguales. |
| Las preguntas disponibles | `src/adivinaquien/modelo/Filtro.java`. El orden de las constantes es el desempate del Greedy. |
| Cuándo arriesga la máquina | `EstrategiaGreedy.UMBRAL_ARRIESGUE` |
| Las reglas de dependencia lógica | `src/adivinaquien/motor/BaseConocimiento.java` |
| Cómo se dibujan las caras | `src/adivinaquien/ui/TarjetaPersonaje.java` |

Después de cualquier cambio, correr `PruebasAutomaticas`.
