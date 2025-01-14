package org.stock.richman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.stock.richman.model.StockData;
import org.stock.richman.model.StockEntity;
import org.stock.richman.repository.StockEntityRepository;
import org.stock.richman.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class StockDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RedisTemplate<String, Object> redisTemplate;
    public StockDataService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockEntityRepository stockEntityRepository;
    @Autowired
    private KafkaTemplate<String, StockData> kafkaTemplate;

    @Value("${koreainvestment.api.app-key}")
    private String APP_KEY;
    @Value("${koreainvestment.api.app-secret}")
    private String APP_SECRET;
    @Value("${koreainvestment.api.base-url}")
    private String BASE_URL;

    public void fetchAndSendStockData(String symbol) {

        String token = getAccessToken();

        String url = BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-price?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=" + symbol;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("appKey", APP_KEY);
            headers.set("appSecret", APP_SECRET);
            headers.set("tr_id", "FHKST01010100");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode output = jsonNode.path("output");

                String stockName = getStockName(symbol);
                double currentPrice = output.path("stck_prpr").asDouble();

                StockData stockData = new StockData();
                stockData.setName(stockName);
                stockData.setSymbol(symbol);
                stockData.setPrice(currentPrice);
                stockData.setTimestamp(LocalDateTime.now());

                kafkaTemplate.send("stock-data-topic", stockData);
            } else {
                System.out.println("데이터 조회 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getAccessToken(){

        String cachedToken = (String) redisTemplate.opsForValue().get("koreainvestment:accessToken");

        if (cachedToken != null) {
            return cachedToken;
        }

        String url = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{"
                + "\"grant_type\":\"client_credentials\","
                + "\"appkey\":\"" + APP_KEY + "\","
                + "\"appsecret\":\"" + APP_SECRET + "\""
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getBody());
                String accessToken = jsonNode.path("access_token").asText();

                redisTemplate.opsForValue().set("koreainvestment:accessToken", accessToken, 24, TimeUnit.HOURS);
                return accessToken;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("error");
    }

    public String getStockName(String symbol) {
        String cachedName = (String) redisTemplate.opsForValue().get("koreainvestment:"+symbol);

        if (cachedName != null) {
            return cachedName;
        }

        String stockName = stockEntityRepository.findByStockCode(symbol)
                .map(StockEntity::getStockName)
                .orElse("");
        if(!stockName.equals("")){
            redisTemplate.opsForValue().set("koreainvestment:"+symbol, stockName, 1, TimeUnit.HOURS);
        }

        return stockName;
    }
}
