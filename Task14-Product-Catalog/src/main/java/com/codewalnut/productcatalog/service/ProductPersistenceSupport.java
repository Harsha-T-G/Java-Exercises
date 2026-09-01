package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceSupport {

    private static final String SKU_UNIQUE_CONSTRAINT = "products_sku_unique_lower";

    private final ProductRepository productRepository;

    public ProductPersistenceSupport(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    ProductEntity saveAndFlush(ProductEntity entity, String skuForDuplicateMapping) {
        try {
            return productRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            DuplicateSkuException duplicateSku = mapDuplicateSku(exception, skuForDuplicateMapping);
            if (duplicateSku != null) {
                throw duplicateSku;
            }
            throw exception;
        }
    }

    private DuplicateSkuException mapDuplicateSku(DataIntegrityViolationException exception, String sku) {
        if (isSkuConstraintViolation(exception)) {
            return new DuplicateSkuException(sku);
        }
        return null;
    }

    private boolean isSkuConstraintViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof ConstraintViolationException constraintViolation) {
            String constraintName = constraintViolation.getConstraintName();
            return SKU_UNIQUE_CONSTRAINT.equals(constraintName);
        }
        String message = exception.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase().contains(SKU_UNIQUE_CONSTRAINT);
    }
}
