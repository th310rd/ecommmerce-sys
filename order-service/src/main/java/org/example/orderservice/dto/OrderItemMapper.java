package org.example.orderservice.dto;

import org.example.orderservice.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {
    public OrderItem toEntity(OrderItemRequestDto orderItemRequestDto) {
        if (orderItemRequestDto == null) {
            return null;
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(orderItemRequestDto.getProductId());
        orderItem.setQuantity(orderItemRequestDto.getQuantity());
        return orderItem;
    }

    public OrderItemResponseDto toDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderItemResponseDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}
