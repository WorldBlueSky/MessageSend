package com.study.controller;

import com.study.service.SendSmsImpl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class SmsApiController {

    @Autowired
    public SendSmsImpl sendSms;

    @Autowired
    public RedisTemplate<String,Object> redisTemplate;

    public Random random = new Random();

    @RequestMapping("/send")
    public String sendCode(@RequestParam(value = "phone") String phone) {
        System.out.println(phone);
        //调用发送的方法即可

        //1、连接Redis，查找手机验证码是否存在
        String code = (String)redisTemplate.opsForValue().get(phone);

        System.out.println(code);

        //====================================================
        // 1、1如果存在的话，说明在5分钟内已经发送过验证码了，不能再发了
        if (!StringUtils.isEmpty(code)) {
            System.out.println("已存在，还没有过期，不能再次发送");
            return phone+":"+code+" 已存在，还没有过期";
        }
        //=====================================================

        //1。2 如果不存在的话,那么redis创建键值对生成验证码并存储，设置过期时间
        String newCode = "";
        // 生成6位随机验证码
        for (int i = 0; i < 6; i++) {
            newCode += random.nextInt(10);
        }
        // 将6位随机验证码对手机号进行发送
        boolean idSend = sendSms.send(phone,"xxxxx",newCode);

        //=====================================================

        // 因为有短信轰炸的情况，短信服务对每次发送限制次数，所以有发送不成功的情况，要考虑

        if(idSend){//如果发送成功将验证码存储到redis中
            redisTemplate.opsForValue().set(phone, newCode, 5, TimeUnit.MINUTES);
            System.out.println("发送成功!");
            return phone+":"+newCode+" 发送成功!";
        }else{
            System.out.println("发送失败!");
            return "发送失败!";
        }

    }
}
