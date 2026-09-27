# Genera los diagramas UML como SVG (dentro de un HTML) para capturarlos con Chrome headless.
import html, math, subprocess, os, sys

CHAR_T = 8.4   # ancho aprox. por carácter del título (Segoe UI Semibold 14px)
CHAR_M = 7.0   # ancho aprox. por carácter de miembros (Consolas 12.5px)
LINE = 17
PAD = 8

INK = '#131720'; SOFT = '#5B6475'; ACC = '#1E3A8F'; BORDER = '#9AA3B5'; HEAD = '#E3E9F7'

class Box:
    def __init__(self, key, x, y, title, stereo=None, attrs=(), methods=(), w=None, head=HEAD):
        self.key, self.x, self.y, self.title, self.stereo = key, x, y, title, stereo
        self.attrs, self.methods, self.head = list(attrs), list(methods), head
        wt = len(title) * CHAR_T + 2 * PAD + 10
        ws = len(stereo or '') * 7 + 2 * PAD + 10
        wm = max([len(s) * CHAR_M for s in self.attrs + self.methods] + [0]) + 2 * PAD + 4
        self.w = w or max(wt, ws, wm, 120)
        self.hh = (22 if stereo else 0) + 26
        self.ha = max(len(self.attrs), 0) * LINE + (10 if self.attrs else 6)
        self.hm = len(self.methods) * LINE + (10 if self.methods else 6)
        self.h = self.hh + self.ha + self.hm

    def cx(self): return self.x + self.w / 2
    def cy(self): return self.y + self.h / 2

    def svg(self):
        o = []
        o.append(f'<rect x="{self.x}" y="{self.y}" width="{self.w}" height="{self.h}" fill="#fff" stroke="{BORDER}" stroke-width="1.3"/>')
        o.append(f'<rect x="{self.x}" y="{self.y}" width="{self.w}" height="{self.hh}" fill="{self.head}" stroke="{BORDER}" stroke-width="1.3"/>')
        ty = self.y + 20
        if self.stereo:
            o.append(f'<text x="{self.cx()}" y="{ty}" class="st">{html.escape(self.stereo)}</text>')
            ty += 20
        style = ' font-style="italic"' if self.stereo == '«abstract»' else ''
        o.append(f'<text x="{self.cx()}" y="{ty}" class="t"{style}>{html.escape(self.title)}</text>')
        y = self.y + self.hh
        o.append(f'<line x1="{self.x}" y1="{y + self.ha}" x2="{self.x + self.w}" y2="{y + self.ha}" stroke="{BORDER}"/>')
        yy = y + 16
        for a in self.attrs:
            o.append(f'<text x="{self.x + PAD}" y="{yy}" class="m">{html.escape(a)}</text>'); yy += LINE
        yy = y + self.ha + 16
        for m in self.methods:
            o.append(f'<text x="{self.x + PAD}" y="{yy}" class="m">{html.escape(m)}</text>'); yy += LINE
        return '\n'.join(o)

def clip(box, px, py, qx, qy):
    """Punto donde el segmento desde el centro de box hacia (qx,qy) corta el borde."""
    cx, cy = px, py
    dx, dy = qx - cx, qy - cy
    if dx == 0 and dy == 0: return cx, cy
    hw, hh = box.w / 2, box.h / 2
    tx = hw / abs(dx) if dx else 1e9
    ty = hh / abs(dy) if dy else 1e9
    t = min(tx, ty)
    return cx + dx * t, cy + dy * t

def edge(boxes, a, b, kind, label=None, mult_b=None, mult_a=None, via=(), sa=None, sb=None, lpos=0.5, loff=(0, -6)):
    A, B = boxes[a], boxes[b]
    ax, ay = sa if sa else (A.cx(), A.cy())
    bx, by = sb if sb else (B.cx(), B.cy())
    pts = [(ax, ay)] + list(via) + [(bx, by)]
    # recortar extremos contra los bordes
    p0 = clip(A, ax, ay, *pts[1]) if not sa else (ax, ay)
    pn = clip(B, bx, by, *pts[-2]) if not sb else (bx, by)
    pts[0], pts[-1] = p0, pn
    dash = ' stroke-dasharray="7 5"' if kind in ('dep', 'real') else ''
    d = 'M ' + ' L '.join(f'{x:.1f} {y:.1f}' for x, y in pts)
    o = [f'<path d="{d}" fill="none" stroke="{INK}" stroke-width="1.4"{dash}/>']
    # punta en B
    (x1, y1), (x2, y2) = pts[-2], pts[-1]
    ang = math.atan2(y2 - y1, x2 - x1)
    def rot(px, py):
        return (x2 + px * math.cos(ang) - py * math.sin(ang), y2 + px * math.sin(ang) + py * math.cos(ang))
    if kind in ('inh', 'real'):
        p = [rot(0, 0), rot(-14, -8), rot(-14, 8)]
        o.append('<polygon points="' + ' '.join(f'{x:.1f},{y:.1f}' for x, y in p) + '" fill="#fff" stroke="' + INK + '" stroke-width="1.4"/>')
    else:
        p = [rot(-12, -6), rot(0, 0), rot(-12, 6)]
        o.append('<polyline points="' + ' '.join(f'{x:.1f},{y:.1f}' for x, y in p) + f'" fill="none" stroke="{INK}" stroke-width="1.4"/>')
    # rombo en A
    if kind in ('comp', 'agg'):
        (x1, y1), (x2, y2) = pts[1], pts[0]
        ang2 = math.atan2(y2 - y1, x2 - x1)
        def rot2(px, py):
            return (x2 + px * math.cos(ang2) - py * math.sin(ang2), y2 + px * math.sin(ang2) + py * math.cos(ang2))
        p = [rot2(0, 0), rot2(-10, -6), rot2(-20, 0), rot2(-10, 6)]
        fill = INK if kind == 'comp' else '#fff'
        o.append('<polygon points="' + ' '.join(f'{x:.1f},{y:.1f}' for x, y in p) + f'" fill="{fill}" stroke="{INK}" stroke-width="1.3"/>')
    if label:
        # etiqueta sobre el segmento más largo
        segs = list(zip(pts, pts[1:]))
        (sx1, sy1), (sx2, sy2) = max(segs, key=lambda s: math.dist(*s))
        lx, ly = sx1 + (sx2 - sx1) * lpos + loff[0], sy1 + (sy2 - sy1) * lpos + loff[1]
        o.append(f'<text x="{lx:.1f}" y="{ly:.1f}" class="lbl">{html.escape(label)}</text>')
    if mult_b:
        (x1, y1), (x2, y2) = pts[-2], pts[-1]
        L = math.dist((x1, y1), (x2, y2)) or 1
        ux, uy = (x2 - x1) / L, (y2 - y1) / L
        o.append(f'<text x="{x2 - ux * 26 - uy * 12:.1f}" y="{y2 - uy * 26 + ux * 12 + 4:.1f}" class="mul">{html.escape(mult_b)}</text>')
    if mult_a:
        (x1, y1), (x2, y2) = pts[1], pts[0]
        L = math.dist((x1, y1), (x2, y2)) or 1
        ux, uy = (x2 - x1) / L, (y2 - y1) / L
        o.append(f'<text x="{x2 - ux * 30 - uy * 12:.1f}" y="{y2 - uy * 30 + ux * 12 + 4:.1f}" class="mul">{html.escape(mult_a)}</text>')
    return '\n'.join(o)

def page(W, H, title, body):
    return f'''<!doctype html><html><head><meta charset="utf-8"><style>
body{{margin:0;background:#fff}}
.t{{font:600 14.5px "Segoe UI",sans-serif;fill:{INK};text-anchor:middle}}
.st{{font:12px "Segoe UI",sans-serif;fill:{SOFT};text-anchor:middle}}
.m{{font:12.5px Consolas,monospace;fill:{INK}}}
.lbl{{font:italic 12px "Segoe UI",sans-serif;fill:{ACC};text-anchor:middle}}
.mul{{font:12px Consolas,monospace;fill:{INK};text-anchor:middle}}
.pk{{font:600 13px "Segoe UI",sans-serif;fill:{SOFT}}}
.cap{{font:600 18px "Segoe UI",sans-serif;fill:{INK}}}
</style></head><body><svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<text x="20" y="30" class="cap">{html.escape(title)}</text>
{body}</svg></body></html>'''

def paquete(x, y, w, h, nombre):
    return (f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" fill="#F6F7FA" stroke="#C9CFDA" stroke-dasharray="4 3"/>'
            f'<text x="{x + 10}" y="{y + 18}" class="pk">{html.escape(nombre)}</text>')

def render(nombre, W, H, svg_html, salida):
    f = os.path.join(salida, nombre + '.html')
    open(f, 'w', encoding='utf-8').write(svg_html)
    png = os.path.join(salida, nombre + '.png')
    chrome = r'C:\Program Files\Google\Chrome\Application\chrome.exe'
    url = 'file:///' + f.replace('\\', '/')
    subprocess.run([chrome, '--headless=new', '--disable-gpu', '--hide-scrollbars', f'--screenshot={png}',
                    f'--window-size={W},{H}', '--force-device-scale-factor=2', url],
                   check=True, capture_output=True)
    print('ok', png)

# ---------------------------------------------------------------- Diagrama 1: motor del juego
def motor(salida):
    W, H = 1640, 1060
    B = {}
    def add(b): B[b.key] = b
    add(Box('Partida', 30, 70, 'Partida', attrs=['- jugador1, jugador2: Jugador', '- turno: Jugador', '- ganador: Jugador',
        '- historial: List<ResultadoTurno>'], methods=['+ jugarTurnoMaquina(): ResultadoTurno', '+ preguntar(f: Filtro): ResultadoTurno',
        '+ arriesgar(p: Personaje): ResultadoTurno', '+ revelarSecreto(j: Jugador): Personaje', '- rival(): Oraculo']))
    add(Box('Oraculo', 560, 60, 'Oraculo', '«interface»', methods=['+ responder(f: Filtro): boolean', '+ esTuPersonaje(p: Personaje): boolean']))
    add(Box('Jugador', 520, 270, 'Jugador', '«abstract»', attrs=['- nombre: String', '- secreto: Personaje', '- candidatos: Set<Personaje>',
        '- conocimiento: BaseConocimiento'], methods=['~ preguntar(n, f, rival: Oraculo, …): ResultadoTurno',
        '~ arriesgar(n, p, rival: Oraculo, …): ResultadoTurno', '~ revelarSecreto(): Personaje', '+ getCandidatos(): Set<Personaje>',
        '+ estaResuelto(f: Filtro): boolean']))
    add(Box('Humano', 420, 610, 'JugadorHumano', methods=['+ JugadorHumano(nombre, tablero, secreto)']))
    add(Box('Maquina', 800, 610, 'JugadorMaquina', attrs=['- estrategia: EstrategiaGreedy', '- azar: Random'],
        methods=['~ jugarTurno(n, rival: Oraculo): ResultadoTurno']))
    add(Box('Resultado', 30, 420, 'ResultadoTurno', attrs=['- accion: Accion', '- filtro: Filtro', '- respuesta: boolean',
        '- evaluaciones: List<EvaluacionFiltro>', '- inferencias: List<String>', '- descartados: List<Personaje>'],
        methods=['+ esAcierto(): boolean', '+ describir(detallado): String']))
    add(Box('Base', 30, 790, 'BaseConocimiento', attrs=['- hechos: EnumMap<Filtro, Boolean>'],
        methods=['+ registrar(f, valor): List<String>', '+ estaResuelto(f): boolean', '+ copia(): BaseConocimiento']))
    add(Box('Greedy', 560, 830, 'EstrategiaGreedy', attrs=['+ UMBRAL_ARRIESGUE: int = 2'],
        methods=['+ evaluar(cands, conocimiento): List<EvaluacionFiltro>', '+ elegir(evals): EvaluacionFiltro', '+ convieneArriesgar(n): boolean']))
    add(Box('Eval', 1010, 870, 'EvaluacionFiltro', attrs=['- filtro: Filtro', '- si: int', '- no: int'],
        methods=['+ getDescarteGarantizado(): int']))
    add(Box('Tablero', 1260, 60, 'Tablero', attrs=['- personajes: List<Personaje>', '- contadorId: int'],
        methods=['+ agregar(p: Personaje)', '+ obtenerPorId(id): Personaje']))
    add(Box('Personaje', 1260, 300, 'Personaje', attrs=['- id: int', '- nombre: String', '- genero: Genero', '- pelado: boolean',
        '- colorPelo: ColorPelo', '- lentes, barba, sombrero: boolean'], methods=['+ perfil(): int']))
    add(Box('Filtro', 1260, 610, 'Filtro', '«enumeration»', attrs=['ES_MUJER, ES_PELADO, USA_LENTES,', 'PELO_COLORADO, PELO_NEGRO,',
        'PELO_AMARILLO, TIENE_BARBA,', 'USA_SOMBRERO'], methods=['+ evaluar(p: Personaje): boolean']))
    add(Box('Arbol', 1260, 870, 'ArbolDecision', attrs=['- raiz: NodoDecision'], methods=['+ construir(cands): ArbolDecision', '+ turnosPara(p): int']))

    e = []
    e.append(edge(B, 'Jugador', 'Oraculo', 'real'))
    e.append(edge(B, 'Humano', 'Jugador', 'inh', sa=(B['Humano'].cx(), B['Humano'].y), sb=(B['Humano'].cx(), B['Jugador'].y + B['Jugador'].h)))
    e.append(edge(B, 'Maquina', 'Jugador', 'inh', sa=(B['Maquina'].cx() - 40, B['Maquina'].y), sb=(B['Maquina'].cx() - 40, B['Jugador'].y + B['Jugador'].h)))
    e.append(edge(B, 'Partida', 'Jugador', 'assoc', label='juegan', mult_b='2', sa=(B['Partida'].x + B['Partida'].w, 330), sb=(B['Jugador'].x, 330)))
    e.append(edge(B, 'Partida', 'Resultado', 'comp', label='historial', mult_b='*', lpos=0.5, loff=(40, 4),
                  sa=(150, B['Partida'].y + B['Partida'].h), sb=(150, B['Resultado'].y)))
    e.append(edge(B, 'Partida', 'Oraculo', 'dep', label='«entrega al rival como»', sa=(B['Partida'].x + B['Partida'].w, 110), sb=(B['Oraculo'].x, 110)))
    e.append(edge(B, 'Jugador', 'Personaje', 'assoc', label='secreto 1 · candidatos 0..23', lpos=0.42, sa=(B['Jugador'].x + B['Jugador'].w, 380), sb=(B['Personaje'].x, 380)))
    e.append(edge(B, 'Jugador', 'Base', 'comp', mult_b='1', via=[(400, 470), (400, 730), (230, 730)], sa=(B['Jugador'].x, 470), sb=(230, B['Base'].y)))
    e.append(edge(B, 'Maquina', 'Greedy', 'comp', mult_b='1', sa=(B['Maquina'].cx(), B['Maquina'].y + B['Maquina'].h), sb=(B['Maquina'].cx(), B['Greedy'].y)))
    e.append(edge(B, 'Maquina', 'Oraculo', 'dep', label='«rival en cada turno»', via=[(1150, 700), (1150, 130)], lpos=0.28, loff=(-72, 0),
                  sa=(B['Maquina'].x + B['Maquina'].w, 700), sb=(B['Oraculo'].x + B['Oraculo'].w, 130)))
    e.append(edge(B, 'Greedy', 'Eval', 'dep', label='«crea»', sa=(B['Greedy'].x + B['Greedy'].w, 920), sb=(B['Eval'].x, 920)))
    e.append(edge(B, 'Greedy', 'Base', 'dep', label='«consulta»', sa=(B['Greedy'].x, 880), sb=(B['Base'].x + B['Base'].w, 880)))
    e.append(edge(B, 'Tablero', 'Personaje', 'agg', mult_b='23', sa=(B['Tablero'].cx(), B['Tablero'].y + B['Tablero'].h), sb=(B['Tablero'].cx(), B['Personaje'].y)))
    e.append(edge(B, 'Filtro', 'Personaje', 'dep', label='«evalúa»', loff=(34, 0), sa=(B['Filtro'].cx(), B['Filtro'].y), sb=(B['Filtro'].cx(), B['Personaje'].y + B['Personaje'].h)))
    e.append(edge(B, 'Eval', 'Filtro', 'assoc', mult_b='1', sa=(1180, B['Eval'].y), via=[(1180, 730)], sb=(B['Filtro'].x, 730)))
    e.append(edge(B, 'Arbol', 'Greedy', 'dep', label='«usa»', via=[(1400, 1030), (700, 1030)], lpos=0.5, loff=(0, -6),
                  sa=(1400, B['Arbol'].y + B['Arbol'].h), sb=(700, B['Greedy'].y + B['Greedy'].h)))
    body = '\n'.join(e) + '\n' + '\n'.join(b.svg() for b in B.values())
    render('uml-motor', W, H, page(W, H, 'Diagrama de clases 1 · Motor del juego (paquetes motor y modelo)', body), salida)
    return B

# ---------------------------------------------------------------- Diagrama 2: datos, ordenamiento y arranque
def datos(salida):
    W, H = 1640, 900
    B = {}
    def add(b): B[b.key] = b
    add(Box('Catalogo', 40, 90, 'CatalogoPersonajes', attrs=['+ CANTIDAD: int = 23'], methods=['+ crear(): List<Personaje>']))
    add(Box('Validador', 40, 290, 'ValidadorCatalogo', methods=['+ validar(catalogo): List<String>']))
    add(Box('Organizador', 420, 190, 'OrganizadorTablero', methods=['+ organizar(): Tablero', '+ organizar(catalogo, traza): Tablero']))
    add(Box('Tablero', 830, 90, 'Tablero', attrs=['- personajes: List<Personaje>', '- contadorId: int'],
        methods=['+ agregar(p: Personaje)', '+ obtenerPorId(id: int): Personaje', '+ getPersonajes(): List<Personaje>']))
    add(Box('Personaje', 830, 360, 'Personaje', attrs=['- id: int', '- nombre: String', '- claveOrden: String', '- genero: Genero',
        '- pelado: boolean', '- colorPelo: ColorPelo', '- lentes, barba, sombrero: boolean'],
        methods=['+ POR_NOMBRE: Comparator<Personaje>', '~ asignarId(id: int)', '+ perfil(): int', '+ normalizar(texto): String']))
    add(Box('Genero', 1290, 330, 'Genero', '«enumeration»', attrs=['MUJER', 'HOMBRE']))
    add(Box('Color', 1290, 520, 'ColorPelo', '«enumeration»', attrs=['COLORADO', 'NEGRO', 'AMARILLO', 'NINGUNO']))
    add(Box('Algoritmo', 330, 470, 'AlgoritmoOrdenamiento', '«interface»', methods=['+ getNombre(): String',
        '+ <T> ordenar(lista: List<T>, cmp: Comparator)']))
    add(Box('Merge', 40, 700, 'MergeSort', methods=['+ ordenarConTraza(lista, cmp, traza)', '- combinar(…)']))
    add(Box('Quick', 350, 700, 'QuickSort', methods=['- particionar(…): int']))
    add(Box('Ins', 560, 700, 'OrdenamientoInsercion'))
    add(Box('Burb', 770, 700, 'OrdenamientoBurbuja'))
    add(Box('Binaria', 1250, 90, 'BusquedaBinaria', methods=['+ buscarPorNombre(lista, nombre): int']))
    add(Box('Bench', 1010, 700, 'Benchmark', methods=['+ medir(tamaños…): List<Resultado>', '+ generarAleatorios(n, semilla)']))
    add(Box('UI', 1290, 700, 'paquete ui', '«paquete»', attrs=['VentanaPrincipal, PanelInicio,', 'PanelJugadorVsMaquina,',
        'PanelMaquinaVsMaquina, PanelOrdenamiento,', 'PanelArbolDecision, PanelTablero,', 'TarjetaPersonaje, …'], head='#F4E9F0'))
    e = []
    e.append(edge(B, 'Organizador', 'Catalogo', 'dep', label='«usa»', sa=(B['Organizador'].x, 215), sb=(B['Catalogo'].x + B['Catalogo'].w, 130)))
    e.append(edge(B, 'Organizador', 'Validador', 'dep', label='«valida con»', sa=(B['Organizador'].x, 250), sb=(B['Validador'].x + B['Validador'].w, 310)))
    e.append(edge(B, 'Organizador', 'Tablero', 'dep', label='«crea»', sa=(B['Organizador'].x + B['Organizador'].w, 215), sb=(B['Tablero'].x, 160)))
    e.append(edge(B, 'Organizador', 'Merge', 'dep', label='«ordena con»', via=[(450, 420), (100, 420)], sa=(450, B['Organizador'].y + B['Organizador'].h),
                  sb=(100, B['Merge'].y), lpos=0.5, loff=(0, -8)))
    e.append(edge(B, 'Tablero', 'Personaje', 'agg', mult_b='23', sa=(1000, B['Tablero'].y + B['Tablero'].h), sb=(1000, B['Personaje'].y)))
    e.append(edge(B, 'Personaje', 'Genero', 'assoc', mult_b='1', sa=(B['Personaje'].x + B['Personaje'].w, 390), sb=(B['Genero'].x, 390)))
    e.append(edge(B, 'Personaje', 'Color', 'assoc', mult_b='1', sa=(B['Personaje'].x + B['Personaje'].w, 560), sb=(B['Color'].x, 560)))
    for k, x in (('Merge', 150), ('Quick', 420), ('Ins', 640), ('Burb', 850)):
        e.append(edge(B, k, 'Algoritmo', 'real', sa=(x, B[k].y), via=[(x, 650), (480, 650)] if k in ('Merge', 'Quick') else [(x, 650), (560, 650)],
                      sb=(480 if k in ('Merge', 'Quick') else 560, B['Algoritmo'].y + B['Algoritmo'].h)))
    e.append(edge(B, 'Bench', 'Algoritmo', 'dep', label='«mide»', via=[(1030, 625), (740, 625), (740, 540)], sa=(1030, B['Bench'].y), sb=(B['Algoritmo'].x + B['Algoritmo'].w, 540)))
    e.append(edge(B, 'Binaria', 'Tablero', 'dep', label='«busca en»', sa=(B['Binaria'].x, 130), sb=(B['Tablero'].x + B['Tablero'].w, 130)))
    e.append(edge(B, 'UI', 'Personaje', 'dep', label='«usa motor y modelo»', via=[(1500, 675), (1060, 675)], lpos=0.5, loff=(0, -8),
                  sa=(1500, B['UI'].y), sb=(1060, B['Personaje'].y + B['Personaje'].h)))
    body = '\n'.join(e) + '\n' + '\n'.join(b.svg() for b in B.values())
    render('uml-datos', W, H, page(W, H, 'Diagrama de clases 2 · Datos, ordenamiento y armado del tablero', body), salida)

# ---------------------------------------------------------------- Diagrama 0: paquetes
def paquetes(salida):
    W, H = 1490, 470
    B = {}
    def add(b): B[b.key] = b
    add(Box('ui', 40, 190, 'ui', '«paquete»', attrs=['Swing: ventana, pantallas,', 'tableros y cartas'], w=250, head='#F4E9F0'))
    add(Box('motor', 400, 190, 'motor', '«paquete»', attrs=['Oraculo, Jugador, Partida,', 'EstrategiaGreedy, BaseConocimiento,', 'ArbolDecision, OrganizadorTablero'], w=320))
    add(Box('algo', 830, 60, 'algoritmos', '«paquete»', attrs=['MergeSort, QuickSort, Inserción,', 'Burbujeo, BusquedaBinaria, Benchmark'], w=330))
    add(Box('datos', 830, 320, 'datos', '«paquete»', attrs=['CatalogoPersonajes,', 'ValidadorCatalogo'], w=330))
    add(Box('modelo', 1230, 190, 'modelo', '«paquete»', attrs=['Personaje, Tablero,', 'Filtro, Genero, ColorPelo'], w=230))
    e = []
    e.append(edge(B, 'ui', 'motor', 'dep'))
    e.append(edge(B, 'motor', 'algo', 'dep'))
    e.append(edge(B, 'motor', 'datos', 'dep'))
    e.append(edge(B, 'algo', 'modelo', 'dep'))
    e.append(edge(B, 'datos', 'modelo', 'dep'))
    e.append(edge(B, 'motor', 'modelo', 'dep', sa=(B['motor'].x + B['motor'].w, 250), sb=(B['modelo'].x, 250)))
    body = '\n'.join(e) + '\n' + '\n'.join(b.svg() for b in B.values())
    render('uml-paquetes', W, H, page(W, H, 'Estructura del proyecto · dependencias entre paquetes (sin ciclos)', body), salida)

if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'img')
    paquetes(out); motor(out); datos(out)
