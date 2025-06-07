package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.ShopTypeDTO;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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

    /**
     * 新增商户分类
     * @param dto 商户分类数据
     * @return 新增结果
     */
    @PostMapping("/shop-types")
    @ApiOperation("新增商户分类")
    public Result createShopType(@Valid @RequestBody ShopTypeDTO dto) {
        log.info("管理员新增商户分类: {}", dto);
        return shopTypeService.createShopType(dto);
    }

    /**
     * 更新商户分类
     * @param id 分类ID
     * @param dto 商户分类数据
     * @return 更新结果
     */
    @PutMapping("/shop-types/{id}")
    @ApiOperation("更新商户分类")
    public Result updateShopType(@PathVariable("id") Long id, @Valid @RequestBody ShopTypeDTO dto) {
        log.info("管理员更新商户分类: id={}, dto={}", id, dto);
        return shopTypeService.updateShopType(id, dto);
    }

    /**
     * 删除商户分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/shop-types/{id}")
    @ApiOperation("删除商户分类")
    public Result deleteShopType(@PathVariable("id") Long id) {
        log.info("管理员删除商户分类: id={}", id);
        return shopTypeService.deleteShopType(id);
    }
}