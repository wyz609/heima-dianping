package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.hmdp.constant.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    // 注入Redis操作对象
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        // 先从Redis中查询，这里的常量值是固定前缀+店铺id
        List<String> shopTypes = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 如果不为空(查询到了)，则转为ShopType类型返回
        if(!shopTypes.isEmpty()){
            List<ShopType> tmp = new ArrayList<>();
            for(String types :shopTypes ){
                ShopType shopType = JSONUtil.toBean(types, ShopType.class);
                tmp.add(shopType);
            }
            return Result.ok(tmp);
        }
        // 如果为空，则从数据库中查询
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        if (shopTypeList == null){
            return Result.fail("商铺类型不存在!!!");
        }
        // 将从数据库查到的数据转为json字符串。存入Redis中
        for(ShopType shopType : shopTypeList ){
            String jsonStr = JSONUtil.toJsonStr(shopType);
//            stringRedisTemplate.opsForList().leftPush(CACHE_SHOP_TYPE_KEY, jsonStr);
            shopTypes.add(jsonStr);
        }
        stringRedisTemplate.opsForList().leftPushAll(CACHE_SHOP_TYPE_KEY, shopTypes);
        // 最终把查询到的商铺分类信息返回给前端
        System.out.println("shopTypeList = "+shopTypeList);
        return Result.ok(shopTypeList);
    }

    // Stream流的方式
//    @Override
//    public Result queryList() {
//        // 先从Redis中查，这里的常量值是固定前缀 + 店铺id
//        List<String> shopTypes =
//                stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
//        // 如果不为空（查询到了），则转为ShopType类型直接返回
//        if (!shopTypes.isEmpty()) {
//            List<ShopType> tmp = shopTypes.stream().map(type -> JSONUtil.toBean(type, ShopType.class))
//                    .collect(Collectors.toList());
//            return Result.ok(tmp);
//        }
//        // 否则去数据库中查
//        List<ShopType> tmp = query().orderByAsc("sort").list();
//        if (tmp == null){
//            return Result.fail("店铺类型不存在！！");
//        }
//        // 查到了转为json字符串，存入redis
//        shopTypes = tmp.stream().map(type -> JSONUtil.toJsonStr(type))
//                .collect(Collectors.toList());
//        stringRedisTemplate.opsForList().leftPushAll(CACHE_SHOP_TYPE_KEY,shopTypes);
//        // 最终把查询到的商户分类信息返回给前端
//        return Result.ok(tmp);
//    }
}
