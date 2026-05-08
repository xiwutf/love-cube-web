/** 加入任意团体入口：登记「本团展示姓名」时的提示（与各页一致） */
export const JOIN_MEMBER_REAL_NAME_PROMPT =
  '请输入真实姓名（仅在你所在的团体内向其他成员展示，不会出现在公开主页）'

/** 列表/详情内提醒条文案 */
export const MEMBER_DISPLAY_PANEL_HINT =
  '为方便成员互相认识，请补全你在本团的展示姓名（仅本团成员可见，不会出现在公开主页）。'

/** 审核制团体的申请说明输入 */
export const AUDIT_JOIN_MESSAGE_PROMPT = '请输入申请验证信息（必填）'

/** @param {boolean} isUpdate 是否已有姓名、执行修改 */
export function supplementMemberDisplayTitle(isUpdate) {
  return isUpdate
    ? '修改你在本团的展示姓名（仅本团成员可见）'
    : '补全你在本团的展示姓名（仅本团成员可见）'
}

/** 与其它入口一致：未填写时的错误提示 */
export const ERR_EMPTY_MEMBER_REAL_NAME = '请填写真实姓名后再加入'

/** 审核制申请说明未填（与各页一致） */
export const ERR_EMPTY_AUDIT_JOIN_MESSAGE = '请填写申请验证信息'

/** 补丁/补填为空 */
export const ERR_EMPTY_DISPLAY_NAME_PATCH = '请填写姓名'
