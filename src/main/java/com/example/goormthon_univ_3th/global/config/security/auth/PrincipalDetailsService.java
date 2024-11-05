package com.example.goormthon_univ_3th.global.config.security.auth;

import com.example.goormthon_univ_3th.domain.member.domain.Member;
import com.example.goormthon_univ_3th.domain.member.service.DetailMemberService;
import com.example.goormthon_univ_3th.domain.member.status.MemberErrorStatus;
import com.example.goormthon_univ_3th.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final DetailMemberService detailMemberService;

    @Override
    public PrincipalDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(username); // Long으로 변환
            Member memberEntity = detailMemberService.findById(userId) // 서비스에서 회원 조회
                    .orElseThrow(() -> new RestApiException(MemberErrorStatus.EMPTY_MEMBER));
            return new PrincipalDetails(memberEntity); // PrincipalDetails 반환
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid ID format", e); // 잘못된 ID 형식 예외 처리
        }
    }
}
