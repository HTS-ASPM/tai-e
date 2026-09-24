import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// A class literal of void in an annotation compiles to the class_info "V"
// (JVM Spec 4.7.16.1), which is not a field descriptor.
@VoidClassLiteral.Returns(void.class)
public class VoidClassLiteral {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Returns {
        Class<?> value();
    }
}
