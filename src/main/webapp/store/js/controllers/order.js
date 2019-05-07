app.controller('OrderCtrl',
    function ($rootScope, $scope, $http, $location, localStorageService) {
        if ($rootScope.getCart()['countPositions'] !== 0) {
            $scope.deliveryOptions = $rootScope.deliveryTypes;
            $scope.selDeliveryType = {id: '1', val: 'Самовывоз'};
            $scope.addr = null;
            $scope.addrCity = null;
            $scope.order = {};
            $scope.orderOk = false;

            $scope.sendOrder = function () {
                if ($scope.orderForm.$valid) {
                    $scope.order['deliveryType'] = parseInt($scope.selDeliveryType['id']);
                    $scope.order['deliveryAddress'] = $scope.addrCity.concat(', ', $scope.addr);
                    $scope.order['totalPrice'] = $rootScope.getCart()['totalPrice'];
                    $scope.order['productsList'] = $rootScope.getCart()['productsList'];

                    $http.post('shop-api/send-order', $scope.order, {}).then(function success(resp) {
                        $rootScope.clearCart();
                        $scope.order = {};
                        $scope.addr = null;
                        $scope.addrCity = null;
                        $scope.orderOk = true;
                    }, function error() {

                    })
                }
            }

        } else {
            $location.path('/cart');
        }
    });