app.controller('OrdersCtrl',
    function ($rootScope, $scope, $http) {
        $scope.orders = null;
        $scope.orderStatuses = $rootScope.orderStatuses;
        $scope.thisStatus = $scope.orderStatuses[0];
        $scope.selOrder = null;
        $scope.selUpdStatus = $scope.orderStatuses[0];

        $scope.updateOrders = function () {
            $scope.getOrders($scope.thisStatus.id);
        };

        $scope.setSelOrder = function (order) {
            $scope.selOrder = order;
        };

        $scope.getOrders = function (status) {
            var ps;
            if (status !== undefined && status !== null) {
                ps = {params: {status: status}};
            } else {
                ps = {};
            }

            $http.get('../admin-api/orders', ps).then(function success(resp) {
                $scope.orders = resp.data;
            }, function error(resp) {

            });
        };

        $scope.changeStatus = function () {
            $http.post('../admin-api/set-order-status', {}, {
                params: {
                    id: parseInt($scope.selOrder.id),
                    status: parseInt($scope.selUpdStatus.id)
                }
            }).then(function success(resp) {
                $scope.getOrders($scope.thisStatus.id);
            }, function error(resp) {

            });
        };

        $scope.updateOrders();
    });