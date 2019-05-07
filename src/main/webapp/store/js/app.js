var app = angular.module("kotomka", ['ngRoute', 'LocalStorageModule']);

app.config(function (localStorageServiceProvider) {
    localStorageServiceProvider
        .setPrefix('test4')
        .setStorageType('localStorage')
        .setDefaultToCookie(true)
        .setStorageCookie(30, '/', false)
        .setStorageCookieDomain('');
});

app.run(function ($rootScope, localStorageService, $http) {
    $rootScope.init = function () {
        $rootScope.selProductsCategory = null;
        $rootScope.selProductsType = null;
        $rootScope.selProduct = null;
        $rootScope.categoriesWithTypes = null;
        $rootScope.cart = {
            countPositions: 0,
            totalPrice: 0,
            productsList: []
        };
        $rootScope.quantityOptions = [
            {id: '1', val: '1'},
            {id: '2', val: '2'},
            {id: '3', val: '3'},
            {id: '4', val: '4'},
            {id: '5', val: '5'},
            {id: '6', val: '6'},
            {id: '7', val: '7'},
            {id: '8', val: '8'},
            {id: '9', val: '9'}
        ];
        $rootScope.deliveryTypes = [
            {id: '1', val: 'Самовывоз'},
            {id: '2', val: 'Доставка курьером'},
            {id: '3', val: 'Доставка почтой'}
        ];

        if (localStorageService.get('selProductsCategory') === null) {
            localStorageService.set('selProductsCategory', null);
        }
        if (localStorageService.get('selProductsType') === null) {
            localStorageService.set('selProductsType', null);
        }
        if (localStorageService.get('selProduct') === null) {
            localStorageService.set('selProduct', null);
        }
        if (localStorageService.get('cart') === null) {
            localStorageService.set('cart', {
                countPositions: 0,
                totalPrice: 0,
                productsList: []
            });
        }
        localStorageService.bind($rootScope, 'selProductsCategory');
        localStorageService.bind($rootScope, 'selProductsType');
        localStorageService.bind($rootScope, 'selProduct');
        localStorageService.bind($rootScope, 'cart');
    };
    $rootScope.init();

    $rootScope.setSelProductsCategoryAndType = function (category, type) {
        $rootScope.selProductsCategory = category;
        $rootScope.selProductsType = type;
    };

    $rootScope.setSelProductsCategory = function (category) {
        $rootScope.selProductsCategory = category;
        $rootScope.selProductsType = null;
    };

    $rootScope.setSelProductsType = function (type) {
        $rootScope.selProductsType = type;
        $rootScope.selProductsCategory = null;
    };

    $rootScope.setSelProduct = function (product) {
        $rootScope.selProduct = product;
    };

    $rootScope.getSelProductsCategory = function () {
        return $rootScope.selProductsCategory;
    };

    $rootScope.getSelProductsType = function () {
        return $rootScope.selProductsType;
    };

    $rootScope.getSelProduct = function () {
        return $rootScope.selProduct;
    };

    $rootScope.putToCart = function (product, additionFlag) {
        var countPositionsInCart = $rootScope.cart['countPositions'];
        product['count'] = parseInt(product['count']);
        if (countPositionsInCart === 0) {
            $rootScope.cart['productsList'].push(product);
            $rootScope.cart['countPositions'] += 1;
            $rootScope.cart['totalPrice'] += product['price'] * product['count'];
        } else {
            var allProductsInCart = $rootScope.cart['productsList'];
            var existsFlag = -1;
            for (var i = 0; i < allProductsInCart.length; i++) {
                if (allProductsInCart[i]['id'] === product['id']) {
                    existsFlag = i;
                    break;
                }
            }

            if (existsFlag !== -1) {
                if (additionFlag) {
                    allProductsInCart[existsFlag]['count'] += product['count'];
                    $rootScope.cart['totalPrice'] += product['price'] * product['count'];
                } else {
                    allProductsInCart[existsFlag]['count'] = product['count'];
                    $rootScope.cart['totalPrice'] += -(allProductsInCart[existsFlag]['price'] * allProductsInCart[existsFlag]['count'])
                        + (product['price'] * product['count']);
                }
            } else {
                $rootScope.cart['productsList'].push(product);
                $rootScope.cart['countPositions'] += 1;
                $rootScope.cart['totalPrice'] += product['price'] * product['count'];
            }
        }
    };

    $rootScope.removeFromCart = function (productId) {
        for (var i = 0; i < $rootScope.cart['productsList'].length; i++) {
            if ($rootScope.cart['productsList'][i]['id'] === productId) {
                $rootScope.cart['totalPrice'] -= $rootScope.cart['productsList'][i]['price'] * $rootScope.cart['productsList'][i]['count'];
                $rootScope.cart['productsList'].splice(i, 1);
                $rootScope.cart['countPositions'] -= 1;
                break;
            }
        }
    };

    $rootScope.getCountPositionsInCart = function () {
        return $rootScope.cart['countPositions'];
    };

    $rootScope.getProductsListInCart = function () {
        return $rootScope.cart['productsList'];
    };

    $rootScope.getCart = function () {
        return $rootScope.cart;
    };

    $rootScope.clearCart = function () {
        $rootScope.cart = {
            countPositions: 0,
            totalPrice: 0,
            productsList: []
        };
    };

    $rootScope.setSelProductById = function (productId) {
        $http.get("shop-api/goods/" + productId, {}).then(function success(resp) {
            $rootScope.setSelProduct(resp.data);
        }, function error() {

        })
    };

    $rootScope.getGoodsCategoriesWithTypes = function () {
        return $rootScope.categoriesWithTypes;
    };

    $http.get("shop-api/categories-types", {}).then(function success(resp) {
        $rootScope.categoriesWithTypes = resp.data;
    }, function error() {

    });
});