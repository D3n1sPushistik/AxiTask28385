package com.ignatkin.async_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import com.ignatkin.async_service.model.RequestStatusEntity;
import com.ignatkin.async_service.repository.RequestRepository;
import com.ignatkin.async_service.repository.RequestStatusRepository;
import com.ignatkin.async_service.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final RequestStatusRepository statusRepository;

    public RequestController(RequestService requestService,
                             RequestRepository requestRepository,
                             RequestStatusRepository statusRepository) {
        this.requestService = requestService;
        this.requestRepository = requestRepository;
        this.statusRepository = statusRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitRequest(@RequestBody String requestBody) throws JsonProcessingException {
        Long requestId = requestService.submitRequest(requestBody);
        return ResponseEntity.ok(requestId);
    }


    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(@PathVariable Long id) {
        RequestStatus status = requestService.getCurrentStatus(id);
        return ResponseEntity.ok(status != null ? status.name() : "UNKNOWN");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<RequestEntity> requestOpt = requestRepository.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RequestStatus enumStatus;
        try {
            enumStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.error("Неверный статус '{}': {}", status, ex.getMessage());
            return ResponseEntity.badRequest().body("Unknown status: " + status);
        }


        RequestStatusEntity newStatus = new RequestStatusEntity();
        newStatus.setRequest(requestOpt.get());
        newStatus.setStatus(enumStatus);
        statusRepository.save(newStatus);

        return ResponseEntity.ok("Status updated to: " + enumStatus.name());

    }
}
