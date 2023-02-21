package com.friendship.controller;

import com.friendship.mapper.UserGroupMapper;
import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.pojo.UserGroupRelation;
import com.friendship.service.impl.UserGroupRelationService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/userGroupRelations")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class UserGroupRelationController {
    @Autowired
    private UserGroupRelationService groupRelationService;

    /**
     *  处理用户申请加入群聊的操作
     * @param groupId
     * @param request
     * @return
     */
    @PostMapping("/{groupId}")
    public Result applyToJoinGroup(@PathVariable Long groupId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Integer result = groupRelationService.applyToJoinGroup(groupId, token);
        if (result == 1) {
            return new Result(Code.INSERT_OK.getCode(), "加入成功!!");
        } else {
            return new Result(Code.INSERT_ERR.getCode(), "加入失败!!");
        }
    }

    /**
     * 当群主收到申请后如果同意则允许用户加入群聊, 将status设为1, 让用户成为正式群员
     * @param request
     * @param userId: 用户的id
     * @param groupId: 相关群的id
     * @return
     */
    @PutMapping
    public Result allowUserJoinGroup(HttpServletRequest request, Long userId, Long groupId) {
        Integer result = groupRelationService.allowUserJoinGroup(request.getHeader("token"), userId, groupId);
        if (result == null) {
            return new Result(Code.ILLEGAL_REQUEST.getCode(), "非法请求");
        }
        return new Result(Code.UPDATE_OK.getCode(), "更新成功");
    }

    /**
     * 得到所有的加群信息根据群主的id
     * @param request
     * @return
     */
    @GetMapping("/leader")
    public Result getAllApplyByGroupLeaderId(HttpServletRequest request) {
        List<Map<String, Object>> applyList = groupRelationService.getAllApplyByGroupLeaderId(request.getHeader("token"));
        return new Result(Code.SELECT_OK.getCode(), applyList);
    }

    @GetMapping("/leader/unread")
    public Result getUnreadEnterGroupMessageNumber(HttpServletRequest request){
        Long messageNumber = groupRelationService.getUnreadEnterGroupMessageNumber(request.getHeader("token"));
        return new Result(Code.SELECT_OK.getCode(), messageNumber);
    }


    /**
     * 通过用户id得到参加的所有群信息
     * @param request
     * @return
     */
    @GetMapping
    public Result getGroupByUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        List<Map<String, Object>> groups = groupRelationService.getGroupByUserId(token);
        return new Result(Code.SELECT_OK.getCode(), groups);
    }

    @DeleteMapping("/{groupId}")
    public Result exitGroupChat(@PathVariable Long groupId, HttpServletRequest request) {
        Integer result = groupRelationService.exitGroupChat(groupId, request.getHeader("token"));
        if (result != null) {
            return new Result(Code.DELETE_OK.getCode(), "退出成功!");
        }
        return new Result(Code.DELETE_ERR.getCode(), "退出失败!");
    }
}
