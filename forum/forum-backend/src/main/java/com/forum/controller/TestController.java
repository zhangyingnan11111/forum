package com.forum.controller;

import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }

    @GetMapping("/need-auth")
    @LoginRequired
    public Result<String> needAuth() {
        return Result.success("认证通过");
    }
}