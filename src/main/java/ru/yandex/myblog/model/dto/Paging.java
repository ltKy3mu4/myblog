package ru.yandex.myblog.model.dto;

public record Paging (
    int pageNumber,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious){};
