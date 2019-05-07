app.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'store/views/welcome.html',
                controller: 'WelcomeCtrl'
            })
            .when('/products', {
                templateUrl: 'store/views/products.html',
                controller: 'ProductsCtrl'
            })
            .when('/product', {
                templateUrl: 'store/views/product-detail.html',
                controller: 'ProductDetailCtrl'
            })
            .when('/cart', {
                templateUrl: 'store/views/cart.html',
                controller: 'CartCtrl'
            })
            .when('/order', {
                templateUrl: 'store/views/order.html',
                controller: 'OrderCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);