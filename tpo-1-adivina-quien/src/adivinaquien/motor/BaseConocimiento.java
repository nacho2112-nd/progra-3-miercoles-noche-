package adivinaquien.motor;

import adivinaquien.modelo.Filtro;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lo que un jugador ya sabe del personaje secreto del rival: cada filtro resuelto con su valor.
 *
 * Además de guardar las respuestas, aplica la dependencia lógica entre filtros:
 * - Exactamente uno de {pelado, colorado, negro, amarillo} es verdadero (pelado => sin color).
 * - Mujer => sin barba (y su contrarrecíproco: barba => no es mujer).
 * Así, una sola respuesta puede resolver varios filtros, y esos ya no se vuelven a preguntar.
 */
public class BaseConocimiento {

    /** Grupo exclusivo: de estos cuatro, uno y sólo uno es verdadero para cada personaje. */
    private static final Set<Filtro> PELO = EnumSet.of(
            Filtro.ES_PELADO, Filtro.PELO_COLORADO, Filtro.PELO_NEGRO, Filtro.PELO_AMARILLO);

    private final EnumMap<Filtro, Boolean> hechos = new EnumMap<>(Filtro.class);

    public boolean estaResuelto(Filtro filtro) {
        return hechos.containsKey(filtro);
    }

    public Boolean valor(Filtro filtro) {
        return hechos.get(filtro);
    }

    public Map<Filtro, Boolean> getHechos() {
        return new EnumMap<>(hechos);
    }

    /**
     * Registra una respuesta y propaga las reglas hasta que no se deduzca nada más.
     * Devuelve las inferencias nuevas (sin contar la respuesta misma). Cada regla es O(1) y hay
     * a lo sumo 8 filtros, así que la propagación es O(1).
     */
    public List<String> registrar(Filtro filtro, boolean valor) {
        fijar(filtro, valor);
        List<String> inferencias = new ArrayList<>();
        boolean cambio = true;
        while (cambio) {
            cambio = aplicarReglas(inferencias);
        }
        return inferencias;
    }

    private boolean aplicarReglas(List<String> inferencias) {
        boolean cambio = false;

        // Regla 1: grupo exclusivo del pelo.
        Filtro verdadero = null;
        int falsos = 0;
        Filtro pendiente = null;
        for (Filtro f : PELO) {
            Boolean v = hechos.get(f);
            if (Boolean.TRUE.equals(v)) {
                verdadero = f;
            } else if (Boolean.FALSE.equals(v)) {
                falsos++;
            } else {
                pendiente = f;
            }
        }
        if (verdadero != null) {
            for (Filtro f : PELO) {
                if (!hechos.containsKey(f)) {
                    cambio |= inferir(f, false, "porque " + verdadero.getPregunta() + " es SÍ", inferencias);
                }
            }
        } else if (falsos == PELO.size() - 1 && pendiente != null) {
            cambio |= inferir(pendiente, true, "porque las otras tres opciones de pelo son NO", inferencias);
        }

        // Regla 2: mujer => sin barba; barba => no es mujer.
        if (Boolean.TRUE.equals(hechos.get(Filtro.ES_MUJER)) && !hechos.containsKey(Filtro.TIENE_BARBA)) {
            cambio |= inferir(Filtro.TIENE_BARBA, false, "porque es mujer", inferencias);
        }
        if (Boolean.TRUE.equals(hechos.get(Filtro.TIENE_BARBA)) && !hechos.containsKey(Filtro.ES_MUJER)) {
            cambio |= inferir(Filtro.ES_MUJER, false, "porque tiene barba", inferencias);
        }
        return cambio;
    }

    private boolean inferir(Filtro filtro, boolean valor, String motivo, List<String> inferencias) {
        if (!fijar(filtro, valor)) {
            return false;
        }
        inferencias.add(filtro.getPregunta() + " = " + (valor ? "SÍ" : "NO") + " (" + motivo + ")");
        return true;
    }

    /** Devuelve true si el hecho es nuevo. Validación: dos respuestas distintas sobre lo mismo son un error. */
    private boolean fijar(Filtro filtro, boolean valor) {
        Boolean anterior = hechos.get(filtro);
        if (anterior != null) {
            if (anterior != valor) {
                throw new IllegalStateException("Respuestas contradictorias sobre " + filtro.getPregunta());
            }
            return false;
        }
        hechos.put(filtro, valor);
        return true;
    }

    public BaseConocimiento copia() {
        BaseConocimiento otra = new BaseConocimiento();
        otra.hechos.putAll(hechos);
        return otra;
    }
}
