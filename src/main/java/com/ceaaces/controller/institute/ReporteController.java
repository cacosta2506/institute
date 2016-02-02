/*
 * 
 * Desarrollado por: cam
 * 
 * Copyright (c) 2013-2013 CEAACESS All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of CEAACESS.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with CEAACES.
 * 
 * CEAACES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 */
package ec.gob.ceaaces.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.util.JsfUtil;

/**
 * Class description goes here.
 * 
 * @author cam
 * 
 */

public class ReporteController implements Serializable {

    /**
     * classVar1 documentation comment serialVersionUID
     */

    private static final long serialVersionUID = 8076107438688720711L;

    private static final Logger LOG = Logger.getLogger(ReporteController.class
	    .getSimpleName());

    private MallaCurricularDTO malla;
    private StreamedContent documentoDescarga;
    private Boolean descargar = false;
    private String url;
    private String archivo;
    private String auxiliar;

    public ReporteController() {
	this.malla = new MallaCurricularDTO();
	auxiliar = "";
    }

    public void generarReporte() {

	LOG.info("MALLA" + malla.getId());
	String[] placeholder = new String[5];
	String[] toAdd = new String[5];

	placeholder[0] = "PERIODO1";
	placeholder[1] = "AÑOS1";
	placeholder[2] = "AÑOS2";
	placeholder[3] = "CREDITOS1";
	placeholder[4] = "CREDITOS2";

	// placeholder[9] = "ITEM9 NOMBRE UNIVERSIDAD";

	toAdd[0] = "2013-2014";
	toAdd[1] = "5 Años";
	toAdd[2] = "3 Años";
	toAdd[3] = "3";
	toAdd[4] = "4";

	// toAdd[9] = "asdfasdfasdfas";

	ProcesadorMallaCurricular procesador = new ProcesadorMallaCurricular();
	WordprocessingMLPackage template = null;
	try {
	    template = procesador.getTemplate();
	} catch (FileNotFoundException | Docx4JException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	for (int i = 0; i < toAdd.length; i++) {
	    procesador.replaceParagraph(placeholder[i], toAdd[i], template,
		    template.getMainDocumentPart());
	}

	try {
	    String AsignaturaB = "";
	    String ABformacion1 = "";
	    for (int i = 0; i < 5; i++) {
		AsignaturaB = "Asignatura: " + "Asignatura" + i + "\n"
		        + "NoCreditos: " + i + "\n" + "PRE: " + "Q98" + i
		        + "\n" + "COREQ: " + "CORE" + i + "\n";
		ABformacion1 += AsignaturaB + "\n\n";
	    }
	    String Nivel1 = "Eje Transversal: " + "\n\n"
		    + "Nivel Organización Aprendizaje: ";
	    Map<String, String> fila1Tabla1 = new HashMap<String, String>();
	    fila1Tabla1.put("Nivel1", Nivel1);
	    fila1Tabla1.put("ABformacion1", ABformacion1);
	    fila1Tabla1.put("ABformacion2", "");
	    fila1Tabla1.put("ABformacion3", "");
	    fila1Tabla1.put("ABformacion4", "");
	    fila1Tabla1.put("ABformacion5", "");

	    List<Map<String, String>> lista = new ArrayList<Map<String, String>>();

	    lista.add(fila1Tabla1);

	    procesador.replaceTablelist(new String[] { "Nivel1",
		    "ABformacion1", "ABformacion2", "ABformacion3",
		    "ABformacion4", "ABformacion5" }, Arrays.asList(lista),
		    template);

	    String AsignaturaP = "";
	    String APformacion1 = "";
	    for (int i = 0; i < 7; i++) {
		AsignaturaP = "Asignatura: " + "Asignatura" + i + "\n"
		        + "NoCreditos: " + i + "\n" + "PRE: " + "Q98" + i
		        + "\n" + "COREQ: " + "CORE" + i + "\n";
		APformacion1 += AsignaturaP + "\n\n";
	    }
	    String Nivel2 = "Eje Transversal: " + "\n\n"
		    + "Nivel Organización Aprendizaje: ";
	    Map<String, String> fila1Tabla2 = new HashMap<String, String>();
	    fila1Tabla2.put("Nivel2", Nivel2);
	    fila1Tabla2.put("APformacion1", APformacion1);
	    fila1Tabla2.put("APformacion2", "");
	    fila1Tabla2.put("APformacion3", "");
	    fila1Tabla2.put("APformacion4", "");
	    fila1Tabla2.put("APformacion5", "");

	    List<Map<String, String>> lista1 = new ArrayList<Map<String, String>>();

	    lista1.add(fila1Tabla2);

	    procesador.replaceTablelist(new String[] { "Nivel2",
		    "APformacion1", "APformacion2", "APformacion3",
		    "APformacion4", "APformacion5" }, Arrays.asList(lista1),
		    template);

	    String AsignaturaT = "";
	    String ATformacion1 = "";
	    for (int i = 0; i < 5; i++) {
		AsignaturaT = "Asignatura: " + "Asignatura" + i + "\n"
		        + "NoCreditos: " + i + "\n" + "PRE: " + "Q98" + i
		        + "\n" + "COREQ: " + "CORE" + i + "\n";
		ATformacion1 += AsignaturaT + "\n\n";
	    }
	    String Nivel3 = "Eje Transversal: " + "\n\n"
		    + "Nivel Organización Aprendizaje: ";
	    Map<String, String> fila1Tabla3 = new HashMap<String, String>();
	    fila1Tabla3.put("Nivel3", Nivel3);
	    fila1Tabla3.put("ATformacion1", ATformacion1);
	    fila1Tabla3.put("ATformacion2", "");
	    fila1Tabla3.put("ATformacion3", "");
	    fila1Tabla3.put("ATformacion4", "");
	    fila1Tabla3.put("ATformacion5", "");

	    List<Map<String, String>> lista2 = new ArrayList<Map<String, String>>();

	    lista2.add(fila1Tabla3);

	    procesador.replaceTablelist(new String[] { "Nivel3",
		    "ATformacion1", "ATformacion2", "ATformacion3",
		    "ATformacion4", "ATformacion5" }, Arrays.asList(lista2),
		    template);
	    Date date = new Date();
	    DateFormat hourdateFormat = new SimpleDateFormat("ddMMYYHHmmss");

	    archivo = malla.getCodigoUnico() + "-MALLA" + "-"
		    + hourdateFormat.format(date).toString();
	    url = "/tmp/";
	    String direccion = url + archivo + ".docx";
	    descargar = true;
	    procesador.writeDocxToStream(template, direccion);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Docx4JException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    // } catch (JAXBException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }

	} catch (JAXBException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public MallaCurricularDTO getMalla() {
	return malla;
    }

    public void setMalla(MallaCurricularDTO malla) {
	this.malla = malla;
    }

    public StreamedContent getDocumentoDescarga() {
	if (descargar) {
	    File doc = new File(url + archivo + ".docx");

	    String mimeTypes = obtenerMimeTypes("docx");
	    FileInputStream fis = null;
	    try {
		fis = new FileInputStream(doc);
		byte[] bloque = new byte[(int) doc.length()];
		fis.read(bloque);

		ByteArrayInputStream bais = new ByteArrayInputStream(bloque);

		InputStream is = bais;

		documentoDescarga = new DefaultStreamedContent(is, mimeTypes,
		        archivo + ".docx");

	    } catch (IOException e) {
		e.printStackTrace();
	    } finally {
		try {
		    fis.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	    doc.delete();
	    return documentoDescarga;
	} else {
	    JsfUtil.msgError("No se pudo descargar el documento");
	    return null;
	}
    }

    public void setDocumentoDescarga(StreamedContent documentoDescarga) {
	this.documentoDescarga = documentoDescarga;
    }

    public String obtenerMimeTypes(String extencion) {
	String minesTypes = null;

	// pdf
	if (extencion.equals("PDF") || extencion.equals("pdf")) {
	    minesTypes = "application/pdf";
	    return minesTypes;
	}

	// docx
	if (extencion.equals("docx") || extencion.equals("DOCX")) {
	    minesTypes = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	    return minesTypes;
	}

	// doc
	if (extencion.equals("doc") || extencion.equals("DOC")) {
	    minesTypes = "application/msword";
	    return minesTypes;
	}

	// xls
	if (extencion.equals("xls") || extencion.equals("XLS")) {
	    minesTypes = "application/vnd.ms-excel";
	    return minesTypes;
	}

	// xlsx
	if (extencion.equals("xlsx") || extencion.equals("XLSX")) {
	    minesTypes = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	    return minesTypes;
	}

	// ppt
	if (extencion.equals("ppt") || extencion.equals("PPT")) {
	    minesTypes = "application/vnd.ms-powerpoint";
	    return minesTypes;
	}

	// pptx
	if (extencion.equals("pptx") || extencion.equals("PPTX")) {
	    minesTypes = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	    return minesTypes;
	}

	// zip
	if (extencion.equals("zip") || extencion.equals("ZIP")) {
	    minesTypes = "application/zip";
	    return minesTypes;
	}

	// rar
	if (extencion.equals("rar")) {
	    minesTypes = "application/x-rar-compressed";
	    return minesTypes;
	}

	// rss, .xml
	if (extencion.equals("rss") || extencion.equals("xml")) {
	    minesTypes = "application/rss+xml";
	    return minesTypes;
	}

	// .csv
	if (extencion.equals("csv")) {
	    minesTypes = "text/csv";
	    return minesTypes;
	}

	// .jpeg, .jpg
	if (extencion.equals("jpeg") || extencion.equals("jpg")) {
	    minesTypes = "image/jpeg";
	    return minesTypes;
	}

	// .png
	if (extencion.equals("png")) {
	    minesTypes = "image/png";
	    return minesTypes;
	}

	// // .wma
	// minesTypes = "audio/x-ms-wma";
	// // .flv
	// minesTypes = "video/x-flv";
	//
	// // .flv
	// minesTypes = "";
	//
	// // .html
	// minesTypes = "text/html";+ // .swf
	// minesTypes = "application/x-shockwave-flash";

	return minesTypes;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getArchivo() {
	return archivo;
    }

    public void setArchivo(String archivo) {
	this.archivo = archivo;
    }

    public String getAuxiliar() {
	return auxiliar;
    }

    public void setAuxiliar(String auxiliar) {
	this.auxiliar = auxiliar;
    }

}
