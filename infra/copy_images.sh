#!/bin/bash

# Run only using Makefile command
src_file="infra/x_image.png"
dest_dir="uploads/images"

mkdir -p "$dest_dir"

for i in {1..25}; do
    cp "$src_file" "$dest_dir/image_path_$i.png"
    echo "Copied: $src_file -> $dest_dir/image_path_$i.png"
done

echo "All product images copied successfully!"
