package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.entity.*;
import com.hmdp.mapper.*;
import com.hmdp.service.ICartService;
import com.hmdp.service.IOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单信息表 服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ICartService cartService;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private OrderCommentMapper orderCommentMapper;

    @Override
    @Transactional
    public Result createOrderFromCart(CreateOrderDTO createOrderDTO) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 获取用户购物车
        Result cartResult = cartService.getCurrentUserCart();
        if (!cartResult.getSuccess()) {
            return Result.fail("获取购物车失败");
        }

        CartDTO cartDTO = (CartDTO) cartResult.getData();
        if (cartDTO == null || cartDTO.getItems() == null || cartDTO.getItems().isEmpty()) {
            return Result.fail("购物车为空");
        }

        // 3. 验证购物车商品并按商铺分组
        Map<Long, List<CartItemDTO>> shopItemsMap = new HashMap<>();
        for (CartItemDTO item : cartDTO.getItems()) {
            // 验证商品状态和库存
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                return Result.fail("商品不存在：" + item.getProductTitle());
            }
            if (product.getStatus() != 1) {
                return Result.fail("商品已下架：" + item.getProductTitle());
            }
            if (product.getStock() < item.getQuantity()) {
                return Result.fail("商品库存不足：" + item.getProductTitle());
            }

            // 按商铺分组
            Long shopId = product.getShopId();
            shopItemsMap.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
        }

        // 4. 为每个商铺创建订单
        List<Long> orderIds = new ArrayList<>();
        for (Map.Entry<Long, List<CartItemDTO>> entry : shopItemsMap.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemDTO> items = entry.getValue();

            // 创建订单
            Long orderId = createSingleOrder(userId, shopId, items, createOrderDTO);
            if (orderId == null) {
                throw new RuntimeException("创建订单失败");
            }
            orderIds.add(orderId);
        }

        // 5. 清空购物车
        cartService.clearCurrentUserCart();

        // 6. 返回订单ID列表
        return Result.success(orderIds);
    }

    /**
     * 创建单个订单
     */
    private Long createSingleOrder(Long userId, Long shopId, List<CartItemDTO> items, CreateOrderDTO createOrderDTO) {
        // 1. 计算订单总金额
        long totalAmount = items.stream()
                .mapToLong(CartItemDTO::getSubtotalPrice)
                .sum();

        // 2. 生成订单ID
        long orderId = redisIdWorker.nextId("order");

        // 3. 创建订单
        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo("ORDER" + orderId);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setTotalAmount(totalAmount);
        order.setStatus(1); // 待支付
        order.setPayType(createOrderDTO.getPayType());
        order.setAddress(createOrderDTO.getAddress());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean saveOrder = save(order);
        if (!saveOrder) {
            return null;
        }

        // 4. 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDTO item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductTitle(item.getProductTitle());
            orderItem.setProductImage(item.getProductImage());
            orderItem.setProductPrice(item.getProductPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSpecifications(item.getSpecifications());
            orderItem.setSubtotalAmount(item.getSubtotalPrice());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        // 批量插入订单项
        for (OrderItem orderItem : orderItems) {
            orderItemMapper.insert(orderItem);
        }

        // 5. 扣减商品库存
        for (CartItemDTO item : items) {
            boolean success = productMapper.updateStock(item.getProductId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("扣减库存失败：" + item.getProductTitle());
            }
        }

        return orderId;
    }

    @Override
    public Result getOrderById(Long orderId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 3. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权访问该订单");
        }

        // 4. 构建订单DTO
        return Result.success(buildOrderDTO(order));
    }

    @Override
    public Result getCurrentUserOrders(Integer status, Integer current, Integer size) {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return getUserOrders(userId, status, current, size);
    }

    @Override
    public Result getUserOrders(Long userId, Integer status, Integer current, Integer size) {
        // 1. 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        // 2. 分页查询
        Page<Order> page = new Page<>(current, size);
        Page<Order> orderPage = page(page, queryWrapper);

        // 3. 构建DTO列表
        List<OrderDTO> orderDTOs = orderPage.getRecords().stream()
                .map(this::buildOrderDTO)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", orderPage.getTotal());
        result.put("orders", orderDTOs);
        result.put("current", orderPage.getCurrent());
        result.put("size", orderPage.getSize());

        return Result.success(result);
    }

    @Override
    @Transactional
    public Result updateOrderStatus(Long orderId, Integer status) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 3. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作该订单");
        }

        // 4. 验证状态转换合法性
        if (!isValidStatusTransition(order.getStatus(), status)) {
            return Result.fail("订单状态转换不合法");
        }

        // 5. 更新订单状态
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());

        if (status == 2) { // 已支付
            order.setPayTime(LocalDateTime.now());
        }

        boolean success = updateById(order);
        if (!success) {
            return Result.fail("更新订单状态失败");
        }

        return Result.success("订单状态更新成功");
    }

    @Override
    @Transactional
    public Result cancelOrder(Long orderId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 3. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作该订单");
        }

        // 4. 验证订单状态
        if (order.getStatus() != 1) {
            return Result.fail("只能取消待支付订单");
        }

        // 5. 取消订单
        order.setStatus(4); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(order);
        if (!success) {
            return Result.fail("取消订单失败");
        }

        // 6. 恢复商品库存
        restoreProductStock(orderId);

        return Result.success("订单取消成功");
    }

    @Override
    @Transactional
    public Result payOrder(Long orderId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 3. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作该订单");
        }

        // 4. 验证订单状态
        if (order.getStatus() != 1) {
            return Result.fail("订单状态不正确");
        }

        // 5. 模拟支付成功（实际项目中需要对接支付系统）
        order.setStatus(2); // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(order);
        if (!success) {
            return Result.fail("支付失败");
        }

        return Result.success("支付成功");
    }

    @Override
    public Result getUserCompletedOrdersByShop(Long shopId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 构建查询条件：用户ID + 商店ID + 已完成状态
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("shop_id", shopId)
                   .eq("status", 3) // 3表示已完成
                   .orderByDesc("create_time");

        // 3. 查询订单
        List<Order> orders = list(queryWrapper);

        // 4. 构建DTO列表
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::buildOrderDTO)
                .collect(Collectors.toList());

        return Result.success(orderDTOs);
    }

    /**
     * 构建订单DTO
     */
    private OrderDTO buildOrderDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setShopId(order.getShopId());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setStatusDesc(getStatusDesc(order.getStatus()));
        orderDTO.setPayType(order.getPayType());
        orderDTO.setPayTypeDesc(getPayTypeDesc(order.getPayType()));
        orderDTO.setAddress(order.getAddress());
        orderDTO.setPayTime(order.getPayTime());
        orderDTO.setCreateTime(order.getCreateTime());
        orderDTO.setUpdateTime(order.getUpdateTime());

        // 获取商铺名称
        Shop shop = shopMapper.selectById(order.getShopId());
        if (shop != null) {
            orderDTO.setShopName(shop.getName());
        }

        // 获取订单项
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setOrderId(item.getOrderId());
            itemDTO.setProductId(item.getProductId());
            itemDTO.setProductTitle(item.getProductTitle());
            itemDTO.setProductImage(item.getProductImage());
            itemDTO.setProductPrice(item.getProductPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSpecifications(item.getSpecifications());
            itemDTO.setSubtotalAmount(item.getSubtotalAmount());
            itemDTO.setCreateTime(item.getCreateTime());
            return itemDTO;
        }).collect(Collectors.toList());

        orderDTO.setItems(itemDTOs);
        return orderDTO;
    }

    /**
     * 获取订单状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知状态";
        }
    }

    /**
     * 获取支付方式描述
     */
    private String getPayTypeDesc(Integer payType) {
        if (payType == null) return "";
        switch (payType) {
            case 1: return "余额支付";
            case 2: return "支付宝";
            case 3: return "微信";
            default: return "未知支付方式";
        }
    }

    /**
     * 验证订单状态转换是否合法
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        // 待支付 -> 已支付、已取消
        if (currentStatus == 1) {
            return newStatus == 2 || newStatus == 4;
        }
        // 已支付 -> 已完成、已退款
        if (currentStatus == 2) {
            return newStatus == 3 || newStatus == 5;
        }
        // 其他状态不允许转换
        return false;
    }

    /**
     * 恢复商品库存
     */
    private void restoreProductStock(Long orderId) {
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem item : orderItems) {
            // 恢复库存（增加库存）
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    // ========== 用户端订单管理方法实现 ==========

    @Override
    public Result getUserOrderList(Long userId, UserOrderQueryDTO queryDTO) {
        log.info("获取用户订单列表，用户ID：{}，查询参数：{}", userId, queryDTO);
        
        // 1. 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        // 强制使用传入的userId，忽略queryDTO中可能存在的userId
        queryWrapper.eq("user_id", userId);
        log.info("强制使用用户ID：{}", userId);

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        // 日期范围筛选
        if (queryDTO.getStartDate() != null && !queryDTO.getStartDate().isEmpty()) {
            queryWrapper.ge("create_time", queryDTO.getStartDate() + " 00:00:00");
        }
        if (queryDTO.getEndDate() != null && !queryDTO.getEndDate().isEmpty()) {
            queryWrapper.le("create_time", queryDTO.getEndDate() + " 23:59:59");
        }

        queryWrapper.orderByDesc("create_time");

        // 2. 分页查询
        Page<Order> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Order> orderPage = page(page, queryWrapper);
        
        log.info("查询到订单数量：{}", orderPage.getRecords().size());

        // 3. 构建用户订单列表DTO
        List<UserOrderListDTO> orderListDTOs = orderPage.getRecords().stream()
                .map(this::buildUserOrderListDTO)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", orderListDTOs);
        result.put("total", orderPage.getTotal());
        result.put("current", orderPage.getCurrent());
        result.put("size", orderPage.getSize());

        return Result.success(result);
    }

    @Override
    public Result getUserOrderDetail(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权限访问该订单");
        }

        // 3. 构建用户订单详情DTO
        UserOrderDetailDTO detailDTO = buildUserOrderDetailDTO(order);

        return Result.success(detailDTO);
    }

    @Override
    @Transactional
    public Result payUserOrder(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权限操作该订单");
        }

        // 3. 验证订单状态
        if (order.getStatus() != 1) {
            return Result.fail("订单状态不允许支付");
        }

        // 4. 更新订单状态为已支付
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(order);
        if (!success) {
            return Result.fail("支付失败");
        }

        return Result.success("支付成功");
    }

    @Override
    @Transactional
    public Result cancelUserOrder(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权限操作该订单");
        }

        // 3. 验证订单状态
        if (order.getStatus() != 1) {
            return Result.fail("只能取消待支付订单");
        }

        // 4. 更新订单状态为已取消
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(order);
        if (!success) {
            return Result.fail("取消订单失败");
        }

        // 5. 恢复商品库存
        restoreProductStock(orderId);

        return Result.success("订单取消成功");
    }

    @Override
    @Transactional
    public Result confirmUserOrder(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权限操作该订单");
        }

        // 3. 验证订单状态
        if (order.getStatus() != 2) {
            return Result.fail("只能确认已支付订单");
        }

        // 4. 更新订单状态为已完成
        order.setStatus(3);
        order.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(order);
        if (!success) {
            return Result.fail("确认收货失败");
        }

        return Result.success("确认收货成功");
    }

    @Override
    @Transactional
    public Result commentUserOrder(Long userId, Long orderId, OrderCommentDTO commentDTO) {
        // 1. 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权限操作该订单");
        }

        // 3. 验证订单状态
        if (order.getStatus() != 3) {
            return Result.fail("只能评价已完成订单");
        }

        // 4. 检查是否已经评价过
        QueryWrapper<OrderComment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("order_id", orderId);
        OrderComment existingComment = orderCommentMapper.selectOne(commentQueryWrapper);
        if (existingComment != null) {
            return Result.fail("该订单已评价过");
        }

        // 5. 创建订单评价
        OrderComment orderComment = new OrderComment();
        orderComment.setUserId(userId);
        orderComment.setShopId(order.getShopId());
        orderComment.setOrderId(orderId);
        orderComment.setScore(commentDTO.getRating());
        orderComment.setContent(commentDTO.getContent());

        // 处理图片列表
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            orderComment.setImages(String.join(",", commentDTO.getImages()));
        }

        orderComment.setStatus(0); // 未回复
        orderComment.setCreateTime(LocalDateTime.now());

        boolean success = orderCommentMapper.insert(orderComment) > 0;
        if (!success) {
            return Result.fail("评价失败");
        }

        return Result.success("评价成功");
    }

    /**
     * 构建用户订单列表DTO
     */
    private UserOrderListDTO buildUserOrderListDTO(Order order) {
        UserOrderListDTO listDTO = new UserOrderListDTO();
        listDTO.setId(order.getId());

        // 设置订单号
        listDTO.setOrderNo(order.getOrderNo() != null ? order.getOrderNo() : "ORDER" + order.getId());

        listDTO.setTotalAmount(order.getTotalAmount());
        listDTO.setStatus(order.getStatus());
        listDTO.setCreateTime(order.getCreateTime());

        // 获取商铺名称
        Shop shop = shopMapper.selectById(order.getShopId());
        if (shop != null) {
            listDTO.setShopName(shop.getName());
        }

        // 获取订单项信息，生成商品摘要和数量
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        if (!orderItems.isEmpty()) {
            // 生成商品摘要
            if (orderItems.size() == 1) {
                listDTO.setProductSummary(orderItems.get(0).getProductTitle());
            } else {
                listDTO.setProductSummary(orderItems.get(0).getProductTitle() + " 等" + orderItems.size() + "件商品");
            }

            // 计算商品总数量
            int totalCount = orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
            listDTO.setItemCount(totalCount);
        }

        return listDTO;
    }

    /**
     * 构建用户订单详情DTO
     */
    private UserOrderDetailDTO buildUserOrderDetailDTO(Order order) {
        UserOrderDetailDTO detailDTO = new UserOrderDetailDTO();

        // 订单基本信息
        detailDTO.setId(order.getId());
        detailDTO.setUserId(order.getUserId());
        detailDTO.setShopId(order.getShopId());
        detailDTO.setTotalAmount(order.getTotalAmount());
        detailDTO.setStatus(order.getStatus());
        detailDTO.setPayType(order.getPayType());
        detailDTO.setAddress(order.getAddress());

        // 时间信息
        detailDTO.setCreateTime(order.getCreateTime());
        detailDTO.setPayTime(order.getPayTime());

        // 获取商铺名称
        Shop shop = shopMapper.selectById(order.getShopId());
        if (shop != null) {
            detailDTO.setShopName(shop.getName());
        }

        // 获取订单项列表
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setOrderId(item.getOrderId());
            itemDTO.setProductId(item.getProductId());
            itemDTO.setProductTitle(item.getProductTitle());
            itemDTO.setProductImage(item.getProductImage());
            itemDTO.setProductPrice(item.getProductPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSpecifications(item.getSpecifications());
            itemDTO.setSubtotalAmount(item.getSubtotalAmount());
            itemDTO.setCreateTime(item.getCreateTime());
            return itemDTO;
        }).collect(Collectors.toList());

        detailDTO.setItems(itemDTOs);

        // 计算金额明细
        long itemsAmount = orderItems.stream().mapToLong(OrderItem::getSubtotalAmount).sum();
        detailDTO.setItemsAmount(itemsAmount);
        detailDTO.setDiscountAmount(0L); // 暂时设为0，后续可扩展优惠功能
        detailDTO.setDeliveryFee(0L); // 暂时设为0，后续可扩展运费功能
        detailDTO.setActualAmount(order.getTotalAmount());

        return detailDTO;
    }
}
