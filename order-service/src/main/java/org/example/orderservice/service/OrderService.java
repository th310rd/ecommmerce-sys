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
import java.util.Optional;

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
        Order order = new  Order();
        order.setStatus(Status.PENDING);
        BigDecimal totalPrice = BigDecimal.ZERO;
        order.setItems(orderRequestDto.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .toList());

        for (OrderItem item : order.getItems()){
            Product product = client.getProduct(item.getProductId());
            if (item.getQuantity() > product.getStockQuantity()){
                order.setStatus(Status.CANCELLED);
                throw new OutOfStockException("Not enough product in the stock");
            }
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setPrice(product.getPrice());
            order.setTotalPrice(totalPrice);
            order.setOrderDate(LocalDateTime.now());
        }
        orderRepository.save(order);
        publishStockUpdate(order);
        return orderMapper.toDto(order);
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

    public void publishStockUpdate(Order order) {
        for (OrderItem item : order.getItems()){
            StockUpdateEvent event = new StockUpdateEvent(item.getProductId(), item.getQuantity());
            kafkaTemplate.send("stock-update", event);
        }
    }
}
