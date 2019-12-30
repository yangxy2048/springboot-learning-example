package org.spring.springboot.controller;

import org.spring.springboot.aop.NoRepeatSubmit;
import org.spring.springboot.vo.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author DevinYang
 * @since 2019-12-11
 */

@RestController
public class SubmitController {

    @PostMapping("submit")
    @NoRepeatSubmit(lockTime = 30)
    public Object submit(@RequestBody UserBean userBean) {
        try {
            // 模拟业务场景
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new ApiResult(200, "成功", userBean.userId);
    }

    public static class UserBean {
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId == null ? null : userId.trim();
        }
    }
}
