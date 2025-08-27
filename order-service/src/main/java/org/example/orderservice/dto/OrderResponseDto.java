package org.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long id;
    private UUID orderNumber;
    private Status status;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private List<OrderItemResponseDto> items;
}