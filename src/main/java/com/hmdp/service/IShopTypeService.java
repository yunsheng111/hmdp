package com.hmdp.service;

import com.hmdp.common.Result;
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
}
