"""ETL 前端预览代理：静态服务 reports-web/ + 转发 /api/** 到本地后端 18089。

用法：python preview_proxy.py [port]   （默认 8080）
使前端与后端同源，便于在预览浏览器中完整验证向导、任务、调试等流程。
"""
import http.server
import os
import sys
import urllib.error
import urllib.request

BACKEND = 'http://localhost:18089'
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'reports-web')


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=ROOT, **kwargs)

    def end_headers(self):
        # 开发预览禁用缓存，避免前端 JS/CSS 改动后浏览器仍用旧文件
        self.send_header('Cache-Control', 'no-store')
        super().end_headers()

    def _proxy(self):
        length = int(self.headers.get('Content-Length') or 0)
        body = self.rfile.read(length) if length else None
        req = urllib.request.Request(BACKEND + self.path, data=body, method=self.command)
        if 'Content-Type' in self.headers:
            req.add_header('Content-Type', self.headers['Content-Type'])
        try:
            with urllib.request.urlopen(req) as resp:
                data = resp.read()
                self.send_response(resp.status)
                self.send_header('Content-Type', resp.headers.get('Content-Type', 'application/json'))
                self.send_header('Content-Length', str(len(data)))
                self.end_headers()
                self.wfile.write(data)
        except urllib.error.HTTPError as e:
            data = e.read()
            self.send_response(e.code)
            self.send_header('Content-Type', e.headers.get('Content-Type', 'application/json'))
            self.send_header('Content-Length', str(len(data)))
            self.end_headers()
            self.wfile.write(data)

    def do_GET(self):
        if self.path.startswith('/api/'):
            self._proxy()
        else:
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/api/'):
            self._proxy()
        else:
            super().do_POST()

    def do_PUT(self):
        if self.path.startswith('/api/'):
            self._proxy()
        else:
            super().do_PUT()

    def do_DELETE(self):
        if self.path.startswith('/api/'):
            self._proxy()
        else:
            super().do_DELETE()


if __name__ == '__main__':
    http.server.ThreadingHTTPServer(('127.0.0.1', PORT), Handler).serve_forever()
