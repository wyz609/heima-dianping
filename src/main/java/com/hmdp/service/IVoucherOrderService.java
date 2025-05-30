package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 根据优惠券的id进行秒杀
     * @param voucherId 优惠券id
     * @return 秒杀结果
     */
    Result seckillVoucher(Long voucherId);

    Result creatVocherOrder(Long voucherId);
}
