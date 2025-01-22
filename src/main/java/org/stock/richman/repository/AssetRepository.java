package org.stock.richman.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.stock.richman.model.Asset;
import org.stock.richman.model.StockEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetCode(String AssetCode);
    List<Asset> findByCheckedTrue();
}
