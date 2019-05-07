package com.kotomka.clientservice;

import com.kotomka.core.dao.ShopCoreDAO;
import com.kotomka.core.exceptions.BadRequestException;
import com.kotomka.core.model.logic.*;
import com.kotomka.core.model.server.ResponseMsg;
import com.kotomka.core.service.EmailSenderService;
import com.kotomka.core.service.ProductPhotoService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/shop-api")
public class ClientServiceCtrl {

    private static final String METHOD_GET_GOODS_BY_ID = "goods/{id}";
    private static final String METHOD_GET_GOODS_COMMENTS_BY_ID = "goods/{id}/comments";
    private static final String METHOD_GET_GOODS_PHOTO_BY_ID = "goods/{id}/photo";
    private static final String METHOD_GET_GOODS_CATEGORIES = "categories";
    private static final String METHOD_GET_GOODS_TYPES = "types";
    private static final String METHOD_GET_GOODS_CATEGORIES_WITH_TYPES = "categories-types";
    private static final String METHOD_GET_GOODS_LIST = "goods-list";
    private static final String METHOD_SEND_NEW_ORDER = "send-order";
    private static final String METHOD_ADD_GOODS_COMMENT = "add-comment";
    private static final String METHOD_ADD_SUBSCRIBER = "add-subscriber";
    private static final String METHOD_SEARCH_GOODS = "search-goods";

    private final ShopCoreDAO shopCoreDAO;
    private final ProductPhotoService productPhotoService;
    private final EmailSenderService emailSenderService;

    @Autowired
    public ClientServiceCtrl(ShopCoreDAO shopCoreDAO, ProductPhotoService productPhotoService,
                             EmailSenderService emailSenderService) {
        this.shopCoreDAO = shopCoreDAO;
        this.productPhotoService = productPhotoService;
        this.emailSenderService = emailSenderService;
    }

    private abstract class Executer<T> {

        private final String methodName;
        private String user;

        public Executer(String methodName) {
            this.methodName = methodName;
            this.user = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.equals("anonymousUser")) {
                shopCoreDAO.setSuperuserScope(false);
            } else {
                shopCoreDAO.setSuperuserScope(true);
            }
        }

        abstract T doExecute() throws Exception;

        public ResponseEntity<?> execute() {
            try {
                return new ResponseEntity<Object>(doExecute(), HttpStatus.OK);
            } catch (EmptyResultDataAccessException emptyExc) {
                return new ResponseEntity<>(null, HttpStatus.OK);
            } catch (NotFoundException ne) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (BadRequestException br) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } catch (final Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @RequestMapping(value = METHOD_GET_GOODS_BY_ID, method = GET)
    @ResponseBody
    public ResponseEntity<?> getGoodsById(@PathVariable(value = "id") final Long productId) {
        return (new Executer<Product>(METHOD_GET_GOODS_BY_ID) {
            @Override
            Product doExecute() throws Exception {
                return shopCoreDAO.getProductById(productId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_GOODS_COMMENTS_BY_ID, method = GET)
    @ResponseBody
    public ResponseEntity<?> getGoodsCommentsById(@PathVariable(value = "id") final Long productId) {
        return (new Executer<List<ProductComment>>(METHOD_GET_GOODS_COMMENTS_BY_ID) {
            @Override
            List<ProductComment> doExecute() throws Exception {
                return shopCoreDAO.getGoodsCommentsById(productId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_SEARCH_GOODS, method = GET)
    @ResponseBody
    public ResponseEntity<?> searchGoods(@RequestParam("search") final String search) {
        return (new Executer<List<Product>>(METHOD_SEARCH_GOODS) {
            @Override
            List<Product> doExecute() throws Exception {
                return shopCoreDAO.searchGoods(search);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_GOODS_CATEGORIES, method = GET)
    @ResponseBody
    public ResponseEntity<?> getGoodsCategories() {
        return (new Executer<List<GoodsCategory>>(METHOD_GET_GOODS_CATEGORIES) {
            @Override
            List<GoodsCategory> doExecute() throws Exception {
                return shopCoreDAO.getGoodsCategories();
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_GOODS_TYPES, method = GET)
    @ResponseBody
    public ResponseEntity<?> getGoodsTypes(
            @RequestParam(value = "category", required = false) final Long categoryId) {
        return (new Executer<List<GoodsType>>(METHOD_GET_GOODS_TYPES) {
            @Override
            List<GoodsType> doExecute() throws Exception {
                return shopCoreDAO.getGoodsTypes(categoryId);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_GOODS_CATEGORIES_WITH_TYPES, method = GET)
    @ResponseBody
    public ResponseEntity<?> getGoodsCategoriesWithTypes() {
        return (new Executer<List<GoodsCategoryWithTypes>>(METHOD_GET_GOODS_CATEGORIES_WITH_TYPES) {
            @Override
            List<GoodsCategoryWithTypes> doExecute() throws Exception {
                return shopCoreDAO.getGoodsCategoriesWithTypes();
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_GET_GOODS_LIST, method = GET)
    @ResponseBody
    public ResponseEntity<?> getPreviewGoodsList(
            @RequestParam(value = "category", required = false) final Long category,
            @RequestParam(value = "type", required = false) final Long type) {
        return (new Executer<List<Product>>(METHOD_GET_GOODS_LIST) {
            @Override
            List<Product> doExecute() throws Exception {
                return shopCoreDAO.getPreviewGoodsList((type != null && type < 0) ? null : type,
                        (category != null && category < 0) ? null : category);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_SEND_NEW_ORDER, method = POST)
    @ResponseBody
    public ResponseEntity<?> sendNewOrder(@RequestBody final Order order) {
        return (new Executer<ResponseMsg>(METHOD_SEND_NEW_ORDER) {
            @Override
            ResponseMsg doExecute() throws Exception {
                ResponseMsg responseMsg = shopCoreDAO.addNewOrder(order);

                StringBuilder msgBody = new StringBuilder();
                msgBody.append("<div>Ваш заказ принят и будет обработан в ближайшее время...\n</div><br>");
                msgBody.append("<div>Вы разместили заказ на сумму " + order.getTotalPrice() + " P в следующем виде:\n");
                for (Product p : order.getProductsList()) {
                    msgBody.append("<div> > ").append(p.getNaming()).append(", ").append(p.getCount()).append(" шт.\n</div>");
                }
                msgBody.append("</div>");
                msgBody.append("<br><br>").append("<div>С уваженимем, Kotomka Shop</div>");
                emailSenderService.sendEmailHtmlMsg(new String[]{order.getEmail()},
                        "Подтверждение заказа", msgBody.toString());

                return responseMsg;
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ADD_GOODS_COMMENT, method = POST)
    @ResponseBody
    public ResponseEntity<?> sendNewOrder(@RequestBody final ProductComment comment) {
        return (new Executer<ResponseMsg>(METHOD_ADD_GOODS_COMMENT) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return shopCoreDAO.addNewGoodsComment(comment);
            }
        }).execute();
    }

    @RequestMapping(value = METHOD_ADD_SUBSCRIBER, method = POST)
    @ResponseBody
    public ResponseEntity<?> addNewSubscriber(@RequestParam("email") final String email) {
        return (new Executer<ResponseMsg>(METHOD_ADD_SUBSCRIBER) {
            @Override
            ResponseMsg doExecute() throws Exception {
                return shopCoreDAO.addSubscriber(email);
            }
        }).execute();
    }

    @Value("${products.photos.dir}")
    String productPhotosDirPath;

    @RequestMapping(value = METHOD_GET_GOODS_PHOTO_BY_ID, method = GET, produces = "image/png", headers = "Accept=*/*")
    @ResponseBody
    public ResponseEntity<?> getProductPhoto(@PathVariable(value = "id") final Long productId) {
        return (new Executer<byte[]>(METHOD_GET_GOODS_PHOTO_BY_ID) {
            @Override
            byte[] doExecute() throws Exception {
                Product product = shopCoreDAO.getProductById(productId);
                if (product != null) {
                    byte[] f = productPhotoService.forDownloadProductPhoto(productPhotosDirPath,
                            productId + ".png");
                    if (f != null) {
                        return f;
                    }
                }

                String notFoundPhoto = ClientServiceCtrl.class.getClassLoader().getResource("images/notFoundPhoto.png").toURI().getPath();
                try (InputStream input = new FileInputStream(notFoundPhoto)) {
                    return IOUtils.toByteArray(input);
                }
            }
        }).execute();
    }
}