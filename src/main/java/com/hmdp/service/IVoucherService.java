package com.hmdp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    // ========== 商家端接口 ==========

    /**
     * 创建普通优惠券
     */
    VoucherDetailDTO createVoucher(VoucherCreateDTO createDTO);

    /**
     * 创建秒杀优惠券
     */
    VoucherDetailDTO createSeckillVoucher(SeckillVoucherCreateDTO createDTO);

    /**
     * 分页查询优惠券列表
     */
    IPage<VoucherDetailDTO> queryVoucherList(VoucherQueryDTO queryDTO);

    /**
     * 获取优惠券详情
     */
    VoucherDetailDTO getVoucherDetail(Long id, Long shopId);

    /**
     * 更新优惠券状态
     */
    void updateVoucherStatus(Long id, Long shopId, Integer status);

    /**
     * 更新优惠券库存
     */
    VoucherStockUpdateResultDTO updateVoucherStock(Long id, Long shopId, VoucherStockUpdateDTO updateDTO);
    
    /**
     * 更新普通优惠券信息
     * @param id 优惠券ID
     * @param shopId 商铺ID
     * @param updateDTO 更新信息
     * @return 更新后的优惠券详情
     */
    VoucherDetailDTO updateVoucher(Long id, Long shopId, VoucherUpdateDTO updateDTO);
    
    /**
     * 更新秒杀优惠券信息
     * @param id 优惠券ID
     * @param shopId 商铺ID
     * @param updateDTO 更新信息
     * @return 更新后的优惠券详情
     */
    VoucherDetailDTO updateSeckillVoucher(Long id, Long shopId, SeckillVoucherUpdateDTO updateDTO);
}
