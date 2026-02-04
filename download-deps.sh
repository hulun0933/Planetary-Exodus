#!/bin/bash
set -e

echo "🔧 下载 Planetary Exodus 必要的依赖..."

# 清理缓存
echo "🧹 清理缓存..."
rm -rf ~/.gradle/caches/fabric-loom 2>/dev/null || true
rm -rf ~/.gradle/caches/modules-2/files-2.1/net.fabricmc 2>/dev/null || true

# 手动下载关键依赖
mkdir -p libs
cd libs

# 1. 下载 Fabric Loader
echo "📥 下载 Fabric Loader 0.15.11..."
if [ ! -f "fabric-loader-0.15.11.jar" ]; then
    wget -q --show-progress "https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.15.11/fabric-loader-0.15.11.jar" || {
        echo "❌ 从主源下载失败，尝试备用源..."
        wget -q --show-progress "https://cdn.modrinth.com/data/P7dR8mSH/versions/aVt8dOe7/fabric-loader-0.15.11.jar" || {
            echo "⚠️  Fabric Loader 下载失败，请手动下载："
            echo "   https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.15.11/fabric-loader-0.15.11.jar"
        }
    }
fi

# 2. 下载 Fabric API（多个备选版本）
echo "📥 下载 Fabric API..."
FABRIC_API_FOUND=false

# 尝试下载不同版本
versions=(
    "0.97.0+1.21.1"  # 最稳定的版本
    "0.96.0+1.21.1"
    "0.95.0+1.21.1"
    "0.98.0+1.21.1"
)

for version in "${versions[@]}"; do
    echo "  尝试版本: $version"
    if wget -q --show-progress "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$version/fabric-api-$version.jar" 2>/dev/null; then
        echo "  ✅ 下载成功: $version"
        FABRIC_API_FOUND=true
        break
    else
        echo "  ❌ 版本 $version 不可用"
    fi
done

# 如果主源都失败，尝试备用源
if [ "$FABRIC_API_FOUND" = false ]; then
    echo "⚠️  所有主源版本都失败，尝试备用源..."
    
    # 尝试从Modrinth下载
    wget -q --show-progress "https://cdn.modrinth.com/data/P7dR8mSH/versions/aVt8dOe7/fabric-api-0.97.0+1.21.1.jar" 2>/dev/null && {
        echo "  ✅ 从Modrinth下载成功"
        FABRIC_API_FOUND=true
    } || true
fi

# 如果还是失败，给出手动下载指南
if [ "$FABRIC_API_FOUND" = false ]; then
    echo ""
    echo "❌ 无法自动下载 Fabric API，请手动操作："
    echo "=========================================="
    echo "1. 访问 https://modrinth.com/mod/fabric-api"
    echo "2. 下载版本 0.97.0+1.21.1"
    echo "3. 将文件重命名为 'fabric-api-0.97.0+1.21.1.jar'"
    echo "4. 放入 'libs/' 目录"
    echo "=========================================="
    echo ""
    echo "或者使用离线模式构建："
    echo "  ./gradlew --offline build"
fi

# 回到项目目录
cd ..
echo ""
echo "📊 依赖状态："
if [ -f "libs/fabric-loader-0.15.11.jar" ]; then
    echo "  ✅ fabric-loader-0.15.11.jar"
else
    echo "  ❌ fabric-loader-0.15.11.jar (缺失)"
fi

if ls libs/fabric-api-*.jar 1>/dev/null 2>&1; then
    echo "  ✅ Fabric API: $(ls libs/fabric-api-*.jar | head -1 | xargs basename)"
else
    echo "  ❌ Fabric API (缺失)"
fi

echo ""
echo "🎯 下一步："
echo "  运行: ./gradlew --refresh-dependencies"
echo "  或使用离线模式: ./gradlew --offline build"
echo ""
echo "⚠️  注意: 如果依赖下载失败，可能需要："
echo "  1. 检查网络连接"
echo "  2. 使用VPN（如果需要）"
echo "  3. 手动下载缺失的jar文件"
echo "  4. 使用IDE（如IntelliJ IDEA）自动处理依赖"
