package com.rickyyeung.profolio.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRespondDto {
    private UserDto userDto;
    private String accessToken;
    private String refreshToken;
}
