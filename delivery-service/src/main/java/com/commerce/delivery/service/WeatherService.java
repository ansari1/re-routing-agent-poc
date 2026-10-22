package com.commerce.delivery.service;

import com.commerce.common.contract.DeliveryContracts.WeatherStatus;

public interface WeatherService {

    WeatherStatus current();

    WeatherStatus update(WeatherStatus status);
}
