package com.spring.fit.backend.order.service.carrier;

import com.spring.fit.backend.common.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CarrierServiceFactory {

    private final List<CarrierService> carrierServices;

    public CarrierService getService(String carrier) {
        if (carrier == null || carrier.isBlank()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Carrier is required for tracking");
        }

        if (CollectionUtils.isEmpty(carrierServices)) {
            throw new ErrorException(HttpStatus.SERVICE_UNAVAILABLE, "No carrier integrations are configured");
        }

        return carrierServices.stream()
                .filter(service -> service.supports(carrier))
                .findFirst()
                .orElseThrow(() -> new ErrorException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported carrier: " + carrier));
    }
}
