package com.br.rafapaes.btgpactual.orderms.service;

import com.br.rafapaes.btgpactual.orderms.entity.OrderEntity;
import com.br.rafapaes.btgpactual.orderms.listener.dto.OrderCreatedEvent;
import com.br.rafapaes.btgpactual.orderms.listener.dto.OrderItemEvent;
import com.br.rafapaes.btgpactual.orderms.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void saveOrder() {
        // Arrange
        OrderRepository orderRepository = mock(OrderRepository.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        OrderService orderService = new OrderService(orderRepository, mongoTemplate);

        OrderCreatedEvent event = mock(OrderCreatedEvent.class);
        when(event.codigoPedido()).thenReturn(1L);
        when(event.codigoCliente()).thenReturn(100L);
        when(event.itens()).thenReturn(List.of(
                new OrderItemEvent("Produto A", 2, new BigDecimal("10.00")),
                new OrderItemEvent("Produto B", 1, new BigDecimal("20.00"))
        ));

        // Act
        orderService.saveOrder(event);

        // Assert
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void findAllByCustomerId() {
        // Arrange
        OrderRepository orderRepository = mock(OrderRepository.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        OrderService orderService = new OrderService(orderRepository, mongoTemplate);

        Long customerId = 100L;
        PageRequest pageRequest = PageRequest.of(0, 10);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(1L);
        orderEntity.setCustomerId(customerId);
        orderEntity.setTotal(new BigDecimal("30.00"));

        when(orderRepository.findAllByCustomerId(customerId, pageRequest))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(orderEntity)));

        // Act
        var result = orderService.findAllByCustomerId(customerId, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(customerId, result.getContent().get(0).customerId());
        verify(orderRepository, times(1)).findAllByCustomerId(customerId, pageRequest);
    }

    @Test
    void findTotalOnOrdersByCustomerId() {
        // Arrange
        OrderRepository orderRepository = mock(OrderRepository.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        OrderService orderService = new OrderService(orderRepository, mongoTemplate);

        Long customerId = 100L;
        BigDecimal expectedTotal = new BigDecimal("60.00");

        var document = new org.bson.Document();
        document.put("total", expectedTotal);

        when(mongoTemplate.aggregate(any(), eq(OrderEntity.class), eq(org.bson.Document.class)))
                .thenReturn(new org.springframework.data.mongodb.core.aggregation.AggregationResults<>(
                        List.of(document), new org.bson.Document()));

        // Act
        var result = orderService.findTotalOnOrdersByCustomerId(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(mongoTemplate, times(1)).aggregate(any(), eq(OrderEntity.class), eq(org.bson.Document.class));
    }
}