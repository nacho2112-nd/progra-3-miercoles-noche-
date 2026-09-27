# Genera capturas de fragmentos de código reales del proyecto, con números de línea y resaltado.
import html, os, re, subprocess, sys, json

RAIZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'adivinaquien')
CHROME = r'C:\Program Files\Google\Chrome\Application\chrome.exe'

KW = set('''abstract boolean break case class continue default do double else enum extends final for if implements
import int interface long new null package private protected public return static super switch this throw throws
try catch void while true false var'''.split())

def extraer(archivo, marcador, metodos=1, con_doc=True):
    lineas = open(os.path.join(RAIZ, archivo), encoding='utf-8').read().split('\n')
    i = next(k for k, l in enumerate(lineas) if marcador in l)
    ini = i
    if con_doc:
        j = i - 1
        while j >= 0 and lineas[j].strip().startswith(('/**', '*', '*/', '//', '@')):
            j -= 1
        ini = j + 1
    k = i
    for _ in range(metodos):
        prof = 0; empezo = False
        while True:
            prof += lineas[k].count('{') - lineas[k].count('}')
            if '{' in lineas[k]: empezo = True
            if empezo and prof == 0: break
            k += 1
        k += 1
        # saltar líneas en blanco y javadoc hasta el próximo método
        if _ < metodos - 1:
            while lineas[k].strip() == '' or lineas[k].strip().startswith(('/**', '*', '*/', '@')):
                k += 1
    fin = k
    bloque = lineas[ini:fin]
    sangria = min(len(l) - len(l.lstrip()) for l in bloque if l.strip())
    return ini + 1, [l[sangria:] for l in bloque]

def resaltar(linea, en_comentario):
    out = []; i = 0; n = len(linea)
    while i < n:
        if en_comentario:
            j = linea.find('*/', i)
            fin = n if j < 0 else j + 2
            out.append(f'<span class="c">{html.escape(linea[i:fin])}</span>')
            en_comentario = j < 0; i = fin; continue
        if linea.startswith('//', i):
            out.append(f'<span class="c">{html.escape(linea[i:])}</span>'); break
        if linea.startswith('/*', i):
            en_comentario = True; continue
        ch = linea[i]
        if ch == '"':
            j = i + 1
            while j < n and linea[j] != '"':
                j += 2 if linea[j] == '\\' else 1
            out.append(f'<span class="s">{html.escape(linea[i:j + 1])}</span>'); i = j + 1; continue
        if ch.isalpha() or ch == '_':
            j = i
            while j < n and (linea[j].isalnum() or linea[j] == '_'): j += 1
            w = linea[i:j]
            if w in KW: out.append(f'<span class="k">{w}</span>')
            elif w[0].isupper(): out.append(f'<span class="t">{w}</span>')
            else: out.append(html.escape(w))
            i = j; continue
        if ch.isdigit():
            j = i
            while j < n and (linea[j].isalnum() or linea[j] in '._'): j += 1
            out.append(f'<span class="n">{linea[i:j]}</span>'); i = j; continue
        out.append(html.escape(ch)); i += 1
    return ''.join(out), en_comentario

def render(nombre, archivo, titulo, big_o, primera, lineas, salida):
    filas = []; en_c = False
    for k, l in enumerate(lineas):
        h, en_c = resaltar(l, en_c)
        filas.append(f'<tr><td class="ln">{primera + k}</td><td class="code">{h or "&nbsp;"}</td></tr>')
    alto = 80 + 26 * len(lineas) + 60
    doc = f'''<!doctype html><html><head><meta charset="utf-8"><style>
body{{margin:0;background:#fff;font-family:"Segoe UI",sans-serif}}
.win{{width:980px;border:1px solid #C9CFDA;border-radius:8px;overflow:hidden;margin:0}}
.bar{{display:flex;justify-content:space-between;align-items:center;background:#EEF1F6;border-bottom:1px solid #D9DDE5;padding:9px 14px;height:22px}}
.f{{font:600 13.5px "Segoe UI";color:#131720}} .f span{{font-weight:400;color:#5B6475}}
.o{{font:600 13px Consolas,monospace;color:#fff;background:#1E3A8F;border-radius:5px;padding:3px 9px}}
table{{border-collapse:collapse;margin:6px 0 8px 0;font:13.2px/19px Consolas,monospace}}
.ln{{color:#9AA1AE;text-align:right;padding:0 12px 0 12px;user-select:none;width:34px}}
.code{{white-space:pre;color:#131720;padding-right:14px}}
.k{{color:#1E3A8F;font-weight:bold}} .t{{color:#0B6B74}} .s{{color:#A9541F}} .c{{color:#6B7280;font-style:italic}} .n{{color:#7C3AED}}
</style></head><body><div class="win"><div class="bar"><div class="f">{html.escape(archivo)} <span>· {html.escape(titulo)}</span></div>
<div class="o">{html.escape(big_o)}</div></div><table>{"".join(filas)}</table></div></body></html>'''
    f = os.path.join(salida, nombre + '.html')
    open(f, 'w', encoding='utf-8').write(doc)
    png = os.path.join(salida, nombre + '.png')
    subprocess.run([CHROME, '--headless=new', '--disable-gpu', '--hide-scrollbars', f'--screenshot={png}',
                    f'--window-size=982,{alto}', '--force-device-scale-factor=2', 'file:///' + f.replace('\\', '/')],
                   check=True, capture_output=True)
    from PIL import Image, ImageChops
    im = Image.open(png).convert('RGB')
    fondo = Image.new('RGB', im.size, (255, 255, 255))
    caja = ImageChops.difference(im, fondo).getbbox()
    im.crop((0, 0, im.width, caja[3] + 2)).save(png)
    return png, alto

FRAGMENTOS = [
    ('cod-mergesort', 'algoritmos/MergeSort.java', '    private <T> void ordenarRango(', 2, 'MergeSort: dividir, conquistar y combinar', 'Θ(n log n)'),
    ('cod-tablero', 'modelo/Tablero.java', '    public void agregar(Personaje p)', 1, 'ID autoincremental al agregar', 'O(1)'),
    ('cod-organizador', 'motor/OrganizadorTablero.java', '    public static Tablero organizar(List<Personaje> catalogo', 1, 'La máquina arma el tablero', 'Θ(n log n)'),
    ('cod-binaria', 'algoritmos/BusquedaBinaria.java', '    private static int buscar(', 1, 'Búsqueda binaria recursiva del arriesgue', 'O(log n)'),
    ('cod-greedy', 'motor/EstrategiaGreedy.java', '    public List<EvaluacionFiltro> evaluar(', 3, 'Función de evaluación y de selección', 'O(f · c)'),
    ('cod-maquina', 'motor/JugadorMaquina.java', '    ResultadoTurno jugarTurno(', 1, 'Decisión de la máquina en cada turno', 'O(f · c)'),
    ('cod-descarte', 'motor/Jugador.java', '    ResultadoTurno preguntar(', 1, 'Descarte de candidatos', 'O(c)'),
    ('cod-dependencia', 'motor/BaseConocimiento.java', '    private boolean aplicarReglas(', 1, 'Dependencia lógica entre filtros', 'O(1)'),
    ('cod-arbol', 'motor/ArbolDecision.java', '    private static NodoDecision construirNodo(', 1, 'Árbol de decisión por Divide y Conquista', 'O(f · n · h)'),
    ('cod-oraculo', 'motor/Jugador.java', '    public final boolean responder(', 2, 'El secreto sólo se consulta, nunca se lee', 'O(1)'),
    ('cod-validador', 'datos/ValidadorCatalogo.java', '    public static List<String> validar(', 1, 'Precondiciones del catálogo', 'O(n)'),
]

if __name__ == '__main__':
    salida = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'img')
    info = {}
    for nombre, archivo, marcador, metodos, titulo, big_o in FRAGMENTOS:
        primera, lineas = extraer(archivo, marcador, metodos)
        png, alto = render(nombre, archivo.split('/')[-1], titulo, big_o, primera, lineas, salida)
        info[nombre] = {'archivo': archivo, 'titulo': titulo, 'bigO': big_o, 'lineas': len(lineas), 'alto': alto, 'desde': primera}
        print(nombre, primera, len(lineas))
    json.dump(info, open(os.path.join(salida, 'snippets.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
