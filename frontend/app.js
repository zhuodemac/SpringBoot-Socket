const isLocalFile = window.location.protocol === "file:";
const sameOrigin = !isLocalFile && (window.location.port === "" || window.location.port === "80" || window.location.port === "3000");
const API_BASE = sameOrigin ? "" : "http://localhost:8080";
const WS_URL = sameOrigin
    ? `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws/chat`
    : "ws://localhost:8080/ws/chat";

const messagesEl = document.getElementById("messages");
const form = document.getElementById("chat-form");
const nicknameEl = document.getElementById("nickname");
const contentEl = document.getElementById("content");
const wsStatusEl = document.getElementById("ws-status");
const tcpCountEl = document.getElementById("tcp-count");
const wsCountEl = document.getElementById("ws-count");
const poolStatsEl = document.getElementById("pool-stats");
const threadListEl = document.getElementById("thread-list");

nicknameEl.value = `user-${Math.floor(Math.random() * 1000)}`;

function renderMessage(message) {
    const item = document.createElement("div");
    item.className = `message${message.sender === "system" ? " system" : ""}`;
    const time = new Date(message.timestamp).toLocaleTimeString();
    item.innerHTML = `
        <div class="meta">${time} · ${message.sender} · ${message.channel} · ${message.threadName}</div>
        <div>${escapeHtml(message.content)}</div>
    `;
    messagesEl.appendChild(item);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function escapeHtml(text) {
    return text.replace(/[&<>"']/g, (char) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[char]));
}

async function loadHistory() {
    const response = await fetch(`${API_BASE}/api/messages`);
    const messages = await response.json();
    messagesEl.innerHTML = "";
    messages.forEach(renderMessage);
}

function connectWebSocket() {
    const socket = new WebSocket(WS_URL);
    socket.addEventListener("open", () => {
        wsStatusEl.textContent = "WebSocket 已连接";
        wsStatusEl.classList.add("on");
        wsStatusEl.classList.remove("off");
    });
    socket.addEventListener("close", () => {
        wsStatusEl.textContent = "WebSocket 已断开，3 秒后重连";
        wsStatusEl.classList.add("off");
        wsStatusEl.classList.remove("on");
        setTimeout(connectWebSocket, 3000);
    });
    socket.addEventListener("message", (event) => {
        renderMessage(JSON.parse(event.data));
    });
    form.onsubmit = (event) => {
        event.preventDefault();
        const payload = {
            sender: nicknameEl.value.trim(),
            content: contentEl.value.trim()
        };
        if (!payload.sender || !payload.content) {
            return;
        }
        socket.send(JSON.stringify(payload));
        contentEl.value = "";
    };
}

async function refreshStatus() {
    const [statusRes, threadRes] = await Promise.all([
        fetch(`${API_BASE}/api/status`),
        fetch(`${API_BASE}/api/threads`)
    ]);
    const status = await statusRes.json();
    const threads = await threadRes.json();
    tcpCountEl.textContent = `TCP 客户端 ${status.tcpClients}`;
    wsCountEl.textContent = `浏览器会话 ${status.webSessions}`;

    poolStatsEl.innerHTML = Object.entries(threads.pool)
        .map(([key, value]) => `<dt>${key}</dt><dd>${value}</dd>`)
        .join("");
    threadListEl.innerHTML = threads.threads
        .map((thread) => `<li>${thread.name} · ${thread.state}</li>`)
        .join("");
}

loadHistory().catch(console.error);
connectWebSocket();
refreshStatus().catch(console.error);
setInterval(() => refreshStatus().catch(console.error), 2000);
