(function() {
    'use strict';

    angular
        .module('iscamApp')
        .controller('PuestoModalCatalogoController', PuestoModalCatalogoController);

    PuestoModalCatalogoController.$inject = ['Puesto', 'ParseLinks', 'AlertService', 'paginationConstants', '$uibModalInstance', '$uibModal', '$translate', '$filter'];

    function PuestoModalCatalogoController(Puesto, ParseLinks, AlertService, paginationConstants, $uibModalInstance, $uibModal, $translate, $filter) {

        var vm = this;

        function onBusquedaPersonalizada(){
            vm.isEmpty = false;
            vm.puestos = angular.copy(puestosSinFiltro);

            if(vm.inputBuscarPorClave !== null && vm.inputBuscarPorClave !== ""){
                vm.puestos = $filter('filter')(vm.puestos,function (puestoComponent){
                    return (puestoComponent.puesto.id.toString()).includes(vm.inputBuscarPorClave);
                });
            }
            if(vm.inputBuscarPorDescripcion !== null && vm.inputBuscarPorDescripcion !== ""){
                vm.puestos = $filter('filter')(vm.puestos,function (puestoComponent){
                    return ($filter('removeAccents')(puestoComponent.puesto.descripcion)).toLowerCase().includes($filter('removeAccents')(vm.inputBuscarPorDescripcion).toLowerCase());
                });
            }
            if(vm.puestos.length == 0){
                vm.isEmpty = true;
            }
        }
    }

})();
