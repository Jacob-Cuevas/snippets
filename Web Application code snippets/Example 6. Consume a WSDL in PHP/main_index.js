	//URL RAIZ para la subida de ARCHIVOS
	var url = 'server/php/';
	var clave_proveedor = "";
	$('.fileinput-button').hide();
	$('.progress').hide();
	$('.print').hide();
	$('.loadingsat').hide();
	
	var empresa = $('#empresa').val();
	var org = $('#org').val();	
	var clave_prov = $('#clave_prov').val();
	var nif_empresa = $('#nif').val();
	
	var tamanoArchivo = 10000000; //10mb
	
	var headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'POST, GET, OPTIONS, PUT',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': '*'
    };

	
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
		
		
		//Subir XML
		$(document).on("click",".fileupload",function() {

			var folioOrden = $(this).attr('id').split("_");//Se hace el split de la cadena para obtener la OC y la EM
			
			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
					
			$(this).fileupload({
				formData: { 
					nombreOrden: folioOrden[1], 
					entradaMercancia: folioOrden[2], 
					isXml: 'true', 
					isActualizarXml: 'false'
				},
				url: url,
				dataType: 'json',
				done: function (e, data) {

					$.each(data.result.files, function (index, file) {
						
						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).hide();
						
						//Solo se valida el importe de la EM
						var montoEntradaMerc = $("#importeEM_" + folioOrden[1] + "_" + folioOrden[2]).text().replace('$','').replace(',','');
						var totalOc = $("#importeOC_" + folioOrden[1]).text().replace('$','').replace(',','');
						//var organizacion = $('input[name=organizacion]').val();
						
						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).show();
						
						//Se parsea el archivo
						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Leyendo el archivo');
						
						
						$.ajax({
							type: "POST" ,
							url: "validaciones_xml_php/parsea_xml.php" ,
							data: {
								nombrearchivo: file.name,
								org: org
							},
							add: function (e, data) {
								var goUpload = true;
								var uploadFile = data.files[0];
								if (!(/\.(xml|XML)$/i).test(uploadFile.name)) {
									goUpload = false;
								}
								if (uploadFile.size > tamanoArchivo) { // 5mb
									goUpload = false;
								}
								if (goUpload == true) {
									data.submit();
								}
							},
							success: function(resultval) {
								
								try{
									
									var response = jQuery.parseJSON(resultval);
									
									if(typeof response =='object'){ 
									
										//Se validan importes/RFC Sociedad
										$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando Importes/RFCs de la factura');
										$.ajax({
											type: "POST" ,
											url: "validaciones_xml_php/valida_rfc_importe_xml.php" ,
											data: {
												rfcsociedad: nif_empresa,
												nombrearchivo: file.name,
												totalEntradaMercancia: montoEntradaMerc,
												json: response
											},
											success: function(resultValidRfc) {
												
												if(resultValidRfc == "OK"){
													
													//Se valida estructura
													$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando estructura del XML');
													
													$(response).each(function(i,val){
														$.each(val,function(k,v){
															
															if(v == "SAT"){
																//SAT 
																
																$.ajax({
																	type: "POST" ,
																	url: "validaciones_xml_php/valida_estructura.php" ,
																	data: {
																		nombrearchivo: file.name
																	},
																	success: function(resultEstruct) {
																		
																		if(resultEstruct == "OK"){
																			
																			//Se valida el sello XML
																			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando el sello de Emisor del XML');
																			
																			$.ajax({
																				type: "POST" ,
																				url: "validaciones_xml_php/verifica_sello_xml.php" ,
																				data: {
																					nombrearchivo: file.name,
																					json: response
																				},
																				success: function(resultSelloXml) {
																					
																					if(resultSelloXml == "OK"){
																						
																						//Se realiza la validacion sello CFD
																						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando el sello CFDI ante el SAT');
																						
																						$.ajax({
																							type: "POST" ,
																							url: "validaciones_xml_php/verifica_sello_cfd.php" ,
																							data: {
																								nombrearchivo: file.name,
																								json: response
																							},
																							success: function(resultSelloCFD) {
																								
																								if(resultSelloCFD == "OK"){
																									
																									//Se realiza la validacion del sello SAT
																									$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando vigencia de la factura');
																									
																									$.ajax({
																										type: "POST" ,
																										url: "validaciones_xml_php/valida_vigencia_sat.php" ,
																										data: {
																											nombrearchivo: file.name,
																											json: response
																										},
																										success: function(resultSelloSAT) {
																											
																											if(resultSelloSAT == "OK"){
																												
																												//Proceso interno
																												$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Guardando factura...');
																												
																												$.ajax({
																													type: "POST" ,
																													url: "validarXml.php" ,
																													data: {
																														sociedad: 'dsfsdfsd',
																														ordencompra: folioOrden[1],
																														entradamercancia: folioOrden[2],
																														nombrearchivo: file.name,
																														importeem: montoEntradaMerc,
																														importeoc: totalOc,
																														proveedor: clave_proveedor,
																														isactualizar: 'false',
																														json: response
																													},
																													success: function(resultServer) {
																														
																														$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
																														$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																														
																														if (resultServer.indexOf("OK") >= 0){
																															
																															$.ajax({
																																type: "POST" ,
																																url: "actualizarSubidaProveedor.php" ,
																																data: {
																																	proveedor: clave_proveedor,
																																	ordencompra: folioOrden[1],
																																	entradamercancia: folioOrden[2],
																																	isfactura: 1
																																},
																																success: function(resultSubida) {
																																	
																																	$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																	
																																	if(resultSubida == 'OK'){
																																		
																																		$('#print_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																		$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																		$('<p/>').text('Archivo ' + file.name + ' subido con éxito.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																																	
																																	} else {
																																	
																																		$.ajax({
																																			type: "POST" ,
																																			url: "sap/insertarRegistroFacturaProveedorSap.php" ,
																																			data: {
																																				proveedor: clave_proveedor,
																																				ordencompra: folioOrden[1],
																																				entradamercancia: folioOrden[2],
																																				totalOrdenCompra: totalOc,
																																				totalEntradaMercancia: montoEntradaMerc,
																																				totalxml: '0.0',
																																				nombrearchivo: file.name,
																																				rutaxml: '' ,
																																				operacion: 'B',
																																				serie: '',
																																				UUID: ''
																																			},
																																			success: function(resultInsercion) {
																																				
																																				$('<p/>').text(resultInsercion).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																																				
																																			}

																																		});
																																		
																																		
																																	}
																																	
																																}
																															});
																															
																														} else {

																															$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																															$('<p/>').html(resultServer).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																															$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																															$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																															
																														}
																															
																													}
																												});
																												
																											} else {
																												
																												$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																												$('<p/>').text(resultSelloSAT).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																												$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																												$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																												
																											}

																										}
																									});
																									
																								} else {
																									
																									$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																									$('<p/>').text(resultSelloCFD).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																									$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																									
																								}

																							}
																						});

																					} else {
																						
																						$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																						$('<p/>').text(resultSelloXml).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																						
																					}

																				}
																			});
																			
																			
																		} else {
																			
																			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																			$('<p/>').html(resultEstruct).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																			$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																			$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																			
																		}

																	}
																});
																
															} else 
															
															if(v == "VERIFACT"){
																//verifact
																
																$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando la factura con proveedor externo.');
																
																$.ajax({
																	type: "POST" ,
																	url: "validaciones_xml_php/valida_verifact.php" ,
																	data: {
																		nombrearchivo: file.name
																	},
																	success: function(resultverifact) {
																		
																		if(resultverifact == "OK"){
																			
																			//Proceso interno
																			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Guardando factura...');
																			
																			$.ajax({
																				type: "POST" ,
																				url: "validarXml.php" ,
																				data: {
																					sociedad: empresa,
																					ordencompra: folioOrden[1],
																					entradamercancia: folioOrden[2],
																					nombrearchivo: file.name,
																					importeem: montoEntradaMerc,
																					importeoc: totalOc,
																					proveedor: clave_proveedor,
																					isactualizar: 'false',
																					json: response
																				},
																				success: function(resultServer) {
																					
																					$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
																					$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																					
																					if (resultServer.indexOf("OK") >= 0){
																						
																						$.ajax({
																							type: "POST" ,
																							url: "actualizarSubidaProveedor.php" ,
																							data: {
																								proveedor: clave_proveedor,
																								ordencompra: folioOrden[1],
																								entradamercancia: folioOrden[2],
																								isfactura: 1
																							},
																							success: function(resultSubida) {
																								
																								$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																								
																								if(resultSubida == 'OK'){
																									
																									$('#print_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('<p/>').text('Archivo ' + file.name + ' subido con éxito.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																								
																								} else {
																								
																									$.ajax({
																										type: "POST" ,
																										url: "sap/insertarRegistroFacturaProveedorSap.php" ,
																										data: {
																											proveedor: clave_proveedor,
																											ordencompra: folioOrden[1],
																											entradamercancia: folioOrden[2],
																											totalOrdenCompra: totalOc,
																											totalEntradaMercancia: montoEntradaMerc,
																											totalxml: '0.0',
																											nombrearchivo: file.name,
																											rutaxml: '' ,
																											operacion: 'B',
																											serie: '',
																											UUID: ''
																										},
																										success: function(resultInsercion) {
																											
																											$('<p/>').text(resultInsercion).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																											
																										}

																									});
																									
																									
																								}
																								
																							}
																						});
																						
																					} else {

																						$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																						$('<p/>').html(resultServer).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																						
																					}
																						
																				}
																			});
																			
																		} else {
																			
																			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																			$('<p/>').text(resultverifact).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																			$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																			$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																			
																		}
																		
																	}

																});
																
															}
														});
													});
													
												} else {
													
													$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
													$('<p/>').html(resultValidRfc).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
													$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
													$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
													
												}
												
											}
										});
										
										
									  
									} else {
										
										$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
										$('<p/>').text(resultval).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
										$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
										$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
										
									}
									
								}catch(err) { 
									
									$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
									$('<p/>').text(resultval).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
									$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
									$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
									
								}
								
							}
						});

					});
					
				},
				fail: function (e,data) {
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
					$('<p/>').html("No se puede subir el XML").appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
					$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
					$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
				},
				progressall: function (e, data) {
					progress = parseInt(data.loaded / data.total * 100, 10);
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css(
						'width',
						progress + '%'
					);
				}
			}).prop('disabled', !$.support.fileInput)
				.parent().addClass($.support.fileInput ? undefined : 'disabled');
					
		});

		
		//Verificar existencia XML
		$(document).on("click",".verify",function() {
			var folioOrden = $(this).attr('id').split("_");
			$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
			$('#btnUpdateXml_' + folioOrden[1] + "_" + folioOrden[2]).show();
			$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).show();
			
			$.ajax({
				type: "POST" ,
				url: "verificarExistenciaXmlProveedor.php" ,
				data: {
					orden_compra: folioOrden[1],
					entrada_mercancia: folioOrden[2]
				},
				success: function(numRecords) {
					$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
					$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
					if(numRecords > 0){
						$('#btnUpload_' + folioOrden[1] + "_" + folioOrden[2]).hide();
						$('#progress_' + folioOrden[1] + "_" + folioOrden[2]).show();
						$('#print_' + folioOrden[1] + "_" + folioOrden[2]).show();
						$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
						$('<p/>').text('Ya se ha asignado la factura XML a la orden de compra.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
					} else {
						$('#btnUpload_' + folioOrden[1] + "_" + folioOrden[2]).show();
						$('#progress_' + folioOrden[1] + "_" + folioOrden[2]).show();
						$('#btnUpdateXml_' + folioOrden[1] + "_" + folioOrden[2]).hide();
						$('#print_' + folioOrden[1] + "_" + folioOrden[2]).hide();
						$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).hide();
					}
				}
			});

		});
		
		
		//Subir archivo PDF
		$(document).on("click",".fileuploadPdf",function() {
			var folioOrden = $(this).attr('id').split("_");
			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
			
			$(this).fileupload({
				formData: { 
					nombreOrden: folioOrden[1], 
					entradaMercancia: folioOrden[2], 
					isXml: 'false'
				},
				url: url,
				dataType: 'json',
				add: function (e, data) {
					var goUpload = true;
					var uploadFile = data.files[0];
					if (!(/\.(pdf|PDF)$/i).test(uploadFile.name)) {
						goUpload = false;
					}
					if (uploadFile.size > tamanoArchivo) { // 10mb
						goUpload = false;
					}
					if (goUpload == true) {
						data.submit();
					}
				},
				done: function (e, data) {
					
				},
				success: function(resultval){
					
					if(resultval == true){
						
						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
						$('<p/>').text('Archivo subido con éxito.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
						
					} else {
					
						$('<p/>').text('Ha ocurrido un error al adjuntar el PDF, intente mas tarde.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
					
					}
					
				},
				progressall: function (e, data) {
					progress = parseInt(data.loaded / data.total * 100, 10);
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css(	'width',progress + '%');
				},
				fail: function (e,data){
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
					$.ajax({
						type: "POST" ,
						url: "verificarExistenciaXmlProveedor.php" ,
						data: {
							orden_compra: folioOrden[1],
							entrada_mercancia: folioOrden[2]
						},
						success: function(numRecords) {
							$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
							if(numRecords == 0){
								$('<p/>').text('Ingrese primeramente el archivo XML antes de adjuntar el PDF').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
							}
						}
					});
				}
			}).prop('disabled', !$.support.fileInput)
				.parent().addClass($.support.fileInput ? undefined : 'disabled');
			
		});
		
		
		//Actualizar archivo XML
		$(document).on("click",".fileUpdateXml",function() {
			
			var folioOrden = $(this).attr('id').split("_");
			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
			
			$(this).fileupload({
				formData: { 
					nombreOrden: folioOrden[1], 
					entradaMercancia: folioOrden[2], 
					isXml: 'true', 
					isActualizarXml: 'true'
				},
				url: url,
				dataType: 'json',
				add: function (e, data) {
					var goUpload = true;
					var uploadFile = data.files[0];
					if (!(/\.(xml|XML)$/i).test(uploadFile.name)) {
						goUpload = false;
					}
					if (uploadFile.size > tamanoArchivo) { // 5mb
						goUpload = false;
					}
					if (goUpload == true) {
						data.submit();
					}
				},
				done: function (e, data) {

					$.each(data.result.files, function (index, file) {
						
						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
						
						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).hide();

						//Solo se valida el importe de la EM
						var montoEntradaMerc = $("#importeEM_" + folioOrden[1] + "_" + folioOrden[2]).text().replace('$','').replace(',','');
						var totalOc = $("#importeOC_" + folioOrden[1]).text().replace('$','').replace(',','');	

						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).show();
						
						//Se parsea el archivo
						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Leyendo el archivo');
						
						$.ajax({
							type: "POST" ,
							url: "validaciones_xml_php/parsea_xml.php" ,
							data: {
								nombrearchivo: file.name,
								org: org
							},
							add: function (e, data) {
								var goUpload = true;
								var uploadFile = data.files[0];
								if (!(/\.(xml|XML)$/i).test(uploadFile.name)) {
									goUpload = false;
								}
								if (uploadFile.size > tamanoArchivo) { // 5mb
									goUpload = false;
								}
								if (goUpload == true) {
									data.submit();
								}
							},
							success: function(resultval) {
								
								try{
									
									var response = jQuery.parseJSON(resultval);
									
									if(typeof response =='object'){ 
									
										//Se validan importes/RFC Sociedad
										$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando Importes/RFCs de la factura');
										
										$.ajax({
											type: "POST" ,
											url: "validaciones_xml_php/valida_rfc_importe_xml.php" ,
											data: {
												rfcsociedad: nif_empresa,
												nombrearchivo: file.name,
												totalEntradaMercancia: montoEntradaMerc,
												json: response
											},
											success: function(resultValidRfc) {
												
												if(resultValidRfc == "OK"){
													
													//Se valida estructura
													$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando estructura del XML');
													
													$(response).each(function(i,val){
														$.each(val,function(k,v){
															
															if(v == "SAT"){
																
																$.ajax({
																	type: "POST" ,
																	url: "validaciones_xml_php/valida_estructura.php" ,
																	data: {
																		nombrearchivo: file.name
																	},
																	success: function(resultEstruct) {
																		
																		if(resultEstruct == "OK"){
																			
																			//Se valida el sello XML
																			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando el sello de Emisor del XML');
																			
																			$.ajax({
																				type: "POST" ,
																				url: "validaciones_xml_php/verifica_sello_xml.php" ,
																				data: {
																					nombrearchivo: file.name,
																					json: response
																				},
																				success: function(resultSelloXml) {
																					
																					if(resultSelloXml == "OK"){
																						
																						//Se realiza la validacion sello CFD
																						$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando el sello CFDI ante el SAT');
																						
																						$.ajax({
																							type: "POST" ,
																							url: "validaciones_xml_php/verifica_sello_cfd.php" ,
																							data: {
																								nombrearchivo: file.name,
																								json: response
																							},
																							success: function(resultSelloCFD) {
																								
																								if(resultSelloCFD == "OK"){
																									
																									//Se realiza la validacion del sello SAT
																									$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando la vigencia de la factura');
																									
																									$.ajax({
																										type: "POST" ,
																										url: "validaciones_xml_php/valida_vigencia_sat.php" ,
																										data: {
																											nombrearchivo: file.name,
																											json: response
																										},
																										success: function(resultSelloSAT) {
																											
																											if(resultSelloSAT == "OK"){
																												
																												//Proceso interno
																												$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Guardando factura...');
																												
																												$.ajax({
																													type: "POST" ,
																													url: "validarXml.php" ,
																													data: {
																														sociedad: empresa,
																														ordencompra: folioOrden[1],
																														entradamercancia: folioOrden[2],
																														nombrearchivo: file.name,
																														importeem: montoEntradaMerc,
																														importeoc: totalOc,
																														proveedor: clave_proveedor,
																														isactualizar: 'true',
																														json: response
																													},
																													success: function(resultServer) {
																														
																														$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
																														$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																														
																														if (resultServer.indexOf("OK") >= 0){
																															
																															$.ajax({
																																type: "POST" ,
																																url: "actualizarSubidaProveedor.php" ,
																																data: {
																																	proveedor: clave_proveedor,
																																	ordencompra: folioOrden[1],
																																	entradamercancia: folioOrden[2],
																																	isfactura: 1
																																},
																																success: function(resultSubida) {
																																	
																																	$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																	
																																	if(resultSubida == 'OK'){
																																		
																																		$('#print_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																		$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
																																		$('<p/>').text('Archivo ' + file.name + ' subido con éxito.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																																	
																																	} else {
																																	
																																		$.ajax({
																																			type: "POST" ,
																																			url: "sap/insertarRegistroFacturaProveedorSap.php" ,
																																			data: {
																																				proveedor: clave_proveedor,
																																				ordencompra: folioOrden[1],
																																				entradamercancia: folioOrden[2],
																																				totalOrdenCompra: totalOc,
																																				totalEntradaMercancia: montoEntradaMerc,
																																				totalxml: '0.0',
																																				nombrearchivo: file.name,
																																				rutaxml: '' ,
																																				operacion: 'B',
																																				serie: '',
																																				UUID: ''
																																			},
																																			success: function(resultInsercion) {
																																				
																																				$('<p/>').text(resultInsercion).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																																				
																																			}

																																		});
																																		
																																		
																																	}
																																	
																																}
																															});
																															
																														} else {

																															$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																															$('<p/>').html(resultServer).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																															$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																															$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																															
																														}
																															
																													}
																												});
																												
																											} else {
																												
																												$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																												$('<p/>').text(resultSelloSAT).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																												$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																												$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																												
																											}

																										}
																									});
																									
																								} else {
																									
																									$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																									$('<p/>').text(resultSelloCFD).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																									$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																									
																								}

																							}
																						});

																					} else {
																						
																						$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																						$('<p/>').text(resultSelloXml).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																						
																					}

																				}
																			});
																			
																			
																		} else {
																			
																			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																			$('<p/>').html(resultEstruct).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																			$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																			$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																			
																		}

																	}
																});
																
															} else 
																
															if(v == "VERIFACT"){
																
																$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Validando la factura con proveedor externo.');
																
																$.ajax({
																	type: "POST" ,
																	url: "validaciones_xml_php/valida_verifact.php" ,
																	data: {
																		nombrearchivo: file.name
																	},
																	success: function(resultverifact) {
																		
																		if(resultverifact == "OK"){
																			
																			//Proceso interno
																			$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('Actualizando factura...');
																			
																			$.ajax({
																				type: "POST" ,
																				url: "validarXml.php" ,
																				data: {
																					sociedad: empresa,
																					ordencompra: folioOrden[1],
																					entradamercancia: folioOrden[2],
																					nombrearchivo: file.name,
																					importeem: montoEntradaMerc,
																					importeoc: totalOc,
																					proveedor: clave_proveedor,
																					isactualizar: 'true',
																					json: response
																				},
																				success: function(resultServer) {
																					
																					$('#files_' + folioOrden[1] + "_" + folioOrden[2]).text('');
																					$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																					
																					if (resultServer.indexOf("OK") >= 0){
																						
																						$.ajax({
																							type: "POST" ,
																							url: "actualizarSubidaProveedor.php" ,
																							data: {
																								proveedor: clave_proveedor,
																								ordencompra: folioOrden[1],
																								entradamercancia: folioOrden[2],
																								isfactura: 1
																							},
																							success: function(resultSubida) {
																								
																								$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																								
																								if(resultSubida == 'OK'){
																									
																									$('#print_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('#btnUploadPdf_' + folioOrden[1] + "_" + folioOrden[2]).show();
																									$('<p/>').text('Archivo ' + file.name + ' subido con éxito.').appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																								
																								} else {
																								
																									$.ajax({
																										type: "POST" ,
																										url: "sap/insertarRegistroFacturaProveedorSap.php" ,
																										data: {
																											proveedor: clave_proveedor,
																											ordencompra: folioOrden[1],
																											entradamercancia: folioOrden[2],
																											totalOrdenCompra: totalOc,
																											totalEntradaMercancia: montoEntradaMerc,
																											totalxml: '0.0',
																											nombrearchivo: file.name,
																											rutaxml: '' ,
																											operacion: 'B',
																											serie: '',
																											UUID: ''
																										},
																										success: function(resultInsercion) {
																											
																											$('<p/>').text(resultInsercion).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																											
																										}

																									});
																									
																								}
																								
																							}
																						});
																						
																					} else {

																						$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																						$('<p/>').html(resultServer).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																						$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																						$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																						
																					}
																						
																				}
																			});
																			
																		} else {
																			
																			$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
																			$('<p/>').text(resultverifact).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
																			$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
																			$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
																			
																		}
																		
																	}

																});
																
															}
															
														});
														
													});
													
												}else{
													
													$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
													$('<p/>').html(resultValidRfc).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
													$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
													$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
													
												}
												
											}
										});
										

										
									  
									} else {
										
										$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
										$('<p/>').text(resultval).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
										$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
										$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
										
									}
									
								}catch(err) { 
									
									$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
									$('<p/>').text(resultval).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
									$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
									$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
									
								}
								
							}
						});
					
					});
					
				},
				fail: function (e,data){
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
					$('<p/>').html("No se puede actualizar el XML").appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
					$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
				},
				progressall: function (e, data) {
					progress = parseInt(data.loaded / data.total * 100, 10);
					$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css(	'width',progress + '%');
				}
			}).prop('disabled', !$.support.fileInput)
				.parent().addClass($.support.fileInput ? undefined : 'disabled');
			
		});
		
		
		//Imprimir Recibo
		$(document).on("click",".print",function() {
			
			var folioOrden = $(this).attr('id').split("_");
			
			submit_post_via_hidden_form(
				'imprimirRecibo.php',
				{
					ordencompra: folioOrden[1],
					entradamercancia: folioOrden[2],
					logoempresa: empresa
				}
			);
			
			
		});
		
		//Regresar al listado de empresas
		$(document).on("click","#regresar",function() {
			
			window.location.href = "galeria_empresas.php"; 
			
		});
		
		//Login
		$(document).on("click",".regresar_login",function() {
		
			 window.location.href = "cerrarSesion.php"; 
			
		});
		
		function submit_post_via_hidden_form(url, params) {
			var f = $("<form target='_blank' method='POST' style='display:none;'></form>").attr({
				action: url
			}).appendTo(document.body);

			for (var i in params) {
				if (params.hasOwnProperty(i)) {
					$('<input type="hidden" />').attr({
						name: i,
						value: params[i]
					}).appendTo(f);
				}
			}

			f.submit();

			f.remove();
		}
		