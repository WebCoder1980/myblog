package org.myblog.users.dto.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.myblog.users.model.RoleModel;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserPutRequest {
    @Size(min = 3, max = 20)
    private String username;

    @Size(min = 3, max = 50)
    @Email
    private String email;

    @Size(max = 120)
    private String password;

    private Set<String> roles = new HashSet<>();
}
