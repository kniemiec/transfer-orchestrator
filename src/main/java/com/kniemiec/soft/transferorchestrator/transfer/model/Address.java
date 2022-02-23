package com.kniemiec.soft.transferorchestrator.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    String street;
    String city;
    String postalCode;
    String country;
}
