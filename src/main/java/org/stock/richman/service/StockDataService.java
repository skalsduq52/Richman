package org.stock.richman.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.stock.richman.model.StockData;
import org.stock.richman.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

@Service
public class StockDataService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private KafkaTemplate<String, StockData> kafkaTemplate;

//    @Value("${alpha.vantage.api-key}")
//    private String API_KEY;
    @Value("${upbit.base-url}")
    private String BASE_URL;

    public void fetchAndSendStockData(String symbol) {

        String url = BASE_URL + "/ticker?markets=" + symbol;
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode firstData = jsonNode.get(0);
            long price = firstData.get("trade_price").asLong();

            StockData stockData = new StockData();
            stockData.setSymbol(symbol);
            stockData.setPrice(price);
            stockData.setTimestamp(LocalDateTime.now());

            // MySQL에 저장
            stockRepository.save(stockData);

            // Kafka로 전송
            kafkaTemplate.send("stock-data-topic", stockData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
