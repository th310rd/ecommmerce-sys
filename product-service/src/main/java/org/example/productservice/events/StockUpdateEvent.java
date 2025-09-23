package org.example.productservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateEvent {
    private Long productId;
    private Integer quantity;
}
