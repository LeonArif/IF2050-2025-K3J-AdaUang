#!/bin/bash

# Menghapus folder bin jika ada
rm -rf bin

# Membuat folder bin/assets
mkdir -p bin/assets

# Menyalin resource dari src/assets ke bin/assets
cp -r src/assets/* bin/assets/

echo "[1] Compiling source code..."
javac -d bin -sourcepath src src/AppLauncher.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed."
    exit 1
fi

echo "[2] Running game..."
java -cp "bin:lib/mysql-connector-j-9.3.0.jar" AppLauncher
