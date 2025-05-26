package com.hmdp.utils;

import java.io.File;

public class SystemConstants {
    // 检查当前环境并设置合适的上传路径
    public static final String IMAGE_UPLOAD_DIR = initImageUploadDir();
    public static final String USER_NICK_NAME_PREFIX = "user_";
    public static final int DEFAULT_PAGE_SIZE = 5;

    //热门博客最大数量
    public static final int MAX_PAGE_SIZE = 10;
    
    /**
     * 初始化图片上传目录
     * 如果配置的目录不存在或无权限，则使用临时目录
     */
    private static String initImageUploadDir() {
        // 默认路径 - 修改为当前工作目录下的Nginx路径
        String defaultPath = "D:\\workspace\\hmdp\\hmdp-front\\nginx-1.18.0\\html\\hmdp\\imgs\\";
        File dir = new File(defaultPath);
        
        // 检查目录是否存在且可写
        if (!dir.exists()) {
            // 尝试创建目录
            boolean created = dir.mkdirs();
            if (!created) {
                // 创建失败，使用临时目录
                String tempDir = System.getProperty("java.io.tmpdir") + "hmdp" + File.separator + "imgs" + File.separator;
                File tempFile = new File(tempDir);
                tempFile.mkdirs();
                System.out.println("使用临时目录存储图片: " + tempDir);
                return tempDir;
            }
        } else if (!dir.canWrite()) {
            // 目录存在但不可写，使用临时目录
            String tempDir = System.getProperty("java.io.tmpdir") + "hmdp" + File.separator + "imgs" + File.separator;
            File tempFile = new File(tempDir);
            tempFile.mkdirs();
            System.out.println("原目录不可写，使用临时目录存储图片: " + tempDir);
            return tempDir;
        }
        
        System.out.println("使用配置的图片存储目录: " + defaultPath);
        return defaultPath;
    }
}
