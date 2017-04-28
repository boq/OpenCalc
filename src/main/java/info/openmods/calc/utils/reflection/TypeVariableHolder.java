package info.openmods.calc.utils.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface TypeVariableHolder {

	public static class UseDeclaringType {}

	public Class<?> value() default UseDeclaringType.class;

}
