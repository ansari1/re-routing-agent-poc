package com.commerce.payment.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentView;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.paging.PageResponse;
import com.commerce.payment.domain.Payment;
import com.commerce.payment.domain.PaymentRepository;

@Service
@Transactional
public class DefaultPaymentService implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentProcessor processor;

    public DefaultPaymentService(PaymentRepository repository, PaymentProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    @Override
    public PaymentView pay(PaymentRequest request) {
        return repository.findByOrderId(request.orderId())
                .map(existing -> replay(existing, request))
                .orElseGet(() -> toView(repository.save(new Payment(request.orderId(), request.amount(),
                        request.method(), processor.authorize(request)))));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "payments", key = "#id")
    public PaymentView get(Long id) {
        return repository.findById(id).map(DefaultPaymentService::toView)
                .orElseThrow(() -> new NotFoundException("Payment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentView> list(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable), DefaultPaymentService::toView);
    }

    private static PaymentView replay(Payment existing, PaymentRequest request) {
        if (!existing.matches(request.amount(), request.method())) {
            throw new BusinessRuleException("Order %d already has a different payment".formatted(request.orderId()));
        }
        return toView(existing);
    }

    private static PaymentView toView(Payment p) {
        return new PaymentView(p.getId(), p.getOrderId(), p.getAmount(), p.getMethod(), p.getStatus(),
                p.getCreatedAt());
    }
}
