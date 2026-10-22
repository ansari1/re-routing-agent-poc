package com.commerce.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.common.contract.PaymentContracts.PaymentMethod;
import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentStatus;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.payment.config.PaymentProperties;
import com.commerce.payment.domain.Payment;
import com.commerce.payment.domain.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class DefaultPaymentServiceTest {

    @Mock
    PaymentRepository repository;

    DefaultPaymentService service;

    @BeforeEach
    void setUp() {
        var processor = new SimulatedPaymentProcessor(new PaymentProperties(new BigDecimal("100")));
        service = new DefaultPaymentService(repository, processor);
    }

    @Test
    void authorisesPaymentWithinLimit() {
        when(repository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.pay(new PaymentRequest(1L, new BigDecimal("50.00"), PaymentMethod.CARD));

        assertThat(view.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    void declinesPaymentAboveLimit() {
        when(repository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.pay(new PaymentRequest(1L, new BigDecimal("500.00"), PaymentMethod.CARD));

        assertThat(view.status()).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    void repeatedRequestReturnsExistingPaymentWithoutSaving() {
        var existing = new Payment(1L, new BigDecimal("50.00"), PaymentMethod.CARD, PaymentStatus.AUTHORIZED);
        when(repository.findByOrderId(1L)).thenReturn(Optional.of(existing));

        var view = service.pay(new PaymentRequest(1L, new BigDecimal("50"), PaymentMethod.CARD));

        assertThat(view.status()).isEqualTo(PaymentStatus.AUTHORIZED);
        verify(repository, never()).save(any());
    }

    @Test
    void conflictingRetryIsRejected() {
        var existing = new Payment(1L, new BigDecimal("50.00"), PaymentMethod.CARD, PaymentStatus.AUTHORIZED);
        when(repository.findByOrderId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.pay(new PaymentRequest(1L, new BigDecimal("60.00"), PaymentMethod.CARD)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getUnknownPaymentThrowsNotFound() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(3L)).isInstanceOf(NotFoundException.class);
    }
}
