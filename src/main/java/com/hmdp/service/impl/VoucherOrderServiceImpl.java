package com.hmdp.service.impl;

import com.hmdp.common.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
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

    @Resource
    private RedisIdWorker redisIdWorker;
    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        //1. 查询优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);

        //2. 判断优惠券是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        //3. 判断优惠券是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已结束");
        }

        //4. 判断优惠券库存是否足够
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("秒杀库存不足");
        }
        //5. 扣减优惠券库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).update();
        if (!success) {
            return Result.fail("扣减库存失败,库存不足");
        }
        //6. 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //6.1 订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //6.2 用户id
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        //6.3 优惠券id
        voucherOrder.setVoucherId(voucherId);
        //7. 保存订单
        boolean save = this.save(voucherOrder);
        if (!save) {
            return Result.fail("创建订单失败");
        }
        //8. 返回订单id
        return Result.success(orderId);
    }
}
