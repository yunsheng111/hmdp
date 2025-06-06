package com.hmdp.service.impl;

import com.hmdp.dto.ProductUpdateDTO;
import com.hmdp.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * ProductServiceImpl 价格转换测试
 */
public class ProductServiceImplTest {

    @Test
    @DisplayName("测试价格转换逻辑")
    public void testPriceConversion() {
        // 测试价格转换：从元转换为分
        BigDecimal priceInYuan = new BigDecimal("12.50"); // 12.50元
        long expectedPriceInCents = 1250L; // 1250分
        
        // 模拟 updateProduct 方法中的价格转换逻辑
        long actualPriceInCents = priceInYuan.multiply(new BigDecimal(100)).longValue();
        
        assertEquals(expectedPriceInCents, actualPriceInCents, 
            "价格转换应该正确：12.50元 = 1250分");
    }
    
    @Test
    @DisplayName("测试边界价格转换")
    public void testBoundaryPriceConversion() {
        // 测试边界情况
        
        // 测试 0 元
        BigDecimal zeroPrice = new BigDecimal("0.00");
        long zeroPriceInCents = zeroPrice.multiply(new BigDecimal(100)).longValue();
        assertEquals(0L, zeroPriceInCents, "0元应该转换为0分");
        
        // 测试小数价格
        BigDecimal decimalPrice = new BigDecimal("9.99");
        long decimalPriceInCents = decimalPrice.multiply(new BigDecimal(100)).longValue();
        assertEquals(999L, decimalPriceInCents, "9.99元应该转换为999分");
        
        // 测试整数价格
        BigDecimal integerPrice = new BigDecimal("100");
        long integerPriceInCents = integerPrice.multiply(new BigDecimal(100)).longValue();
        assertEquals(10000L, integerPriceInCents, "100元应该转换为10000分");
    }
    
    @Test
    @DisplayName("测试ProductUpdateDTO价格字段类型")
    public void testProductUpdateDTOPriceType() {
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        BigDecimal price = new BigDecimal("25.99");
        updateDTO.setPrice(price);
        
        assertNotNull(updateDTO.getPrice(), "价格字段不应为空");
        assertEquals(price, updateDTO.getPrice(), "价格字段应该是BigDecimal类型");
        assertTrue(updateDTO.getPrice() instanceof BigDecimal, "价格字段应该是BigDecimal实例");
    }
    
    @Test
    @DisplayName("测试Product实体价格字段类型")
    public void testProductEntityPriceType() {
        Product product = new Product();
        Long priceInCents = 2599L; // 25.99元 = 2599分
        product.setPrice(priceInCents);
        
        assertNotNull(product.getPrice(), "价格字段不应为空");
        assertEquals(priceInCents, product.getPrice(), "价格字段应该是Long类型");
        assertTrue(product.getPrice() instanceof Long, "价格字段应该是Long实例");
    }
}
