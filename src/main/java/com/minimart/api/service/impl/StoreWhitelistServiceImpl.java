package com.minimart.api.service.impl;

import com.minimart.api.dto.PaginationDto;
import com.minimart.api.dto.store.whitelist.CreateWhitelistRequest;
import com.minimart.api.dto.store.whitelist.WhitelistDto;
import com.minimart.api.dto.store.whitelist.WhitelistResponseDto;
import com.minimart.api.entity.StoreWhitelists;
import com.minimart.api.entity.Stores;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.StoreRepository;
import com.minimart.api.repository.StoreWhitelistRepository;
import com.minimart.api.service.IStoreWhitelistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreWhitelistServiceImpl implements IStoreWhitelistService {

    private final StoreWhitelistRepository whitelistRepository;
    private final StoreRepository storeRepository;
    @Override
    public void createWhitelist(CreateWhitelistRequest request) {
        Stores store = storeRepository.findById(request.store_id()).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store data not found"
                )
        );

        if (store.getDeletedAt() != null || !store.getIsActive()) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Store data is probably deleted or inactive"
            );
        }


        StoreWhitelists existingWhitelist = whitelistRepository.findByStoreId(request.store_id());

        if (existingWhitelist != null) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT.getReasonPhrase(),
                    "Store is already registered in whitelist"
            );
        }

        StoreWhitelists whitelists = new StoreWhitelists();
        whitelists.setStore(store);
        whitelistRepository.save(whitelists);
    }

    @Override
    public WhitelistResponseDto getWhitelists(int pageNumber, int pageSize, String provinceName) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 100) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "pageNumber must be at least 1 and pageSize must be between 1 and 100"
            );
        }

        int pageIndex = Math.max(0, pageNumber - 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(
                Sort.Order.desc("createdAt")
        ));

        Page<StoreWhitelists> whitelists = whitelistRepository.findVisibleStores(pageable, provinceName);

        List<WhitelistDto> whitelistsDto = whitelists.stream().map(
                whitelist -> {
                    var dto = new WhitelistDto();
                    dto.setId(whitelist.getId());
                    dto.setStore_id(whitelist.getStore().getId());
                    dto.setStore_code(whitelist.getStore().getStoreCode());
                    dto.setStore_name(whitelist.getStore().getStoreName());
                    dto.setBranch_id(whitelist.getStore().getBranch().getId());
                    dto.setBranch_code(whitelist.getStore().getBranch().getBranchCode());
                    dto.setBranch_name(whitelist.getStore().getBranch().getBranchName());
                    dto.setProvince_id(whitelist.getStore().getBranch().getProvince().getId());
                    dto.setProvince_name(whitelist.getStore().getBranch().getProvince().getName());
                    dto.setCreated_at(whitelist.getCreatedAt());
                    dto.setCreated_by(whitelist.getCreatedBy());

                    return dto;
                }
        ).toList();

        long totalItems = whitelists.getTotalElements();
        long from = 0;
        long to = 0;

        if (totalItems > 0) {
            from = (long) pageIndex * pageSize + 1;
            to = from + whitelists.getNumberOfElements() - 1;
        }

        PaginationDto pagination = new PaginationDto(
                pageNumber,
                pageSize,
                totalItems,
                whitelists.getTotalPages(),
                from,
                to,
                whitelists.hasNext(),
                whitelists.hasPrevious()
        );

        return new WhitelistResponseDto(
                whitelistsDto,
                pagination
        );
    }

    @Override
    public WhitelistDto getWhitelist(Long id) {
        StoreWhitelists whitelist = whitelistRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store whitelist data not found"
                )
        );

        if (!whitelist.getStore().getIsActive() ||
                whitelist.getStore().getDeletedAt() != null ||
                    !whitelist.getStore().getBranch().getIsActive() ||
                        whitelist.getStore().getBranch().getDeletedAt() != null
        ) {
            throw new AppException(
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    "Store is probably been deleted or inactive"
            );
        }

        var dto = new WhitelistDto();
        dto.setId(whitelist.getId());
        dto.setStore_id(whitelist.getStore().getId());
        dto.setStore_code(whitelist.getStore().getStoreCode());
        dto.setStore_name(whitelist.getStore().getStoreName());
        dto.setBranch_id(whitelist.getStore().getBranch().getId());
        dto.setBranch_code(whitelist.getStore().getBranch().getBranchCode());
        dto.setBranch_name(whitelist.getStore().getBranch().getBranchName());
        dto.setProvince_id(whitelist.getStore().getBranch().getProvince().getId());
        dto.setProvince_name(whitelist.getStore().getBranch().getProvince().getName());
        dto.setCreated_at(whitelist.getCreatedAt());
        dto.setCreated_by(whitelist.getCreatedBy());

        return dto;
    }

    @Override
    public void deleteWhitelist(Long id) {
        StoreWhitelists whitelist = whitelistRepository.findById(id).orElseThrow(
                () -> new AppException(
                        HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store whitelist data not found"
                )
        );

        whitelistRepository.deleteById(whitelist.getId());
    }

}
