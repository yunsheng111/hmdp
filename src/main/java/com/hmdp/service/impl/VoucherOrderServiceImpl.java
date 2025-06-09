package com.hmdp.service.impl;

import com.hmdp.common.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.service.IVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimleRedisLock;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private IVoucherService voucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;
    @Override
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

        Long userId = UserHolder.getUser().getId();
        //创建锁对象
        //SimleRedisLock lock = new SimleRedisLock("order" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //获取锁
        boolean islock = lock.tryLock();
        if (!islock) {
            return Result.fail("不允许重复下单");
        }

        try {
            //获取代理对象（事务）
            IVoucherOrderService proxy =(IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
             lock.unlock();
        }

    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        //5. 一人一券
        Long userId = UserHolder.getUser().getId();
        //5.1 查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        //5.2 判断是否有订单
        if (count > 0) {
            return Result.fail("您已购买过该优惠券");
        }
        //6. 扣减优惠券库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).gt("stock", 0) //where id = ? and stock > 0
                .update();
        if (!success) {
            return Result.fail("扣减库存失败,库存不足");
        }
        //7. 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //7.1 订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //7.2 用户id
        voucherOrder.setUserId(userId);
        //7.3 优惠券id
        voucherOrder.setVoucherId(voucherId);
        //8. 保存订单
        boolean save = this.save(voucherOrder);
        if (!save) {
            return Result.fail("创建订单失败");
        }
        //9. 返回订单id
        return Result.success(orderId);
    }

    @Override
    public Result claimVoucher(Long voucherId) {
        // 1. 查询优惠券
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }

        // 2. 验证是否为普通优惠券
        if (voucher.getType() != 0) {
            return Result.fail("该优惠券不支持直接领取");
        }

        // 3. 验证优惠券状态
        if (voucher.getStatus() != 1) {
            return Result.fail("优惠券已下架或过期");
        }

        // 4. 检查用户登录状态
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("请先登录");
        }

        // 5. 检查是否已经领取过该优惠券
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail("您已领取过该优惠券");
        }

        // 6. 创建优惠券订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1 订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 6.2 用户id
        voucherOrder.setUserId(userId);
        // 6.3 优惠券id
        voucherOrder.setVoucherId(voucherId);
        // 6.4 商铺id
        voucherOrder.setShopId(voucher.getShopId());
        // 6.5 支付方式（普通券直接设为已支付）
        voucherOrder.setPayType(1);
        // 6.6 订单状态（普通券直接设为已支付）
        voucherOrder.setStatus(2);
        // 6.7 创建时间和支付时间
        LocalDateTime now = LocalDateTime.now();
        voucherOrder.setCreateTime(now);
        voucherOrder.setPayTime(now);

        // 7. 保存订单
        boolean save = this.save(voucherOrder);
        if (!save) {
            return Result.fail("领取失败，请重试");
        }

        // 8. 返回订单id
        return Result.success(orderId);
    }

    @Override
    public Result getClaimedVouchers(Long shopId) {
        // 检查用户登录状态
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("请先登录");
        }

        // 查询用户在该商店已领取的优惠券订单
        List<VoucherOrder> claimedOrders = query()
            .eq("user_id", userId)
            .eq("shop_id", shopId)
            .list();

        return Result.success(claimedOrders);
    }
}
