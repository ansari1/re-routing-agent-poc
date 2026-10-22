package com.commerce.agent.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.commerce.agent.client.CrmGateway;
import com.commerce.common.contract.CrmContracts.NotificationRequest;
import com.commerce.common.error.DomainException;

/** Customer notifications are best-effort and must never fail a checkout. */
@Component
public class CustomerNotifier {

    private static final Logger log = LoggerFactory.getLogger(CustomerNotifier.class);

    private final CrmGateway crm;

    public CustomerNotifier(CrmGateway crm) {
        this.crm = crm;
    }

    public void notify(Long customerId, Long orderId, String message) {
        try {
            crm.notifyCustomer(new NotificationRequest(customerId, orderId, message));
        } catch (DomainException e) {
            log.warn("Could not notify customer {} about order {}: {}", customerId, orderId, e.getMessage());
        }
    }
}
