package com.rickyyeung.profolio.Dto;

import com.rickyyeung.profolio.model.User;
import lombok.Data;

@Data
public class UserDto {

    private Long userId;
    private String email;
    private String googleId;
    private String displayName;
    private String avatarUrl;

}
