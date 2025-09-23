package org.example.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.dto.*;
import org.example.productservice.exceptions.ProductNotFoundException;
import org.example.productservice.model.Product;
import org.example.commonevents.StockUpdateEvent;
import org.example.productservice.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ProductResponseDTO save(ProductRequestDTO productRequestDTO) {
        Product product = ProductMapper.toEntity(productRequestDTO);
        return ProductMapper.toResponseDTO(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));

        oldProduct.setName(productRequestDTO.getName());
        oldProduct.setDescription(productRequestDTO.getDescription());
        oldProduct.setCategory(productRequestDTO.getCategory());
        oldProduct.setStockQuantity(productRequestDTO.getStockQuantity());
        oldProduct.setPrice(productRequestDTO.getPrice());
        oldProduct.setImageUrl(productRequestDTO.getImageUrl());
        if (productRequestDTO.getRating() != null) {
            oldProduct.setRating(ProductMapper.toEntity(productRequestDTO).getRating());
        }

        return ProductMapper.toResponseDTO(productRepository.save(oldProduct));
    }


    @Transactional(readOnly = true)
    public ProductResponseDTO findProductByName(String name) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with name " + name));
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllPaged(int page,int size,String sortBy,String direction){
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,size,sort);
        return productRepository.findAll(pageable)
                .map(ProductMapper::toResponseDTO);
    }

    @Transactional
    public List<ProductResponseDTO> saveAll(List<ProductRequestDTO> productRequestDTOS) {
        List<Product> products = productRequestDTOS.stream()
                .map(ProductMapper::toEntity)
                .toList();
        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts.stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @KafkaListener(topics = "stock-update", groupId = "product-service")
    public void handleStockUpdateEvent(StockUpdateEvent stockUpdateEvent) {
        Product product = productRepository.findById(stockUpdateEvent.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + stockUpdateEvent.getProductId()));

        int newStockQuantity = product.getStockQuantity() - stockUpdateEvent.getQuantity();
        if (newStockQuantity < 0) {
            log.warn("{} is negative", stockUpdateEvent.getProductId());
            return;
        }
        product.setStockQuantity(newStockQuantity);
        productRepository.save(product);
        log.info("{} stock updated", stockUpdateEvent.getProductId());
    }
}
