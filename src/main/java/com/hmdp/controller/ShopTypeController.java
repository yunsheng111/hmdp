package com.hmdp.controller;


import com.hmdp.common.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    /**
     * @description: 查询所有商铺类型
     * @author: yate
     * @date: 2025/1/11 0011 1:31
     * @param: []
     * @return: com.hmdp.common.Result
     **/
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.getTypeList();
    }
}
