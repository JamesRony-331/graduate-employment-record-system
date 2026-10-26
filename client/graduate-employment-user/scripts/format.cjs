const fs = require('node:fs');
const path = require('node:path');
const beautify = require('js-beautify');
const
{
  parse,
  babelParse
} = require('vue/compiler-sfc');
const root = path.resolve(__dirname, '..');
const options = JSON.parse(fs.readFileSync(path.join(root, '.jsbeautifyrc'), 'utf8'));
const check = process.argv.includes('--check');
const extensions = new Set(['.vue', '.js', '.mjs', '.cjs', '.css', '.html']);
const ignored = new Set(['node_modules', 'dist', '.git', 'coverage', 'public', 'docs']);

function files(dir)
{
  return fs.readdirSync(dir,
  {
    withFileTypes: true
  }).flatMap(entry =>
  {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory())
      return ignored.has(entry.name) ? [] : files(file);
    return extensions.has(path.extname(file)) ? [file] : [];
  });
}

// Split unbraced control-flow bodies without changing their scope or AST.
function formatJavaScript(source, settings)
{
  const tree = babelParse(source,
  {
    sourceType: 'unambiguous'
  });
  const positions = new Set();

  function visit(node)
  {
    if (!node || typeof node !== 'object')
      return;
    const bodies = node.type === 'IfStatement' ? [node.consequent, node.alternate] : [
        'ForStatement', 'ForOfStatement', 'ForInStatement', 'WhileStatement', 'DoWhileStatement'
      ]
      .includes(node.type) ? [node.body] : [];
    for (const body of bodies)
    {
      if (body && body.type !== 'BlockStatement' && body.type !== 'IfStatement')
        positions.add(body.start);
    }
    for (const value of Object.values(node))
    {
      if (Array.isArray(value))
        value.forEach(visit);
      else if (value && typeof value === 'object' && value.type)
        visit(value);
    }
  }
  visit(tree);
  for (const offset of [...positions].sort((a, b) => b - a))
  {
    if (source.slice(source.lastIndexOf('\n', offset - 1) + 1, offset).trim())
      source = source.slice(0, offset) + '\n' + source.slice(offset);
  }
  return beautify.js(source, settings);
}

function format(source, ext)
{
  if (ext === '.vue')
  {
    const
    {
      descriptor,
      errors
    } = parse(source);
    if (errors.length)
      throw new Error(String(errors[0]));
    const blocks = [descriptor.script, descriptor.scriptSetup, descriptor.template, ...descriptor
      .styles
    ].filter(Boolean);
    let result = source;
    for (const block of blocks.sort((a, b) => b.loc.start.offset - a.loc.start.offset))
    {
      const formatter = block.type === 'script' ? formatJavaScript : block.type === 'style' ?
        beautify
        .css : beautify.html;
      const content = formatter(block.content.trim(), options).trimEnd();
      result = result.slice(0, block.loc.start.offset) + '\n' + content + '\n' + result.slice(block
        .loc.end.offset);
    }
    return result.trimEnd() + '\n';
  }
  const formatter = ext === '.css' ? beautify.css : ext === '.html' ? beautify.html :
    formatJavaScript;
  return formatter(source, options).trimEnd() + '\n';
}
let changes = 0;
for (const file of files(root))
{
  const source = fs.readFileSync(file, 'utf8');
  const result = format(source, path.extname(file));
  if (source === result)
    continue;
  changes++;
  console.log(path.relative(root, file));
  if (!check)
    fs.writeFileSync(file, result);
}
console.log(check ? changes + ' files need formatting' : changes + ' files formatted');
if (check && changes)
  process.exitCode = 1;
