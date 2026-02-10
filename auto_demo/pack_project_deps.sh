#!/bin/bash

# --- 配置 ---
OFFLINE_DIR="maven_offline_bundle"
DATE=$(date +%Y%m%d_%H%M)
OUTPUT_NAME="project_repo_$DATE.tar.gz"

echo "================================================="
echo "   Maven 标准布局打包工具 (2026 稳定版)"
echo "================================================="

# 1. 环境检查
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在包含 pom.xml 的项目根目录运行。"
    exit 1
fi

# 清理旧目录
rm -rf "$OFFLINE_DIR"
mkdir -p "$OFFLINE_DIR"

# 2. 核心提取命令 (关键参数说明)
# -Dmdep.useRepositoryLayout=true : 强制生成 com/xxx/xxx 这种目录结构
# -Dmdep.copyPom=true : 同时拷贝 POM 文件（离线解析必需）
# -DaddParentPoms=true : 包含父工程 POM
echo "步骤 1: 正在从本地仓库提取依赖 (带目录结构)..."

mvn dependency:copy-dependencies \
    -DoutputDirectory="$OFFLINE_DIR" \
    -Dmdep.useRepositoryLayout=true \
    -Dmdep.copyPom=true \
    -DaddParentPoms=true \
    -DincludeScope=runtime

# 3. 检查是否成功提取
if [ -z "$(ls -A $OFFLINE_DIR)" ]; then
    echo "错误: 提取失败，文件夹是空的！"
    echo "请检查：1. 你的项目是否能正常编译？ 2. 是否安装了 Maven 并配置了环境变量？"
    exit 1
fi

# 4. 打包压缩
echo "步骤 2: 正在打包为 $OUTPUT_NAME ..."
tar -czf "$OUTPUT_NAME" "$OFFLINE_DIR"

# 清理
rm -rf "$OFFLINE_DIR"

echo "================================================="
echo "打包成功！"
echo "使用方法: 解压后，将内部文件夹内容直接覆盖到离线电脑的 .m2/repository"
echo "================================================="