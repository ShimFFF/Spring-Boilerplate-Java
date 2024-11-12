package com.example.sample.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("관리자", 0),
    MEMBER("일반 멤버", 1);

    private final String toKorean;
    private final int priority;
}
