package com.ignatkin.async_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import com.ignatkin.async_service.model.RequestStatusEntity;
import com.ignatkin.async_service.repository.RequestRepository;
import com.ignatkin.async_service.repository.RequestStatusRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestStatusRepository statusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String normalizeJson(String json) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(json);
        JsonNode normalized = sortJsonNode(node);
        return objectMapper.writeValueAsString(normalized);
    }

    private JsonNode sortJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();

            // Получаем ключи, сортируем, и по очереди вставляем в новый ObjectNode
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);

            for (String field : fieldNames) {
                sorted.set(field, sortJsonNode(node.get(field)));
            }

            return sorted;
        }

        if (node.isArray()) {
            List<JsonNode> elements = new ArrayList<>();
            node.elements().forEachRemaining(e -> elements.add(sortJsonNode(e)));

            // Сортируем по строковому представлению
            elements.sort(Comparator.comparing(JsonNode::toString));
            return objectMapper.getNodeFactory().arrayNode().addAll(elements);
        }

        return node;
    }

    public Long submitRequest(String jsonPayload) throws JsonProcessingException {
        String normalizedJson = normalizeJson(jsonPayload);
        String hash = DigestUtils.sha256Hex(normalizedJson);

        List<RequestEntity> activeDuplicates = requestRepository
                .findByPayloadHashAndNotInTerminalState(hash);

        if (!activeDuplicates.isEmpty()) {
            RequestEntity existing = activeDuplicates.get(0);
            existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            requestRepository.save(existing);
            return existing.getId();
        }

        RequestEntity entity = new RequestEntity();
        entity.setRequestJson(normalizedJson);
        entity.setPayloadHash(hash);

        RequestEntity saved = requestRepository.save(entity);

        RequestStatusEntity status = new RequestStatusEntity();
        status.setRequest(saved);
        status.setStatus(RequestStatus.RECEIVED);
        statusRepository.save(status);

        return saved.getId();
    }


    public RequestStatus getCurrentStatus(Long requestId) {
        return statusRepository.findByRequestIdOrderByCreatedAtDesc(requestId)
                .stream()
                .findFirst()
                .map(RequestStatusEntity::getStatus)
                .orElse(null);
    }

    @Async
    public void processRequestAsync(RequestEntity entity) {


        try {
            addStatus(entity, RequestStatus.VALIDATING);
            Thread.sleep(4000);

            addStatus(entity, RequestStatus.PROCESSING);
            Thread.sleep(4000);

            addStatus(entity, RequestStatus.FINALIZING);
            Thread.sleep(4000);

            addStatus(entity, RequestStatus.DONE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            addStatus(entity, RequestStatus.ERROR);
        }
    }

    private void addStatus(RequestEntity request, RequestStatus status) {
        RequestStatusEntity entity = new RequestStatusEntity();
        entity.setRequest(request);
        entity.setStatus(status);
        statusRepository.save(entity);
    }
}