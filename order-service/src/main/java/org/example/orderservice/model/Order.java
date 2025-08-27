package org.example.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_number", nullable = false, unique = true)
    private UUID orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "order",
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<OrderItem> items =  new ArrayList<>();

    @PrePersist
    public void prePersist()
    {
        if (orderDate == null){
            orderDate = LocalDateTime.now();
        }
        if (orderNumber == null){
            orderNumber = UUID.randomUUID();
        }
        if (totalPrice == null){
            totalPrice = BigDecimal.ZERO;
        }
    }

    public void addItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }

}
