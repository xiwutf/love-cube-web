package com.lovecube.backend.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/** 序列化时把 localhost /uploads 收成浏览器可访问的 /admin/uploads。 */
public class PublicUploadUrlSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(UploadUrlSupport.toPublicApiUrl(value));
    }
}
