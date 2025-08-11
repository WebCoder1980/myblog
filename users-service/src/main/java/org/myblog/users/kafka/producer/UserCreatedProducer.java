package org.myblog.users.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.myblog.users.kafka.event.UserCreatedEvent;
import org.myblog.users.model.OutboxModel;
import org.myblog.users.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class UserCreatedProducer {
    public static final String TOPIC_NAME = "user.created";

    @Autowired
    private Logger logger;

    @Autowired
    private KafkaTemplate<Integer, UserCreatedEvent> kafkaTemplate;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void watch() {
        List<OutboxModel> messages = outboxRepository.findByTopic(TOPIC_NAME);

        for (OutboxModel message : messages) {
            Integer key = message.getKey();
            UserCreatedEvent value = null;
            try {
                value = objectMapper.readValue(message.getValue(), UserCreatedEvent.class);
            } catch (JsonProcessingException ex) {
                logger.error(String.format("Unknown deserialization error: %s", ex.getMessage()));
                return;
            }

            try {
                kafkaTemplate.send(TOPIC_NAME, key, value).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            outboxRepository.delete(message);
        }
    }
}
