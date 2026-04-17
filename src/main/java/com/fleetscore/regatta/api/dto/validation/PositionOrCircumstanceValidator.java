package com.fleetscore.regatta.api.dto.validation;

import com.fleetscore.regatta.api.dto.RaceResultRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PositionOrCircumstanceValidator
    implements ConstraintValidator<PositionOrCircumstance, RaceResultRequest> {

  @Override
  public boolean isValid(RaceResultRequest request, ConstraintValidatorContext ctx) {
    if (request == null) {
      return true;
    }
    boolean positionPresent = request.position() != null;
    boolean circumstancePresent = request.circumstance() != null;
    return positionPresent ^ circumstancePresent;
  }
}
