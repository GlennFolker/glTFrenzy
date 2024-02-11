package gltf.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Spec{
    Prop[] value() default {};

    @interface Prop{
        String name();

        Class<?> type();

        boolean required() default false;

        Def def() default @Def("");
    }

    @interface Def{
        String value();

        Class<?>[] args() default {};
    }
}
