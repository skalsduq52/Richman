package org.stock.richman.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.stock.richman.model.AssetData;

@Repository
public interface StockRepository extends JpaRepository<AssetData, Long> {
}
