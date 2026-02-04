#!/bin/bash
# Setup script for developing buy-02 locally
# Installs git hooks and configures environment

set -e

echo "🚀 Setting up buy-02 development environment..."

# Install git hooks
echo "📌 Installing git hooks..."
cp .git/hooks/pre-commit .git/hooks/pre-commit
cp .git/hooks/pre-push .git/hooks/pre-push
chmod +x .git/hooks/pre-commit .git/hooks/pre-push

echo "✅ Git hooks installed"

# Check for required tools
echo "🔍 Checking required tools..."

if ! command -v java &> /dev/null; then
    echo "❌ Java 21 is required but not installed"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is required but not installed"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "❌ Docker is required but not installed"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=")\d+' | head -1)
if [ "$JAVA_VERSION" != "21" ]; then
    echo "⚠️  Java version is $JAVA_VERSION, but 21 is recommended"
fi

echo "✅ All required tools are installed"

# Setup environment
echo "📝 Setting up environment..."
if [ ! -f .env ]; then
    cp .env.example .env
    echo "⚠️  .env created from .env.example"
    echo "⚠️  Please update .env with your actual secrets"
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "📚 Next steps:"
echo "1. Update .env with your secrets"
echo "2. Run: mvn clean verify"
echo "3. Start services: docker compose -f docker-compose.dev.yml up -d"
echo ""
