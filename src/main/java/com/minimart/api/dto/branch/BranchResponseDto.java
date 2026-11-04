package com.minimart.api.dto.branch;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BranchResponseDto {
    private Long id;
    private Long province_id;
    private String province_name;
    private String branch_code;
    private String branch_name;
    private Boolean is_active;
}
