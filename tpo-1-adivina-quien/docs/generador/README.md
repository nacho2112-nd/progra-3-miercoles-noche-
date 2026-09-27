# Generador de la documentación

Estos scripts arman `../TPO-AdivinaQuien-Documentacion.docx` y su PDF. Hacen falta sólo si cambia el código o el contenido. Para corregir un texto suelto, conviene editar el `.docx` en Word.

> **Atención:** regenerar el `.docx` pisa cualquier edición hecha a mano en Word, incluidos los campos `[COMPLETAR]` ya completados. Si el equipo ya editó el Word, pasar esos cambios a `build.js` antes de regenerar.

## Qué genera cada script

| Script | Genera | Requisitos |
|---|---|---|
| `uml.py` | `../img/uml-*.png`, los diagramas de clases | Python 3 y Google Chrome |
| `snippets.py` | `../img/cod-*.png`, las capturas de código leídas de `src/` | Python 3, Pillow y Google Chrome |
| `adivinaquien.GeneradorCapturas` (en `test/`) | `../img/app/*.png`, las capturas de la aplicación | JDK 17 o superior |
| `build.js` | el `.docx`, con todo el texto del documento | Node.js y `npm install` en esta carpeta |
| `exportar.ps1` | actualiza el índice en Word y exporta el PDF | Windows con Microsoft Word |

## Orden de ejecución

Todos los comandos se corren desde esta carpeta (`docs/generador`).

1. Generar los diagramas y las capturas de código:

   ```
   python uml.py
   python snippets.py
   ```

2. Compilar el proyecto y generar las capturas de la aplicación. En la raíz del TPO, después de compilar `src` y `test` en `out`:

   ```
   java -Dsun.java2d.uiScale=2 -cp out adivinaquien.GeneradorCapturas docs/img/app
   ```

3. Armar el Word:

   ```
   npm install
   node build.js
   ```

4. Actualizar el índice y exportar el PDF (PowerShell, con rutas absolutas):

   ```
   powershell -ExecutionPolicy Bypass -File exportar.ps1 -docx "<ruta>\TPO-AdivinaQuien-Documentacion.docx" -pdf "<ruta>\TPO-AdivinaQuien-Documentacion.pdf"
   ```
