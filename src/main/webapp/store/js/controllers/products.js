app.controller('ProductsCtrl',
    function ($rootScope, $scope, $http, $routeParams) {
        $scope.productsList = null;
        var search = $routeParams.search;
        var c = $routeParams.c;
        var t = $routeParams.t;

        if (!(c === null) && !(c === undefined)) {
            $scope.categoryId = c;
            if (!(t === null) && !(t === undefined)) {
                $scope.typeId = t;
            } else {
                $scope.typeId = null;
            }

            $http.get("shop-api/goods-list", {
                params: {
                    category: $scope.categoryId,
                    type: $scope.typeId
                }
            }).then(function success(resp) {
                $scope.productsList = resp.data;
            }, function error() {

            })
        } else if (!(search === null) && !(search === undefined)) {
            $http.get("shop-api/search-goods", {params: {search: search}}).then(function success(resp) {
                $scope.productsList = resp.data;
            }, function error() {

            })
        } else {
            $http.get("shop-api/goods-list", {}).then(function success(resp) {
                $scope.productsList = resp.data;
            }, function error() {

            })
        }
    });