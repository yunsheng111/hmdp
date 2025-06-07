package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员商户分类控制器
 * </p>
 *
 * @author yate
 * @since 2024-07-31
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员商户分类接口")
public class AdminShopTypeController {

    @Resource
    private IShopTypeService shopTypeService;

    /**
     * 获取所有商户分类
     * @return 商户分类列表
     */
    @GetMapping("/shop-types")
    @ApiOperation("获取所有商户分类")
    public Result getAllShopTypes() {
        log.info("管理员获取所有商户分类");
        try {
            List<ShopType> shopTypes = shopTypeService.list();
            Map<String, Object> data = new HashMap<>();
            data.put("list", shopTypes);
            data.put("total", shopTypes.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商户分类失败", e);
            return Result.fail("获取商户分类失败");
        }
    }
} 