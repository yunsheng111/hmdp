package com.hmdp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 增强的关注用户DTO
 * 继承FollowUserDTO，添加关注状态和互关状态信息
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnhancedFollowUserDTO extends FollowUserDTO {
    
    /**
     * 当前用户是否已关注此用户
     */
    private Boolean isFollowed;
    
    /**
     * 是否互相关注
     */
    private Boolean isMutualFollow;
    
    /**
     * 构造函数
     */
    public EnhancedFollowUserDTO() {
        super();
        this.isFollowed = false;
        this.isMutualFollow = false;
    }
    
    /**
     * 从FollowUserDTO创建EnhancedFollowUserDTO
     * @param followUserDTO 基础关注用户DTO
     * @return 增强的关注用户DTO
     */
    public static EnhancedFollowUserDTO fromFollowUserDTO(FollowUserDTO followUserDTO) {
        EnhancedFollowUserDTO enhanced = new EnhancedFollowUserDTO();
        enhanced.setId(followUserDTO.getId());
        enhanced.setNickName(followUserDTO.getNickName());
        enhanced.setIcon(followUserDTO.getIcon());
        enhanced.setIntroduce(followUserDTO.getIntroduce());
        enhanced.setIsFollowed(false);
        enhanced.setIsMutualFollow(false);
        return enhanced;
    }
    
    /**
     * 设置关注状态
     * @param isFollowed 是否已关注
     * @param isMutualFollow 是否互相关注
     */
    public void setFollowStatus(Boolean isFollowed, Boolean isMutualFollow) {
        this.isFollowed = isFollowed;
        this.isMutualFollow = isMutualFollow;
    }
}
