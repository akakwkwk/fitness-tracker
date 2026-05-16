# FitTracker Pro - 个人健身训练与饮食记录平台

一个现代化、企业级的全栈个人健身训练与饮食记录网站。

## 技术栈

### 后端

- Java 17 + Spring Boot 3.2
- MyBatis Plus 3.5
- MySQL 8.0
- Redis 7.x
- Spring Security + JWT
- Knife4j (Swagger)

### 前端

- Vue 3 + Composition API
- Vite 5
- Element Plus
- Pinia
- ECharts 5
- Axios

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.x+
- Maven 3.8+

### 1. 数据库初始化

```bash
# 登录MySQL并执行初始化脚本
mysql -u root -p < backend/sql/init.sql
```

### 2. 启动后端

```bash
cd backend

# 修改数据库配置 (src/main/resources/application.yml)
# 数据库连接: localhost:3306/fitness_tracker
# Redis连接: localhost:6379

# 启动
mvn spring-boot:run
```

后端将运行在 http://localhost:9090/api

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将运行在 http://localhost:3000

### 4. 访问应用

- 前端: http://localhost:3000
- 后端API: http://localhost:9090/api
- 接口文档: http://localhost:9090/api/doc.html

### 5. 演示账号

- 用户名: admin
- 密码: admin123

## 功能特性

- 用户注册/登录 (JWT认证)
- 仪表盘 (今日概览、本周进度、营养趋势)
- 训练记录 (创建、编辑、删除、详情)
- 动作库 (按肌群分类)
- 训练模板 (保存常用训练计划)
- 饮食记录 (按餐次分类、食物搜索)
- 营养追踪 (卡路里、蛋白质、碳水、脂肪)
- 身体数据 (体重、体脂、BMI自动计算)
- 成就系统 (自动解锁)
- 饮水记录
- 睡眠记录
- 数据统计与可视化 (ECharts)
- 响应式设计
- 深色模式

## 项目结构

```
fitness-tracker/
├── backend/                    # 后端Spring Boot项目
│   ├── pom.xml
│   ├── sql/                    # 数据库脚本
│   └── src/main/java/com/fitness/
│       ├── config/             # 配置类
│       ├── controller/         # 控制器
│       ├── dto/                # 数据传输对象
│       ├── entity/             # 实体类
│       ├── enums/              # 枚举
│       ├── mapper/             # MyBatis Mapper
│       ├── security/           # 安全相关
│       ├── service/            # 服务层
│       ├── utils/              # 工具类
│       └── vo/                 # 视图对象
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── api/                # API请求
│   │   ├── components/         # 组件
│   │   ├── layouts/            # 布局
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   ├── styles/             # 样式
│   │   └── views/              # 页面
│   └── package.json
└── README.md
```

## 接口文档

启动后端后访问: http://localhost:9090/api/doc.html

## Docker部署 (可选)

```bash
# 构建后端
cd backend
mvn clean package -DskipTests
docker build -t fitness-tracker-backend .

# 构建前端
cd frontend
npm run build
docker build -t fitness-tracker-frontend .
```
