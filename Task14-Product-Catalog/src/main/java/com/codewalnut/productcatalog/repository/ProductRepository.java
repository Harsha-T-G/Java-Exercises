package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    Optional<ProductEntity> findBySkuIgnoreCase(String sku);

    List<ProductEntity> findByActiveTrueAndStockQuantityLessThanEqual(int threshold);

    List<ProductEntity> findByCategoryIgnoreCase(String category);
}
