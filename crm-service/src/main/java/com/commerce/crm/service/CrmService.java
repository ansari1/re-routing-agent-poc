package com.commerce.crm.service;

import org.springframework.data.domain.Pageable;

import com.commerce.common.contract.CrmContracts.ActivityView;
import com.commerce.common.contract.CrmContracts.CustomerRequest;
import com.commerce.common.contract.CrmContracts.CustomerView;
import com.commerce.common.contract.CrmContracts.NotificationRequest;
import com.commerce.common.paging.PageResponse;

public interface CrmService {

    CustomerView createCustomer(CustomerRequest request);

    CustomerView getCustomer(Long id);

    PageResponse<CustomerView> listCustomers(Pageable pageable);

    ActivityView notifyCustomer(NotificationRequest request);

    PageResponse<ActivityView> listActivities(Long customerId, Pageable pageable);
}
