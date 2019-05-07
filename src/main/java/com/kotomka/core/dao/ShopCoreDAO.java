package com.kotomka.core.dao;

import com.kotomka.core.model.logic.*;
import com.kotomka.core.model.server.ResponseMsg;

import java.util.List;

public interface ShopCoreDAO {

    void setSuperuserScope(final boolean flag);

    Product getProductById(final Long productId);

    List<ProductComment> getGoodsCommentsById(final Long productId);

    List<GoodsCategory> getGoodsCategories();

    List<GoodsType> getGoodsTypes(final Long categoryId);

    List<GoodsCategoryWithTypes> getGoodsCategoriesWithTypes();

    List<Product> getPreviewGoodsList(final Long typeId, final Long categoryId);

    ResponseMsg addNewOrder(final Order order);

    ResponseMsg addNewGoodsComment(final ProductComment productComment);

    ResponseMsg addSubscriber(final String email);

    List<Product> searchGoods(final String search);
}