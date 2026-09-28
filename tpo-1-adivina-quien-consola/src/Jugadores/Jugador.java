package Jugadores;

import Functional_Things.IndiceAtributos;
import Things.Personajes;
import Things.Pregunta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class Jugador implements Oraculo {

    // Una pregunta que hizo este jugador y lo que le respondieron
    public record Registro(Pregunta pregunta, boolean respuesta) {}

    private final String nombre;
    // Privado y SIN getter: el rival solo lo puede consultar con responder() (Oraculo)
    private Personajes personajeSecreto;
    protected final List<Personajes> tablero;
    protected final IndiceAtributos indice;
    // Tablero propio: los personajes que este jugador todavia no descarto
    protected final Set<Personajes> candidatos = new LinkedHashSet<>();
    private final List<Registro> historial = new ArrayList<>();

    protected Jugador(String nombre, List<Personajes> tablero, IndiceAtributos indice) {
        this.nombre = nombre;
        this.tablero = tablero;
        this.indice = indice;
        candidatos.addAll(tablero);
    }

    // Cada jugador elige su personaje a su manera
    protected abstract Personajes elegir(List<Personajes> disponibles);

    // Cada jugador decide que preguntar (o a quien arriesgar) a su manera
    protected abstract Pregunta decidir();

    // Se elige una sola vez: despues no se puede cambiar
    public void elegirSecreto(List<Personajes> disponibles) {
        if (personajeSecreto != null) throw new IllegalStateException("El personaje secreto no se puede cambiar");
        personajeSecreto = elegir(disponibles);
    }

    @Override
    public boolean responder(Pregunta pregunta) {
        return pregunta.aplicaA(personajeSecreto);
    }

    // Un turno: decide, el rival responde y se descarta. Devuelve true si adivino.
    public boolean jugarTurno(Oraculo rival) {
        Pregunta pregunta = decidir();
        boolean si = rival.responder(pregunta);
        aprender(pregunta, si);
        System.out.println(nombre + ": " + pregunta + " -> " + (si ? "SÍ" : "NO")
                + " (le quedan " + candidatos.size() + " candidatos)");
        return si && pregunta.esArriesgue();
    }

    // SI: se queda con los que cumplen. NO: descarta a los que cumplen. O(c)
    protected void aprender(Pregunta pregunta, boolean si) {
        historial.add(new Registro(pregunta, si));
        candidatos.removeIf(p -> pregunta.aplicaA(p) != si);
    }

    // Nivel nuevo con el mismo personaje secreto: se vuelve a mirar todo el tablero
    public void reiniciar() {
        candidatos.clear();
        candidatos.addAll(tablero);
        historial.clear();
    }

    public String getNombre() {
        return nombre;
    }

    public Set<Personajes> getCandidatos() {
        return Collections.unmodifiableSet(candidatos);
    }

    public List<Registro> getHistorial() {
        return Collections.unmodifiableList(historial);
    }
}
