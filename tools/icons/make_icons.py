"""Draws the Waystones Customizations group icons (16x16 pixel art) from the maps in icons.txt.

Every icon gets the same dark outline automatically, so the set looks consistent.
Usage:  python make_icons.py <output sprite folder> <index file> [preview.png]
"""
import os
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))

PALETTE = {
    'W': '#c8a46a', 'w': '#8f6c3c', 'v': '#5e4426',
    'R': '#e0473a', 'r': '#9c2a1e', 'P': '#f7b3c9', 'p': '#d0739a',
    'G': '#72c84e', 'g': '#4a9230', 'h': '#2f6a1d',
    'D': '#9a6434', 'd': '#6b4423',
    'S': '#a3a3a3', 's': '#707070', 'T': '#d2d2d2',
    'Y': '#ffe066', 'y': '#d9a62a',
    'O': '#ff9a2e', 'o': '#c0661a',
    'B': '#4aa3ff', 'b': '#2a6fc4', 'C': '#9af0ff', 'c': '#3cbdd6',
    'U': '#a65ff2', 'u': '#6a2bb3', 'V': '#dcb4ff',
    'K': '#3a3a3a', 'k': '#1a1a1a',
    'E': '#fafafa', 'e': '#cfcfcf',
    'N': '#8c2f2f', 'n': '#521a1a',
    'L': '#ff7a1c', 'l': '#ffd23f',
    'M': '#4be07a', 'm': '#1e9a45',
    'Z': '#f0dc94', 'z': '#c9b064',
    'I': '#e6e6e6', 'i': '#a8a8a8',
    'A': '#ffd84a', 'a': '#d69a1e',
    'X': '#f2f2bc', 'x': '#c4c48a',
    'J': '#0f4656', 'j': '#2ee0ec',
    'F': '#ff5a1c',
}
OUTLINE = (24, 20, 26, 255)


def hex_to_rgba(value):
    value = value.lstrip('#')
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def parse(path):
    icons = []
    name, category, rows = None, None, []
    for raw in open(path, encoding='utf-8'):
        line = raw.rstrip('\n')
        if line.startswith('#') or not line.strip():
            continue
        if line.startswith('@'):
            if name:
                icons.append((name, category, rows))
            parts = line[1:].split()
            name, category, rows = parts[0], parts[1], []
        else:
            rows.append(line.strip())
    if name:
        icons.append((name, category, rows))
    return icons


def _speck(a, b, mod, hit=0):
    return (a * 7 + b * 13 + a * b) % mod == hit


def iso(kind):
    """Isometric block, top face as a diamond, left face lighter than the right face."""
    def top(c, r, edge):
        if kind == 'grass':
            return 'g' if _speck(c, r, 5) else 'G'
        if kind == 'ore':
            return 'C' if _speck(c, r, 6) else ('T' if _speck(c, r, 7, 3) else 'S')
        if kind == 'tnt':
            return 'k' if c in (7, 8) and r in (3, 4) else ('s' if edge else 'S')
        if kind == 'log':
            if edge:
                return 'v'
            return 'w' if (abs(c - 7.5) * 0.5 + abs(r - 4)) % 2 < 0.8 else 'W'
        raise ValueError(kind)

    def face(i, j, left):
        if kind == 'grass':
            if j < 2 or (j == 2 and i % 3 == 0):
                return 'g' if left else 'h'
            if left:
                return 'd' if _speck(i, j, 5) else 'D'
            return 'v' if _speck(i, j, 5) else 'd'
        if kind == 'ore':
            if _speck(i, j, 6, 1):
                return 'C' if left else 'c'
            return 'S' if left else 's'
        if kind == 'tnt':
            if 2 <= j <= 4:
                if j == 3 and i % 2 == 1:
                    return 'k'
                return 'E' if left else 'e'
            return 'R' if left else 'r'
        if kind == 'log':
            if left:
                return 'v' if i % 3 == 1 else 'd'
            return 'k' if i % 3 == 1 else 'v'
        raise ValueError(kind)

    grid = [['.'] * 16 for _ in range(16)]
    for c in range(1, 15):
        left = c <= 7
        h = (c - 1) // 2 if left else (14 - c) // 2
        for r in range(4 - h, 4 + h + 1):
            grid[r][c] = top(c, r, r in (4 - h, 4 + h))
        for r in range(5 + h, 12 + h):
            i = c - 1 if left else 14 - c
            grid[r][c] = face(i, r - (5 + h), left)
    return [''.join(row) for row in grid]


def render(rows):
    if rows and rows[0].startswith('=iso'):
        rows = iso(rows[0].split()[1])
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows[:16]):
        if len(row) != 16:
            raise ValueError(f'row {y} has {len(row)} pixels: {row!r}')
        for x, ch in enumerate(row):
            if ch in PALETTE:
                img.putpixel((x, y), hex_to_rgba(PALETTE[ch]))
    return finish(img)


def _mix(c, t, f):
    return tuple(round(c[i] + (t[i] - c[i]) * f) for i in range(3)) + (255,)


def finish(img):
    """Shading + coloured outline, so every icon gets the same polished item look."""
    src = img.copy()
    filled = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and src.getpixel((x, y))[3] > 0
    # 1) light from the top left, shadow on the bottom right edge of every shape
    for y in range(16):
        for x in range(16):
            if not filled(x, y):
                continue
            c = src.getpixel((x, y))
            lum = (c[0] * 3 + c[1] * 6 + c[2]) / 10
            if not filled(x, y - 1) or not filled(x - 1, y):
                if lum < 235:
                    img.putpixel((x, y), _mix(c, (255, 255, 240), 0.22))
            elif not filled(x, y + 1) or not filled(x + 1, y):
                img.putpixel((x, y), _mix(c, (10, 6, 20), 0.28))
    # 2) outline in a very dark version of the neighbouring colour (instead of flat black)
    out = img.copy()
    for y in range(16):
        for x in range(16):
            if filled(x, y):
                continue
            near = [src.getpixel((x + dx, y + dy)) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)) if filled(x + dx, y + dy)]
            if not near:
                continue
            avg = tuple(sum(n[i] for n in near) // len(near) for i in range(3))
            out.putpixel((x, y), _mix(avg, OUTLINE[:3], 0.78))
    # 3) soft drop shadow one pixel down-right, behind everything
    for y in range(15, 0, -1):
        for x in range(15, 0, -1):
            if out.getpixel((x, y))[3] == 0 and out.getpixel((x - 1, y - 1))[3] > 0 and not filled(x - 1, y - 1):
                out.putpixel((x, y), (0, 0, 0, 70))
    return out


def main():
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    icons = parse(os.path.join(HERE, 'icons.txt'))
    images = []
    for name, category, rows in icons:
        img = render(rows)
        img.save(os.path.join(out, name + '.png'))
        images.append((name, category, img))
    with open(sys.argv[2], 'w', newline='\n') as f:
        for name, category, _ in icons:
            f.write(f'{category} {name}\n')
    if len(sys.argv) > 3:
        cols = 10
        scale = 6
        rows_n = (len(images) + cols - 1) // cols
        sheet = Image.new('RGBA', (cols * 18 * scale, rows_n * 18 * scale), (60, 60, 60, 255))
        for i, (_, _, img) in enumerate(images):
            big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
            sheet.alpha_composite(big, ((i % cols) * 18 * scale + scale, (i // cols) * 18 * scale + scale))
        sheet.save(sys.argv[3])
    print(f'{len(images)} icons written')


if __name__ == '__main__':
    main()
