package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ActividadVinculacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ActividadVinculacionHistoricoDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "actividadVincController")
public class AdminActividadVinculacionController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminActividadVinculacionController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutoServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    private ActividadVinculacionDTO actividadVinculacionDTO;
    private ActividadVinculacionDTO actividadVincSeleccionada;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private List<ActividadVinculacionDTO> listaActividadVinculacionDTO;
    private List<ActividadVinculacionHistoricoDTO> listaActividadHistorico;

    private String usuario;
    private Long idInformacionIes;
    private Long idSeleccionado;
    private String perfil;
    private String fase;

    private FaseIesDTO faseIesDTO;
    private List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto;
    private Long idEvidencia;

    private Boolean evidenciaConcepto = false;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Integer totalActividades = 0;

    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private StreamedContent documentoDescarga;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    public AdminActividadVinculacionController() {

	actividadVinculacionDTO = new ActividadVinculacionDTO();
	actividadVincSeleccionada = new ActividadVinculacionDTO();
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	faseIesDTO = new FaseIesDTO();

	listaActividadVinculacionDTO = new ArrayList<ActividadVinculacionDTO>();
	listaActividadHistorico = new ArrayList<ActividadVinculacionHistoricoDTO>();
	listaEvidenciaConceptoDto = new ArrayList<EvidenciaConceptoDTO>();
    }

    @PostConstruct
    public void start() {
	try {
	    alertaEvaluador = false;
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;
	    alertaFase = false;
	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.iesDTO = controller.getIes();
	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.perfil = controller.getPerfil().getNombre();

	    LOG.info("FaseIES "
		    + controller.getFaseIesDTO().getProcesoDTO().getAplicaA());
	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.equals("EVALUACION")) {
		alertaFase = true;
	    }
	    LOG.info("Fase Ies " + this.faseIesDTO.getFaseDTO().getNombre());

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);

	    this.idInformacionIes = informacionIesDto.getId();

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    this.cargarActividadesVinculacionIes(idInformacionIes);
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarActividadesVinculacionIes(Long idInfoIes) {
	try {
	    LOG.info("cargarActividadesVinculacionIes:");
	    this.listaActividadVinculacionDTO.clear();
	    this.listaActividadVinculacionDTO = registroServicio
		    .obtenerActividadesVinculacionPorIes(idInfoIes);

	    this.totalActividades = this.listaActividadVinculacionDTO.size();
	    LOG.info("totalActividades:" + totalActividades + ".");
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Actividades de Vinculación de la IES");
	}
    }

    public void iniciarValores() {
	this.cargarActividadesVinculacionIes(idInformacionIes);
    }

    public void obtenerHistoricoActivVinc(ActividadVinculacionDTO actividad) {
	listaActividadHistorico.clear();
	try {
	    listaActividadHistorico = evaluacionServicio
		    .obtenerActividadVinculacionHistorico(actividad.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void nuevaActividad() {

	try {

	    actividadVinculacionDTO = new ActividadVinculacionDTO();
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogAdministracion.show();");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void eliminarActividad() {
	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (actividadVinculacionDTO.getId() != null
		        && !actividadVinculacionDTO.getFaseIesDTO().getTipo()
		                .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar esta actividad en fase de Rectificación.");
		    return;
		}
	    }

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);

	    actividadVinculacionDTO.setAuditoria(auditoria);
	    actividadVinculacionDTO.setActivo(Boolean.FALSE);
	    registroServicio
		    .registrarActividadVinculacion(actividadVinculacionDTO);
	    JsfUtil.msgInfo("La Actividad de Vinculación: "
		    + actividadVinculacionDTO.getNombre()
		    + " fue eliminada correctamente.");
	    listaActividadVinculacionDTO.remove(actividadVinculacionDTO);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar la sede, comuníquese con el administrador del sistema.");
	    e.printStackTrace();
	}
    }

    public void editarActividadVinculacion(Long idActividadVinculacion) {
	LOG.info("editarActividadVinculacion");
	if (alertaFaseRectificacion && alertaUsuarioIes) {

	    if (actividadVinculacionDTO.getId() != null
		    && !actividadVinculacionDTO.getFaseIesDTO().getTipo()
		            .name().startsWith("RECTIFICACION")) {

		JsfUtil.msgError("No se puede editar esta actividad en fase de Rectificación.");
		return;
	    }
	}

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dialogAdministracion.show()");

    }

    public void guardarActividadVinculacion() {

	if (!this.validarDatos(actividadVinculacionDTO)) {
	    return;
	}

	try {
	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    actividadVinculacionDTO.setAuditoria(auditorDto);
	    actividadVinculacionDTO.setActivo(true);
	    actividadVinculacionDTO.setFaseIesDTO(faseIesDTO);

	    InformacionIesDTO inforIes = new InformacionIesDTO();
	    inforIes.setId(idInformacionIes);
	    actividadVinculacionDTO.setInformacionIesDTO(inforIes);
	    if (alertaEvaluador) {
		actividadVinculacionDTO.setVerificarEvidencia(false);
	    }
	    actividadVinculacionDTO = registroServicio
		    .registrarActividadVinculacion(actividadVinculacionDTO);

	    if (null != actividadVinculacionDTO.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		actividadVinculacionDTO = new ActividadVinculacionDTO();
		cargarActividadesVinculacionIes(idInformacionIes);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogAdministracion.hide();");
	    } else {
		JsfUtil.msgError("Error al Guardar Actividad de Vinculación IES");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al Guardar Actividad de Vinculación IES, consulte con el Administrador del Sistema");
	}
    }

    public boolean validarDatos(ActividadVinculacionDTO actividadV) {

	if (null == actividadV.getNombre() || actividadV.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre");
	    return false;
	}

	if (actividadV.getFechaInicio() instanceof Date) {
	    if (actividadV.getFechaInicio() == null) {
		JsfUtil.msgError("Ingrese la fecha de Inicio");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Ingrese la fecha de inicio de forma correcta");
	    return false;
	}

	if (actividadV.getFechaFin() instanceof Date) {
	    if (actividadV.getFechaFin() == null) {
		JsfUtil.msgError("Ingrese la fecha de Fin");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Ingrese la fecha de fin de forma correcta");
	    return false;
	}
	if (actividadV.getFechaInicio().after(actividadV.getFechaFin())) {
	    JsfUtil.msgError("La fecha de inicio debe ser menor a la fecha fin");
	    return false;
	}

	return true;
    }

    public void cargarEvidencia() {

	try {
	    idEvidencia = actividadVinculacionDTO.getId();
	    LOG.info("id Actividad Vinculacion: " + idEvidencia.toString());
	    listaEvidenciaConceptoDto.clear();
	    listaEvidenciaConceptoDto = institutoServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), 7L, idEvidencia,
		            "actividades_vinculacion");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
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

    public ActividadVinculacionDTO getActividadVinculacionDTO() {
	return actividadVinculacionDTO;
    }

    public void setActividadVinculacionDTO(
	    ActividadVinculacionDTO actividadVinculacionDTO) {
	this.actividadVinculacionDTO = actividadVinculacionDTO;
    }

    public List<ActividadVinculacionDTO> getListaActividadVinculacionDTO() {
	return listaActividadVinculacionDTO;
    }

    public void setListaActividadVinculacionDTO(
	    List<ActividadVinculacionDTO> listaActividadVinculacionDTO) {
	this.listaActividadVinculacionDTO = listaActividadVinculacionDTO;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
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

    public Integer getTotalActividades() {
	return totalActividades;
    }

    public void setTotalActividades(Integer totalActividades) {
	this.totalActividades = totalActividades;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

    public ActividadVinculacionDTO getActividadVincSeleccionada() {
	return actividadVincSeleccionada;
    }

    public void setActividadVincSeleccionada(
	    ActividadVinculacionDTO actividadVincSeleccionada) {
	this.actividadVincSeleccionada = actividadVincSeleccionada;
    }

    public List<ActividadVinculacionHistoricoDTO> getListaActividadHistorico() {
	return listaActividadHistorico;
    }

    public void setListaActividadHistorico(
	    List<ActividadVinculacionHistoricoDTO> listaActividadHistorico) {
	this.listaActividadHistorico = listaActividadHistorico;
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

}