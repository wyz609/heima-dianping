package com.hmdp.constant;

/**
 * Class name: RedisCOnstants
 * Package: com.hmdp.constant
 * Description:
 *
 * @Create: 2025/5/15 21:06
 * @Author: jay
 * @Version: 1.0
 */

public class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
    // Redis中缓存所有店铺类型信息的List集合的key
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type";


    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";

}
