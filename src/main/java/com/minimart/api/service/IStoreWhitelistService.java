package com.minimart.api.service;

import com.minimart.api.dto.store.whitelist.CreateWhitelistRequest;
import com.minimart.api.dto.store.whitelist.WhitelistDto;
import com.minimart.api.dto.store.whitelist.WhitelistResponseDto;

public interface IStoreWhitelistService {

    void createWhitelist(CreateWhitelistRequest request);

    WhitelistResponseDto getWhitelists(int pageNumber, int pageSize, String provinceName);

    WhitelistDto getWhitelist(Long id);

    void deleteWhitelist(Long id);
}
