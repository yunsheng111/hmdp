package com.hmdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.common.Result;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            log.info("开始上传文件，原始文件名：{}", originalFilename);
            
            // 检查文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("文件类型不允许：{}", contentType);
                return Result.fail("只允许上传图片文件");
            }
            
            // 检查文件大小
            long fileSize = image.getSize();
            if (fileSize > 10 * 1024 * 1024) { // 10MB限制
                log.warn("文件大小超过限制：{} bytes", fileSize);
                return Result.fail("图片大小不能超过10MB");
            }
            
            // 检查上传目录是否存在
            File uploadDir = new File(SystemConstants.IMAGE_UPLOAD_DIR);
            log.info("上传目录路径: {}, 是否存在: {}, 是否可写: {}", 
                    uploadDir.getAbsolutePath(), 
                    uploadDir.exists(), 
                    uploadDir.canWrite());
            
            if (!uploadDir.exists()) {
                log.warn("上传目录不存在，尝试创建：{}", SystemConstants.IMAGE_UPLOAD_DIR);
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    log.error("创建上传目录失败：{}", SystemConstants.IMAGE_UPLOAD_DIR);
                    return Result.fail("服务器存储目录创建失败");
                }
            }
            
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            File targetFile = new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName);
            
            // 检查目标文件目录是否存在
            File parentDir = targetFile.getParentFile();
            log.info("目标文件目录: {}, 是否存在: {}", 
                    parentDir.getAbsolutePath(), 
                    parentDir.exists());
            
            if (!parentDir.exists()) {
                log.info("目标文件目录不存在，尝试创建：{}", parentDir.getAbsolutePath());
                boolean created = parentDir.mkdirs();
                if (!created) {
                    log.error("创建目标文件目录失败：{}", parentDir.getAbsolutePath());
                    return Result.fail("服务器存储子目录创建失败");
                }
            }
            
            // 保存文件
            log.info("开始保存文件到：{}", targetFile.getAbsolutePath());
            image.transferTo(targetFile);
            
            // 验证文件是否成功保存
            if (!targetFile.exists()) {
                log.error("文件保存失败，文件不存在：{}", targetFile.getAbsolutePath());
                return Result.fail("文件保存失败，请检查服务器存储权限");
            }
            
            // 返回结果
            log.info("文件上传成功，路径：{}", fileName);
            return Result.success(fileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误", e);
            return Result.fail("文件上传过程中发生错误：" + e.getMessage());
        }
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.success();
    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
