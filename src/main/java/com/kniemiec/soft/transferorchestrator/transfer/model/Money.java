package com.kniemiec.soft.transferorchestrator.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Money {

    @NotBlank(message = "Currency can not be empty")
    String currency;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer=3, fraction=2)
    BigDecimal value;
}
