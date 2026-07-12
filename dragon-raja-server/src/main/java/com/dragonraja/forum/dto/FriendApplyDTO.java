package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendApplyDTO {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
}
