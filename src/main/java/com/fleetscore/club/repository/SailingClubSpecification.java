package com.fleetscore.club.repository;

import com.fleetscore.club.api.dto.SailingClubFilter;
import com.fleetscore.club.domain.SailingClub;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SailingClubSpecification {

    private SailingClubSpecification() {
    }

    public static Specification<SailingClub> withFilter(SailingClubFilter filter) {
        return (root, query, cb) -> {
            if (filter == null || filter.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), containsLower(filter.name())));
            }

            if (filter.sailingNationId() != null) {
                predicates.add(cb.equal(root.get("sailingNation").get("id"), filter.sailingNationId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String containsLower(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}