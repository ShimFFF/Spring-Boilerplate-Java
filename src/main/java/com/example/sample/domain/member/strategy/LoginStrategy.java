package com.example.sample.domain.member.strategy;

import com.example.sample.domain.member.dto.response.MemberLoginResponse;

public interface LoginStrategy {
    MemberLoginResponse login(String accessToken);
}
