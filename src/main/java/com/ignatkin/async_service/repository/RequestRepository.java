package com.ignatkin.async_service.repository;

import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestEntity, Long> {

    @Query("""
        SELECT r FROM RequestEntity r 
        WHERE r.payloadHash = :payloadHash AND r.id IN (
            SELECT rs.request.id FROM RequestStatusEntity rs WHERE rs.status NOT IN ('DONE', 'ERROR')
        )
    """)
    List<RequestEntity> findByPayloadHashAndNotInTerminalState(String payloadHash);

    @Query("""
        SELECT r FROM RequestEntity r
        WHERE EXISTS (
            SELECT rs FROM RequestStatusEntity rs
            WHERE rs.request = r AND rs.status = :status
              AND rs.createdAt = (
                  SELECT MAX(rs2.createdAt) FROM RequestStatusEntity rs2 WHERE rs2.request = r
              )
        )
    """)
    List<RequestEntity> findByCurrentStatus(@Param("status") RequestStatus status);
}
