package Juego;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Marcador persistido en marcador.txt: una linea "nombre;partidasGanadas;nivelMaximo" por jugador.
public class Marcador {

    private static final Path ARCHIVO = Path.of("marcador.txt");
    private final Map<String, int[]> datos = new LinkedHashMap<>(); // nombre -> {ganadas, nivelMaximo}

    public Marcador() {
        try {
            for (String linea : Files.readAllLines(ARCHIVO)) {
                String[] c = linea.split(";");
                datos.put(c[0], new int[] {Integer.parseInt(c[1]), Integer.parseInt(c[2])});
            }
        } catch (IOException | RuntimeException e) {
            // todavia no hay marcador: arranca vacio
        }
    }

    // Guarda al instante y devuelve cuantas partidas lleva ganadas
    public int registrar(String nombre, int nivel, boolean gano) {
        int[] d = datos.computeIfAbsent(nombre, k -> new int[2]);
        if (gano) d[0]++;
        d[1] = Math.max(d[1], nivel);
        List<String> lineas = new ArrayList<>();
        datos.forEach((n, v) -> lineas.add(n + ";" + v[0] + ";" + v[1]));
        try {
            Files.write(ARCHIVO, lineas);
        } catch (IOException e) {
            System.out.println("No se pudo guardar el marcador: " + e.getMessage());
        }
        return d[0];
    }

    public void mostrar() {
        System.out.println("=== Marcador ===");
        if (datos.isEmpty()) System.out.println("Todavía no jugó nadie.");
        datos.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()[0]
                        + " partidas ganadas (llegó al nivel " + e.getValue()[1] + ")"));
    }
}
