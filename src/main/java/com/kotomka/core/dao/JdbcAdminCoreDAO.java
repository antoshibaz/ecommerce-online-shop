package com.kotomka.core.dao;

import com.kotomka.core.exceptions.AuthException;
import com.kotomka.core.model.logic.*;
import com.kotomka.core.model.server.ResponseMsg;
import com.kotomka.core.model.server.UserModel;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcAdminCoreDAO implements AdminCoreDAO {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate extJdbcTemplate;
    private TransactionTemplate transactionTemplate;

    public JdbcAdminCoreDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.extJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @Override
    public UserModel checkSystemUser(final String login, final String password) throws AuthException {
        try {
            return jdbcTemplate.queryForObject("select * from accounts where login=? and passwd=? limit 1",
                    (resultSet, i) -> {
                        UserModel userModel = new UserModel();
                        userModel.setLogin(resultSet.getString("login"));
                        return userModel;
                    }, login, password);
        } catch (Exception e) {
            throw new AuthException("Auth failed: incorrect system user login or password");
        }
    }

    @Override
    public List<Product> getAllGoods() {
        return jdbcTemplate.query("select * from goods", (resultSet, i) -> {
            Product product = new Product();
            product.setId(resultSet.getLong("id"));
            product.setNaming(resultSet.getString("naming"));
            product.setDescriptionPreview(resultSet.getString("descr_prev"));
            product.setDescriptionExtended(resultSet.getString("descr_ext"));
            product.setPrice(resultSet.getDouble("price"));
            product.setCount(resultSet.getLong("count"));
            product.setHidden(resultSet.getBoolean("is_hidden"));
            return product;
        });
    }

    @Override
    public ResponseMsg addProduct(Product product) {
        if (product != null) {
            try {
                String q = "insert into goods(naming, descr_prev, descr_ext, price, \"count\", is_hidden) " +
                        "values (?,?,?,?,?,default)";
                KeyHolder keyHolder = new GeneratedKeyHolder();
                int rows = jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(q, new String[]{"id"});
                    ps.setString(1, product.getNaming());
                    ps.setString(2, product.getDescriptionPreview());
                    ps.setString(3, product.getDescriptionExtended());
                    ps.setDouble(4, product.getPrice());
                    ps.setLong(5, product.getCount());
                    return ps;
                }, keyHolder);

                if (rows > 0) {
                    long productId = keyHolder.getKey().longValue();
                    return new ResponseMsg("success", productId);
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "product is not added");
            }

        }
        return new ResponseMsg("error", "product is not added");
    }

    @Override
    public ResponseMsg updateProduct(Product product) {
        if (product != null && product.getId() != null) {
            try {
                int c = jdbcTemplate.update("update goods set naming=?, descr_prev=?, descr_ext=?, price=?, \"count\"=? where id=?",
                        product.getNaming(), product.getDescriptionPreview(), product.getDescriptionExtended(),
                        product.getPrice(), product.getCount(), product.getId());
                if (c > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "product is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "product is not updated");
            }
        }
        return new ResponseMsg("error", "product is not updated");
    }

    @Override
    public ResponseMsg removeProduct(Long productId) {
        if (productId != null) {
            try {
                int c = jdbcTemplate.update("delete from goods where id=?", productId);
                if (c > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "product is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "product is not deleted");
            }
        }
        return new ResponseMsg("error", "product is not deleted");
    }

    @Override
    public ResponseMsg attachProduct(Product product, Long typeId, Long categoryId) {
        if (product != null && product.getId() != null) {
            try {
                Long catTypeMapId = jdbcTemplate.queryForObject("select id from map_categories_types where goods_type=? and goods_category=?",
                        Long.class, typeId, categoryId);
                Long f = jdbcTemplate.queryForObject("select count(id) from goods_list where good=? and goods_cat_type=?",
                        Long.class, product.getId(), catTypeMapId);
                int c;
                if (f != null && f == 0) {
                    // создаем новую привязку продукта к категории-типу
                    c = jdbcTemplate.update("insert into goods_list(good, goods_cat_type) values (?,?)", product.getId(), catTypeMapId);
                } else {
                    // обновляем уже существующую привязку продукта к категории-типу
                    c = jdbcTemplate.update("update goods_list set goods_cat_type=? where good=?", catTypeMapId, product.getId());
                }
                if (c > 0) {
                    return new ResponseMsg("success");
                }
            } catch (EmptyResultDataAccessException emptyEx) {
                return new ResponseMsg("not_exists", "category or type is not exists");
            } catch (Exception e) {
                return new ResponseMsg("error", "product is not attached");
            }
        }
        return new ResponseMsg("error", "product is not attached");
    }

    @Override
    public ResponseMsg detachProduct(Product product, Long typeId, Long categoryId) {
        if (product != null && product.getId() != null) {
            try {
                Long catTypeMapId = jdbcTemplate.queryForObject("select id from map_categories_types where goods_type=? and goods_category=?",
                        Long.class, typeId, categoryId);
                int c = jdbcTemplate.update("delete from goods_list where good=? and goods_cat_type=?", product.getId(), catTypeMapId);
                if (c > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "product is not exists");
                }
            } catch (EmptyResultDataAccessException emptyEx) {
                return new ResponseMsg("not_exists", "category or type is not exists");
            } catch (Exception e) {
                return new ResponseMsg("error", "product is not detached");
            }
        }
        return new ResponseMsg("error", "product is not detached");
    }

    @Override
    public ResponseMsg addCategory(GoodsCategory goodsCategory) {
        if (goodsCategory != null) {
            long c = jdbcTemplate.queryForObject("select count(id) from goods_category where naming=?", Long.class, goodsCategory.getNaming());
            if (c > 0) {
                return new ResponseMsg("already_exists");
            } else {
                try {
                    int rows = jdbcTemplate.update("insert into goods_category(naming) values (?)", goodsCategory.getNaming());
                    if (rows > 0) {
                        return new ResponseMsg("success");
                    }
                } catch (Exception e) {
                    return new ResponseMsg("error", "category is not added");
                }
            }
        }
        return new ResponseMsg("error", "category is not added");
    }

    @Override
    public ResponseMsg updateCategory(GoodsCategory goodsCategory) {
        if (goodsCategory != null && goodsCategory.getId() != null) {
            try {
                int rows = jdbcTemplate.update("update goods_category set naming=? where id=?", goodsCategory.getNaming(), goodsCategory.getId());
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "category is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "category is not updated");
            }
        }
        return new ResponseMsg("error", "category is not updated");
    }

    @Override
    public ResponseMsg removeCategory(Long categoryId) {
        if (categoryId != null) {
            try {
                int rows = jdbcTemplate.update("delete from goods_category where id=?", categoryId);
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "category is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "category is not deleted");
            }
        }
        return new ResponseMsg("error", "category is not deleted");
    }

    @Override
    public ResponseMsg addType(GoodsType goodsType) {
        if (goodsType != null) {
            long c = jdbcTemplate.queryForObject("select count(id) from goods_type where naming=?", Long.class, goodsType.getNaming());
            if (c > 0) {
                return new ResponseMsg("already_exists");
            } else {
                try {
                    int rows = jdbcTemplate.update("insert into goods_type(naming) values (?)", goodsType.getNaming());
                    if (rows > 0) {
                        return new ResponseMsg("success");
                    }
                } catch (Exception e) {
                    return new ResponseMsg("error", "type is not added");
                }
            }
        }
        return new ResponseMsg("error", "type is not added");
    }

    @Override
    public ResponseMsg updateType(GoodsType goodsType) {
        if (goodsType != null && goodsType.getId() != null) {
            try {
                int rows = jdbcTemplate.update("update goods_type set naming=? where id=?", goodsType.getNaming(), goodsType.getId());
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "type is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "type is not updated");
            }
        }
        return new ResponseMsg("error", "type is not updated");
    }

    @Override
    public ResponseMsg removeType(Long typeId) {
        if (typeId != null) {
            try {
                int rows = jdbcTemplate.update("delete from goods_type where id=?", typeId);
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "type is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "type is not deleted");
            }
        }
        return new ResponseMsg("error", "type is not deleted");
    }

    @Override
    public ResponseMsg mapCategoryAndType(final Long categoryId, final Long typeId) {
        if (categoryId != null && typeId != null) {
            long c = jdbcTemplate.queryForObject("select count(id) from map_categories_types where goods_category=? " +
                    "and goods_type=?", Long.class, categoryId, typeId);
            if (c > 0) {
                return new ResponseMsg("already_exists");
            } else {
                try {
                    int rows = jdbcTemplate.update("insert into map_categories_types(goods_type, goods_category) " +
                            "values (?,?)", typeId, categoryId);
                    if (rows > 0) {
                        return new ResponseMsg("success");
                    }
                } catch (Exception e) {
                    return new ResponseMsg("error", "category and type is not mapped");
                }
            }
        }
        return new ResponseMsg("error", "category and type is not mapped");
    }

    @Override
    public ResponseMsg unmapCategoryAndType(final Long categoryId, final Long typeId) {
        if (categoryId != null && typeId != null) {
            try {
                Long mapId = jdbcTemplate.queryForObject("select id from map_categories_types where goods_category=? " +
                        "and goods_type=?", Long.class, categoryId, typeId);
                int rows = jdbcTemplate.update("delete from map_categories_types where id=? ", mapId);
                if (rows > 0) {
                    return new ResponseMsg("success");
                }
            } catch (EmptyResultDataAccessException emptyEx) {
                return new ResponseMsg("not_exists", "category-type mapping relation is not exists");
            } catch (Exception e) {
                return new ResponseMsg("error", "category and type is not mapped");
            }
        }
        return new ResponseMsg("error", "category and type is not mapped");
    }

    @Override
    public ResponseMsg updateComment(Long commentId, Boolean hideComment) {
        if (hideComment != null && commentId != null) {
            try {
                Boolean isHidden = jdbcTemplate.queryForObject("select is_hidden from goods_comments where id=?", Boolean.class, commentId);
                if (isHidden && hideComment) {
                    return new ResponseMsg("already_hidden");
                } else if (!isHidden && !hideComment) {
                    return new ResponseMsg("already_showed");
                } else {
                    int rows = jdbcTemplate.update("update goods_comments(is_hidden) values (?) where id=?", hideComment, commentId);
                    if (rows > 0) {
                        return new ResponseMsg("success");
                    }
                }
            } catch (EmptyResultDataAccessException emptyEx) {
                return new ResponseMsg("not_exists", "product comment is not exists");
            } catch (Exception e) {
                return new ResponseMsg("error", "product comment is not updated");
            }
        }
        return new ResponseMsg("error", "product comment is not updated");
    }

    @Override
    public ResponseMsg removeComment(Long commentId) {
        if (commentId != null) {
            try {
                int rows = jdbcTemplate.update("delete from goods_comments where id=?", commentId);
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "product comment is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "product comment is not deleted");
            }
        }
        return new ResponseMsg("error", "product comment is not deleted");
    }

    @Override
    public List<Order> getOrdersPreview(Integer status) {
        String q = "select * from orders";
        if (status != null) {
            q = q.concat(" where status=?");
            return jdbcTemplate.query(q, getOrderPreviewRowMapper(), status);
        } else {
            return jdbcTemplate.query(q, getOrderPreviewRowMapper());
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        if (orderId != null) {
            try {
                return jdbcTemplate.queryForObject("select * from orders where id=?", getOrderRowMapper(), orderId);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public ResponseMsg updateOrderStatus(final Long orderId, final Integer status) {
        if (orderId != null && status != null) {
            try {
                int rows = jdbcTemplate.update("update orders set status=? where id=?", status, orderId);
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "order is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "order is not updated");
            }
        }
        return new ResponseMsg("error", "order is not updated");
    }

    @Override
    public ResponseMsg updateOrderInfo(Order order) {
        if (order != null && order.getId() != null) {
            try {
                int rows = jdbcTemplate.update("update orders set " +
                        "first_name=?, last_name=?, email=?, phone_number=?, delivery_address=?, user_comment=?, delivery_type=?, total_price=? " +
                        "where id=?", order.getFirstName(), order.getLastName(), order.getEmail(), order.getPhoneNumber(), order.getDeliveryAddress(),
                        order.getComment(), order.getDeliveryType(), order.getTotalPrice(), order.getId());
                if (rows > 0) {
                    return new ResponseMsg("success");
                } else {
                    return new ResponseMsg("not_exists", "order is not exists");
                }
            } catch (Exception e) {
                return new ResponseMsg("error", "order is not updated");
            }
        }
        return new ResponseMsg("error", "order is not updated");
    }

    @Override
    public List<Product> getOrderProductsPreview(Long orderId) {
        if (orderId != null) {
            return jdbcTemplate.query("select opl.id, opl.order_id, opl.product_id, g.naming, opl.count from orders_products_list opl " +
                    "left join goods g on opl.product_id=g.id " +
                    "where order_id=?", (resultSet, i) -> {
                Product product = new Product();
                product.setId(resultSet.getLong("product_id"));
                product.setNaming(resultSet.getString("naming"));
                product.setCount(resultSet.getLong("count"));
                return product;
            }, orderId);
        } else {
            return null;
        }
    }

    private RowMapper<Order> getOrderPreviewRowMapper() {
        return (resultSet, i) -> {
            Order order = new Order();
            order.setId(resultSet.getLong("id"));
            order.setFirstName(resultSet.getString("first_name"));
            order.setLastName(resultSet.getString("last_name"));
            order.setDeliveryType(resultSet.getInt("delivery_type"));
            order.setDatetime(resultSet.getString("datepoint"));
            order.setStatus(resultSet.getInt("status"));
            order.setTotalPrice(resultSet.getLong("total_price"));
            return order;
        };
    }

    private RowMapper<Order> getOrderRowMapper() {
        return (resultSet, i) -> {
            Order order = new Order();
            order.setId(resultSet.getLong("id"));
            order.setFirstName(resultSet.getString("first_name"));
            order.setLastName(resultSet.getString("last_name"));
            order.setEmail(resultSet.getString("email"));
            order.setPhoneNumber(resultSet.getString("phone_number"));
            order.setDeliveryType(resultSet.getInt("delivery_type"));
            order.setDeliveryAddress(resultSet.getString("delivery_address"));
            order.setComment(resultSet.getString("user_comment"));
            order.setDatetime(resultSet.getString("datepoint"));
            order.setStatus(resultSet.getInt("status"));
            order.setTotalPrice(resultSet.getLong("total_price"));
            return order;
        };
    }
}