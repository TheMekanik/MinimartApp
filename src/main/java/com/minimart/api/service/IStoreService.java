package com.minimart.api.service;

import com.minimart.api.dto.store.StoreRequestDto;
import com.minimart.api.dto.store.StoreResponseDto;
import com.minimart.api.dto.store.StoresResponseDto;

public interface IStoreService {

    void createStore(StoreRequestDto request);

    void updateStore(Long id, StoreRequestDto request);

    void deleteStore(Long id);

    StoresResponseDto getStores(int pageNumber, int pageSize, Boolean isActive, String provinceName);

    StoreResponseDto getStore(Long id);
}
