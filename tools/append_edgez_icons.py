#!/usr/bin/env python3
"""Add EdgeZ symbols to the existing Organic Maps atlases without moving existing symbols."""

from io import BytesIO
from pathlib import Path
import xml.etree.ElementTree as ET

import cairosvg
from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SIZES = {"mdpi": 18, "hdpi": 27, "xhdpi": 36, "6plus": 43, "xxhdpi": 54, "xxxhdpi": 64}
ICONS = sorted((ROOT / "data/styles/default/light/symbols").glob("edgez_*-*.svg"))

for density, size in SIZES.items():
    for theme in ("light", "dark"):
        atlas_dir = ROOT / "data/symbols" / density / theme
        image_path = atlas_dir / "symbols.png"
        xml_path = atlas_dir / "symbols.xml"
        tree = ET.parse(xml_path)
        file_element = tree.getroot().find("file")
        if file_element is None:
            raise ValueError(f"Missing atlas metadata in {xml_path}")
        symbols = file_element.findall("symbol")
        existing = {symbol.get("name") for symbol in symbols}
        y = max(int(symbol.get("maxY", "0")) for symbol in symbols) + 2
        x = 2
        with Image.open(image_path) as original:
            image = original.convert("RGBA")
        for light_icon in ICONS:
            name = light_icon.stem
            if name in existing:
                raise ValueError(f"Icon {name} already exists in {xml_path}")
            svg = ROOT / "data/styles/default" / theme / "symbols" / light_icon.name
            rendered = cairosvg.svg2png(url=str(svg), output_width=size, output_height=size)
            icon = Image.open(BytesIO(rendered)).convert("RGBA")
            if x + size + 2 > image.width:
                x = 2
                y += size + 2
            if y + size + 2 > image.height:
                raise ValueError(f"No room for {name} in {image_path}")
            image.alpha_composite(icon, (x, y))
            ET.SubElement(file_element, "symbol", {
                "minX": str(x), "minY": str(y),
                "maxX": str(x + size), "maxY": str(y + size), "name": name,
            })
            x += size + 2
        image.save(image_path, optimize=True)
        tree.write(xml_path, encoding="utf-8", xml_declaration=True)
        print(f"Added {len(ICONS)} icons to {density}/{theme}")
