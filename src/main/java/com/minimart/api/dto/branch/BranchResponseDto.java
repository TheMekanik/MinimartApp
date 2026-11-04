package com.minimart.api.dto.branch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minimart.api.dto.store.StoreResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BranchResponseDto {
    private Long id;
    private Long province_id;
    private String province_name;
    private String branch_code;
    private String branch_name;
    private Boolean is_active;
    private List<StoreResponseDto> stores;
}
