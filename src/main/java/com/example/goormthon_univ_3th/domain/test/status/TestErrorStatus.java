package com.example.goormthon_univ_3th.domain.test.status;

import com.example.goormthon_univ_3th.global.common.exception.code.BaseCodeDto;
import com.example.goormthon_univ_3th.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TestErrorStatus implements BaseCodeInterface {

    TEST_ERROR_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "TEST400", "테스트용 에러");

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCodeDto getCode() {
        return BaseCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
