package org.myblog.users.service;

import jakarta.annotation.PostConstruct;
import org.myblog.users.appenum.RoleEnum;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;
import org.myblog.users.repository.RoleRepository;
import org.myblog.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

@Service
public class DataInitializer {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            UserModel admin = new UserModel();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("adminPassword"));
            admin.setEmail("admin@myblog.org");
            admin.setRoles(new HashSet<RoleModel>(Arrays.asList(roleRepository.findByName(RoleEnum.ROLE_ADMIN).orElseThrow())));
            userRepository.save(admin);

            UserModel moder = new UserModel();
            moder.setUsername("moderator");
            moder.setPassword(encoder.encode("moderatorPassword"));
            moder.setEmail("moderator@myblog.org");
            moder.setRoles(new HashSet<RoleModel>(Arrays.asList(roleRepository.findByName(RoleEnum.ROLE_MODERATOR).orElseThrow())));

            userRepository.save(moder);

            UserModel maxsmg = new UserModel();
            maxsmg.setUsername("maxsmg");
            maxsmg.setPassword(encoder.encode("qweqwe"));
            maxsmg.setEmail("maxsmg@myblog.org");
            maxsmg.setRoles(new HashSet<RoleModel>(Arrays.asList(roleRepository.findByName(RoleEnum.ROLE_USER).orElseThrow())));

            userRepository.save(maxsmg);


        }

        if (!roleRepository.existsByName(RoleEnum.ROLE_USER)) {
            RoleModel userRole = new RoleModel(RoleEnum.ROLE_USER);
            roleRepository.save(userRole);

            RoleModel moderatorRole = new RoleModel(RoleEnum.ROLE_MODERATOR);
            roleRepository.save(moderatorRole);

            RoleModel adminRole = new RoleModel(RoleEnum.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }
}
