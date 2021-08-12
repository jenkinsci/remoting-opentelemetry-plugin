package io.jenkins.plugins.remotingopentelemetry.engine.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD,
})
public @interface ConfigOption {
    String env();
    boolean required() default false;
}
