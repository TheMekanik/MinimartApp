package com.minimart.api.controller;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.dto.store.StoreRequestDto;
import com.minimart.api.dto.store.StoreResponseDto;
import com.minimart.api.dto.store.StoresResponseDto;
import com.minimart.api.service.IStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store")
public class StoreController {

    private final IStoreService storeService;

    @PostMapping("")
    @PreAuthorize("hasAuthority('STORE_CREATE')")
    public ResponseEntity<ApiResponseDto<Void>> createStore(@Valid @RequestBody StoreRequestDto request) {
        storeService.createStore(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message(HttpStatus.CREATED.getReasonPhrase())
                        .details("Store has been successfully created")
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<ApiResponseDto<Void>> updateStore(@PathVariable Long id, @Valid @RequestBody StoreRequestDto request) {
        storeService.updateStore(id, request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Store has been successfully updated")
                        .build()
        );
    }

    @PatchMapping("/delete-store/{id}")
    @PreAuthorize("hasAuthority('STORE_DELETE')")
    public ResponseEntity<ApiResponseDto<Void>> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                        .details("Store has been successfully deleted")
                        .build()
        );
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ResponseEntity<ApiResponseDto<List<StoreResponseDto>>> getStores(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String provinceName
    ) {
        StoresResponseDto stores = storeService.getStores(pageNumber, pageSize, isActive, provinceName);

        return ResponseEntity.ok().body(
                ApiResponseDto.<List<StoreResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get stores successful")
                        .data(stores.stores())
                        .meta(stores.meta())
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ResponseEntity<ApiResponseDto<StoreResponseDto>> getStore(@PathVariable Long id) {
        StoreResponseDto storeDto = storeService.getStore(id);

        return ResponseEntity.ok().body(
                ApiResponseDto.<StoreResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Get store successful")
                        .data(storeDto)
                        .build()
        );
    }
}
