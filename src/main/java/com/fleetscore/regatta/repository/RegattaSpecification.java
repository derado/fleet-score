package com.fleetscore.regatta.repository;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.regatta.api.dto.RegattaFilter;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.sailingclass.domain.SailingClass;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class RegattaSpecification {

    private RegattaSpecification() {
    }

    public static Specification<Regatta> withFilter(RegattaFilter filter) {
        return (root, query, cb) -> {
            if (filter == null || filter.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), containsLower(filter.name())));
            }

            if (filter.startDate() != null) {
                predicates.add(cb.equal(root.get("startDate"), filter.startDate()));
            }

            if (filter.venue() != null) {
                predicates.add(cb.like(cb.lower(root.get("venue")), containsLower(filter.venue())));
            }

            if (filter.sailingClass() != null) {
                Join<Regatta, SailingClass> sailingClassJoin = root.join("sailingClasses", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(sailingClassJoin.get("name")), containsLower(filter.sailingClass())));
            }

            if (filter.organiser() != null) {
                Join<Regatta, SailingClub> organiserJoin = root.join("organisers", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(organiserJoin.get("name")), containsLower(filter.organiser())));
            }

            if (filter.organisation() != null) {
                Join<Regatta, Organisation> organisationJoin = root.join("organisation", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(organisationJoin.get("name")), containsLower(filter.organisation())));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String containsLower(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
