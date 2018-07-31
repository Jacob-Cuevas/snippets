(function() {
    'use strict';

    angular
        .module('iscamApp')
        .filter('removeAccents', removeAccents);

    function removeAccents() {
        return removeAccentsFilter;

        function removeAccentsFilter (input) {
            if (input !== null) {
                input = input.toLowerCase();
                input = input.replace(/á/g, 'a')
                    .replace(/é/g, 'e')
                    .replace(/í/g, 'i')
                    .replace(/ó/g, 'o')
                    .replace(/ú/g, 'u');
            }
            return input;
        }
    }
})();
