package org.stock.richman.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.stock.richman.model.AssetData;
import org.stock.richman.repository.StockRepository;
import org.stock.richman.service.SlackService;

@Service
public class StockDataConsumer {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private SlackService slackService;

    @KafkaListener(topics = "stock-data-topic", groupId = "stock-group")
    public void consumeStockData(AssetData assetData){
        stockRepository.save(assetData);
    }

    @KafkaListener(topics = "stock-data-topic", groupId = "alert-group")
    public void alertStockData(AssetData assetData){

        long price = (long) assetData.getPrice();
        boolean flagAlert = false;
        String alertType = "";

        if(assetData.getHighValue()<price){
            flagAlert = true;
            alertType = "High";
        }else if(assetData.getLowValue()>price){
            flagAlert = true;
            alertType = "Low";
        }

        if(flagAlert){
            slackService.sendMessage(assetData,alertType);
        }

    }
}
