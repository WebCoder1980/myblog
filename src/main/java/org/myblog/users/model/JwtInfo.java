package org.myblog.users.model;

import lombok.Data;

import java.util.Date;

@Data
public class JwtInfo {
    private JwtUserInfo userInfo;
    private Date expiration;
}
