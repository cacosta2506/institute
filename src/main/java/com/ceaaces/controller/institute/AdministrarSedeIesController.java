package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FuncionarioDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDistribucionFisicaDTO;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "adminSedeIesController")
public class AdministrarSedeIesController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private RegistroServicio registroServicio;

    private SedeIesDTO sedeIesDto;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;

    private List<SedeIesDTO> listaSedeIesDto;

    private String usuario;
    private Long idInformacionIes;
    private Long idSeleccionado;
    private String perfil;
    private FaseIesDTO faseIesDTO;
    private Boolean alertaFaseRectificacion = false;
    private Boolean alertaEvaluador = false;
    private Boolean alertaUsuarioIes = false;

    public AdministrarSedeIesController() {
	listaSedeIesDto = new ArrayList<SedeIesDTO>();
	sedeIesDto = new SedeIesDTO();
	// sedeIesDto.setLatitud(0.0);
	// sedeIesDto.setLongitud(0.0);

	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
    }

    @PostConstruct
    public void start() {
	try {
	    alertaEvaluador = false;
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    iesDTO = controller.getIes();
	    this.perfil = controller.getPerfil().getNombre();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }
	    informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    idInformacionIes = informacionIesDto.getId();
	    cargarSedeIes(idInformacionIes);
	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	this.cargarSedeIes(idInformacionIes);
    }

    public void cargarSedeIes(Long idInfoIes) {
	try {
	    this.listaSedeIesDto.clear();
	    this.listaSedeIesDto = registroServicio.obtenerSedesIes(idInfoIes);

	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    public void iniciarValores() {

	this.cargarSedeIes(idInformacionIes);
    }

    public void eliminarSedeIes() {
	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (sedeIesDto.getId() != null
		        && !sedeIesDto.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar el registro en fase de Rectificación.");
		    return;
		}
	    }

	    List<FuncionarioDTO> funcionarios = registroServicio
		    .obtenerFuncionariosPorIdSede(sedeIesDto.getId());
	    List<InformacionCarreraDTO> carreras = registroServicio
		    .obtenerInfCarreraPorSede(sedeIesDto.getId(), null);
	    List<SedeIesDistribucionFisicaDTO> campus = registroServicio
		    .obtenerSedeDistribucionFisica(sedeIesDto.getId());
	    String mensajeError = null;
	    if (funcionarios != null && !funcionarios.isEmpty()) {
		mensajeError = "funcionarios";
	    }
	    if (carreras != null && !carreras.isEmpty()) {
		if (mensajeError != null) {
		    mensajeError = mensajeError + ", carreras";
		} else {
		    mensajeError = "carreras";
		}
	    }
	    if (campus != null && !campus.isEmpty()) {
		if (mensajeError != null) {
		    mensajeError = mensajeError + " y campus";
		} else {
		    mensajeError = "campus";
		}
	    }

	    if (mensajeError != null) {
		JsfUtil.msgError("No se puede eliminar la sede porque tiene registros relacionados con: "
		        + mensajeError);
		return;
	    }
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);
	    sedeIesDto.setAuditoria(auditoria);
	    sedeIesDto.setActivo(Boolean.FALSE);
	    // el método cuando tiene el id, actualiza el registro
	    registroServicio.registrarSedeIes(sedeIesDto);
	    JsfUtil.msgInfo("La sede: " + sedeIesDto.getNombre()
		    + " fue eliminada correctamente.");
	    listaSedeIesDto.remove(sedeIesDto);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar la sede, comuníquese con el administrador del sistema.");
	    e.printStackTrace();
	}
    }

    public void guardarSedeIes() {

	if (!this.validarDatos(sedeIesDto)) {
	    return;
	}

	try {
	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    sedeIesDto.setAuditoria(auditorDto);

	    sedeIesDto.setActivo(true);

	    InformacionIesDTO inforIes = new InformacionIesDTO();
	    inforIes.setId(idInformacionIes);
	    sedeIesDto.setInformacionIesDTO(inforIes);
	    sedeIesDto.setFaseIesDTO(faseIesDTO);

	    sedeIesDto = registroServicio.registrarSedeIes(sedeIesDto);

	    if (null != sedeIesDto.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		sedeIesDto = new SedeIesDTO();
		// sedeIesDto.setLatitud(0.0);
		// sedeIesDto.setLongitud(0.0);
		cargarSedeIes(idInformacionIes);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgSedeIes.hide()");
	    } else {
		JsfUtil.msgError("Error al Guardar Sede IES");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al Guardar Sede IES, consulte con el Administrador del Sistema");
	}
    }

    public void salirSedeIesDetalle() {

    }

    public boolean validarDatos(SedeIesDTO sedeIesv) {

	if (null == sedeIesv.getNombre() || sedeIesv.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre");
	    return false;
	}

	if (null == sedeIesv.getTipoSede() || sedeIesv.getTipoSede().equals("")) {
	    JsfUtil.msgError("Seleccione el Tipo");
	    return false;
	}

	if (sedeIesv.getFechaCreacion() instanceof Date) {
	    if (sedeIesv.getFechaCreacion() == null) {
		JsfUtil.msgError("Ingrese la fecha de Creación");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Ingrese la fecha de creación de forma correcta");
	    return false;
	}

	return true;
    }

    /**
     * @return the sedeIesDto
     */
    public SedeIesDTO getSedeIesDto() {
	return sedeIesDto;
    }

    /**
     * @param sedeIesDto
     *            the sedeIesDto to set
     */
    public void setSedeIesDto(SedeIesDTO sedeIesDto) {
	this.sedeIesDto = sedeIesDto;
    }

    /**
     * @return the listaSedeIesDto
     */
    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    /**
     * @param listaSedeIesDto
     *            the listaSedeIesDto to set
     */
    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
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

    public Boolean getAlertaFaseRectificacion() {
	return alertaFaseRectificacion;
    }

    public void setAlertaFaseRectificacion(Boolean alertaFaseRectificacion) {
	this.alertaFaseRectificacion = alertaFaseRectificacion;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public Boolean getAlertaUsuarioIes() {
	return alertaUsuarioIes;
    }

    public void setAlertaUsuarioIes(Boolean alertaUsuarioIes) {
	this.alertaUsuarioIes = alertaUsuarioIes;
    }

}