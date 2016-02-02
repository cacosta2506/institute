/**
 * @autor eteran, fecha 30/10/2013
 */
package ec.gob.ceaaces.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ws.client.core.SourceExtractor;
import org.springframework.xml.transform.StringSource;

import template.objetos.Asignatura;
import template.objetos.Carrera;
import template.objetos.ClienteWSCes;
import template.objetos.Cohorte;
import template.objetos.Sede;

import com.thoughtworks.xstream.XStream;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.GradoTituloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.SubAreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.EstadoCarreraEnum;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.OrganizacionCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.RequisitoAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrganizacionMallaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoRequisitoMallaEnum;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.UsuarioIesCarrerasServicio;
import ec.gob.ceaaces.util.JsfUtil;
import ec.gob.ceaaces.utilitarios.SenescytClient;

/**
 * @author eteran
 * 
 */
public class ClientWSController {

    private static final Logger LOG = Logger.getLogger(ClientWSController.class
	    .getSimpleName());

    private File seleccionado;

    private static final char SEP = File.separatorChar;
    // private static final String PATH_CARPETA_IES = SEP + "root" + SEP
    // + "RespaldosWebService" + SEP;

    private static final String PATH_CARPETA_IES = SEP + "home" + SEP
	    + "eteran" + SEP + "Escritorio" + SEP + "RespaldosWebService" + SEP;

    private String carpetaIes;
    private String archivoSeleccionado;
    private String nombreBoton = "btnReadWebService";

    private IesDTO ies;
    private IesDTO iesSeleccionada;
    private List<MallaCurricularDTO> mallas;
    private List<IesDTO> listaIes;
    private String usuarioSistema;
    private String respuesta;
    private Date dateActual;

    private List<String> archivos;

    private StreamedContent respaldoXML;
    private byte[] archivoXML;

    @Autowired
    private UsuarioIesCarrerasServicio uicServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private SenescytClient webServiceClient;

    public ClientWSController() {
	listaIes = new ArrayList<>();
	mallas = new ArrayList<>();
	ies = new IesDTO();
	iesSeleccionada = new IesDTO();
	archivos = new ArrayList<>();
    }

    /**
     * @return the nombreBoton
     */
    public String getNombreBoton() {
	return nombreBoton;
    }

    /**
     * @param nombreBoton
     *            the nombreBoton to set
     */
    public void setNombreBoton(String nombreBoton) {
	this.nombreBoton = nombreBoton;
    }

    /**
     * @return the seleccionado
     */
    public File getSeleccionado() {
	return seleccionado;
    }

    /**
     * @param seleccionado
     *            the seleccionado to set
     */
    public void setSeleccionado(File seleccionado) {
	this.seleccionado = seleccionado;
    }

    /**
     * @param archivoSeleccionado
     *            the archivoSeleccionado to set
     */
    public void setArchivoSeleccionado(String archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

    public String getArchivoSeleccionado() {
	return this.archivoSeleccionado;
    }

    /**
     * @return the carpetaIes
     */
    public String getCarpetaIes() {
	return carpetaIes;
    }

    /**
     * @param carpetaIes
     *            the carpetaIes to set
     */
    public void setCarpetaIes(String carpetaIes) {
	this.carpetaIes = carpetaIes;
    }

    /**
     * @return the archivos
     */
    public List<String> getArchivos() {
	return archivos;
    }

    /**
     * @param archivos
     *            the archivos to set
     */
    public void setArchivos(List<String> archivos) {
	this.archivos = archivos;
    }

    /**
     * @return the respaldoXML
     */
    public StreamedContent getRespaldoXML() {
	return respaldoXML;
    }

    /**
     * @param respaldoXML
     *            the respaldoXML to set
     */
    public void setRespaldoXML(StreamedContent respaldoXML) {
	this.respaldoXML = respaldoXML;
    }

    /**
     * @return the dateActual
     */
    public Date getDateActual() {
	dateActual = new Date();
	return dateActual;
    }

    /**
     * @param dateActual
     *            the dateActual to set
     */
    public void setDateActual(Date dateActual) {
	this.dateActual = dateActual;
    }

    /**
     * @return the iesSeleccionada
     */
    public IesDTO getIesSeleccionada() {
	return iesSeleccionada;
    }

    /**
     * @param iesSeleccionada
     *            the iesSeleccionada to set
     */
    public void setIesSeleccionada(IesDTO iesSeleccionada) {
	this.iesSeleccionada = iesSeleccionada;
    }

    /**
     * @return the listaIes
     */
    public List<IesDTO> getListaIes() {
	return listaIes;
    }

    /**
     * @param listaIes
     *            the listaIes to set
     */
    public void setListaIes(List<IesDTO> listaIes) {
	this.listaIes = listaIes;
    }

    /**
     * @return the mallas
     */
    public List<MallaCurricularDTO> getMallas() {
	return mallas;
    }

    /**
     * @param mallas
     *            the mallas to set
     */
    public void setMallas(List<MallaCurricularDTO> mallas) {
	this.mallas = mallas;
    }

    /**
     * @return the ies
     */
    public IesDTO getIes() {
	return ies;
    }

    /**
     * @param ies
     *            the ies to set
     */
    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    @PostConstruct
    private void context() {
	usuarioSistema = SecurityContextHolder.getContext().getAuthentication()
	        .getName();
    }

    public Date getFechaActual() {
	Date fecha = new Date();
	return fecha;
    }

    /**
     * @return the usuarioSistema
     */
    public String getUsuarioSistema() {
	return usuarioSistema;
    }

    /**
     * @param usuarioSistema
     *            the usuarioSistema to set
     */
    public void setUsuarioSistema(String usuarioSistema) {
	this.usuarioSistema = usuarioSistema;
    }

    /**
     * @return the respuesta
     */
    public String getRespuesta() {
	return respuesta;
    }

    /**
     * @param respuesta
     *            the respuesta to set
     */
    public void setRespuesta(String respuesta) {
	this.respuesta = respuesta;
    }

    public void cargarIes() {
	this.listaIes.clear();
	try {
	    this.listaIes = uicServicio.obtenerIes();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se cargaron las Ies");
	    return;
	}
    }

    // TODO en pruebas
    public void tomarNombreBoton(ActionEvent event) {
	nombreBoton = "";
	FacesContext ctx = FacesContext.getCurrentInstance();
	nombreBoton = event.getComponent().getClientId(ctx).toString();
    }

    // TODO en pruebas
    public void crearDirectorioIes() {

	if (nombreBoton.contains("btnIes")) {
	    seleccionado = null;
	    archivos.clear();
	    mallas.clear();
	}

	if (!carpetaIes.isEmpty()) {
	    File directorioIes = new File(PATH_CARPETA_IES
		    + carpetaIes.toUpperCase());

	    if (!directorioIes.exists()) {
		directorioIes.mkdirs();
	    }

	    if (directorioIes.exists()) {
		cargarArchivos(directorioIes);
	    }
	}
    }

    // TODO en pruebas
    private boolean crearArchivoXML(String template) {
	boolean salida = false;
	if (template != null) {
	    FileWriter fichero = null;
	    PrintWriter pw = null;
	    String nombreArchivoXML = carpetaIes.toLowerCase() + " "
		    + Calendar.getInstance().getTime() + ".xml";
	    try {
		fichero = new FileWriter(PATH_CARPETA_IES
		        + carpetaIes.toUpperCase() + SEP + nombreArchivoXML);
		pw = new PrintWriter(fichero);
		pw.println(template);
		salida = true;
	    } catch (IOException e) {
		e.printStackTrace();
	    } finally {
		if (fichero != null) {
		    try {
			fichero.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	return salida;
    }

    // TODO
    private void cargarArchivos(File directorioIes) {
	archivos.clear();
	if (directorioIes != null) {
	    for (String arch : directorioIes.list()) {
		archivos.add(arch);
	    }
	}
    }

    // TODO
    public void cargarSeleccionado(String nombreArchivo) {
	if (!carpetaIes.isEmpty()) {
	    File root = new File(PATH_CARPETA_IES + carpetaIes);
	    for (File f : root.listFiles()) {
		if (f.isFile()) {
		    if (f.getName().equals(nombreArchivo)) {
			seleccionado = new File(f.getAbsolutePath());
			break;
		    }
		}
	    }
	}
    }

    public void readingWS() {
	try {
	    readWS();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    mallas.clear();
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo guardar la informacion del webservice");
	    return;
	}
    }

    private void readFile() {
	FileReader lector = null;
	try {
	    lector = new FileReader(seleccionado);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	BufferedReader contenido = new BufferedReader(lector);

	StringBuilder template = new StringBuilder();
	String texto = "";

	try {
	    while ((texto = contenido.readLine()) != null) {
		template.append(texto);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	respuesta = template.toString();
    }

    @SuppressWarnings({ "resource" })
    private void readWS() throws IOException {
	if (nombreBoton.contains("btnReadWebService")) {
	    try {
		customerClient();
		LOG.info("WEBSERVICE: " + respuesta);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    // TODO hasta que no se tomen datos del webservice(temporal):
	    File curricula = new File("/home/eteran/Escritorio/templateWS.xml");
	    // File curricula = new File("/root/templateWS.xml");
	    FileReader lector = new FileReader(curricula);
	    BufferedReader contenido = new BufferedReader(lector);
	    StringBuilder template = new StringBuilder();
	    String texto = "";

	    while ((texto = contenido.readLine()) != null) {
		template.append(texto);
	    }

	    respuesta = template.toString();
	    LOG.info("TEMPLATE WS: " + respuesta);

	    if (crearArchivoXML(template.toString())) {
		JsfUtil.msgInfo("Archivo de respaldo fue creado");
	    } else {
		JsfUtil.msgAdvert("No se pudo crear un archivo de respaldo");
	    }
	}

	if (nombreBoton.contains("btnReadFile")) {
	    readFile();
	}

	registrarMalla();
	crearDirectorioIes();
    }

    // TODO en pruebas
    private void registrarMalla() {
	mallas.clear();

	XStream x = new XStream();

	x.processAnnotations(ClienteWSCes.class);

	ClienteWSCes clienteWS = (ClienteWSCes) x.fromXML(respuesta);

	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setUsuarioModificacion(usuarioSistema);

	// Datos de la ies:
	IesDTO iesDTO = new IesDTO();
	iesDTO.setCodigo(clienteWS.getCodeIES());
	iesDTO.setNombre(clienteWS.getNombreIES());

	ies = iesDTO;

	for (Sede sede : clienteWS.getSedeData()) {

	    // Datos de la sede:
	    SedeIesDTO sedeDTO = new SedeIesDTO();
	    sedeDTO.setCodigo(sede.getCodeSede());
	    // sedeDTO.setTipoSede(sede.getNomTipoSede());
	    sedeDTO.setActivo(true);

	    for (Carrera car : sede.getCarreraData()) {

		// Datos de la carrera:
		CarreraIesDTO carreraDTO = new CarreraIesDTO();
		carreraDTO.setCodigo(car.getCodeCarrera());
		carreraDTO.setCodigoCarrera(car.getCodeCarrera());
		carreraDTO.setNombre(car.getNomCarrera());
		carreraDTO.setNivel(car.getNomNivelCarrera());
		carreraDTO.setCodigoIes(iesDTO.getCodigo());
		carreraDTO.setEstado(EstadoCarreraEnum.parse(car
		        .getNomEstadoCarrera().trim()));
		carreraDTO.setActivo(true);

		InformacionCarreraDTO informacionCarreraDTO = new InformacionCarreraDTO();
		// informacionCarreraDTO.setModalidad(ModalidadEnum.parse(car
		// .getNomModalidad()));
		// TODO
		// informacionCarreraDTO.setEstado(EstadoCarreraEnum.parse(car
		// .getNomEstadoCarrera()));
		// informacionCarreraDTO.setCarreraIesDTO(carreraDTO);
		informacionCarreraDTO.setSedeIesDTO(sedeDTO);

		GradoTituloDTO nivelDTO = new GradoTituloDTO();
		nivelDTO.setNombre(car.getNomNivelCarrera());

		informacionCarreraDTO.setGradoTituloDTO(nivelDTO);

		for (Cohorte cd : car.getCohorteData()) {

		    // Datos de la malla curricular (cohorte):
		    MallaCurricularDTO mallaDTO = new MallaCurricularDTO();

		    mallaDTO.setCodigoUnico(cd.getCodeCohorte());
		    mallaDTO.setNumeroNiveles(Integer.valueOf(cd
			    .getNumeroPeriodosCohorte().trim()));

		    SimpleDateFormat formatoDelTexto = new SimpleDateFormat(
			    "yyyy-MM-dd");
		    String fechaI = cd.getInicioCohorte();
		    String fechaF = cd.getFinCohorte();

		    Date fechaInicio = null;
		    Date fechaFin = null;

		    try {
			fechaInicio = formatoDelTexto.parse(fechaI);
			fechaFin = formatoDelTexto.parse(fechaF);
		    } catch (ParseException e) {
			e.printStackTrace();
			return;
		    }

		    mallaDTO.setFechaInicioVigencia(fechaInicio);
		    mallaDTO.setFechaFinVigencia(fechaFin);
		    mallaDTO.setInformacionCarreraDTO(informacionCarreraDTO);
		    // mallaDTO.setNumCreditosSinTesis(Integer.valueOf(cd
		    // .getCreditosSinTesis()));
		    // mallaDTO.setNumCreditosConTesis(Integer.valueOf(cd
		    // .getCreditosConTesis()));
		    // mallaDTO.setMesesSinTesis(Integer.valueOf(cd
		    // .getMesesSinTesis()));
		    // mallaDTO.setMesesConTesis(Integer.valueOf(cd
		    // .getMesesConTesis()));
		    mallaDTO.setOrganizacionMallaEnum(OrganizacionMallaEnum
			    .valueOf(cd.getTipoCohorte().getNombreTipoCohorte()
			            .trim()));
		    mallaDTO.setActivo(true);

		    // Auditoria:
		    mallaDTO.setAuditoria(auditoriaDTO);

		    mallas.add(mallaDTO);

		    List<OrganizacionCurricularDTO> organizacionCurricularDTO = new ArrayList<>();

		    OrganizacionCurricularDTO basicoDTO = new OrganizacionCurricularDTO();
		    basicoDTO.setEjeTransversal(cd.getBasico()
			    .getEjesTransversales());
		    // basicoDTO.setOrganizacionDelAprendizaje(Integer.valueOf(cd
		    // .getBasico().getCode()));

		    organizacionCurricularDTO.add(basicoDTO);

		    OrganizacionCurricularDTO profesionalDTO = new OrganizacionCurricularDTO();
		    profesionalDTO.setEjeTransversal(cd.getProfesional()
			    .getEjesTransversales());
		    // profesionalDTO.setOrganizacionDelAprendizaje(Integer
		    // .valueOf(cd.getProfesional().getCode()));

		    organizacionCurricularDTO.add(profesionalDTO);

		    OrganizacionCurricularDTO titulacionDTO = new OrganizacionCurricularDTO();
		    titulacionDTO.setEjeTransversal(cd.getTitulacion()
			    .getEjesTransversales());
		    // titulacionDTO.setOrganizacionDelAprendizaje(Integer
		    // .valueOf(cd.getTitulacion().getCode()));

		    organizacionCurricularDTO.add(titulacionDTO);

		    mallaDTO.getOrganizacionesCurricularDTO().addAll(
			    organizacionCurricularDTO);

		    List<AsignaturaDTO> asignaturasDTO = new ArrayList<>();

		    for (Asignatura asig : cd.getAsignaturaData()) {

			AsignaturaDTO asignaturaDTO = new AsignaturaDTO();
			asignaturaDTO.setActivo(true);
			asignaturaDTO.setCodigo(asig.getCodeUnicoAsignatura());
			// asignaturaDTO.setNivelFormacionEnum(NivelFormacionEnum
			// .parse(asig.getNomNivelFormacionAsig().trim()));
			// asignaturaDTO.setNumeroCreditos(Integer.valueOf(asig
			// .getCreditosAsignatura()));
			asignaturaDTO.setAreaFormacion(asig
			        .getNomAreaFormacionAsig());
			asignaturaDTO.setAuditoria(auditoriaDTO);

			// Datos de subarea de conocimiento:
			SubAreaConocimientoDTO subAreaDTO = new SubAreaConocimientoDTO();
			subAreaDTO.setNombre(asig.getNomAreaAsig());
			asignaturaDTO.setSubAreaConocimientoDTO(subAreaDTO);

			List<RequisitoAsignaturaDTO> pres = new ArrayList<>();

			for (String codePre : asig.getPrerrequisitos()
			        .getPrerrequisito()) {
			    if (!codePre.isEmpty()) {
				AsignaturaDTO asigDTO = new AsignaturaDTO();
				asigDTO.setCodigo(codePre);
				RequisitoAsignaturaDTO preRequisito = new RequisitoAsignaturaDTO();
				preRequisito
				        .setTipoEnum(TipoRequisitoMallaEnum.PRE);
				preRequisito.setAsignaturaRequisitoDTO(asigDTO);
				pres.add(preRequisito);
			    }
			}

			if (!pres.isEmpty()) {
			    asignaturaDTO.getPreRequisitoMalla().addAll(pres);
			}

			List<RequisitoAsignaturaDTO> cors = new ArrayList<>();

			for (String codeCo : asig.getCorrequisitos()
			        .getCorrequisito()) {
			    if (!codeCo.isEmpty()) {
				AsignaturaDTO asigDTO = new AsignaturaDTO();
				asigDTO.setCodigo(codeCo);
				RequisitoAsignaturaDTO corRequisito = new RequisitoAsignaturaDTO();
				corRequisito
				        .setTipoEnum(TipoRequisitoMallaEnum.COR);
				corRequisito.setAsignaturaRequisitoDTO(asigDTO);
				cors.add(corRequisito);
			    }
			}

			if (!cors.isEmpty()) {
			    asignaturaDTO.getCorRequisitoMalla().addAll(cors);
			}

			asignaturasDTO.add(asignaturaDTO);
		    }

		    mallaDTO.getAsignaturasDTO().addAll(asignaturasDTO);

		}
	    }
	}

	if (!mallas.isEmpty()) {
	    JsfUtil.msgInfo("Los datos de las mallas curriculares se registraron conrrectamente");
	} else {
	    JsfUtil.msgAdvert("No hay datos del webservice");
	}

	iesSeleccionada = new IesDTO();
    }

    /**
     * WEB SERVICE
     * */
    private void customerClient() throws Exception {
	StringSource requestPayload7 = new StringSource(
	        "<q0:consultaTitulo xmlns:q0=\"http://webServices.titulos.senescyt.gob.ec/\"><arg0>"
	                + 1703751816 + "</arg0></q0:consultaTitulo>");
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
	// LOG.info(webServiceClient.getWebServiceTemplate().getDefaultUri());
	StreamSource source = requestPayload7;
	webServiceClient.getWebServiceTemplate().sendSourceAndReceive(source,
	        resultSenescyt);
    }

}
