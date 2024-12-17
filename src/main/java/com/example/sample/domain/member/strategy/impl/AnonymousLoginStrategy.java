package com.example.sample.domain.member.strategy.impl;


import com.example.sample.domain.member.domain.LoginType;
import com.example.sample.domain.member.domain.Member;
import com.example.sample.domain.member.domain.Role;
import com.example.sample.domain.member.dto.response.MemberLoginResponse;
import com.example.sample.domain.member.mapper.MemberMapper;
import com.example.sample.domain.member.repository.MemberRepository;
import com.example.sample.domain.member.service.MemberService;
import com.example.sample.domain.member.strategy.LoginStrategy;
import com.example.sample.global.config.security.jwt.JwtProvider;
import com.example.sample.global.config.security.jwt.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AnonymousLoginStrategy implements LoginStrategy {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @Override
    public MemberLoginResponse login(String accessToken) {
        // Anonymous-specific logic
        Optional<Member> getMember = Optional.ofNullable(memberRepository.findByClientIdAndLoginType(accessToken, LoginType.ANONYMOUS));

        if (getMember.isEmpty()) {
            return saveNewMember(accessToken, LoginType.ANONYMOUS);
        }

        Member member = getMember.get();
        boolean isServiceMember = member.getName() != null;
        TokenInfo tokenInfo = generateToken(member);

        return memberMapper.toLoginMember(member, tokenInfo, isServiceMember, member.getRole());
    }

    private MemberLoginResponse saveNewMember(String clientId, LoginType loginType) {
        Member member = memberMapper.toMember(clientId, loginType);
        member.changeRole(Role.GUEST);
        Member newMember = memberService.saveEntity(member);
        TokenInfo tokenInfo = generateToken(newMember);
        return memberMapper.toLoginMember(newMember, tokenInfo, false, Role.GUEST);
    }

    private TokenInfo generateToken(Member member) {
        return jwtProvider.generateToken(member.getId().toString(), member.getRole().toString());
    }
}

