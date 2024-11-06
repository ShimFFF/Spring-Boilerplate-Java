package com.example.goormthon_univ_3th.domain.member.domain;

import com.example.goormthon_univ_3th.global.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String clientId;


    @Builder
    public Member(String name, String email, SocialType socialType, String clientId) {
        this.name = name;
        this.email = email;
        this.role = Role.MEMBER;
        this.socialType = socialType;
        this.clientId = clientId;
    }


}