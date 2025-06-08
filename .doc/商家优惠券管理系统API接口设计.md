# 商家优惠券管理系统API接口设计文档

## 一、概述

本文档定义了商家优惠券管理系统的后端API接口设计，包括基础优惠券管理、规则模板管理、批量操作、记录统计和秒杀优惠券专项功能模块。

### 1.1 现有实体分析

基于现有的Voucher实体类字段结构：
- **基础字段**：id, shopId, title, subTitle, rules, payValue, actualValue, minAmount, validDays, type, status, createTime, updateTime
- **秒杀相关字段**：stock, beginTime, endTime（通过@TableField(exist = false)标注，存储在SeckillVoucher表中）
- **优惠券类型**：0-普通券，1-秒杀券
- **优惠券状态**：1-上架，2-下架，3-过期

### 1.2 统一响应格式

所有API接口使用统一的Result类进行响应：
```json
{
  "success": true/false,
  "errorMsg": "错误信息",
  "data": "响应数据",
  "total": "总数量（分页时使用）"
}
```

### 1.3 异常处理

- 使用现有的WebExceptionAdvice全局异常处理器
- 创建VoucherException自定义异常类
- 统一错误码和错误信息

## 二、基础优惠券管理模块

### 2.1 创建优惠券

**接口地址**：`POST /voucher`

**请求参数**：
```json
{
  "shopId": 1001,
  "type": 0,
  "title": "满100减20优惠券",
  "subTitle": "新用户专享",
  "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
  "payValue": 10000,
  "actualValue": 2000,
  "minAmount": 10000,
  "validDays": 30
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 12345,
    "shopId": 1001,
    "type": 0,
    "title": "满100减20优惠券",
    "subTitle": "新用户专享",
    "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
    "payValue": 10000,
    "actualValue": 2000,
    "minAmount": 10000,
    "validDays": 30,
    "status": 2,
    "createTime": "2024-12-22T10:30:00",
    "updateTime": "2024-12-22T10:30:00"
  },
  "total": null
}
```

### 2.2 获取优惠券列表

**接口地址**：`GET /voucher/list`

**请求参数**：
```
shopId: 1001 (必填)
type: 0 (可选，0-普通券，1-秒杀券)
status: 1 (可选，1-上架，2-下架，3-过期)
title: "满减" (可选，标题模糊查询)
current: 1 (可选，当前页码，默认1)
size: 10 (可选，每页大小，默认10)
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "records": [
      {
        "id": 12345,
        "shopId": 1001,
        "type": 0,
        "title": "满100减20优惠券",
        "subTitle": "新用户专享",
        "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
        "payValue": 10000,
        "actualValue": 2000,
        "minAmount": 10000,
        "validDays": 30,
        "status": 1,
        "createTime": "2024-12-22T10:30:00",
        "updateTime": "2024-12-22T10:30:00"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10,
    "pages": 3
  },
  "total": 25
}
```

### 2.3 获取优惠券详情

**接口地址**：`GET /voucher/{id}`

**路径参数**：
- id: 优惠券ID

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 12345,
    "shopId": 1001,
    "type": 0,
    "title": "满100减20优惠券",
    "subTitle": "新用户专享",
    "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
    "payValue": 10000,
    "actualValue": 2000,
    "minAmount": 10000,
    "validDays": 30,
    "status": 1,
    "createTime": "2024-12-22T10:30:00",
    "updateTime": "2024-12-22T10:30:00",
    "receivedCount": 150,
    "usedCount": 80,
    "remainingCount": 70
  },
  "total": null
}
```

### 2.4 更新优惠券状态

**接口地址**：`PUT /voucher/{id}/status`

**路径参数**：
- id: 优惠券ID

**请求参数**：
```json
{
  "status": 1
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

### 2.5 更新优惠券库存

**接口地址**：`PUT /voucher/{id}/stock`

**路径参数**：
- id: 优惠券ID

**请求参数**：
```json
{
  "stock": 1000,
  "operation": "add"
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "currentStock": 1500,
    "operation": "add",
    "changeAmount": 1000
  },
  "total": null
}
```

### 2.6 更新优惠券信息

**接口地址**：`PUT /voucher/{id}`

**路径参数**：
- id: 优惠券ID

**请求参数**：
```json
{
  "shopId": 1001,
  "type": 0,
  "title": "满100减30优惠券",
  "subTitle": "周末特惠",
  "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
  "payValue": 10000,
  "actualValue": 3000,
  "minAmount": 10000,
  "validDays": 30,
  "status": 1
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 12345,
    "shopId": 1001,
    "type": 0,
    "title": "满100减30优惠券",
    "subTitle": "周末特惠",
    "rules": "1. 满100元可用\n2. 不可与其他优惠叠加\n3. 有效期30天",
    "payValue": 10000,
    "actualValue": 3000,
    "minAmount": 10000,
    "validDays": 30,
    "status": 1,
    "createTime": "2024-12-22T10:30:00",
    "updateTime": "2024-12-22T16:45:00"
  },
  "total": null
}
```

## 三、优惠券规则与批量操作模块

### 3.1 优惠券规则模板管理

#### 3.1.1 创建规则模板

**接口地址**：`POST /voucher/template`

**请求参数**：
```json
{
  "name": "满减券模板",
  "description": "通用满减券规则模板",
  "rules": "1. 满{minAmount}元可用\n2. 减{actualValue}元\n3. 不可与其他优惠叠加\n4. 有效期{validDays}天",
  "templateType": "FULL_REDUCTION"
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 101,
    "name": "满减券模板",
    "description": "通用满减券规则模板",
    "rules": "1. 满{minAmount}元可用\n2. 减{actualValue}元\n3. 不可与其他优惠叠加\n4. 有效期{validDays}天",
    "templateType": "FULL_REDUCTION",
    "createTime": "2024-12-22T10:30:00"
  },
  "total": null
}
```

#### 3.1.2 获取规则模板列表

**接口地址**：`GET /voucher/template/list`

**请求参数**：
```
templateType: "FULL_REDUCTION" (可选)
current: 1 (可选，当前页码，默认1)
size: 10 (可选，每页大小，默认10)
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "records": [
      {
        "id": 101,
        "name": "满减券模板",
        "description": "通用满减券规则模板",
        "rules": "1. 满{minAmount}元可用\n2. 减{actualValue}元\n3. 不可与其他优惠叠加\n4. 有效期{validDays}天",
        "templateType": "FULL_REDUCTION",
        "createTime": "2024-12-22T10:30:00"
      }
    ],
    "total": 5,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "total": 5
}
```

### 3.2 优惠券批量操作

#### 3.2.1 批量创建优惠券

**接口地址**：`POST /voucher/batch/create`

**请求参数**：
```json
{
  "templateId": 101,
  "shopId": 1001,
  "batchConfig": {
    "count": 100,
    "title": "双11满减券",
    "subTitle": "限时特惠",
    "payValue": 10000,
    "actualValue": 2000,
    "minAmount": 10000,
    "validDays": 7
  }
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "batchId": "BATCH_20241222_001",
    "successCount": 100,
    "failCount": 0,
    "voucherIds": [12346, 12347, 12348]
  },
  "total": null
}
```

#### 3.2.2 批量状态变更

**接口地址**：`PUT /voucher/batch/status`

**请求参数**：
```json
{
  "voucherIds": [12345, 12346, 12347],
  "status": 2
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "successCount": 3,
    "failCount": 0,
    "failedIds": []
  },
  "total": null
}
```

#### 3.2.3 批量删除优惠券

**接口地址**：`DELETE /voucher/batch`

**请求参数**：
```json
{
  "voucherIds": [12345, 12346, 12347]
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "successCount": 3,
    "failCount": 0,
    "failedIds": []
  },
  "total": null
}
```

## 四、优惠券记录与统计模块

### 4.1 优惠券领取记录查询

**接口地址**：`GET /voucher/{id}/records`

**路径参数**：
- id: 优惠券ID

**请求参数**：
```
userId: 123 (可选，按用户ID筛选)
status: 1 (可选，1-未使用，2-已使用，3-已过期)
startTime: "2024-12-01T00:00:00" (可选，开始时间)
endTime: "2024-12-31T23:59:59" (可选，结束时间)
current: 1 (可选，当前页码，默认1)
size: 10 (可选，每页大小，默认10)
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "records": [
      {
        "id": 10001,
        "userId": 123,
        "userName": "张三",
        "userPhone": "138****5678",
        "voucherId": 12345,
        "voucherTitle": "满100减20优惠券",
        "receiveTime": "2024-12-22T10:30:00",
        "useTime": "2024-12-22T15:20:00",
        "status": 2,
        "statusDesc": "已使用",
        "orderId": 20001
      }
    ],
    "total": 150,
    "current": 1,
    "size": 10,
    "pages": 15
  },
  "total": 150
}
```

### 4.2 优惠券发放统计

**接口地址**：`GET /voucher/statistics`

**请求参数**：
```
shopId: 1001 (必填)
voucherId: 12345 (可选，指定优惠券ID)
startTime: "2024-12-01T00:00:00" (可选，开始时间)
endTime: "2024-12-31T23:59:59" (可选，结束时间)
groupBy: "day" (可选，统计维度：day/week/month)
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "summary": {
      "totalVouchers": 10,
      "totalReceived": 1500,
      "totalUsed": 800,
      "totalExpired": 200,
      "usageRate": 53.33,
      "expiryRate": 13.33
    },
    "details": [
      {
        "voucherId": 12345,
        "voucherTitle": "满100减20优惠券",
        "receivedCount": 150,
        "usedCount": 80,
        "expiredCount": 20,
        "usageRate": 53.33,
        "expiryRate": 13.33
      }
    ],
    "timeSeriesData": [
      {
        "date": "2024-12-22",
        "receivedCount": 50,
        "usedCount": 25,
        "expiredCount": 5
      }
    ]
  },
  "total": null
}
```

### 4.3 优惠券使用分析

**接口地址**：`GET /voucher/analysis`

**请求参数**：
```
shopId: 1001 (必填)
analysisType: "usage_trend" (必填，分析类型：usage_trend/user_behavior/conversion_rate)
startTime: "2024-12-01T00:00:00" (可选，开始时间)
endTime: "2024-12-31T23:59:59" (可选，结束时间)
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "analysisType": "usage_trend",
    "period": "2024-12-01 至 2024-12-31",
    "metrics": {
      "peakUsageHour": 14,
      "peakUsageDay": "周六",
      "averageDailyUsage": 26.7,
      "conversionRate": 53.33
    },
    "trendData": [
      {
        "hour": 10,
        "usageCount": 15,
        "percentage": 5.8
      },
      {
        "hour": 14,
        "usageCount": 45,
        "percentage": 17.3
      }
    ]
  },
  "total": null
}
```

## 五、秒杀优惠券专项模块

### 5.1 创建秒杀券

**接口地址**：`POST /voucher/seckill`

**请求参数**：
```json
{
  "shopId": 1001,
  "title": "限时秒杀券",
  "subTitle": "每日限量100张",
  "rules": "1. 满50元可用\n2. 减10元\n3. 限时抢购\n4. 每人限购1张",
  "payValue": 5000,
  "actualValue": 1000,
  "minAmount": 5000,
  "validDays": 1,
  "stock": 100,
  "beginTime": "2024-12-23T10:00:00",
  "endTime": "2024-12-23T12:00:00",
  "limitPerUser": 1
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 12350,
    "shopId": 1001,
    "type": 1,
    "title": "限时秒杀券",
    "subTitle": "每日限量100张",
    "rules": "1. 满50元可用\n2. 减10元\n3. 限时抢购\n4. 每人限购1张",
    "payValue": 5000,
    "actualValue": 1000,
    "minAmount": 5000,
    "validDays": 1,
    "status": 1,
    "stock": 100,
    "beginTime": "2024-12-23T10:00:00",
    "endTime": "2024-12-23T12:00:00",
    "limitPerUser": 1,
    "createTime": "2024-12-22T10:30:00",
    "updateTime": "2024-12-22T10:30:00"
  },
  "total": null
}
```

### 5.2 查询秒杀券库存

**接口地址**：`GET /voucher/seckill/{id}/stock`

**路径参数**：
- id: 秒杀券ID

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "voucherId": 12350,
    "currentStock": 85,
    "totalStock": 100,
    "soldCount": 15,
    "soldPercentage": 15.0,
    "status": "ACTIVE",
    "beginTime": "2024-12-23T10:00:00",
    "endTime": "2024-12-23T12:00:00",
    "remainingTime": 3600
  },
  "total": null
}
```

### 5.3 秒杀活动预热

**接口地址**：`POST /voucher/seckill/{id}/preheat`

**路径参数**：
- id: 秒杀券ID

**请求参数**：
```json
{
  "preheatTime": "2024-12-23T09:30:00",
  "cacheStrategy": "REDIS_PRELOAD"
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "voucherId": 12350,
    "preheatStatus": "SUCCESS",
    "cacheLoaded": true,
    "stockCached": 100,
    "preheatTime": "2024-12-23T09:30:00",
    "estimatedQPS": 1000
  },
  "total": null
}
```

### 5.4 秒杀活动实时监控

**接口地址**：`GET /voucher/seckill/{id}/monitor`

**路径参数**：
- id: 秒杀券ID

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "voucherId": 12350,
    "activityStatus": "ACTIVE",
    "realTimeMetrics": {
      "currentQPS": 850,
      "peakQPS": 1200,
      "currentStock": 85,
      "soldInLastMinute": 5,
      "errorRate": 0.02,
      "averageResponseTime": 120
    },
    "systemHealth": {
      "redisStatus": "HEALTHY",
      "dbStatus": "HEALTHY",
      "cacheHitRate": 98.5
    },
    "alerts": [
      {
        "level": "WARNING",
        "message": "QPS接近阈值",
        "timestamp": "2024-12-23T10:15:00"
      }
    ]
  },
  "total": null
}
```

### 5.5 更新秒杀券信息

**接口地址**：`PUT /voucher/seckill/{id}`

**路径参数**：
- id: 秒杀券ID

**请求参数**：
```json
{
  "shopId": 1001,
  "title": "限时秒杀券-升级版",
  "subTitle": "限量200张",
  "rules": "1. 满50元可用\n2. 减10元\n3. 限时抢购\n4. 每人限购2张",
  "payValue": 5000,
  "actualValue": 1000,
  "minAmount": 5000,
  "validDays": 1,
  "stock": 200,
  "beginTime": "2024-12-24T10:00:00",
  "endTime": "2024-12-24T12:00:00",
  "limitPerUser": 2,
  "status": 1
}
```

**响应结果**：
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 12350,
    "shopId": 1001,
    "type": 1,
    "title": "限时秒杀券-升级版",
    "subTitle": "限量200张",
    "rules": "1. 满50元可用\n2. 减10元\n3. 限时抢购\n4. 每人限购2张",
    "payValue": 5000,
    "actualValue": 1000,
    "minAmount": 5000,
    "validDays": 1,
    "status": 1,
    "stock": 200,
    "beginTime": "2024-12-24T10:00:00",
    "endTime": "2024-12-24T12:00:00",
    "limitPerUser": 2,
    "createTime": "2024-12-22T10:30:00",
    "updateTime": "2024-12-23T09:45:00"
  },
  "total": null
}
```

## 六、数据传输对象(DTO)设计

### 6.1 请求DTO

#### VoucherCreateDTO
```java
@Data
public class VoucherCreateDTO {
    @NotNull(message = "商铺ID不能为空")
    private Long shopId;

    @NotNull(message = "优惠券类型不能为空")
    private Integer type;

    @NotBlank(message = "优惠券标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    @Size(max = 200, message = "副标题长度不能超过200个字符")
    private String subTitle;

    @NotBlank(message = "使用规则不能为空")
    @Size(max = 1000, message = "使用规则长度不能超过1000个字符")
    private String rules;

    @NotNull(message = "支付金额不能为空")
    @Min(value = 0, message = "支付金额不能为负数")
    private Long payValue;

    @NotNull(message = "抵扣金额不能为空")
    @Min(value = 0, message = "抵扣金额不能为负数")
    private Long actualValue;

    @NotNull(message = "最低消费金额不能为空")
    @Min(value = 0, message = "最低消费金额不能为负数")
    private Long minAmount;

    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数必须大于0")
    private Integer validDays;
}
```

#### SeckillVoucherCreateDTO
```java
@Data
public class SeckillVoucherCreateDTO extends VoucherCreateDTO {
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer stock;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime beginTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;

    @Min(value = 1, message = "每人限购数量必须大于0")
    private Integer limitPerUser = 1;
}
```

#### VoucherQueryDTO
```java
@Data
public class VoucherQueryDTO {
    @NotNull(message = "商铺ID不能为空")
    private Long shopId;

    private Integer type;
    private Integer status;

    @Size(max = 50, message = "标题搜索关键词长度不能超过50个字符")
    private String title;

    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
}
```

#### VoucherBatchOperationDTO
```java
@Data
public class VoucherBatchOperationDTO {
    @NotEmpty(message = "优惠券ID列表不能为空")
    @Size(max = 100, message = "批量操作数量不能超过100个")
    private List<Long> voucherIds;

    private Integer status;

    @NotBlank(message = "操作类型不能为空")
    private String operation;
}
```

### 6.2 响应DTO

#### VoucherDetailDTO
```java
@Data
public class VoucherDetailDTO {
    private Long id;
    private Long shopId;
    private Integer type;
    private String title;
    private String subTitle;
    private String rules;
    private Long payValue;
    private Long actualValue;
    private Long minAmount;
    private Integer validDays;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 统计信息
    private Integer receivedCount;
    private Integer usedCount;
    private Integer remainingCount;

    // 秒杀券特有字段
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer limitPerUser;
}
```

#### VoucherStatisticsDTO
```java
@Data
public class VoucherStatisticsDTO {
    private StatisticsSummary summary;
    private List<VoucherStatisticsDetail> details;
    private List<TimeSeriesData> timeSeriesData;

    @Data
    public static class StatisticsSummary {
        private Integer totalVouchers;
        private Integer totalReceived;
        private Integer totalUsed;
        private Integer totalExpired;
        private Double usageRate;
        private Double expiryRate;
    }

    @Data
    public static class VoucherStatisticsDetail {
        private Long voucherId;
        private String voucherTitle;
        private Integer receivedCount;
        private Integer usedCount;
        private Integer expiredCount;
        private Double usageRate;
        private Double expiryRate;
    }

    @Data
    public static class TimeSeriesData {
        private String date;
        private Integer receivedCount;
        private Integer usedCount;
        private Integer expiredCount;
    }
}
```

#### VoucherRecordDTO
```java
@Data
public class VoucherRecordDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long voucherId;
    private String voucherTitle;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private Integer status;
    private String statusDesc;
    private Long orderId;
}
```

#### SeckillMonitorDTO
```java
@Data
public class SeckillMonitorDTO {
    private Long voucherId;
    private String activityStatus;
    private RealTimeMetrics realTimeMetrics;
    private SystemHealth systemHealth;
    private List<Alert> alerts;

    @Data
    public static class RealTimeMetrics {
        private Integer currentQPS;
        private Integer peakQPS;
        private Integer currentStock;
        private Integer soldInLastMinute;
        private Double errorRate;
        private Integer averageResponseTime;
    }

    @Data
    public static class SystemHealth {
        private String redisStatus;
        private String dbStatus;
        private Double cacheHitRate;
    }

    @Data
    public static class Alert {
        private String level;
        private String message;
        private LocalDateTime timestamp;
    }
}
```

## 七、异常处理设计

### 7.1 自定义异常类

#### VoucherException
```java
package com.hmdp.exception;

/**
 * 优惠券相关异常
 */
public class VoucherException extends RuntimeException {

    public VoucherException(String message) {
        super(message);
    }

    public VoucherException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 7.2 全局异常处理器扩展

在现有的WebExceptionAdvice中添加VoucherException处理：

```java
@ExceptionHandler(VoucherException.class)
public Result handleVoucherException(VoucherException e) {
    log.error("优惠券异常：{}", e.getMessage(), e);
    return Result.fail(e.getMessage());
}
```

## 八、接口权限设计

### 8.1 权限控制

- 所有接口需要商家登录认证
- 使用现有的MerchantLoginInterceptor进行权限验证
- 通过@RequestMapping("/merchant/voucher")设置统一路径前缀

### 8.2 数据权限

- 商家只能操作自己店铺的优惠券
- 通过MerchantHolder获取当前登录商家信息
- 在Service层进行数据权限校验

```java
// 示例：数据权限校验
Long currentShopId = MerchantHolder.getMerchant().getShopId();
if (!voucher.getShopId().equals(currentShopId)) {
    throw new VoucherException("无权限操作该优惠券");
}
```

## 九、性能优化设计

### 9.1 缓存策略

- **优惠券详情缓存**：Redis缓存，TTL 30分钟
- **秒杀券库存缓存**：Redis缓存，实时更新
- **统计数据缓存**：Redis缓存，TTL 1小时
- **规则模板缓存**：Redis缓存，TTL 24小时

### 9.2 数据库优化

- 添加必要的索引：shop_id, type, status, create_time
- 分页查询优化：使用覆盖索引
- 批量操作优化：使用批量插入/更新

### 9.3 秒杀优化

- **Redis预加载库存**：活动开始前预加载到Redis
- **Lua脚本保证原子性**：库存扣减使用Lua脚本
- **限流和熔断机制**：防止系统过载
- **异步处理**：订单创建异步处理

## 十、接口路径规划

### 10.1 路径前缀

所有商家优惠券管理接口使用统一前缀：`/merchant/voucher`

### 10.2 完整接口路径列表

```
# 基础优惠券管理
POST   /merchant/voucher                    # 创建优惠券
GET    /merchant/voucher/list               # 获取优惠券列表
GET    /merchant/voucher/{id}               # 获取优惠券详情
PUT    /merchant/voucher/{id}/status        # 更新优惠券状态
PUT    /merchant/voucher/{id}/stock         # 更新优惠券库存
PUT    /merchant/voucher/{id}               # 更新优惠券信息

# 规则模板管理
POST   /merchant/voucher/template           # 创建规则模板
GET    /merchant/voucher/template/list      # 获取规则模板列表
PUT    /merchant/voucher/template/{id}      # 更新规则模板
DELETE /merchant/voucher/template/{id}      # 删除规则模板

# 批量操作
POST   /merchant/voucher/batch/create       # 批量创建优惠券
PUT    /merchant/voucher/batch/status       # 批量状态变更
DELETE /merchant/voucher/batch              # 批量删除优惠券

# 记录与统计
GET    /merchant/voucher/{id}/records       # 优惠券领取记录查询
GET    /merchant/voucher/statistics         # 优惠券发放统计
GET    /merchant/voucher/analysis           # 优惠券使用分析

# 秒杀优惠券
POST   /merchant/voucher/seckill            # 创建秒杀券
GET    /merchant/voucher/seckill/{id}/stock # 查询秒杀券库存
POST   /merchant/voucher/seckill/{id}/preheat # 秒杀活动预热
GET    /merchant/voucher/seckill/{id}/monitor # 秒杀活动实时监控
PUT    /merchant/voucher/seckill/{id}       # 更新秒杀券信息
```

---

**设计完成时间**：2024年12月22日
**设计版本**：v1.0
**设计状态**：待确认

## 十一、后续开发计划

### 第一阶段：后端设计 ✅
- API接口设计完成
- DTO类设计完成
- 异常处理设计完成（简化版，不使用错误码）

### 第二阶段：后端实现（待确认后开始）
1. 创建DTO类和异常类
2. 实现Service层业务逻辑
3. 实现Controller层接口
4. 添加数据库索引优化
5. 实现缓存策略
6. 编写单元测试

### 第三阶段：前端对接（后端实现完成后）
1. 前端页面开发
2. API接口联调
3. 功能测试
4. 性能优化

请确认API接口设计是否符合要求，确认后将进入第二阶段的后端实现。
