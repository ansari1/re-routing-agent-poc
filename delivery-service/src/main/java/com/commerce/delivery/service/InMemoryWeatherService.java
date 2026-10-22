package com.commerce.delivery.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.commerce.common.contract.DeliveryContracts.WeatherStatus;
import com.commerce.delivery.config.WeatherProperties;

/** Holds the weather picture in memory; swap for a real weather feed without touching callers. */
@Service
public class InMemoryWeatherService implements WeatherService {

    private final AtomicReference<WeatherStatus> status;

    public InMemoryWeatherService(WeatherProperties properties) {
        this.status = new AtomicReference<>(new WeatherStatus(properties.severe(), properties.affectedRegions()));
    }

    @Override
    public WeatherStatus current() {
        return status.get();
    }

    @Override
    public WeatherStatus update(WeatherStatus newStatus) {
        status.set(newStatus);
        return newStatus;
    }
}
