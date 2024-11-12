package com.example.sample.domain.member.repository;

import com.example.sample.domain.member.domain.Member;
import com.example.sample.domain.member.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByClientIdAndSocialType(String clientId, SocialType socialType);
}

