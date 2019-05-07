app.controller('ProductCtrl',
    function ($rootScope, $scope, $http, $routeParams, FileUploader, $route) {
        $scope.product = null;
        $scope.productComments = null;

        var productIdParam = $routeParams.id;
        if (productIdParam !== null) {
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
                    $route.reload();
                }
            });
            uploader.onBeforeUploadItem = function (item) {
                item.headers.id = encodeURIComponent(item.headers.id);
            };
            uploader.onProgressItem = function (item) {
                item.headers.id = decodeURIComponent(item.headers.id);
            };

            $http.get('../shop-api/goods/' + productIdParam.toString(), {}).then(function success(resp) {
                $scope.product = resp.data;
                $scope.setImg();
                $scope.refreshComments();

            }, function error(resp) {

            });

            $scope.refreshComments = function () {
                $http.get('../shop-api/goods/' + productIdParam.toString() + "/comments", {}).then(function success(resp) {
                    $scope.productComments = resp.data;

                }, function error(resp) {

                });
            };

            $scope.updateProduct = function (product) {
                $http.post('../admin-api/update-product', product, {}).then(function success(resp) {

                }, function error() {

                });

                if (uploader.queue.length > 0) {
                    angular.forEach($scope.uploader.queue, function (item, key) {
                        item.headers.id = product['id'];
                    });
                    uploader.uploadAll();
                }

                $scope.setImg();
            };

            $scope.deleteComment = function (comment) {
                $http.post('../admin-api/remove-comment', {}, {
                    params: {
                        id: comment['id']
                    }
                }).then(function success(resp) {
                    $scope.refreshComments();
                }, function error() {

                })
            };

            $scope.setImg = function () {
                $scope.imgSrc = $scope.product.id;
            }
        }
    });