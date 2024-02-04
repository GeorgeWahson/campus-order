package com.watson.order.controller;

import com.watson.order.dto.Result;
import com.watson.order.exception.CustomException;
import com.watson.order.utils.AliOSSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
@Api(tags = "图片相关接口")
public class CommonController {

    private final AliOSSUtils aliOSSUtils;

    /**
     * 将前端上传的图片上传到阿里云oss
     *
     * @param file 图片文件
     * @return 包含图片网址的 封装结果对象
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片接口")
    public Result<String> upload(MultipartFile file) {

        log.info("upload original file name is: \"{}\"", file.getOriginalFilename());
        String url;
        try {
            url = aliOSSUtils.upload(file);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException("文件上传失败！");
        }
        return Result.success(url);
    }

    /**
     * 前端取消保存或覆盖已有图片时，删除原先已上传的图片
     *
     * @param url 被删除图片的网址
     */
    @DeleteMapping
    @ApiOperation(value = "删除图片接口")
    public void delete(String url) {

        try {
            aliOSSUtils.delete(url);
        } catch (Exception e) {
            log.info("error occurred when delete url: {}", url);
            log.error(e.getMessage());
            throw new CustomException("文件删除失败！");
        }
    }

}
