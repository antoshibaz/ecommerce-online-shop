app.controller('OrderCtrl',
    function ($rootScope, $scope, $http, $routeParams) {
        $scope.order = null;
        $scope.deliveryTypes = $rootScope.deliveryTypes;

        var orderIdParam = $routeParams.id;
        if (orderIdParam !== null) {
            $http.get('../admin-api/orders/' + orderIdParam.toString(), {}).then(function success(resp) {
                $scope.order = resp.data;
                $scope.selUpdDeliveryType = $scope.deliveryTypes[parseInt($scope.order.deliveryType) - 1];

                $http.get('../admin-api/orders/' + orderIdParam.toString() + "/goods-list", {}).then(function success(resp) {
                    $scope.order['goodsList'] = resp.data;
                }, function error(resp) {

                });

            }, function error(resp) {

            });

            $scope.updateOrderInfo = function (order) {
                $scope.order['deliveryType'] = $scope.selUpdDeliveryType['id'];
                $http.post("../admin-api/update-order-info", order, {}).then(function success(resp) {

                }, function error(resp) {

                });
            }
        }
    });