/** 与后端 {@code UserController#activateFellowship} 对齐的开通门禁（年龄、婚姻、头像、生活照） */

export const FELLOWSHIP_ACTIVATION_HINT =
  '开通联谊前须填写有效年龄、选择婚姻状况、上传头像，并保存至少一张生活照。'

export const FELLOWSHIP_MARITAL_FOR_ACTIVATION = ['单身', '已婚', '离异']

/**
 * @param {object} snapshot
 * @param {unknown} snapshot.age
 * @param {unknown} snapshot.maritalStatus
 * @param {unknown} [snapshot.avatarUrl]
 * @param {unknown} [snapshot.avatar]
 * @param {unknown} [snapshot.photos]
 * @param {number} [snapshot.photoCount] 生活照数量（与 photos 二选一均可）
 * @returns {{ ok: boolean, missingKeys: string[], missingLabels: string[] }}
 */
export function fellowshipActivationReadiness(snapshot) {
  if (!snapshot || typeof snapshot !== 'object') {
    return {
      ok: false,
      missingKeys: ['age', 'maritalStatus', 'avatar', 'lifePhotos'],
      missingLabels: ['年龄', '婚姻状况', '头像', '生活照']
    }
  }

  const missingKeys = []
  const missingLabels = []

  const age = Number(snapshot.age)
  if (!Number.isInteger(age) || age <= 0) {
    missingKeys.push('age')
    missingLabels.push('年龄')
  }

  const marital = String(snapshot.maritalStatus ?? '').trim()
  if (!FELLOWSHIP_MARITAL_FOR_ACTIVATION.includes(marital)) {
    missingKeys.push('maritalStatus')
    missingLabels.push('婚姻状况')
  }

  const avatarRaw = snapshot.avatarUrl ?? snapshot.avatar ?? ''
  const avatar = String(avatarRaw == null ? '' : avatarRaw).trim()
  if (!avatar) {
    missingKeys.push('avatar')
    missingLabels.push('头像')
  }

  let photoCount =
    snapshot.photoCount != null && snapshot.photoCount !== ''
      ? Number(snapshot.photoCount)
      : NaN
  if (Number.isNaN(photoCount)) {
    const photos = snapshot.photos
    photoCount = Array.isArray(photos) ? photos.filter((u) => u && String(u).trim()).length : 0
  }
  if (!(Number.isFinite(photoCount) && photoCount >= 1)) {
    missingKeys.push('lifePhotos')
    missingLabels.push('生活照')
  }

  return {
    ok: missingKeys.length === 0,
    missingKeys,
    missingLabels
  }
}
