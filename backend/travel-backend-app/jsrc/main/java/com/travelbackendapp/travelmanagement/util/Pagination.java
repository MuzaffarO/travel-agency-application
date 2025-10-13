package com.travelbackendapp.travelmanagement.util;

public final class Pagination {
    private Pagination() {
    }

    public static int totalPages(int totalItems, int pageSize) {
        return (int) Math.ceil(totalItems / (double) pageSize);
    }

    public static int[] range(int totalItems, int page, int pageSize) {
        int from = Math.min(Math.max(0, (page - 1) * pageSize), totalItems);
        int to = Math.min(from + pageSize, totalItems);
        return new int[]{from, to};
    }
}
