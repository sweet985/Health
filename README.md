# 心理健康网站项目指南

## 项目简介
这是一个基于 Java 21 + Spring Boot 3 + Vue 3 + MySQL 8.0 的全栈心理健康网站。包含树洞、社区、交友、聊天、治愈资源等模块。

## 技术栈
- **后端**: Java 21, Spring Boot 3.2.2, MyBatis-Plus 3.5.5, JWT, MySQL 8.0
- **前端**: Vue 3, Vite, Element Plus, Pinia, Axios, Vue Router

## 目录结构
- `src/main/java`: 后端源代码
- `src/main/resources`: 后端配置与资源
- `frontend`: 前端源代码
- `sql`: 数据库脚本

## 启动步骤

### 1. 数据库准备
1. 确保已安装 MySQL 8.0。
2. 创建数据库 `health_db` (如果脚本未自动创建)。
3. 运行 `sql/schema.sql` 脚本初始化表结构和数据。
   ```sql
   source /path/to/project/sql/schema.sql;
   ```

### 2. 后端启动
1. 打开 `src/main/resources/application.yml`。
2. 修改 `spring.datasource.password` 为你的 MySQL 密码。
3. 使用 IDEA 或 Maven 运行项目：
   ```bash
   mvn spring-boot:run
   ```
   或者运行 `com.example.health.HealthApplication` 的 main 方法。
   服务将在 `http://localhost:8080` 启动。

### 3. 前端启动
1. 进入前端目录：
   ```bash
   cd frontend
   ```
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
4. 浏览器访问 `http://localhost:5173` (或控制台显示的端口)。

## 功能说明
- **注册/登录**: 首次使用需注册。
- **树洞**: 匿名发布，不显示个人信息。
- **社区**: 实名发布，显示用户名和头像。
- **寻找他人**: 搜索用户名添加好友，支持私信聊天（5分钟内可撤回）。
- **个人中心**: 修改资料，注销账号（慎用）。

## 注意事项
- 确保前后端端口未被占用。
- 确保 MySQL 服务已启动。
- JWT 密钥在 `application.yml` 中配置，生产环境请修改。
