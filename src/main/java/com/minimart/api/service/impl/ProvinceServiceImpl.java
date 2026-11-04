package com.minimart.api.service.impl;

import com.minimart.api.dto.province.ProvinceResponseDto;
import com.minimart.api.entity.Provinces;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.ProvinceRepository;
import com.minimart.api.service.IProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements IProvinceService {

    private final ProvinceRepository provinceRepository;

    @Override
    public List<ProvinceResponseDto> getProvinces() {
        List<Provinces> provinces = provinceRepository.findAll();

        return provinces.stream().map(
                province -> {
                    var dto = new ProvinceResponseDto();
                    dto.setId(province.getId());
                    dto.setName(province.getName());

                    return dto;
                }
        ).toList();
    }

    @Override
    public ProvinceResponseDto getProvince(Long id) {
        Provinces province = provinceRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Data provinsi tidak ditemukan"
                )
        );

        ProvinceResponseDto provinceDto = new ProvinceResponseDto();
        provinceDto.setId(province.getId());
        provinceDto.setName(province.getName());

        return provinceDto;
    }

}
