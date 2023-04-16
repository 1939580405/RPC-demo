package com.rrtv.rpc.core.common;


public class ServiceUtil {

    /**
     *
     * @param serviceName
     * @param version
     * @return
     */
    public static String serviceKey(String serviceName, String version) {
        return String.join("-", serviceName, version);
    }

}
