package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisCacheHelper;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisCacheHelper redisCacheHelper;
    /**
     * 根据id查询商铺信息，使用缓存
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result queryById(Long id) {

        Shop shop = redisCacheHelper.querWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.MINUTES, LOCK_SHOP_KEY);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }

        return Result.ok(shop);
//        // 先从Redis中根据id查商铺信息
//        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
//        // 如果查询到的数据不为空(查询到了)，则直接转换为Shop类型返回
//        if(StrUtil.isNotBlank(shopJson)){
//            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            System.out.println("从Redis中获取商铺信息");
//            return Result.ok(shop);
//        }
//        // 如果Redis中没有商铺信息，则从数据库中查找
//        Shop shop = getById(id);
//        // 查不到的话就返回一个空或则报错信息都可以
//
//        // 查不到的话则将空字符串写入Redis中
//        if(shop == null){
//            // 这里的常量值为两分钟
//            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
//            return Result.fail("商铺不存在!!!");
//        }
//        // 查到了则转换为json字符串
//        String jsonStr = JSONUtil.toJsonStr(shop);
//        // 并且存入到Redis中，并且设置过期时间
//        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, jsonStr);
//        stringRedisTemplate.expire(CACHE_SHOP_KEY + id, CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        // 最终将查询到的商铺信息返回给前端
//        return Result.ok(shop);
    }

    @Override
    public Result update(Shop shop) {
        // 先校验id是否为空
        if(shop.getId() == null){
            return Result.fail("店铺id不能为空!!");
        }

        // 先修改数据库
        updateById(shop);
        // 再删除Redis中的缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    @Override
    public void saveShop2Redis(Long id, Long expireSeconds) {
        Shop shop = getById(id);
        RedisData<Object> redisData = new RedisData<>();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(redisData));
    }
}
