package com.vrd.common.storage;

import org.springframework.util.StringUtils;

import java.io.File;

public final class StorageKeyUtils {

    private StorageKeyUtils() {
    }

    /**
     * 从存储 key、历史本地路径或访问地址中解析对象 key。
     */
    public static String resolveObjectKey(String storageKey, String filePath, String storageAddress,
                                          StorageService storageService) {
        if (StringUtils.hasText(storageKey)) {
            return normalizeKey(storageKey);
        }
        if (StringUtils.hasText(filePath)) {
            String normalizedPath = filePath.replace('\\', '/');
            if (isLegacyLocalPath(normalizedPath)) {
                return null;
            }
            if (!normalizedPath.contains("://")) {
                return normalizeKey(normalizedPath);
            }
        }
        if (StringUtils.hasText(storageAddress) && storageService != null) {
            String baseUrl = trimTrailingSlash(storageService.getUrl(""));
            if (storageAddress.startsWith(baseUrl)) {
                String key = storageAddress.substring(baseUrl.length());
                if (key.startsWith("/")) {
                    key = key.substring(1);
                }
                return normalizeKey(key);
            }
        }
        return StringUtils.hasText(filePath) ? normalizeKey(filePath.replace('\\', '/')) : null;
    }

    public static boolean isLegacyLocalPath(String path) {
        if (!StringUtils.hasText(path) || path.contains("://")) {
            return false;
        }
        return path.startsWith("/") || (path.length() > 2 && path.charAt(1) == ':');
    }

    public static File resolveLegacyLocalFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        File file = new File(filePath);
        return file.exists() ? file : null;
    }

    private static String normalizeKey(String key) {
        String normalized = key.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
