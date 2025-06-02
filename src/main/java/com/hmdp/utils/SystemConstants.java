package com.hmdp.utils;

import java.io.File;

public class SystemConstants {
    // 检查当前环境并设置合适的上传路径
    public static final String IMAGE_UPLOAD_DIR = initImageUploadDir();
    public static final String USER_NICK_NAME_PREFIX = "user_";
    public static final int DEFAULT_PAGE_SIZE = 5;

    //热门博客最大数量
    public static final int MAX_PAGE_SIZE = 10;
    
    // 商店评论状态常量
    public static final int COMMENT_STATUS_NORMAL = 0;         // 正常状态
    public static final int COMMENT_STATUS_HIDDEN_BY_USER = 1;  // 用户隐藏
    public static final int COMMENT_STATUS_HIDDEN_BY_ADMIN = 2; // 管理员隐藏
    
    // 评论举报状态常量
    public static final int REPORT_STATUS_PENDING = 0;  // 待处理
    public static final int REPORT_STATUS_RESOLVED = 1; // 已处理
    
    // 评论分页和排序常量
    public static final int COMMENT_PAGE_SIZE = 5;      // 评论默认分页大小
    public static final String COMMENT_SORT_BY_TIME = "time";     // 按时间排序
    public static final String COMMENT_SORT_BY_RATING = "rating"; // 按评分排序
    public static final String COMMENT_SORT_BY_HOT = "hotness";   // 按热度排序
    
    // 商店评论图片上传目录（相对于IMAGE_UPLOAD_DIR）
    public static final String SHOP_COMMENT_IMAGE_DIR = "shop-comments/";
    
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
