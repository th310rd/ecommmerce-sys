package org.example.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.productservice.dto.ProductRequestDTO;
import org.example.productservice.dto.ProductResponseDTO;
import org.example.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> save(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO createdProduct = productService.save(productRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PostMapping("/save")
    public ResponseEntity<List<ProductResponseDTO>> saveProducts(@Valid @RequestBody List<ProductRequestDTO> productRequestDTOList) {
        List<ProductResponseDTO> createdProducts = productService.saveAll(productRequestDTOList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-name")
    public ResponseEntity<ProductResponseDTO> findByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.findProductByName(name));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ProductResponseDTO>> findAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
            ){
        return ResponseEntity.ok(productService.findAllPaged(page,size,sortBy,direction));
    }
}
