package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ParticipacionProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PresupuestoProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ProyectoHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.ConceptoEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "proyectoController")
public class AdministrarProyectoController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarProyectoController.class.getSimpleName());

    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private ProyectoDTO proyectoDto;
    private ProyectoDTO proyectoSeleccionado;
    private FaseIesDTO faseIesDTO;

    private List<ProyectoDTO> listaProyectoDto;
    private List<ProyectoHistoricoDTO> listaProyectoHistorico;

    private String usuario;
    private Long idSeleccionado;
    private String perfil;

    private boolean alertaIes = false;
    private Boolean alertaEvaluador = false;
    private Integer totalProyectos = 0;

    private List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto;
    private Long idEvidencia;
    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private StreamedContent documentoDescarga;
    private Long idProceso;
    private ConceptoDTO proyVincConceptoDTO = new ConceptoDTO();
    private ConceptoDTO proyInvConceptoDTO = new ConceptoDTO();
    private List<ConceptoDTO> listConceptoDTO = new ArrayList<ConceptoDTO>();
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    // PARTICIPACION DOCENTES

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private InstitutosServicio institutoServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    public AdministrarProyectoController() {
	proyectoDto = new ProyectoDTO();
	proyectoSeleccionado = new ProyectoDTO();
	listaProyectoDto = new ArrayList<ProyectoDTO>();
	listaEvidenciaConceptoDto = new ArrayList<EvidenciaConceptoDTO>();
	listaProyectoHistorico = new ArrayList<ProyectoHistoricoDTO>();
	listConceptoDTO = new ArrayList<ConceptoDTO>();
	proyVincConceptoDTO = new ConceptoDTO();
	proyInvConceptoDTO = new ConceptoDTO();
    }

    @PostConstruct
    public void start() {
	obtenerInformacionIes();

    }

    public void obtenerInformacionIes() {

	try {
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.perfil = controller.getPerfil().getNombre();
	    if (this.perfil.startsWith("IES")) {
		alertaIes = true;
	    }
	    this.iesDTO = controller.getIes();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);

	    faseIesDTO = controller.getFaseIesDTO();
	    // idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();

	    proyVincConceptoDTO = catalogoServicio
		    .obtenerConceptoPorCodigo(ConceptoEnum.PROYECTOS_INV
		            .getValue());
	    if (proyVincConceptoDTO.getId() != null) {
		listConceptoDTO.add(proyVincConceptoDTO);
	    }
	    proyInvConceptoDTO = catalogoServicio
		    .obtenerConceptoPorCodigo(ConceptoEnum.PROYECTOS_VIN
		            .getValue());
	    if (proyInvConceptoDTO.getId() != null) {
		listConceptoDTO.add(proyInvConceptoDTO);
	    }
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

	    cargarProyectos();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void nuevoProyecto() {

	try {

	    proyectoDto = new ProyectoDTO();
	    ExternalContext ec = FacesContext.getCurrentInstance()
		    .getExternalContext();

	    AdminProyectoPresupuestoParticipacionController controller = (AdminProyectoPresupuestoParticipacionController) ec
		    .getSessionMap().get(
		            "proyectoPresupuestoParticipacionController");

	    controller.setProyectoDto(proyectoDto);
	    controller.setFaseIesDTO(this.faseIesDTO);

	    ec.redirect("/" + ec.getContextName()
		    + "/proyectos/adminProyectoPresupuestoParticipacion.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void obtenerHistoricoProyecto(ProyectoDTO proyecto) {
	listaProyectoHistorico.clear();
	try {
	    listaProyectoHistorico = evaluacionServicio
		    .obtenerProyectoHistorico(proyecto.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void eliminarProyecto() {

	try {
	    AuditoriaDTO auditoriaDto = new AuditoriaDTO();
	    auditoriaDto.setUsuarioModificacion(usuario);
	    auditoriaDto.setFechaModificacion(new Date());
	    proyectoDto.setAuditoriaDTO(auditoriaDto);
	    proyectoDto.setActivo(false);
	    proyectoDto.setFaseIesDTO(this.faseIesDTO);
	    registroServicio.registrarProyecto(proyectoDto);
	    cargarProyectos();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar");
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar, consulte con el administrador");
	    e.printStackTrace();
	}

    }

    public void comprobarProyecto(Long idProyecto) {

	try {
	    boolean tieneHijos = false;
	    ProyectoDTO proyectoEliminar = new ProyectoDTO();
	    proyectoEliminar = registroServicio
		    .obtenerProyectoPorId(idProyecto);

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (proyectoEliminar.getId() != null
		        && !proyectoEliminar.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar este proyecto en fase de Rectificación.");
		    return;
		}
	    }

	    List<PresupuestoProyectoDTO> listaPresupuesto = new ArrayList<PresupuestoProyectoDTO>();
	    for (PresupuestoProyectoDTO presupuesto : proyectoEliminar
		    .getListaPresupuestoProyectosDTO()) {
		if (presupuesto.getActivo()) {
		    tieneHijos = true;
		    presupuesto.setActivo(false);
		    listaPresupuesto.add(presupuesto);
		}
	    }

	    proyectoEliminar.getListaPresupuestoProyectosDTO().clear();
	    proyectoEliminar.setListaPresupuestoProyectosDTO(listaPresupuesto);

	    List<ParticipacionProyectoDTO> listaParticipacion = new ArrayList<ParticipacionProyectoDTO>();
	    for (ParticipacionProyectoDTO participacion : proyectoEliminar
		    .getListaParticipacionProyectosDTO()) {
		if (participacion.getActivo()) {
		    tieneHijos = true;
		    participacion.setActivo(false);
		    listaParticipacion.add(participacion);
		}
	    }
	    proyectoEliminar.getListaParticipacionProyectosDTO().clear();
	    proyectoEliminar
		    .setListaParticipacionProyectosDTO(listaParticipacion);

	    proyectoDto = proyectoEliminar;

	    if (tieneHijos) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation2.show()");
	    } else {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation.show()");
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar, consulte con el administrador");
	    e.printStackTrace();
	}

    }

    public void editarProyectos(Long idProyecto) {

	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();

	try {

	    proyectoDto = registroServicio.obtenerProyectoPorId(idProyecto);

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (proyectoDto.getId() != null
		        && !proyectoDto.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede editar este proyecto en fase de Rectificación.");
		    return;
		}
	    }

	    EditarProyectoPresupuestoParticipacionController controller = (EditarProyectoPresupuestoParticipacionController) ec
		    .getSessionMap().get(
		            "editarProyectoPresupuestoParticipacionController");

	    List<PresupuestoProyectoDTO> listaPresupuesto = new ArrayList<PresupuestoProyectoDTO>();
	    for (PresupuestoProyectoDTO presupuesto : proyectoDto
		    .getListaPresupuestoProyectosDTO()) {
		if (presupuesto.getActivo()) {
		    listaPresupuesto.add(presupuesto);
		}
	    }
	    proyectoDto.getListaPresupuestoProyectosDTO().clear();
	    proyectoDto.setListaPresupuestoProyectosDTO(listaPresupuesto);

	    List<ParticipacionProyectoDTO> listaParticipacion = new ArrayList<ParticipacionProyectoDTO>();
	    for (ParticipacionProyectoDTO participacion : proyectoDto
		    .getListaParticipacionProyectosDTO()) {
		if (participacion.getActivo()) {
		    listaParticipacion.add(participacion);
		}
	    }
	    proyectoDto.getListaParticipacionProyectosDTO().clear();
	    proyectoDto.setListaParticipacionProyectosDTO(listaParticipacion);

	    controller.setTipoSeleccionado(proyectoDto.getTipo().getValue());
	    controller.setProyectoDto(proyectoDto);
	    controller.setFaseIesDTO(this.faseIesDTO);

	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al cargar proyecto");
	    e.printStackTrace();
	}

	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/proyectos/editarProyectoPresupuestoParticipacion.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void cargarProyectos() {
	try {

	    this.listaProyectoDto.clear();

	    this.listaProyectoDto = registroServicio.obtenerProyectos(
		    informacionIesDto.getId(), null);

	    this.totalProyectos = this.listaProyectoDto.size();

	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Proyectos");
	}
    }

    public void salirSedeIesDetalle() {

    }

    public void cargarEvidencia() {

	try {

	    // Long idConcepto = null;
	    // if
	    // (TipoProyectoEnum.INVESTIGACION_E_INNOVACION.equals(proyectoDto
	    // .getTipo())) {
	    // idConcepto = 6L;
	    // }

	    // if (TipoProyectoEnum.VINCULACION.equals(proyectoDto.getTipo())) {
	    // idConcepto = 5L;
	    // }

	    idEvidencia = proyectoDto.getId();
	    LOG.info("id proyectos: " + idEvidencia.toString());
	    listaEvidenciaConceptoDto.clear();

	    for (ConceptoDTO concepto : listConceptoDTO) {
		// List<EvidenciaConceptoDTO>
		// listaVinculacionEvidenciaConceptoDto = new
		// ArrayList<EvidenciaConceptoDTO>();
		// listaVinculacionEvidenciaConceptoDto = ;

		listaEvidenciaConceptoDto.addAll(institutoServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                iesDTO.getId(), concepto.getId(), idEvidencia,
		                "proyectos"));
	    }

	    // listaEvidenciaConceptoDto = institutoServicio
	    // .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
	    // iesDTO.getId(), idConcepto, idEvidencia,
	    // "proyectos");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {
	if (null == evidencia)
	    return;

	LOG.info(evidencia.getNombreArchivo());
	setFichero(evidencia.getNombreArchivo());
	url = evidencia.getUrl();
	String[] nombreFichero = evidencia.getNombreArchivo().split("[.]");

	extensionDocumento = nombreFichero[nombreFichero.length - 1];
	LOG.info(extensionDocumento);
	LOG.info(url);
	setDescargar(true);
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

    /**
     * @return the proyectoDto
     */
    public ProyectoDTO getProyectoDto() {
	return proyectoDto;
    }

    /**
     * @param proyectoDto
     *            the proyectoDto to set
     */
    public void setProyectoDto(ProyectoDTO proyectoDto) {
	this.proyectoDto = proyectoDto;
    }

    /**
     * @return the listaProyectoDto
     */
    public List<ProyectoDTO> getListaProyectoDto() {
	return listaProyectoDto;
    }

    /**
     * @param listaProyectoDto
     *            the listaProyectoDto to set
     */
    public void setListaProyectoDto(List<ProyectoDTO> listaProyectoDto) {
	this.listaProyectoDto = listaProyectoDto;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public boolean isAlertaIes() {
	return alertaIes;
    }

    public void setAlertaIes(boolean alertaIes) {
	this.alertaIes = alertaIes;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public Integer getTotalProyectos() {
	return totalProyectos;
    }

    public void setTotalProyectos(Integer totalProyectos) {
	this.totalProyectos = totalProyectos;
    }

    public String getFichero() {
	return fichero;
    }

    public void setFichero(String fichero) {
	this.fichero = fichero;
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

    public List<EvidenciaConceptoDTO> getListaEvidenciaConceptoDto() {
	return listaEvidenciaConceptoDto;
    }

    public void setListaEvidenciaConceptoDto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto) {
	this.listaEvidenciaConceptoDto = listaEvidenciaConceptoDto;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public List<ProyectoHistoricoDTO> getListaProyectoHistorico() {
	return listaProyectoHistorico;
    }

    public void setListaProyectoHistorico(
	    List<ProyectoHistoricoDTO> listaProyectoHistorico) {
	this.listaProyectoHistorico = listaProyectoHistorico;
    }

    public ProyectoDTO getProyectoSeleccionado() {
	return proyectoSeleccionado;
    }

    public void setProyectoSeleccionado(ProyectoDTO proyectoSeleccionado) {
	this.proyectoSeleccionado = proyectoSeleccionado;
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
