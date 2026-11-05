package com.minimart.api.service;

import com.minimart.api.dto.store.StoreRequestDto;
import com.minimart.api.entity.Branches;
import com.minimart.api.entity.Stores;
import com.minimart.api.repository.BranchRepository;
import com.minimart.api.repository.StoreRepository;
import com.minimart.api.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BranchRepository branchRepository;

    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        storeService = new StoreServiceImpl(storeRepository, branchRepository);
    }

    @Test
    void getStoresPassesProvinceStatusAndPaginationToRepository() {
        when(storeRepository.findActiveStores(eq(true), eq("Jakarta"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = storeService.getStores(2, 20, true, "Jakarta");

        assertEquals(2, result.meta().currentPage());
        assertEquals(20, result.meta().perPage());
        verify(storeRepository).findActiveStores(eq(true), eq("Jakarta"), any(Pageable.class));
    }

    @Test
    void createStoreUsesVisibleBranch() {
        Branches branch = new Branches();
        StoreRequestDto request = new StoreRequestDto(1L, 7L, "Store A", "ST-A", true);
        when(branchRepository.findVisibleById(7L)).thenReturn(Optional.of(branch));

        storeService.createStore(request);

        verify(storeRepository).save(argThat(store ->
                store.getBranch() == branch
                        && "Store A".equals(store.getStoreName())
                        && "ST-A".equals(store.getStoreCode())
                        && Boolean.TRUE.equals(store.getIsActive())
        ));
    }
}
