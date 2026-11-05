package com.minimart.api.service;

import com.minimart.api.dto.branch.BranchRequestDto;
import com.minimart.api.entity.Branches;
import com.minimart.api.entity.Provinces;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.BranchRepository;
import com.minimart.api.repository.ProvinceRepository;
import com.minimart.api.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProvinceRepository provinceRepository;

    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        branchService = new BranchServiceImpl(branchRepository, provinceRepository);
    }

    @Test
    void createBranchSavesBranchWithProvinceAndRequestValues() {
        Provinces province = new Provinces();
        province.setId(1L);
        BranchRequestDto request = new BranchRequestDto(1L, 1L, "BR-01", "Central", true);

        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));

        branchService.createBranch(request);

        ArgumentCaptor<Branches> captor = ArgumentCaptor.forClass(Branches.class);
        verify(branchRepository).save(captor.capture());
        Branches saved = captor.getValue();
        assertEquals(province, saved.getProvince());
        assertEquals("BR-01", saved.getBranchCode());
        assertEquals("Central", saved.getBranchName());
        assertEquals(true, saved.getIsActive());
    }

    @Test
    void updateBranchUpdatesEditableBranch() {
        Provinces province = new Provinces();
        province.setId(1L);
        Branches branch = new Branches();
        branch.setProvince(province);
        BranchRequestDto request = new BranchRequestDto(1L, 1L, "BR-02", "Updated", false);

        when(branchRepository.findEditableById(10L)).thenReturn(Optional.of(branch));

        branchService.updateBranch(10L, request);

        verify(branchRepository).save(branch);
        assertEquals("BR-02", branch.getBranchCode());
        assertEquals("Updated", branch.getBranchName());
        assertEquals(false, branch.getIsActive());
    }

    @Test
    void deleteBranchSoftDeletesExistingBranch() {
        Branches branch = new Branches();
        when(branchRepository.findEditableById(10L)).thenReturn(Optional.of(branch));

        branchService.deleteBranch(10L);

        verify(branchRepository).save(branch);
        assertNotNull(branch.getDeletedAt());
    }

    @Test
    void updateBranchThrowsWhenBranchDoesNotExist() {
        when(branchRepository.findEditableById(99L)).thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> branchService.updateBranch(
                        99L,
                        new BranchRequestDto(99L, 1L, "BR-99", "Missing", true)
                )
        );

        verify(branchRepository, never()).save(any());
    }
}
