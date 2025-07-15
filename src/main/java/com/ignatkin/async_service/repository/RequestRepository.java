package com.ignatkin.async_service.repository;

import com.ignatkin.async_service.model.RequestEntity;
import com.ignatkin.async_service.model.RequestStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestEntity, Long> {

    @Query("""
                SELECT DISTINCT r FROM RequestEntity r
                JOIN RequestStatusEntity rs ON rs.request = r
                WHERE r.payloadHash = :payloadHash
                  AND rs.status NOT IN ('DONE', 'ERROR')
            """)
    List<RequestEntity> findByPayloadHashAndNotInTerminalState(@Param("payloadHash") String payloadHash);


    @Query("""
                SELECT rs.request FROM RequestStatusEntity rs
                JOIN (
                    SELECT rs2.request.id AS requestId, MAX(rs2.createdAt) AS maxCreated
                    FROM RequestStatusEntity rs2
                    GROUP BY rs2.request.id
                ) latest ON rs.request.id = latest.requestId AND rs.createdAt = latest.maxCreated
                WHERE rs.status = :status
            """)
    List<RequestEntity> findByCurrentStatus(@Param("status") RequestStatus status);

}
