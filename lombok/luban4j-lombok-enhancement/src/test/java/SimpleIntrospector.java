import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public  class SimpleIntrospector {
        private SimpleIntrospector() { }

        private static final String GET_METHOD_PREFIX = "get";
        private static final String IS_METHOD_PREFIX = "is";

        // cache to avoid repeated lookups
        private static final Map<Class<?>, SoftReference<List<Method>>> cache =
            Collections.synchronizedMap(
                new WeakHashMap<Class<?>,SoftReference<List<Method>>>());

        /**
         * Returns the list of methods cached for the given class, or {@code null}
         * if not cached.
         */
        private static List<Method> getCachedMethods(Class<?> clazz) {
            // return cached methods if possible
            SoftReference<List<Method>> ref = cache.get(clazz);
            if (ref != null) {
                List<Method> cached = ref.get();
                if (cached != null)
                    return cached;
            }
            return null;
        }

        /**
         * Returns {@code true} if the given method is a "getter" method (where
         * "getter" method is a public method of the form getXXX or "boolean
         * isXXX")
         */
        static boolean isReadMethod(Method method) {
            // ignore static methods
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers))
                return false;

            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            int paramCount = paramTypes.length;

            if (paramCount == 0 && name.length() > 2) {
                // boolean isXXX()
                if (name.startsWith(IS_METHOD_PREFIX))
                    return (method.getReturnType() == boolean.class);
                // getXXX()
                if (name.length() > 3 && name.startsWith(GET_METHOD_PREFIX))
                    return (method.getReturnType() != void.class);
            }
            return false;
        }

        /**
         * Returns the list of "getter" methods for the given class. The list
         * is ordered so that isXXX methods appear before getXXX methods - this
         * is for compatibility with the JavaBeans Introspector.
         */
        static List<Method> getReadMethods(Class<?> clazz) {
            // return cached result if available
            List<Method> cachedResult = getCachedMethods(clazz);
            if (cachedResult != null)
                return cachedResult;

            // get list of public methods, filtering out methods that have
            // been overridden to return a more specific type.
            List<Method> methods =
                    Arrays.stream(clazz.getMethods()).collect(Collectors.toList());
//            methods = MBeanAnalyzer.eliminateCovariantMethods(methods);

            // filter out the non-getter methods
            List<Method> result = new LinkedList<Method>();
            for (Method m: methods) {
                if (isReadMethod(m)) {
                    // favor isXXX over getXXX
                    if (m.getName().startsWith(IS_METHOD_PREFIX)) {
                        result.add(0, m);
                    } else {
                        result.add(m);
                    }
                }
            }

            // add result to cache
            cache.put(clazz, new SoftReference<List<Method>>(result));

            return result;
        }

        /**
         * Returns the "getter" to read the given property from the given class or
         * {@code null} if no method is found.
         */
        static Method getReadMethod(Class<?> clazz, String property) {
            // first character in uppercase (compatibility with JavaBeans)
            property = property.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                property.substring(1);
            String getMethod = GET_METHOD_PREFIX + property;
            String isMethod = IS_METHOD_PREFIX + property;
            for (Method m: getReadMethods(clazz)) {
                String name = m.getName();
                if (name.equals(isMethod) || name.equals(getMethod)) {
                    return m;
                }
            }
            return null;
        }
    }