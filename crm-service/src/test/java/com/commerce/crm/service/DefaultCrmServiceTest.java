package com.commerce.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.common.contract.CrmContracts.CustomerRequest;
import com.commerce.common.contract.CrmContracts.NotificationRequest;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.crm.domain.Customer;
import com.commerce.crm.domain.CustomerActivity;
import com.commerce.crm.domain.CustomerActivityRepository;
import com.commerce.crm.domain.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class DefaultCrmServiceTest {

    @Mock
    CustomerRepository customers;
    @Mock
    CustomerActivityRepository activities;

    DefaultCrmService service;

    @BeforeEach
    void setUp() {
        service = new DefaultCrmService(customers, activities);
    }

    @Test
    void createsCustomer() {
        when(customers.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(customers.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.createCustomer(new CustomerRequest("Ann", "a@b.com"));

        assertThat(view.email()).isEqualTo("a@b.com");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(customers.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createCustomer(new CustomerRequest("Ann", "a@b.com")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void notifyRecordsActivityForExistingCustomer() {
        when(customers.existsById(1L)).thenReturn(true);
        when(activities.save(any(CustomerActivity.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.notifyCustomer(new NotificationRequest(1L, 7L, "Order rerouted"));

        assertThat(view.message()).isEqualTo("Order rerouted");
        assertThat(view.orderId()).isEqualTo(7L);
    }

    @Test
    void notifyUnknownCustomerThrowsNotFound() {
        when(customers.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.notifyCustomer(new NotificationRequest(1L, null, "x")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUnknownCustomerThrowsNotFound() {
        when(customers.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCustomer(2L)).isInstanceOf(NotFoundException.class);
    }
}
