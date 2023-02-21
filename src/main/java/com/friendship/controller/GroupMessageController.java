package com.friendship.controller;

import com.friendship.pojo.Code;
import com.friendship.pojo.Result;
import com.friendship.service.impl.GroupMessageService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groupMessages")
@CrossOrigin(originPatterns = {"*"}, allowCredentials = "true")
public class GroupMessageController {
    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * 根据群id得到群的所有信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getGroupMessageByGroupId(@PathVariable Long id, HttpServletRequest request) {
        List<Map<String, Object>> groupMessages = groupMessageService.getGroupMessageByGroupId(id, request);
        if (groupMessages == null) {
            return new Result(Code.ILLEGAL_REQUEST.getCode(), "非法请求");
        }
        return new Result(Code.SELECT_OK.getCode(), groupMessages);
    }
}
