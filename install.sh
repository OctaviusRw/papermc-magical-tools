#!/bin/bash

# Custom Pickaxe Plugin Installation Script

echo "Building Custom Pickaxe Plugin..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 Plugin JAR created at: build/libs/custom-pickaxe-plugin-1.0.0.jar"
    echo ""
    echo "To install on your PaperMC server:"
    echo "1. Copy build/libs/custom-pickaxe-plugin-1.0.0.jar to your server's plugins/ directory"
    echo "2. Restart your server"
    echo "3. The plugin will automatically give new players a custom wooden pickaxe!"
    echo ""
    echo "Commands available:"
    echo "  /custompickaxe give   - Manually give a custom pickaxe"
    echo "  /custompickaxe reload - Reload plugin configuration"
    echo ""
    echo "Permissions:"
    echo "  custompickaxe.give   - Allow giving pickaxes (default: op)"
    echo "  custompickaxe.reload - Allow reloading config (default: op)"
else
    echo "❌ Build failed! Check the error messages above."
    exit 1
fi
