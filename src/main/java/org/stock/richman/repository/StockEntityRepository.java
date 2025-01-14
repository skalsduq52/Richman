package org.stock.richman.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.stock.richman.model.StockEntity;

import java.util.Optional;

@Repository
public interface StockEntityRepository extends JpaRepository<StockEntity, Long> {
    Optional<StockEntity> findByStockCode(String stockCode);
}
