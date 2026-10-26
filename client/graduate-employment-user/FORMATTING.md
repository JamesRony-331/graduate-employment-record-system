# 前端代码格式

在本目录运行：

```sh
npm run format
npm run format:check
```

- 使用两空格缩进，不压缩源码。
- 函数、条件、循环和 CSS 规则的大括号换行（Allman 风格）。
- Vue 模板的多属性标签分行，脚本和样式分别格式化。
- 无大括号的条件、循环语句，其执行语句另起一行。
- 保留 JavaScript 对象表达式、return 和模板字符串所需的语法，避免换行改变返回值。
- 覆盖 src、测试、前端脚本、Vite 配置和 HTML 入口；跳过依赖、构建产物、静态资源和文档。
- 规则在 .jsbeautifyrc，入口为 scripts/format.cjs。不要用 Prettier 重写源码（不支持此大括号风格）。
