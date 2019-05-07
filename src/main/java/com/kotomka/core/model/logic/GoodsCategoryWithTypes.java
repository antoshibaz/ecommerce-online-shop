package com.kotomka.core.model.logic;

import java.util.List;

public class GoodsCategoryWithTypes {

    private GoodsCategory goodsCategory;
    private List<GoodsType> goodsTypes;

    public GoodsCategory getGoodsCategory() {
        return goodsCategory;
    }

    public void setGoodsCategory(GoodsCategory goodsCategory) {
        this.goodsCategory = goodsCategory;
    }

    public List<GoodsType> getGoodsTypes() {
        return goodsTypes;
    }

    public void setGoodsTypes(List<GoodsType> goodsTypes) {
        this.goodsTypes = goodsTypes;
    }
}