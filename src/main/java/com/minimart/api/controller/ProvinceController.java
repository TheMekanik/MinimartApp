package com.minimart.api.controller;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.dto.province.ProvinceResponseDto;
import com.minimart.api.service.IProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/province")
public class ProvinceController {

    private final IProvinceService provinceService;

    @GetMapping("")
    @PreAuthorize("hasAuthority('PROVINCE_VIEW')")
    public ResponseEntity<ApiResponseDto<List<ProvinceResponseDto>>> getProvinces() {
        List<ProvinceResponseDto> provinces = provinceService.getProvinces();

        return ResponseEntity.ok().body(
                ApiResponseDto.<List<ProvinceResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get provinces successful")
                        .data(provinces)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROVINCE_VIEW')")
    public ResponseEntity<ApiResponseDto<ProvinceResponseDto>> getProvince(@PathVariable Long id) {
        ProvinceResponseDto province = provinceService.getProvince(id);

        return ResponseEntity.ok().body(
                ApiResponseDto.<ProvinceResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get province successful")
                        .data(province)
                        .build()
        );
    }
}
