package com.minimart.api.dto.store.whitelist;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class WhitelistDto {
    private Long id;
    private Long store_id;
    private String store_code;
    private String store_name;
    private Long branch_id;
    private String branch_code;
    private String branch_name;
    private Long province_id;
    private String province_name;
    private Instant created_at;
    private String created_by;
}
