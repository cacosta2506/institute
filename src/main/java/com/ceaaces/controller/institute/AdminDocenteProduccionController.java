package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
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
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProduccionDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminDocenteProduccionController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    private String usuario;
    private String participacionSeleccionado;
    private String perfil;

    private ProduccionDTO produccionDTO;
    private DocenteDTO docenteDto;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private ProduccionDTO produccionSeleccionada;

    private List<ProduccionDTO> listaProduccionDTO;
    private List<DocenteProduccionDTO> listaDocenteProduccionDTO;
    private List<DocenteDTO> listaDocenteDto;

    // Evidencia
    private List<ItemEvidenciaLocal> listaItemEvidencias;
    private FaseDTO faseEvaluacion;
    private Evidencia archivoSeleccionado;
    private FaseIesDTO faseIesDTO;

    private List<DocenteProduccionDTO> vistaParticipaciones;

    public AdminDocenteProduccionController() {

	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	produccionDTO = new ProduccionDTO();
	docenteDto = new DocenteDTO();
	faseEvaluacion = new FaseDTO();
	archivoSeleccionado = new Evidencia();
	produccionSeleccionada = new ProduccionDTO();

	listaProduccionDTO = new ArrayList<ProduccionDTO>();
	listaDocenteProduccionDTO = new ArrayList<DocenteProduccionDTO>();
	listaDocenteDto = new ArrayList<DocenteDTO>();
	listaItemEvidencias = new ArrayList<ItemEvidenciaLocal>();
	vistaParticipaciones = new ArrayList<>();
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

	    this.iesDTO = controller.getIes();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.perfil = controller.getPerfil().getNombre();

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();
	    this.faseIesDTO = controller.getFaseIesDTO();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void guardarPublicacion() {

	if (!validarDatos(produccionDTO)) {
	    return;
	}

	if (listaDocenteProduccionDTO.isEmpty()) {
	    JsfUtil.msgAdvert("Debe ingresar por lo menos un participante");
	    return;
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	produccionDTO.setAuditoria(auditoria);

	produccionDTO.setActivo(true);

	produccionDTO.setInformacionIesDTO(informacionIesDto);
	produccionDTO.setFaseIesDTO(faseIesDTO);

	produccionDTO.setListaDocenteProduccionDTO(listaDocenteProduccionDTO);

	try {
	    produccionDTO = registroServicio.registrarProduccion(produccionDTO);
	} catch (ServicioException e) {
	    JsfUtil.msgError("No se pudo almacenar la producción: "
		    + e.getMessage());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
	if (null != produccionDTO.getId()) {
	    JsfUtil.msgInfo("Registro almacenado correctamente");
	    limpiarDatos();
	}

    }

    public boolean validarDatos(ProduccionDTO produccion) {

	if (null == produccion.getNombre() || produccion.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el Nombre Produccion");
	    return false;
	}

	if (null == produccion.getTipo() || produccion.getTipo().equals("")) {
	    JsfUtil.msgError("Ingrese el Nombre Produccion");
	    return false;
	}

	if (null == produccion.getFechaPresentacion()) {
	    JsfUtil.msgError("Ingrese la Fecha de Presentación");
	    return false;
	}

	if (produccion.getPremio() == true) {
	    if (null == produccion.getNombrePremio()
		    || produccion.getNombrePremio().equals("")) {
		JsfUtil.msgError("Ingrese el Nombre del Premio");
		return false;
	    }
	    if (null == produccion.getFechaOtorgaPremio()) {
		JsfUtil.msgError("Ingrese la Fecha que se Otorga el Premio");
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

	produccionDTO = new ProduccionDTO();
	listaDocenteProduccionDTO.clear();
	listaDocenteDto.clear();
    }

    public void nuevaParticipacion() {
	docenteDto = new DocenteDTO();
	participacionSeleccionado = "";
	vistaParticipaciones.clear();
	obtenerDocentes();

    }

    public void obtenerDocentes() {
	ListaDocenteController controller = (ListaDocenteController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaDocenteController");
	controller.setInformacionIesDTO(this.informacionIesDto);
	controller.setOpcion("PUBLICACIONES");
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

	// for (DocentePublicacionDTO lisDocente : listaDocentePublicacionDto) {
	// if (lisDocente.getDocenteDTO().getId().equals(docenteDto.getId())) {
	// msgAdvert("El docente ya esta agregado");
	// return;
	// }
	// }

	DocenteProduccionDTO docente = new DocenteProduccionDTO();

	docente.setDocenteDTO(docenteDto);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	docente.setAuditoriaDTO(auditoria);

	docente.setActivo(true);
	docente.setFaseIesDTO(faseIesDTO);

	vistaParticipaciones.add(docente);

	docenteDto = new DocenteDTO();
	participacionSeleccionado = null;
    }

    //
    public void agregarParticipante() {
	listaDocenteProduccionDTO.clear();
	listaDocenteProduccionDTO.addAll(vistaParticipaciones);

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

	for (DocenteProduccionDTO participacion : listaDocenteProduccionDTO) {
	    if (participacion.getDocenteDTO().getId()
		    .equals(docenteDto.getId())) {
		JsfUtil.msgError("El docente ya ha sido asignado");
		return false;
	    }
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

    public ProduccionDTO getProduccionDTO() {
	return produccionDTO;
    }

    public void setProduccionDTO(ProduccionDTO produccionDTO) {
	this.produccionDTO = produccionDTO;
    }

    public ProduccionDTO getProduccionSeleccionada() {
	return produccionSeleccionada;
    }

    public void setProduccionSeleccionada(ProduccionDTO produccionSeleccionada) {
	this.produccionSeleccionada = produccionSeleccionada;
    }

    public List<ProduccionDTO> getListaProduccionDTO() {
	return listaProduccionDTO;
    }

    public void setListaProduccionDTO(List<ProduccionDTO> listaProduccionDTO) {
	this.listaProduccionDTO = listaProduccionDTO;
    }

    public List<DocenteProduccionDTO> getListaDocenteProduccionDTO() {
	return listaDocenteProduccionDTO;
    }

    public void setListaDocenteProduccionDTO(
	    List<DocenteProduccionDTO> listaDocenteProduccionDTO) {
	this.listaDocenteProduccionDTO = listaDocenteProduccionDTO;
    }

    public List<DocenteProduccionDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    public void setVistaParticipaciones(
	    List<DocenteProduccionDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

}