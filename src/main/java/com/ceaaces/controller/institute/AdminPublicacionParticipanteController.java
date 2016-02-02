package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.data.ItemEvidenciaLocal;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocentePublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoPublicacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminPublicacionParticipanteController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminPublicacionParticipanteController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private InstitutosServicio institutoServicio;

    private String usuario;
    private String nada;
    private String[] tipoEnum;
    private String tipoSeleccionado;
    private Boolean panelArticulo;
    private Boolean panelLibro;
    private Boolean panelCapituloLibro;
    private Boolean panelOtroTipo;
    private String participacionSeleccionado;
    private String perfil;

    private PublicacionDTO publicacionDto;
    private DocenteDTO docenteDto;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private PublicacionDTO publicacionSeleccionada;

    private List<PublicacionDTO> listaPublicacionDto;
    private List<DocentePublicacionDTO> listaDocentePublicacionDto;
    private List<DocenteDTO> listaDocenteDto;
    private List<String> listaParticipacionCombo;
    private List<DocenteDTO> listaDocentes;

    // Evidencia
    private List<ItemEvidenciaLocal> listaItemEvidencias;
    private FaseDTO faseEvaluacion;
    private Evidencia archivoSeleccionado;
    private FaseIesDTO faseIesDTO;

    private List<DocentePublicacionDTO> vistaParticipaciones;
    private List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto;

    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private StreamedContent documentoDescarga;
    private Long idEvidencia;
    private Boolean alertaEvaluador = false;

    public AdminPublicacionParticipanteController() {

	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	publicacionDto = new PublicacionDTO();
	docenteDto = new DocenteDTO();
	panelArticulo = new Boolean(false);
	panelLibro = new Boolean(false);
	panelOtroTipo = new Boolean(false);
	faseEvaluacion = new FaseDTO();
	archivoSeleccionado = new Evidencia();
	publicacionSeleccionada = new PublicacionDTO();

	listaPublicacionDto = new ArrayList<PublicacionDTO>();
	listaDocentePublicacionDto = new ArrayList<DocentePublicacionDTO>();
	listaDocenteDto = new ArrayList<DocenteDTO>();
	listaParticipacionCombo = new ArrayList<String>();
	listaItemEvidencias = new ArrayList<ItemEvidenciaLocal>();
	vistaParticipaciones = new ArrayList<DocentePublicacionDTO>();
	listaDocentes = new ArrayList<DocenteDTO>();
	listaEvidenciaConceptoDto = new ArrayList<EvidenciaConceptoDTO>();
    }

    @PostConstruct
    public void start() {

	try {
	    obtenerInformacionIes();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void obtenerInformacionIes() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.alertaEvaluador = false;

	    this.iesDTO = controller.getIes();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.perfil = controller.getPerfil().getNombre();

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    iniciarValores();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarEvidencia() {

	try {
	    idEvidencia = publicacionDto.getId();
	    LOG.info("id publicacion: " + idEvidencia.toString());
	    listaEvidenciaConceptoDto.clear();
	    listaEvidenciaConceptoDto = institutoServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), 9L, idEvidencia, "publicaciones");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void guardarPublicacion() {

	if (!validarDatos(publicacionDto)) {
	    return;
	}

	if (listaDocentePublicacionDto.isEmpty()) {
	    JsfUtil.msgAdvert("Debe ingresar por lo menos un participante");
	    return;
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	publicacionDto.setAuditoriaDTO(auditoria);

	publicacionDto.setActivo(true);

	publicacionDto.setTipo(TipoPublicacionEnum.parse(tipoSeleccionado));

	publicacionDto.setInformacionIesDTO(informacionIesDto);
	publicacionDto.setFaseIesDTO(faseIesDTO);

	publicacionDto
	        .setListaDocentePublicacionDTO(listaDocentePublicacionDto);

	try {
	    publicacionDto = registroServicio
		    .registroPublicacion(publicacionDto);
	} catch (ServicioException e) {
	    JsfUtil.msgError("No se pudo almacenar la publicación: "
		    + e.getMessage());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
	if (null != publicacionDto.getId()) {
	    JsfUtil.msgInfo("Registro almacenado correctamente");

	    regresarPublicacion();
	}

    }

    public boolean validarDatos(PublicacionDTO publicacion) {

	if (null == tipoSeleccionado || tipoSeleccionado.equals("")) {
	    JsfUtil.msgError("Seleccione el tipo");
	    return false;
	}

	if (null == publicacion.getTitulo()
	        || publicacion.getTitulo().equals("")) {
	    JsfUtil.msgError("Ingrese el Nombre Libro Revista");
	    return false;
	}

	if (null == publicacion.getFechaPublicacion()) {
	    JsfUtil.msgError("Ingrese la Fecha de Publicación");
	    return false;
	}

	if (tipoSeleccionado.equals("ARTICULO")) {
	    if (null == publicacion.getNombreRevista()
		    || publicacion.getNombreRevista().equals("")) {
		JsfUtil.msgError("Ingrese el nombre de la revista");
		return false;
	    }
	}
	return true;
    }

    public void regresarPublicacion() {
	limpiarDatos();
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();

	AdministrarPublicacionesController controller = (AdministrarPublicacionesController) ec
	        .getSessionMap().get("publicacionController");
	controller.recargarPublicaciones();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/publicaciones/administracionPublicaciones.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void limpiarDatos() {
	panelArticulo = false;
	panelLibro = false;
	panelOtroTipo = false;
	publicacionDto = new PublicacionDTO();
	tipoSeleccionado = "";
	listaDocentePublicacionDto.clear();
	listaDocenteDto.clear();
    }

    public void seleccionarPanel() {
	publicacionDto = new PublicacionDTO();

	if (tipoSeleccionado.equals("")) {
	    panelLibro = false;
	    panelArticulo = false;
	    panelOtroTipo = false;
	    panelCapituloLibro = false;
	} else if (tipoSeleccionado.equals("ARTICULO")) {
	    panelArticulo = true;
	    panelLibro = false;
	    panelOtroTipo = false;
	    panelCapituloLibro = false;
	} else if (tipoSeleccionado.equals("LIBRO")) {
	    panelLibro = true;
	    panelArticulo = false;
	    panelOtroTipo = false;
	    panelCapituloLibro = false;
	} else if (tipoSeleccionado.equals("CAPITULO LIBRO")) {
	    panelCapituloLibro = true;
	    panelLibro = false;
	    panelArticulo = false;
	    panelOtroTipo = false;
	} else {
	    panelLibro = false;
	    panelArticulo = false;
	    panelOtroTipo = true;
	    panelCapituloLibro = false;
	}
    }

    public void iniciarValores() {

	obtenerParticipacionCombo();

	tipoEnum = new String[4];
	tipoEnum[0] = TipoPublicacionEnum.ARTICULO.getValue();
	tipoEnum[1] = TipoPublicacionEnum.LIBRO.getValue();
	tipoEnum[2] = TipoPublicacionEnum.CAPITULO_LIBRO.getValue();
	tipoEnum[3] = TipoPublicacionEnum.OTRO.getValue();

    }

    public void nuevaParticipacion() {
	docenteDto = new DocenteDTO();
	participacionSeleccionado = "";
	vistaParticipaciones.clear();

	obtenerDocentes();

    }

    public void obtenerDocentes() {

	try {
	    DocenteDTO docenteDTO = null;
	    listaDocentes = registroServicio.obtenerDocentesPorInformacionIes(
		    docenteDTO, informacionIesDto.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void obtenerParticipacionCombo() {
	listaParticipacionCombo.clear();
	listaParticipacionCombo.add("Autor");
	listaParticipacionCombo.add("Coautor");
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {
	if (null == evidencia)
	    return;

	LOG.info(evidencia.getNombreArchivo());
	fichero = evidencia.getNombreArchivo();
	url = evidencia.getUrl();
	String[] nombreFichero = evidencia.getNombreArchivo().split("[.]");

	extensionDocumento = nombreFichero[nombreFichero.length - 1];
	LOG.info(extensionDocumento);
	LOG.info(url);
	descargar = true;
	return;
    }

    //
    public void agregarListaParticipantes() {
	if (!validarDatosParticipante(docenteDto)) {
	    return;
	}

	for (int i = 0; i < vistaParticipaciones.size(); i++) {
	    if (vistaParticipaciones.get(i).getDocenteDTO().getId()
		    .equals(docenteDto.getId())) {
		vistaParticipaciones.remove(i);
		break;
	    }
	}

	DocentePublicacionDTO docente = new DocentePublicacionDTO();

	docente.setDocenteDTO(docenteDto);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	docente.setAuditoriaDTO(auditoria);

	docente.setActivo(true);
	docente.setParticipacion(this.participacionSeleccionado);
	docente.setFaseIesDTO(this.faseIesDTO);
	listaDocentePublicacionDto.add(docente);
	vistaParticipaciones.add(docente);

	docenteDto = new DocenteDTO();
	participacionSeleccionado = null;
    }

    //
    public void agregarParticipante() {
	// listaDocentePublicacionDto.clear();
	// for (DocentePublicacionDTO participacion : vistaParticipaciones) {
	// listaDocentePublicacionDto.add(participacion);
	// }

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgParticipacion.hide()");

	JsfUtil.msgInfo("Participante agregado, No se olvide de Guardar toda la información");
    }

    public boolean validarDatosParticipante(DocenteDTO docent) {

	if (null == docent.getId()) {
	    JsfUtil.msgError("Seleccione un Docente");
	    return false;
	}

	if (null == participacionSeleccionado
	        || participacionSeleccionado.equals("")) {
	    JsfUtil.msgError("Seleccione la participación");
	    return false;
	}

	for (DocentePublicacionDTO participacion : listaDocentePublicacionDto) {
	    if (participacion.getDocenteDTO().getId()
		    .equals(docenteDto.getId())) {
		JsfUtil.msgError("El docente ya ha sido asignado");
		return false;
	    }
	}
	//
	// if (null == docent.getAnio()) {
	// msgError("Ingrese el año");
	// return false;
	// }
	//
	// if (!Util.validarEsNumerico(docent.getAnio().toString())) {
	// msgAdvert("El año debe ser númerico");
	// return false;
	// }
	//
	// if (!Util.validarEsPositivo(docent.getAnio().toString())) {
	// msgAdvert("El valor del año debe ser positivo");
	// return false;
	// }

	return true;
    }

    /**
     * @return the usuario
     */
    public String getUsuario() {
	return usuario;
    }

    /**
     * @param usuario
     *            the usuario to set
     */
    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    /**
     * @return the publicacionDto
     */
    public PublicacionDTO getPublicacionDto() {
	return publicacionDto;
    }

    /**
     * @param publicacionDto
     *            the publicacionDto to set
     */
    public void setPublicacionDto(PublicacionDTO publicacionDto) {
	this.publicacionDto = publicacionDto;
    }

    /**
     * @return the listaPublicacionDto
     */
    public List<PublicacionDTO> getListaPublicacionDto() {
	return listaPublicacionDto;
    }

    /**
     * @param listaPublicacionDto
     *            the listaPublicacionDto to set
     */
    public void setListaPublicacionDto(List<PublicacionDTO> listaPublicacionDto) {
	this.listaPublicacionDto = listaPublicacionDto;
    }

    /**
     * @return the tipoEnum
     */
    public String[] getTipoEnum() {
	return tipoEnum;
    }

    /**
     * @param tipoEnum
     *            the tipoEnum to set
     */
    public void setTipoEnum(String[] tipoEnum) {
	this.tipoEnum = tipoEnum;
    }

    /**
     * @return the tipoSeleccionado
     */
    public String getTipoSeleccionado() {
	return tipoSeleccionado;
    }

    /**
     * @param tipoSeleccionado
     *            the tipoSeleccionado to set
     */
    public void setTipoSeleccionado(String tipoSeleccionado) {
	this.tipoSeleccionado = tipoSeleccionado;
    }

    /**
     * @return the nada
     */
    public String getNada() {
	return nada;
    }

    /**
     * @param nada
     *            the nada to set
     */
    public void setNada(String nada) {
	this.nada = nada;
    }

    /**
     * @return the panelArticulo
     */
    public Boolean getPanelArticulo() {
	return panelArticulo;
    }

    /**
     * @param panelArticulo
     *            the panelArticulo to set
     */
    public void setPanelArticulo(Boolean panelArticulo) {
	this.panelArticulo = panelArticulo;
    }

    /**
     * @return the panelLibro
     */
    public Boolean getPanelLibro() {
	return panelLibro;
    }

    /**
     * @param panelLibro
     *            the panelLibro to set
     */
    public void setPanelLibro(Boolean panelLibro) {
	this.panelLibro = panelLibro;
    }

    /**
     * @return the listaDocentePublicacionDto
     */
    public List<DocentePublicacionDTO> getListaDocentePublicacionDto() {
	return listaDocentePublicacionDto;
    }

    /**
     * @param listaDocentePublicacionDto
     *            the listaDocentePublicacionDto to set
     */
    public void setListaDocentePublicacionDto(
	    List<DocentePublicacionDTO> listaDocentePublicacionDto) {
	this.listaDocentePublicacionDto = listaDocentePublicacionDto;
    }

    /**
     * @return the listaDocenteDto
     */
    public List<DocenteDTO> getListaDocenteDto() {
	return listaDocenteDto;
    }

    /**
     * @param listaDocenteDto
     *            the listaDocenteDto to set
     */
    public void setListaDocenteDto(List<DocenteDTO> listaDocenteDto) {
	this.listaDocenteDto = listaDocenteDto;
    }

    /**
     * @return the docenteDto
     */
    public DocenteDTO getDocenteDto() {
	return docenteDto;
    }

    /**
     * @param docenteDto
     *            the docenteDto to set
     */
    public void setDocenteDto(DocenteDTO docenteDto) {
	this.docenteDto = docenteDto;
    }

    /**
     * @return the listaParticipacionCombo
     */
    public List<String> getListaParticipacionCombo() {
	return listaParticipacionCombo;
    }

    /**
     * @param listaParticipacionCombo
     *            the listaParticipacionCombo to set
     */
    public void setListaParticipacionCombo(List<String> listaParticipacionCombo) {
	this.listaParticipacionCombo = listaParticipacionCombo;
    }

    /**
     * @return the participacionSeleccionado
     */
    public String getParticipacionSeleccionado() {
	return participacionSeleccionado;
    }

    /**
     * @param participacionSeleccionado
     *            the participacionSeleccionado to set
     */
    public void setParticipacionSeleccionado(String participacionSeleccionado) {
	this.participacionSeleccionado = participacionSeleccionado;
    }

    /**
     * @return the informacionIesDto
     */
    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    /**
     * @param informacionIesDto
     *            the informacionIesDto to set
     */
    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public PublicacionDTO getPublicacionSeleccionada() {
	return publicacionSeleccionada;
    }

    public void setPublicacionSeleccionada(
	    PublicacionDTO publicacionSeleccionada) {
	this.publicacionSeleccionada = publicacionSeleccionada;
    }

    public List<ItemEvidenciaLocal> getListaItemEvidencias() {
	return listaItemEvidencias;
    }

    public void setListaItemEvidencias(
	    List<ItemEvidenciaLocal> listaItemEvidencias) {
	this.listaItemEvidencias = listaItemEvidencias;
    }

    public FaseDTO getFaseEvaluacion() {
	return faseEvaluacion;
    }

    public void setFaseEvaluacion(FaseDTO faseEvaluacion) {
	this.faseEvaluacion = faseEvaluacion;
    }

    public Evidencia getArchivoSeleccionado() {
	return archivoSeleccionado;
    }

    public void setArchivoSeleccionado(Evidencia archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public Date getFechaActual() {
	return new Date();
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public List<DocentePublicacionDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    public void setVistaParticipaciones(
	    List<DocentePublicacionDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

    public Boolean getPanelOtroTipo() {
	return panelOtroTipo;
    }

    public void setPanelOtroTipo(Boolean panelOtroTipo) {
	this.panelOtroTipo = panelOtroTipo;
    }

    public List<DocenteDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<DocenteDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConceptoDto() {
	return listaEvidenciaConceptoDto;
    }

    public void setListaEvidenciaConceptoDto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto) {
	this.listaEvidenciaConceptoDto = listaEvidenciaConceptoDto;
    }

    public String getFichero() {
	return fichero;
    }

    public void setFichero(String fichero) {
	this.fichero = fichero;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
    }

    public StreamedContent getDocumentoDescarga() {
	if (descargar) {
	    return ArchivoUtil
		    .obtenerDescarga(url, fichero, extensionDocumento);
	} else {
	    JsfUtil.msgError("No se pudo descargar el documento");
	    return null;
	}
    }

    public void setDocumentoDescarga(StreamedContent documentoDescarga) {
	this.documentoDescarga = documentoDescarga;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public void setPanelCapituloLibro(Boolean panelCapituloLibro) {
	this.panelCapituloLibro = panelCapituloLibro;
    }

    public Boolean getPanelCapituloLibro() {
	return panelCapituloLibro;
    }

}