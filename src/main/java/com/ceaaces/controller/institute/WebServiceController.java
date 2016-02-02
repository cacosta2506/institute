package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.core.SourceExtractor;
import org.springframework.xml.transform.StringSource;

import ec.gob.ceaaces.data.TituloSenescyt;
import ec.gob.ceaaces.utilitarios.SenescytClient;

public class WebServiceController implements Serializable {

    /**
     * @author eviscarra
     */

    private static final long serialVersionUID = -7472034743985815106L;

    private static final Logger LOG = Logger
	    .getLogger(WebServiceController.class.getSimpleName());

    @Autowired
    private SenescytClient webServiceClient;

    private String respuesta = null;
    private String identificacion;
    private List<TituloSenescyt> titulosReconocidos;
    private String nombreDocente;

    public WebServiceController() {
	this.titulosReconocidos = new ArrayList<>();
    }

    public void cargarIdentificacion(String documento) {
	this.identificacion = documento;
	LOG.info("THIS.IDENTIFICACION: " + this.identificacion);
    }

    public List<TituloSenescyt> getTitulosReconocidos() {
	return titulosReconocidos;
    }

    public void setTitulosReconocidos(List<TituloSenescyt> titulosReconocidos) {
	this.titulosReconocidos = titulosReconocidos;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public String getRespuesta() {
	return respuesta;
    }

    public void setRespuesta(String respuesta) {
	this.respuesta = respuesta;
    }

    /**
     * 
     * Para inicializar el controlador
     * 
     * @author vllumiquinga
     * @version 08/08/2014 - 17:32:34
     */
    public void init() {
	LOG.info(">>>> WEBSERVICECONTROLLER");
    }

    public void consultaSenescyt() {
	LOG.info("METODO consultaSenescyt");

	this.titulosReconocidos.clear();
	LOG.info("IDENTIFICACION: " + this.identificacion);
	try {
	    if (!getIdentificacion().equals("")) {
		titulosReconocidos = customerClient();
	    } else {
		msgAdvertencia(""
		        + "Ingrese su identifiacion antes de dar click en el boton SENESCYT!");
		return;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * WEB SERVICE
     * */
    public List<TituloSenescyt> customerClient() throws Exception {

	StringSource requestPayload7 = new StringSource(
	        "<q0:consultaTitulo xmlns:q0=\"http://webServices.titulos.senescyt.gob.ec/\"><arg0>"
	                + this.identificacion + "</arg0></q0:consultaTitulo>");
	SourceExtractor<Object> resultSenescyt = new SourceExtractor<Object>() {

	    @Override
	    public Object extractData(Source source) throws IOException,
		    TransformerException {
		DOMSource domSource = (DOMSource) source;
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		writer.flush();
		respuesta = writer.toString();
		return respuesta;
	    }
	};
	// LOG.info(webServiceClient.getWebServiceTemplate()
	// .getDefaultUri());
	StreamSource source = requestPayload7;
	webServiceClient.getWebServiceTemplate().sendSourceAndReceive(source,
	        resultSenescyt);
	LOG.info("RESPUESTA: " + respuesta);
	List<TituloSenescyt> titulos = procesarRespuestaSenescyt(respuesta);
	// LOG.info("TITULOS: " + titulos.size());
	return titulos;
    }

    private List<TituloSenescyt> procesarRespuestaSenescyt(String cadena) {
	List<TituloSenescyt> titulosReconocidos = new ArrayList<>();
	List<Character> caracteres = new ArrayList<>();
	for (int i = 0; i < cadena.length(); i++) {
	    caracteres.add(cadena.charAt(i));
	}

	for (int i = 0; i < caracteres.size(); i++) {
	    if (caracteres.get(i).charValue() == '/') {
		caracteres.remove(i);
	    }
	}

	String resultado = "";

	for (int i = 0; i < caracteres.size(); i++) {
	    resultado += caracteres.get(i);
	}

	String[] segmentoNivelTitulos = resultado.split("<niveltitulos>");
	String[] nombre = resultado.split("<nombre>");
	if (nombre.length > 1) {
	    nombreDocente = nombre[1];

	    for (int i = 1; i < segmentoNivelTitulos.length; i++) {
		// LOG.info("cantidad segmentos: "
		// + segmentoNivelTitulos.length);
		if ((i % 2) == 1) {
		    if (segmentoNivelTitulos[i].split("<nivel>").length > 1) {
			//
			// if (segmentoNivelTitulos[i].split("<nivel>")[1]
			// .equals("Títulos de Cuarto Nivel")) {
			String[] titulos4 = segmentoNivelTitulos[i]
			        .split("<titulo>");
			for (int j = 1; j < titulos4.length; j++) {
			    if ((j % 2) != 0) {
				titulosReconocidos.add(obtenerNivelesTitulos(
				        titulos4[j], segmentoNivelTitulos[i]
				                .split("<nivel>")[1]));
			    }
			}
			// }
		    }
		}
	    }
	}
	return titulosReconocidos;
    }

    private static TituloSenescyt obtenerNivelesTitulos(String segmentoNivel,
	    String nivel) {

	TituloSenescyt titulo = new TituloSenescyt();

	// nivel = segmentoNivel.split("<nivel>")[1];
	String fechaRegistro = segmentoNivel.split("<fechaRegistro>")[1];
	String ies = segmentoNivel.split("<ies>")[1];
	String nombreTitulo = segmentoNivel.split("<nombreTitulo>")[1];
	String numeroRegistro = segmentoNivel.split("<numeroRegistro>")[1];
	String tipo = segmentoNivel.split("<tipo>")[1];
	// String tipoExtranjero =
	// segmentoNivel.split("<tipoExtrajeroColegio>")[1];
	if (nivel.endsWith("Tercer Nivel")) {
	    titulo.setNivel("TERCER NIVEL");
	} else if (nivel.endsWith("Cuarto Nivel")) {
	    titulo.setNivel("CUARTO NIVEL");
	} else {
	    titulo.setNivel("TECNICO/TECNOLOGO");
	}
	titulo.setFechaRegistro(fechaRegistro);
	titulo.setInstitucion(ies);
	titulo.setNombreTitulo(nombreTitulo);
	titulo.setRegistroSenescyt(numeroRegistro);
	titulo.setTipo(tipo);

	return titulo;
    }

    private static void msgAdvertencia(String msg) {
	FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg,
	        null);
	FacesContext.getCurrentInstance().addMessage(null, fmsg);
    }

    public String getNombreDocente() {
	return nombreDocente;
    }

    public void setNombreDocente(String nombreDocente) {
	this.nombreDocente = nombreDocente;
    }

}