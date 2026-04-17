package com.fleetscore.regatta.service;

import java.time.LocalDate;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailor.repository.SailorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class SailorResolverTest {

    @Autowired
    private SailorResolver sailorResolver;

    @Autowired
    private SailorRepository sailorRepository;

    private static final String EMAIL = "john@example.com";
    private static final String NAME = "John Doe";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 15);
    private static final Gender GENDER = Gender.M;

    @Test
    void findOrCreate_withSailorId_returnsSailorById() {
        Sailor existingSailor = createAndSaveSailor(EMAIL, NAME, DATE_OF_BIRTH);

        Sailor result = sailorResolver.findOrCreate(existingSailor.getId(), "other@example.com", "Other Name", LocalDate.of(2000, 1, 1), Gender.F);

        assertThat(result.getId()).isEqualTo(existingSailor.getId());
        assertThat(result.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void findOrCreate_withoutSailorId_findsByNameAndDateOfBirth() {
        Sailor existingSailor = createAndSaveSailor("original@example.com", NAME, DATE_OF_BIRTH);

        Sailor result = sailorResolver.findOrCreate(null, "new@example.com", NAME, DATE_OF_BIRTH, Gender.F);

        assertThat(result.getId()).isEqualTo(existingSailor.getId());
        assertThat(sailorRepository.count()).isEqualTo(1);
    }

    @Test
    void findOrCreate_noMatchFound_createsNewSailor() {
        Sailor result = sailorResolver.findOrCreate(null, EMAIL, NAME, DATE_OF_BIRTH, GENDER);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
        assertThat(result.getGender()).isEqualTo(GENDER);
        assertThat(sailorRepository.count()).isEqualTo(1);
    }

    private Sailor createAndSaveSailor(String email, String name, LocalDate dateOfBirth) {
        Sailor sailor = new Sailor();
        sailor.setName(name);
        sailor.setEmail(email);
        sailor.setDateOfBirth(dateOfBirth);
        sailor.setGender(GENDER);
        return sailorRepository.save(sailor);
    }
}
