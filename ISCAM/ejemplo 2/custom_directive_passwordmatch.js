(function() {
    'use strict';

    angular
        .module('iscamApp')
        .directive('passwordConfirm', passwordmatch);

    function passwordmatch() {

        return {
            restrict: 'A',
            scope: {
                matchTarget: '='
            },
            require: 'ngModel',
            link: function link(scope, elem, attrs, ctrl) {
                var validator = function (value) {
                    ctrl.$setValidity('match', value === scope.matchTarget);
                    return value;
                }

                ctrl.$parsers.unshift(validator);
                ctrl.$formatters.push(validator);

                scope.$watch('matchTarget', function(newval, oldval) {
                    validator(ctrl.$viewValue);
                });

            }
        };
    }
})();
