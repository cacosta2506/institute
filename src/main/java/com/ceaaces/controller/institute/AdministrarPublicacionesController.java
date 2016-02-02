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
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocentePublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.historico.PublicacionHistoricoDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "publicacionController")
public class AdministrarPublicacionesController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarPublicacionesController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutoServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    private IesDTO iesDTO;
    private InformacionIesDTO informacionIes;
    private PublicacionDTO publicacionDto;
    private PublicacionDTO publicacionSeleccionada;
    private FaseDTO faseEvaluacion;
    private Evidencia archivoSeleccionado;
    private FaseIesDTO faseIesDTO;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    private String usuario;
    private String perfil;
    private String fase;
    private String accion;
    private Integer totalPublicaciones = 0;

    private List<PublicacionDTO> listaPublicacionDto;
    private List<PublicacionHistoricoDTO> listaPublicacionHistorico;

    private List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto;
    private Long idEvidencia;
    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private StreamedContent documentoDescarga;

    public AdministrarPublicacionesController() {
	publicacionDto = new PublicacionDTO();
	publicacionSeleccionada = new PublicacionDTO();
	listaPublicacionDto = new ArrayList<PublicacionDTO>();
	listaPublicacionHistorico = new ArrayList<PublicacionHistoricoDTO>();
	iesDTO = new IesDTO();
	listaEvidenciaConceptoDto = new ArrayList<EvidenciaConceptoDTO>();

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
	    this.faseIesDTO = controller.getFaseIesDTO();

	    iesDTO = controller.getIes();

	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.startsWith("EVALUACION")) {
		alertaFase = true;
	    }

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    obtenerPublicaciones();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void nuevaPublicacion() {
	accion = "nuevo";
	try {
	    publicacionDto = new PublicacionDTO();

	    ExternalContext ec = FacesContext.getCurrentInstance()
		    .getExternalContext();

	    AdminPublicacionParticipanteController controller = (AdminPublicacionParticipanteController) ec
		    .getSessionMap().get("publicacionParticipanteController");
	    controller.setTipoSeleccionado("");
	    controller.setPanelArticulo(false);
	    controller.setPanelLibro(false);
	    controller.setPanelCapituloLibro(false);
	    controller.setPanelOtroTipo(false);
	    controller.setPublicacionDto(publicacionDto);
	    controller.iniciarValores();
	    controller.setInformacionIesDto(this.informacionIes);
	    ec.redirect("/" + ec.getContextName()
		    + "/publicaciones/adminPublicacionParticipante.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void recargarPublicaciones() {

	try {

	    listaPublicacionDto = registroServicio
		    .obtenerPublicaciones(informacionIes.getId());

	} catch (ServicioException e) {
	    LOG.info(e.getLocalizedMessage());
	}
    }

    public void obtenerHistoricoPublicacion(PublicacionDTO publicacion) {
	listaPublicacionHistorico.clear();
	try {
	    listaPublicacionHistorico = evaluacionServicio
		    .obtenerPublicacionHistorico(publicacion.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void editarPublicacion(Long idPublicacion) {

	accion = "editar";
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();

	try {
	    LOG.info("editarPublicacion");
	    publicacionDto = registroServicio
		    .obtenerPublicacionPorId(idPublicacion);
	    LOG.info("1 editarPublicacion.."
		    + publicacionDto.getFaseIesDTO().getTipo());
	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (publicacionDto.getId() != null
		        && !publicacionDto.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede editar esta publicación en fase de Rectificación.");
		    return;
		}
	    }
	    LOG.info("2 editarPublicacion");
	    EditarPublicacionParticipanteController controller = (EditarPublicacionParticipanteController) ec
		    .getSessionMap().get(
		            "editarPublicacionParticipanteController");

	    controller.setTipoSeleccionado(publicacionDto.getTipo().getValue());
	    controller.setTipoPublicacion(true);
	    if (publicacionDto.getTipo().getValue().equals("ARTICULO")) {
		controller.setPanelArticulo(true);
		controller.setPanelLibro(false);
		controller.setPanelCapituloLibro(false);
		controller.setPanelOtroTipo(false);
	    } else if (publicacionDto.getTipo().getValue().equals("LIBRO")) {
		controller.setPanelLibro(true);
		controller.setPanelArticulo(false);
		controller.setPanelCapituloLibro(false);
		controller.setPanelOtroTipo(false);
	    } else if (publicacionDto.getTipo().getValue()
		    .equals("CAPITULO LIBRO")) {
		controller.setPanelLibro(false);
		controller.setPanelArticulo(false);
		controller.setPanelCapituloLibro(true);
		controller.setPanelOtroTipo(false);
	    } else if (publicacionDto.getTipo().getValue().equals("OTRO")) {
		controller.setPanelLibro(false);
		controller.setPanelArticulo(false);
		controller.setPanelCapituloLibro(false);
		controller.setPanelOtroTipo(true);
	    }

	    List<DocentePublicacionDTO> listaDocentePublica = new ArrayList<DocentePublicacionDTO>();
	    for (DocentePublicacionDTO docentePublic : publicacionDto
		    .getListaDocentePublicacionDTO()) {
		if (docentePublic.getActivo()) {
		    listaDocentePublica.add(docentePublic);
		}
	    }

	    publicacionDto.getListaDocentePublicacionDTO().clear();
	    publicacionDto.setListaDocentePublicacionDTO(listaDocentePublica);
	    controller.setPublicacionDto(publicacionDto);

	    controller.setInformacionIesDto(this.informacionIes);
	    controller.iniciarValores();
	    ec.redirect("/" + ec.getContextName()
		    + "/publicaciones/editarPublicacionParticipante.jsf");

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar publicación");
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Setea las publicaciones con sus respectivos hijos en true para poder
     * eliminar solo aquellas
     */
    public void comprobarPublicacion(Long idPublicacion) {
	boolean tieneHijos = false;

	try {
	    publicacionDto = registroServicio
		    .obtenerPublicacionPorId(idPublicacion);

	    List<DocentePublicacionDTO> listaDocentePublica = new ArrayList<DocentePublicacionDTO>();
	    for (DocentePublicacionDTO docentePublic : publicacionDto
		    .getListaDocentePublicacionDTO()) {
		if (docentePublic.getActivo()) {
		    tieneHijos = true;
		    docentePublic.setActivo(false);
		    listaDocentePublica.add(docentePublic);
		}
	    }
	    publicacionDto.getListaDocentePublicacionDTO().clear();
	    publicacionDto.setListaDocentePublicacionDTO(listaDocentePublica);

	    if (tieneHijos) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation2.show()");
	    } else {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation.show()");
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar publicación");
	}
    }

    public void eliminarPublicaciones() {

	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (publicacionDto.getId() != null
		        && !publicacionDto.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar esta publicación en fase de Rectificación.");
		    return;
		}
	    }

	    AuditoriaDTO auditoriaDto = new AuditoriaDTO();
	    auditoriaDto.setUsuarioModificacion(usuario);
	    auditoriaDto.setFechaModificacion(new Date());
	    publicacionDto.setAuditoriaDTO(auditoriaDto);
	    publicacionDto.setActivo(false);
	    publicacionDto = registroServicio
		    .registroPublicacion(publicacionDto);
	    recargarPublicaciones();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar Publicaciones");
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void obtenerPublicaciones() {

	try {

	    listaPublicacionDto.clear();

	    listaPublicacionDto = registroServicio
		    .obtenerPublicaciones(informacionIes.getId());

	    setTotalPublicaciones(listaPublicacionDto.size());

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void cargarEvidencia() {

	try {
	    idEvidencia = publicacionDto.getId();
	    LOG.info("id Actividad Vinculacion: " + idEvidencia.toString());
	    listaEvidenciaConceptoDto.clear();
	    listaEvidenciaConceptoDto = institutoServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), 9L, idEvidencia, "publicaciones");
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

    /** GETTERS AND SETTERS **/
    public PublicacionDTO getPublicacionDto() {
	return publicacionDto;
    }

    public void setPublicacionDto(PublicacionDTO publicacionDto) {
	this.publicacionDto = publicacionDto;
    }

    public List<PublicacionDTO> getListaPublicacionDto() {
	obtenerInformacionIes();
	return listaPublicacionDto;
    }

    public void setListaPublicacionDto(List<PublicacionDTO> listaPublicacionDto) {
	this.listaPublicacionDto = listaPublicacionDto;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
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

    public String getAccion() {
	return accion;
    }

    public void setAccion(String accion) {
	this.accion = accion;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public RegistroServicio getRegistroServicio() {
	return registroServicio;
    }

    public void setRegistroServicio(RegistroServicio registroServicio) {
	this.registroServicio = registroServicio;
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

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public Integer getTotalPublicaciones() {
	return totalPublicaciones;
    }

    public void setTotalPublicaciones(Integer totalPublicaciones) {
	this.totalPublicaciones = totalPublicaciones;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public PublicacionDTO getPublicacionSeleccionada() {
	return publicacionSeleccionada;
    }

    public void setPublicacionSeleccionada(
	    PublicacionDTO publicacionSeleccionada) {
	this.publicacionSeleccionada = publicacionSeleccionada;
    }

    public List<PublicacionHistoricoDTO> getListaPublicacionHistorico() {
	return listaPublicacionHistorico;
    }

    public void setListaPublicacionHistorico(
	    List<PublicacionHistoricoDTO> listaPublicacionHistorico) {
	this.listaPublicacionHistorico = listaPublicacionHistorico;
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
