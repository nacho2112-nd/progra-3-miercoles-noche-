package Juego;

import Functional_Things.Consola;
import Functional_Things.GeneradorPersonajes;
import Functional_Things.IndiceAtributos;
import Jugadores.Jugador;
import Jugadores.JugadorHumano;
import Jugadores.JugadorMaquina;
import Jugadores.Oraculo;
import Things.Personajes;
import Things.Pregunta;
import java.util.List;

public class AdivinaQuien {

    private final Marcador marcador = new Marcador();

    public void iniciar() {
        while (true) {
            System.out.println("\n=== Adivina Quién ===\n1 - Jugar (2 niveles)\n2 - Máquina vs Máquina\n3 - Marcador\n0 - Salir");
            switch (Consola.leerEntero("Opción:", 0, 3)) {
                case 1 -> jugarCampania();
                case 2 -> jugarMaquinaVsMaquina();
                case 3 -> marcador.mostrar();
                default -> {
                    System.out.println("¡Gracias por jugar!");
                    return;
                }
            }
        }
    }

    // Nivel 1 contra la Maquina 1; si gana, nivel 2 contra la Maquina 2 con el MISMO personaje.
    private void jugarCampania() {
        String nombre = Consola.leerTexto("Tu nombre:").replace(";", "");
        if (nombre.isEmpty()) nombre = "Anónimo";
        List<Personajes> tablero = armarTablero(false);
        IndiceAtributos indice = new IndiceAtributos(tablero);
        JugadorHumano humano = new JugadorHumano(nombre, tablero, indice);
        humano.elegirSecreto(tablero);

        System.out.println("\n=== NIVEL 1: contra la Máquina 1 (pregunta al azar) ===");
        JugadorMaquina rival = new JugadorMaquina(1, tablero, indice, List.of(), false);
        rival.elegirSecreto(tablero);
        boolean gano = jugar(humano, rival, false) == humano;
        int nivel = 1;

        if (gano) {
            nivel = 2;
            // Ventaja de la Maquina 2: arranca sabiendo todo lo que la Maquina 1 le pregunto al humano
            JugadorMaquina m2 = new JugadorMaquina(2, tablero, indice, rival.getHistorial(), false);
            m2.elegirSecreto(sinElDe(rival, tablero));
            System.out.println("\n=== NIVEL 2: contra la Máquina 2 (greedy) ===");
            System.out.println("Se acuerda de las " + rival.getHistorial().size() + " preguntas de la Máquina 1: arranca con "
                    + m2.getCandidatos().size() + " candidatos en vez de " + tablero.size() + ".");
            humano.reiniciar();
            rival = m2;
            gano = jugar(humano, rival, false) == humano;
        }

        if (!gano) System.out.println("Perdiste. El personaje de la " + rival.getNombre() + " era " + secretoDe(rival, tablero));
        int ganadas = marcador.registrar(nombre, nivel, gano);
        System.out.println((gano ? "¡Le ganaste a las dos máquinas! " : "") + nombre + " lleva " + ganadas + " partidas ganadas.");
    }

    // Las dos maquinas en modo verboso: se ve cada decision, turno por turno
    private void jugarMaquinaVsMaquina() {
        List<Personajes> tablero = armarTablero(true);
        IndiceAtributos indice = new IndiceAtributos(tablero);
        JugadorMaquina m1 = new JugadorMaquina(1, tablero, indice, List.of(), true);
        m1.elegirSecreto(tablero);
        JugadorMaquina m2 = new JugadorMaquina(2, tablero, indice, List.of(), true);
        m2.elegirSecreto(sinElDe(m1, tablero));
        System.out.println("Máquina 1 eligió a " + secretoDe(m1, tablero).get_Nombre()
                + " y Máquina 2 a " + secretoDe(m2, tablero).get_Nombre() + ".");
        jugar(m1, m2, true);
    }

    // Turnos alternados sin limite de preguntas: termina cuando alguien acierta.
    private Jugador jugar(Jugador primero, Jugador segundo, boolean pausa) {
        for (int turno = 1; ; turno++) {
            Jugador actual = turno % 2 == 1 ? primero : segundo;
            System.out.println("\n--- Turno " + turno + ": " + actual.getNombre() + " ---");
            if (actual.jugarTurno(actual == primero ? segundo : primero)) {
                System.out.println("¡Ganó " + actual.getNombre() + " en el turno " + turno + "!");
                return actual;
            }
            if (pausa) Consola.pausa();
        }
    }

    private List<Personajes> armarTablero(boolean mostrarOriginal) {
        List<Personajes> porGenero = GeneradorPersonajes.crear_Lista();
        if (mostrarOriginal) System.out.println("Personajes por género: " + porGenero.stream().map(Personajes::get_Nombre).toList());
        List<Personajes> tablero = JugadorMaquina.armarTablero(porGenero);
        System.out.println("Tablero ordenado por la máquina (MergeSort, ID autoincremental):");
        tablero.forEach(System.out::println);
        return tablero;
    }

    // El arbitro tambien usa solo el Oraculo: busca al personaje por el que el jugador dice que SI
    public static Personajes secretoDe(Oraculo jugador, List<Personajes> tablero) {
        return tablero.stream().filter(p -> jugador.responder(Pregunta.arriesgue(p))).findFirst().orElseThrow();
    }

    public static List<Personajes> sinElDe(Oraculo jugador, List<Personajes> tablero) {
        return tablero.stream().filter(p -> !jugador.responder(Pregunta.arriesgue(p))).toList();
    }
}
