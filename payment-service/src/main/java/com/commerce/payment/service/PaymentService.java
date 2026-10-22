package com.commerce.payment.service;

import org.springframework.data.domain.Pageable;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentView;
import com.commerce.common.paging.PageResponse;

public interface PaymentService {

    /** Idempotent per order: repeating the same request returns the original payment. */
    PaymentView pay(PaymentRequest request);

    PaymentView get(Long id);

    PageResponse<PaymentView> list(Pageable pageable);
}
