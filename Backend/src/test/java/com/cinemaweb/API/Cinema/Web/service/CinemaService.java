package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.CinemaRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.CinemaResponse;
import com.cinemaweb.API.Cinema.Web.entity.Cinema;
import com.cinemaweb.API.Cinema.Web.mapper.CinemaMapper;
import com.cinemaweb.API.Cinema.Web.repository.CinemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private CinemaMapper cinemaMapper;


    public List<CinemaResponse> getAllCinemas() {
        return cinemaMapper.toCinemaResponseList(cinemaRepository.findAll());
    }

    public CinemaResponse getCinema(String cinemaId) {
        return cinemaMapper.toCinemaResponse(cinemaRepository.findById(cinemaId).orElseThrow(() ->
        new RuntimeException("Cinema id is not found")));
    }

    public void createCinema(CinemaRequest cinemaCreateRequest) {
        cinemaRepository.save(cinemaMapper.toCinema(cinemaCreateRequest));
    }

    public void updateCinema(String cinemaId,CinemaRequest cinemaUpdateRequest) {
        Cinema cinema = cinemaRepository.findById(cinemaId).orElseThrow(()
                -> new RuntimeException("Cinema id is not found"));
        cinemaMapper.toUpdateCinema(cinema, cinemaUpdateRequest);
        cinemaRepository.save(cinema);
    }

    public void deleteCinema(String cinemaId) {
        cinemaRepository.deleteById(cinemaId);
    }
}
