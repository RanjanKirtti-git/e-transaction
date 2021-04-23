package com.kirtti.banking.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
@Data
public class Customers {
    @Id
    private String userId;
//    @NotBlank
    private String password;
//    @NotBlank
//    @Pattern(regexp = "[A-Z a-z]")
    private String firstName;
//    @NotBlank
//    @Pattern(regexp = "[A-Z a-z]")
    private String lastName;
//    @NotBlank
    private String email;
//    @NotBlank
//    @Pattern(regexp = "[0-9]{10}")
    private Long phNo;
//    @NotBlank
    private String adrs;

    private String gender;
    private String cimage;
    private Double cash;
}
