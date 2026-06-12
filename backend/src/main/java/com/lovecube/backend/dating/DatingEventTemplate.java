package com.lovecube.backend.dating;

public final class DatingEventTemplate {

    public static final String TYPE_GENERAL = "GENERAL";
    public static final String TYPE_DATING = "DATING";

    public static final String SIDE_MALE = "MALE";
    public static final String SIDE_FEMALE = "FEMALE";
    public static final String SIDE_OTHER = "OTHER";

    private DatingEventTemplate() {
    }

    public static boolean isDating(String templateType) {
        return TYPE_DATING.equalsIgnoreCase(templateType);
    }
}
