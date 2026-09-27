package adivinaquien.motor;

import adivinaquien.algoritmos.AlgoritmoOrdenamiento;
import adivinaquien.algoritmos.Benchmark;
import adivinaquien.algoritmos.BusquedaBinaria;
import adivinaquien.datos.CatalogoPersonajes;
import adivinaquien.datos.ValidadorCatalogo;
import adivinaquien.modelo.ColorPelo;
import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Genero;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pruebas del motor sin librerías externas. Se corren con:
 *   java -cp out adivinaquien.motor.PruebasAutomaticas
 * Está en el paquete motor para poder hacer jugar a la máquina sola, sin rival que le gane antes.
 * Termina con código 1 si alguna falla.
 */
public class PruebasAutomaticas {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        catalogoValido();
        validadorDetectaErrores();
        ordenamientosOrdenanIgual();
        tableroConIdsAutoincrementales();
        busquedaBinariaEncuentraATodos();
        dependenciaLogica();
        maquinaAdivinaATodosSinPreguntasInutiles();
        arbolCoincideConElJuego();
        partidasMaquinaVsMaquinaTerminan();
        partidaHumanoRespetaTurnos();
        secretoEncapsulado();

        System.out.println();
        System.out.println(ok + " pruebas OK, " + fallas + " fallas");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    private static void catalogoValido() {
        List<String> errores = ValidadorCatalogo.validar(CatalogoPersonajes.crear());
        verificar(errores.isEmpty(), "el catálogo cumple todas las precondiciones " + errores);
    }

    private static void validadorDetectaErrores() {
        List<Personaje> malo = CatalogoPersonajes.crear();
        malo.set(0, new Personaje("Sofía", Genero.MUJER, true, ColorPelo.NEGRO, true, true, false));
        List<String> errores = ValidadorCatalogo.validar(malo);
        verificar(errores.size() >= 2, "el validador detecta pelada con color y mujer con barba: " + errores.size() + " errores");
        try {
            OrganizadorTablero.organizar(malo, null);
            verificar(false, "un catálogo inválido no arma tablero");
        } catch (IllegalArgumentException esperado) {
            verificar(true, "un catálogo inválido no arma tablero");
        }
    }

    private static void ordenamientosOrdenanIgual() {
        for (int n : new int[]{0, 1, 2, 23, 500}) {
            List<Personaje> base = n == 23 ? CatalogoPersonajes.crear() : Benchmark.generarAleatorios(n, n);
            List<Personaje> esperado = new ArrayList<>(base);
            esperado.sort(Personaje.POR_NOMBRE);
            for (AlgoritmoOrdenamiento a : Benchmark.algoritmos()) {
                List<Personaje> copia = new ArrayList<>(base);
                a.ordenar(copia, Personaje.POR_NOMBRE);
                verificar(copia.equals(esperado), a.getNombre() + " ordena bien con n=" + n);
            }
        }
    }

    private static void tableroConIdsAutoincrementales() {
        Tablero t = OrganizadorTablero.organizar();
        boolean idsBien = true;
        boolean ordenBien = true;
        for (int i = 0; i < t.tamanio(); i++) {
            idsBien &= t.getPersonajes().get(i).getId() == i + 1;
            if (i > 0) {
                ordenBien &= Personaje.POR_NOMBRE.compare(t.getPersonajes().get(i - 1), t.getPersonajes().get(i)) <= 0;
            }
        }
        verificar(t.tamanio() == 23, "el tablero tiene 23 personajes");
        verificar(idsBien, "los IDs van de 1 a 23 en el orden en que se agregan");
        verificar(ordenBien, "el tablero queda ordenado por nombre");
        verificar(t.obtenerPorId(1).getNombre().equals("Ana"), "el ID 1 es Ana (primera alfabéticamente)");
        try {
            Tablero otro = new Tablero();
            otro.agregar(new Personaje("Zoe", Genero.MUJER, false, ColorPelo.NEGRO, false, false, false));
            otro.agregar(new Personaje("Ana", Genero.MUJER, false, ColorPelo.NEGRO, false, false, false));
            verificar(false, "el tablero rechaza un agregado fuera de orden");
        } catch (IllegalArgumentException esperado) {
            verificar(true, "el tablero rechaza un agregado fuera de orden");
        }
    }

    private static void busquedaBinariaEncuentraATodos() {
        Tablero t = OrganizadorTablero.organizar();
        boolean todos = true;
        for (Personaje p : t.getPersonajes()) {
            todos &= BusquedaBinaria.buscarPorNombre(t.getPersonajes(), p.getNombre()) == p.getId() - 1;
        }
        verificar(todos, "la búsqueda binaria encuentra a los 23");
        verificar(BusquedaBinaria.buscarPorNombre(t.getPersonajes(), "LUCIA") >= 0, "encuentra 'LUCIA' sin tilde ni mayúsculas");
        verificar(BusquedaBinaria.buscarPorNombre(t.getPersonajes(), "Pepe") == -1, "no encuentra a quien no está");
    }

    private static void dependenciaLogica() {
        BaseConocimiento bc = new BaseConocimiento();
        List<String> inf = bc.registrar(Filtro.ES_PELADO, true);
        verificar(inf.size() == 3 && Boolean.FALSE.equals(bc.valor(Filtro.PELO_NEGRO)), "pelado = SÍ resuelve los 3 colores como NO");

        bc = new BaseConocimiento();
        bc.registrar(Filtro.PELO_AMARILLO, true);
        verificar(Boolean.FALSE.equals(bc.valor(Filtro.ES_PELADO)) && Boolean.FALSE.equals(bc.valor(Filtro.PELO_COLORADO)),
                "pelo amarillo = SÍ resuelve pelado y los otros colores como NO");

        bc = new BaseConocimiento();
        bc.registrar(Filtro.ES_PELADO, false);
        bc.registrar(Filtro.PELO_NEGRO, false);
        bc.registrar(Filtro.PELO_COLORADO, false);
        verificar(Boolean.TRUE.equals(bc.valor(Filtro.PELO_AMARILLO)), "sin pelado, ni negro, ni colorado, queda amarillo");

        bc = new BaseConocimiento();
        bc.registrar(Filtro.ES_MUJER, true);
        verificar(Boolean.FALSE.equals(bc.valor(Filtro.TIENE_BARBA)), "mujer = SÍ resuelve barba como NO");

        bc = new BaseConocimiento();
        bc.registrar(Filtro.TIENE_BARBA, true);
        verificar(Boolean.FALSE.equals(bc.valor(Filtro.ES_MUJER)), "barba = SÍ resuelve mujer como NO");

        try {
            bc.registrar(Filtro.ES_MUJER, true);
            verificar(false, "detecta respuestas contradictorias");
        } catch (IllegalStateException esperado) {
            verificar(true, "detecta respuestas contradictorias");
        }
    }

    /** Juega la máquina sola contra cada uno de los 23 secretos, sin que el rival le gane antes. */
    private static void maquinaAdivinaATodosSinPreguntasInutiles() {
        boolean siempreGana = true;
        boolean nuncaInutil = true;
        boolean nuncaResuelto = true;
        boolean coincideConArbol = true;
        int maxTurnos = 0;
        for (int id = 1; id <= 23; id++) {
            Tablero t = OrganizadorTablero.organizar();
            ArbolDecision arbol = ArbolDecision.construir(t.getPersonajes());
            JugadorMaquina maquina = new JugadorMaquina("Máquina", t, t.obtenerPorId(1), new Random(id));
            Oraculo rival = new JugadorHumano("Humano", t, t.obtenerPorId(id));
            int turnos = 0;
            boolean gano = false;
            int preguntas = 0;
            while (!gano && turnos < 50) {
                ResultadoTurno r = maquina.jugarTurno(turnos + 1, rival);
                turnos++;
                gano = r.esAcierto();
                if (r.getAccion() == ResultadoTurno.Accion.PREGUNTA) {
                    preguntas++;
                    nuncaInutil &= !r.getDescartados().isEmpty();
                    nuncaResuelto &= r.getEvaluaciones().stream().anyMatch(e -> e.getFiltro() == r.getFiltro() && e.esUtil());
                }
            }
            siempreGana &= gano;
            // Las preguntas son las del camino del árbol; sólo el orden del arriesgue final es al azar.
            coincideConArbol &= preguntas == arbol.turnosPara(t.obtenerPorId(id)) - posicionEnHoja(arbol, t.obtenerPorId(id));
            maxTurnos = Math.max(maxTurnos, turnos);
        }
        verificar(siempreGana, "la máquina adivina a los 23 secretos");
        verificar(nuncaInutil, "cada pregunta de la máquina descarta al menos un candidato");
        verificar(nuncaResuelto, "la máquina sólo pregunta filtros que separan y no están resueltos");
        verificar(coincideConArbol, "la máquina hace exactamente las preguntas del árbol de decisión");
        verificar(maxTurnos <= 7, "la máquina necesita como mucho 7 turnos (máximo observado: " + maxTurnos + ")");
    }

    private static int posicionEnHoja(ArbolDecision arbol, Personaje secreto) {
        NodoDecision nodo = arbol.getRaiz();
        while (!nodo.esHoja()) {
            nodo = nodo.getFiltro().evaluar(secreto) ? nodo.getSi() : nodo.getNo();
        }
        return nodo.getCandidatos().indexOf(secreto) + 1;
    }

    private static void arbolCoincideConElJuego() {
        Tablero t = OrganizadorTablero.organizar();
        ArbolDecision arbol = ArbolDecision.construir(t.getPersonajes());
        verificar(arbol.getRaiz().getFiltro() == Filtro.ES_MUJER, "la primera pregunta del árbol es ¿Es mujer? (11 contra 12)");
        verificar(arbol.turnosMaximos() <= 7, "el árbol gana en " + arbol.turnosMaximos() + " turnos como máximo");
        System.out.printf("   árbol: altura %d preguntas, %d turnos máx., %.2f turnos promedio%n",
                arbol.profundidadMaxima(), arbol.turnosMaximos(), arbol.turnosPromedio());
    }

    private static void partidasMaquinaVsMaquinaTerminan() {
        int ganaA = 0;
        int ganaB = 0;
        for (int semilla = 0; semilla < 200; semilla++) {
            Tablero t = OrganizadorTablero.organizar();
            Random azar = new Random(semilla);
            JugadorMaquina a = new JugadorMaquina("Máquina A", t, azar);
            JugadorMaquina b = new JugadorMaquina("Máquina B", t, azar);
            Partida p = new Partida(t, a, b);
            int turnos = 0;
            while (!p.terminada() && turnos < 100) {
                p.jugarTurnoMaquina();
                turnos++;
            }
            if (p.getGanador() == a) ganaA++;
            if (p.getGanador() == b) ganaB++;
        }
        verificar(ganaA + ganaB == 200, "200 partidas Máquina vs Máquina terminan todas (A: " + ganaA + ", B: " + ganaB + ")");
    }

    private static void partidaHumanoRespetaTurnos() {
        Tablero t = OrganizadorTablero.organizar();
        JugadorHumano humano = new JugadorHumano("Vos", t, t.obtenerPorId(5));
        JugadorMaquina maquina = new JugadorMaquina("Máquina", t, t.obtenerPorId(10), new Random(1));
        Partida p = new Partida(t, humano, maquina);
        try {
            p.jugarTurnoMaquina();
            verificar(false, "la máquina no puede jugar en el turno del humano");
        } catch (IllegalStateException esperado) {
            verificar(true, "la máquina no puede jugar en el turno del humano");
        }
        p.preguntar(Filtro.ES_MUJER);
        try {
            p.preguntar(Filtro.USA_LENTES);
            verificar(false, "el humano no puede jugar dos turnos seguidos");
        } catch (IllegalStateException esperado) {
            verificar(true, "el humano no puede jugar dos turnos seguidos");
        }
        p.jugarTurnoMaquina();
        try {
            p.preguntar(Filtro.ES_MUJER);
            verificar(false, "no se puede repetir una pregunta ya resuelta");
        } catch (IllegalArgumentException esperado) {
            verificar(true, "no se puede repetir una pregunta ya resuelta");
        }
        try {
            p.revelarSecreto(maquina);
            verificar(false, "el secreto no se revela antes del final");
        } catch (IllegalStateException esperado) {
            verificar(true, "el secreto no se revela antes del final");
        }
        p.arriesgar(t.obtenerPorId(10));
        verificar(p.terminada() && p.getGanador() == humano, "arriesgar el personaje correcto gana");
        verificar(p.revelarSecreto(maquina) == t.obtenerPorId(10), "al terminar se revela el secreto");
    }

    /** Reflexión: la máquina no guarda al rival ni tiene cómo leer su secreto. */
    private static void secretoEncapsulado() {
        boolean sinRival = true;
        for (Field f : JugadorMaquina.class.getDeclaredFields()) {
            Class<?> tipo = f.getType();
            sinRival &= !Oraculo.class.isAssignableFrom(tipo) && tipo != Personaje.class;
        }
        verificar(sinRival, "JugadorMaquina no tiene campos de tipo Oraculo, Jugador ni Personaje");
        boolean oraculoCerrado = true;
        for (Method m : Oraculo.class.getMethods()) {
            oraculoCerrado &= m.getReturnType() == boolean.class;
        }
        verificar(oraculoCerrado, "la interfaz Oraculo sólo devuelve SÍ/NO, nunca un Personaje");
        try {
            Field secreto = Jugador.class.getDeclaredField("secreto");
            verificar(java.lang.reflect.Modifier.isPrivate(secreto.getModifiers()), "el secreto es un campo private de Jugador");
        } catch (NoSuchFieldException e) {
            verificar(false, "el secreto es un campo private de Jugador");
        }
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }
}
