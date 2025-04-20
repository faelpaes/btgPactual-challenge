package com.br.rafapaes.btgpactual.orderms.controller.dto;

import com.br.rafapaes.btgpactual.orderms.entity.OrderEntity;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

public record OrderResponse(Long orderId,
                            Long customerId,
                            BigDecimal total) {

    public static OrderResponse fromEntity(OrderEntity order) {
        return new OrderResponse(order.getOrderId(), order.getCustomerId(), order.getTotal());
    }
}
