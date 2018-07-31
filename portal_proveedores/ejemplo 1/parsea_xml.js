Proyecto: Portal de proveedores con consumo de servicios web provenientes de SAT.

*Codigo para subir un archivo XML para ser parseado.

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