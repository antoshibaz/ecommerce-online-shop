package com.kotomka.core.dao;

import com.kotomka.core.exceptions.AuthException;
import com.kotomka.core.model.logic.*;
import com.kotomka.core.model.server.ResponseMsg;
import com.kotomka.core.model.server.UserModel;

import java.util.List;

public interface AdminCoreDAO {

    UserModel checkSystemUser(final String login, final String password) throws AuthException;

    List<Product> getAllGoods();

    ResponseMsg addProduct(final Product product);

    ResponseMsg updateProduct(final Product product);

    ResponseMsg removeProduct(final Long productId);

    ResponseMsg attachProduct(final Product product, final Long typeId, final Long categoryId);

    ResponseMsg detachProduct(final Product product, final Long typeId, final Long categoryId);

    ResponseMsg addCategory(final GoodsCategory goodsCategory);

    ResponseMsg updateCategory(final GoodsCategory goodsCategory);

    ResponseMsg removeCategory(final Long categoryId);

    ResponseMsg addType(final GoodsType goodsType);

    ResponseMsg updateType(final GoodsType goodsType);

    ResponseMsg removeType(final Long categoryId);

    ResponseMsg mapCategoryAndType(final Long categoryId, final Long typeId);

    ResponseMsg unmapCategoryAndType(final Long categoryId, final Long typeId);

    ResponseMsg updateComment(final Long commentId, final Boolean hideComment);

    ResponseMsg removeComment(final Long commentId);

    List<Order> getOrdersPreview(final Integer status);

    Order getOrderById(final Long orderId);

    ResponseMsg updateOrderStatus(final Long orderId, final Integer status);

    ResponseMsg updateOrderInfo(final Order order);

    List<Product> getOrderProductsPreview(final Long orderId);
}