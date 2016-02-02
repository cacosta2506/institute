package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoIesRazonEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.seguridad.model.dtos.PerfilDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.services.UsuarioIesCarrerasServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class ListaIesController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private CatalogoServicio catalagoServicio;

    @Autowired
    private UsuarioIesCarrerasServicio uicServicio;

    @Autowired
    private RegistroServicio registroServicio;

    private String usuario;

    private IesDTO ies;
    private CarreraIesDTO carrera;

    private List<IesDTO> listaIes;
    private List<CarreraIesDTO> carreras;
    private PerfilDTO perfil;
    private Long idPerfilSeleccionado;
    private List<InformacionCarreraDTO> listaInfoCarrerasDTO;
    private FaseIesDTO faseIesDTO;
    private Long idAplicacion;

    public ListaIesController() {
	listaIes = new ArrayList<IesDTO>();
	carreras = new ArrayList<CarreraIesDTO>();
	ies = new IesDTO();
	carrera = new CarreraIesDTO();
	perfil = new PerfilDTO();
	faseIesDTO = new FaseIesDTO();
    }

    @PostConstruct
    public void context() {
	this.usuario = SecurityContextHolder.getContext().getAuthentication()
	        .getName();

	cargarIes();

	IngresoController iController = (IngresoController) JsfUtil
	        .obtenerObjetoSesion("ingresoController");

	this.idAplicacion = iController.getAplicacionDTO().getId();

    }

    public void cargarIes() {
	this.listaIes.clear();

	try {
	    this.listaIes = uicServicio.obtenerIesPorUsuario(usuario,
		    TipoIesRazonEnum.INSTITUTO.getValue());

	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void cargarCarrerasPorIes() {
	String salida = "";

	tomarFaseEvaluacion();
	carreras.clear();

	try {
	    perfil = uicServicio.obtenerPerfil(idPerfilSeleccionado);
	    if (perfil.getNombre().startsWith("IES")) {
		salida = "index.jsf";
	    } else if (perfil.getNombre().startsWith("CAR")) {

		// carreras = catalagoServicio.obtenerCarrerasPorUsuarioIes(
		// this.ies.getId(), usuario);
		InformacionIesDTO infoIesDTO = registroServicio
		        .obtenerInformacionIesPorIes(ies);
		listaInfoCarrerasDTO = registroServicio
		        .obtenerInformacionCarreraPorInformacionIes(infoIesDTO
		                .getId());
		salida = "listaCarreras.jsf";
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}

	ExternalContext ectx = FacesContext.getCurrentInstance()
	        .getExternalContext();

	try {
	    ectx.redirect("/" + ectx.getContextName() + "/" + salida);
	} catch (IOException e) {
	    e.printStackTrace();
	    return;
	}
    }

    /**
     * 
     * Para no ingresar urls sin haber seleccionado primero la ies
     * 
     * @author eteran
     * @version 29/04/2014 - 10:37:54
     * @param event
     */
    public void retornarListaIes(ComponentSystemEvent event) {
	if (ies != null && ies.getId() == null) {
	    try {
		FacesContext fctx = FacesContext.getCurrentInstance();
		ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) fctx
		        .getApplication().getNavigationHandler();
		handler.performNavigation("listaIes?faces-redirect=true");
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    /**
     * @return the idAplicacion
     */
    public Long getIdAplicacion() {
	return idAplicacion;
    }

    /**
     * @param idAplicacion
     *            the idAplicacion to set
     */
    public void setIdAplicacion(Long idAplicacion) {
	this.idAplicacion = idAplicacion;
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
     * @return the ies
     */
    public IesDTO getIes() {
	return ies;
    }

    /**
     * @param ies
     *            the ies to set
     */
    public void setIes(IesDTO ies) {
	this.ies = ies;
	carrera = new CarreraIesDTO();
	cargarMenus();
    }

    /**
     * @return the carrera
     */
    public CarreraIesDTO getCarrera() {
	return carrera;
    }

    public void tomarFaseEvaluacion() {
	IngresoController controller = (IngresoController) JsfUtil
	        .obtenerObjetoSesion("ingresoController");
	try {
	    faseIesDTO = catalagoServicio.obtenerFaseIesActual(
		    this.ies.getId(), controller.getIdProceso(), new Date());
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void cargarMenus() {
	ExternalContext ectx = FacesContext.getCurrentInstance()
	        .getExternalContext();

	IngresoController iController = (IngresoController) JsfUtil
	        .obtenerObjetoSesion("ingresoController");

	tomarFaseEvaluacion();

	if (iController != null) {
	    iController.setIdIes(ies.getId());
	    iController.cargarFaseEvaluacion();

	    MenuController mController = (MenuController) JsfUtil
		    .obtenerObjetoSesion("menuController");

	    if (mController != null) {
		mController.cargarMenus();
	    }
	}
	HttpSession session = (HttpSession) ectx.getSession(true);
	session.removeAttribute("mallasController");
    }

    public String seleccionarCarrera() {
	if (carrera.getNombre().equals("CARRERA TELCONET")) {
	    return "index?faces-redirect=true";
	}
	return "index?faces-redirect=true";
    }

    /**
     * @param carrera
     *            the carrera to set
     */
    public void setCarrera(CarreraIesDTO carrera) {
	this.carrera = carrera;
    }

    /**
     * @return the listaIes
     */
    public List<IesDTO> getListaIes() {
	return listaIes;
    }

    /**
     * @param listaIes
     *            the listaIes to set
     */
    public void setListaIes(List<IesDTO> listaIes) {
	this.listaIes = listaIes;
    }

    /**
     * @return the carreras
     */
    public List<CarreraIesDTO> getCarreras() {
	return carreras;
    }

    /**
     * @param carreras
     *            the carreras to set
     */
    public void setCarreras(List<CarreraIesDTO> carreras) {
	this.carreras = carreras;
    }

    public PerfilDTO getPerfil() {
	return perfil;
    }

    public void setPerfil(PerfilDTO perfil) {
	this.perfil = perfil;
    }

    public Long getIdPerfilSeleccionado() {
	return idPerfilSeleccionado;
    }

    public void setIdPerfilSeleccionado(Long idPerfilSeleccionado) {
	this.idPerfilSeleccionado = idPerfilSeleccionado;
    }

    public List<InformacionCarreraDTO> getListaInfoCarrerasDTO() {
	return listaInfoCarrerasDTO;
    }

    public void setListaInfoCarrerasDTO(
	    List<InformacionCarreraDTO> listaInfoCarrerasDTO) {
	this.listaInfoCarrerasDTO = listaInfoCarrerasDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public void redireccionarPagina() throws IOException {
	ExternalContext externalContext = FacesContext.getCurrentInstance()
	        .getExternalContext();
	HttpServletRequest request = ((HttpServletRequest) externalContext
	        .getRequest());
	HttpServletResponse response = ((HttpServletResponse) externalContext
	        .getResponse());
	String url = request.getParameter("url");

	String SESIONES_NECESARIAS = "ingresoController,"
	        + "valoresUsuarioController,logoutController,"
	        + "listaIesController,valoresUsuarioController,"
	        + "perfil,menuController,logoutController,";
	Map<String, Object> sesiones = externalContext.getSessionMap();
	for (Map.Entry<String, Object> objeto : sesiones.entrySet()) {
	    if (!SESIONES_NECESARIAS.contains(objeto.getKey())
		    && objeto.getKey().endsWith("Controller")) {
		externalContext.getSessionMap().remove(objeto.getKey());
	    }
	}
	response.sendRedirect(url);
    }

    public void validarFase(ComponentSystemEvent event) {
	FacesContext fctx = FacesContext.getCurrentInstance();
	ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) fctx
	        .getApplication().getNavigationHandler();
	MenuController mController = (MenuController) JsfUtil
	        .obtenerObjetoSesion("menuController");
	if (mController.getMenus().isEmpty()) {
	    handler.performNavigation("mensajeFinFase?faces-redirect=true");
	}
    }

}
