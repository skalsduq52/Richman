package org.stock.richman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.stock.richman.service.StockDataService;

@RestController
public class StockController {

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/stock")
    public String fetchAndSendStockData(@RequestParam String symbol) {
        stockDataService.fetchAndSendStockData(symbol);
        return "기업 " + symbol + "의 주가는";
    }

}
