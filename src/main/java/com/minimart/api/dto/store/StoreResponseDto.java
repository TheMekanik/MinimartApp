package com.minimart.api.dto.store;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreResponseDto {

    private Long id;
    private Long branch_id;
    private String branch_name;
    private String province_name;
    private String store_code;
    private String store_name;
    private Boolean is_active;
}
