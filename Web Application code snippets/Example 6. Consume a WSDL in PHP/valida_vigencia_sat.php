<?php
	error_reporting(E_ALL ^ E_WARNING);
	require_once('..//lib/lib_nusoap/nusoap.php');
	include '..\\const.php';
	
	$nombreArchivo = isset($_POST['nombrearchivo']) ? $_POST['nombrearchivo'] : '';
	$json = isset($_POST['json']) ? $_POST['json'] : '';
	
	$xml_total = $json["xml_total"][0];
	$UUID = $json["UUID"][0];
	$rfc_Emisor = $json["rfc_Emisor"][0];
	$rfc_Receptor = $json["rfc_Receptor"][0];
	$rutaArchivos =  RUTA_RAIZ_TEMPORAL_XML;
	
	validar_vigencia_xml_SAT($xml_total, $UUID, $rfc_Emisor, $rfc_Receptor, $rutaArchivos, $nombreArchivo);
	

	function validar_vigencia_xml_SAT($xml_total, $UUID, $rfc_Emisor, $rfc_Receptor, $rutaArchivos, $nombreArchivo){
	
		$url = URL_WSDL_SAT;
		//Libreria para comunicacion WSDL
		$soapclient = new nusoap_client($url,$esWSDL=true); 
		$soapclient->soap_defencoding = 'UTF-8';  
		$soapclient->decode_utf8 = false; 
		//Se preparan los parametros
		$impo = (double)$xml_total; 
		$impo=sprintf("%.6f", $impo); 
		$impo = str_pad($impo,17,"0",STR_PAD_LEFT); 
		$uuid = strtoupper($UUID); 

		$factura = "?re=$rfc_Emisor&rr=$rfc_Receptor&tt=$impo&id=$UUID"; 
		$params = array('expresionImpresa'=>$factura); 
		//Se realiza la peticion
		$buscar = $soapclient->call('Consulta',$params); 

		if( $buscar['ConsultaResult']['Estado'] != 'Vigente' ){
			echo 'La factura no se encuentra vigente ante el SAT, favor de verificar la factura antes de adjuntarla.';
			@unlink($rutaArchivos . $nombreArchivo);
			die();
		}
		
		echo 'OK';
	}

?>