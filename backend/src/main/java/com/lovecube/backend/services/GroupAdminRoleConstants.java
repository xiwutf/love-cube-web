package com.lovecube.backend.services;

public final class GroupAdminRoleConstants {
    private GroupAdminRoleConstants() {}

    public static final String OWNER    = "OWNER";
    public static final String ADMIN    = "ADMIN";
    public static final String REVIEWER = "REVIEWER";

    /** 旧值兼容 */
    private static final String LEGACY = "GROUP_OWNER";

    /** 标准化角色，null / "" / GROUP_OWNER → OWNER */
    public static String normalize(String role) {
        if (role == null || role.isBlank() || LEGACY.equals(role)) return OWNER;
        return role;
    }

    public static String displayName(String role) {
        return switch (normalize(role)) {
            case ADMIN    -> "副管理员";
            case REVIEWER -> "审核员";
            default       -> "团长";
        };
    }

    public static boolean isOwner(String role)    { return OWNER.equals(normalize(role)); }
    public static boolean canManage(String role)  { return isOwner(role) || ADMIN.equals(normalize(role)); }
    public static boolean canReview(String role)  { return canManage(role) || REVIEWER.equals(normalize(role)); }
}
