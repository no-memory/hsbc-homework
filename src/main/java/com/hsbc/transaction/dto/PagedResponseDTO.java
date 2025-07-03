package com.hsbc.transaction.dto;

import java.util.List;

public class PagedResponseDTO<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public PagedResponseDTO() {}
    
    public PagedResponseDTO(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
    
    // Getters
    public List<T> getContent() {
        return content;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    // Setters
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
} 