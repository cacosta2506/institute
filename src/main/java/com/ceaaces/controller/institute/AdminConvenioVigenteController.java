package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ConvenioVigenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ConvenioVigenteHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "convenioController")
public class AdminConvenioVigenteController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminConvenioVigenteController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    private ConvenioVigenteDTO convenioVigenteDTO;
    private ConvenioVigenteDTO convenioSeleccionado;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private FaseIesDTO faseIesDTO;
    private ConceptoDTO conceptoSeleccionado;

    private List<ConvenioVigenteDTO> listaConvenioVigenteDTO;
    private List<ConvenioVigenteHistoricoDTO> listaConvenioHistorico;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;

    private String usuario;
    // private List<ConceptoDTO> listaEvidencia;
    private ConceptoDTO conceptoDTO;
    private Long idInformacionIes;
    private String extensionDocumento;
    private StreamedContent documentoDescarga;
    private String fichero;
    private Boolean descargar = false;
    private String url;
    private Long idProceso;
    private Long idSeleccionado;
    private String nombreFichero;
    private Boolean evidenciaConcepto = false;
    private String perfil;
    private String fase;
    private Long idEvidencia;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    private Integer totalConvenios = 0;
    private Date fechaActual;

    public AdminConvenioVigenteController() {
	listaConvenioVigenteDTO = new ArrayList<ConvenioVigenteDTO>();

	convenioVigenteDTO = new ConvenioVigenteDTO();
	convenioSeleccionado = new ConvenioVigenteDTO();
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	// listaEvidencia = new ArrayList<ConceptoDTO>();
	// conceptoDTO = new ConceptoDTO();
	conceptoSeleccionado = new ConceptoDTO();
	listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	listaConvenioHistorico = new ArrayList<ConvenioVigenteHistoricoDTO>();
    }

    @PostConstruct
    public void start() {
	try {
	    alertaEvaluador = false;
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.iesDTO = controller.getIes();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.perfil = controller.getPerfil().getNombre();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.startsWith("EVALUACION")) {
		alertaFase = true;
	    }

	    informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    idInformacionIes = informacionIesDto.getId();
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();
	    // listaEvidencia = catalogoServicio
	    // .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
	    // GrupoConceptoEnum.VINCULACION_INVESTIGACION
	    // .getValor());

	    conceptoDTO = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
		    OrigenInformacionEnum.CONVENIOS.getValor(),
		    GrupoConceptoEnum.VINCULACION_INVESTIGACION.getValor());
	    if (conceptoDTO != null) {
		// conceptoSeleccionado = listaEvidencia.get(5);
		conceptoSeleccionado = conceptoDTO;
	    } else {
		JsfUtil.msgError("No se pudo recuperar el concepto para desplegar las evidencias. Comuníquese con el Administrador");
	    }
	    cargarConveniosVigentesIes(idInformacionIes);
	} catch (Exception e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    public void cargarConveniosVigentesIes(Long idInfoIes) {
	try {
	    this.totalConvenios = 0;
	    this.listaConvenioVigenteDTO.clear();
	    this.listaConvenioVigenteDTO = registroServicio
		    .obtenerConvenioVigentePorIes(idInfoIes);

	    this.totalConvenios = this.listaConvenioVigenteDTO.size();

	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Convenios de la IES");
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    public void obtenerHistoricoConvenio(ConvenioVigenteDTO convenio) {
	listaConvenioHistorico.clear();
	try {
	    listaConvenioHistorico = evaluacionServicio
		    .obtenerConvenioVigenteHistorico(convenio.getId());
	} catch (ServicioException e) {
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    public void iniciarValores() {
	this.cargarConveniosVigentesIes(idInformacionIes);

    }

    public void cancelarConvenio() {
	this.convenioVigenteDTO = new ConvenioVigenteDTO();

    }

    /**
     * 
     * ...method doSomethingElse documentation comment...
     * 
     * @author cam
     * @version 19/08/2014 - 15:24:21
     */
    public void cargarEConcepto() {
	evidenciaConcepto = true;
	if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CONVENIOS.getValor())) {
	    idEvidencia = convenioVigenteDTO.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + convenioVigenteDTO.getId();
	}

	try {
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), conceptoSeleccionado.getId(),
		            idEvidencia, conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    public void eliminarConvenio() {
	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (convenioVigenteDTO.getId() != null
		        && !convenioVigenteDTO.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar este convenio en fase de Rectificación.");
		    return;
		}
	    }

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);

	    convenioVigenteDTO.setAuditoria(auditoria);
	    convenioVigenteDTO.setActivo(Boolean.FALSE);
	    registroServicio.registrarConvenio(convenioVigenteDTO);
	    JsfUtil.msgInfo("El Convenio: " + convenioVigenteDTO.getNombre()
		    + " fue eliminado correctamente.");
	    listaConvenioVigenteDTO.remove(convenioVigenteDTO);
	} catch (Exception e) {
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void nuevoConvenio() {
	convenioVigenteDTO = new ConvenioVigenteDTO();
	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    context.execute("dialogConvenio.show();");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void editarConvenio() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (convenioVigenteDTO.getId() != null
		    && !convenioVigenteDTO.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede agregar/editar este convenio en fase de Rectificación.");
		return;
	    }
	}
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    context.execute("dialogConvenio.show();");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void guardarConvenioVigente() {

	if (!this.validarDatos(convenioVigenteDTO)) {
	    return;
	}

	try {

	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date());
	    convenioVigenteDTO.setAuditoria(auditorDto);

	    /* INFORMACION IES */
	    InformacionIesDTO inforIes = new InformacionIesDTO();
	    inforIes.setId(idInformacionIes);
	    convenioVigenteDTO.setInformacionIesDTO(inforIes);

	    convenioVigenteDTO.setActivo(true);
	    convenioVigenteDTO.setFaseIesDTO(faseIesDTO);
	    if (alertaEvaluador) {
		convenioVigenteDTO.setVerificarEvidencia(false);
	    }
	    convenioVigenteDTO = registroServicio
		    .registrarConvenio(convenioVigenteDTO);

	    if (null != convenioVigenteDTO.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		convenioVigenteDTO = new ConvenioVigenteDTO();

		cargarConveniosVigentesIes(idInformacionIes);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogConvenio.hide()");
	    } else {
		JsfUtil.msgError("Error al Guardar Convenio Vigente de la IES");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    /**
     * 
     * ...method doSomethingElse documentation comment...
     * 
     * @author cam
     * @version 19/08/2014 - 15:34:25
     * @param evidencia
     */
    public void enviarEvidencia(EvidenciaDTO evidencia) {

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

    public boolean validarDatos(ConvenioVigenteDTO convenioV) {

	if (null == convenioV.getNombre() || convenioV.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre del convenio");
	    return false;
	}

	if (null == convenioV.getNombreInstitucion()
	        || convenioV.getNombreInstitucion().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre de la Institución");
	    return false;
	}

	if (convenioV.getFechaInicio() == null) {
	    JsfUtil.msgError("Ingrese la fecha de Inicio");
	    return false;
	}

	if (convenioV.getFechaFin() == null) {
	    JsfUtil.msgError("Ingrese la fecha Fin del convenio");
	    return false;
	}

	if (convenioV.getFechaFin() != null) {
	    if (convenioV.getFechaInicio().after(convenioV.getFechaFin())) {
		JsfUtil.msgAdvert("La Fecha Fin debe ser mayor que la Fecha de Inicio");
		return false;
	    }
	}

	return true;
    }

    public Date getFechaActual() {
	Calendar cal = Calendar.getInstance();
	fechaActual = new Date(cal.getTimeInMillis());
	return fechaActual;
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
     * @return the idInformacionIes
     */
    public Long getIdInformacionIes() {
	return idInformacionIes;
    }

    /**
     * @param idInformacionIes
     *            the idInformacionIes to set
     */
    public void setIdInformacionIes(Long idInformacionIes) {
	this.idInformacionIes = idInformacionIes;
    }

    /**
     * @return the idSeleccionado
     */
    public Long getIdSeleccionado() {
	return idSeleccionado;
    }

    /**
     * @param idSeleccionado
     *            the idSeleccionado to set
     */
    public void setIdSeleccionado(Long idSeleccionado) {
	this.idSeleccionado = idSeleccionado;
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

    /**
     * @return the iesDTO
     */
    public IesDTO getIesDTO() {
	return iesDTO;
    }

    /**
     * @param iesDTO
     *            the iesDTO to set
     */
    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public ConvenioVigenteDTO getConvenioVigenteDTO() {
	return convenioVigenteDTO;
    }

    public void setConvenioVigenteDTO(ConvenioVigenteDTO convenioVigenteDTO) {
	this.convenioVigenteDTO = convenioVigenteDTO;
    }

    public List<ConvenioVigenteDTO> getListaConvenioVigenteDTO() {
	return listaConvenioVigenteDTO;
    }

    public void setListaConvenioVigenteDTO(
	    List<ConvenioVigenteDTO> listaConvenioVigenteDTO) {
	this.listaConvenioVigenteDTO = listaConvenioVigenteDTO;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

    public Integer getTotalConvenios() {
	return totalConvenios;
    }

    public void setTotalConvenios(Integer totalConvenios) {
	this.totalConvenios = totalConvenios;
    }

    public Long getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(Long idProceso) {
	this.idProceso = idProceso;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public String getNombreFichero() {
	return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
	this.nombreFichero = nombreFichero;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
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

    public List<ConvenioVigenteHistoricoDTO> getListaConvenioHistorico() {
	return listaConvenioHistorico;
    }

    public void setListaConvenioHistorico(
	    List<ConvenioVigenteHistoricoDTO> listaConvenioHistorico) {
	this.listaConvenioHistorico = listaConvenioHistorico;
    }

    public ConvenioVigenteDTO getConvenioSeleccionado() {
	return convenioSeleccionado;
    }

    public void setConvenioSeleccionado(ConvenioVigenteDTO convenioSeleccionado) {
	this.convenioSeleccionado = convenioSeleccionado;
    }

    public Boolean getAlertaUsuarioIes() {
	return alertaUsuarioIes;
    }

    public void setAlertaUsuarioIes(Boolean alertaUsuarioIes) {
	this.alertaUsuarioIes = alertaUsuarioIes;
    }

    public Boolean getAlertaFaseRectificacion() {
	return alertaFaseRectificacion;
    }

    public void setAlertaFaseRectificacion(Boolean alertaFaseRectificacion) {
	this.alertaFaseRectificacion = alertaFaseRectificacion;
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

}