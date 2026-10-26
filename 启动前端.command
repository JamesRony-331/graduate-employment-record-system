#!/bin/bash
# macOS 可双击运行；终端运行：./启动前端.command
# 自定义端口：ADMIN_PORT=5186 CLIENT_PORT=5183 ./启动前端.command
set -e
PROJECT_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"
if ! command -v node >/dev/null 2>&1; then
  echo '未找到 Node.js，请安装 Node.js 后重新运行。'
  exit 1
fi
exec node --input-type=module - "$PROJECT_DIR" <<'NODE'
import { spawn } from 'node:child_process';
import { existsSync } from 'node:fs';
import { join } from 'node:path';

const root = process.argv[2];
const services = [
  { name: '管理端', folder: 'admin/graduate-employment-admin', port: process.env.ADMIN_PORT || '5176' },
  { name: '学生端', folder: 'client/graduate-employment-user', port: process.env.CLIENT_PORT || '5173' },
];
for (const service of services) {
  if (!/^\d+$/.test(service.port) || Number(service.port) < 1024 || Number(service.port) > 65535) {
    console.error(`${service.name}端口必须为 1024～65535 的整数。`);
    process.exit(1);
  }
  service.port = String(Number(service.port));
  service.cwd = join(root, service.folder);
  service.entry = join(service.cwd, 'node_modules/vite/bin/vite.js');
  if (!existsSync(service.entry)) {
    console.error(`${service.name}缺少依赖，请先执行：\ncd "${service.cwd}" && npm install`);
    process.exit(1);
  }
}
if (services[0].port === services[1].port) {
  console.error('管理端与学生端不能使用同一个端口。');
  process.exit(1);
}

const children = new Set();
let stopping = false;
let exitCode = 0;
let timer;
function signalGroup(child, signal) {
  if (!child.pid) return;
  try { process.kill(-child.pid, signal); }
  catch (error) { if (error.code !== 'ESRCH') console.error(error.message); }
}
function stop(code = 0) {
  if (stopping) return;
  stopping = true;
  exitCode = code;
  console.log('\n正在停止本次启动的两个前端服务…');
  for (const child of children) signalGroup(child, 'SIGTERM');
  if (!children.size) process.exit(exitCode);
  timer = setTimeout(() => {
    for (const child of children) signalGroup(child, 'SIGKILL');
  }, 4000);
}
process.on('SIGINT', () => stop());
process.on('SIGTERM', () => stop());
process.on('SIGHUP', () => stop());
console.log('同时启动管理端和学生端（后端需单独启动）。');
console.log('按 Ctrl+C 停止两个服务；端口占用会报错，不会结束已有进程。\n');
for (const service of services) {
  console.log(`${service.name}：http://127.0.0.1:${service.port}/`);
  const child = spawn(process.execPath,
    [service.entry, '--host', '127.0.0.1', '--port', service.port, '--strictPort'],
    { cwd: service.cwd, stdio: ['ignore', 'inherit', 'inherit'], detached: true });
  children.add(child);
  child.on('error', error => {
    console.error(`${service.name}启动失败：${error.message}`);
    stop(1);
  });
  child.on('close', code => {
    children.delete(child);
    if (!stopping) {
      console.error(`${service.name}已退出（退出码 ${code ?? 'signal'}），停止另一个服务。`);
      stop(code || 1);
    }
    if (stopping && !children.size) { clearTimeout(timer); process.exit(exitCode); }
  });
}
NODE
