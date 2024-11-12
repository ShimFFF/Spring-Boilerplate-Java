package com.example.sample.domain.member.client;

import com.example.sample.domain.member.dto.client.KakaoResponse;
import com.example.sample.global.common.exception.RestApiException;
import com.example.sample.global.common.exception.code.status.AuthErrorStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KakaoMemberClient {

    private final WebClient webClient;


    public KakaoMemberClient(WebClient.Builder webClientBuilder ) {
        this.webClient = webClientBuilder
                .baseUrl("https://kapi.kakao.com/v2/user/me")
                .build();
    }

    public String getkakaoClientID(final String accessToken) {
        KakaoResponse response = webClient.get()
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoResponse.class)
                .block();

        if(response == null)
            throw new RestApiException(AuthErrorStatus.FAILED_SOCIAL_LOGIN);

        return response.getId();
    }
}
