package com.campus.core.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量查询工具类
 * 用于优化批量查询性能，减少数据库查询次数
 */
public class BatchQueryUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private BatchQueryUtils() {
    }

    /**
     * 批量查询并转换为Map（接受Set）
     */
    public static <T, R> Map<R, T> batchQueryToMap(
            Set<R> ids,
            Function<List<R>, List<T>> queryFunc,
            Function<T, R> idFunc) {
        return batchQueryToMap(new ArrayList<>(ids), queryFunc, idFunc);
    }

    /**
     * 批量查询并转换为Map
     *
     * @param ids ID列表
     * @param queryFunc 查询函数，接收ID列表返回结果列表
     * @param idFunc ID提取函数，从结果中提取ID
     * @param <T> 结果类型
     * @param <R> ID类型
     * @return ID到结果的Map
     */
    public static <T, R> Map<R, T> batchQueryToMap(
            List<R> ids,
            Function<List<R>, List<T>> queryFunc,
            Function<T, R> idFunc) {

        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        // 去重
        List<R> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

        // 批量查询
        List<T> results = queryFunc.apply(uniqueIds);

        // 转换为Map
        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }

        return results.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        idFunc,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 批量查询并分组
     *
     * @param ids ID列表
     * @param queryFunc 查询函数
     * @param keyFunc 分组键提取函数
     * @param <T> 结果类型
     * @param <K> 键类型
     * @return 键到结果列表的Map
     */
    public static <T, R, K> Map<K, List<T>> batchQueryToGroupedMap(
            List<R> ids,
            Function<List<R>, List<T>> queryFunc,
            Function<T, K> keyFunc) {

        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        // 去重
        List<R> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

        // 批量查询
        List<T> results = queryFunc.apply(uniqueIds);

        // 分组
        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }

        return results.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(keyFunc));
    }

    /**
     * 分批处理大量数据
     *
     * @param allIds 所有ID
     * @param batchSize 每批大小
     * @param processor 处理器函数
     * @param <T> 结果类型
     * @param <R> ID类型
     * @return 所有结果
     */
    public static <T, R> List<T> batchProcess(
            List<R> allIds,
            int batchSize,
            Function<List<R>, List<T>> processor) {

        if (allIds == null || allIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> allResults = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < allIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allIds.size());
            List<R> batch = allIds.subList(i, end);

            try {
                List<T> batchResults = processor.apply(batch);
                if (batchResults != null) {
                    allResults.addAll(batchResults);
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他批次
                System.err.println("Batch processing failed for batch starting at " + i + ": " + e.getMessage());
            }
        }

        return allResults;
    }

    /**
     * 批量查询并转换为列表
     *
     * @param ids ID列表
     * @param queryFunc 查询函数
     * @param <T> 结果类型
     * @param <R> ID类型
     * @return 结果列表
     */
    public static <T, R> List<T> batchQueryToList(
            List<R> ids,
            Function<List<R>, List<T>> queryFunc) {

        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // 去重并保持顺序
        List<R> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

        // 批量查询
        List<T> results = queryFunc.apply(uniqueIds);

        if (results == null) {
            return new ArrayList<>();
        }

        return results;
    }
}
