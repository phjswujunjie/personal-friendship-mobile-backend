package com.friendship.controller;

import com.friendship.accessControl.LoginRequired;
import com.friendship.pojo.Code;
import com.friendship.pojo.UserGroup;
import com.friendship.pojo.Result;
import com.friendship.service.impl.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class GroupController {
    @Resource
    private GroupService groupService;

    @LoginRequired
    @PostMapping
    public Result createGroup(MultipartFile avatar, String groupName, HttpServletRequest request) throws Exception{
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName(groupName);
        String token = request.getHeader("token");
        Long id = groupService.createGroup(userGroup, token, avatar);
        return new Result(Code.OK.getCode(), id);
    }

    /**
     * 根据群的id得到群设置的相关信息
     * @param groupId: 群id
     * @param request
     * @return
     */
    @LoginRequired
    @GetMapping("/setting/{groupId}")
    public Result getGroupSettingInfoById(@PathVariable Long groupId, HttpServletRequest request) {
        List<Map<String, Object>> result = groupService.getGroupSettingInfoById(groupId, request.getHeader("token"));
        if (result == null) {
            return new Result(Code.FORBIDDEN.getCode(), "非法请求");
        }
        return new Result(Code.OK.getCode(), result);
    }

    /**
     * 根据群的id得到群主页的相关信息
     * @param groupId: 群id
     * @param request
     * @return
     */
    @GetMapping("/homepage/{groupId}")
    public Result getGroupHomepageInfoById(@PathVariable Long groupId, HttpServletRequest request) {
        List<Map<String, Object>> result = groupService.getGroupHomepageInfoById(groupId, request.getHeader("token"));
        return new Result(Code.OK.getCode(), result);
    }
}
