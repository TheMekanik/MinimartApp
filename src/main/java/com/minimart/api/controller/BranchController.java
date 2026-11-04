package com.minimart.api.controller;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.dto.branch.BranchRequestDto;
import com.minimart.api.dto.branch.BranchResponseDto;
import com.minimart.api.dto.branch.BranchesResponseDto;
import com.minimart.api.service.IBranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branch")
public class BranchController {

    private final IBranchService branchService;

    @PostMapping("")
    @PreAuthorize("hasAuthority('BRANCH_CREATE')")
    public ResponseEntity<ApiResponseDto<Void>> createBranch(@Valid @RequestBody BranchRequestDto request) {
        branchService.createBranch(request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message(HttpStatus.CREATED.getReasonPhrase())
                        .details("Branch has been successfully created")
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_UPDATE')")
    public ResponseEntity<ApiResponseDto<Void>> updateBranch(@PathVariable Long id, @Valid @RequestBody BranchRequestDto request) {
        branchService.updateBranch(id, request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Branch has been successfully updated")
                        .build()
        );
    }

    @PatchMapping("/delete-branch/{id}")
    @PreAuthorize("hasAuthority('BRANCH_DELETE')")
    public ResponseEntity<ApiResponseDto<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);

        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                        .details("Branch has been successfully deleted")
                        .build()
        );
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('BRANCH_VIEW')")
    public ResponseEntity<ApiResponseDto<List<BranchResponseDto>>> getBranches(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Boolean isActive
    ) {
        BranchesResponseDto branches = branchService.getBranches(pageNumber, pageSize, isActive);

        return ResponseEntity.ok().body(
                ApiResponseDto.<List<BranchResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get branches successful")
                        .data(branches.branches())
                        .meta(branches.meta())
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_VIEW')")
    public ResponseEntity<ApiResponseDto<BranchResponseDto>> getBranch(@PathVariable Long id) {

        BranchResponseDto branch = branchService.getBranch(id);

        return ResponseEntity.ok().body(
                ApiResponseDto.<BranchResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get branch successful")
                        .data(branch)
                        .build()
        );
    }
}
