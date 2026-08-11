import { useState } from 'react';

/**
 * 结构树组件 —— 递归渲染 JSON schema 树
 *
 * @param {Array} nodes     ExtractTestResult.schema 的根节点数组
 * @param {string} selected 当前选中的 path（用于高亮）
 * @param {function} onSelect 点击叶子节点时的回调，传入 path
 * @param {number} depth     递归深度，控制缩进
 */
export default function SchemaTree({ nodes, selected = null, onSelect, depth = 0 }) {
  if (!nodes || nodes.length === 0) {
    return <div className="text-muted text-sm" style={{ padding: '8px 0' }}>无结构数据</div>;
  }

  return (
    <div style={{ paddingLeft: depth === 0 ? 0 : depth * 16 }}>
      {nodes.map((node, i) => (
        <SchemaNode
          key={i}
          node={node}
          selected={selected}
          onSelect={onSelect}
          depth={depth}
        />
      ))}
    </div>
  );
}

function SchemaNode({ node, selected, onSelect, depth }) {
  const [expanded, setExpanded] = useState(depth < 2); // 默认展开前两层
  const isLeaf = !node.children || node.children.length === 0;
  const isSelected = selected === node.path;
  const isContainer = node.type === 'object' || node.type === 'array';

  const typeColor = {
    string: 'var(--accent-cyan)',
    number: 'var(--accent-green)',
    decimal: 'var(--accent-green)',
    boolean: 'var(--accent-orange)',
    object: 'var(--accent-purple)',
    array: 'var(--accent-blue)',
    null: 'var(--text-muted)',
    array_item: 'var(--text-muted)',
  };

  const typeLabel = {
    string: '字符串',
    number: '数字',
    decimal: '小数',
    boolean: '布尔',
    object: '对象',
    array: '数组',
    null: '空',
  };

  const sampleStr = formatSample(node.sample);

  return (
    <div>
      <div
        onClick={() => {
          if (isContainer) {
            setExpanded(v => !v);
          } else if (onSelect) {
            onSelect(node.path);
          }
        }}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 6,
          padding: '3px 8px',
          cursor: isContainer ? 'pointer' : 'pointer',
          borderRadius: 6,
          background: isSelected
            ? 'rgba(34,211,238,0.15)'
            : 'transparent',
          border: isSelected
            ? '1px solid rgba(34,211,238,0.4)'
            : '1px solid transparent',
          transition: 'all 0.15s',
          fontFamily: 'var(--font-mono)',
          fontSize: 12,
        }}
        onMouseEnter={e => {
          if (!isSelected) e.currentTarget.style.background = 'var(--bg-card-hover)';
        }}
        onMouseLeave={e => {
          if (!isSelected) e.currentTarget.style.background = 'transparent';
        }}
      >
        {/* 展开/折叠箭头 */}
        <span style={{ width: 14, textAlign: 'center', color: 'var(--text-muted)', flexShrink: 0 }}>
          {isContainer ? (expanded ? '▾' : '▸') : '·'}
        </span>

        {/* 路径 */}
        <span style={{ color: 'var(--text-primary)', fontWeight: 600, wordBreak: 'break-all' }}>
          {node.path}
        </span>

        {/* 类型标签 */}
        <span
          className="tag"
          style={{
            fontSize: 10,
            padding: '1px 6px',
            backgroundColor: `${typeColor[node.type] || 'var(--accent-dim)' || 'rgba(100,100,100,0.1)'}`,
            color: typeColor[node.type] || 'var(--text-muted)',
            borderColor: `${typeColor[node.type] || 'var(--border-dim)'}`,
            flexShrink: 0,
          }}
        >
          {typeLabel[node.type] || node.type}
          {node.size && ` ×${node.size}`}
        </span>

        {/* 样本值 */}
        {sampleStr && isLeaf && (
          <span style={{ color: 'var(--text-muted)', fontSize: 11, marginLeft: 4, flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            = {sampleStr}
          </span>
        )}
      </div>

      {/* 子节点 */}
      {isContainer && expanded && node.children && (
        <SchemaTree nodes={node.children} selected={selected} onSelect={onSelect} depth={depth + 1} />
      )}
    </div>
  );
}

function formatSample(value) {
  if (value === null || value === undefined) return '';
  if (typeof value === 'string') return `"${value}"`;
  if (typeof value === 'boolean') return value ? 'true' : 'false';
  return String(value);
}
