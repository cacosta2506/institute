package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.model.map.MapModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CantonIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParroquiaIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ProvinciaIesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.LibroDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDistribucionFisicaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoSedeEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "adminSedeIesDistriFisicaController")
public class AdministrarSedeIesDistribucionFisicaController implements
        Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarSedeIesDistribucionFisicaController.class
	            .getSimpleName());

    private SedeIesDistribucionFisicaDTO sedeIesDisFisicaDto;
    private SedeIesDTO sedeIesDto;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;

    private List<SedeIesDistribucionFisicaDTO> listaSedeIesDisFisDto;
    private List<SedeIesDTO> listaSedeIesDto;
    private List<ProvinciaIesDTO> listaProvinciaIes;
    private List<CantonIesDTO> listaCantonIesDto;
    private List<ParroquiaIesDTO> listaParroquiaIesDto;
    private List<CantonIesDTO> listaCantonSede;

    private String tipoSeleccionada;
    private final String mensajeAdvertencia = "";
    private String[] tipoEnum;

    private String usuario;
    private String opcion;
    private Long idInformacionIes;
    private Long idSedeSeleccionada;
    private Long idProvinciaSelec;
    private Long idCantonSelect;
    private Long idParroquiaSelect;
    private Long idProvinciaSede = 0L;
    private Long idCantonSede = 0L;
    private Long idSedeIes;
    private int indiceTab;
    private boolean verTabDetalle;
    private boolean deshabilitarCampus;
    private FaseIesDTO faseIesDTO;
    private Boolean alertaEvaluador = false;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    // uso de mapas
    private MapModel miMapa;
    private String latLong;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    public AdministrarSedeIesDistribucionFisicaController() {
	limpiarDatos();
	sedeIesDto = new SedeIesDTO();
	sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
	listaSedeIesDisFisDto = new ArrayList<SedeIesDistribucionFisicaDTO>();
	listaSedeIesDto = new ArrayList<SedeIesDTO>();
	listaProvinciaIes = new ArrayList<ProvinciaIesDTO>();
	listaCantonIesDto = new ArrayList<CantonIesDTO>();
	listaCantonSede = new ArrayList<CantonIesDTO>();
	listaParroquiaIesDto = new ArrayList<ParroquiaIesDTO>();
    }

    @PostConstruct
    public void start() {

	try {
	    alertaEvaluador = false;
	    alertaUsuarioIes = false;

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.idInformacionIes = informacionIesDto.getId();
	    this.faseIesDTO = controller.getFaseIesDTO();
	    llenarProvincia();
	    llenarDatos();

	    String perfil = controller.getPerfil().getNombre();

	    if (perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    if (perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }
	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void llenarDatos() {
	tipoEnum = new String[3];
	tipoEnum[0] = TipoSedeEnum.CAMPUS.getValue();
	tipoEnum[1] = TipoSedeEnum.CENTRO_APOYO.getValue();
	tipoEnum[2] = TipoSedeEnum.EDIFICACION.getValue();

	// tipoEdificacionEnum = new String[2];
	// tipoEdificacionEnum[0] = TipoEdificacionEnum.CASA.getValue();
	// tipoEdificacionEnum[1] = TipoEdificacionEnum.EDIFICIO.getValue();

	// tipoPropiedadEnum = new String[3];
	// tipoPropiedadEnum[0] = TipoPropiedadEnum.A_NOMBRE_DE_LA_INSTITUCION
	// .getValue();
	// tipoPropiedadEnum[1] = TipoPropiedadEnum.ARRENDADO.getValue();
	// tipoPropiedadEnum[2] = TipoPropiedadEnum.COMODATO.getValue();
    }

    public void limpiarDatos() {
	this.idSedeSeleccionada = 0L;
	this.idProvinciaSelec = 0L;
	this.idCantonSelect = 0L;
    }

    public void nuevaSedeIesDistriFisica() {
	if (!validarDatosSedeIes(sedeIesDto)) {
	    return;
	}
	idProvinciaSelec = idProvinciaSede;
	llenarCanton(idProvinciaSelec);
	sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
	tipoSeleccionada = null;
	// tipoEdificacionSelec = null;
	// tipoPropiedad = null;
	idCantonSelect = 0L;
	idParroquiaSelect = 0L;
	RequestContext.getCurrentInstance().reset(
	        "formSedeIesDetalleDistriFisica");
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgSedeIesDistriFisica.show()");
    }

    public void editarSedeIesDistriFisica() {
	// RequestContext.getCurrentInstance().reset(
	// "formSedeIesDetalleDistriFisica");

	idProvinciaSelec = sedeIesDisFisicaDto.getParroquia().getCanton()
	        .getProvincia().getId();
	llenarCanton(idProvinciaSelec);
	idCantonSelect = sedeIesDisFisicaDto.getParroquia().getCanton().getId();
	llenarParroquia(idCantonSelect);
	idParroquiaSelect = sedeIesDisFisicaDto.getParroquia().getId();

	tipoSeleccionada = sedeIesDisFisicaDto.getTipo().getValue();

    }

    public void verificarEliminacionCampus(javax.faces.event.ActionEvent e) {

	List<LibroDTO> libros = registroServicio
	        .obtenerLibroPorSedeDistribucionFisica(sedeIesDisFisicaDto
	                .getId());

	String mensajeAdvertencia = "";
	if (libros != null && !libros.isEmpty()) {
	    mensajeAdvertencia = "libros";
	}

	if (!"".equals(mensajeAdvertencia)) {
	    JsfUtil.msgError("Aunque tenga referencias con: "
		    + mensajeAdvertencia);
	}
    }

    public void eliminarSedeIesDistriFisica() {

	if (null == sedeIesDisFisicaDto.getId()) {
	    return;
	}
	AuditoriaDTO auditorDto = new AuditoriaDTO();
	auditorDto.setUsuarioModificacion(usuario);
	auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	sedeIesDisFisicaDto.setAuditoriaDTO(auditorDto);
	sedeIesDisFisicaDto.setActivo(false);

	try {
	    sedeIesDisFisicaDto.setSedeIes(sedeIesDto);
	    sedeIesDisFisicaDto.setFaseIesDTO(faseIesDTO);
	    sedeIesDisFisicaDto = registroServicio
		    .registrarSedeIesDistribucionFisica(sedeIesDisFisicaDto);

	    if (null != sedeIesDisFisicaDto.getId()) {
		JsfUtil.msgInfo("Registro eliminado correctamente");
		sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
		cargarDetalleDistribucionFisica(sedeIesDto.getId());
	    } else {
		JsfUtil.msgError("Error al eliminar Sede Ies Distribución Fisica");
	    }

	} catch (ServerException e) {
	    JsfUtil.msgError("Error al eliminar, consulte con el Administrador del sistema");
	}
    }

    public void guardarSedeIes() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (sedeIesDto.getId() != null
		    && !sedeIesDto.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede modificar el registro en fase de Rectificación.");
		return;
	    }
	}

	if (!this.validarDatosSedeIes(sedeIesDto)) {
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
	    sedeIesDto.setSedeIesDistribucionFisicasDTO(listaSedeIesDisFisDto);
	    sedeIesDto.setFaseIesDTO(faseIesDTO);

	    ListaIesController listaIesController = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");

	    String codigo = listaIesController.getIes().getCodigo() + "-"
		    + sedeIesDto.getTipoSede().toString().substring(0, 3) + "-"
		    + obtenerCodigoSecuencialSede();
	    LOG.info("codigo===> " + codigo);
	    sedeIesDto.setCodigo(codigo);

	    LOG.info("idCantonSede: " + idCantonSede);
	    sedeIesDto.setIdCanton(idCantonSede);
	    sedeIesDto = registroServicio.registrarSedeIes(sedeIesDto);
	    deshabilitarCampus = false;
	    if (null != sedeIesDto.getId()) {

		JsfUtil.msgInfo("Registro almacenado correctamente)");
		if (listaSedeIesDisFisDto.isEmpty()) {
		    JsfUtil.msgInfo("Ingrese por lo menos una Ubicación Física");
		}
		idSedeSeleccionada = sedeIesDto.getId();
		listaSedeIesDisFisDto.clear();
		opcion = "E";
	    } else {
		JsfUtil.msgError("Error al Guardar Sede IES");
	    }
	} catch (ServicioException e) {
	    sedeIesDto.setCodigo(null);
	    JsfUtil.msgError("Error al Guardar Sede IES, consulte con el Administrador del Sistema");
	} catch (Exception e) {

	    JsfUtil.msgError("Error al Guardar Sede IES, consulte con el Administrador del Sistema: "
		    + e.getMessage());
	    e.printStackTrace();
	    sedeIesDto.setCodigo(null);
	}
    }

    private String obtenerCodigoSecuencialSede() {
	cargarSedeIes();
	int secuencial = 0;
	for (SedeIesDTO sede : listaSedeIesDto) {
	    if (sede.getCodigo() != null) {
		LOG.info("CODIGO: " + sede.getCodigo().length());
		int secuencialLista = Integer.parseInt(sede.getCodigo()
		        .substring(sede.getCodigo().length() - 3));
		if (secuencialLista > secuencial) {
		    secuencial = secuencialLista;
		}
	    }
	}
	String codigoSecuencial = String.valueOf(secuencial + 1);
	if (codigoSecuencial.length() == 1) {
	    codigoSecuencial = "0" + codigoSecuencial;
	}
	// else if (codigoSecuencial.length() == 2) {
	// codigoSecuencial = "0" + codigoSecuencial;
	// }
	return codigoSecuencial;

    }

    public boolean validarDatosSedeIes(SedeIesDTO sedeIesv) {

	if (null == sedeIesv.getNombre() || sedeIesv.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre");
	    return false;
	}

	if (null == sedeIesv.getTipoSede() || sedeIesv.getTipoSede().equals("")) {
	    JsfUtil.msgError("Seleccione el Tipo");
	    return false;
	}
	if (null == idProvinciaSede || 0L == idProvinciaSede) {
	    JsfUtil.msgError("Seleccione la provincia");
	    return false;
	}
	if (null == idCantonSede || 0L == idCantonSede) {
	    JsfUtil.msgError("Seleccione el cantón");
	    return false;
	}

	if (sedeIesv.getFechaCreacion() instanceof Date) {
	    if (sedeIesv.getFechaCreacion() == null) {
		JsfUtil.msgError("Ingrese la fecha de Creación");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Ingrese la fecha de Creación correctamente");
	    return false;
	}

	Calendar fechaActual = Calendar.getInstance();
	fechaActual = asignarHoraCero(fechaActual);
	if (sedeIesv.getFechaCreacion().compareTo(fechaActual.getTime()) >= 0) {
	    JsfUtil.msgError("La fecha de creación debe ser menor fecha actual.");
	    return false;
	}

	AdministrarSedeIesController controller = (AdministrarSedeIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("adminSedeIesController");
	for (SedeIesDTO sedes : controller.getListaSedeIesDto()) {
	    Long idProvincia = 0L;
	    // if (sedes.getIdCanton() != null) {
	    // CantonIesDTO canton = catalogoServicio.obtenerCantonDTO(sedes
	    // .getIdCanton());
	    // idProvincia = canton.getProvincia().getId();
	    // }
	    if ("MATRIZ".equals(sedeIesDto.getTipoSede().toString())
		    && "MATRIZ".equals(sedes.getTipoSede().toString())
		    && !sedes.getId().equals(sedeIesDto.getId())
		    && idProvincia.equals(idProvinciaSede)) {
		JsfUtil.msgError("Ya existe una sede de tipo matriz para esta provincia");
		return false;
	    }
	}

	return true;
    }

    public void guardarSedeIesDistriFisica() {

	try {
	    if (!this.validarDatos(sedeIesDisFisicaDto)) {
		return;
	    }

	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    sedeIesDisFisicaDto.setAuditoriaDTO(auditorDto);
	    sedeIesDisFisicaDto.setActivo(true);
	    ParroquiaIesDTO parroquiaDto = new ParroquiaIesDTO();
	    parroquiaDto.setId(idParroquiaSelect);
	    sedeIesDisFisicaDto.setParroquia(parroquiaDto);
	    sedeIesDisFisicaDto.setTipo(TipoSedeEnum.parse(tipoSeleccionada));

	    if (null == sedeIesDto.getId()) {
		opcion = "N";
		if (sedeIesDisFisicaDto.getId() == null
		        || sedeIesDisFisicaDto.getCodigo() == null) {
		    sedeIesDisFisicaDto.setCodigo(sedeIesDto.getCodigo() + "-"
			    + obtenerCodigoProvincia() + "-"
			    + obtenerCodigoCanton() + "-"
			    + obtenerCodigoSecuencialCampus());
		}
		listaSedeIesDisFisDto.add(sedeIesDisFisicaDto);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgSedeIesDistriFisica.hide()");
		return;
	    } else {
		opcion = "E";
		sedeIesDisFisicaDto.setSedeIes(sedeIesDto);
		sedeIesDisFisicaDto.setFaseIesDTO(faseIesDTO);
		if (sedeIesDisFisicaDto.getId() == null
		        || sedeIesDisFisicaDto.getCodigo() == null) {
		    sedeIesDisFisicaDto.setCodigo(sedeIesDto.getCodigo() + "-"
			    + obtenerCodigoProvincia() + "-"
			    + obtenerCodigoCanton() + "-"
			    + obtenerCodigoSecuencialCampus());
		} else {
		    if (sedeIesDisFisicaDto.getId() != null) {
			String[] codigo = sedeIesDisFisicaDto.getCodigo()
			        .split("-");
			if (codigo.length == 6) {
			    sedeIesDisFisicaDto.setCodigo(sedeIesDto
				    .getCodigo()
				    + "-"
				    + obtenerCodigoProvincia()
				    + "-"
				    + obtenerCodigoCanton() + "-" + codigo[5]);
			}
		    }
		}

		sedeIesDisFisicaDto = registroServicio
		        .registrarSedeIesDistribucionFisica(sedeIesDisFisicaDto);

		if (null != sedeIesDisFisicaDto.getId()) {
		    JsfUtil.msgInfo("Registro almacenado correctamente");
		    sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
		    cargarDetalleDistribucionFisica(sedeIesDto.getId());
		    RequestContext context = RequestContext
			    .getCurrentInstance();
		    context.execute("dlgSedeIesDistriFisica.hide()");
		} else {
		    JsfUtil.msgError("Error al guardar el registro. Consulte con el administrador");
		}
	    }

	    // if (null == sedeIesDto.getId()) {
	    // opcion = "N";
	    // listaSedeIesDisFisDto.add(sedeIesDisFisicaDto);
	    // RequestContext context = RequestContext.getCurrentInstance();
	    // context.execute("dlgSedeIesDistriFisica.hide()");
	    // return;
	    // } else {
	    // try {
	    // opcion = "E";
	    // sedeIesDisFisicaDto.setSedeIes(sedeIesDto);
	    // sedeIesDisFisicaDto.setIdFaseIes(faseIesDTO.getId());
	    // // COD_SEDE,PRO,CANT, SEQ
	    // if (sedeIesDisFisicaDto.getId() == null) {
	    // sedeIesDisFisicaDto.setCodigo(sedeIesDto.getCodigo() + "-"
	    // + idProvinciaSelec + "-" + idCantonSelect + "-"
	    // + obtenerCodigoSecuencialCampus());
	    // }
	    // sedeIesDisFisicaDto = registroServicio
	    // .registrarSedeIesDistribucionFisica(sedeIesDisFisicaDto);
	    //
	    // if (null != sedeIesDisFisicaDto.getId()) {
	    // JsfUtil.msgInfo("Registro almacenado correctamente, ");
	    // sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
	    // cargarDetalleDistribucionFisica(sedeIesDto.getId());
	    // RequestContext context = RequestContext
	    // .getCurrentInstance();
	    // context.execute("dlgSedeIesDistriFisica.hide()");
	    // } else {
	    // JsfUtil.msgError("Error al Guardar Sede Ies Distribución Fisica");
	    // }
	    //
	    // } catch (ServerException e) {
	    // e.printStackTrace();
	    // }
	    // }
	} catch (ServerException e) {
	    JsfUtil.msgError("Error al guardar el registro. Consulte con el administrador");
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar el registro. Consulte con el administrador");
	}

    }

    private String obtenerCodigoSecuencialCampus() {
	// cargarSedeIesDisFisica(sedeIesDto.getId());
	// int secuencial = 0;
	// for (SedeIesDistribucionFisicaDTO sede : listaSedeIesDisFisDto) {
	// if (sede.getCodigo() != null) {
	// int secuencialLista = Integer.parseInt(sede.getCodigo()
	// .substring(sede.getCodigo().length() - 3));
	// if (secuencialLista > secuencial) {
	// secuencial = secuencialLista;
	// }
	// }
	// }
	// String codigoSecuencial = String.valueOf(secuencial + 1);
	// if (codigoSecuencial.length() == 1) {
	// codigoSecuencial = "0" + codigoSecuencial;
	// }
	// // else if (codigoSecuencial.length() == 2) {
	// // codigoSecuencial = "0" + codigoSecuencial;
	// // }
	// return codigoSecuencial;

	List<SedeIesDistribucionFisicaDTO> listaSedeIesDisFisDtoSecuancial = registroServicio
	        .obtenerTodasSedeDistribucionFisica(sedeIesDto.getId());
	int secuencial = 0;
	for (SedeIesDistribucionFisicaDTO sede : listaSedeIesDisFisDtoSecuancial) {
	    if (sede.getCodigo() != null) {
		int secuencialLista = Integer.parseInt(sede.getCodigo()
		        .substring(sede.getCodigo().length() - 2));
		if (secuencialLista > secuencial) {
		    secuencial = secuencialLista;
		}
	    }
	}
	String codigoSecuencial = String.valueOf(secuencial + 1);
	if (codigoSecuencial.length() == 1) {
	    codigoSecuencial = "0" + codigoSecuencial;
	}
	// else if (codigoSecuencial.length() == 2) {
	// codigoSecuencial = "0" + codigoSecuencial;
	// }
	return codigoSecuencial;
    }

    private String obtenerCodigoProvincia() {
	for (ProvinciaIesDTO provincia : listaProvinciaIes) {
	    if (provincia.getId().equals(idProvinciaSelec)) {
		return provincia.getCodigo();
	    }
	}
	return null;
    }

    private String obtenerCodigoCanton() {
	for (CantonIesDTO canton : listaCantonIesDto) {
	    if (canton.getId().equals(idCantonSelect)) {
		return canton.getCodigo();
	    }
	}
	return null;
    }

    public void cargarSedeIesDisFisica(Long idSede) {
	try {
	    this.listaSedeIesDisFisDto.clear();
	    SedeIesDTO sedeIesCompleta = new SedeIesDTO();
	    sedeIesCompleta = registroServicio.obtenerSedeIesPorId(idSede);
	    if (!sedeIesCompleta.getSedeIesDistribucionFisicasDTO().isEmpty()) {
		listaSedeIesDisFisDto.addAll(sedeIesCompleta
		        .getSedeIesDistribucionFisicasDTO());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar Sede Ies Distribucion Fisica"
		    + e.getLocalizedMessage());
	}
    }

    public void cargarDetalleDistribucionFisica(Long idSede) {
	try {
	    this.listaSedeIesDisFisDto.clear();
	    listaSedeIesDisFisDto = registroServicio
		    .obtenerSedeDistribucionFisica(idSede);
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar Sede Ies Distribucion Fisica"
		    + e.getLocalizedMessage());
	}
    }

    public void cargarSedeIesDisFisCombo() {
	// listaSedeIesDisFisDto = new
	// ArrayList<SedeIesDistribucionFisicaDTO>();
	cargarSedeIesDisFisica(idSedeSeleccionada);
    }

    public void cargarSedeIes() {
	try {
	    this.listaSedeIesDto.clear();
	    this.listaSedeIesDto = registroServicio
		    .obtenerSedesIes(idInformacionIes);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    public void salirSedeIesDetalleDistriFisica() {

    }

    public void regresarSedeIesCabecera() {

	limpiarDatos();
	sedeIesDto = new SedeIesDTO();
	sedeIesDisFisicaDto = new SedeIesDistribucionFisicaDTO();
	listaSedeIesDisFisDto.clear();
	// listaSedeIesDisFisDtoTemp.clear();
	AdministrarSedeIesController controller = (AdministrarSedeIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("adminSedeIesController");
	controller.cargarSedeIes(informacionIesDto.getId());

	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/administracionSedeIes.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public boolean validarDatos(SedeIesDistribucionFisicaDTO sede) {

	// if (null != sede.getId()) {
	// for (SedeIesDistribucionFisicaDTO sedeIes : listaSedeIesDisFisDto) {
	// if (sedeIes.getCodigo().equals(sede.getCodigo())) {
	// JsfUtil.msgAdvert("El Código ya esta ingresado");
	// return false;
	// }
	// }
	// }

	if (null == idSedeSeleccionada) {
	    JsfUtil.msgError("No esta asignado el ID de SedeIes");
	    return false;
	}

	if (null == sede.getNombre() || sede.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el Nombre");
	    return false;
	}

	if (null == sede.getAreaUbicacion()
	        || sede.getAreaUbicacion().equals("")) {
	    JsfUtil.msgError("Ingrese Area Ubicación");
	    return false;
	}

	if (null == sede.getDireccion()) {
	    JsfUtil.msgError("Ingrese la Dirección");
	    return false;
	}

	if (null == tipoSeleccionada) {
	    JsfUtil.msgError("Selecciones Tipo");
	    return false;
	}

	if (sede.getTotalSitiosBiblioteca() instanceof Number) {
	    if (sede.getTotalSitiosBiblioteca() < 0) {
		JsfUtil.msgError("Números de Sitios Biblioteca no puede ser negativo");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Número Sitios Biblioteca debe ser numerico");
	    return false;
	}

	if (sede.getNumSitiosBibliotecaInternet() instanceof Number) {
	    if (sede.getNumSitiosBibliotecaInternet() < 0) {
		JsfUtil.msgError("Números de Sitios Biblioteca con Internet no puede ser negativo");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Número Sitios Biblioteca con Internet debe ser numerico");
	    return false;
	}

	if (sede.getNumTotalAulas() instanceof Number) {
	    // if (sede.getNumTotalAulas() == 0) {
	    // JsfUtil.msgError("Ingrese Número Total de Aulas");
	    // return false;
	    // }
	    if (sede.getNumTotalAulas() < 0) {
		JsfUtil.msgError("Números Total de Aulas no puede ser negativo");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Número Total de Aulas debe ser numerico");
	    return false;
	}

	if (sede.getNumTotalSitiosTrabajoDtc() instanceof Number) {
	    // if (sede.getNumTotalSitiosTrabajoDtc() == 0) {
	    // JsfUtil.msgError("Ingrese Número Total de Trabajo Docente TC");
	    // return false;
	    // }
	    if (sede.getNumTotalSitiosTrabajoDtc() < 0) {
		JsfUtil.msgError("Número Total de Trabajo Docente TC no puede ser negativo");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Número Total de Trabajo Docente TC debe ser numerico");
	    return false;
	}

	if (sede.getNumComputadoresEstudiantes() instanceof Number) {
	    // if (sede.getNumComputadoresEstudiantes() == 0) {
	    // //
	    // JsfUtil.msgError("Ingrese Número de Computadores para Estudiantes");
	    // // return false;
	    // // }
	    if (sede.getNumComputadoresEstudiantes() < 0) {
		JsfUtil.msgError("Número de Computadores para Estudiantes no puede ser negativo");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Número de Computadores para Estudiantes debe ser numerico");
	    return false;
	}

	LOG.info("Fecha Creación: " + sede.getFechaCreacion());
	if (null == sede.getFechaCreacion()) {
	    JsfUtil.msgError("Ingrese Fecha de Creación");
	    return false;
	}

	if (null == idParroquiaSelect || idParroquiaSelect.equals(0L)) {
	    JsfUtil.msgError("Seleccione la Parroquia");
	    return false;
	}

	return true;
    }

    public void obtenerCanton() {
	if (null != idProvinciaSelec && !idProvinciaSelec.equals("")) {
	    this.llenarParroquia(0L);
	    this.llenarCanton(idProvinciaSelec);
	} else {
	    this.listaCantonIesDto = new ArrayList<CantonIesDTO>();
	    this.listaParroquiaIesDto = new ArrayList<ParroquiaIesDTO>();
	}
    }

    public void obtenerCantonSede() {
	if (null != idProvinciaSede && !idProvinciaSede.equals("")) {
	    this.llenarCantonSede(idProvinciaSede);
	} else {
	    this.listaCantonSede = new ArrayList<CantonIesDTO>();
	}
    }

    public void llenarCantonSede(Long idProvincia) {
	try {
	    listaCantonSede.clear();
	    this.listaCantonSede = catalogoServicio
		    .obtenerCantonIesDTO(idProvincia);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Canton."
		    + e.getLocalizedMessage());
	}
    }

    public void obtenerParroquia() {
	if (null != idCantonSelect && !idCantonSelect.equals("")) {
	    this.llenarParroquia(idCantonSelect);
	} else {
	    this.listaParroquiaIesDto = new ArrayList<ParroquiaIesDTO>();
	}
    }

    public void llenarProvincia() {
	try {
	    this.listaProvinciaIes.clear();
	    this.listaProvinciaIes = catalogoServicio
		    .obtenerProvinciaIesDTO(1L);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Provincias."
		    + e.getLocalizedMessage());
	}
    }

    public void llenarParroquia(Long idCanton) {
	try {
	    listaParroquiaIesDto.clear();
	    this.listaParroquiaIesDto = catalogoServicio
		    .obtenerParroquiaIes(idCanton);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Parroquia."
		    + e.getLocalizedMessage());
	}
    }

    public void llenarCanton(Long idProvincia) {
	try {
	    listaCantonIesDto.clear();
	    this.listaCantonIesDto = catalogoServicio
		    .obtenerCantonIesDTO(idProvinciaSelec);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Canton."
		    + e.getLocalizedMessage());
	}
    }

    public void editarSedeIes() {
	deshabilitarCampus = false;
	CantonIesDTO canton = catalogoServicio.obtenerCantonDTO(sedeIesDto
	        .getIdCanton());
	idCantonSede = canton.getId();
	idProvinciaSede = canton.getProvincia().getId();
	llenarCantonSede(idProvinciaSede);
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	// LOG.info("editar distribucion fisica");
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/administracionSedeIesDistribucionFisica.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Metodo para dibujar mapas
     */
    // public void dibujaMapa() {
    // latLong = sedeIesDto.getLatitud() + ", " + sedeIesDto.getLongitud();
    // LatLng coordenada = new LatLng(sedeIesDto.getLatitud(),
    // sedeIesDto.getLongitud());
    // this.miMapa = new DefaultMapModel();
    // miMapa.addOverlay(new Marker(coordenada, sedeIesDto.getNombre()));
    // }

    /**
     * @return the sedeIesDisFisicaDto
     */
    public SedeIesDistribucionFisicaDTO getSedeIesDisFisicaDto() {
	return sedeIesDisFisicaDto;
    }

    /**
     * @param sedeIesDisFisicaDto
     *            the sedeIesDisFisicaDto to set
     */
    public void setSedeIesDisFisicaDto(
	    SedeIesDistribucionFisicaDTO sedeIesDisFisicaDto) {
	this.sedeIesDisFisicaDto = sedeIesDisFisicaDto;
    }

    /**
     * @return the listaSedeIesDisFisDto
     */
    public List<SedeIesDistribucionFisicaDTO> getListaSedeIesDisFisDto() {
	{
	    if (!opcion.equals("N")) {
		cargarDetalleDistribucionFisica(idSedeSeleccionada);
	    }
	}

	return listaSedeIesDisFisDto;
    }

    /**
     * @param listaSedeIesDisFisDto
     *            the listaSedeIesDisFisDto to set
     */
    public void setListaSedeIesDisFisDto(
	    List<SedeIesDistribucionFisicaDTO> listaSedeIesDisFisDto) {
	this.listaSedeIesDisFisDto = listaSedeIesDisFisDto;
    }

    /**
     * @return the sedeIesDto
     */
    public SedeIesDTO getSedeIesDto() {
	if (null != sedeIesDto.getId()) {
	    // LOG.info("cargando cabecera detalle..");
	    idSedeSeleccionada = sedeIesDto.getId();
	    cargarDetalleDistribucionFisica(idSedeSeleccionada);
	    opcion = "E";
	} else {
	    opcion = "N";
	}
	return sedeIesDto;
    }

    public void nuevaSedeIes() {
	sedeIesDto = new SedeIesDTO();
	idProvinciaSede = 0L;
	idCantonSede = 0L;
	deshabilitarCampus = true;
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/administracionSedeIesDistribucionFisica.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void validarUbicacionMapa() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("abrirMapa", false);
	if (sedeIesDisFisicaDto.getLatitud() == 0) {
	    JsfUtil.msgError("Ingrese la latitud");
	    return;
	}
	if (sedeIesDisFisicaDto.getLongitud() == 0) {
	    JsfUtil.msgError("Ingrese la longitud");
	    return;
	}
	context.addCallbackParam("abrirMapa", true);
    }

    /**
     * @param sedeIesDto
     *            the sedeIesDto to set
     */
    public void setSedeIesDto(SedeIesDTO sedeIesDto) {
	this.sedeIesDto = sedeIesDto;
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
     * @return the idSedeSeleccionada
     */
    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    /**
     * @param idSedeSeleccionada
     *            the idSedeSeleccionada to set
     */
    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
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
     * @return the indiceTab
     */
    public int getIndiceTab() {
	return indiceTab;
    }

    /**
     * @param indiceTab
     *            the indiceTab to set
     */
    public void setIndiceTab(int indiceTab) {
	this.indiceTab = indiceTab;
    }

    /**
     * @return the verTabDetalle
     */
    public boolean isVerTabDetalle() {
	return verTabDetalle;
    }

    /**
     * @param verTabDetalle
     *            the verTabDetalle to set
     */
    public void setVerTabDetalle(boolean verTabDetalle) {
	this.verTabDetalle = verTabDetalle;
    }

    /**
     * @return the listaProvinciaIes
     */
    public List<ProvinciaIesDTO> getListaProvinciaIes() {
	return listaProvinciaIes;
    }

    /**
     * @param listaProvinciaIes
     *            the listaProvinciaIes to set
     */
    public void setListaProvinciaIes(List<ProvinciaIesDTO> listaProvinciaIes) {
	this.listaProvinciaIes = listaProvinciaIes;
    }

    /**
     * @return the listaCantonIesDto
     */
    public List<CantonIesDTO> getListaCantonIesDto() {
	return listaCantonIesDto;
    }

    /**
     * @param listaCantonIesDto
     *            the listaCantonIesDto to set
     */
    public void setListaCantonIesDto(List<CantonIesDTO> listaCantonIesDto) {
	this.listaCantonIesDto = listaCantonIesDto;
    }

    /**
     * @return the listaParroquiaIesDto
     */
    public List<ParroquiaIesDTO> getListaParroquiaIesDto() {
	return listaParroquiaIesDto;
    }

    /**
     * @param listaParroquiaIesDto
     *            the listaParroquiaIesDto to set
     */
    public void setListaParroquiaIesDto(
	    List<ParroquiaIesDTO> listaParroquiaIesDto) {
	this.listaParroquiaIesDto = listaParroquiaIesDto;
    }

    /**
     * @return the idProvinciaSelec
     */
    public Long getIdProvinciaSelec() {
	return idProvinciaSelec;
    }

    /**
     * @param idProvinciaSelec
     *            the idProvinciaSelec to set
     */
    public void setIdProvinciaSelec(Long idProvinciaSelec) {
	this.idProvinciaSelec = idProvinciaSelec;
    }

    /**
     * @return the idCantonSelect
     */
    public Long getIdCantonSelect() {
	return idCantonSelect;
    }

    /**
     * @param idCantonSelect
     *            the idCantonSelect to set
     */
    public void setIdCantonSelect(Long idCantonSelect) {
	this.idCantonSelect = idCantonSelect;
    }

    /**
     * @return the idParroquiaSelect
     */
    public Long getIdParroquiaSelect() {
	return idParroquiaSelect;
    }

    /**
     * @param idParroquiaSelect
     *            the idParroquiaSelect to set
     */
    public void setIdParroquiaSelect(Long idParroquiaSelect) {
	this.idParroquiaSelect = idParroquiaSelect;
    }

    /**
     * @return the idSedeIes
     */
    public Long getIdSedeIes() {
	return idSedeIes;
    }

    /**
     * @param idSedeIes
     *            the idSedeIes to set
     */
    public void setIdSedeIes(Long idSedeIes) {
	this.idSedeIes = idSedeIes;
    }

    /**
     * @return the opcion
     */
    public String getOpcion() {
	return opcion;
    }

    /**
     * @param opcion
     *            the opcion to set
     */
    public void setOpcion(String opcion) {
	this.opcion = opcion;
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
     * @return the tipoSeleccionada
     */
    public String getTipoSeleccionada() {
	return tipoSeleccionada;
    }

    /**
     * @param tipoSeleccionada
     *            the tipoSeleccionada to set
     */
    public void setTipoSeleccionada(String tipoSeleccionada) {
	this.tipoSeleccionada = tipoSeleccionada;
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
     * @return the miMapa
     */
    public MapModel getMiMapa() {
	return miMapa;
    }

    /**
     * @param miMapa
     *            the miMapa to set
     */
    public void setMiMapa(MapModel miMapa) {
	this.miMapa = miMapa;
    }

    /**
     * @return the latLong
     */
    public String getLatLong() {
	return latLong;
    }

    /**
     * @param latLong
     *            the latLong to set
     */
    public void setLatLong(String latLong) {
	this.latLong = latLong;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public Date getFechaActual() {
	return new Date();
    }

    /**
     * @return the idProvinciaSede
     */
    public Long getIdProvinciaSede() {
	return idProvinciaSede;
    }

    /**
     * @param idProvinciaSede
     *            the idProvinciaSede to set
     */
    public void setIdProvinciaSede(Long idProvinciaSede) {
	this.idProvinciaSede = idProvinciaSede;
    }

    /**
     * @return the idCantonSede
     */
    public Long getIdCantonSede() {
	return idCantonSede;
    }

    /**
     * @param idCantonSede
     *            the idCantonSede to set
     */
    public void setIdCantonSede(Long idCantonSede) {
	this.idCantonSede = idCantonSede;
    }

    /**
     * @return the listaCantonSede
     */
    public List<CantonIesDTO> getListaCantonSede() {
	return listaCantonSede;
    }

    private Calendar asignarHoraCero(Calendar fecha) {
	fecha.set(Calendar.HOUR_OF_DAY, 0);
	fecha.set(Calendar.MINUTE, 0);
	fecha.set(Calendar.SECOND, 0);
	fecha.set(Calendar.MILLISECOND, 0);
	return fecha;
    }

    /**
     * @return the mensajeAdvertencia
     */
    public String getMensajeAdvertencia() {
	return mensajeAdvertencia;
    }

    public boolean isDeshabilitarCampus() {
	return deshabilitarCampus;
    }

    public void setDeshabilitarCampus(boolean deshabilitarCampus) {
	this.deshabilitarCampus = deshabilitarCampus;
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

    public Boolean getAlertaFaseRectificacion() {
	return alertaFaseRectificacion;
    }

    public void setAlertaFaseRectificacion(Boolean alertaFaseRectificacion) {
	this.alertaFaseRectificacion = alertaFaseRectificacion;
    }

    public void setListaCantonSede(List<CantonIesDTO> listaCantonSede) {
	this.listaCantonSede = listaCantonSede;
    }

}