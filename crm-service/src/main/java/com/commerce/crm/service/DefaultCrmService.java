package com.commerce.crm.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.CrmContracts.ActivityView;
import com.commerce.common.contract.CrmContracts.CustomerRequest;
import com.commerce.common.contract.CrmContracts.CustomerView;
import com.commerce.common.contract.CrmContracts.NotificationRequest;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.paging.PageResponse;
import com.commerce.crm.domain.Customer;
import com.commerce.crm.domain.CustomerActivity;
import com.commerce.crm.domain.CustomerActivityRepository;
import com.commerce.crm.domain.CustomerRepository;

@Service
@Transactional
public class DefaultCrmService implements CrmService {

    private final CustomerRepository customers;
    private final CustomerActivityRepository activities;

    public DefaultCrmService(CustomerRepository customers, CustomerActivityRepository activities) {
        this.customers = customers;
        this.activities = activities;
    }

    @Override
    public CustomerView createCustomer(CustomerRequest request) {
        if (customers.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("A customer with email %s already exists".formatted(request.email()));
        }
        return toView(customers.save(new Customer(request.name(), request.email())));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "customers", key = "#id")
    public CustomerView getCustomer(Long id) {
        return customers.findById(id).map(DefaultCrmService::toView)
                .orElseThrow(() -> new NotFoundException("Customer", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerView> listCustomers(Pageable pageable) {
        return PageResponse.of(customers.findAll(pageable), DefaultCrmService::toView);
    }

    @Override
    public ActivityView notifyCustomer(NotificationRequest request) {
        if (!customers.existsById(request.customerId())) {
            throw new NotFoundException("Customer", request.customerId());
        }
        var saved = activities.save(new CustomerActivity(request.customerId(), request.orderId(), request.message()));
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ActivityView> listActivities(Long customerId, Pageable pageable) {
        if (!customers.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        return PageResponse.of(activities.findByCustomerId(customerId, pageable), DefaultCrmService::toView);
    }

    private static CustomerView toView(Customer c) {
        return new CustomerView(c.getId(), c.getName(), c.getEmail());
    }

    private static ActivityView toView(CustomerActivity a) {
        return new ActivityView(a.getId(), a.getCustomerId(), a.getOrderId(), a.getMessage(), a.getCreatedAt());
    }
}
