package com.fleetscore.regatta.api.dto.validation;

import com.fleetscore.regatta.api.dto.CreateRegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SailingClubIdentifiedValidator
    implements ConstraintValidator<SailingClubIdentified, CreateRegistrationRequest> {

  @Override
  public boolean isValid(CreateRegistrationRequest request, ConstraintValidatorContext ctx) {
    if (request == null) {
      return true;
    }
    return request.sailingClubId() != null
        || (request.sailingClubName() != null && !request.sailingClubName().isBlank());
  }
}
