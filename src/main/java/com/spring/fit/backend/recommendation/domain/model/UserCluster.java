package com.spring.fit.backend.recommendation.domain.model;

import com.spring.fit.backend.recommendation.domain.enums.BMIRange;
import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCluster {
    private Gender gender;
    private BMIRange bmiRange;

    public static UserCluster fromMeasurements(UserMeasurements measurements) {
        return new UserCluster(
            measurements.getGender(),
            BMIRange.fromBMI(measurements.getBmi())
        );
    }

    public BigDecimal getBmiMin() {
        return bmiRange.getMin();
    }

    public BigDecimal getBmiMax() {
        return bmiRange.getMax();
    }
}
