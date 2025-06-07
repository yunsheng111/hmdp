package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.dto.ShopTypeDTO;
import com.hmdp.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * @description: 获取商铺类型列表
     * @author: yate
     * @date: 2025/1/11 0011 1:31
     * @param: []
     * @return: com.hmdp.common.Result
     **/
    Result getTypeList();

    /**
     * 创建商户分类
     * @param dto 商户分类数据
     * @return 创建结果
     */
    Result createShopType(ShopTypeDTO dto);

    /**
     * 更新商户分类
     * @param id 分类ID
     * @param dto 商户分类数据
     * @return 更新结果
     */
    Result updateShopType(Long id, ShopTypeDTO dto);

    /**
     * 删除商户分类
     * @param id 分类ID
     * @return 删除结果
     */
    Result deleteShopType(Long id);
}
