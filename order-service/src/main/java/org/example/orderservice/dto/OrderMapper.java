package org.example.orderservice.dto;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;

    public Order toEntity(OrderRequestDto orderRequestDto) {
        if (orderRequestDto == null) {
            return null;
        }

        Order order = new Order();
        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()){
            List<OrderItem> items = orderRequestDto.getOrderItems().stream()
                    .map(orderItemMapper::toEntity)
                    .toList();
            items.forEach(order::addItem);
        }

        return order;
    }

    public OrderResponseDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDto> items = order.getItems().stream()
                        .map(orderItemMapper::toDto)
                                .toList();


        return OrderResponseDto.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderNumber(order.getOrderNumber())
                .items(items)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
