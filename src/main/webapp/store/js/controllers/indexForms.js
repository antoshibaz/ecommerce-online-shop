app.controller('FormsCtrl',
    function ($rootScope, $scope, $http, $location) {
        $scope.email = null;
        $scope.searchQuery = null;

        $scope.addSubscriber = function () {
            if ($scope.subscriberForm.$valid) {
                $http.post("shop-api/add-subscriber", {}, {
                    params: {email: $scope.email}
                }).then(function success(resp) {
                    $scope.email = null;
                }, function error() {

                });
            }
        };

        $scope.searchGoods = function () {
            if ($scope.searchForm.$valid) {
                $location.path("/products").search({search: $scope.searchQuery});
            }
        }
    });