<?php
    if (isset($_SERVER['HTTP_ORIGIN'])) {
        header("Access-Control-Allow-Origin: {$_SERVER['HTTP_ORIGIN']}");
        header('Access-Control-Allow-Credentials: true');
        header('Access-Control-Max-Age: 86400');    // cache for 1 day
    }

    // Access-Control headers are received during OPTIONS requests
    if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {

        if (isset($_SERVER['HTTP_ACCESS_CONTROL_REQUEST_METHOD']))
            header("Access-Control-Allow-Methods: GET, POST, OPTIONS");         

        if (isset($_SERVER['HTTP_ACCESS_CONTROL_REQUEST_HEADERS']))
            header("Access-Control-Allow-Headers: {$_SERVER['HTTP_ACCESS_CONTROL_REQUEST_HEADERS']}");

        exit(0);
    }

	if ($_SERVER['REQUEST_METHOD'] == 'POST' && empty($_POST))
		$_POST = json_decode(file_get_contents('php://input'), true);

	$clave_proveedor = isset($_POST['clave']) ? $_POST['clave'] : '';
	$nombre_proveedor = isset($_POST['nombre']) ? $_POST['nombre'] : '';
	$rfc_proveedor = isset($_POST['rfc']) ? $_POST['rfc'] : '';
	
	include 'clasesSAP/sap.php';
	include 'clasesSAP/sap_connection.php';
	include 'clasesSAP/sap_function.php';
	include 'clasesSAP/sap_table.php';	
	include 'FuncionesSap.php';
	include 'const.php';
	
	$parametrosConexionSAP = array('servidor'=>SERVIDORSAP,'usuario'=>USERSAP,
							'contrasena'=>PASSWORDSAP,'mandante'=>MANDANTE,
							'idioma'=>LANGUAGE,'funcion'=>'ZFI_OBTENER_DATOS_PROVEEDOR');
							
	$funcionesSap = new FuncionesSAP();
	
	$rutaLog = RUTA_LOG;
	
	if( $clave_proveedor == "" AND $nombre_proveedor == "" AND $rfc_proveedor == "" ){
		echo "Ingrese todos los campos";
		exit;
	} else {
		
		if($clave_proveedor != "" AND $nombre_proveedor == "" AND $rfc_proveedor == ""){
			$nombre_proveedor = "*";
		}
		

		$parametrosFuncion = array();
		$parametrosFuncion['clave_prov'] = $clave_proveedor;
		$parametrosFuncion['nombre_prov'] = $nombre_proveedor;
		$parametrosFuncion['rfc_prov'] = $rfc_proveedor;
		
		$datosProveedor = $funcionesSap->obtenerInformacionProveedor($parametrosConexionSAP,$parametrosFuncion);
		
		if (sizeof($datosProveedor) > 0){
			echo json_encode($datosProveedor);
			/*foreach($datosProveedor as $itemDatos){
				echo $itemDatos;
			}*/
			
		} else {
			echo "No se encontraron resultados.";
			
		}
		
	}

?>