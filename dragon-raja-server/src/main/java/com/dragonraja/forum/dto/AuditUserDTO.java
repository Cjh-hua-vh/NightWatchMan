package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户审核 DTO
 * 管理员审核待审核用户时使用
 *
 * @author Kou
 */
@Data
public class AuditUserDTO {

    /** 审核结果：1-通过 2-拒绝（封禁） */
    @NotNull(message = "审核结果不能为空")
    private Integer status;
}
