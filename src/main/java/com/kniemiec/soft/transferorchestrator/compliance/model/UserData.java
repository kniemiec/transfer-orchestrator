package com.kniemiec.soft.transferorchestrator.compliance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    String userId;
    String surname;
    String lastname;
    Address address;
}