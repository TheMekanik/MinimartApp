package com.minimart.api.service.impl;

import com.minimart.api.dto.PaginationDto;
import com.minimart.api.dto.store.StoreRequestDto;
import com.minimart.api.dto.store.StoreResponseDto;
import com.minimart.api.dto.store.StoresResponseDto;
import com.minimart.api.entity.Branches;
import com.minimart.api.entity.Stores;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.BranchRepository;
import com.minimart.api.repository.StoreRepository;
import com.minimart.api.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements IStoreService {

    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;

    @Override
    public void createStore(StoreRequestDto request) {
        Branches branch = branchRepository.findById(request.branch_id()).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Branch data for store is not found"
                )
        );

        Stores store = new Stores();
        store.setStoreName(request.store_name());
        store.setStoreCode(request.store_code());
        store.setBranch(branch);
        store.setIsActive(request.is_active());

        storeRepository.save(store);
    }

    @Override
    public void updateStore(Long id, StoreRequestDto request) {
        Stores store = storeRepository.findById(id).orElseThrow(
                () ->  new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store data not found"
                )
        );

        if (!store.getBranch().getId().equals(request.branch_id())) {
            Branches storeBranch = branchRepository.findById(request.branch_id()).orElseThrow(
                    () -> new AppException(
                            HttpStatus.NOT_FOUND,
                            HttpStatus.NOT_FOUND.getReasonPhrase(),
                            "Branch data for store is not found"
                    )
            );

            store.setBranch(storeBranch);
        }

        store.setStoreCode(request.store_code());
        store.setStoreName(request.store_name());
        store.setIsActive(request.is_active());

        storeRepository.save(store);
    }

    @Override
    public void deleteStore(Long id) {
        Stores store = storeRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store data not found"
                )
        );

        store.setDeletedAt(Instant.now());
        storeRepository.save(store);
    }

    @Override
    public StoresResponseDto getStores(int pageNumber, int pageSize, Boolean isActive, String provinceName) {
        int pageIndex = Math.max(0, pageNumber - 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(
           Sort.Order.asc("branch.id"),
                Sort.Order.desc("updatedAt")
        ));

        Page<Stores> stores = storeRepository.findActiveStores(isActive, provinceName, pageable);

        List<StoreResponseDto> storesDto = stores.stream().map(
                store -> {
                    var dto = new StoreResponseDto();
                    dto.setId(store.getId());
                    dto.setStore_code(store.getStoreCode());
                    dto.setStore_name(store.getStoreName());
                    dto.setBranch_id(store.getBranch().getId());
                    dto.setBranch_name(store.getBranch().getBranchName());
                    dto.setProvince_name(store.getBranch().getProvince().getName());
                    dto.setIs_active(store.getIsActive());

                    return dto;
                }
        ).toList();

        long totalItems = stores.getTotalElements();
        long from = 0;
        long to = 0;

        if (totalItems > 0) {
            from = (long) pageIndex * pageSize + 1;
            to = from + stores.getNumberOfElements() - 1;
        }

        PaginationDto pagination = new PaginationDto(
                pageNumber,
                pageSize,
                totalItems,
                stores.getTotalPages(),
                from,
                to,
                stores.hasNext(),
                stores.hasPrevious()
        );

        return new StoresResponseDto(
                storesDto, pagination
        );
    }

    @Override
    public StoreResponseDto getStore(Long id) {
        Stores store = storeRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store data not found"
                )
        );

        StoreResponseDto storeDto = new StoreResponseDto();
        storeDto.setId(store.getId());
        storeDto.setStore_code(store.getStoreCode());
        storeDto.setStore_name(store.getStoreName());
        storeDto.setBranch_id(store.getBranch().getId());
        storeDto.setBranch_name(store.getBranch().getBranchName());
        storeDto.setProvince_name(store.getBranch().getProvince().getName());
        storeDto.setIs_active(store.getIsActive());

        return storeDto;
    }
}
