package org.stock.richman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.stock.richman.model.AssetData;
import org.stock.richman.model.StockEntity;
import org.stock.richman.repository.StockEntityRepository;
import org.stock.richman.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CoinDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RedisTemplate<String, Object> redisTemplate;
    public CoinDataService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private KafkaTemplate<String, AssetData> kafkaTemplate;

//    @Value("${alpha.vantage.api-key}")
//    private String API_KEY;
    @Value("${upbit.base-url}")
    private String BASE_URL;
    @Autowired
    private StockEntityRepository stockEntityRepository;

    public void fetchAndSendCoinData(String symbol) {

        String url = BASE_URL + "/ticker?markets=" + symbol;
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode firstData = jsonNode.get(0);
            long price = firstData.get("trade_price").asLong();

            AssetData assetData = new AssetData();
            assetData.setName(getCoinName(symbol));
            assetData.setSymbol(symbol);
            assetData.setPrice(price);
            assetData.setTimestamp(LocalDateTime.now());

            // Kafka로 전송
            kafkaTemplate.send("stock-data-topic", assetData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getCoinName(String symbol) {
        String cachedName = (String) redisTemplate.opsForValue().get("upbit:"+symbol);

        if (cachedName != null) {
            return cachedName;
        }

        String coinName = stockEntityRepository.findByStockCode(symbol)
                .map(StockEntity::getStockName)
                .orElse("");
        if(!coinName.equals("")){
            redisTemplate.opsForValue().set("upbit:"+symbol, coinName, 1, TimeUnit.HOURS);
        }

        return coinName;
    }

    // 임시
    public void temptest() {
        String UPBIT_API_URL = "https://api.upbit.com/v1/market/all";
        List<String> coinList = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(UPBIT_API_URL, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            for (JsonNode node : jsonNode) {
                StockEntity stockEntity = new StockEntity();
                stockEntity.setStockCode(node.path("market").asText());
                stockEntity.setStockName(node.path("korean_name").asText());
                stockEntity.setStockType("암호화폐");

                stockEntityRepository.save(stockEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
