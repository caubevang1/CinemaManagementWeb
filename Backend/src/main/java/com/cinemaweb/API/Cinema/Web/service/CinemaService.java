package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.CinemaRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.CinemaResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.configuration.CacheConfig;
import com.cinemaweb.API.Cinema.Web.mapper.CinemaMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import com.cinemaweb.API.Cinema.Web.search.CinemaSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaService {
    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;
    private final CinemaSearchService cinemaSearchService;


    @Cacheable(value = CacheConfig.CINEMAS)
    public List<CinemaResponse> getAllCinemas() {
        return cinemaMapper.toCinemaResponseList(cinemaRepository.findAll());
    }

    @Cacheable(value = CacheConfig.CINEMA, key = "#cinemaId")
    public CinemaResponse getCinema(String cinemaId) {
        return cinemaMapper.toCinemaResponse(cinemaRepository.findById(cinemaId).orElseThrow(() ->
        new RuntimeException("Cinema id is not found")));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CINEMAS, allEntries = true),
            @CacheEvict(value = CacheConfig.CINEMA, allEntries = true)
    })
    public void createCinema(CinemaRequest cinemaCreateRequest) {
        if(cinemaRepository.existsByCinemaName(cinemaCreateRequest.getCinemaName())) {
            throw new RuntimeException("You can't create because cinema name has existed!");
        }
        Cinema saved = cinemaRepository.save(cinemaMapper.toCinema(cinemaCreateRequest));
        cinemaSearchService.upsert(saved); // đồng bộ chỉ mục tìm kiếm
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CINEMAS, allEntries = true),
            @CacheEvict(value = CacheConfig.CINEMA, key = "#cinemaId")
    })
    public void updateCinema(String cinemaId,CinemaRequest cinemaUpdateRequest) {
        Cinema cinema = cinemaRepository.findById(cinemaId).orElseThrow(()
                -> new RuntimeException("Cinema id is not found"));
        if(cinema.getCinemaName().equals(cinemaUpdateRequest.getCinemaName())) {
            cinemaMapper.toUpdateCinema(cinema, cinemaUpdateRequest);
            cinemaSearchService.upsert(cinemaRepository.save(cinema)); // đồng bộ chỉ mục tìm kiếm
        } else {
            if (cinemaRepository.existsByCinemaName(cinemaUpdateRequest.getCinemaName())) {
                throw new RuntimeException("You can't update because cinema name has existed!");
            } else {
                cinemaMapper.toUpdateCinema(cinema, cinemaUpdateRequest);
                cinemaRepository.save(cinema);
            }
        }
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CINEMAS, allEntries = true),
            @CacheEvict(value = CacheConfig.CINEMA, key = "#cinemaId")
    })
    public void deleteCinema(String cinemaId) {
        cinemaRepository.deleteById(cinemaId);
        cinemaSearchService.remove(Integer.parseInt(cinemaId)); // gỡ khỏi chỉ mục tìm kiếm
    }
}
