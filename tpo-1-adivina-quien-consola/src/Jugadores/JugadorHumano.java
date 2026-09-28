package Jugadores;

import Functional_Things.Consola;
import Functional_Things.IndiceAtributos;
import Things.Personajes;
import Things.Pregunta;
import java.util.List;

public class JugadorHumano extends Jugador {

    public JugadorHumano(String nombre, List<Personajes> tablero, IndiceAtributos indice) {
        super(nombre, tablero, indice);
    }

    // Si viene uno solo (clic en la ventana) es ese. Si no, se pide por consola:
    // el ID es la posicion en el tablero + 1 (autoincremental), asi que se busca directo
    @Override
    protected Personajes elegir(List<Personajes> disponibles) {
        if (disponibles.size() == 1) return disponibles.get(0);
        return tablero.get(Consola.leerEntero("Elegí tu personaje secreto (id). No lo vas a poder cambiar:", 1, tablero.size()) - 1);
    }

    @Override
    protected Pregunta decidir() {
        List<Pregunta> preguntas = indice.preguntasPosibles();
        for (int i = 0; i < preguntas.size(); i++) System.out.println((i + 1) + " - " + preguntas.get(i));
        System.out.println("0 - Arriesgar   -1 - Ver mis candidatos (" + candidatos.size() + ")");
        int opcion;
        while ((opcion = Consola.leerEntero("¿Qué hacés?", -1, preguntas.size())) == -1) {
            candidatos.forEach(System.out::println);
        }
        if (opcion > 0) return preguntas.get(opcion - 1);
        return Pregunta.arriesgue(tablero.get(Consola.leerEntero("Id del personaje:", 1, tablero.size()) - 1));
    }
}
