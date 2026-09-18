package duribun.be.common;

import java.lang.reflect.Field;

public final class TestEntityUtils {

    private TestEntityUtils() {}

    public static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
