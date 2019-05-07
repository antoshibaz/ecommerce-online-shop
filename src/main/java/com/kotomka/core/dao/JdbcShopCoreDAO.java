package com.kotomka.core.dao;

import com.kotomka.core.model.logic.*;
import com.kotomka.core.model.server.ResponseMsg;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcShopCoreDAO implements ShopCoreDAO {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate extJdbcTemplate;
    private TransactionTemplate transactionTemplate;

    private Boolean isAdminScope = false;

    public JdbcShopCoreDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.extJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    public Boolean getAdminScope() {
        return isAdminScope;
    }

    public void setAdminScope(Boolean adminScope) {
        isAdminScope = adminScope;
    }

    @Override
    public void setSuperuserScope(boolean flag) {
        setAdminScope(flag);
    }

    @Override
    public Product getProductById(Long productId) {
        if (productId != null) {
            String query;
            if (!isAdminScope) {
                query = "select id, naming, descr_prev, descr_ext, price, count, is_hidden " +
                        "from goods where id in (select good from goods_list where good=?) and is_hidden=false";
            } else {
                query = "select * from goods where id=?";
            }
            try {
                return jdbcTemplate.queryForObject(query, getGoodsRowMapper(), productId);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private RowMapper<Product> getGoodsRowMapper() {
        return (resultSet, i) -> {
            Product product = new Product();
            product.setId(resultSet.getLong("id"));
            product.setNaming(resultSet.getString("naming"));
            product.setDescriptionPreview(resultSet.getString("descr_prev"));
            product.setDescriptionExtended(resultSet.getString("descr_ext"));
            product.setPrice(resultSet.getDouble("price"));
            product.setCount(resultSet.getLong("count"));
            if (isAdminScope) {
                product.setHidden(resultSet.getBoolean("is_hidden"));
            }
            return product;
        };
    }

    private RowMapper<Product> getGoodsPreviewRowMapper() {
        return (resultSet, i) -> {
            Product product = new Product();
            product.setId(resultSet.getLong("id"));
            product.setNaming(resultSet.getString("naming"));
            product.setPrice(resultSet.getDouble("price"));
            product.setDescriptionPreview(resultSet.getString("descr_prev"));
            product.setCount(resultSet.getLong("count"));
            if (isAdminScope) {
                product.setHidden(resultSet.getBoolean("is_hidden"));
            }
            return product;
        };
    }

    @Override
    public List<ProductComment> getGoodsCommentsById(Long productId) {
        if (productId != null) {
            String query;
            if (!isAdminScope) {
                query = "select * from goods_comments where good in (select id from goods where id=? and is_hidden=false)";
            } else {
                query = "select * from goods_comments where good=?";
            }
            return jdbcTemplate.query(query, (resultSet, i) -> {
                ProductComment comment = new ProductComment();
                comment.setId(resultSet.getLong("id"));
                comment.setProductId(resultSet.getLong("good"));
                comment.setUsername(resultSet.getString("username"));
                comment.setUserEmail(resultSet.getString("usermail"));
                comment.setComment(resultSet.getString("usercomment"));
                comment.setDatetime(resultSet.getString("datime"));
                if (isAdminScope) {
                    comment.setHidden(resultSet.getBoolean("is_hidden"));
                }
                return comment;
            }, productId);
        }
        return null;
    }

    @Override
    public List<GoodsCategory> getGoodsCategories() {
        String query;
        if (!isAdminScope) {
            query = "select * from goods_category where id in (select goods_category from map_categories_types)";
        } else {
            query = "select * from goods_category";
        }
        return jdbcTemplate.query(query, getGoodsCategoryRowMapper());
    }

    private RowMapper<GoodsCategory> getGoodsCategoryRowMapper() {
        return (resultSet, i) -> {
            GoodsCategory category = new GoodsCategory();
            category.setId(resultSet.getLong("id"));
            category.setNaming(resultSet.getString("naming"));
            return category;
        };
    }

    @Override
    public List<GoodsType> getGoodsTypes(final Long categoryId) {
        String query;
        if (categoryId == null) {
            if (isAdminScope) {
                query = "select * from goods_type";
            } else {
                query = "select * from goods_type where id in (select goods_type from map_categories_types)";
            }
            return jdbcTemplate.query(query, getGoodsTypeRowMapper());
        } else {
            query = "select gt.id, gt.naming from map_categories_types mct left join goods_type gt on mct.goods_type=gt.id " +
                    "where mct.goods_category=?";
            return jdbcTemplate.query(query, getGoodsTypeRowMapper(), categoryId);
        }
    }

    private RowMapper<GoodsType> getGoodsTypeRowMapper() {
        return (resultSet, i) -> {
            GoodsType type = new GoodsType();
            type.setId(resultSet.getLong("id"));
            type.setNaming(resultSet.getString("naming"));
            return type;
        };
    }

    @Override
    public List<GoodsCategoryWithTypes> getGoodsCategoriesWithTypes() {
        String query;
        if (isAdminScope) {
            query = "select * from goods_category";
        } else {
            query = "select * from goods_category where id in (select goods_category from map_categories_types)";
        }
        return jdbcTemplate.query(query, (resultSet, i) -> {
            GoodsCategoryWithTypes goodsCategoryWithTypes = new GoodsCategoryWithTypes();
            GoodsCategory goodsCategory = new GoodsCategory();
            goodsCategory.setId(resultSet.getLong("id"));
            goodsCategory.setNaming(resultSet.getString("naming"));
            goodsCategoryWithTypes.setGoodsCategory(goodsCategory);
            goodsCategoryWithTypes.setGoodsTypes(getGoodsTypes(goodsCategory.getId()));
            return goodsCategoryWithTypes;
        });
    }

    @Override
    public List<Product> getPreviewGoodsList(Long typeId, Long categoryId) {
        StringBuilder sbQuery = new StringBuilder();
        if (!isAdminScope) {
            sbQuery.append("select t1.good_id as id, t1.naming, t1.descr_prev, t1.descr_ext, t1.photo_path, t1.price, t1.count from ");
            sbQuery.append("(select gl.id, gl.good as good_id, gl.goods_cat_type, g.naming, g.descr_prev, g.descr_ext, g.photo_path, g.price, g.count ");
            sbQuery.append("from goods_list gl left join goods g on gl.good=g.id where g.is_hidden=false)");
            sbQuery.append(" t1 left join map_categories_types t2 on t1.goods_cat_type=t2.id");

            if (categoryId != null || typeId != null) {
                Map<String, Long> params = new HashMap<>();
                sbQuery.append(" where");
                boolean isBegin = true;
                if (categoryId != null) {
                    sbQuery.append(" t2.goods_category=:categoryId");
                    params.put("categoryId", categoryId);
                    isBegin = false;
                }
                if (typeId != null) {
                    if (!isBegin) {
                        sbQuery.append(" and");
                    }
                    sbQuery.append(" t2.goods_type=:typeId");
                    params.put("typeId", typeId);
                }
                return extJdbcTemplate.query(sbQuery.toString(), params, getGoodsPreviewRowMapper());
            } else {
                sbQuery.append(" group by (t1.good_id, t1.naming, t1.descr_prev, t1.descr_ext, t1.photo_path, t1.price, t1.count)");
                return jdbcTemplate.query(sbQuery.toString(), getGoodsPreviewRowMapper());
            }
        } else {
            sbQuery.append("select g.id, g.naming, g.descr_prev, g.descr_ext, g.photo_path, g.price, g.count, g.is_hidden" +
                    " from goods g left join goods_list gl on g.id=gl.good left join map_categories_types mct" +
                    " on mct.id=gl.goods_cat_type");
            if (categoryId != null || typeId != null) {
                Map<String, Long> params = new HashMap<>();
                sbQuery.append(" where");
                boolean isBegin = true;
                if (categoryId != null) {
                    sbQuery.append(" mct.goods_category=:categoryId");
                    params.put("categoryId", categoryId);
                    isBegin = false;
                }
                if (typeId != null) {
                    if (!isBegin) {
                        sbQuery.append(" and");
                    }
                    sbQuery.append(" mct.goods_type=:typeId");
                    params.put("typeId", typeId);
                }
                return extJdbcTemplate.query(sbQuery.toString(), params, getGoodsPreviewRowMapper());
            } else {
                sbQuery.append(" where mct.goods_category is null or mct.goods_type is null");
                return jdbcTemplate.query(sbQuery.toString(), getGoodsPreviewRowMapper());
            }
        }
    }

    @Override
    public ResponseMsg addNewOrder(Order order) {
        return transactionTemplate.execute(transactionStatus -> {
            Object sp = transactionStatus.createSavepoint();
            String queryAddOrder = "insert into orders values (default,?,?,?,?,?,?,default,default,?,?)";
            long calcTotalPrice = computeOrderTotalPrice(order.getProductsList());
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(queryAddOrder, new String[]{"id"});
                ps.setString(1, order.getFirstName());
                ps.setString(2, order.getLastName());
                ps.setString(3, order.getEmail());
                ps.setString(4, order.getPhoneNumber());
                ps.setString(5, order.getDeliveryAddress());
                ps.setString(6, order.getComment());
                ps.setInt(7, order.getDeliveryType());
                ps.setLong(8, computeOrderTotalPrice(order.getProductsList()));
                return ps;
            }, keyHolder);

            long orderId = keyHolder.getKey().longValue();
            String queryAddProdList = "insert into orders_products_list(order_id, product_id, \"count\") values (?,?,?)";
            for (Product product : order.getProductsList()) {
                long c = jdbcTemplate.queryForObject("select count(id) from goods " +
                        "where id in (select good from goods_list where good=?) and is_hidden=false", Long.class, product.getId());
                if (c > 0) {
                    int c2 = jdbcTemplate.update(queryAddProdList, orderId, product.getId(), product.getCount());
                    if (c2 == 0) {
                        transactionStatus.rollbackToSavepoint(sp);
                        return new ResponseMsg("error", "order is not succeeded");
                    }
                } else {
                    transactionStatus.rollbackToSavepoint(sp);
                    return new ResponseMsg("not_exists", "product with id=" + product.getId() + " in order is not exists");
                }
            }
            return new ResponseMsg("success");
        });
    }

    @Override
    public ResponseMsg addNewGoodsComment(ProductComment productComment) {
        if (productComment != null) {
            long c = jdbcTemplate.queryForObject("select count(id) from goods " +
                    "where id in (select good from goods_list where good=?) and is_hidden=false", Long.class, productComment.getProductId());
            if (c > 0) {
                int rows = jdbcTemplate.update("insert into goods_comments(good, username, usermail, usercomment) values (?,?,?,?)",
                        productComment.getProductId(), productComment.getUsername(), productComment.getUserEmail(), productComment.getComment());
                if (rows > 0) {
                    return new ResponseMsg("success");
                }
            } else {
                return new ResponseMsg("not_exists",
                        "product comment is not added, product with id=" + productComment.getProductId() + " is not exists");
            }
        }
        return new ResponseMsg("error", "product comment is not added");
    }

    @Override
    public ResponseMsg addSubscriber(String email) {
        if (email != null) {
            try {
                int rows = jdbcTemplate.update("insert into subscribers(email) values (?)", email);
                if (rows > 0) {
                    return new ResponseMsg("success");
                }
            } catch (Exception e) {
                return new ResponseMsg("already_exists");
            }
        }
        return new ResponseMsg("error", "subscriber is not added");
    }

    @Override
    public List<Product> searchGoods(String search) {
        String searchQuery = "%" + search + "%";
        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append("select t1.good_id as id, t1.naming, t1.descr_prev, t1.descr_ext, t1.photo_path, t1.price, t1.count from ");
        sbQuery.append("(select gl.id, gl.good as good_id, gl.goods_cat_type, g.naming, g.descr_prev, g.descr_ext, g.photo_path, g.price, g.count ");
        sbQuery.append("from goods_list gl left join goods g on gl.good=g.id where g.is_hidden=false)");
        sbQuery.append(" t1 left join map_categories_types t2 on t1.goods_cat_type=t2.id");
        sbQuery.append(" where t1.naming ilike ?");
        sbQuery.append(" group by (t1.good_id, t1.naming, t1.descr_prev, t1.descr_ext, t1.photo_path, t1.price, t1.count)");

        return jdbcTemplate.query(sbQuery.toString(), getGoodsPreviewRowMapper(), searchQuery);
    }

    private long computeOrderTotalPrice(List<Product> products) {
        long sum = 0;
        for (Product p : products) {
            p.setPrice(getProductById(p.getId()).getPrice());
            sum += p.getCount() * p.getPrice();
        }
        return sum;
    }
}