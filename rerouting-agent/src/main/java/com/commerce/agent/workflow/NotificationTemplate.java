package com.commerce.agent.workflow;

/** All customer-facing wording in one place. */
public enum NotificationTemplate {
    ORDER_CONFIRMED("Order %d is confirmed and shipping from %s."),
    ORDER_REROUTED("Order %d was delayed by %s, so it is now shipping from %s."),
    ORDER_DELAYED("Order %d is delayed (%s). We will ship it as soon as conditions allow."),
    ORDER_CANCELLED("Order %d was cancelled: %s.");

    private final String pattern;

    NotificationTemplate(String pattern) {
        this.pattern = pattern;
    }

    public String render(Object... args) {
        return pattern.formatted(args);
    }
}
