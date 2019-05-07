package com.kotomka.adminservice;

import com.kotomka.core.dao.AdminCoreDAO;
import com.kotomka.core.dao.ShopCoreDAO;
import com.kotomka.core.model.logic.GoodsCategory;
import com.kotomka.core.model.logic.GoodsType;
import com.kotomka.core.model.logic.Order;
import com.kotomka.core.model.logic.Product;
import com.kotomka.core.model.server.ResponseMsg;
import com.kotomka.core.service.ProductPhotoService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/admin-api")
public class AdminServiceCtrl {

    private static final String METHOD_GET_ALL_GOODS = "all-goods";
    private static final String METHOD_ADD_PRODUCT = "add-product";
    private static final String METHOD_UPDATE_PRODUCT = "update-product";
    private static final String METHOD_REMOVE_PRODUCT = "remove-product";
    private static final String METHOD_ATTACH_PRODUCT = "attach-product";
    private static final String METHOD_DETACH_PRODUCT = "detach-product";
    private static final String METHOD_ADD_TYPE = "add-type";
    private static final String METHOD_UPDATE_TYPE = "update-type";
    private static final String METHOD_REMOVE_TYPE = "remove-type";
    private static final String METHOD_ADD_CATEGORY = "add-category";
    private static final String METHOD_UPDATE_CATEGORY = "update-category";
    private static final String METHOD_REMOVE_CATEGORY = "remove-category";
    private static final String METHOD_MAP_CATEGORY_AND_TYPE = "map-category-type";
    private static final String METHOD_UNMAP_CATEGORY_AND_TYPE = "unmap-category-type";
    private static final String METHOD_UPDATE_PRODUCT_COMMENT = "update-comment";
    private static final String METHOD_REMOVE_PRODUCT_COMMENT = "remove-comment";
    private static final String METHOD_SET_ORDER_STATUS = "set-order-status";
    private static final String METHOD_UPDATE_ORDER_INFO = "update-order-info";
    private static final String METHOD_GET_ORDERS = "orders";
    private static final String METHOD_GET_ORDER_BY_ID = "orders/{id}";
    private static final String METHOD_GET_ORDER_PRODUCTS = "orders/{id}/goods-list";
    private static final String METHOD_SET_PRODUCT_PHOTO = "set-product-photo";

    private final AdminCoreDAO adminCoreDAO;
    private final ShopCoreDAO shopCoreDAO;
    private final ProductPhotoService productPhotoService;

    @Autowired
    public AdminServiceCtrl(AdminCoreDAO adminCoreDAO, ShopCoreDAO shopCoreDAO,
                            ProductPhotoService productPhotoService) {
        this.adminCoreDAO = adminCoreDAO;
        this.shopCoreDAO = shopCoreDAO;
        this.productPhotoService = productPhotoService;
    }

    private abstract class Executer<T> {

        private final String methodName;
        private String user;

        public Executer(String methodName) {
            this.methodName = methodName;
            this.user = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.equals("admin")) {
                shopCoreDAO.setSuperuserScope(true);
            } else {
                shopCoreDAO.setSuperuserScope(false);
            }
        }

        abstract T doExecute() throws Exception;

        public ResponseEntity<?> execute() {
            try {
                return new ResponseEntity<Object>(doExecute(), HttpStatus.OK);
            } catch (NotFoundException ne) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (final Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @RequestMapping(value = METHOD_GET_ALL_GOODS, method = GET)
    @ResponseBody
    public ResponseEntity<?> getAllGoods() {
        return (new Executer<List<Product>>(METHOD_GET_ALL_GOODS) {
            @Override
            List<Product> doExecute() throws Exception {
                return adminCoreDAO.getAllGoods();
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ADD_PRODUCT, method = POST)
    @ResponseBody
    public ResponseEntity<?> addProduct(@RequestBody final Product product) {
        return (new Executer<ResponseMsg>(METHOD_ADD_PRODUCT) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.addProduct(product);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UPDATE_PRODUCT, method = POST)
    @ResponseBody
    public ResponseEntity<?> updateProduct(@RequestBody final Product product) {
        return (new Executer<ResponseMsg>(METHOD_UPDATE_PRODUCT) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.updateProduct(product);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_REMOVE_PRODUCT, method = POST)
    @ResponseBody
    public ResponseEntity<?> removeProduct(@RequestParam("id") final Long productId) {
        return (new Executer<ResponseMsg>(METHOD_REMOVE_PRODUCT) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.removeProduct(productId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ADD_CATEGORY, method = POST)
    @ResponseBody
    public ResponseEntity<?> addCategory(@RequestBody final GoodsCategory goodsCategory) {
        return (new Executer<ResponseMsg>(METHOD_ADD_CATEGORY) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.addCategory(goodsCategory);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UPDATE_CATEGORY, method = POST)
    @ResponseBody
    public ResponseEntity<?> updateCategory(@RequestBody final GoodsCategory goodsCategory) {
        return (new Executer<ResponseMsg>(METHOD_UPDATE_CATEGORY) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.updateCategory(goodsCategory);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_REMOVE_CATEGORY, method = POST)
    @ResponseBody
    public ResponseEntity<?> removeCategory(@RequestParam("id") final Long categoryId) {
        return (new Executer<ResponseMsg>(METHOD_REMOVE_CATEGORY) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.removeCategory(categoryId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ADD_TYPE, method = POST)
    @ResponseBody
    public ResponseEntity<?> addType(@RequestBody final GoodsType goodsType) {
        return (new Executer<ResponseMsg>(METHOD_ADD_TYPE) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.addType(goodsType);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UPDATE_TYPE, method = POST)
    @ResponseBody
    public ResponseEntity<?> updateType(@RequestBody final GoodsType goodsType) {
        return (new Executer<ResponseMsg>(METHOD_UPDATE_TYPE) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return adminCoreDAO.updateType(goodsType);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_REMOVE_TYPE, method = POST)
    @ResponseBody
    public ResponseEntity<?> removeType(@RequestParam("id") final Long typeId) {
        return (new Executer<ResponseMsg>(METHOD_REMOVE_TYPE) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.removeType(typeId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ATTACH_PRODUCT, method = POST)
    @ResponseBody
    public ResponseEntity<?> attachProduct(@RequestBody final Product product,
                                           @RequestParam("type") final Long typeId,
                                           @RequestParam("category") final Long categoryId) {
        return (new Executer<ResponseMsg>(METHOD_ATTACH_PRODUCT) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.attachProduct(product, typeId, categoryId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_DETACH_PRODUCT, method = POST)
    @ResponseBody
    public ResponseEntity<?> detachProduct(@RequestBody final Product product,
                                           @RequestParam("type") final Long typeId,
                                           @RequestParam("category") final Long categoryId) {
        return (new Executer<ResponseMsg>(METHOD_DETACH_PRODUCT) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.detachProduct(product, typeId, categoryId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_MAP_CATEGORY_AND_TYPE, method = POST)
    @ResponseBody
    public ResponseEntity<?> mapCategoryType(@RequestParam("category") final Long categoryId,
                                             @RequestParam("type") final Long typeId) {
        return (new Executer<ResponseMsg>(METHOD_MAP_CATEGORY_AND_TYPE) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.mapCategoryAndType(categoryId, typeId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UNMAP_CATEGORY_AND_TYPE, method = POST)
    @ResponseBody
    public ResponseEntity<?> unmapCategoryType(@RequestParam("category") final Long categoryId,
                                               @RequestParam("type") final Long typeId) {
        return (new Executer<ResponseMsg>(METHOD_UNMAP_CATEGORY_AND_TYPE) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.unmapCategoryAndType(categoryId, typeId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UPDATE_PRODUCT_COMMENT, method = POST)
    @ResponseBody
    public ResponseEntity<?> updateProductComment(@RequestParam("id") final Long commentId,
                                                  @RequestParam("hide") final Boolean hide) {
        return (new Executer<ResponseMsg>(METHOD_UPDATE_PRODUCT_COMMENT) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.updateComment(commentId, hide);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_REMOVE_PRODUCT_COMMENT, method = POST)
    @ResponseBody
    public ResponseEntity<?> removeProductComment(@RequestParam("id") final Long commentId) {
        return (new Executer<ResponseMsg>(METHOD_REMOVE_PRODUCT_COMMENT) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.removeComment(commentId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_SET_ORDER_STATUS, method = POST)
    @ResponseBody
    public ResponseEntity<?> setOrderStatus(@RequestParam("id") final Long orderId,
                                         @RequestParam("status") final Integer status) {
        return (new Executer<ResponseMsg>(METHOD_SET_ORDER_STATUS) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.updateOrderStatus(orderId, status);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_UPDATE_ORDER_INFO, method = POST)
    @ResponseBody
    public ResponseEntity<?> updateOrderInfo(@RequestBody final Order order) {
        return (new Executer<ResponseMsg>(METHOD_UPDATE_ORDER_INFO) {
            @Override
            ResponseMsg doExecute() {
                return adminCoreDAO.updateOrderInfo(order);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_ORDERS, method = GET)
    @ResponseBody
    public ResponseEntity<?> getPreviewOrdersList(@RequestParam(value = "status", required = false) final Integer status) {
        return (new Executer<List<Order>>(METHOD_GET_ORDERS) {
            @Override
            List<Order> doExecute() throws Exception {
                return adminCoreDAO.getOrdersPreview(status);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_ORDER_BY_ID, method = GET)
    @ResponseBody
    public ResponseEntity<?> getOrder(@PathVariable(value = "id") final Long orderId) {
        return (new Executer<Order>(METHOD_GET_ORDER_BY_ID) {
            @Override
            Order doExecute() throws Exception {
                return adminCoreDAO.getOrderById(orderId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_ORDER_PRODUCTS, method = GET)
    @ResponseBody
    public ResponseEntity<?> getPreviewOrderProductsList(@PathVariable(value = "id") final Long orderId) {
        return (new Executer<List<Product>>(METHOD_GET_ORDER_PRODUCTS) {
            @Override
            List<Product> doExecute() throws Exception {
                return adminCoreDAO.getOrderProductsPreview(orderId);
            }
        }).execute();
    }

    @Value("${products.photos.dir}")
    String productPhotosDirPath;

    @RequestMapping(value = METHOD_SET_PRODUCT_PHOTO, method = POST)
    @ResponseBody
    public ResponseEntity<?> uploadProductPhoto(
            @RequestHeader(value = "id") final String productId,
            @RequestParam(value = "file") final MultipartFile file) {
        return new Executer<ResponseMsg>(METHOD_SET_PRODUCT_PHOTO) {
            @Override
            ResponseMsg doExecute() throws Exception {
                Long id = Long.parseLong(productId);
                Product product = shopCoreDAO.getProductById(id);
                try {
                    if (product != null) {
                        String filename = String.valueOf(id);
                        String fileExt = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
                        if (productPhotoService.forUploadProductPhoto(
                                file.getInputStream(),
                                productPhotosDirPath,
                                filename + "." + fileExt)) {
                            return new ResponseMsg("success");
                        }
                    }
                } catch (Exception e) {
                }
                return new ResponseMsg("error");
            }
        }.execute();
    }
}