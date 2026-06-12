# -*- coding: utf-8 -*-
"""生成 VRD 系统架构图 PNG，供 Word/Markdown 文档嵌入。"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

OUT_DIR = Path(__file__).resolve().parent.parent / "docs" / "images"
OUT_DIR.mkdir(parents=True, exist_ok=True)

COLORS = {
    "bg": "#f5f7fa",
    "white": "#ffffff",
    "text": "#263238",
    "sub": "#607d8b",
    "arrow": "#90a4ae",
    "arrow_up": "#0277bd",
    "present": ("#00897b", "#e0f2f1"),
    "gateway": ("#ef6c00", "#fff3e0"),
    "service": ("#e65100", "#fbe9e7"),
    "data": ("#6a1b9a", "#f3e5f5"),
    "mid": ("#2e7d32", "#e8f5e9"),
    "access": ("#00838f", "#e0f7fa"),
    "edge": ("#1565c0", "#e3f2fd"),
    "nacos": ("#1565c0", "#e3f2fd"),
    "box_border": "#b0bec5",
    "db_border": "#8e24aa",
}


def font(size: int, bold: bool = False):
    candidates = [
        "C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc",
        "C:/Windows/Fonts/simhei.ttf",
        "C:/Windows/Fonts/simsun.ttc",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def text_size(draw, text, fnt):
    bbox = draw.textbbox((0, 0), text, font=fnt)
    return bbox[2] - bbox[0], bbox[3] - bbox[1]


def draw_box(draw, x, y, w, h, title, subtitle=None, fill="#ffffff", border=COLORS["box_border"], key=False):
    if key:
        border = COLORS["gateway"][0]
    draw.rounded_rectangle((x, y, x + w, y + h), radius=8, fill=fill, outline=border, width=2 if key else 1)
    tw, th = text_size(draw, title, font(13, True))
    draw.text((x + (w - tw) / 2, y + 10), title, fill=COLORS["text"], font=font(13, True))
    if subtitle:
        sw, sh = text_size(draw, subtitle, font(10))
        draw.text((x + (w - sw) / 2, y + 10 + th + 4), subtitle, fill=COLORS["sub"], font=font(10))


def draw_db(draw, x, y, w, h, title, subtitle):
    draw.rounded_rectangle((x, y, x + w, y + h), radius=8, fill="#f3e5f5", outline=COLORS["db_border"], width=1)
    tw, th = text_size(draw, title, font(12, True))
    draw.text((x + (w - tw) / 2, y + 8), title, fill="#4a148c", font=font(12, True))
    sw, _ = text_size(draw, subtitle, font(9))
    draw.text((x + (w - sw) / 2, y + 8 + th + 2), subtitle, fill="#ab47bc", font=font(9))


def draw_layer(draw, y, tag, tag_color, panel_color, panel_x, panel_w, panel_h, content_fn):
    tag_w, tag_h = 72, panel_h
    draw.rounded_rectangle((40, y, 40 + tag_w, y + tag_h), radius=6, fill=tag_color)
    lines = tag.split("\n")
    ty = y + (tag_h - len(lines) * 16) / 2
    for line in lines:
        tw, th = text_size(draw, line, font(11, True))
        draw.text((40 + (tag_w - tw) / 2, ty), line, fill="#ffffff", font=font(11, True))
        ty += 16
    draw.rounded_rectangle((panel_x, y, panel_x + panel_w, y + panel_h), radius=8, fill=panel_color, outline=tag_color, width=2)
    content_fn(panel_x, y, panel_w, panel_h)


def draw_arrow(draw, x, y, direction="down", label=None, color=None):
    color = color or COLORS["arrow"]
    if direction == "down":
        draw.line((x, y, x, y + 18), fill=color, width=2)
        draw.polygon([(x, y + 22), (x - 6, y + 14), (x + 6, y + 14)], fill=color)
        ly = y + 26
    else:
        draw.line((x, y + 22, x, y + 4), fill=color, width=2)
        draw.polygon([(x, y), (x - 6, y + 8), (x + 6, y + 8)], fill=color)
        ly = y - 18
    if label:
        lw, lh = text_size(draw, label, font(10))
        draw.text((x + 12, ly), label, fill=color, font=font(10))


def generate_system_architecture():
    W, H = 1100, 1180
    img = Image.new("RGB", (W, H), COLORS["bg"])
    draw = ImageDraw.Draw(img)

    draw.text((40, 20), "车辆远程诊断系统 — 系统总体架构图", fill="#1a237e", font=font(20, True))
    draw.text((40, 52), "管理端自上而下；车端自下而上经接入层进入平台。Nacos 独立侧栏，虚线关联各微服务。", fill=COLORS["sub"], font=font(11))

    panel_x, panel_w = 128, 820
    cy = 90

    def layer1(px, y, pw, ph):
        draw_box(draw, px + 300, y + 14, 220, 52, "frontend", "Vue3 管理端 :3000", key=True)

    draw_layer(draw, cy, "①\n展现层", *COLORS["present"], panel_x, panel_w, 80, layer1)
    cy += 80
    draw_arrow(draw, panel_x + panel_w // 2, cy, "down", "管理端请求下行")
    cy += 36

    def layer2(px, y, pw, ph):
        draw_box(draw, px + 280, y + 14, 260, 52, "service-gateway", "路由转发 · JWT 鉴权 :8080", key=True)

    draw_layer(draw, cy, "②\n网关层", *COLORS["gateway"], panel_x, panel_w, 80, layer2)
    cy += 80
    draw_arrow(draw, panel_x + panel_w // 2, cy, "down", "按服务名 lb:// 转发")
    cy += 36

    services = [
        ("service-auth", ":8081"),
        ("service-vehicle", ":8082"),
        ("service-ecu-log", ":8083"),
        ("service-dbc", ":8084"),
        ("service-signal", ":8085"),
        ("service-access", ":8086"),
    ]

    def layer3(px, y, pw, ph):
        bx, gap = px + 16, 10
        bw = (pw - 32 - gap * 2) // 3
        for i, (name, port) in enumerate(services):
            row, col = i // 3, i % 3
            x = bx + col * (bw + gap)
            yy = y + 12 + row * 58
            is_access = name == "service-access"
            draw_box(draw, x, yy, bw, 48, name, port,
                     border=COLORS["access"][0] if is_access else COLORS["box_border"],
                     key=is_access)

    draw_layer(draw, cy, "③\n服务层", *COLORS["service"], panel_x, panel_w, 130, layer3)
    cy += 130
    draw_arrow(draw, panel_x + panel_w // 2, cy, "down", "读写持久化 / 发布消息")
    cy += 36

    dbs = [("MySQL", "业务分库"), ("Redis", "缓存"), ("ClickHouse", "时序/日志"), ("对象存储", "文件实体")]

    def layer4(px, y, pw, ph):
        bx, gap, bw, bh = px + 40, 14, 150, 46
        for i, (name, desc) in enumerate(dbs):
            x = bx + i * (bw + gap)
            draw_db(draw, x, y + 16, bw, bh, name, desc)

    draw_layer(draw, cy, "④\n数据层", *COLORS["data"], panel_x, panel_w, 80, layer4)
    cy += 80
    draw_arrow(draw, panel_x + panel_w // 2, cy, "down", "异步 / 物联网通道")
    cy += 36

    def layer5(px, y, pw, ph):
        draw_box(draw, px + 140, y + 14, 260, 52, "Kafka", "vehicle-data · vehicle-signals")
        draw_box(draw, px + 440, y + 14, 200, 52, "MQTT Broker", "vehicle/signal/+ · :1883")

    draw_layer(draw, cy, "⑤\n中间件", *COLORS["mid"], panel_x, panel_w, 80, layer5)
    cy += 80
    draw_arrow(draw, panel_x + panel_w // 2, cy, "up", "车端数据上行", COLORS["arrow_up"])
    cy += 36

    def layer6(px, y, pw, ph):
        draw.rounded_rectangle((px + 30, y + 10, px + pw - 30, y + ph - 10), radius=8, outline=COLORS["access"][0], width=1)
        draw_box(draw, px + 40, y + 18, 360, 58, "HTTP 接入",
                 "/ecu-log/vehicle/* · /signal/vehicle/receive", border=COLORS["access"][0])
        draw_box(draw, px + 420, y + 18, 360, 58, "MQTT 接入",
                 "MqttSignalReceiver → Kafka → ClickHouse", border=COLORS["access"][0])

    draw_layer(draw, cy, "⑥\n接入层", *COLORS["access"], panel_x, panel_w, 90, layer6)
    cy += 90
    draw_arrow(draw, panel_x + panel_w // 2, cy, "up", "采集与上报", COLORS["arrow_up"])
    cy += 36

    def layer7(px, y, pw, ph):
        draw_box(draw, px + 180, y + 14, 200, 52, "ECU 控制器", "日志 / 信号采集", key=True)
        draw.text((px + pw // 2 - 8, y + 30), "↔", fill=COLORS["sub"], font=font(18))
        draw_box(draw, px + 440, y + 14, 200, 52, "车载网关", "HTTP / MQTT 上报", key=True)

    draw_layer(draw, cy, "⑦\n车端", *COLORS["edge"], panel_x, panel_w, 80, layer7)

    # Nacos sidebar
    nx, ny, nw, nh = 970, 200, 120, 280
    draw.rounded_rectangle((nx, ny, nx + nw, ny + nh), radius=10, fill=COLORS["white"], outline=COLORS["nacos"][0], width=2)
    draw.line([(nx + 10, ny + 70), (nx + nw - 10, ny + 70)], fill="#90caf9", width=1)
    draw.text((nx + 12, ny + 12), "Nacos", fill=COLORS["nacos"][0], font=font(12, True))
    draw.text((nx + 8, ny + 32), "注册/配置", fill=COLORS["sub"], font=font(9))
    draw.text((nx + 8, ny + 82), "服务发现", fill=COLORS["sub"], font=font(9))
    draw.text((nx + 8, ny + 100), "配置热更新", fill=COLORS["sub"], font=font(9))
    draw.text((nx + 8, ny + 118), "负载均衡", fill=COLORS["sub"], font=font(9))
    for i, yy in enumerate([150, 190, 230]):
        draw.line([(nx + 20, yy), (128, yy + 80 + i * 20)], fill="#90caf9", width=1)

    # Matrix
    my = cy + 100
    draw.text((40, my), "服务与数据归属矩阵", fill=COLORS["sub"], font=font(12, True))
    my += 24
    headers = ["微服务", "MySQL", "Redis", "ClickHouse", "对象存储", "Kafka", "MQTT"]
    col_w = [160, 70, 70, 90, 90, 70, 70]
    rows = [
        ("service-gateway", "", "●", "", "", "", ""),
        ("service-auth", "●", "●", "", "", "", ""),
        ("service-vehicle", "●", "", "", "", "●", ""),
        ("service-ecu-log", "", "", "●", "●", "", ""),
        ("service-dbc", "●", "", "", "●", "", ""),
        ("service-signal", "", "", "●", "", "", ""),
        ("service-access", "", "", "●", "●", "●", "●"),
    ]
    x0 = 40
    for j, h in enumerate(headers):
        x = x0 + sum(col_w[:j])
        draw.rectangle((x, my, x + col_w[j], my + 24), fill="#eceff1", outline="#cfd8dc")
        tw, _ = text_size(draw, h, font(10, True))
        draw.text((x + (col_w[j] - tw) / 2, my + 6), h, fill=COLORS["text"], font=font(10, True))
    for i, row in enumerate(rows):
        ry = my + 24 + i * 24
        for j, cell in enumerate(row):
            x = x0 + sum(col_w[:j])
            fill = "#fafafa" if j == 0 else COLORS["white"]
            draw.rectangle((x, ry, x + col_w[j], ry + 24), fill=fill, outline="#cfd8dc")
            f = font(10, j == 0)
            tw, th = text_size(draw, cell, f)
            draw.text((x + (col_w[j] - tw) / 2, ry + 6), cell, fill="#7b1fa2" if cell == "●" else COLORS["text"], font=f)

    out = OUT_DIR / "system-architecture.png"
    img.save(out, "PNG", dpi=(150, 150))
    print(f"generated: {out}")


def generate_data_flow():
    W, H = 1100, 720
    img = Image.new("RGB", (W, H), COLORS["bg"])
    draw = ImageDraw.Draw(img)
    draw.text((40, 20), "车辆远程诊断系统 — 数据流架构图", fill="#1a237e", font=font(20, True))

    # Management flow (top)
    draw.rounded_rectangle((30, 60, 1070, 310), radius=10, fill="#e3f2fd", outline="#1565c0", width=2)
    draw.text((50, 72), "管理端数据流（下行）", fill="#1565c0", font=font(14, True))
    mgmt = ["浏览器", "frontend\n:3000", "service-gateway\n:8080", "微服务集群", "MySQL / ClickHouse\n/ 对象存储"]
    x = 60
    for i, label in enumerate(mgmt):
        draw_box(draw, x, 120, 170, 60, *label.split("\n", 1) if "\n" in label else (label, None))
        if i < len(mgmt) - 1:
            draw.text((x + 175, 145), "→", fill=COLORS["arrow"], font=font(18))
        x += 195

    # Vehicle flow (bottom)
    draw.rounded_rectangle((30, 340, 1070, 680), radius=10, fill="#e8f5e9", outline="#2e7d32", width=2)
    draw.text((50, 352), "车端数据流（上行）", fill="#2e7d32", font=font(14, True))

    # Signal path
    draw.text((50, 390), "信号链路", fill=COLORS["text"], font=font(12, True))
    sig = ["车端 ECU", "MQTT/HTTP", "service-access", "Kafka", "ClickHouse", "service-signal\n(查询)"]
    x = 60
    for i, label in enumerate(sig):
        parts = label.split("\n", 1)
        draw_box(draw, x, 420, 140, 55, parts[0], parts[1] if len(parts) > 1 else None)
        if i < len(sig) - 1:
            draw.text((x + 145, 440), "→", fill=COLORS["arrow"], font=font(16))
        x += 160

    # Log path
    draw.text((50, 510), "日志链路", fill=COLORS["text"], font=font(12, True))
    log = ["车端 ECU", "HTTP 分片/直传", "service-access", "对象存储", "ClickHouse", "service-ecu-log\n(查询/下载)"]
    x = 60
    for i, label in enumerate(log):
        parts = label.split("\n", 1)
        draw_box(draw, x, 540, 140, 55, parts[0], parts[1] if len(parts) > 1 else None)
        if i < len(log) - 1:
            draw.text((x + 145, 560), "→", fill=COLORS["arrow"], font=font(16))
        x += 160

    # Vehicle sync
    draw.text((50, 620), "车辆同步", fill=COLORS["text"], font=font(12, True))
    sync = ["外部系统", "Kafka vehicle-data", "service-vehicle", "MySQL vrd_vehicle"]
    x = 60
    for i, label in enumerate(sync):
        draw_box(draw, x, 640, 180, 40, label)
        if i < len(sync) - 1:
            draw.text((x + 185, 652), "→", fill=COLORS["arrow"], font=font(16))
        x += 210

    out = OUT_DIR / "data-flow-architecture.png"
    img.save(out, "PNG", dpi=(150, 150))
    print(f"generated: {out}")


def generate_deployment():
    W, H = 1100, 780
    img = Image.new("RGB", (W, H), COLORS["bg"])
    draw = ImageDraw.Draw(img)
    draw.text((40, 20), "车辆远程诊断系统 — 部署架构图", fill="#1a237e", font=font(20, True))
    draw.text((40, 52), "Docker Compose 编排，基础设施与微服务分层部署", fill=COLORS["sub"], font=font(11))

    def section(title, y, color, items, cols=4):
        draw.rounded_rectangle((30, y, 1070, y + 30 + ((len(items) + cols - 1) // cols) * 58), radius=10, fill=color[1], outline=color[0], width=2)
        draw.text((50, y + 10), title, fill=color[0], font=font(13, True))
        bx, by = 50, y + 38
        bw, gap = 230, 16
        for i, (name, port) in enumerate(items):
            col, row = i % cols, i // cols
            x = bx + col * (bw + gap)
            yy = by + row * 58
            draw_box(draw, x, yy, bw, 48, name, port)

    section("基础设施层", 80, COLORS["data"], [
        ("MySQL 8.0", ":3306"), ("Redis 7", ":6379"), ("Zookeeper", ":2181"),
        ("Kafka", ":9092"), ("Mosquitto MQTT", ":1883"), ("Nacos", ":8848"),
        ("ClickHouse", ":8123"), ("Hadoop HDFS", ":9000"),
    ])

    section("微服务层", 300, COLORS["service"], [
        ("service-gateway", ":8080"), ("service-auth", ":8081"), ("service-vehicle", ":8082"),
        ("service-ecu-log", ":8083"), ("service-dbc", ":8084"), ("service-signal", ":8085"),
        ("service-access", ":8086"),
    ], cols=4)

    section("展现层", 520, COLORS["present"], [
        ("frontend (Vue3)", ":3000 → Nginx"),
    ], cols=1)

    # External access
    draw.rounded_rectangle((30, 610, 1070, 750), radius=10, fill="#fff8e1", outline="#f9a825", width=2)
    draw.text((50, 622), "外部访问入口", fill="#f57f17", font=font(13, True))
    entries = [
        ("管理端", "http://localhost:3000"),
        ("API 网关", "http://localhost:8080/api"),
        ("Nacos 控制台", "http://localhost:8848/nacos"),
        ("车端 MQTT", "tcp://localhost:1883"),
    ]
    x = 50
    for title, url in entries:
        draw_box(draw, x, 660, 230, 55, title, url)
        x += 250

    out = OUT_DIR / "deployment-architecture.png"
    img.save(out, "PNG", dpi=(150, 150))
    print(f"generated: {out}")


if __name__ == "__main__":
    generate_system_architecture()
    generate_data_flow()
    generate_deployment()
