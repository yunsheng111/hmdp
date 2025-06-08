package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.entity.Voucher;
import com.hmdp.exception.VoucherException;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.success(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
    }

    // ========== 商家端接口实现 ==========

    @Override
    @Transactional
    public VoucherDetailDTO createVoucher(VoucherCreateDTO createDTO) {
        // 创建优惠券实体
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(createDTO, voucher);
        voucher.setStatus(2); // 默认下架状态
        voucher.setCreateTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        save(voucher);

        // 转换为响应DTO
        return convertToDetailDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDetailDTO createSeckillVoucher(SeckillVoucherCreateDTO createDTO) {
        // 验证时间
        if (createDTO.getEndTime().isBefore(createDTO.getBeginTime())) {
            throw new VoucherException("结束时间不能早于开始时间");
        }

        // 创建优惠券实体
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(createDTO, voucher);
        voucher.setType(1); // 秒杀券
        voucher.setStatus(2); // 默认下架状态
        voucher.setCreateTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());

        // 保存优惠券
        save(voucher);

        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(createDTO.getStock());
        seckillVoucher.setBeginTime(createDTO.getBeginTime());
        seckillVoucher.setEndTime(createDTO.getEndTime());
        seckillVoucher.setCreateTime(LocalDateTime.now());
        seckillVoucher.setUpdateTime(LocalDateTime.now());
        seckillVoucherService.save(seckillVoucher);

        // 转换为响应DTO
        VoucherDetailDTO detailDTO = convertToDetailDTO(voucher);
        detailDTO.setStock(createDTO.getStock());
        detailDTO.setBeginTime(createDTO.getBeginTime());
        detailDTO.setEndTime(createDTO.getEndTime());
        detailDTO.setLimitPerUser(createDTO.getLimitPerUser());

        return detailDTO;
    }

    @Override
    public IPage<VoucherDetailDTO> queryVoucherList(VoucherQueryDTO queryDTO) {
        // 创建分页对象
        Page<Voucher> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 构建查询条件
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Voucher::getShopId, queryDTO.getShopId())
               .eq(queryDTO.getType() != null, Voucher::getType, queryDTO.getType())
               .eq(queryDTO.getStatus() != null, Voucher::getStatus, queryDTO.getStatus())
               .like(StringUtils.hasText(queryDTO.getTitle()), Voucher::getTitle, queryDTO.getTitle())
               .orderByDesc(Voucher::getCreateTime);

        // 执行查询
        IPage<Voucher> voucherPage = page(page, wrapper);

        // 转换为DTO
        IPage<VoucherDetailDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(voucherPage, resultPage);

        List<VoucherDetailDTO> detailList = voucherPage.getRecords().stream()
                .map(this::convertToDetailDTO)
                .collect(java.util.stream.Collectors.toList());

        // 为秒杀券补充库存信息
        List<Long> seckillVoucherIds = detailList.stream()
                .filter(dto -> dto.getType() == 1) // 筛选秒杀券
                .map(VoucherDetailDTO::getId)
                .collect(java.util.stream.Collectors.toList());

        if (!seckillVoucherIds.isEmpty()) {
            // 批量查询秒杀券信息
            List<SeckillVoucher> seckillVouchers = seckillVoucherService.listByIds(seckillVoucherIds);

            // 创建ID到库存的映射
            java.util.Map<Long, SeckillVoucher> seckillMap = seckillVouchers.stream()
                    .collect(java.util.stream.Collectors.toMap(SeckillVoucher::getVoucherId, sv -> sv));

            // 为秒杀券设置库存信息
            detailList.forEach(dto -> {
                if (dto.getType() == 1) {
                    SeckillVoucher seckillVoucher = seckillMap.get(dto.getId());
                    if (seckillVoucher != null) {
                        dto.setStock(seckillVoucher.getStock());
                        dto.setBeginTime(seckillVoucher.getBeginTime());
                        dto.setEndTime(seckillVoucher.getEndTime());
                    }
                }
            });
        }

        resultPage.setRecords(detailList);

        return resultPage;
    }

    @Override
    public VoucherDetailDTO getVoucherDetail(Long id, Long shopId) {
        // 查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            throw new VoucherException("优惠券不存在");
        }

        // 验证数据权限
        if (!voucher.getShopId().equals(shopId)) {
            throw new VoucherException("无权限访问该优惠券");
        }

        // 转换为DTO
        VoucherDetailDTO detailDTO = convertToDetailDTO(voucher);

        // 如果是秒杀券，查询秒杀信息
        if (voucher.getType() == 1) {
            SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
            if (seckillVoucher != null) {
                detailDTO.setStock(seckillVoucher.getStock());
                detailDTO.setBeginTime(seckillVoucher.getBeginTime());
                detailDTO.setEndTime(seckillVoucher.getEndTime());
            }
        }

        return detailDTO;
    }

    @Override
    @Transactional
    public void updateVoucherStatus(Long id, Long shopId, Integer status) {
        // 查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            throw new VoucherException("优惠券不存在");
        }

        // 验证数据权限
        if (!voucher.getShopId().equals(shopId)) {
            throw new VoucherException("无权限操作该优惠券");
        }

        // 更新状态
        voucher.setStatus(status);
        voucher.setUpdateTime(LocalDateTime.now());
        updateById(voucher);
    }

    @Override
    @Transactional
    public VoucherStockUpdateResultDTO updateVoucherStock(Long id, Long shopId, VoucherStockUpdateDTO updateDTO) {
        // 查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            throw new VoucherException("优惠券不存在");
        }

        // 验证数据权限
        if (!voucher.getShopId().equals(shopId)) {
            throw new VoucherException("无权限操作该优惠券");
        }

        // 只有秒杀券才能更新库存
        if (voucher.getType() != 1) {
            throw new VoucherException("只有秒杀券才能更新库存");
        }

        // 查询秒杀券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        if (seckillVoucher == null) {
            throw new VoucherException("秒杀券信息不存在");
        }

        Integer oldStock = seckillVoucher.getStock();
        Integer newStock;
        Integer changeAmount;

        if ("add".equals(updateDTO.getOperation())) {
            // 增加库存
            newStock = oldStock + updateDTO.getStock();
            changeAmount = updateDTO.getStock();
        } else if ("set".equals(updateDTO.getOperation())) {
            // 设置库存
            newStock = updateDTO.getStock();
            changeAmount = newStock - oldStock;
        } else {
            throw new VoucherException("不支持的操作类型");
        }

        // 更新库存
        seckillVoucher.setStock(newStock);
        seckillVoucher.setUpdateTime(LocalDateTime.now());
        seckillVoucherService.updateById(seckillVoucher);

        return new VoucherStockUpdateResultDTO(newStock, updateDTO.getOperation(), changeAmount);
    }

    @Override
    @Transactional
    public VoucherDetailDTO updateVoucher(Long id, Long shopId, VoucherUpdateDTO updateDTO) {
        // 查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            throw new VoucherException("优惠券不存在");
        }

        // 验证数据权限
        if (!voucher.getShopId().equals(shopId)) {
            throw new VoucherException("无权限操作该优惠券");
        }

        // 验证是否为普通券
        if (voucher.getType() == 1) {
            throw new VoucherException("秒杀券不能使用此接口更新，请使用秒杀券更新接口");
        }

        // 更新优惠券信息
        BeanUtils.copyProperties(updateDTO, voucher);
        voucher.setUpdateTime(LocalDateTime.now());
        updateById(voucher);

        // 返回更新后的详情
        return convertToDetailDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDetailDTO updateSeckillVoucher(Long id, Long shopId, SeckillVoucherUpdateDTO updateDTO) {
        // 查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            throw new VoucherException("优惠券不存在");
        }

        // 验证数据权限
        if (!voucher.getShopId().equals(shopId)) {
            throw new VoucherException("无权限操作该优惠券");
        }

        // 验证是否为秒杀券
        if (voucher.getType() != 1) {
            throw new VoucherException("普通券不能使用此接口更新，请使用普通券更新接口");
        }

        // 验证时间
        if (updateDTO.getEndTime().isBefore(updateDTO.getBeginTime())) {
            throw new VoucherException("结束时间不能早于开始时间");
        }

        // 查询秒杀券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        if (seckillVoucher == null) {
            throw new VoucherException("秒杀券信息不存在");
        }

        // 更新优惠券基本信息
        BeanUtils.copyProperties(updateDTO, voucher);
        voucher.setType(1); // 确保类型为秒杀券
        voucher.setUpdateTime(LocalDateTime.now());
        updateById(voucher);

        // 更新秒杀券信息
        seckillVoucher.setStock(updateDTO.getStock());
        seckillVoucher.setBeginTime(updateDTO.getBeginTime());
        seckillVoucher.setEndTime(updateDTO.getEndTime());
        seckillVoucher.setUpdateTime(LocalDateTime.now());
        seckillVoucherService.updateById(seckillVoucher);

        // 返回更新后的详情
        VoucherDetailDTO detailDTO = convertToDetailDTO(voucher);
        detailDTO.setStock(updateDTO.getStock());
        detailDTO.setBeginTime(updateDTO.getBeginTime());
        detailDTO.setEndTime(updateDTO.getEndTime());
        detailDTO.setLimitPerUser(updateDTO.getLimitPerUser());

        return detailDTO;
    }

    /**
     * 转换为详情DTO
     */
    private VoucherDetailDTO convertToDetailDTO(Voucher voucher) {
        VoucherDetailDTO detailDTO = new VoucherDetailDTO();
        BeanUtils.copyProperties(voucher, detailDTO);
        return detailDTO;
    }
}
