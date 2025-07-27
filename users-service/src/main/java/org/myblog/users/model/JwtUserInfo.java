package org.myblog.users.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.myblog.users.service.security.UserDetailsImpl;

@NoArgsConstructor
@Data
public class JwtUserInfo {
    private Integer id;

    public JwtUserInfo(UserDetailsImpl userDetailsImpl) {
        setId(userDetailsImpl.getId());
    }
}
