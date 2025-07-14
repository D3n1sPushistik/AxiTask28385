package com.ignatkin.async_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatusEntity;
import com.ignatkin.async_service.repository.RequestRepository;
import com.ignatkin.async_service.repository.RequestStatusRepository;
import com.ignatkin.async_service.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/requests")
public class RequestController {

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
    public ResponseEntity<?> submitRequest(@RequestBody String requestBody) {
        try {
            Long requestId = requestService.submitRequest(requestBody);
            return ResponseEntity.ok(requestId);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(@PathVariable Long id) {
        String status = requestService.getCurrentStatus(id);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<RequestEntity> requestOpt = requestRepository.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RequestStatusEntity newStatus = new RequestStatusEntity();
        newStatus.setRequest(requestOpt.get());
        newStatus.setStatus(status.toUpperCase());
        statusRepository.save(newStatus);

        return ResponseEntity.ok("Status updated to: " + status.toUpperCase());

    }
}
