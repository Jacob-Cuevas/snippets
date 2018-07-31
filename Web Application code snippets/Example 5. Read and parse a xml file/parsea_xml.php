<?php

	//set_error_handler("warning_handler", E_WARNING);
	//error_reporting(E_ALL ^ E_WARNING);
	
	register_shutdown_function('shutDownFunction');

	//include '..\\const.php';
	include '..\\conexion.php';
	
	$nombreArchivo = isset($_POST['nombrearchivo']) ? $_POST['nombrearchivo'] : '';
	$organizacion = isset($_POST['org']) ? $_POST['org'] : '';
	$rutaArchivos =  RUTA_RAIZ_TEMPORAL_XML;

	$xml_subtotal = 0;
	$xml_total = 0;
	$UUID = '';
	$serie = '';
	$folio = '';
	$xml_descuento = 0;
	$metodo_pago = '';
	$rfc_Emisor = '';
	$rfc_Receptor = '';
	$fecha_Xml = '';
	$certificado = '';
	$no_certificado = '';
	$sello = '';
	$sello_CFD = '';
	$sello_SAT = '';
	$no_certificadoSAT = '';	
	$total_imp_trasladados = 0;
	$total_imp_retenidos = 0;
	
	
	if(preg_match("/\.(xml|txt)$/", strtolower($nombreArchivo))){
	
		if (file_exists($rutaArchivos . $nombreArchivo)) {
			
			try{
				
				//Se parsea el contenido del xml
				$xml = @simplexml_load_file($rutaArchivos . $nombreArchivo); 
				$ns = @$xml->getNamespaces(true);
				@$xml->registerXPathNamespace('c', $ns['cfdi']);
				@$xml->registerXPathNamespace('t', $ns['tfd']);
				
				if( @$xml->xpath('//cfdi:Comprobante') == null  ){
					@unlink($rutaArchivos . $nombreArchivo);
					echo 'Error al procesar el contenido del XML, puede deberse a un error del contenido del mismo. Verifique el archivo antes de adjuntarlo.';
					die();
				}
				
				foreach ( $xml->xpath('//cfdi:Comprobante') as $cfdiComprobante){ 
					$xml_descuento = $cfdiComprobante['descuento'];
					$xml_subtotal = $cfdiComprobante['subTotal'];
					$xml_total = $cfdiComprobante['total'];
					$serie = $cfdiComprobante['serie'];
					$folio = $cfdiComprobante['folio'];
					$metodo_pago = $cfdiComprobante['metodoDePago'];
					$certificado = $cfdiComprobante['certificado'];
					$no_certificado = trim($cfdiComprobante['noCertificado']);
					$sello = $cfdiComprobante['sello'];

					$array = explode( 'T' ,$cfdiComprobante['fecha'] );
					$fechaSinFormato = explode("-", $array[0] );//AAAA-MM-DD
					
					$fecha_Xml = $fechaSinFormato[2] . '.' . $fechaSinFormato[1] . '.' . $fechaSinFormato[0];
				} 	
				foreach ($xml->xpath('//cfdi:Comprobante//cfdi:Emisor') as $cfdiEmisor) {
					$rfc_Emisor = $cfdiEmisor['rfc'];
				}

				foreach ($xml->xpath('//cfdi:Comprobante//cfdi:Receptor') as $cfdiReceptor) {
					$rfc_Receptor = $cfdiReceptor['rfc'];
				}
				
				foreach ($xml->xpath('//cfdi:Comprobante//cfdi:Impuestos') as $cfdiImpuestos) {
					
					if(!isset($cfdiImpuestos['totalImpuestosTrasladados'])){

						foreach ($xml->xpath('//cfdi:Comprobante//cfdi:Impuestos//cfdi:Traslados//cfdi:Traslado') as $cfdiImpuestosTraslado) {
							$total_imp_trasladados += floatval($cfdiImpuestosTraslado['importe']);
						}
						
					} else {
						$total_imp_trasladados = $cfdiImpuestos['totalImpuestosTrasladados'];
					}
					
					if(!isset($cfdiImpuestos['totalImpuestosRetenidos'])){
						foreach ($xml->xpath('//cfdi:Comprobante//cfdi:Impuestos//cfdi:Retenciones//cfdi:Retencion') as $cfdiImpuestosRetencion) {
							$total_imp_retenidos += floatval($cfdiImpuestosRetencion['importe']);
						}
					} else {
						$total_imp_retenidos = $cfdiImpuestos['totalImpuestosRetenidos'];
					}
					
				}
				
				foreach ($xml->xpath('//t:TimbreFiscalDigital') as $tfdTimbreFiscalDigital) {
					$sello_CFD = $tfdTimbreFiscalDigital['selloCFD'];
					$sello_SAT = $tfdTimbreFiscalDigital['selloSAT'];
					$UUID = $tfdTimbreFiscalDigital['UUID'];
					$no_certificadoSAT = $tfdTimbreFiscalDigital['noCertificadoSAT'];
				}

				$parametrosxml = array();
				$parametrosxml['xml_subtotal'] = $xml_subtotal;
				$parametrosxml['xml_total'] = $xml_total;
				$parametrosxml['UUID'] = $UUID;
				$parametrosxml['serie'] = $serie;
				$parametrosxml['folio'] = $folio;
				$parametrosxml['xml_descuento'] = $xml_descuento;
				$parametrosxml['metodo_pago'] = $metodo_pago;
				$parametrosxml['rfc_Emisor'] = $rfc_Emisor;
				$parametrosxml['rfc_Receptor'] = $rfc_Receptor;
				$parametrosxml['fecha_Xml'] = $fecha_Xml;
				$parametrosxml['certificado'] = $certificado;
				$parametrosxml['no_certificado'] = $no_certificado;
				$parametrosxml['sello'] = $sello;
				$parametrosxml['sello_CFD'] = $sello_CFD;
				$parametrosxml['sello_SAT'] = $sello_SAT;
				$parametrosxml['no_certificadoSAT'] = $no_certificadoSAT;
				$parametrosxml['total_impuestos_trasladados'] = $total_imp_trasladados;
				$parametrosxml['total_impuestos_retenidos'] = $total_imp_retenidos;
				
				
				//Se verifica la sociedad CO para determinar el validador del xml
				if($organizacion != ''){
					
					$parametrosxml['validador_xml'] = get_validador_xml_sociedad($organizacion);
					
				} else {
					
					$parametrosxml['validador_xml'] = 'SAT';
					
				}
				
				echo json_encode($parametrosxml);
					
				

			}catch(Exception $e){
				@unlink($rutaArchivos . $nombreArchivo);
				echo 'Error al procesar el contenido del XML, puede deberse a un error del contenido del mismo';
				die();
			}
			
		}
		
	}

	
	function warning_handler($errno, $errstr) { 
		unlink($rutaArchivos . $nombreArchivo);
		exit('Archivo XML mal formado o con version anterior valida.');

	}
	
	function shutDownFunction() { 
		$error = error_get_last();
		// fatal error, E_ERROR === 1
		if ($error['type'] === E_ERROR) { 
			echo "El archivo XML se encuentra dañado o con un caracter inválido, favor de verificarlo."; 
		} 
	}

	
	function get_validador_xml_sociedad($sociedadco){
		
		$sql ="SELECT valida_sat, valida_verifact FROM fi_xml_conf_validador_xml WHERE sociedad_co = '$sociedadco' ;";
		$result=mysql_query($sql);
		$data=mysql_fetch_assoc($result);

		if($data['valida_sat'] == 1){
			return "SAT";
		}

		if($data['valida_verifact'] == 1){
			return "VERIFACT";
		}
		
	}

?>