package com.example.goormthon_univ_3th.domain.member.service;

import com.example.goormthon_univ_3th.domain.member.domain.Member;
import com.example.goormthon_univ_3th.domain.member.domain.SocialType;
import com.example.goormthon_univ_3th.domain.member.dto.request.MemberSignUpRequest;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberGenerateTokenResponse;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberIdResponse;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberLoginResponse;

public interface MemberAuthorService {
    // 소셜 로그인
    MemberLoginResponse socialLogin(final String accessToken, SocialType socialType);
    // 회원가입
    MemberIdResponse signUp(Member member, MemberSignUpRequest request);
    // 새로운 액세스 토큰 발급
    MemberGenerateTokenResponse generateNewAccessToken(String refreshToken, Member member);
    // 로그아웃
    MemberIdResponse logout(Member member);
    // 회원 탈퇴
    MemberIdResponse withdrawal(Member member);

    Member loadEntity(Long id);
}
