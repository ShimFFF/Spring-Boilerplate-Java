package com.example.sample.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberGenerateTokenResponse {
    private String accessToken;
}
