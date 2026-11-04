package com.minimart.api.service;

import com.minimart.api.dto.province.ProvinceResponseDto;

import java.util.List;

public interface IProvinceService {
    List<ProvinceResponseDto> getProvinces();
    ProvinceResponseDto getProvince(Long id);
}
