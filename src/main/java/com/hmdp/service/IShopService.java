package com.hmdp.service;

import com.hmdp.common.Result;
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
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    Result queryById(Long id);

    /**
     * @description: 更新商铺信息
     * @author: yate
     * @date: 2025/1/11 0011 20:16
     * @param: [shop]
     * @return: com.hmdp.common.Result
     **/
    Result update(Shop shop);
}
