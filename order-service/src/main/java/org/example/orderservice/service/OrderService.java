package org.example.orderservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.*;
import org.example.orderservice.exceptions.OrderNotFoundException;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.Status;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, ProductDetailsRequest> kafkaTemplate;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    @KafkaListener(topics = "product-details-response-topic",groupId = "order-service")
    public void handleProductDetailsResponse(ProductDetailsResponse response) {
        Order order = orderRepository.findById(response.getOrderId()).orElseThrow(() -> new OrderNotFoundException("Order not found"));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (ProductDetails details : response.getProductDetails()){
            OrderItem orderItem = order.getItems().stream()
                    .filter(item -> item.getProductId().equals(details.getProductId()) )
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            orderItem.setPrice(details.getPrice());
            if (details.getStockQuantity() < orderItem.getQuantity()){
                orderItem.setQuantity(details.getStockQuantity());
            }
            totalPrice = totalPrice.add(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        order.setTotalPrice(totalPrice);
        order.setStatus(Status.CREATED);
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponseDto createOrder(@Valid OrderRequestDto orderRequestDto) {
        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setItems(orderRequestDto.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .toList());
        Order savedOrder = orderRepository.save(order);

        ProductDetailsRequest request = new ProductDetailsRequest();
        request.setOrderId(savedOrder.getId());
        request.setProductIds(savedOrder.getItems().stream()
                .map(OrderItem::getProductId)
                        .toList()
                );
        kafkaTemplate.send("order-details-request-topic", request);
        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponseDto> getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(()->new OrderNotFoundException("Order with id " + id + "not found"));
        return Optional.of(orderMapper.toDto(order));
    }

    @Transactional
    public OrderResponseDto updateOrderById(Long id, Status status) {
        Order oldOrder = orderRepository.findById(id)
                .orElseThrow(()->new OrderNotFoundException("Order with id " + id + "not found"));

        oldOrder.setStatus(status);
        orderRepository.save(oldOrder);
        return orderMapper.toDto(oldOrder);
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order oldOrder = orderRepository.findById(id)
                        .orElseThrow(()->new OrderNotFoundException("Order with id " + id + "not found"));
        orderRepository.delete(oldOrder);
    }




}
