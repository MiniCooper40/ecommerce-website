#!/usr/bin/env python3
"""
Generate placeholder images for MinIO seed data
Creates simple colored rectangles with text as placeholder product images
"""

import os
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("\n❌ PIL (Pillow) not found. Installing...")
    import subprocess
    import sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image, ImageDraw, ImageFont

# Define output directory
output_dir = Path(__file__).parent / "products"
output_dir.mkdir(exist_ok=True)

# Define images with colors
images = [
    # Electronics - Blue tones
    ("headphones-1.jpg", "#4A90E2"),
    ("headphones-2.jpg", "#357ABD"),
    ("fitness-watch-1.jpg", "#5B9BD5"),
    ("fitness-watch-2.jpg", "#2E75B6"),
    ("fitness-watch-3.jpg", "#1F4E78"),
    ("webcam-1.jpg", "#4472C4"),
    ("webcam-2.jpg", "#2F5496"),
    
    # Home & Kitchen - Red/Orange tones
    ("coffee-maker-1.jpg", "#E74C3C"),
    ("coffee-maker-2.jpg", "#C0392B"),
    ("cookware-1.jpg", "#E67E22"),
    ("cookware-2.jpg", "#D35400"),
    ("cookware-3.jpg", "#CA6F1E"),
    ("robot-vacuum-1.jpg", "#F39C12"),
    ("robot-vacuum-2.jpg", "#E67E22"),
    
    # Sports & Outdoors - Green tones
    ("yoga-mat-1.jpg", "#27AE60"),
    ("yoga-mat-2.jpg", "#229954"),
    ("yoga-mat-3.jpg", "#1E8449"),
    ("dumbbells-1.jpg", "#52BE80"),
    ("dumbbells-2.jpg", "#45B39D"),
    ("tent-1.jpg", "#16A085"),
    ("tent-2.jpg", "#138D75"),
    ("tent-3.jpg", "#117A65"),
    
    # Books & Media - Purple tones
    ("book-programming-1.jpg", "#8E44AD"),
    ("book-programming-2.jpg", "#7D3C98"),
    
    # Fashion - Mixed tones
    ("backpack-1.jpg", "#34495E"),
    ("backpack-2.jpg", "#2C3E50"),
    ("backpack-3.jpg", "#1C2833"),
    ("running-shoes-1.jpg", "#E74C3C"),
    ("running-shoes-2.jpg", "#EC7063"),
    ("running-shoes-3.jpg", "#F1948A"),
]

def hex_to_rgb(hex_color):
    """Convert hex color to RGB tuple"""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

def create_placeholder_image(filename, color, width=800, height=600):
    """Create a placeholder image with colored background and text"""
    # Create image
    rgb_color = hex_to_rgb(color)
    img = Image.new('RGB', (width, height), color=rgb_color)
    draw = ImageDraw.Draw(img)
    
    # Extract text from filename
    text = filename.replace('-', ' ').replace('.jpg', '').title()
    text_lines = text.split()
    
    # Try to use a nice font, fall back to default if not available
    try:
        font = ImageFont.truetype("arial.ttf", 48)
        small_font = ImageFont.truetype("arial.ttf", 24)
    except:
        try:
            font = ImageFont.truetype("Arial.ttf", 48)
            small_font = ImageFont.truetype("Arial.ttf", 24)
        except:
            # Use default font
            font = ImageFont.load_default()
            small_font = ImageFont.load_default()
    
    # Calculate text position (centered)
    # Draw product name
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    x = (width - text_width) // 2
    y = (height - text_height) // 2 - 20
    
    # Draw text with shadow for better visibility
    shadow_offset = 2
    draw.text((x + shadow_offset, y + shadow_offset), text, fill=(0, 0, 0, 128), font=font)
    draw.text((x, y), text, fill=(255, 255, 255), font=font)
    
    # Draw "Placeholder" label
    placeholder_text = "Placeholder Image"
    bbox = draw.textbbox((0, 0), placeholder_text, font=small_font)
    text_width = bbox[2] - bbox[0]
    x = (width - text_width) // 2
    y = height - 60
    draw.text((x + 1, y + 1), placeholder_text, fill=(0, 0, 0, 100), font=small_font)
    draw.text((x, y), placeholder_text, fill=(255, 255, 255, 200), font=small_font)
    
    return img

def main():
    print("🎨 Generating placeholder product images...")
    print("")
    
    count = 0
    for filename, color in images:
        output_path = output_dir / filename
        img = create_placeholder_image(filename, color)
        img.save(output_path, 'JPEG', quality=85)
        count += 1
        print(f"[{count}/{len(images)}] ✓ Created {filename}")
    
    print("")
    print(f"✓ Successfully generated {count} placeholder images")
    print(f"  Location: {output_dir}")
    print("")
    print("Next steps:")
    print(f"1. Review images in: {output_dir}")
    print("2. Replace with actual product images if desired")
    print("3. Restart minio-setup container: docker-compose restart minio-setup")

if __name__ == "__main__":
    main()
