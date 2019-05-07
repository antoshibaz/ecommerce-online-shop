app.controller('CategsTypesCtrl',
    function ($rootScope, $scope, $http) {
        $scope.categoriesWithTypes = null;
        $scope.formData = {model: null};
        $scope.selCat = null;
        $scope.selType = null;
        $scope.selTypeForMap = null;
        $scope.allTypes = null;
        $scope.selAllType = null;
        $scope.typeMode = {
            id: '1'
        };
        $scope.cat = null;
        $scope.catAddingMode = true;
        $scope.type = null;
        $scope.typeAddingMode = true;

        $scope.onOptionsChanged = function (option) {
            angular.forEach($scope.categoriesWithTypes, function (optionItem) {
                if (optionItem.goodsCategory.id === option.goodsCategory.id) {
                    $scope.selCat = optionItem;
                    $scope.selType = optionItem.goodsTypes[0];
                }
            })
        };

        $scope.updateTypesForCategory = function () {
            if ($scope.selCat['goodsTypes'] !== null) {
                $scope.selType = $scope.selCat['goodsTypes'][0];
            }
        };

        $scope.refreshCategoriesWithTypes = function (selCatInd, selTypeInd) {
            $http.get("../shop-api/categories-types", {}).then(function success(resp) {
                $scope.categoriesWithTypes = resp.data;

                if ($scope.categoriesWithTypes !== undefined && $scope.categoriesWithTypes !== null) {
                    if (selCatInd !== -1 && selCatInd < $scope.categoriesWithTypes.length) {
                        $scope.selCat = $scope.categoriesWithTypes[selCatInd];
                    } else {
                        $scope.selCat = $scope.categoriesWithTypes[0];
                    }
                    if ($scope.selCat !== undefined && $scope.selCat !== null) {
                        if (selTypeInd !== -1 && selTypeInd < $scope.selCat.goodsTypes.length) {
                            $scope.selType = $scope.selCat['goodsTypes'][selTypeInd];
                        } else {
                            $scope.selType = $scope.selCat['goodsTypes'][0];
                        }
                    }
                }

            }, function error() {

            });
        };

        $scope.refreshAllTypes = function () {
            $http.get("../shop-api/types", {}).then(function success(resp) {
                $scope.allTypes = resp.data;
                if ($scope.allTypes !== null) {
                    $scope.selAllType = $scope.allTypes[0];
                    $scope.selTypeForMap = $scope.allTypes[0];
                }
            }, function error() {

            });
        };

        $scope.updateTypeMode = function () {
            if ($scope.typeMode.id === '1') {
                $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), $scope.getSelTypeIndex());
            } else if ($scope.typeMode.id === '2') {
                $scope.refreshAllTypes();
            }
        };

        $scope.setCatAddingMode = function () {
            $scope.catAddingMode = true;
            $scope.cat = {};
        };
        $scope.setCatUpdatingMode = function () {
            $scope.catAddingMode = false;
            $scope.cat = $scope.selCat['goodsCategory'];
        };

        $scope.setTypeAddingMode = function () {
            $scope.typeAddingMode = true;
            $scope.type = {};
        };
        $scope.setTypeUpdatingMode = function () {
            $scope.typeAddingMode = false;
            if ($scope.typeMode.id === '1'
                && ($scope.selType !== undefined && $scope.selType !== null)) {
                $scope.type = $scope.selType;
            } else if ($scope.typeMode.id === '2'
                && ($scope.selAllType !== undefined && $scope.selAllType !== null)) {
                $scope.type = $scope.selAllType;
            }
        };

        $scope.addNewCategory = function (category) {
            $http.post("../admin-api/add-category", category, {}).then(function success(resp) {
                $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), $scope.getSelTypeIndex());
            }, function error() {

            });
        };

        $scope.addNewType = function (type) {
            if ($scope.typeMode.id === '1') {
                // назначение (привязка) уже существующего типа к выбранной категории
                if ($scope.selTypeForMap !== null) {
                    $http.post("../admin-api/map-category-type", {}, {
                        params: {
                            category: $scope.selCat['goodsCategory']['id'],
                            type: $scope.selTypeForMap['id']
                        }
                    }).then(function success(resp) {
                        $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), $scope.getSelTypeIndex());
                    }, function error() {

                    });
                }
            } else if ($scope.typeMode.id === '2') {
                // добавление нового типа
                $http.post("../admin-api/add-type", type, {}).then(function success(resp) {
                    $scope.refreshAllTypes();
                }, function error() {

                });
            }
        };

        $scope.updCategory = function (category) {
            if (category !== undefined && category !== null) {
                $http.post("../admin-api/update-category", category, {}).then(function success(resp) {
                    $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), $scope.getSelTypeIndex());
                }, function error() {

                });
            }
        };

        $scope.updType = function (type) {
            if (type !== undefined && type !== null) {
                $http.post("../admin-api/update-type", type, {}).then(function success(resp) {
                    if ($scope.typeMode.id === '1') {
                        $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), $scope.getSelTypeIndex());
                    } else if ($scope.typeMode.id === '2') {
                        $scope.refreshAllTypes();
                    }
                }, function error() {

                });
            }
        };

        $scope.removeCategory = function () {
            if ($scope.selCat !== undefined && $scope.selCat !== null) {
                $http.post("../admin-api/remove-category", {}, {
                    params: {
                        id: $scope.selCat['goodsCategory']['id']
                    }
                }).then(function success(resp) {
                    $scope.refreshCategoriesWithTypes(0, $scope.getSelTypeIndex());
                }, function error() {

                });
            }
        };

        $scope.removeType = function () {
            if ($scope.typeMode.id === '1' && $scope.selType !== undefined && $scope.selType !== null) {
                // удалить тип только для выбранной категории
                $http.post("../admin-api/unmap-category-type", {}, {
                    params: {
                        category: $scope.selCat['goodsCategory']['id'],
                        type: $scope.selType['id']
                    }
                }).then(function success(resp) {
                    $scope.refreshCategoriesWithTypes($scope.getSelCatIndex(), 0);
                }, function error() {

                });
            } else if ($scope.typeMode.id === '2' && $scope.selAllType !== undefined && $scope.selAllType !== null) {
                // удалить тип глобально
                $http.post("../admin-api/remove-type", {}, {
                    params: {
                        id: $scope.selAllType['id']
                    }
                }).then(function success(resp) {
                    $scope.refreshAllTypes();
                }, function error() {

                });
            }
        };

        $scope.getSelCatIndex = function () {
            return $scope.categoriesWithTypes.indexOf($scope.selCat);
        };

        $scope.getSelTypeIndex = function () {
            return $scope.selCat.goodsTypes.indexOf($scope.selType);
        };

        // $scope.updateTypeMode();
        $scope.refreshCategoriesWithTypes(0, 0);
        $scope.refreshAllTypes();
    });