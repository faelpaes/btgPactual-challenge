package com.br.rafapaes.btgpactual.orderms.service;

import com.br.rafapaes.btgpactual.orderms.controller.dto.OrderResponse;
import com.br.rafapaes.btgpactual.orderms.entity.OrderEntity;
import com.br.rafapaes.btgpactual.orderms.entity.OrderItem;
import com.br.rafapaes.btgpactual.orderms.listener.dto.OrderCreatedEvent;
import com.br.rafapaes.btgpactual.orderms.repository.OrderRepository;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void saveOrder(OrderCreatedEvent event) {
        var entity = new OrderEntity();
        entity.setOrderId(event.codigoPedido());
        entity.setCustomerId(event.codigoCliente());
        entity.setItems(getOrderItems(event));
        entity.setTotal(getTotal(event));

        orderRepository.save(entity);
    }

    public Page<OrderResponse> findAllByCustomerId(Long customerId, PageRequest pageRequest) {
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        return orders.map(OrderResponse::fromEntity);
    }

    public BigDecimal findTotalOnOrdersByCustomerId(Long customerId) {
        var aggregations = newAggregation(
                match(Criteria.where("customerId").is(customerId)),
                group().sum("total").as("total")
        );

        var response = mongoTemplate.aggregate(aggregations, OrderEntity.class, Document.class);

        return new BigDecimal(Objects.requireNonNull(response.getUniqueMappedResult()).get("total").toString());
    }

    private BigDecimal getTotal(OrderCreatedEvent event) {
        return event.itens().stream()
                .map(item -> item.preco().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<OrderItem> getOrderItems(OrderCreatedEvent event) {
        return event.itens().stream()
                .map(item -> new OrderItem(item.produto(), item.quantidade(), item.preco()))
                .toList();
    }
}
