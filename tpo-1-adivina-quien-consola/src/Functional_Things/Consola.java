package Functional_Things;

import java.util.Scanner;

// Unico lector de teclado. Lee siempre la linea entera: mezclar nextInt() con nextLine()
// dejaba un Enter colgado y se salteaba la pregunta siguiente.
public final class Consola {

    private static final Scanner teclado = new Scanner(System.in);

    private Consola() {
    }

    public static String leerTexto(String mensaje) {
        System.out.print(mensaje + " ");
        if (!teclado.hasNextLine()) {
            System.out.println("\n(se terminó la entrada)");
            System.exit(0);
        }
        return teclado.nextLine().trim();
    }

    public static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(leerTexto(mensaje));
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // cae al aviso
            }
            System.out.println("Ingresá un número entre " + min + " y " + max + ".");
        }
    }

    public static void pausa() {
        leerTexto("(Enter para seguir)");
    }
}
