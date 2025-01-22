package org.stock.richman.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.stock.richman.model.AssetData;

@Service
public class SlackService {

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(AssetData assetData, String alertType){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String message = null;
            long price = (long) assetData.getPrice();

            if(alertType.equals("High")) {
                message = String.format("가격 알림! : %s 종목이 지정하신 가격 %,d 를 초과했습니다. (현재가격 : %,d)", assetData.getName(), assetData.getHighValue(),price);
            }else{
                message = String.format("가격 알림! : %s 종목이 지정하신 가격 %,d 보다 떨어졌습니다 (현재가격 : %,d).", assetData.getName(), assetData.getLowValue(),price);
            }

            String payload = String.format("{\"text\": \"%s\"}", message);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
            } else {
                System.out.println(response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
