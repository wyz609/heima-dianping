package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * 根据id查询商铺信息，使用缓存
     * @param id 商铺id
     * @return 商铺详情数据
     */
    Result queryById(Long id);

    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return 无
     */
    Result update(Shop shop);

    /**
     * 根据id查询商铺信息，进行测试逻辑过期
     * @param id 商铺id
     * @param expireSeconds 逻辑过期
     */
    void saveShop2Redis(Long id, Long expireSeconds);

}
