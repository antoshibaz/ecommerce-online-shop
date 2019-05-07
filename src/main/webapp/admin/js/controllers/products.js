app.controller('ProductsCtrl',
    function ($rootScope, $scope, $http, FileUploader) {
        $scope.goods = null;
        $scope.categoriesWithTypes = null;
        $scope.selCat = null;
        $scope.selType = null;
        $scope.selProduct = {};
        $scope.isAddingMode = true;
        $scope.productsForAttaching = {};

        var uploader = $scope.uploader = new FileUploader({
            url: '../admin-api/set-product-photo',
            queueLimit: 1,
            filters: [{
                name: 'imageFilter',
                fn: function (item /*{File|FileLikeObject}*/, options) {
                    var filename = item.name.toLowerCase();
                    return (item.name.lastIndexOf('.png') === filename.length - '.png'.length)
                        || (item.name.lastIndexOf('.gif') === filename.length - '.gif'.length)
                        || (item.name.lastIndexOf('.jpg') === filename.length - '.jpg'.length)
                        || (item.name.lastIndexOf('.jpeg') === filename.length - '.jpeg'.length);
                }
            }],
            onCompleteAll: function () {
                uploader.clearQueue();
            }
        });
        uploader.onBeforeUploadItem = function (item) {
            item.headers.id = encodeURIComponent(item.headers.id);
        };
        uploader.onProgressItem = function (item) {
            item.headers.id = decodeURIComponent(item.headers.id);
        };

        $http.get("../shop-api/categories-types", {}).then(function success(resp) {
            $scope.categoriesWithTypes = resp.data;
            for (var i = 0; i < $scope.categoriesWithTypes.length; i++) {
                $scope.categoriesWithTypes[i]['goodsTypes'].push({'id': -1, 'naming': 'Нет'});
            }
            $scope.categoriesWithTypes.push({
                'goodsCategory': {'id': -1, 'naming': 'Нет'},
                'goodsTypes': [{'id': -1, 'naming': 'Нет'}]
            });
            $scope.selCat = $scope.categoriesWithTypes[$scope.categoriesWithTypes.length - 1];
            $scope.selType = $scope.selCat['goodsTypes'][0];

            $scope.updateProducts();

        }, function error() {

        });

        $scope.addingMode = function () {
            $scope.isAddingMode = true;
            $http.get("../admin-api/all-goods", {}).then(function success(resp) {
                $scope.productsForAttaching = resp.data;
                if ($scope.productsForAttaching !== null) {
                    $scope.selProduct = $scope.productsForAttaching[0];
                }
            }, function error() {

            });
        };

        $scope.creatingMode = function () {
            $scope.isAddingMode = false;
            $scope.selProduct = {};
        };

        $scope.updateProducts = function () {
            $http.get("../shop-api/goods-list", {
                params: {
                    category: $scope.selCat['goodsCategory']['id'],
                    type: $scope.selType['id']
                }
            }).then(function success(resp) {
                $scope.goods = resp.data;
                console.log($scope.goods);

            }, function error() {

            });
        };

        $scope.updateProductsForType = function () {
            $scope.updateProducts();
        };

        $scope.updateProductsForCategory = function () {
            $scope.selType = $scope.selCat['goodsTypes'][$scope.selCat['goodsTypes'].length - 1];
            $scope.updateProducts();
        };

        $scope.addProduct = function (product) {
            $scope.attachProduct(product);
        };

        $scope.createProduct = function (product) {
            $http.post('../admin-api/add-product', product, {}).then(function success(resp) {
                var prodId = parseInt(resp.data['returnedParam']);
                if (uploader.queue.length > 0) {
                    angular.forEach($scope.uploader.queue, function (item, key) {
                        item.headers.id = prodId;
                    });
                    uploader.uploadAll();
                }

                if ($scope.selCat['goodsCategory']['id'] !== -1 && $scope.selType['id'] !== -1) {

                } else {
                    $scope.updateProducts();
                }
            }, function error() {

            })
        };

        $scope.removeProduct = function (product) {
            $http.post('../admin-api/remove-product', {}, {
                params: {
                    id: product['id']
                }
            }).then(function success(resp) {
                $scope.updateProducts();
            }, function error() {

            })
        };

        $scope.attachProduct = function (product) {
            console.log($scope.selCat['goodsCategory']['id']);
            console.log($scope.selType['id']);
            $http.post('../admin-api/attach-product', product, {
                params: {
                    category: $scope.selCat['goodsCategory']['id'],
                    type: $scope.selType['id']
                }
            }).then(function success(resp) {
                $scope.updateProducts();
            }, function error() {

            });
        };

        $scope.detachProduct = function (product) {
            $http.post('../admin-api/detach-product', product, {
                params: {
                    category: $scope.selCat['goodsCategory']['id'],
                    type: $scope.selType['id']
                }
            }).then(function success(resp) {
                $scope.updateProducts();
            }, function error() {

            });
        };
    });