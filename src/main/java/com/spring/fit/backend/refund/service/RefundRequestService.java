package com.spring.fit.backend.refund.service;

import com.spring.fit.backend.common.enums.RefundStatus;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.CreateRefundRequest;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.RefundRequestResponse;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.UpdateRefundStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundRequestService {

    RefundRequestResponse createRefundRequest(Long userId, CreateRefundRequest request);

    RefundRequestResponse getRefundRequestById(Long id);

    Page<RefundRequestResponse> getUserRefundRequests(Long userId, RefundStatus status, Pageable pageable);

    Page<RefundRequestResponse> getAllRefundRequests(RefundStatus status, Pageable pageable);

    RefundRequestResponse updateRefundStatus(Long refundRequestId, Long adminUserId, UpdateRefundStatusRequest request);
}



