package org.myblog.users.repository;

import org.myblog.users.appenum.RoleEnum;
import org.myblog.users.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    Optional<RoleModel> findByName(RoleEnum name);

    boolean existsByName(RoleEnum name);
}