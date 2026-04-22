package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.product.request.CreateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.request.UpdateProductRequest;
import com.fedeval.cartservicechallenge.dtos.product.response.ProductResponse;
import com.fedeval.cartservicechallenge.exceptions.ForbiddenException;
import com.fedeval.cartservicechallenge.exceptions.ResourceNotFoundException;
import com.fedeval.cartservicechallenge.mappers.ProductMapper;
import com.fedeval.cartservicechallenge.models.Category;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.Product;
import com.fedeval.cartservicechallenge.models.enums.RoleType;
import com.fedeval.cartservicechallenge.repositories.CategoryRepository;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import com.fedeval.cartservicechallenge.repositories.ProductRepository;
import com.fedeval.cartservicechallenge.services.ProductService;
import com.fedeval.cartservicechallenge.utils.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ClientRepository clientRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request, String email) {

        Client authenticatedClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated client not found"));

        if (authenticatedClient.getRole() != RoleType.ADMIN) {
            throw new ForbiddenException("You are not allowed to perform this action");
        }


        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String productCode = CodeGenerator.generateProductCode();

        while (productRepository.findByCode(productCode).isPresent()) {
            productCode = CodeGenerator.generateProductCode();
        }

        Product product = Product.builder()
                .name(request.getName())
                .category(category)
                .stock(request.getStock())
                .price(request.getPrice())
                .code(productCode)
                .build();

        Product savedProduct = productRepository.save(product);

        return ProductResponse.builder()
                .id(savedProduct.getId())
                .code(savedProduct.getCode())
                .name(savedProduct.getName())
                .categoryName(savedProduct.getCategory().getName())
                .stock(savedProduct.getStock())
                .price(savedProduct.getPrice())
                .build();
    }

    @Override
    public ProductResponse updateProduct(String productCode, UpdateProductRequest request, String email) {

        Client authenticatedClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated client not found"));

        if (authenticatedClient.getRole() != RoleType.ADMIN) {
            throw new ForbiddenException("You are not allowed to perform this action");
        }

        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        Product updatedProduct = productRepository.save(product);

        return ProductMapper.toResponse(updatedProduct);
    }

}
