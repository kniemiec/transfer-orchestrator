package com.kniemiec.soft.transferorchestrator.compliance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    String country;
    String postalCode;
    String city;
    String street;
}
