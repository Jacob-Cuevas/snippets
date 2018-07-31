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

	} else {																							
		$('#progress_' + folioOrden[1] + "_" + folioOrden[2] + ' .progress-bar').css('width',	0 + '%');
		$('<p/>').text(resultSelloSAT).appendTo('#files_' + folioOrden[1] + "_" + folioOrden[2]);
		$('.rowButtons_' + folioOrden[1] + "_" + folioOrden[2]).show();
		$('#loadingsat_' + folioOrden[1] + "_" + folioOrden[2]).hide();
	}
});	
																								