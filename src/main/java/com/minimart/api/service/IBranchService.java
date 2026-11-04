package com.minimart.api.service;

import com.minimart.api.dto.branch.BranchRequestDto;
import com.minimart.api.dto.branch.BranchResponseDto;
import com.minimart.api.dto.branch.BranchesResponseDto;
import com.minimart.api.entity.Branches;

import java.util.List;

public interface IBranchService {

    void createBranch(BranchRequestDto request);

    void updateBranch(Long id, BranchRequestDto request);

    void deleteBranch(Long id);

    BranchesResponseDto getBranches(int pageNumber, int pageSize, Boolean isActive);
}
