# Socket 线程前后端分离聊天室

Spring Boot + Maven 后端，静态前端分离部署。后端同时提供：

- REST API
- WebSocket（浏览器实时通信）
- Java `ServerSocket` + 线程池（TCP Socket）

## 项目结构

```
backend/     Spring Boot Maven 服务
frontend/    Nginx 托管的静态页面
docker-compose.yml
```

## 本地启动后端

需要 JDK 17+。后端已包含 Maven Wrapper，不必单独安装 Maven。

```bash
cd backend
./mvnw spring-boot:run
```

- HTTP / WebSocket: `http://localhost:8080`
- TCP Socket: `localhost:19090`

前端可直接用浏览器打开 `frontend/index.html`（页面会连接 `localhost:8080`），或用任意静态服务器。

## Docker 启动

```bash
docker compose up --build
```

- 前端：http://localhost:3000
- 后端 REST：http://localhost:8080/api/status
- TCP：`nc localhost 19090`

## 接口

- `GET /api/messages` 历史消息
- `POST /api/messages` HTTP 发消息
- `GET /api/status` 连接数
- `GET /api/threads` 线程池与相关线程
- `WS /ws/chat` 浏览器实时通道

TCP 客户端每行一条文本，输入 `quit` 退出。Web 端与 TCP 端消息会互相广播。
