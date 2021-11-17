package io.volvox.td;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TdObjectJsonSerializer {

    private final ObjectMapper objectMapper;

    static {
        try {
            Init.start();
        } catch (CantLoadLibrary e) {
            throw new RuntimeException(e);
        }
    }

    public TdObjectJsonSerializer() {
        var objectMapper = new ObjectMapper();
        var validator = BasicPolymorphicTypeValidator.builder();
        // Iterate TdApi inner classes
        for (Class<?> declaredClass : TdApi.class.getDeclaredClasses()) {
            // Register only TDLib objects
            if (TdApi.Object.class.isAssignableFrom(declaredClass)) {
                if (Modifier.isAbstract(declaredClass.getModifiers())) {
                    // Register abstract base type

                    objectMapper.addMixIn(declaredClass, AbstractTypeMixIn.class);
                    validator.allowIfBaseType(declaredClass);
                } else {
                    // Register named subtype

                    validator.allowIfSubType(declaredClass);
                    objectMapper.registerSubtypes(new NamedType(declaredClass, declaredClass.getSimpleName()));
                }
            }
        }
        this.objectMapper = objectMapper;
    }

    public TdApi.Object deserialize(InputStream json) {
        try {
            return objectMapper.readValue(json, TdApi.Object.class);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public String serialize(TdApi.Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public abstract static class AbstractTypeMixIn {}
}
