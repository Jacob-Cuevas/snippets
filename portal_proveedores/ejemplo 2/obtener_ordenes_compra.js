// Visualización de datos agrupados en un wrapper general. Fragmento de código de angularjs.
/**
 * El objetivo es agrupar información de documentos pendientes por cobrar proveniente del ERP SAP 
 * para presentarla al usuario en la siguiente estructura:
 *  -> Documento OrdenCompra
 *      |-> Documento de entrada de mercancías
 *          |-> Detalle del documento de entrada de mercancías.
 */

var app = angular.module("myapp", []);

		//Recuperar datos proveedor y OC
        app.controller("MyController", function($scope, $http) {		
			
			$scope.isVisible = false;
			$scope.isProveedor = false;
			$scope.isOrdenesCompras = false;
			$scope.isVacioOrdenCompras = false;
			$scope.loading = false;
			$scope.isErrorInterno = false;
			$scope.loadingsat = false;
			
			$scope.buscadorordencompra = '';
			
			$scope.sendPost = function() {
				/*headers: {'Content-Type': 'application/x-www-form-urlencoded'},*/
				
				$scope.loading = true;
				
				var request = $http({
                    method: "POST",
                    url: "sap/obtenerDatosProveedor.php",
					data: {'clave' : clave_prov/*$scope.clave_prov*/}
                });

				request.success(
				
                    function( respuesta ) {
						
						if(respuesta.status == "OK"){
							
							$scope.isVisible = true;
							$scope.isProveedor = false;
							$scope.isErrorInterno = false;
							$scope.snombre = respuesta.nombre;
							$scope.spoblacion = respuesta.poblacion;
							$scope.spostal = respuesta.cod_postal;
							$scope.sregion = respuesta.region;
							$scope.scalle = respuesta.calle;
							$scope.sdireccion = respuesta.direccion;
							$scope.snif = respuesta.nif;
							$scope.stelefono = respuesta.telefono;
							clave_proveedor = respuesta.codigo;
							//$scope.loading = false;
						} else {
							$scope.isVisible = false;
							$scope.isProveedor = true;
							$scope.isOrdenesCompras = false;
							$scope.loading = false;
							$scope.isErrorInterno = false;
						}
						
                    }
                );
				
				request.error(
				
					function( respuesta ) {
						
						$scope.isVisible = false;
						$scope.isProveedor = false;
						$scope.isOrdenesCompras = false;
						$scope.loading = false;
						$scope.isErrorInterno = true;
						
					}
				
				);
				
				/*headers: {'Content-Type': 'application/x-www-form-urlencoded'},*/
				
				$scope.loading = true;
				
				var requestOrden = $http({
					method: "POST",
					url: "sap/obtenerOrdenComprasEntradaMercancias.php",
					data: {'clave_prov' : clave_prov/*$scope.clave_prov*/, 
							'clave_soc' : empresa/*$scope.empresa*/}
				});
				
				requestOrden.success(
				
					function( respuestaOrdenes ) {

						if(respuestaOrdenes.ordencompras.length > 0){
							
							$scope.isOrdenesCompras = true;
							$scope.isVacioOrdenCompras = false;
							
							$scope.ordenesCompras = respuestaOrdenes.ordencompras;
							
							$scope.containerPrincipal = [];
							$scope.listadoOrdenes = [];
							$scope.listadoEntradasMerc = [];
							$scope.listadoDetalleEntradasMerc = [];
							
							angular.forEach($scope.ordenesCompras,function(valueOrdn,indexOrdn){
								
								//Se valida el arreglo para las OC
								if($scope.listadoOrdenes.length > 0){
									//Se valida si ya existe el elemento en el arreglo
									$scope.isExist = false;
									angular.forEach($scope.listadoOrdenes, function(valExistOrd, indexExistOrd){
										
										if($scope.isExist == false){
											
											if(valExistOrd.ord_comp == valueOrdn.EBELN ){
											
												$scope.isExist = true;
											
											}
											
										}
										
									});
									
									if($scope.isExist == false){
										
										//Se agrega el nuevo valor de la OC
										$scope.elementoOrdenCompra = {
											ord_comp  : valueOrdn.EBELN,
											ord_importe : valueOrdn.RLWRT,
										};
										$scope.listadoOrdenes.push($scope.elementoOrdenCompra);
										
									}
									
								} else {
									//Se agrega el primer elemento
									$scope.elementoOrdenCompra = {
										ord_comp  : valueOrdn.EBELN,
										ord_importe : valueOrdn.RLWRT,
									};
									
									$scope.listadoOrdenes.push($scope.elementoOrdenCompra);
									
								}
								
								//Se valida el arreglo para las EM
								if($scope.listadoEntradasMerc.length > 0){
									
									$scope.isExist = false;
									angular.forEach($scope.listadoEntradasMerc, function(valExistEntMerc, indexExistEntMerc){
										
										if($scope.isExist == false){
											
											if(valExistEntMerc.entrada_merc == valueOrdn.BELNR_MAT 
											&& valExistEntMerc.ord_comp == valueOrdn.EBELN){
											
												$scope.isExist = true;
												
												$scope.listadoEntradasMerc[indexExistEntMerc].entrada_merc_importe = parseFloat($scope.listadoEntradasMerc[indexExistEntMerc].entrada_merc_importe) + parseFloat(valueOrdn.WRBTR);
												
											}
											
										}
										
									});
									
									if($scope.isExist == false){
										
										//Se agrega la nueva EM
										$scope.elementoEntradaMerc = {
											ord_comp  : valueOrdn.EBELN,
											entrada_merc  : valueOrdn.BELNR_MAT,
											entrada_merc_importe : valueOrdn.WRBTR,
											doc_transporte : valueOrdn.REBEL,
										};
										$scope.listadoEntradasMerc.push($scope.elementoEntradaMerc);
										
									}
									
								} else {
									//Se agrega el primer elemento
									$scope.elementoEntradaMerc = {
										ord_comp  : valueOrdn.EBELN,
										entrada_merc  : valueOrdn.BELNR_MAT,
										entrada_merc_importe : valueOrdn.WRBTR,
										doc_transporte : valueOrdn.REBEL,
									};
									$scope.listadoEntradasMerc.push($scope.elementoEntradaMerc);
								}
								
								//Se crea el arreglo de detalles de la entrada de mercancias
								$scope.elementoDetalleEntradaMerc = {
									
									orden_comp : valueOrdn.EBELN,
									entrada_merc  : valueOrdn.BELNR_MAT,
									articulo : valueOrdn.MAKTX,
									cantidad : valueOrdn.MENGE
								};
								
								$scope.listadoDetalleEntradasMerc.push($scope.elementoDetalleEntradaMerc);

							});
							
							//Se crea el nuevo arreglo final
							angular.forEach($scope.listadoOrdenes,function(valueoc,indexoc){
								
								$scope.arrayEm = [];
								angular.forEach($scope.listadoEntradasMerc,function(valueem,indexem){
									
									if(valueem.ord_comp == valueoc.ord_comp){
										
										$scope.arrayDetalleEm = [];
										
										angular.forEach($scope.listadoDetalleEntradasMerc,function(valuedetalle,indexdetalle){
										
											if(valuedetalle.entrada_merc == valueem.entrada_merc 
											&& valuedetalle.orden_comp == valueem.ord_comp){
												
												$scope.elementoDetalle = {
												
													orden_comp : valueem.ord_comp,
													entrada_merc : valuedetalle.entrada_merc,
													articulo : valuedetalle.articulo,
													cantidad : valuedetalle.cantidad
													
												};
												
												$scope.arrayDetalleEm.push($scope.elementoDetalle);
												
											}
											
										});
										
										$scope.elementoEntMer = {
											
											ord_comp  : valueem.ord_comp,
											entrada_merc  : valueem.entrada_merc,
											entrada_merc_importe : valueem.entrada_merc_importe,
											doc_transporte : valueem.doc_transporte,
											array_detalle : $scope.arrayDetalleEm
											
										};
										
										$scope.arrayEm.push($scope.elementoEntMer);
										
									}

									
								});
								
								//Se crea el arreglo final
								$scope.elementoFinal = {
									ord_comp : valueoc.ord_comp,
									ord_importe : valueoc.ord_importe ,
									array_entradas : $scope.arrayEm
								};
								
								$scope.containerPrincipal.push($scope.elementoFinal);
								
							});
							 
							$scope.loading = false;

						} else {
							$scope.isVacioOrdenCompras = true;
							$scope.isOrdenesCompras = false;
							$scope.mensajeError = respuestaOrdenes.mensaje;
							$scope.loading = false;
						}
					}
				);
				
			};	
			
			//Metodo valida importe EM
			$scope.validarImporteEM = function(container){
				
				var totalEM = 0;
				var numEm = 0;
				
				for(var i in container.array_entradas){
					
					totalEM += container.array_entradas[i].entrada_merc_importe;
					if(container.array_entradas[i].entrada_merc_importe > 0){
						numEm++;
					}
					
				}
				
				/*if(numEm == 0){
					$scope.isVacioOrdenCompras = true;
					$scope.isOrdenesCompras = false;
				}*/
			
				if(totalEM == 0){
					return false;
				}
				
				return true;
			}
			
        });