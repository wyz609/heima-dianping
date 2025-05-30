package com.hmdp.service.impl;

import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    /**
     * 使用ID唯一生成器进行获取全局唯一ID
     */
    @Resource
    private RedisIdWorker redisIdWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 实现秒杀优惠券的功能
     * @param voucherId 优惠券id
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();

        // 1.查询秒杀券
        queryWrapper.eq(SeckillVoucher::getVoucherId, voucherId);
        SeckillVoucher voucher = seckillVoucherService.getOne(queryWrapper);
        // 2.判断秒杀时间是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始!");
        }
        // 3.判断秒杀时间是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束!");
        }
        // 5.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 尚未开始
            return Result.fail("优惠券被抢光了哦，下次记得手速快些!");
        }
        Long userId = UserHolder.getUser().getId();
        // 创建锁对象
        SimpleRedisLock redisLock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        // 获取锁对象
        boolean isLock = redisLock.tryLock(120);
        // 加锁失败，说明当前用户开朗多个线程进行抢优惠券，但是由于key是SETNX，所以不能创建key，得等key的TTL到期或释放锁(删除key)
        if(!isLock){
            return Result.fail("不允许抢多张优惠券");
        }

        try{
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.creatVocherOrder(voucherId);
        }finally {
            // 释放锁
            redisLock.unlock();
        }
    }

    @Transactional
    public Result creatVocherOrder(Long voucherId){
        // 一人一单逻辑
        Long userid = UserHolder.getUser().getId();
        // 4.查询订单
        int count = query().eq("voucher_id", voucherId).eq("user_id", userid).count();
        if(count > 0){
            return Result.fail("你已经抢过优惠券了哦");
        }
        // 5.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock  = stock - 1")
                .eq("voucher_id", voucherId)
//                .eq("stock",voucher.getStock())
                .gt("stock",0) // 只判断是否有剩余优惠券，只要数据库中的库存大于零，都能这个词完成扣减库存操作
                .update();
        if (!success){
            // 扣减库存失败
            return Result.fail("库存不足!");
        }

        // 6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1 使用ID唯一生成器进行获取全局唯一ID
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 6.2 设置用户ID
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setId(orderId);
        // 6.3 设置优惠券ID
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        // 返回订单id
        return Result.ok(orderId);
    }
}
