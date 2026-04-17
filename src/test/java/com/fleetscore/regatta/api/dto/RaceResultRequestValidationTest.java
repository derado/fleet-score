package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Circumstance;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RaceResultRequestValidationTest {

  private static final String MESSAGE = "Exactly one of position or circumstance must be provided";

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    validatorFactory.close();
  }

  @Test
  void valid_whenPositionProvidedAndCircumstanceNull() {
    var request = new RaceResultRequest(1L, 1, null);

    Set<ConstraintViolation<RaceResultRequest>> violations = validator.validate(request);

    assertThat(violations).filteredOn(v -> v.getMessage().equals(MESSAGE)).isEmpty();
  }

  @Test
  void valid_whenCircumstanceProvidedAndPositionNull() {
    var request = new RaceResultRequest(1L, null, Circumstance.DNF);

    Set<ConstraintViolation<RaceResultRequest>> violations = validator.validate(request);

    assertThat(violations).filteredOn(v -> v.getMessage().equals(MESSAGE)).isEmpty();
  }

  @Test
  void invalid_whenBothPositionAndCircumstanceAreNull() {
    var request = new RaceResultRequest(1L, null, null);

    Set<ConstraintViolation<RaceResultRequest>> violations = validator.validate(request);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(MESSAGE);
  }

  @Test
  void invalid_whenBothPositionAndCircumstanceAreSet() {
    var request = new RaceResultRequest(1L, 1, Circumstance.DNF);

    Set<ConstraintViolation<RaceResultRequest>> violations = validator.validate(request);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains(MESSAGE);
  }
}
