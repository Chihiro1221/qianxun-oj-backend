package com.qianxun.qianxunojbackendfile.controller;

import com.qianxun.qianxunojbackendcommon.common.BaseResponse;
import com.qianxun.qianxunojbackendcommon.common.ResultUtils;
import com.qianxun.qianxunojbackendfile.utils.OssUtil;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/oss")
public class OssController {

    @Autowired
    private OssUtil ossUtil;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = ossUtil.uploadFile(file, "uploads/");
        return ResultUtils.success(url);
    }
}
