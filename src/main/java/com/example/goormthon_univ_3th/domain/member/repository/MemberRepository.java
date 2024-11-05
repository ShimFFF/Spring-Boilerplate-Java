package com.example.goormthon_univ_3th.domain.member.repository;

import com.example.goormthon_univ_3th.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
}
