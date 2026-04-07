package org.example.ash.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.ash.dto.request.AddProductRequest;
import org.example.ash.repository.oracle.IProductRepo;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ProductValidationAspect {

    private final IProductRepo productRepository;

    public ProductValidationAspect(IProductRepo productRepository) {
        this.productRepository = productRepository;
    }

    @Before("@annotation(ValidateProduct)")
    public void validateProduct(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();
        AddProductRequest request = (AddProductRequest) args[0]; // giả sử method nhận 1 Product

        // 1. Validate name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }


        log.info("Product validated successfully");
    }
}

