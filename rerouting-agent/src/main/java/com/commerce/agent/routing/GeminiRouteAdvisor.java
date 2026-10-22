package com.commerce.agent.routing;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commerce.common.contract.InventoryContracts.WarehouseView;

/** Lets Gemini pick among validated candidates; any bad or failed answer falls back to the rule-based advisor. */
public class GeminiRouteAdvisor implements RouteAdvisor {

    private static final Logger log = LoggerFactory.getLogger(GeminiRouteAdvisor.class);
    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private final RoutingAssistant assistant;
    private final RouteAdvisor fallback;

    public GeminiRouteAdvisor(RoutingAssistant assistant, RouteAdvisor fallback) {
        this.assistant = assistant;
        this.fallback = fallback;
    }

    @Override
    public WarehouseView choose(RouteContext context) {
        try {
            var answer = assistant.chooseWarehouse(context.blocked().region(), context.reason(),
                    context.order().destination(), describe(context.candidates()));
            return parse(answer, context.candidates()).orElseGet(() -> {
                log.warn("Gemini answer '{}' matched no candidate; using {}", answer, fallback.name());
                return fallback.choose(context);
            });
        } catch (RuntimeException e) {
            log.warn("Gemini routing failed ({}); using {}", e.getMessage(), fallback.name());
            return fallback.choose(context);
        }
    }

    @Override
    public String name() {
        return "gemini";
    }

    static String describe(List<WarehouseView> candidates) {
        return String.join("\n", candidates.stream()
                .map(w -> "id=%d code=%s region=%s priority=%d".formatted(w.id(), w.code(), w.region(), w.priority()))
                .toList());
    }

    static Optional<WarehouseView> parse(String answer, List<WarehouseView> candidates) {
        if (answer == null) {
            return Optional.empty();
        }
        return NUMBER.matcher(answer).results()
                .map(m -> Long.valueOf(m.group()))
                .flatMap(id -> candidates.stream().filter(w -> w.id().equals(id)))
                .findFirst();
    }
}
