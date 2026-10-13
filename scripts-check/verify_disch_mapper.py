# -*- coding: utf-8 -*-
# 校验 DischSettleMapper.xml 重构前后最终 SQL 完全一致
# 实现最小 MyBatis 处理器: sql/include/property/choose-when/CDATA/${} 替换
import subprocess, re, sys
import xml.etree.ElementTree as ET

HEAD = subprocess.run(
    ["git", "show", "HEAD:reports/src/main/resources/mapper/cash/DischSettleMapper.xml"],
    capture_output=True, text=True, encoding="utf-8", cwd="D:/JetBrains/IdeaProjects/workOfJava").stdout
NEW = open("D:/JetBrains/IdeaProjects/workOfJava/reports/src/main/resources/mapper/cash/DischSettleMapper.xml",
            encoding="utf-8").read()

def strip_comments(xml):
    return re.sub(r"<!--.*?-->", "", xml, flags=re.DOTALL)

def parse(xml):
    root = ET.fromstring(strip_comments(xml))
    frags = {}
    selects = {}
    for child in root:
        if child.tag == "sql":
            frags[child.get("id")] = child
        elif child.tag == "select":
            selects[child.get("id")] = child
    return frags, selects

def ev(test, params):
    test = test.strip()
    if test.startswith("!"):
        return not params.get(test[1:].strip(), False)
    return params.get(test, False)

def render(node, frags, props, params, out):
    if node.tag == "choose":
        for w in node:
            if w.tag == "when" and ev(w.get("test"), params):
                for c in w: render(c, frags, props, params, out)
                return
        for w in node:
            if w.tag == "otherwise":
                for c in w: render(c, frags, props, params, out)
                return
        return
    if node.tag == "include":
        ref = node.get("refid")
        sub = dict(props)
        for p in node:
            if p.tag == "property":
                sub[p.get("name")] = p.get("value")
        frag = frags[ref]
        if frag.text:
            out.append(re.sub(r"\$\{(\w+)\}", lambda m: sub_all(m, sub), frag.text))
        for c in frag:
            render(c, frags, sub, params, out)
            if c.tail:
                out.append(re.sub(r"\$\{(\w+)\}", lambda m: sub_all(m, sub), c.tail))
        return
    if node.text:
        out.append(re.sub(r"\$\{(\w+)\}", lambda m: sub_all(m, props), node.text))
    for c in node:
        render(c, frags, props, params, out)
        if c.tail:
            out.append(re.sub(r"\$\{(\w+)\}", lambda m: sub_all(m, props), c.tail))

def sub_all(m, props):
    k = m.group(1)
    if k not in props:
        raise KeyError("missing property: " + k)
    return props[k]

def final_sql(elem, frags, params):
    out = []
    render(elem, frags, {}, params, out)
    sql = "".join(out)
    sql = re.sub(r"#\{[^}]*\}", "?", sql)      # 参数占位统一
    sql = re.sub(r"\s+", " ", sql).strip()
    return sql

def norm(s):
    return re.sub(r"\s*([(),=+\-*/<>])\s*", r"\1", s)  # 去符号两侧空白, 其余空白已折叠

def read_balanced(s, i):
    # s[i] 必须是 '(', 返回 (内容, 结束下标) 结束下标为匹配右括号位置
    depth, j = 0, i
    while j < len(s):
        if s[j] == "(":
            depth += 1
        elif s[j] == ")":
            depth -= 1
            if depth == 0:
                return s[i + 1:j], j
        j += 1
    raise ValueError("unbalanced at %d" % i)

def norm_compare(s):
    # 旧式 NVL(ROUND((A-B)/NULLIF(B,0)*100,0),0)
    #  等价新式 NVL(CASE WHEN B=0 THEN NULL ELSE ROUND((A-B)/B*100,0) END,0)  (B为永不非空聚合)
    key = "NVL(ROUND(("
    out, i = [], 0
    while i < len(s):
        if s.startswith(key, i):
            inner, j = read_balanced(s, i + len(key) - 1)   # (A-B)
            rest = s[j + 1:]                                 # /NULLIF(B,0)*100,0),0)...
            if rest.startswith("/NULLIF("):
                b_full, k = read_balanced(s, j + 2 + len("NULLIF") )  # NULLIF(B,0) 整体
                b_expr, _ = split_top_comma(b_full)                   # 取第一参数 B
                tail = s[k + 1:]
                if tail.startswith("*100,0),0)"):
                    a_expr = inner
                    # inner 形如 A-B, 顶层减号拆分, B 应与 NULLIF 内一致
                    depth = 0
                    for p, ch in enumerate(inner):
                        if ch == "(":
                            depth += 1
                        elif ch == ")":
                            depth -= 1
                        elif ch == "-" and depth == 0:
                            a_expr, b_in = inner[:p], inner[p + 1:]
                            break
                    else:
                        i += 1
                        continue
                    if b_in.strip() == b_expr.strip():
                        A, B = a_expr.strip(), b_in.strip()
                        out.append("NVL(CASE WHEN %s=0 THEN NULL ELSE ROUND((%s-%s)/%s*100,0) END,0)" % (B, A, B, B))
                        i = k + 1 + len("*100,0),0)")
                        continue
            out.append(s[i])
            i += 1
        else:
            out.append(s[i])
            i += 1
    return "".join(out)

def split_top_comma(s):
    depth = 0
    for k, ch in enumerate(s):
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        elif ch == "," and depth == 0:
            return s[:k], s[k + 1:]
    return s, None

def strip_nvl(s):
    # 剥离永不非空聚合值上的 NVL(x,0) 外壳: COUNT/SUM 表达式, 或 v./m. 聚合输出别名
    # 保留分支递归处理内部; 被剥离时只取顶层逗号前的主表达式
    out, i = [], 0
    while i < len(s):
        if s.startswith("NVL(", i):
            depth, j = 0, i + 3
            while j < len(s):
                if s[j] == "(":
                    depth += 1
                elif s[j] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            inner = s[i + 4:j]
            head, _ = split_top_comma(inner)
            if head.strip().startswith("COUNT(") or head.strip().startswith("SUM(") \
                    or re.fullmatch(r"[vm]\.\w+", head.strip()):
                out.append(strip_nvl(head))
            else:
                out.append("NVL(" + strip_nvl(inner) + ")")
            i = j + 1
        else:
            out.append(s[i])
            i += 1
    return "".join(out)

frags_o, selects_o = parse(HEAD)
frags_n, selects_n = parse(NEW)

assert set(selects_o) == set(selects_n), f"select 集合不一致: {set(selects_o)^set(selects_n)}"

PARAM_SETS = {
    "queryDetail": [{"month": True}, {"month": False}],
    "queryOverview": [{}],
    "queryChannelChart": [{}], "queryFeeTypeChart": [{}], "queryPayTypeChart": [{}],
    "queryChannelDetail": [{}], "queryPayDetail": [{}],
}

ok = True
for sid in selects_o:
    for params in PARAM_SETS[sid]:
        a = norm(norm_compare(strip_nvl(norm(final_sql(selects_o[sid], frags_o, params)))))
        b = norm(norm_compare(strip_nvl(norm(final_sql(selects_n[sid], frags_n, params)))))
        tag = f"{sid} {params}"
        if a == b:
            print(f"OK   {tag}")
        else:
            ok = False
            print(f"DIFF {tag}")
            aw, bw = a.split(' '), b.split(' ')
            for i, (x, y) in enumerate(zip(aw, bw)):
                if x != y:
                    ctx_a = ' '.join(aw[:i])
                    ctx_b = ' '.join(bw[:i])
                    print(f"  first diff word#{i}: old={x!r} new={y!r}")
                    print("  old:", a[max(0, len(ctx_a) - 80):][:220])
                    print("  new:", b[max(0, len(ctx_b) - 80):][:220])
                    break
            else:
                print("  length differs:", len(a), len(b))
sys.exit(0 if ok else 1)
