package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratoAnchoBandaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminInformacionIesController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminInformacionIesController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    private IesDTO iesDto;
    private InformacionIesDTO informacionIesDto;
    private FaseIesDTO faseEvaluacion;
    private String usuario;
    private Long idSedeIesSeleccionada;
    private Boolean panelAnchoBanda;
    private ContratoAnchoBandaDTO contratoAnchoBanda;
    private List<ContratoAnchoBandaDTO> listaContratoAnchoBanda;
    private List<InformacionIesDTO> listaInformacionIesDto;

    public AdminInformacionIesController() {
	panelAnchoBanda = false;
	iesDto = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	listaInformacionIesDto = new ArrayList<InformacionIesDTO>();
	listaContratoAnchoBanda = new ArrayList<ContratoAnchoBandaDTO>();

    }

    @PostConstruct
    public void start() {
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");

	this.usuario = SecurityContextHolder.getContext().getAuthentication()
	        .getName();

	this.iesDto = controller.getIes();
	this.faseEvaluacion = controller.getFaseIesDTO();
	LOG.info("FASE IES: " + this.faseEvaluacion);
	obtenerInformacionIes();
    }

    public void obtenerInformacionIes() {
	listaInformacionIesDto.clear();
	try {

	    informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDto);
	    listaInformacionIesDto.add(informacionIesDto);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void registrarInformacionIes() {

	try {
	    if (!listaContratoAnchoBanda.isEmpty()) {
		informacionIesDto
		        .setContratosAnchoBandaDTO(listaContratoAnchoBanda);
	    }

	    AuditoriaDTO audit = new AuditoriaDTO();
	    audit.setFechaModificacion(new Date());
	    audit.setUsuarioModificacion(usuario);
	    informacionIesDto.setIes(iesDto);
	    informacionIesDto.setActivo(true);
	    informacionIesDto.setAuditoria(audit);
	    informacionIesDto.setIdFaseIes(faseEvaluacion.getId());
	    informacionIesDto = registroServicio
		    .crearActualizar(informacionIesDto);
	    JsfUtil.msgInfo("Registro almacenado correctamente");
	    obtenerInformacionIes();
	    nuevaInformacionIes();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo almacenar el registro");
	}
    }

    public void nuevaInformacionIes() {
	informacionIesDto = new InformacionIesDTO();
	contratoAnchoBanda = new ContratoAnchoBandaDTO();
	listaContratoAnchoBanda.clear();
    }

    public void agregarAnchoBanda() {
	if (!validarAnchoBanda())
	    return;

	listaContratoAnchoBanda.add(contratoAnchoBanda);
	contratoAnchoBanda = new ContratoAnchoBandaDTO();
    }

    public Boolean validarAnchoBanda() {

	if (null == contratoAnchoBanda.getNumContrato()
	        || contratoAnchoBanda.getNumContrato().equals("")) {
	    JsfUtil.msgError("Ingrese el número de contrato");
	    return false;
	}

	if (null == contratoAnchoBanda.getEmpresaServicio()
	        || contratoAnchoBanda.getEmpresaServicio().equals("")) {
	    JsfUtil.msgError("Ingrese la Empresa de Servicio");
	    return false;
	}

	if (null == contratoAnchoBanda.getFechaInicioContrato()) {
	    JsfUtil.msgError("Ingrese la Fecha de Inicio");
	    return false;
	}

	if (null == contratoAnchoBanda.getFechaFinContrato()) {
	    JsfUtil.msgError("Ingrese la Fecha de Fin de Contrato");
	    return false;
	}

	if (null == contratoAnchoBanda.getNumKbpsInternetContratado()
	        || contratoAnchoBanda.getNumKbpsInternetContratado() < 0) {
	    JsfUtil.msgError("Ingrese Número de Kbps");
	    return false;
	}

	if (contratoAnchoBanda.getFechaInicioContrato().after(
	        contratoAnchoBanda.getFechaFinContrato())) {
	    JsfUtil.msgError("La fecha de fin debe ser mayor que la fecha de inicio");
	    return false;
	}

	return true;
    }

    public void nuevoAnchoBanda() {
	panelAnchoBanda = true;
	contratoAnchoBanda = new ContratoAnchoBandaDTO();
	listaContratoAnchoBanda.clear();
    }

    public void cancelar() {
	panelAnchoBanda = false;
    }

    public void quitarAnchoBanda(ContratoAnchoBandaDTO anchoBanda) {
	listaContratoAnchoBanda.remove(anchoBanda);
    }

    public void editarInformacionIes() {
	listaContratoAnchoBanda.clear();
	if (informacionIesDto.getContratosAnchoBandaDTO().isEmpty()) {
	    JsfUtil.msgAdvert("No existen contratos de ancho de banda");
	    return;
	}
	listaContratoAnchoBanda = informacionIesDto.getContratosAnchoBandaDTO();
    }

    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public List<InformacionIesDTO> getListaInformacionIesDto() {
	return listaInformacionIesDto;
    }

    public void setListaInformacionIesDto(
	    List<InformacionIesDTO> listaInformacionIesDto) {
	this.listaInformacionIesDto = listaInformacionIesDto;
    }

    public IesDTO getIesDto() {
	return iesDto;
    }

    public void setIesDto(IesDTO iesDto) {
	this.iesDto = iesDto;
    }

    public FaseIesDTO getFaseEvaluacion() {
	return faseEvaluacion;
    }

    public void setFaseEvaluacion(FaseIesDTO faseEvaluacion) {
	this.faseEvaluacion = faseEvaluacion;
    }

    public ContratoAnchoBandaDTO getContratoAnchoBanda() {
	return contratoAnchoBanda;
    }

    public void setContratoAnchoBanda(ContratoAnchoBandaDTO contratoAnchoBanda) {
	this.contratoAnchoBanda = contratoAnchoBanda;
    }

    public List<ContratoAnchoBandaDTO> getListaContratoAnchoBanda() {
	return listaContratoAnchoBanda;
    }

    public void setListaContratoAnchoBanda(
	    List<ContratoAnchoBandaDTO> listaContratoAnchoBanda) {
	this.listaContratoAnchoBanda = listaContratoAnchoBanda;
    }

    public Boolean getPanelAnchoBanda() {
	return panelAnchoBanda;
    }

    public void setPanelAnchoBanda(Boolean panelAnchoBanda) {
	this.panelAnchoBanda = panelAnchoBanda;
    }

    public Long getIdSedeIesSeleccionada() {
	return idSedeIesSeleccionada;
    }

    public void setIdSedeIesSeleccionada(Long idSedeIesSeleccionada) {
	this.idSedeIesSeleccionada = idSedeIesSeleccionada;
    }

}