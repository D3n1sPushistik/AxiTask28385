package com.ignatkin.async_service.scheduler;

import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import com.ignatkin.async_service.repository.RequestRepository;
import com.ignatkin.async_service.service.RequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestProcessingScheduler {

    private final RequestRepository requestRepository;
    private final RequestService requestService;

    public RequestProcessingScheduler(RequestRepository requestRepository,
                                      RequestService requestService) {
        this.requestRepository = requestRepository;
        this.requestService = requestService;
    }

    @Scheduled(fixedDelay = 5000)
    public void processNewRequests() {
        List<RequestEntity> newRequests = requestRepository.findByCurrentStatus(RequestStatus.RECEIVED);
        for (RequestEntity request : newRequests) {
            requestService.processRequestAsync(request);
        }
    }
}