package com.demo.integration.core.ip;

import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:06
 */
public class IpCheckUtil {

    public static boolean check(
            String clientIp,
            List<String> whiteList) {

        if (whiteList == null || whiteList.isEmpty()) {

            return true;
        }

        return whiteList.contains(clientIp);
    }
}