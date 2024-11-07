package com.example.goormthon_univ_3th.domain.member.mapper;

import com.example.goormthon_univ_3th.domain.member.domain.Member;
import com.example.goormthon_univ_3th.domain.member.domain.Role;
import com.example.goormthon_univ_3th.domain.member.domain.SocialType;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberLoginResponse;
import com.example.goormthon_univ_3th.global.config.security.jwt.TokenInfo;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public Member toMember(final String clientId, SocialType socialType){
        return Member.builder()
                .clientId(clientId)
                .socialType(socialType)
                .build();
    }

    public MemberLoginResponse toLoginMember(final Member member, TokenInfo tokenInfo, boolean isServiceMember) {
        return MemberLoginResponse.builder()
                .memberId(member.getId())
                .accessToken(tokenInfo.accessToken())
                .refreshToken(tokenInfo.refreshToken())
                .isServiceMember(isServiceMember)
                .build();
    }
}

