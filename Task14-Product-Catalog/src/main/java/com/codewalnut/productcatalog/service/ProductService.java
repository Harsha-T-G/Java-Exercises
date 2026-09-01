package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.ProductLimitReachedException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.mapper.ProductEntityMapper;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEntityMapper productEntityMapper;
    private final CatalogProperties catalogProperties;

    public ProductService(
            ProductRepository productRepository,
            ProductEntityMapper productEntityMapper,
            CatalogProperties catalogProperties) {
        this.productRepository = productRepository;
        this.productEntityMapper = productEntityMapper;
        this.catalogProperties = catalogProperties;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.count() >= catalogProperties.getMaximumProducts()) {
            throw new ProductLimitReachedException(catalogProperties.getMaximumProducts());
        }
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        UUID id = UUID.randomUUID();
        ProductEntity entity = productEntityMapper.toNewEntity(id, request);
        try {
            ProductEntity saved = productRepository.save(entity);
            return productEntityMapper.toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(request.getSku());
        }
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productEntityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        int threshold = catalogProperties.getLowStockThreshold();
        return productRepository.findByActiveTrueAndStockQuantityLessThanEqual(threshold).stream()
                .map(productEntityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productEntityMapper.toResponse(entity);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.getSku(), id)) {
            throw new DuplicateSkuException(request.getSku());
        }
        productEntityMapper.applyUpdate(entity, request);
        try {
            ProductEntity saved = productRepository.save(entity);
            return productEntityMapper.toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(request.getSku());
        }
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
