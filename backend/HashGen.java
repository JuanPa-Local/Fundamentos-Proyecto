public class HashGen {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.springframework.data.redis.serializer.RedisSerializer");
        for (java.lang.reflect.Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
        }
    }
}
