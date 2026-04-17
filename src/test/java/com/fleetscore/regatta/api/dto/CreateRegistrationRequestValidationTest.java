package com.fleetscore.regatta.api.dto;

import com.fleetscore.common.domain.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateRegistrationRequestValidationTest {

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
  void valid_whenSailingClubNameProvidedAndIdNull() {
    var request = buildRequest("Yacht Club", null);

    Set<ConstraintViolation<CreateRegistrationRequest>> violations = validator.validate(request);

    assertThat(violations).filteredOn(v -> v.getMessage()
            .contains("sailingClubName or sailingClubId")).isEmpty();
  }

  @Test
  void valid_whenSailingClubIdProvidedAndNameNull() {
    var request = buildRequest(null, 42L);

    Set<ConstraintViolation<CreateRegistrationRequest>> violations = validator.validate(request);

    assertThat(violations).filteredOn(v -> v.getMessage()
            .contains("sailingClubName or sailingClubId")).isEmpty();
  }

  @Test
  void valid_whenBothSailingClubNameAndIdProvided() {
    var request = buildRequest("Yacht Club", 42L);

    Set<ConstraintViolation<CreateRegistrationRequest>> violations = validator.validate(request);

    assertThat(violations).filteredOn(v -> v.getMessage()
            .contains("sailingClubName or sailingClubId")).isEmpty();
  }

  @Test
  void invalid_whenBothSailingClubNameAndIdAbsent() {
    var request = buildRequest(null, null);

    Set<ConstraintViolation<CreateRegistrationRequest>> violations = validator.validate(request);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Either sailingClubName or sailingClubId must be provided");
  }

  @Test
  void invalid_whenSailingClubNameIsBlankAndIdIsNull() {
    var request = buildRequest("   ", null);

    Set<ConstraintViolation<CreateRegistrationRequest>> violations = validator.validate(request);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Either sailingClubName or sailingClubId must be provided");
  }

  private CreateRegistrationRequest buildRequest(String sailingClubName, Long sailingClubId) {
    return new CreateRegistrationRequest(
        "John Doe",
        "john@example.com",
        LocalDate.of(1990, 1, 1),
        Gender.M,
        sailingClubName,
        sailingClubId,
        null,
        1L,
        1L,
        100
    );
  }
}
