package org.stock.richman.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock.richman.model.StockData;
import org.stock.richman.repository.StockRepository;
import org.stock.richman.service.SlackService;

@Service
public class StockDataConsumer {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private SlackService slackService;

    @KafkaListener(topics = "stock-data-topic", groupId = "stock-group")
    public void consumeStockData(StockData stockData){
        stockRepository.save(stockData);
    }

    @KafkaListener(topics = "stock-data-topic", groupId = "alert-group")
    public void alertStockData(StockData stockData){

        long price = (long) stockData.getPrice();
        boolean flagAlert = false;
        String alertType = "";

        if(stockData.getHighValue()<price){
            flagAlert = true;
            alertType = "High";
        }else if(stockData.getLowValue()>price){
            flagAlert = true;
            alertType = "Low";
        }

        if(flagAlert){
            slackService.sendMessage(stockData,alertType);
        }

    }
}
