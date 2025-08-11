package org.myblog.users.kafka.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserCreatedEvent {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private Set<RoleModel> roles = new HashSet<>();
    
    public UserCreatedEvent(UserModel source) {
        setId(source.getId());
        setUsername(source.getUsername());
        setEmail(source.getEmail());
        setPassword(source.getPassword());
        setRoles(source.getRoles());
    }
}
