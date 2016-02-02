package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratoAnchoBandaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.UsuariosAnchoBandaEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminContratosAnchoBandaController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminContratosAnchoBandaController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private ContratoAnchoBandaDTO contratoAnchoBandaDTO;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private FaseIesDTO faseIesDTO;

    private List<ContratoAnchoBandaDTO> listaContratoAnchoBandaDTO;

    private String usuario;
    private Long idSeleccionado;
    private String perfil;
    private String fase;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;
    private Integer totalContratos = 0;
    private String usuarioAnchoBanda;
    private List<String> listaUsuariosAnchoBanda;

    public AdminContratosAnchoBandaController() {
	listaContratoAnchoBandaDTO = new ArrayList<>();
	contratoAnchoBandaDTO = new ContratoAnchoBandaDTO();
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	listaUsuariosAnchoBanda = new ArrayList<>();
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

	    this.cargarContratosAnchoBanda(this.informacionIesDto.getId());
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarContratosAnchoBanda(Long idInfoIes) {
	try {
	    this.listaUsuariosAnchoBanda.clear();
	    for (UsuariosAnchoBandaEnum uab : UsuariosAnchoBandaEnum.values()) {
		this.listaUsuariosAnchoBanda.add(uab.getValue());
	    }
	    this.listaContratoAnchoBandaDTO.clear();
	    this.listaContratoAnchoBandaDTO = registroServicio
		    .obtenerContratosAnchoBandaPorIes(this.informacionIesDto
		            .getId());

	    this.totalContratos = this.listaContratoAnchoBandaDTO.size();

	} catch (Exception e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void iniciarValores() {
	this.cargarContratosAnchoBanda(this.informacionIesDto.getId());
    }

    public void limpiarContratoAnchoBanda() {
	usuarioAnchoBanda = null;
	contratoAnchoBandaDTO = new ContratoAnchoBandaDTO();

    }

    public void eliminarContrato() {
	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (contratoAnchoBandaDTO.getId() != null
		        && !contratoAnchoBandaDTO.getFaseIesDTO().getTipo()
		                .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar este contrato en fase de Rectificación.");
		    return;
		}
	    }

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);

	    contratoAnchoBandaDTO.setAuditoria(auditoria);
	    contratoAnchoBandaDTO.setActivo(Boolean.FALSE);
	    registroServicio.registrarContratoAnchoBanda(contratoAnchoBandaDTO);
	    JsfUtil.msgInfo("El Contrato: "
		    + contratoAnchoBandaDTO.getNumContrato()
		    + " fue eliminado correctamente.");
	    listaContratoAnchoBandaDTO.remove(contratoAnchoBandaDTO);
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarEdicion() {
	this.usuarioAnchoBanda = contratoAnchoBandaDTO
	        .getUsuariosAnchoBandaEnum().getValue();
	try {
	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (contratoAnchoBandaDTO.getId() != null
		        && !contratoAnchoBandaDTO.getFaseIesDTO().getTipo()
		                .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede agregar/editar este contrato en fase de Rectificación.");
		    return;
		}
	    }

	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogContrato.show();");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void nuevoContrato() {

	try {

	    contratoAnchoBandaDTO = new ContratoAnchoBandaDTO();
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogContrato.show();");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void guardarContratoAnchoBanda() {

	if (!this.validarDatos(contratoAnchoBandaDTO)) {
	    return;
	}

	try {
	    /* AUDITORIA */
	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date());
	    contratoAnchoBandaDTO.setAuditoria(auditorDto);

	    /* INFORMACION IES */
	    InformacionIesDTO inforIes = new InformacionIesDTO();
	    inforIes.setId(informacionIesDto.getId());
	    contratoAnchoBandaDTO.setInformacionIesDTO(inforIes);

	    contratoAnchoBandaDTO.setActivo(true);
	    contratoAnchoBandaDTO.setFaseIesDTO(faseIesDTO);
	    contratoAnchoBandaDTO
		    .setUsuariosAnchoBandaEnum(UsuariosAnchoBandaEnum
		            .parse(this.usuarioAnchoBanda));

	    contratoAnchoBandaDTO = registroServicio
		    .registrarContratoAnchoBanda(contratoAnchoBandaDTO);

	    if (null != contratoAnchoBandaDTO.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		contratoAnchoBandaDTO = new ContratoAnchoBandaDTO();

		cargarContratosAnchoBanda(this.informacionIesDto.getId());
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogContrato.hide()");
	    } else {
		JsfUtil.msgError("Error al guardar el contrato de ancho de banda de la IES");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public boolean validarDatos(ContratoAnchoBandaDTO contrato) {

	if (null == contrato.getNumContrato()
	        || contrato.getNumContrato().equals("")) {
	    JsfUtil.msgError("Ingrese el número de contrato");
	    return false;
	}

	if (null == contrato.getEmpresaServicio()
	        || contrato.getEmpresaServicio().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre de la Empresa que brinda el servicio");
	    return false;
	}

	if (contrato.getFechaInicioContrato() == null) {
	    JsfUtil.msgError("Ingrese la fecha de Inicio del contrato");
	    return false;
	}

	if (contrato.getFechaFinContrato() == null) {
	    JsfUtil.msgError("Ingrese la fecha Fin del contrato");
	    return false;
	}

	if (contrato.getFechaFinContrato() != null) {
	    if (contrato.getFechaInicioContrato().after(
		    contrato.getFechaFinContrato())) {
		JsfUtil.msgError("La Fecha Fin debe ser mayor que la Fecha de Inicio");
		return false;
	    }
	}

	if (contrato.getNumKbpsInternetContratado() == 0.0) {
	    JsfUtil.msgError("Num. Kbps Internet Contratado, no puede ser Cero");
	    return false;
	}

	if (contrato.getNumKbpsInternetContratado() < 0) {
	    JsfUtil.msgError("Num. Kbps Internet Contratado, no puede ser Negativo");
	    return false;
	}

	LOG.info("USR:" + usuarioAnchoBanda);
	if (usuarioAnchoBanda == null || usuarioAnchoBanda.isEmpty()) {
	    JsfUtil.msgError("Seleccione los usuarios que utilizan el ancho de banda");
	    return false;
	}

	if (null == contrato.getCoberturaEdificios()
	        || contrato.getCoberturaEdificios().equals("")) {
	    JsfUtil.msgError("Ingrese los Edificios cobertura");
	    return false;
	}

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

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public Integer getTotalContratos() {
	return totalContratos;
    }

    public void setTotalContratos(Integer totalContratos) {
	this.totalContratos = totalContratos;
    }

    public List<ContratoAnchoBandaDTO> getListaContratoAnchoBandaDTO() {
	return listaContratoAnchoBandaDTO;
    }

    public void setListaContratoAnchoBandaDTO(
	    List<ContratoAnchoBandaDTO> listaContratoAnchoBandaDTO) {
	this.listaContratoAnchoBandaDTO = listaContratoAnchoBandaDTO;
    }

    public ContratoAnchoBandaDTO getContratoAnchoBandaDTO() {
	return contratoAnchoBandaDTO;
    }

    public void setContratoAnchoBandaDTO(
	    ContratoAnchoBandaDTO contratoAnchoBandaDTO) {
	this.contratoAnchoBandaDTO = contratoAnchoBandaDTO;
    }

    public String getUsuarioAnchoBanda() {
	return usuarioAnchoBanda;
    }

    public void setUsuarioAnchoBanda(String usuarioAnchoBanda) {
	this.usuarioAnchoBanda = usuarioAnchoBanda;
    }

    public List<String> getListaUsuariosAnchoBanda() {
	return listaUsuariosAnchoBanda;
    }

    public void setListaUsuariosAnchoBanda(List<String> listaUsuariosAnchoBanda) {
	this.listaUsuariosAnchoBanda = listaUsuariosAnchoBanda;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
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