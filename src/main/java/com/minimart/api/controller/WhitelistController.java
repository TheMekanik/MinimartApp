package com.minimart.api.controller;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.dto.store.whitelist.CreateWhitelistRequest;
import com.minimart.api.dto.store.whitelist.WhitelistDto;
import com.minimart.api.dto.store.whitelist.WhitelistResponseDto;
import com.minimart.api.service.IStoreWhitelistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/whitelist")
public class WhitelistController {

    private final IStoreWhitelistService whitelistService;

    @PostMapping("")
    @PreAuthorize("hasAuthority('STORE_WHITELIST_CREATE')")
    public ResponseEntity<ApiResponseDto<Void>> createWhitelist(@Valid @RequestBody CreateWhitelistRequest request) {
        whitelistService.createWhitelist(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message(HttpStatus.CREATED.getReasonPhrase())
                        .details("Whitelist has been successfully created")
                        .build()
        );
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('STORE_WHITELIST_VIEW')")
    public ResponseEntity<ApiResponseDto<List<WhitelistDto>>> getWhitelists(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String provinceName
    ) {
        WhitelistResponseDto whitelists = whitelistService.getWhitelists(pageNumber, pageSize, provinceName);

        return ResponseEntity.ok().body(
                ApiResponseDto.<List<WhitelistDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get store whitelists successful")
                        .data(whitelists.whitelists())
                        .meta(whitelists.meta())
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_WHITELIST_VIEW')")
    public ResponseEntity<ApiResponseDto<WhitelistDto>> getWhitelist(@PathVariable Long id) {
        WhitelistDto whitelist = whitelistService.getWhitelist(id);

        return ResponseEntity.ok().body(
                ApiResponseDto.<WhitelistDto>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get store whitelist successful")
                        .data(whitelist)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_WHITELIST_DELETE')")
    public ResponseEntity<ApiResponseDto<Void>> deleteWhitelist(@PathVariable Long id) {
        whitelistService.deleteWhitelist(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                        .details("Store has been deleted from whitelist")
                        .build()
        );
    }
}
