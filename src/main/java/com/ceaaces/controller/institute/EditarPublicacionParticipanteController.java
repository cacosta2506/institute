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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocentePublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoPublicacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class EditarPublicacionParticipanteController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(EditarPublicacionParticipanteController.class
	            .getSimpleName());

    private Long idInformacionCarrera;
    private String usuario;
    private String nada;
    private PublicacionDTO publicacionDto;
    private List<PublicacionDTO> listaPublicacionDto;
    private List<DocentePublicacionDTO> listaDocentePublicacionDto;
    private String[] tipoEnum;
    private String tipoSeleccionado;
    private Boolean panelArticulo;
    private Boolean panelLibro;
    private Boolean panelOtroTipo;
    private Boolean panelCapituloLibro;
    private Boolean tipoPublicacion = false;
    private String participacion;
    private DocenteDTO docenteDto;
    private List<DocenteDTO> listaDocenteDto;
    private List<String> listaParticipacionCombo;
    private String participacionSeleccionado;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private DocentePublicacionDTO docentePublicacionDto;
    private InformacionCarreraDTO informacionCarreraDto;
    private Boolean alertaEvaluador = false;
    private String perfil;

    private List<DocentePublicacionDTO> vistaParticipaciones;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;
    private FaseIesDTO faseIesDTO;

    public EditarPublicacionParticipanteController() {

	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();

	publicacionDto = new PublicacionDTO();
	listaPublicacionDto = new ArrayList<PublicacionDTO>();
	listaDocentePublicacionDto = new ArrayList<DocentePublicacionDTO>();
	panelArticulo = new Boolean(false);
	panelLibro = new Boolean(false);
	panelOtroTipo = new Boolean(false);

	docenteDto = new DocenteDTO();
	listaDocenteDto = new ArrayList<DocenteDTO>();
	listaParticipacionCombo = new ArrayList<String>();
	docentePublicacionDto = new DocentePublicacionDTO();
	informacionCarreraDto = new InformacionCarreraDTO();
	vistaParticipaciones = new ArrayList<>();
    }

    /**
     * @return the vistaParticipaciones
     */
    public List<DocentePublicacionDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    /**
     * @param vistaParticipaciones
     *            the vistaParticipaciones to set
     */
    public void setVistaParticipaciones(
	    List<DocentePublicacionDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

    @PostConstruct
    public void start() {

	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.alertaEvaluador = false;

	    this.iesDTO = controller.getIes();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.perfil = controller.getPerfil().getNombre();

	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    iniciarValores();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void editarDocentePublicacion() {
	RequestContext.getCurrentInstance().reset("formParticipacion");

	docenteDto = new DocenteDTO();
	docenteDto.setId(docentePublicacionDto.getDocenteDTO().getId());
	docenteDto.setIdentificacion(docentePublicacionDto.getDocenteDTO()
	        .getIdentificacion());
	docenteDto.setNombres(docentePublicacionDto.getDocenteDTO()
	        .getNombres());
	docenteDto.setApellidoMaterno(docentePublicacionDto.getDocenteDTO()
	        .getApellidoMaterno());
	docenteDto.setApellidoPaterno(docentePublicacionDto.getDocenteDTO()
	        .getApellidoPaterno());
	participacionSeleccionado = docentePublicacionDto.getParticipacion();
	obtenerDocentes();
    }

    public void eliminarParticipacion() {

	if (publicacionDto.getListaDocentePublicacionDTO().size() == 1) {

	    JsfUtil.msgAdvert("No se puede eliminar, la Publicacion debe tener al menos 1 participante");
	    return;
	}

	try {
	    docenteDto.setId(docentePublicacionDto.getDocenteDTO().getId());
	    docenteDto.setIdentificacion(docentePublicacionDto.getDocenteDTO()
		    .getIdentificacion());
	    docenteDto.setNombres(docentePublicacionDto.getDocenteDTO()
		    .getNombres());
	    docenteDto.setApellidoMaterno(docentePublicacionDto.getDocenteDTO()
		    .getApellidoMaterno());
	    docenteDto.setApellidoPaterno(docentePublicacionDto.getDocenteDTO()
		    .getApellidoPaterno());
	    participacionSeleccionado = docentePublicacionDto
		    .getParticipacion();

	    DocentePublicacionDTO docentePublicacion = new DocentePublicacionDTO();

	    docentePublicacion.setDocenteDTO(docenteDto);

	    docentePublicacion.setId(docentePublicacionDto.getId());

	    docentePublicacion.setParticipacion(participacionSeleccionado);

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    docentePublicacion.setAuditoriaDTO(auditoria);

	    docentePublicacion.setActivo(false);

	    docentePublicacion.setPublicacionDTO(publicacionDto);
	    docentePublicacion.setFaseIesDTO(faseIesDTO);

	    docentePublicacion = registroServicio
		    .registrarDocentePublicacion(docentePublicacion);

	    if (null != docentePublicacion.getId()) {
		limpiarDatos();
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgParticipacion.hide()");
		JsfUtil.msgInfo("Registro eliminado correctamente.");
		obtenerListaDocentePublicacion();
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void enviarDocente(DocentePublicacionDTO docente) {
	docentePublicacionDto = docente;

    }

    public void guardarPublicacion() {

	publicacionDto.getListaDocentePublicacionDTO().clear();

	if (!validarDatos(publicacionDto)) {
	    return;
	}

	try {
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    publicacionDto.setAuditoriaDTO(auditoria);

	    publicacionDto.setActivo(true);
	    publicacionDto.setFaseIesDTO(faseIesDTO);
	    publicacionDto.setVerificarEvidencia(false);

	    publicacionDto.setTipo(TipoPublicacionEnum.parse(tipoSeleccionado));

	    InformacionCarreraDTO informacionCarrera = new InformacionCarreraDTO();
	    informacionCarrera.setId(idInformacionCarrera);
	    if (idInformacionCarrera != null) {
		publicacionDto.setInformacionIesDTO(informacionIesDto);
	    }
	    if (alertaEvaluador) {
		publicacionDto.setVerificarEvidencia(false);
	    }
	    publicacionDto = registroServicio
		    .registroPublicacion(publicacionDto);

	    if (null != publicacionDto.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		regresarPublicacion();
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
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

	// if (null == publicacion.getAnio()) {
	// msgError("Ingrese el año");
	// return false;
	// }
	//
	// if (!Util.validarEsNumerico(publicacion.getAnio().toString())) {
	// msgError("El valor del año debe ser númerico");
	// return false;
	// }
	//
	// if (!Util.validarEsPositivo(publicacion.getAnio().toString())) {
	// msgAdvert("El valor del año debe ser positivo");
	// return false;
	// }

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
	publicacionDto = new PublicacionDTO();
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
	docentePublicacionDto = new DocentePublicacionDTO();
	participacionSeleccionado = "";
	vistaParticipaciones.clear();
	try {
	    publicacionDto = registroServicio
		    .obtenerPublicacionPorId(publicacionDto.getId());
	    List<DocentePublicacionDTO> listaDocentePublica = new ArrayList<DocentePublicacionDTO>();
	    for (DocentePublicacionDTO docentePublic : publicacionDto
		    .getListaDocentePublicacionDTO()) {
		if (docentePublic.getActivo()) {
		    listaDocentePublica.add(docentePublic);
		}
	    }

	    publicacionDto.getListaDocentePublicacionDTO().clear();
	    publicacionDto.setListaDocentePublicacionDTO(listaDocentePublica);
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	obtenerDocentes();
    }

    public void obtenerDocentes() {
	ListaDocenteController controller = (ListaDocenteController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaDocenteController");
	controller.setInformacionIesDTO(this.informacionIesDto);
	controller.setInformacionCarreraDTO(this.informacionCarreraDto);
	controller.setOpcion("PUBLICACIONES");
    }

    public void obtenerParticipacionCombo() {
	listaParticipacionCombo.clear();
	listaParticipacionCombo.add("Autor");
	listaParticipacionCombo.add("Coautor");
    }

    //
    public void agregarListaParticipantes() {
	if (null == publicacionDto.getId()) {
	    JsfUtil.msgError("Participante no esta asociado a una publicación");
	    return;
	}

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

	DocentePublicacionDTO docentePublicacion = new DocentePublicacionDTO();
	docentePublicacion.setDocenteDTO(docenteDto);
	docentePublicacion.setId(docentePublicacionDto.getId());
	docentePublicacion.setParticipacion(participacionSeleccionado);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	docentePublicacion.setAuditoriaDTO(auditoria);

	docentePublicacion.setActivo(true);

	docentePublicacion.setPublicacionDTO(publicacionDto);
	docentePublicacion.setFaseIesDTO(faseIesDTO);
	LOG.info("PARTICIPANTE SELECCIONADO: " + participacionSeleccionado);
	docentePublicacion.setParticipacion(participacionSeleccionado);

	publicacionDto.getListaDocentePublicacionDTO().add(docentePublicacion);
	vistaParticipaciones.add(docentePublicacion);

	docenteDto = new DocenteDTO();
	docentePublicacionDto = new DocentePublicacionDTO();
	participacionSeleccionado = null;
    }

    public void editarParticipantes() {
	if (participacion.equals("")) {
	    JsfUtil.msgError("Debe escoger una Participación");
	    return;
	} else {
	    try {
		docentePublicacionDto.setParticipacion(participacion);
		DocentePublicacionDTO docentePublicacion = registroServicio
		        .registrarDocentePublicacion(docentePublicacionDto);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgEdicion.hide()");
		JsfUtil.msgInfo("Registro editado correctamente.");

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    //
    public void agregarParticipante() {
	int registros = 0;
	try {
	    for (DocentePublicacionDTO dp : vistaParticipaciones) {
		DocentePublicacionDTO docentePublicacion = registroServicio
		        .registrarDocentePublicacion(dp);

		if (docentePublicacion != null) {
		    ++registros;
		}
	    }

	    if (registros == vistaParticipaciones.size()) {
		// limpiarDatos();
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgParticipacion.hide()");
		JsfUtil.msgInfo("Registros almacenados correctamente.");
		obtenerListaDocentePublicacion();
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void obtenerListaDocentePublicacion() {
	try {
	    publicacionDto = registroServicio
		    .obtenerPublicacionPorId(publicacionDto.getId());
	    List<DocentePublicacionDTO> listaDocentePublica = new ArrayList<DocentePublicacionDTO>();
	    for (DocentePublicacionDTO docentePublic : publicacionDto
		    .getListaDocentePublicacionDTO()) {
		if (docentePublic.getActivo()) {
		    listaDocentePublica.add(docentePublic);
		}
	    }

	    publicacionDto.getListaDocentePublicacionDTO().clear();
	    publicacionDto.setListaDocentePublicacionDTO(listaDocentePublica);

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
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
	for (DocentePublicacionDTO participacion : publicacionDto
	        .getListaDocentePublicacionDTO()) {
	    if (participacion.getDocenteDTO().getId()
		    .equals(docenteDto.getId())) {
		JsfUtil.msgError("El docente ya ha sido asignado");
		return false;
	    }
	}

	return true;
    }

    /**
     * @return the idInformacionCarrera
     */
    public Long getIdInformacionCarrera() {
	return idInformacionCarrera;
    }

    /**
     * @param idInformacionCarrera
     *            the idInformacionCarrera to set
     */
    public void setIdInformacionCarrera(Long idInformacionCarrera) {
	this.idInformacionCarrera = idInformacionCarrera;
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
     * @return the docentePublicacionDto
     */
    public DocentePublicacionDTO getDocentePublicacionDto() {
	return docentePublicacionDto;
    }

    /**
     * @param docentePublicacionDto
     *            the docentePublicacionDto to set
     */
    public void setDocentePublicacionDto(
	    DocentePublicacionDTO docentePublicacionDto) {
	this.docentePublicacionDto = docentePublicacionDto;
	participacion = docentePublicacionDto.getParticipacion();
    }

    /**
     * @return the informacionCarreraDto
     */
    public InformacionCarreraDTO getInformacionCarreraDto() {
	return informacionCarreraDto;
    }

    /**
     * @param informacionCarreraDto
     *            the informacionCarreraDto to set
     */
    public void setInformacionCarreraDto(
	    InformacionCarreraDTO informacionCarreraDto) {
	this.informacionCarreraDto = informacionCarreraDto;
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

    public Date getFechaActual() {
	return new Date();
    }

    public Boolean getPanelOtroTipo() {
	return panelOtroTipo;
    }

    public void setPanelOtroTipo(Boolean panelOtroTipo) {
	this.panelOtroTipo = panelOtroTipo;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

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

    public Boolean getPanelCapituloLibro() {
	return panelCapituloLibro;
    }

    public void setPanelCapituloLibro(Boolean panelCapituloLibro) {
	this.panelCapituloLibro = panelCapituloLibro;
    }

    public Boolean getTipoPublicacion() {
	return tipoPublicacion;
    }

    public void setTipoPublicacion(Boolean tipoPublicacion) {
	this.tipoPublicacion = tipoPublicacion;
    }

    public String getParticipacion() {
	return participacion;
    }

    public void setParticipacion(String participacion) {
	this.participacion = participacion;
    }

}