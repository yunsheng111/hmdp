package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
     * 获取用户关注的人列表
     * @param userId 用户ID
     * @return 关注列表
     */
    @GetMapping("/followees/{userId}")
    public Result getFollowees(@PathVariable("userId") Long userId) {
        return followService.queryFollowees(userId);
    }

    /**
     * 获取用户的粉丝列表
     * @param userId 用户ID
     * @return 粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public Result getFollowers(@PathVariable("userId") Long userId) {
        return followService.queryFollowers(userId);
    }

    @GetMapping("/count/{userId}")
    public Result getFollowCount(@PathVariable("userId") Long userId) {
        return followService.queryFollowCount(userId);
    }

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

    /**
     * 分页获取用户关注的人列表
     * @param userId 用户ID
     * @param page 页码，默认为1
     * @param size 每页大小，默认为15
     * @return 分页关注列表
     */
    @GetMapping("/followees/{userId}/paged")
    public Result getFolloweesPaged(@PathVariable("userId") Long userId,
                                   @RequestParam(value = "page", defaultValue = "1") Integer page,
                                   @RequestParam(value = "size", defaultValue = "15") Integer size) {
        return followService.queryFolloweesPaged(userId, page, size);
    }

    /**
     * 分页获取用户的粉丝列表
     * @param userId 用户ID
     * @param page 页码，默认为1
     * @param size 每页大小，默认为15
     * @return 分页粉丝列表
     */
    @GetMapping("/followers/{userId}/paged")
    public Result getFollowersPaged(@PathVariable("userId") Long userId,
                                   @RequestParam(value = "page", defaultValue = "1") Integer page,
                                   @RequestParam(value = "size", defaultValue = "15") Integer size) {
        return followService.queryFollowersPaged(userId, page, size);
    }

    /**
     * 批量查询关注状态
     * @param targetUserIds 目标用户ID列表
     * @return 关注状态映射
     */
    @PostMapping("/batch-status")
    public Result batchQueryFollowStatus(@RequestBody List<Long> targetUserIds) {
        return followService.batchQueryFollowStatus(targetUserIds);
    }

}
