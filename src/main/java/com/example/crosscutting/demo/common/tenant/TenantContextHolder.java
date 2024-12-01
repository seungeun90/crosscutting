package com.example.crosscutting.demo.common.tenant;

public class TenantContextHolder {
    private static ThreadLocal<String> contextHolder = new ThreadLocal<>();
    /**
     * Retrieve the value of the {@link ThreadLocal} variable
     */
    public static String getTenant() {
        return contextHolder.get();
    }

    /**
     * Set the tenant into the {@link ThreadLocal} variable
     */
    public static void setTenant(String tenant) {
        contextHolder.set(tenant);
    }

    public static void clear() {
        contextHolder.remove();
    }
}
