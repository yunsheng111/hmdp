package com.hmdp.controller;


import com.hmdp.common.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private IFollowService followService;
    /**
     * 关注或取消关注
     * @param followUserId
     * @param isFollow
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow){
        return followService.follow(followUserId,isFollow);
    }


    /**
     * @description: 查询是否关注
     * @author: yate
     * @date: 2025/5/27 上午12:02
     * @param: [followUserId]
     * @return: com.hmdp.common.Result
     **/
    @GetMapping("/or/not/{id}")
    public Result followOr(@PathVariable("id") Long followUserId){
        return followService.isFollow(followUserId);

    }

    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }


}
