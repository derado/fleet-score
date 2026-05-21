package com.fleetscore.regatta.service;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.CreateSeriesRequest;
import com.fleetscore.regatta.api.dto.SeriesResponse;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Series;
import com.fleetscore.regatta.domain.SeriesRegatta;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.SeriesRepository;
import com.fleetscore.user.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final RegattaRepository regattaRepository;

    @Transactional
    public SeriesResponse createSeries(UserAccount owner, CreateSeriesRequest request) {
        Series series = buildSeries(owner, request);
        return toResponse(seriesRepository.save(series));
    }

    @Transactional(readOnly = true)
    public SeriesResponse findSeriesById(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series", id));
        return toResponse(series);
    }

    @Transactional(readOnly = true)
    public List<SeriesResponse> findAllByOwner(UserAccount owner) {
        return seriesRepository.findByOwnerId(owner.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Series buildSeries(UserAccount owner, CreateSeriesRequest request) {
        Series series = new Series();
        series.setName(request.name());
        series.setDescription(request.description());
        series.setScoringType(request.scoringType());
        series.setOwner(owner);
        series.setThrowoutAfter(request.throwoutAfter() != null ? request.throwoutAfter() : 0);
        series.setThrowoutLimit(request.throwoutLimit() != null ? request.throwoutLimit() : 0);
        request.regattas().forEach(entry -> series.getRegattas().add(buildSeriesRegatta(series, entry)));
        return series;
    }

    private SeriesRegatta buildSeriesRegatta(Series series, CreateSeriesRequest.RegattaEntry entry) {
        Regatta regatta = regattaRepository.findById(entry.regattaId())
                .orElseThrow(() -> new ResourceNotFoundException("Regatta", entry.regattaId()));
        SeriesRegatta sr = new SeriesRegatta();
        sr.setSeries(series);
        sr.setRegatta(regatta);
        sr.setCoefficient(entry.coefficient() != null ? entry.coefficient() : 1.0);
        return sr;
    }

    private SeriesResponse toResponse(Series series) {
        List<SeriesResponse.RegattaEntry> regattas = series.getRegattas().stream()
                .map(sr -> new SeriesResponse.RegattaEntry(sr.getRegatta().getId(), sr.getCoefficient()))
                .toList();
        return new SeriesResponse(
                series.getId(),
                series.getName(),
                series.getDescription(),
                series.getScoringType(),
                series.getOwner().getEmail(),
                regattas,
                series.getThrowoutAfter(),
                series.getThrowoutLimit()
        );
    }
}
