package com.example.sample.domain.member.service;

import com.example.sample.domain.member.client.KakaoMemberClient;
import com.example.sample.domain.member.domain.Member;
import com.example.sample.domain.member.domain.LoginType;
import com.example.sample.domain.member.dto.request.MemberSignUpRequest;
import com.example.sample.domain.member.dto.response.MemberGenerateTokenResponse;
import com.example.sample.domain.member.dto.response.MemberIdResponse;
import com.example.sample.domain.member.dto.response.MemberLoginResponse;
import com.example.sample.domain.member.mapper.MemberMapper;
import com.example.sample.domain.member.repository.MemberRepository;
import com.example.sample.domain.member.strategy.context.LoginContext;
import com.example.sample.global.common.exception.RestApiException;
import com.example.sample.global.common.exception.code.status.AuthErrorStatus;
import com.example.sample.global.config.security.jwt.JwtProvider;
import com.example.sample.global.config.security.jwt.TokenInfo;
import com.example.sample.global.config.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {

    public final MemberService memberService;
    public final MemberRefreshTokenService refreshTokenService;

    public final JwtProvider jwtTokenProvider;
    private final LoginContext loginContext;

    // 소셜 로그인을 수행하는 함수
    @Override
    @Transactional
    public MemberLoginResponse socialLogin(String accessToken, LoginType loginType) {
        return loginContext.executeStrategy(accessToken, loginType);
    }

    // 회원가입을 수행하는 함수
    @Override
    @Transactional
    public MemberIdResponse signUp(Member member, MemberSignUpRequest request) {
        //이미 소셜 로그인 후, 인증 완료되면 멤버 엔티티는 생겨 있는 상태
        //그 후 추가 정보를 입력받아 저장하는 메서드
        Member loginMember = memberService.findById(member.getId());

        // 기본 정보 저장 로직 작성 필요
        loginMember.setName(request.getName());

        return new MemberIdResponse(memberService.saveEntity(loginMember).getId());
    }

    // 새로운 액세스 토큰 발급 함수
    @Override
    @Transactional
    public MemberGenerateTokenResponse generateNewAccessToken(String refreshToken, Member member) {

        Member loginMember = memberService.findById(member.getId());

        // 만료된 refreshToken인지 확인
        if (!jwtTokenProvider.validateToken(refreshToken))
            throw new RestApiException(AuthErrorStatus.EXPIRED_REFRESH_TOKEN);

        //편의상 refreshToken을 DB에 저장 후 비교하는 방식으로 감 (비추천)
        String savedRefreshToken = loginMember.getRefreshToken();

        // 디비에 저장된 refreshToken과 동일하지 않다면 유효하지 않음
        if (!refreshToken.equals(savedRefreshToken))
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);

        return new MemberGenerateTokenResponse(
                jwtTokenProvider.generateToken(
                        loginMember.getId().toString(), member.getRole().toString(), TokenType.ACCESS)
        );
    }

    // 로그아웃 함수
    @Override
    @Transactional
    public MemberIdResponse logout(Member member) {
        Member loginMember = memberService.findById(member.getId());

        refreshTokenService.deleteRefreshToken(loginMember);
        return new MemberIdResponse(loginMember.getId());
    }

    // 회원 탈퇴 함수
    @Override
    @Transactional
    public MemberIdResponse withdrawal(Member member) {
        // 멤버 soft delete
        Member loginMember = memberService.findById(member.getId());

        // refreshToken 삭제
        refreshTokenService.deleteRefreshToken(loginMember);

        // 멤버 soft delete
        loginMember.delete();

        return new MemberIdResponse(loginMember.getId());
    }
}
