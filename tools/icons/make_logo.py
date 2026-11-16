"""Draws the Waystones Customizations logo: an isometric rune stone (own model, rendered here
block by block) on a blue gradient, with a painter's palette in front.

Usage:  python make_logo.py <out dir>
Writes logo_128.png (inside the mod) and logo_512.png (Modrinth / CurseForge).
"""
import os
import random
import sys
from PIL import Image

SIZE = 128      # canvas
S = 3           # screen pixels per texel along the iso axes


def rgb(h, a=255):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (a,)


def mix(c1, c2, t):
    return tuple(round(c1[i] + (c2[i] - c1[i]) * t) for i in range(3)) + (255,)


def scale(c, f):
    return tuple(max(0, min(255, round(v * f))) for v in c[:3]) + (255,)


# ---------------------------------------------------------------- background

def background(img):
    top, bottom = rgb('#2f63e8'), rgb('#7fd2ff')
    for y in range(SIZE):
        for x in range(SIZE):
            c = mix(top, bottom, y / (SIZE - 1))
            d = ((x - 52) ** 2 + (y - 58) ** 2) ** 0.5
            if d < 60:
                c = mix(c, rgb('#ffffff'), (1 - d / 60) * 0.18)
            img.putpixel((x, y), c)


# ---------------------------------------------------------------- stone texture

random.seed(7)
STONE = [rgb('#9b9b9e'), rgb('#8c8c90'), rgb('#7e7e83'), rgb('#a9a9ac'), rgb('#74747a')]
NOISE = [[random.choice([0, 0, 1, 1, 1, 2, 2, 3, 4]) for _ in range(64)] for _ in range(64)]


def stone(u, v):
    return STONE[NOISE[int(v) % 64][int(u) % 64]]


# rune glyphs, 3x4 texels each, carved into the pillar sides
GLYPHS = [
    ['XX.', '..X', '.X.', 'X.X'],
    ['X.X', 'X.X', '.X.', '.X.'],
    ['.XX', 'X..', 'XX.', '..X'],
    ['X..', 'XXX', '..X', 'XX.'],
    ['.X.', 'X.X', 'XX.', '..X'],
    ['XXX', '.X.', 'X..', 'X.X'],
]
RUNE = rgb('#d9f6ff')
RUNE_GLOW = rgb('#7fd8ff')


def rune_at(face_seed, u, v, width, height):
    """Rune pixel colour on a pillar side, or None. One column of runes, three rows."""
    col_w, row_h = 4, 6
    left = (width - 3) // 2
    top = 3
    cu, cv = int(u) - left, int(v) - top
    if cu < 0 or cv < 0:
        return None
    col, ix = divmod(cu, col_w)
    row, iy = divmod(cv, row_h)
    if col > 0 or row > 2 or ix > 2 or iy > 3:
        return None
    glyph = GLYPHS[(face_seed + col * 3 + row) % len(GLYPHS)]
    if glyph[iy][ix] == 'X':
        return RUNE
    # soft glow next to a rune pixel
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        nx, ny = ix + dx, iy + dy
        if 0 <= nx < 3 and 0 <= ny < 4 and glyph[ny][nx] == 'X':
            return 'glow'
    return None


# ---------------------------------------------------------------- iso renderer

# boxes: (x0, x1, y0, y1, z0, z1, kind)
BOXES = [
    (-7, 7, 0, 2, -7, 7, 'base'),
    (-6, 6, 2, 4, -6, 6, 'base'),
    (-4, 4, 4, 24, -4, 4, 'pillar'),
    (-5, 5, 24, 26, -5, 5, 'cap'),
    (-4, 4, 26, 28, -4, 4, 'cap'),
    (-2, 2, 28, 29, -2, 2, 'cap'),
]
CX, CY = 48, 102


def faces_of(box):
    x0, x1, y0, y1, z0, z1, kind = box
    return [('top', box), ('left', box), ('right', box)]


def sample(face, box, sx, sy):
    """Colour of this face at screen pixel (sx, sy), or None if the pixel is not on the face."""
    x0, x1, y0, y1, z0, z1, kind = box
    a = (sx + 0.5 - CX) / S
    py = sy + 0.5
    if face == 'top':
        b = (py - CY + y1 * S) / (S / 2)
        X, Z = (a + b) / 2, (b - a) / 2
        if not (x0 <= X < x1 and z0 <= Z < z1):
            return None
        u, v = X - x0, Z - z0
        c = stone(u + 7, v + 31)
        edge = u < 1 or v < 1 or u >= (x1 - x0) - 1 or v >= (z1 - z0) - 1
        c = scale(c, 1.18 if not edge else 1.3)
        return c
    if face == 'left':     # the face at Z = z1, facing the viewer on the left
        X = a + z1
        if not (x0 <= X < x1):
            return None
        Y = (CY + (X + z1) * S / 2 - py) / S
        if not (y0 <= Y < y1):
            return None
        u, v = X - x0, y1 - Y
        shade = 0.92
        seed = 0
    else:                  # the face at X = x1, on the right
        Z = x1 - a
        if not (z0 <= Z < z1):
            return None
        Y = (CY + (x1 + Z) * S / 2 - py) / S
        if not (y0 <= Y < y1):
            return None
        u, v = Z - z0, y1 - Y
        shade = 0.7
        seed = 3
    c = stone(u + (0 if face == 'left' else 20), v + y0 * 3)
    if kind == 'pillar':
        r = rune_at(seed, u, v, x1 - x0 if face == 'left' else z1 - z0, y1 - y0)
        if r == 'glow':
            c = mix(scale(c, 0.85), RUNE_GLOW, 0.22)
            return scale(c, shade + 0.1)
        if r is not None:
            return r if face == 'left' else mix(r, RUNE_GLOW, 0.3)
    # darker line at the bottom of each slab so the tiers read clearly
    if v >= (y1 - y0) - 1 and kind != 'pillar':
        c = scale(c, 0.78)
    if v < 1 and kind != 'pillar':
        c = scale(c, 1.08)
    return scale(c, shade)


def render_stone(img):
    layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    for box in BOXES:           # bottom to top, later boxes are in front
        for sy in range(SIZE):
            for sx in range(SIZE):
                for face in ('top', 'left', 'right'):
                    c = sample(face, box, sx, sy)
                    if c is not None:
                        layer.putpixel((sx, sy), c)
                        break
    # a bit of moss on the lower base
    random.seed(3)
    for sy in range(SIZE):
        for sx in range(SIZE):
            p = layer.getpixel((sx, sy))
            if p[3] and sy > CY - 8 and random.random() < 0.03:
                layer.putpixel((sx, sy), random.choice([rgb('#6a9a3f'), rgb('#557f30')]))
    # ground shadow
    shadow = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    for sy in range(SIZE):
        for sx in range(SIZE):
            dx, dy = (sx - CX - 4) / 50, (sy - CY - 4) / 22
            d = dx * dx + dy * dy
            if d < 1:
                shadow.putpixel((sx, sy), (10, 30, 80, round(90 * (1 - d))))
    img.alpha_composite(shadow)
    img.alpha_composite(layer)


# ---------------------------------------------------------------- palette (drawn on a 32 grid, scaled x3)

N = 32


def palette(img):
    wood_l, wood, wood_d = rgb('#ecc08a'), rgb('#cf9455'), rgb('#94612e')
    cx, cy, rx, ry = 17.5, 23.5, 10.5, 6.0
    for y in range(N):
        for x in range(N):
            dx, dy = (x - cx) / rx, (y - cy) / ry
            if dx * dx + dy * dy > 1:
                continue
            if ((x - 8.5) ** 2 + (y - 28) ** 2) < 7.5:
                continue
            light = -dx * 0.5 - dy * 0.8
            c = wood_l if light > 0.45 else wood if light > -0.45 else wood_d
            img.putpixel((x, y), c)
    for (x, y), c in [((11, 23), '#5a3818'), ((12, 23), '#5a3818'), ((11, 24), '#7a4f24'), ((12, 24), '#94612e')]:
        img.putpixel((x, y), rgb(c))
    blobs = [('#ff4f4f', (13, 20)), ('#ffd23f', (18, 19)), ('#5be36a', (23, 20)),
             ('#3fb6ff', (24, 24)), ('#b06cff', (19, 26)), ('#ffffff', (14, 26))]
    for color, (x, y) in blobs:
        c = rgb(color)
        shadow = mix(c, rgb('#000000'), 0.35)
        for px, py in [(x, y), (x + 1, y), (x, y + 1), (x + 1, y + 1), (x + 2, y), (x + 2, y + 1)]:
            img.putpixel((px, py), c)
        img.putpixel((x, y), mix(c, rgb('#ffffff'), 0.5))
        img.putpixel((x + 1, y + 2), shadow)
        img.putpixel((x + 2, y + 2), shadow)


def palette_brush(img):
    wood, wood_d = rgb('#3b3b46'), rgb('#22222a')
    metal, metal_d = rgb('#ececf2'), rgb('#9a9aa8')
    paint, paint_d = rgb('#3fb6ff'), rgb('#1d7fc0')
    for i in range(6):
        x, y = 23 + i, 11 - i
        img.putpixel((x, y), wood)
        img.putpixel((x, y + 1), wood_d)
    for x, y in [(22, 12), (21, 13)]:
        img.putpixel((x, y), metal)
        img.putpixel((x, y + 1), metal_d)
    for (x, y), c in [((20, 14), paint), ((19, 15), paint), ((20, 15), paint_d), ((19, 14), paint)]:
        img.putpixel((x, y), c)


def outline_layer(layer, color):
    out = layer.copy()
    w, h = layer.size
    for y in range(h):
        for x in range(w):
            if layer.getpixel((x, y))[3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and layer.getpixel((nx, ny))[3] == 255:
                    out.putpixel((x, y), color)
                    break
    return out


def palette_layer():
    small = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    palette(small)
    palette_brush(small)
    small = outline_layer(small, rgb('#120c08'))
    big = small.resize((N * 3, N * 3), Image.NEAREST)
    shadow = Image.new('RGBA', big.size, (0, 0, 0, 0))
    for y in range(big.height):
        for x in range(big.width):
            if big.getpixel((x, y))[3] and x + 3 < big.width and y + 3 < big.height:
                shadow.putpixel((x + 3, y + 3), (8, 20, 60, 90))
    out = Image.new('RGBA', big.size, (0, 0, 0, 0))
    out.alpha_composite(shadow)
    out.alpha_composite(big)
    return out


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
    os.makedirs(out, exist_ok=True)
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 255))
    background(img)
    render_stone(img)
    pal = palette_layer()
    img.alpha_composite(pal, (SIZE - pal.width + 2, SIZE - pal.height + 4))
    img.save(os.path.join(out, 'logo_128.png'))
    img.resize((512, 512), Image.NEAREST).save(os.path.join(out, 'logo_512.png'))
    print('written to', out)


if __name__ == '__main__':
    main()
