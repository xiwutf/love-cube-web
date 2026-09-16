package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class HomeConfigMappingTest {

    @Test
    void configValueMustStayLongTextAndMustNotUseLob() throws NoSuchFieldException {
        Field field = HomeConfig.class.getDeclaredField("configValue");
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.columnDefinition()).isEqualToIgnoringCase("LONGTEXT");
        assertThat(field.getAnnotation(Lob.class)).isNull();
    }
}
