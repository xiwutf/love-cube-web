/**
 * 将注册时保存的 User-Agent 转为中文可读摘要（仅用于后台展示，非严格 UA 识别）。
 * @param {string|null|undefined} ua
 * @returns {string}
 */
export function summarizeRegisterUserAgent(ua) {
  if (ua == null || typeof ua !== 'string') return '-'
  const s = ua.trim()
  if (!s) return '-'

  const parts = []
  const mm = s.match(/MicroMessenger\/([\d.]+)/i)
  const isWeChat = Boolean(mm)

  if (mm) {
    const v = mm[1].split('.').slice(0, 3).join('.')
    parts.push(`微信 ${v}`)
  }

  let osDescribed = false
  const ios = s.match(/CPU (?:iPhone |iPad )?OS ([\d_]+)/i)
  if (ios) {
    const ver = ios[1].replace(/_/g, '.')
    const device = /iPad/i.test(s) ? 'iPad' : 'iPhone'
    parts.push(`${device} · iOS ${ver}`)
    osDescribed = true
  }

  if (!osDescribed) {
    const android = s.match(/Android\s+([\d._]+)/i)
    if (android) {
      const ver = android[1].replace(/_/g, '.').split('.').slice(0, 3).join('.')
      const model = s.match(/;\s*([A-Za-z][A-Za-z0-9._+\-]{1,31})\s+Build\//)
      let chunk = `Android ${ver}`
      if (model && !/^linux$/i.test(model[1])) chunk += ` · ${model[1]}`
      parts.push(chunk)
      osDescribed = true
    }
  }

  if (!osDescribed) {
    if (/Windows NT 10\.0/i.test(s)) parts.push('Windows 10/11')
    else if (/Windows NT 6\.3/i.test(s)) parts.push('Windows 8.1')
    else if (/Windows NT 6\.1/i.test(s)) parts.push('Windows 7')
    else {
      const mac = s.match(/Mac OS X ([\d_]+)/i)
      if (mac) parts.push(`macOS ${mac[1].replace(/_/g, '.')}`)
    }
  }

  const net = s.match(/NetType\/(\w+)/i)
  if (net) {
    const key = net[1].toUpperCase()
    const map = { WIFI: 'Wi‑Fi', '4G': '4G', '5G': '5G', '3G': '3G', '2G': '2G' }
    parts.push(map[key] || `${net[1]} 网络`)
  }

  const inWebView = /\bwv\b/i.test(s) || /;\s*wv\)/i.test(s)
  if (inWebView && !isWeChat) {
    parts.push('应用内网页')
  }
  if (!isWeChat && !inWebView) {
    if (/Edg\//i.test(s)) {
      const m = s.match(/Edg\/([\d.]+)/i)
      parts.push(m ? `Edge ${m[1].split('.')[0]}` : 'Edge')
    } else if (/Chrome\/([\d.]+)/i.test(s)) {
      const m = s.match(/Chrome\/([\d.]+)/i)
      parts.push(m ? `Chrome ${m[1].split('.')[0]}` : 'Chrome')
    } else if (/Firefox\/(\d+)/i.test(s)) {
      const m = s.match(/Firefox\/(\d+)/i)
      parts.push(m ? `Firefox ${m[1]}` : 'Firefox')
    }
  }

  if (!parts.length) return '未知环境'
  return parts.join(' · ')
}
