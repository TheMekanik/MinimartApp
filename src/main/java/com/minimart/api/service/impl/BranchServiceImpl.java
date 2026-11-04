package com.minimart.api.service.impl;

import com.minimart.api.dto.PaginationDto;
import com.minimart.api.dto.branch.BranchRequestDto;
import com.minimart.api.dto.branch.BranchResponseDto;
import com.minimart.api.dto.branch.BranchesResponseDto;
import com.minimart.api.entity.Branches;
import com.minimart.api.entity.Provinces;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.BranchRepository;
import com.minimart.api.repository.ProvinceRepository;
import com.minimart.api.service.IBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements IBranchService {

    private final BranchRepository branchRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    public void createBranch(BranchRequestDto request) {
        Branches branch = new Branches();
        branch.setBranchCode(request.branch_code());
        branch.setBranchName(request.branch_name());
        branch.setIsActive(request.is_active());

        branchRepository.save(branch);
    }

    @Override
    public void updateBranch(Long id, BranchRequestDto request) {
        Branches branch = branchRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Branch not found"
                )
        );

        if (!request.province_id().equals(branch.getProvince().getId())) {
            Provinces province = provinceRepository.findById(request.province_id()).orElseThrow(
                    () -> new AppException(
                            HttpStatus.NOT_FOUND,
                            HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                            "Province data for province_id not found"
                    )
            );

            branch.setProvince(province);
        }

        branch.setBranchCode(request.branch_code());
        branch.setBranchName(request.branch_name());
        branch.setIsActive(request.is_active());
    }

    @Override
    public void deleteBranch(Long id) {
        Branches branch = branchRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Branch data to be deleted is not found"
                )
        );

        branch.setDeletedAt(Instant.now());
        branchRepository.save(branch);
    }

    @Override
    public BranchesResponseDto getBranches(int pageNumber, int pageSize, Boolean isActive) {
        int pageIndex = Math.max(0, pageNumber - 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(
                Sort.Order.asc("province.id"),
                Sort.Order.desc("updatedAt")
        ));

        Page<Branches> branches = branchRepository.findActiveBranches(isActive, pageable);

        List<BranchResponseDto> branchesDto = branches.stream().map(
                branch -> {
                    var dto = new BranchResponseDto();
                    dto.setId(branch.getId());
                    dto.setBranch_code(branch.getBranchCode());
                    dto.setBranch_name(branch.getBranchName());
                    dto.setIs_active(branch.getIsActive());
                    dto.setProvince_id(branch.getProvince().getId());
                    dto.setProvince_name(branch.getProvince().getName());

                    return dto;
                }
        ).toList();

        long totalItems = branches.getTotalElements();
        long from = 0;
        long to = 0;

        if (totalItems > 0) {
            from = (long) pageIndex * pageSize + 1;
            to = from + branches.getNumberOfElements() - 1;
        }

        PaginationDto pagination = new PaginationDto(
          pageNumber,
          pageSize,
          totalItems,
          branches.getTotalPages(),
          from,
          to,
          branches.hasNext(),
          branches.hasPrevious()
        );

        return new BranchesResponseDto(
                branchesDto, pagination
        );
    }

    @Override
    public BranchResponseDto getBranch(Long id) {
        Branches branch = branchRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Branch data not found"
                )
        );

        BranchResponseDto branchDto = new BranchResponseDto();
        branchDto.setId(branch.getId());
        branchDto.setBranch_code(branch.getBranchCode());
        branchDto.setBranch_name(branch.getBranchName());
        branchDto.setProvince_id(branch.getProvince().getId());
        branchDto.setProvince_name(branch.getProvince().getName());
        branchDto.setIs_active(branch.getIsActive());

        return branchDto;
    }

}
