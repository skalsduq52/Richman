package org.stock.richman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stock.richman.model.Asset;
import org.stock.richman.repository.AssetRepository;
import org.stock.richman.service.AssetDataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/asset")
public class RichmanController {

    @Autowired
    private AssetDataService assetDataService;
    @Autowired
    private AssetRepository assetRepository;

    @GetMapping
    public List<Asset> getAssets() {
        return assetDataService.getAssetsData();
    }

    @GetMapping("/price")
    public List<Asset> getAssetsPrice() {
        return assetDataService.getAssetsPriceData();
    }

    @PostMapping
    public ResponseEntity<?> createAsset(@RequestBody Asset asset) {
        return assetDataService.createAssetData(asset);
    }

    @DeleteMapping("/{assetCode}")
    public ResponseEntity<?> deleteAsset(@PathVariable String assetCode) {
        return assetDataService.deleteAssetData(assetCode);
    }

    @PatchMapping("/{assetCode}")
    public ResponseEntity<?> updateChecked(@PathVariable String assetCode, @RequestBody Map<String, Boolean> requestBody) {
        return assetDataService.updateAssetData(assetCode, requestBody);
    }




}
