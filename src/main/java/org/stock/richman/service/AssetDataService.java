package org.stock.richman.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.stock.richman.model.Asset;
import org.stock.richman.model.StockEntity;
import org.stock.richman.repository.AssetRepository;
import org.stock.richman.repository.StockEntityRepository;

import java.util.*;

@Service
public class AssetDataService {

    @Autowired
    StockEntityRepository stockEntityRepository;
    @Autowired
    AssetRepository assetRepository;
    @Autowired
    CoinDataService coinDataService;
    @Autowired
    StockDataService stockDataService;

    public List<Asset> getAssetsData() {
        List<Asset> assets = new ArrayList<Asset>();
        assets = assetRepository.findAll();
        return assets;
    }

    public List<Asset> getAssetsPriceData( ) {
        List<Asset> assets = new ArrayList<Asset>();
        assets = assetRepository.findByCheckedTrue();

        for(Asset asset : assets) {
            if("주식".equals(asset.getAssetType())){
                stockDataService.fetchAndSendStockData(asset.getAssetCode());
            }else{
                coinDataService.fetchAndSendCoinData(asset.getAssetCode());
            }
        }


        return assets;
    }

    public ResponseEntity<?> createAssetData(Asset asset) {

        StockEntity stockEntity = stockEntityRepository.findByStockCode(asset.getAssetCode()).orElse(null);
        if (stockEntity == null) {
            Map<String, String> errorResponse = Map.of("error", "유효하지 않은 종목코드 입니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Asset savedAsset = assetRepository.findByAssetCode(asset.getAssetCode()).orElse(null);
        if (savedAsset != null) {
            Map<String, String> errorResponse = Map.of("duplicate", "이미 등록된 종목코드 입니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        asset.setAssetType(stockEntity.getStockType());
        asset.setAssetName(stockEntity.getStockName());
        savedAsset = assetRepository.save(asset);
        return ResponseEntity.ok(savedAsset);

    }

    public ResponseEntity<?> deleteAssetData(String assetCode) {
        Optional<Asset> asset = assetRepository.findByAssetCode(assetCode);

        if (asset.isPresent()) {
            assetRepository.delete(asset.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<?> updateAssetData(String assetCode, Map<String,Boolean> requestBody) {
        Optional<Asset> asset = assetRepository.findByAssetCode(assetCode);
        if (asset.isPresent()) {
            Asset assetData = asset.get();
            assetData.setChecked(requestBody.get("checked"));
            assetRepository.save(assetData);
            return ResponseEntity.ok("good");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
