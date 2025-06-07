package com.hmdp.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.ShopTypeDTO;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @description: 获取店铺类型列表 ,Redis用list结构存储
     * @author: yate
     * @date: 2025/1/11 0011 17:33
     * @param:
     * @return:
     **/
/*    @Override
    public Result getTypeList() {
        // 1.从Redis查询店铺类型列表,用list结构存储
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range("shopType2", 0, -1);

        // 2.判断是否存在缓存，如果存在，直接返回缓存数据
        if (!shopTypeJsonList.isEmpty()) {
            //使用fastjson将List<String>转换为List<ShopType>集合
            List<ShopType> shopTypeList = JSON.parseArray(shopTypeJsonList.toString(), ShopType.class);
            return Result.success(shopTypeList);
        }
        // 3.Redis中如果不存在，从数据库查询店铺类型列表
        QueryWrapper<ShopType> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<ShopType> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return Result.fail("未查询到任何店铺类型");
        }
        // 4.将查询结果并将数据缓存到Redis
        // 使用fastjson将List<ShopType>转换为List<String>集合
        List<String> shopTypeJsonList2 = JSON.parseArray(JSON.toJSONString(list), String.class);
        stringRedisTemplate.opsForList().rightPushAll("shopType2", shopTypeJsonList2);
        // 设置缓存有效期为1天
        stringRedisTemplate.expire("shopType2", 1, TimeUnit.DAYS);
        // 5.返回查询结果
        return Result.success(list);
    }*/



    /**
     * @description:  获取店铺类型列表 ,Redis用hash结构存储
     * @author: yate
     * @date: 2025/1/11 0011 17:32
     * @param: []
     * @return: com.hmdp.common.Result
     **/
    @Override
    public Result getTypeList() {
        // 1.从Redis查询店铺类型列表,用hash结构存储
        Map<Object, Object> shopTypeMap = stringRedisTemplate.opsForHash().entries("shopType");
        // 2.判断是否存在缓存，如果存在，直接返回缓存数据
        if (!shopTypeMap.isEmpty()) {
            // 将缓存数据转换为List<ShopType>集合
            List<ShopType> shopTypeList = shopTypeMap.values().stream()
                    .map(jsonStr -> JSON.parseObject(jsonStr.toString(), ShopType.class))
                    .collect(Collectors.toList());
              /*List<ShopType> shopTypeList = new ArrayList<>();
                for (Object jsonStr : shopTypeMap.values()) {
                        ShopType shopType = JSON.parseObject(jsonStr.toString(), ShopType.class);
                        shopTypeList.add(shopType);
                }*/
            // 对集合进行排序(Redis的hash结构内部是一个哈希表，哈希表的特性决定了键值对的存储顺序是根据哈希值来决定的，
            // 而不是插入顺序。哈希表的这种特性使得键值对的存储和检索非常高效，但不保证顺序。)
            shopTypeList.sort(Comparator.comparingInt(ShopType::getSort));
            return Result.success(shopTypeList);
        }
        // 3.Redis中如果不存在，从数据库查询店铺类型列表
        QueryWrapper<ShopType> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<ShopType> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return Result.fail("未查询到任何店铺类型");
        }
        // 4.将查询结果并将数据缓存到Redis
        // 将List<ShopType>集合转换为Map对象，键为shopType的id，值为shopType的json字符串
        Map<Object, Object> shopTypeJsonMap = new HashMap<>();
        for (ShopType shopType : list) {
            //使用fastjson将ShopType对象转换为json字符串
            String jsonStr = JSON.toJSONString(shopType);
            // 将Long类型的id转换为字符串，因为Redis的键只能是字符串
            shopTypeJsonMap.put(String.valueOf(shopType.getId()), jsonStr);
        }
        stringRedisTemplate.opsForHash().putAll("shopType", shopTypeJsonMap);
        // 设置缓存有效期为1天
        stringRedisTemplate.expire("shopType", 1, TimeUnit.DAYS);

        // 5.返回查询结果
        return Result.success(list);
    }

    @Override
    public Result createShopType(ShopTypeDTO dto) {
        log.info("创建商户分类: {}", dto);
        try {
            // 1.检查名称是否重复
            QueryWrapper<ShopType> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", dto.getName());
            if (this.count(queryWrapper) > 0) {
                return Result.fail("分类名称已存在");
            }

            // 2.创建新的商户分类
            ShopType shopType = new ShopType();
            BeanUtils.copyProperties(dto, shopType);

            // 3.保存到数据库
            boolean success = this.save(shopType);
            if (!success) {
                return Result.fail("创建分类失败");
            }

            // 4.清除Redis缓存
            stringRedisTemplate.delete("shopType");

            log.info("创建商户分类成功: {}", shopType);
            return Result.success(shopType);
        } catch (Exception e) {
            log.error("创建商户分类失败", e);
            return Result.fail("创建分类失败");
        }
    }

    @Override
    public Result updateShopType(Long id, ShopTypeDTO dto) {
        log.info("更新商户分类: id={}, dto={}", id, dto);
        try {
            // 1.检查分类是否存在
            ShopType existingShopType = this.getById(id);
            if (existingShopType == null) {
                return Result.fail("分类不存在");
            }

            // 2.检查名称是否重复（排除自己）
            QueryWrapper<ShopType> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", dto.getName()).ne("id", id);
            if (this.count(queryWrapper) > 0) {
                return Result.fail("分类名称已存在");
            }

            // 3.更新分类信息
            BeanUtils.copyProperties(dto, existingShopType);

            // 4.保存到数据库
            boolean success = this.updateById(existingShopType);
            if (!success) {
                return Result.fail("更新分类失败");
            }

            // 5.清除Redis缓存
            stringRedisTemplate.delete("shopType");

            log.info("更新商户分类成功: {}", existingShopType);
            return Result.success(existingShopType);
        } catch (Exception e) {
            log.error("更新商户分类失败", e);
            return Result.fail("更新分类失败");
        }
    }

    @Override
    public Result deleteShopType(Long id) {
        log.info("删除商户分类: id={}", id);
        try {
            // 1.检查分类是否存在
            ShopType existingShopType = this.getById(id);
            if (existingShopType == null) {
                return Result.fail("分类不存在");
            }

            // 2.检查是否有商铺使用该分类
            // 这里需要注入ShopService来检查，暂时跳过这个检查
            // 在实际项目中应该添加这个检查

            // 3.删除分类
            boolean success = this.removeById(id);
            if (!success) {
                return Result.fail("删除分类失败");
            }

            // 4.清除Redis缓存
            stringRedisTemplate.delete("shopType");

            log.info("删除商户分类成功: id={}", id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除商户分类失败", e);
            return Result.fail("删除分类失败");
        }
    }

}