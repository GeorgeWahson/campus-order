package com.watson.order.controller;

import com.watson.order.dto.Result;
import com.watson.order.utils.AliOSSUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
public class CommonController {

    private final AliOSSUtils aliOSSUtils;

    /**
     * 将前端上传的图片上传到阿里云oss
     * @param file 图片文件
     * @return 包含图片网址的 封装结果对象
     * @throws IOException 文件io异常
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {

        log.info("upload original file name is: \"{}\"", file.getOriginalFilename());
        String url = aliOSSUtils.upload(file);
        return Result.success(url);
    }

    /**
     * 前端取消保存或覆盖已有图片时，删除原先已上传的图片
     *
     * @param url 被删除图片的网址
     * @throws Exception 文件删除异常
     */
    @DeleteMapping
    public void delete(String url) throws Exception {

        aliOSSUtils.delete(url);
    }

}
