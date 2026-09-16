import assert from 'node:assert/strict'
import test from 'node:test'
import { toBrowserUploadUrl } from './uploadUrl.js'

test('relative /uploads becomes /admin/uploads', () => {
  assert.equal(toBrowserUploadUrl('/uploads/photos/a.jpg'), '/admin/uploads/photos/a.jpg')
})

test('already-prefixed /admin/uploads stays', () => {
  assert.equal(toBrowserUploadUrl('/admin/uploads/avatar/b.png'), '/admin/uploads/avatar/b.png')
})

test('localhost absolute url becomes site-relative /admin/uploads', () => {
  assert.equal(
    toBrowserUploadUrl('http://localhost:8090/admin/uploads/photos/a.jpg'),
    '/admin/uploads/photos/a.jpg'
  )
  assert.equal(
    toBrowserUploadUrl('http://127.0.0.1:8090/uploads/avatar/b.png'),
    '/admin/uploads/avatar/b.png'
  )
})

test('legacy http host with /admin/uploads becomes site-relative', () => {
  assert.equal(
    toBrowserUploadUrl('http://example.com:8090/admin/uploads/photos/a.jpg'),
    '/admin/uploads/photos/a.jpg'
  )
})

test('oss public url stays unchanged', () => {
  const oss = 'https://love-cube-files.oss-cn-beijing.aliyuncs.com/photos/a.jpg'
  assert.equal(toBrowserUploadUrl(oss), oss)
})

