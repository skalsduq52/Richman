package org.stock.richman.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock.richman.model.StockData;

@Service
public class StockDataConsumer {

    @KafkaListener(topics = "stock-data-topic", groupId = "stock-group")
    public void consumeStockData(StockData stockData){
        System.out.println("===============================================");
        System.out.println("Received Stock Data: " + stockData.getSymbol());
        System.out.println("===============================================");
    }
}
