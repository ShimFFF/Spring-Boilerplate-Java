package com.example.goormthon_univ_3th.domain.member.service;

import com.example.goormthon_univ_3th.domain.member.client.KakaoMemberClient;
import com.example.goormthon_univ_3th.domain.member.domain.Member;
import com.example.goormthon_univ_3th.domain.member.domain.SocialType;
import com.example.goormthon_univ_3th.domain.member.dto.request.MemberSignUpRequest;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberGenerateTokenResponse;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberIdResponse;
import com.example.goormthon_univ_3th.domain.member.dto.response.MemberLoginResponse;
import com.example.goormthon_univ_3th.domain.member.mapper.MemberMapper;
import com.example.goormthon_univ_3th.domain.member.repository.MemberRepository;
import com.example.goormthon_univ_3th.global.common.exception.RestApiException;
import com.example.goormthon_univ_3th.global.common.exception.code.status.AuthErrorStatus;
import com.example.goormthon_univ_3th.global.config.security.jwt.JwtProvider;
import com.example.goormthon_univ_3th.global.config.security.jwt.TokenInfo;
import com.example.goormthon_univ_3th.global.config.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthorService {

    public final MemberRepository memberRepository;

    public final MemberService memberService;
    public final MemberRefreshTokenService refreshTokenService;

    public final KakaoMemberClient kakaoMemberClient;

    public final JwtProvider jwtTokenProvider;

    public final MemberMapper memberMapper;

    // 소셜 로그인을 수행하는 함수
    @Override
    public MemberLoginResponse socialLogin(String accessToken, SocialType socialType){
        // 로그인 구분
        if(socialType.equals(SocialType.KAKAO))
            return loginByKakao(accessToken);

        return null;
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

    private MemberLoginResponse loginByKakao(final String accessToken){
        // kakao 서버와 통신해서 유저 고유값(clientId) 받기
        String clientId = kakaoMemberClient.getkakaoClientID(accessToken);
        // 존재 여부 파악
        Optional<Member> getMember = memberRepository.findByClientIdAndSocialType(clientId, SocialType.KAKAO);

        // 1. 없으면 : Member 객체 생성하고 DB 저장
        if(getMember.isEmpty()) {
            return saveNewMember(clientId, SocialType.KAKAO);
        }
        // 2. 있으면 : 새로운 토큰 반환
        boolean isServiceMember = getMember.get().getName() != null;
        return getNewToken(getMember.get(), isServiceMember);
    }

    private MemberLoginResponse saveNewMember(String clientId, SocialType socialType) {
        Member member = memberMapper.toMember(clientId, socialType);
        Member newMember =  memberRepository.save(member);

        return getNewToken(newMember, false);
    }

    private MemberLoginResponse getNewToken(Member member, boolean isServiceMember) {
        // jwt 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(member.getId().toString(), member.getRole().toString());
        // refreshToken 디비에 저장
        refreshTokenService.saveRefreshToken(tokenInfo.refreshToken(), member);

        return memberMapper.toLoginMember(member, tokenInfo, isServiceMember);
    }

    // refresh 토큰 저장 함수
    @Transactional
    public void saveRefreshToken(String refreshToken, Member member) {
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);
    }
}
