var app = angular.module("kotomka-adm", ['ngRoute', 'LocalStorageModule', 'angularFileUpload']);

app.directive('ngThumb', ['$window', function($window) {
    var helper = {
        support: !!($window.FileReader && $window.CanvasRenderingContext2D),
        isFile: function(item) {
            return angular.isObject(item) && item instanceof $window.File;
        },
        isImage: function(file) {
            var type =  '|' + file.type.slice(file.type.lastIndexOf('/') + 1) + '|';
            return '|jpg|png|jpeg|bmp|gif|'.indexOf(type) !== -1;
        }
    };

    return {
        restrict: 'A',
        template: '<canvas/>',
        link: function(scope, element, attributes) {
            if (!helper.support) return;

            var params = scope.$eval(attributes.ngThumb);

            if (!helper.isFile(params.file)) return;
            if (!helper.isImage(params.file)) return;

            var canvas = element.find('canvas');
            var reader = new FileReader();

            reader.onload = onLoadFile;
            reader.readAsDataURL(params.file);

            function onLoadFile(event) {
                var img = new Image();
                img.onload = onLoadImage;
                img.src = event.target.result;
            }

            function onLoadImage() {
                var width = params.width || this.width / this.height * params.height;
                var height = params.height || this.height / this.width * params.width;
                canvas.attr({ width: width, height: height });
                canvas[0].getContext('2d').drawImage(this, 0, 0, width, height);
            }
        }
    };
}]);

app.config(function (localStorageServiceProvider) {
    localStorageServiceProvider
        .setPrefix('testAdmin')
        .setStorageType('localStorage')
        .setDefaultToCookie(true)
        .setStorageCookie(30, '/', false)
        .setStorageCookieDomain('');
});

app.run(function ($rootScope, localStorageService, $http) {
    $rootScope.init = function () {
        $rootScope.deliveryTypes = [
            {id: '1', val: 'Самовывоз'},
            {id: '2', val: 'Доставка курьером'},
            {id: '3', val: 'Доставка почтой'}
        ];

        $rootScope.orderStatuses = [
            {id: 0, val: 'Не обработан'},
            {id: 1, val: 'Обработан/Ожидает оплаты'},
            {id: 2, val: 'Оплачен/Исполняется'},
            {id: 3, val: 'Исполнен/Закрыт'},
            {id: -1, val: 'Отменен'}
        ];
    };

    $rootScope.getDeliveryTypeLabel = function (deliveryId) {
        for (var i = 0; i < $rootScope.deliveryTypes.length; i++) {
            if (parseInt($rootScope.deliveryTypes[i]['id']) === parseInt(deliveryId)) {
                return $rootScope.deliveryTypes[i]['val'];
            }
        }
    };

    $rootScope.getOrderStatusLabel = function (statusId) {
        for (var i = 0; i < $rootScope.orderStatuses.length; i++) {
            if (parseInt($rootScope.orderStatuses[i]['id']) === parseInt(statusId)) {
                return $rootScope.orderStatuses[i]['val'];
            }
        }
    };

    $rootScope.init();
});