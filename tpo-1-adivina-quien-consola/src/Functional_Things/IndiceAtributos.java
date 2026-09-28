package Functional_Things;

import Things.Personajes;
import Things.Pregunta;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// El diccionario grande: se arma UNA vez con el tablero y despues es de SOLO LECTURA.
// clave = una pregunta posible (categoria + valor, ej: pelo = negro)
// valor = todos los personajes que la cumplen.
// El descarte real pasa sobre los candidatos de cada Jugador, nunca sobre esto.
public class IndiceAtributos {

    private final Map<Pregunta, Set<Personajes>> indice = new TreeMap<>();

    public IndiceAtributos(List<Personajes> tablero) {
        for (Personajes p : tablero) {
            p.get_Atributos().forEach((categoria, valor) ->
                    indice.computeIfAbsent(new Pregunta(categoria, valor), k -> new HashSet<>()).add(p));
        }
    }

    // "Dame a todos los que tienen tal valor". Si no existe devuelve vacio, nunca null.
    public Set<Personajes> buscar(Pregunta pregunta) {
        return indice.getOrDefault(pregunta, Set.of());
    }

    public List<Pregunta> preguntasPosibles() {
        return new ArrayList<>(indice.keySet());
    }
}
