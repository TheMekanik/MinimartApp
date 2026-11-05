package com.minimart.api.service;

import com.minimart.api.dto.store.whitelist.CreateWhitelistRequest;
import com.minimart.api.entity.StoreWhitelists;
import com.minimart.api.entity.Stores;
import com.minimart.api.repository.StoreRepository;
import com.minimart.api.repository.StoreWhitelistRepository;
import com.minimart.api.service.impl.StoreWhitelistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreWhitelistServiceImplTest {

    @Mock
    private StoreWhitelistRepository whitelistRepository;

    @Mock
    private StoreRepository storeRepository;

    private StoreWhitelistServiceImpl whitelistService;

    @BeforeEach
    void setUp() {
        whitelistService = new StoreWhitelistServiceImpl(whitelistRepository, storeRepository);
    }

    @Test
    void createWhitelistSavesActiveStoreWhenNotAlreadyWhitelisted() {
        Stores store = new Stores();
        store.setIsActive(true);
        when(storeRepository.findById(5L)).thenReturn(Optional.of(store));
        when(whitelistRepository.findByStoreId(5L)).thenReturn(Optional.empty());

        whitelistService.createWhitelist(new CreateWhitelistRequest(5L));

        verify(whitelistRepository).save(argThat(whitelist -> whitelist.getStore() == store));
    }

    @Test
    void createWhitelistRejectsDuplicateStore() {
        Stores store = new Stores();
        store.setIsActive(true);
        when(storeRepository.findById(5L)).thenReturn(Optional.of(store));
        when(whitelistRepository.findByStoreId(5L)).thenReturn(Optional.of(new StoreWhitelists()));

        assertThrows(
                com.minimart.api.exception.AppException.class,
                () -> whitelistService.createWhitelist(new CreateWhitelistRequest(5L))
        );

        verify(whitelistRepository, never()).save(any());
    }

    @Test
    void deleteWhitelistDeletesExistingWhitelist() {
        StoreWhitelists whitelist = new StoreWhitelists();
        whitelist.setId(8L);
        when(whitelistRepository.findById(8L)).thenReturn(Optional.of(whitelist));

        whitelistService.deleteWhitelist(8L);

        verify(whitelistRepository).deleteById(8L);
    }
}
