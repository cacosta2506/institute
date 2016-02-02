function validarCampoNumerico(componente, idForm){    
    var numero = componente.value;
    var formulario = document.getElementById(idForm);    
    if(isNaN(numero)) {
          componente.style.borderColor = '#ff0000';                     
          recorrerFormulario(formulario, true);          
        }
    else{
        componente.style.borderColor = '#ffffff';            
        recorrerFormulario(formulario, false);
        }
}

function recorrerFormulario(formulario, activo){
    for(var i=0; i<formulario.elements.length; i++){
        if(formulario.elements[i].type == "submit"){
            formulario.elements[i].disabled = activo;
            }        
    }
}