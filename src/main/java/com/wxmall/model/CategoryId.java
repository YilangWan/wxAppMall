package com.wxmall.model;

import com.wxmall.common.api.IErrorCode;

public enum CategoryId {
    ELECTRONICS("00001", "电子产品"),
    PETFOOD("00002", "宠物食品"),
    FOOD("00003", "食品"),
    FURNITURE("00004", "家具"),
    DRINKS("00005", "饮品");

    private String code;
    private String message;

    private CategoryId(String code, String message) {
        this.code = code;
        this.message = message;
    }


}
