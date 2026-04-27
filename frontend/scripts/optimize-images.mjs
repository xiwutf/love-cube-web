import fs from 'node:fs/promises'
import path from 'node:path'
import sharp from 'sharp'

const ROOTS = ['public', 'src/assets']
const IMAGE_EXTS = new Set(['.png', '.jpg', '.jpeg'])
const SIZE_LIMIT = 300 * 1024

async function walk(dir) {
  const entries = await fs.readdir(dir, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      files.push(...await walk(full))
      continue
    }
    files.push(full)
  }
  return files
}

async function main() {
  const converted = []
  for (const root of ROOTS) {
    const absRoot = path.resolve(root)
    let files = []
    try {
      files = await walk(absRoot)
    } catch {
      continue
    }
    for (const file of files) {
      const ext = path.extname(file).toLowerCase()
      if (!IMAGE_EXTS.has(ext)) continue
      const stat = await fs.stat(file)
      if (stat.size <= SIZE_LIMIT) continue
      const outFile = file.replace(/\.(png|jpg|jpeg)$/i, '.webp')
      await sharp(file)
        .rotate()
        .webp({ quality: 76, effort: 5 })
        .toFile(outFile)
      const outStat = await fs.stat(outFile)
      converted.push({ file, outFile, oldSize: stat.size, newSize: outStat.size })
    }
  }

  converted.sort((a, b) => b.oldSize - a.oldSize)
  for (const item of converted) {
    const saved = (((item.oldSize - item.newSize) / item.oldSize) * 100).toFixed(1)
    console.log(`${item.file}\t${(item.oldSize / 1024).toFixed(1)}KB -> ${(item.newSize / 1024).toFixed(1)}KB\t-${saved}%`)
  }
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
