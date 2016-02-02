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
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.data.ItemEvidenciaLocal;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.LibroDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDistribucionFisicaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoLibroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoMedioSoporteEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class LibrosController implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4967773530740528864L;

    private static final Logger LOG = Logger.getLogger(LibrosController.class
	    .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private CatalogoServicio catalogosServicio;

    private String usuario;
    private IesDTO iesDTO;
    private CarreraIesDTO carreraSeleccionada;
    private InformacionIesDTO informacionIes;
    private LibroDTO libroSeleccionado;
    private FaseDTO faseEvaluacion;
    private Evidencia archivoSeleccionado;

    private List<SedeIesDTO> listaSedeIesDto;
    private List<SedeIesDistribucionFisicaDTO> listaSedeDistribucion;
    private List<LibroDTO> listaLibros;
    private List<LibroDTO> listaLibrosFiltrados;
    private List<ItemEvidenciaLocal> listaItemEvidencias;

    private Long idSedeSeleccionada;
    private Long idSedeDistribucionSeleccionada;
    private boolean editarLibro;
    private String[] tipoMedioSoporteEnum;
    private FaseIesDTO faseIesDTO;
    private String medioSoporte;
    private Boolean alertaEvaluador = false;
    private String tipoLibro;
    private String[] listaTipo;
    private Integer totalLibros = 0;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;
    private String perfil;

    public LibrosController() {
	listaLibros = new ArrayList<LibroDTO>();
	libroSeleccionado = new LibroDTO();
	listaLibrosFiltrados = new ArrayList<LibroDTO>();
	listaItemEvidencias = new ArrayList<>();
	archivoSeleccionado = new Evidencia();
	listaSedeIesDto = new ArrayList<SedeIesDTO>();
	listaSedeDistribucion = new ArrayList<SedeIesDistribucionFisicaDTO>();
    }

    @PostConstruct
    public void start() {

	try {

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    alertaFaseRectificacion = false;
	    alertaUsuarioIes = false;

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();
	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.perfil = controller.getPerfil().getNombre();

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    cargarLibros();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void cargarLibros() {

	try {

	    tipoMedioSoporteEnum = new String[5];
	    tipoMedioSoporteEnum[0] = TipoMedioSoporteEnum.LIBRO_IMPRESO
		    .getValue();
	    tipoMedioSoporteEnum[1] = TipoMedioSoporteEnum.REVISTA_IMPRESA
		    .getValue();
	    tipoMedioSoporteEnum[2] = TipoMedioSoporteEnum.REVISTA_ELECTRONICA
		    .getValue();
	    tipoMedioSoporteEnum[3] = TipoMedioSoporteEnum.AUDIO.getValue();
	    tipoMedioSoporteEnum[4] = TipoMedioSoporteEnum.VIDEO.getValue();

	    listaTipo = new String[2];
	    listaTipo[0] = TipoLibroEnum.LIBRO.getValue();
	    listaTipo[1] = TipoLibroEnum.PUBLICACION_PERIODICA.getValue();

	    this.listaLibros.clear();
	    this.listaLibros = registroServicio
		    .obtenerLibroPorIes(informacionIes.getId());
	    this.totalLibros = this.listaLibros.size();

	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar los Libros");
	}
    }

    public void nuevoLibro() {
	libroSeleccionado = new LibroDTO();
	editarLibro = true;
	limpiarDatos();
	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    cargarSedeIes();
	    context.execute("dialogLibros.show();");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    //
    public void cargarSedeIes() {

	try {
	    this.listaSedeIesDto.clear();
	    this.listaSedeIesDto = registroServicio
		    .obtenerSedesIes(informacionIes.getId());
	    LOG.info("listaSedeIesDto.size: " + listaSedeIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    public void cargarListaSedeDistribucion() {

	try {
	    this.listaSedeDistribucion.clear();
	    this.listaSedeDistribucion = registroServicio
		    .obtenerSedeDistribucionFisica(idSedeSeleccionada);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede Distribución Física");
	}
    }

    public void guardarLibro() {
	if (!validarDatosLibros()) {
	    // errorValidacion = true;
	    return;
	}
	try {
	    if (libroSeleccionado.getTitulo() != null) {
		AuditoriaDTO audit = new AuditoriaDTO();
		audit.setFechaModificacion(new Date());
		audit.setUsuarioModificacion(usuario);

		libroSeleccionado.setActivo(true);
		libroSeleccionado.setAuditoria(audit);
		SedeIesDistribucionFisicaDTO campus = new SedeIesDistribucionFisicaDTO();
		campus.setId(idSedeDistribucionSeleccionada);
		libroSeleccionado.setSedeDistribucionFisicaDTO(campus);

		LOG.info("MEDIO SOPORTE: " + medioSoporte);
		libroSeleccionado.setTipoMedioSoporte(TipoMedioSoporteEnum
		        .parse(this.medioSoporte));
		libroSeleccionado.setTipo(TipoLibroEnum.parse(this.tipoLibro));
		LOG.info("TIPO: " + libroSeleccionado.getTipo().toString());
		LOG.info("TIPO medio Soporte: "
		        + libroSeleccionado.getTipoMedioSoporte().toString());

		libroSeleccionado.setOrigenCarga("SISTEMA");
		libroSeleccionado.setFaseIesDTO(faseIesDTO);

		libroSeleccionado = registroServicio
		        .registrarLibro(libroSeleccionado);

		JsfUtil.msgInfo("Registro almacenado correctamente");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogLibros.hide();");
		limpiarDatos();
		cargarLibros();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo almacenar el registro");
	}
    }

    public void cancelarLibro() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dialogLibros.hide();");
	editarLibro = false;
    }

    public void eliminarLibro() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (libroSeleccionado.getId() != null
		    && !libroSeleccionado.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este libro en fase de Rectificación.");
		return;
	    }
	}

	libroSeleccionado.setActivo(false);
	AuditoriaDTO audit = new AuditoriaDTO();
	audit.setUsuarioModificacion(usuario);
	audit.setFechaModificacion(new Date());
	libroSeleccionado.setAuditoria(audit);
	libroSeleccionado.setFaseIesDTO(faseIesDTO);
	LOG.info("TIPO medio Soporte: "
	        + libroSeleccionado.getTipoMedioSoporte().toString());

	try {
	    registroServicio.registrarLibro(libroSeleccionado);
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	    cargarLibros();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo eliminar el registro");
	}
    }

    public void limpiarDatos() {
	libroSeleccionado = new LibroDTO();
	this.listaSedeDistribucion.clear();
	idSedeSeleccionada = 0L;
	idSedeDistribucionSeleccionada = 0L;
	medioSoporte = null;
	tipoLibro = null;
    }

    public void cargarEdicion() {
	LOG.info("cargarEdicion..");
	LOG.info("alertaFaseRectificacion.." + alertaFaseRectificacion);
	LOG.info("alertaUsuarioIes.." + alertaUsuarioIes);
	LOG.info("libroSeleccionado..ID.." + libroSeleccionado.getId());
	LOG.info("libroSeleccionado.getFaseIesDTO().getTipo().name().."
	        + libroSeleccionado.getFaseIesDTO().getTipo().name());
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (libroSeleccionado.getId() != null
		    && !libroSeleccionado.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este libro en fase de Rectificación.");
		return;
	    }
	}

	medioSoporte = libroSeleccionado.getTipoMedioSoporte().getValue()
	        .toString();
	tipoLibro = libroSeleccionado.getTipo().getValue().toString();
	idSedeSeleccionada = libroSeleccionado.getSedeDistribucionFisicaDTO()
	        .getSedeIes().getId();
	idSedeDistribucionSeleccionada = libroSeleccionado
	        .getSedeDistribucionFisicaDTO().getId();

	cargarSedeIes();
	cargarListaSedeDistribucion();
	RequestContext.getCurrentInstance().execute("dialogLibros.show();");
    }

    public boolean validarDatosLibros() {
	boolean valido = true;
	if (libroSeleccionado.getCodigo().equals("")) {
	    JsfUtil.msgError("Ingrese código del libro");
	    return false;
	}

	if (libroSeleccionado.getTitulo().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre del libro");
	    return false;
	}

	if (libroSeleccionado.getNumEjemplares().equals(0)) {
	    JsfUtil.msgError("Ingrese el número de ejemplares");
	    return false;
	}

	if (libroSeleccionado.getNombreBibloteca().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre de la Biblioteca");
	    return false;
	}
	if (tipoLibro == null) {
	    JsfUtil.msgError("Ingrese el tipo de libro");
	    return false;
	}

	if (idSedeDistribucionSeleccionada == null
	        || idSedeDistribucionSeleccionada == 0L) {
	    JsfUtil.msgError("Seleccione la Distribución Física");
	    return false;
	}

	LibroDTO libDTO = new LibroDTO();
	libDTO = registroServicio.obtenerLibroPorCodigoYSedeDistribucion(
	        libroSeleccionado.getCodigo(), idSedeDistribucionSeleccionada);

	if (libDTO != null) {

	    if (idSedeDistribucionSeleccionada.equals(libDTO
		    .getSedeDistribucionFisicaDTO().getId())
		    && libroSeleccionado.getCodigo().equals(libDTO.getCodigo())) {
		if (libroSeleccionado.getId() == null
		        || !libroSeleccionado.getId().equals(libDTO.getId())) {

		    JsfUtil.msgError("Código Duplicado Libro existente");
		    // JsfUtil.msgError("Código de libro se encuentra ingresado para la Distribución Física "
		    // + libDTO.getSedeDistribucionFisicaDTO().getNombre());
		    return false;
		}

	    }
	}

	return valido;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public CarreraIesDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(CarreraIesDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
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

    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
    }

    public Long getIdSedeDistribucionSeleccionada() {
	return idSedeDistribucionSeleccionada;
    }

    public void setIdSedeDistribucionSeleccionada(
	    Long idSedeDistribucionSeleccionada) {
	this.idSedeDistribucionSeleccionada = idSedeDistribucionSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public List<SedeIesDistribucionFisicaDTO> getListaSedeDistribucion() {
	return listaSedeDistribucion;
    }

    public void setListaSedeDistribucion(
	    List<SedeIesDistribucionFisicaDTO> listaSedeDistribucion) {
	this.listaSedeDistribucion = listaSedeDistribucion;
    }

    public LibroDTO getLibroSeleccionado() {
	return libroSeleccionado;
    }

    public void setLibroSeleccionado(LibroDTO libroSeleccionado) {
	this.libroSeleccionado = libroSeleccionado;
    }

    public List<LibroDTO> getListaLibros() {
	return listaLibros;
    }

    public void setListaLibros(List<LibroDTO> listaLibros) {
	this.listaLibros = listaLibros;
    }

    public List<LibroDTO> getListaLibrosFiltrados() {
	return listaLibrosFiltrados;
    }

    public void setListaLibrosFiltrados(List<LibroDTO> listaLibrosFiltrados) {
	this.listaLibrosFiltrados = listaLibrosFiltrados;
    }

    public boolean isEditarLibro() {
	return editarLibro;
    }

    public void setEditarLibro(boolean editarLibro) {
	this.editarLibro = editarLibro;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public String[] getTipoMedioSoporteEnum() {
	return tipoMedioSoporteEnum;
    }

    public void setTipoMedioSoporteEnum(String[] tipoMedioSoporteEnum) {
	this.tipoMedioSoporteEnum = tipoMedioSoporteEnum;
    }

    public String getMedioSoporte() {
	return medioSoporte;
    }

    public void setMedioSoporte(String medioSoporte) {
	this.medioSoporte = medioSoporte;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public String getTipoLibro() {
	return tipoLibro;
    }

    public void setTipoLibro(String tipoLibro) {
	this.tipoLibro = tipoLibro;
    }

    public String[] getListaTipo() {
	return listaTipo;
    }

    public void setListaTipo(String[] listaTipo) {
	this.listaTipo = listaTipo;
    }

    public Integer getTotalLibros() {
	return totalLibros;
    }

    public void setTotalLibros(Integer totalLibros) {
	this.totalLibros = totalLibros;
    }
}