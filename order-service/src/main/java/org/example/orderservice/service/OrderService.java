package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.config.Product;
import org.example.orderservice.config.ProductServiceClient;
import org.example.orderservice.dto.*;
import org.example.commonevents.StockUpdateEvent;
import org.example.orderservice.exceptions.OrderNotFoundException;
import org.example.orderservice.exceptions.OutOfStockException;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.Status;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final KafkaTemplate<String, StockUpdateEvent> kafkaTemplate;
    private final ProductServiceClient client;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) throws OutOfStockException {
        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setOrderDate(LocalDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> items = orderRequestDto.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .toList();

        for (OrderItem item : items) {
            Product product = client.getProduct(item.getProductId());
            if (item.getQuantity() > product.getStockQuantity()) {
                order.setStatus(Status.CANCELLED);
                throw new OutOfStockException("Not enough product in stock");
            }
            item.setPrice(product.getPrice());
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setItems(items);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
        publishStockUpdate(order);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + id + " not found"));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, Status newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + id + " not found"));

        if (order.getStatus() == Status.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled order");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        updateOrderStatus(id, Status.CANCELLED);
    }

    private void publishStockUpdate(Order order) {
        for (OrderItem item : order.getItems()) {
            StockUpdateEvent event = new StockUpdateEvent(item.getProductId(), item.getQuantity());
            kafkaTemplate.send("stock-update", event);
        }
    }
}
