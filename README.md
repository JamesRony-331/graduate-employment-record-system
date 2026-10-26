# 高校毕业生就业信息备案系统

本项目是基于 Spring Boot 和 Vue 3 的高校毕业生就业信息备案系统，包含管理端、用户端和后端服务。

## 项目结构

```text
.
├── admin/graduate-employment-admin   # 管理端（Vue 3 + Vite）
├── client/graduate-employment-user   # 用户端（Vue 3 + Vite）
└── service/EmploymentServer          # 后端（Spring Boot）
```

## 环境要求

- JDK 21
- Maven 3.9 或更高版本
- Node.js 20 或更高版本
- MySQL 8

## 配置后端环境变量

后端从 `service/EmploymentServer/.env` 读取数据库连接信息。`.env` 已加入 Git 忽略规则，不会上传到远程仓库。

首次克隆项目后，进入后端目录并复制示例文件：

```bash
cd service/EmploymentServer
cp .env.example .env
```

然后编辑 `.env`：

```dotenv
DB_URL=jdbc:mysql://localhost:3306/example
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
ARK_API_KEY=你的AI服务密钥
JWT_SECRET=随机生成的Base64签名密钥
```

请把数据库地址、数据库名、用户名和密码替换为本机的实际信息。不要提交 `.env` 文件或真实密码。

## 启动后端

必须在 `service/EmploymentServer` 目录中启动，以便 Spring Boot 找到同目录下的 `.env`：

```bash
cd service/EmploymentServer
mvn spring-boot:run
```

后端默认运行在 `http://localhost:5050`。

## 启动管理端

```bash
cd admin/graduate-employment-admin
npm install
npm run dev
```

## 启动用户端

```bash
cd client/graduate-employment-user
npm install
npm run dev
```

终端会显示前端的本地访问地址。

## 注意事项

- `.env.example` 只用于说明配置格式，可以提交到 Git。
- `.env` 保存本机真实配置，已经被 `.gitignore` 排除。
- 修改 `.env` 后需要重新启动后端服务。

JWT 签名密钥可使用 `openssl rand -base64 48` 生成，保存到后端 `.env` 的 `JWT_SECRET`。测试使用独立测试密钥，不会读取真实签名密钥。
