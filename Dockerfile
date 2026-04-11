# 使用 Maven 官方镜像进行构建
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 执行构建并跳过测试
RUN mvn clean package -DskipTests

# 使用轻量级的 JRE 镜像来运行应用
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8081

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
