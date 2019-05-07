app.controller('ProductDetailCtrl',
    function ($rootScope, $scope, $http) {
        $scope.selQuantity = 1;
        $scope.userComment = {};

        $http.get("shop-api/goods/" + $rootScope.getSelProduct()['id'].toString(), {})
            .then(function success(resp) {
                $scope.product = resp.data;
                console.log($scope.product);

                $scope.getComments();

            }, function error() {

            });

        $scope.pQuantity = function () {
            if ($scope.selQuantity < 9) {
                $scope.selQuantity++;
            } else {
                $scope.selQuantity = 9;
            }
        };
        $scope.mQuantity = function () {
            if ($scope.selQuantity > 1) {
                $scope.selQuantity--;
            } else {
                $scope.selQuantity = 1;
            }
        };

        $scope.getComments = function () {
            $http.get("shop-api/goods/" + $rootScope.getSelProduct()['id'].toString() + "/comments", {})
                .then(function success(resp) {
                    $scope.prodComments = resp.data;
                    console.log($scope.prodComments);
                }, function error() {

                })
        };

        $scope.sendUserComment = function () {
            $scope.userComment['productId'] = $scope.product['id'];
            $http.post("shop-api/add-comment", $scope.userComment)
                .then(function success(resp) {
                    $scope.userComment = {};
                    $scope.getComments();

                }, function error() {

                });
        };

        $scope.putToCart = function () {
            $rootScope.putToCart({
                id: $scope.product['id'],
                naming: $scope.product['naming'],
                price: $scope.product['price'],
                count: $scope.selQuantity
            }, true);
            $scope.selQuantity = 1;
        }
    });