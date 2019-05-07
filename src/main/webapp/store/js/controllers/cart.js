app.controller('CartCtrl',
    function ($rootScope, $scope, $http, $location) {
        $scope.cart = $rootScope.getCart();
        console.log($scope.cart);

        $scope.forwardToOrder = function () {
            $location.path("/order");
        }
    });