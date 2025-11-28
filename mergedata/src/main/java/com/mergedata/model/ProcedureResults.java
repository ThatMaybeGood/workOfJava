package com.mergedata.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// ❗ 这是一个新的 DTO 类
@Data
@AllArgsConstructor
public class ProcedureResults<T> {
    private List<T> listResult;
    private Integer code;
    private String message;


}