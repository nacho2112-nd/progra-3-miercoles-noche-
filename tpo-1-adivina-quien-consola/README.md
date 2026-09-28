# TPO 1 — Adivina Quién (versión consola)

Versión de consola del equipo. La base es el repo de Pilar ([`pilipayo/Programacion3-TPO`](https://github.com/pilipayo/Programacion3-TPO)), y toma cosas de la versión Swing anterior (commit `8c6cd4d` de `main`, carpeta `tpo-1-adivina-quien/`): los 23 personajes, MergeSort, la interfaz `Oraculo` y el criterio Greedy.

## Cómo se juega

- **Campaña de 2 niveles.** Elegís tu personaje una sola vez y jugás contra la Máquina 1. Si le ganás, pasás a la Máquina 2, que arranca sabiendo todo lo que la 1 ya te preguntó.
- **Turnos.** En cada turno se pregunta un filtro o se arriesga. Las preguntas no tienen límite y se pierde cuando el otro acierta.
- **Máquina vs Máquina.** Muestra cada decisión de las dos máquinas, turno por turno.
- **Marcador.** Se guarda en `marcador.txt` y suma una partida cuando le ganás a las dos máquinas.

## Algoritmos

| Dónde | Qué | Costo |
|---|---|---|
| `JugadorMaquina.armarTablero` | MergeSort por nombre + ID autoincremental (Divide y Conquista) | Θ(n log n) |
| Máquina 1 | Pregunta un atributo al azar, sin memoria; arriesga con ≤ 2 candidatos | O(a) por turno |
| Máquina 2 | Greedy: la pregunta con mayor min(SÍ, NO); arriesga con ≤ 3 candidatos | O(q · c) por turno |

En 2000 partidas simuladas, la Máquina 2 necesita 5,04 turnos en promedio para adivinar y la Máquina 1, 7,65.

## Compilar y correr

Hace falta JDK 25, porque `Main` es un archivo compacto (`void main()`). Desde esta carpeta:

```
javac -encoding UTF-8 -d out src/Main.java src/*/*.java
java -cp out Main
```

Sin argumentos abre la ventana (Swing, paquete `Interfaz`). Con `java -cp out Main consola` se juega por consola.
