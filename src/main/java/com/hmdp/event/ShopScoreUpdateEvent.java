package com.hmdp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 商店评分更新事件
 * 用于解决ShopCommentServiceImpl和ShopServiceImpl之间的循环依赖
 */
@Getter
public class ShopScoreUpdateEvent extends ApplicationEvent {
    
    private final Long shopId;
    private final Integer score;
    
    public ShopScoreUpdateEvent(Object source, Long shopId, Integer score) {
        super(source);
        this.shopId = shopId;
        this.score = score;
    }
} 