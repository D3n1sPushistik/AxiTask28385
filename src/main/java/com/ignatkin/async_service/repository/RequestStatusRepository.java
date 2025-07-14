package com.ignatkin.async_service.repository;

import com.ignatkin.async_service.model.RequestStatusEntity;
import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestStatusRepository extends JpaRepository<RequestStatusEntity, Long> {

    List<RequestStatusEntity> findTop1ByRequestOrderByCreatedAtDesc(RequestEntity request);

    List<RequestStatusEntity> findByRequestIdOrderByCreatedAtDesc(Long requestId);

    List<RequestStatusEntity> findByStatus(RequestStatus status);
}
