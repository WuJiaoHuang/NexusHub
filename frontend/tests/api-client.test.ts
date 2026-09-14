import { describe, expect, it } from 'vitest'
import { ApiError } from '../src/api/error'
import { unwrapResult } from '../src/api/result'

describe('unwrapResult', () => {
  it('returns data when result is successful', () => {
    expect(unwrapResult({ success: true, errorMsg: null, data: 'token', total: null })).toBe('token')
  })

  it('throws backend message when result fails', () => {
    expect(() => unwrapResult({ success: false, errorMsg: '验证码错误', data: null, total: null })).toThrow(ApiError)
  })
})
