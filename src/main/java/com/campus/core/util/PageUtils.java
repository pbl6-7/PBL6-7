package com.campus.core.util;

import lombok.Data;

/**
 * 分页工具类
 * 提供分页参数的验证和规范化功能
 */
public class PageUtils {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    private static final int DEFAULT_SIZE = 20;

    /**
     * 最大每页大小
     */
    private static final int MAX_SIZE = 100;

    /**
     * 分页参数
     */
    @Data
    public static class PageParams {
        private int page;
        private int size;
        private int offset;

        public PageParams(int page, int size) {
            this.page = page;
            this.size = size;
            this.offset = (page - 1) * size;
        }
    }

    /**
     * 验证并规范化分页参数
     *
     * @param page 页码
     * @param size 每页大小
     * @return 规范化后的分页参数
     */
    public static PageParams validateAndNormalize(Integer page, Integer size) {
        int normalizedPage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int normalizedSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new PageParams(normalizedPage, normalizedSize);
    }

    /**
     * 验证并规范化分页参数（4参数版本）
     *
     * @param page 页码
     * @param size 每页大小
     * @param offset 偏移量（未使用，为兼容性保留）
     * @param limit 限制数（未使用，为兼容性保留）
     * @return 规范化后的分页参数
     */
    public static PageParams validateAndNormalize(Integer page, Integer size, int offset, int limit) {
        return validateAndNormalize(page, size);
    }

    /**
     * 计算偏移量
     *
     * @param page 页码
     * @param size 每页大小
     * @return 偏移量
     */
    public static int calculateOffset(int page, int size) {
        return (page - 1) * size;
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param size 每页大小
     * @return 总页数
     */
    public static int calculateTotalPages(long total, int size) {
        return (int) Math.ceil((double) total / size);
    }
}
