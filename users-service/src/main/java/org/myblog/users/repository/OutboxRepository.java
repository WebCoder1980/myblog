package org.myblog.users.repository;

import org.myblog.users.model.OutboxModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxModel, Integer> {
    List<OutboxModel> findByTopic(String topic);
}
