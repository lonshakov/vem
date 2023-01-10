package lsa.prototype.vem.engine.spi.meta;

import lsa.prototype.vem.model.context.ChangeRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface History {
    Class<?> request();
    Class<?> unit();
}
