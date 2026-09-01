package com.cms.common.mapper;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolsValidationSource {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @Email
    private String email;

    @Positive
    private int quantite;

}
