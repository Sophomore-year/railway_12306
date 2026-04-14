package org.zys.rail_12306.framework.starter.bases;

//访问级别枚举
import lombok.AccessLevel;
//自动生成无参构造函数的注解
import lombok.NoArgsConstructor;
//线程安全的哈希表，用于存储单例对象
import java.util.concurrent.ConcurrentHashMap;
//函数式接口，用于延迟创建对象
import java.util.function.Supplier;

/**
 *单例对象容器
 *
 * @author SUM
 * @date 2026/04/14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Singleton {
    /**
    * 通过静态的 ConcurrentHashMap来管理和获取全局唯一的对象实例。
    * */
    //在类加载时初始化
    //key：字符串标识符
    //value：单例对象
    private static final ConcurrentHashMap<String, Object> SINGLE_OBJECT_POOL = new ConcurrentHashMap();

    /**
     * 根据 key 获取单例对象
     */
    public static <T> T get(String key) {
        Object result = SINGLE_OBJECT_POOL.get(key);
        return result == null ? null : (T) result;
    }

    /**
     * 根据 key 获取单例对象
     * <p>为空时，通过 supplier 构建单例对象并放入容器
     */
    public static <T> T get(String key, Supplier<T> supplier) {
        Object result = SINGLE_OBJECT_POOL.get(key);
        if (result == null && (result = supplier.get()) != null) {
            SINGLE_OBJECT_POOL.put(key, result);
        }
        return result != null ? (T) result : null;
    }

    /**
     * 对象放入容器
     */
    public static void put(Object value) {
        put(value.getClass().getName(), value);
    }

    /**
     * 对象放入容器
     */
    public static void put(String key, Object value) {
        SINGLE_OBJECT_POOL.put(key, value);
    }
}
