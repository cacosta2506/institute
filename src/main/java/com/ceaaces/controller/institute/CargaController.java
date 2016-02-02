package ec.gob.ceaaces.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.io.FilenameUtils;
import org.jsefa.DeserializationException;
import org.jsefa.Deserializer;
import org.jsefa.common.config.InitialConfigurationException;
import org.jsefa.csv.CsvIOFactory;
import org.jsefa.csv.config.CsvConfiguration;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PaisDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParroquiaIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PuebloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.TipoCargaMasivaDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.EtniaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.carga.CargaCarreraEstudianteData;
import ec.gob.ceaaces.data.carga.CargaCarreraEstudiantePeriodoData;
import ec.gob.ceaaces.data.carga.CargaDocenteData;
import ec.gob.ceaaces.data.carga.CargaEstudianteData;
import ec.gob.ceaaces.data.carga.CargaLibroData;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ArchivoCargaMasivaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudiantePeriodoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.LibroDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDistribucionFisicaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.DiscapacidadEnum;
import ec.gob.ceaaces.institutos.enumeraciones.EstadoCargaMasivaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.GeneroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoLibroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoMedioSoporteEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoSeccionEnum;
import ec.gob.ceaaces.institutos.util.Util;
import ec.gob.ceaaces.services.CargaMasivaServicio;
import ec.gob.ceaaces.services.CargaServicio;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.Constantes;
import ec.gob.ceaaces.util.JsfUtil;

/**
 * 
 * Clase que que maneja carga masiva de archivos
 * 
 * @author tfreire
 * 
 */
@ManagedBean(name = "cargaController")
public class CargaController implements Serializable {

    private static final long serialVersionUID = -8060028796456292272L;

    private static final Logger LOG = Logger.getLogger(CargaController.class
	    .getSimpleName());

    private static String ENCODING = "UTF-8";

    @Autowired
    private CargaServicio cargaServicio;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private CargaMasivaServicio cargaMasivaServicio;

    private UploadedFile uploadedFile;
    private String ruta;
    private TipoCargaMasivaDTO tipoCarga;
    private IesDTO ies;
    private String fileName;
    private InformacionIesDTO informacionIesDTO;
    private CarreraIesDTO carreraIesDTO;
    private String usuario;
    private String email;
    private Date fechaMin;
    private List<TipoCargaMasivaDTO> tiposArchivo;
    private List<ArchivoCargaMasivaDTO> listaArchivosCarga;
    private List<SelectItem> ANIOS = new ArrayList<>();
    private StreamedContent archivoCargaDescarga;
    private StreamedContent plantillaDescarga;
    private int mesInicioPeriodoAcademico;
    private int anioInicioPeriodoAcademico;
    private int anioFinPeriodoAcademico;
    private int mesFinPeriodoAcademico;
    private FaseIesDTO faseIesDTO;
    private final AuditoriaDTO auditoriaDTO;
    private final SelectItem[] MESES = { new SelectItem(0, "Enero"),
	    new SelectItem(1, "Febrero"), new SelectItem(2, "Marzo"),
	    new SelectItem(3, "Abril"), new SelectItem(4, "Mayo"),
	    new SelectItem(5, "Junio"), new SelectItem(6, "Julio"),
	    new SelectItem(7, "Agosto"), new SelectItem(8, "Septiembre"),
	    new SelectItem(9, "Octubre"), new SelectItem(10, "Noviembre"),
	    new SelectItem(11, "Diciembre") };
    private Boolean mostrarMensajeError;
    private String mensajesError;
    private boolean mostrarPeriodosAcademicos = true;

    // Listas para carga masiva
    private List<LibroDTO> librosDTO = new ArrayList<>();
    private List<PersonaDTO> personasDTO = new ArrayList<>();
    private List<EstudianteDTO> estudiantesDTO = new ArrayList<>();
    private List<CarreraEstudianteDTO> carrerasEstudiantesDTO = new ArrayList<>();
    private List<CarreraEstudiantePeriodoDTO> carreraEstudiantePeriodosDTO = new ArrayList<>();

    private List<PeriodoAcademicoDTO> periodosAcademicos = new ArrayList<>();
    private PeriodoAcademicoDTO periodoAcademicoAgregar = new PeriodoAcademicoDTO();
    private PeriodoAcademicoDTO periodoAcademicoEliminar = new PeriodoAcademicoDTO();

    private Long idPeriodoAcademico;
    private ArchivoCargaMasivaDTO archivoSeleccionado;

    public CargaController() {
	this.uploadedFile = null;
	tiposArchivo = new ArrayList<TipoCargaMasivaDTO>();
	tipoCarga = new TipoCargaMasivaDTO();
	listaArchivosCarga = new ArrayList<>();
	auditoriaDTO = new AuditoriaDTO();
	faseIesDTO = new FaseIesDTO();
    }

    @PostConstruct
    public void start() {
	try {
	    ParametroDTO parametro = catalogoServicio
		    .obtenerParametroPorCodigoYReferencia(
		            ParametroEnum.URL_CARGA_MASIVA.getValor(),
		            TipoReferenciaEnum.INST.getValor());
	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");

	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());
	    carreraIesDTO = controller.getCarrera();
	    ies = controller.getIes();
	    usuario = SecurityContextHolder.getContext().getAuthentication()
		    .getName();
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    auditoriaDTO.setFechaModificacion(new Date());
	    if (parametro != null) {
		this.ruta = parametro.getValor();
	    } else {
		JsfUtil.msgError("Ruta de archivos no encontrada");
		return;
	    }
	    cargaAnios();
	    faseIesDTO = controller.getFaseIesDTO();
	    tiposArchivo = catalogoServicio.obtenerTiposCargaMasiva(faseIesDTO
		    .getProcesoDTO().getId());
	    // Este metodo se utiliza para eliminar del select la opción Libro
	    // eliminarLibro();
	    // ---------------------------
	    email = cargaServicio.obtenerEmailUsuario(usuario);

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error en start.");
	}
    }

    public void eliminarArchivo() {
	try {
	    if (archivoSeleccionado != null
		    && archivoSeleccionado.getId() != null) {
		if (archivoSeleccionado.getEstado().equals(
		        EstadoCargaMasivaEnum.ERROR)) {
		    cargaMasivaServicio.eliminarArchivo(
			    archivoSeleccionado.getId(), usuario,
			    faseIesDTO.getId());
		    obtenerArchivosCarga();
		    JsfUtil.msgError("El archivo fue eliminado correctamente.");
		} else {
		    JsfUtil.msgError("Solamente se puede eliminar archivos en estado de ERROR.");
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error al eliminar el archivo seleccionado. Comuníquese con el administrador");
	}
    }

    public void cargaAnios() {
	Calendar fechaActual = Calendar.getInstance();
	int z = fechaActual.get(Calendar.YEAR) + 3;
	for (int i = z; i >= fechaActual.get(Calendar.YEAR) - 9; i--) {
	    SelectItem anio = new SelectItem(i, String.valueOf(i));
	    ANIOS.add(anio);

	}
    }

    private void eliminarLibro() {
	for (int i = 0; i < tiposArchivo.size(); i++) {
	    if (tiposArchivo.get(i).getEtiqueta().contains("Libros")) {
		tiposArchivo.remove(i);
		break;
	    }
	}
    }

    private Calendar asignarHoraCero(Calendar fecha) {
	fecha.set(Calendar.HOUR_OF_DAY, 0);
	fecha.set(Calendar.MINUTE, 0);
	fecha.set(Calendar.SECOND, 0);
	fecha.set(Calendar.MILLISECOND, 0);
	return fecha;
    }

    public int obtenerUltimoDiaMes(int anio, int mes) {

	Calendar calendario = Calendar.getInstance();
	calendario.set(anio, mes, 1);
	int diaUltimo = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
	LOG.info("Dia ultimo del mes: " + mes + " es: " + diaUltimo);
	return diaUltimo;
    }

    public void eliminarPeriodo() {

	// periodoAcademicoEliminar.setActivo(false);
	// periodosAcademicos.add(periodoAcademicoEliminar);
	// registroServicio.registrarPeriodosAcademicos(periodosAcademicos);
	// cargarPeriodosAcademicos();
	// JsfUtil.msgInfo("Periodo eliminado correctamente");

	for (int i = 0; i < periodosAcademicos.size(); i++) {
	    if (periodoAcademicoEliminar.getFechaInicioPeriodo().equals(
		    periodosAcademicos.get(i).getFechaInicioPeriodo())
		    && periodoAcademicoEliminar.getFechaFinPeriodo().equals(
		            periodosAcademicos.get(i).getFechaFinPeriodo())) {
		periodosAcademicos.get(i).setActivo(false);
		registroServicio
		        .registrarPeriodosAcademicos(periodosAcademicos);
		cargarPeriodosAcademicos();
		JsfUtil.msgInfo("Periodo eliminado correctamente");

	    }
	}
    }

    public void agregarPeriodoAcademico() {

	Calendar fechaInicio = Calendar.getInstance();
	fechaInicio.set(anioInicioPeriodoAcademico, mesInicioPeriodoAcademico,
	        1);
	fechaInicio = asignarHoraCero(fechaInicio);

	Calendar fechaFin = Calendar.getInstance();

	fechaFin.set(
	        anioFinPeriodoAcademico,
	        mesFinPeriodoAcademico,
	        obtenerUltimoDiaMes(anioFinPeriodoAcademico,
	                mesFinPeriodoAcademico));

	fechaFin = asignarHoraCero(fechaFin);

	if (fechaInicio.compareTo(fechaFin) >= 0) {
	    JsfUtil.msgError("El periodo de inicio es mayor o igual al periodo final");
	    return;
	}

	periodoAcademicoAgregar.setFechaInicioPeriodo(fechaInicio.getTime());
	periodoAcademicoAgregar.setFechaFinPeriodo(fechaFin.getTime());

	for (PeriodoAcademicoDTO periodo : periodosAcademicos) {
	    if (periodo.getFechaInicioPeriodo().equals(
		    periodoAcademicoAgregar.getFechaInicioPeriodo())
		    && periodo.getFechaFinPeriodo().equals(
		            periodoAcademicoAgregar.getFechaFinPeriodo())) {
		JsfUtil.msgError("El periodo ya existe");
		return;

	    }
	}

	periodoAcademicoAgregar.setFaseIesDTO(faseIesDTO);
	periodoAcademicoAgregar.setActivo(true);
	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	periodoAcademicoAgregar.setAuditoriaDTO(auditoria);
	periodoAcademicoAgregar.setInformacionIes(informacionIesDTO);
	periodosAcademicos.add(periodoAcademicoAgregar);
	periodoAcademicoAgregar = new PeriodoAcademicoDTO();
	anioInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	anioFinPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesFinPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);

	JsfUtil.msgInfo("Periodo agregado satifactoriamente");
	JsfUtil.msgAdvert("Para que se haga permanente el cambio debe oprimir el boton guardar");

    }

    public void guardarPeriodoAcademico() {
	registroServicio.registrarPeriodosAcademicos(periodosAcademicos);
	JsfUtil.msgInfo("Los periodos se guardaron correctamente");
	cargarPeriodosAcademicos();
    }

    public void obtenerArchivosCarga() {
	listaArchivosCarga.clear();

	try {
	    if (tipoCarga != null) {
		mostrarPeriodosAcademicos = true;
		if (tipoCarga.getCodigo().startsWith(
		        Constantes.PREFIJO_MATRICULA)) {
		    mostrarPeriodosAcademicos = false;
		    cargarPeriodosAcademicos();
		} else {
		    periodosAcademicos.clear();
		    listaArchivosCarga = cargaMasivaServicio
			    .obtenerArchivosCargaPorIesYTipo(ies.getId(),
			            tipoCarga.getId());

		}
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al obtener archivos carga masiva ");
	    return;
	}
    }

    public void nuevoPeriodoAcademico() {
	periodoAcademicoAgregar = new PeriodoAcademicoDTO();
	anioInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	anioFinPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesFinPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	cargarPeriodosAcademicos();
    }

    public void obtenerArchivosCargaPorPeriodo() {
	listaArchivosCarga.clear();
	try {
	    if (tipoCarga != null && idPeriodoAcademico != -1) {

		List<ArchivoCargaMasivaDTO> listaArchivosCargaAux = cargaMasivaServicio
		        .obtenerArchivosCargaPorIesYTipo(ies.getId(),
		                tipoCarga.getId());

		if (listaArchivosCarga == null) {
		    listaArchivosCarga = new ArrayList<>();
		}
		for (ArchivoCargaMasivaDTO acmDTO : listaArchivosCargaAux) {
		    if (acmDTO.getFrecuencia().equals(
			    idPeriodoAcademico.toString())) {
			listaArchivosCarga.add(acmDTO);
			break;
		    }
		}
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al obtener archivos carga masiva por periodo ");
	    return;
	}
    }

    private boolean existeArchivoParaProcesar() {

	boolean esOK = true;

	if (uploadedFile == null) {
	    esOK = false;
	    JsfUtil.msgError("Ingrese el archivo a procesar por favor");
	}

	return esOK;

    }

    private boolean validarCodificacionArchivo() {

	boolean codificacion = true;

	try {
	    Charset.availableCharsets().get("UTF-8").newDecoder()
		    .decode(ByteBuffer.wrap(uploadedFile.getContents()));
	    ENCODING = "UTF-8";
	    LOG.info("SI es UTF");

	} catch (CharacterCodingException e1) {

	    try {
		Charset.availableCharsets().get("ISO-8859-1").newDecoder()
		        .decode(ByteBuffer.wrap(uploadedFile.getContents()));
		ENCODING = "ISO-8859-1";
		LOG.info("SI es ISO-8859-1");
		LOG.info("NO es UTF");

	    } catch (CharacterCodingException e2) {
		try {
		    Charset.availableCharsets()
			    .get("ISO-8859-2")
			    .newDecoder()
			    .decode(ByteBuffer.wrap(uploadedFile.getContents()));
		    ENCODING = "ISO-8859-2";
		    LOG.info("SI es ISO-8859-2");
		    LOG.info("NO es UTF");

		} catch (CharacterCodingException e3) {
		    try {
			Charset.availableCharsets()
			        .get("ISO-8859-3")
			        .newDecoder()
			        .decode(ByteBuffer.wrap(uploadedFile
			                .getContents()));
			ENCODING = "ISO-8859-3";
			LOG.info("SI es ISO-8859-3");
			LOG.info("NO es UTF");
		    } catch (CharacterCodingException e) {
			codificacion = false;

		    }

		}

	    }
	}
	return codificacion;
    }

    private boolean validarExtensionArchivo() {

	String extension = FilenameUtils.getExtension(this.uploadedFile
	        .getFileName());

	if (extension.equalsIgnoreCase(Constantes.EXTENSION_ARCHIVO_CSV)) {
	    return true;
	} else {
	    JsfUtil.msgError("Se esperaba un archivo tipo csv o CSV.");
	    return false;
	}

    }

    private String obtenerPrefijoArchivo() {

	if (informacionIesDTO.getIes().getSiglas() != null
	        && !informacionIesDTO.getIes().getSiglas().isEmpty()) {

	    return informacionIesDTO.getIes().getSiglas() + "_"
		    + tipoCarga.getCodigo();

	} else {

	    return informacionIesDTO.getIes().getCodigo() + "_"
		    + tipoCarga.getCodigo();
	}

    }

    /**
     * 
     * Procesa carga masiva de archivos desde pantalla
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:58:05
     */
    public void procesar() {
	mostrarMensajeError = false;

	// Verifica datos de entrada
	try {
	    if (tipoCarga == null) {
		JsfUtil.msgError("Debe seleccionar un tipo de archivo");
		return;
	    }
	    if (!existeArchivoParaProcesar()) {
		return;
	    }
	    if (!validarExtensionArchivo()) {
		return;
	    }
	    if (!validarCodificacionArchivo()) {
		JsfUtil.msgError("La codificación del archivo no es la correcta");
		return;
	    }
	    if (tipoCarga.getCodigo().startsWith(Constantes.PREFIJO_MATRICULA)) {
		if (idPeriodoAcademico == -1) {
		    JsfUtil.msgError("Debe seleccionar un periodo académico.");
		    return;
		}
	    }
	    // if (tipoCarga.getDependencias() != null) {
	    // String[] dependencias = tipoCarga.getDependencias().split(
	    // Constantes.SEPARADOR_COMA);
	    // if (dependencias.length != 0) {
	    // TipoCargaMasivaDTO tipoCargaDependenciaDTO = new
	    // TipoCargaMasivaDTO();
	    // Long idDependencia = 0L;
	    // ArchivoCargaMasivaDTO acmDepDTO = new ArchivoCargaMasivaDTO();
	    // StringBuffer dependenciasFaltantes = new StringBuffer();
	    //
	    // for (int i = 0; i < dependencias.length; i++) {
	    // idDependencia = Long.parseLong(dependencias[i]);
	    // tipoCargaDependenciaDTO = catalogoServicio
	    // .obtenerTipoCargaMasivaPorId(idDependencia);
	    // acmDepDTO = cargaMasivaServicio
	    // .obtenerArchivoCargaPorIesYIdTipoCargaMasiva(
	    // ies.getId(),
	    // tipoCargaDependenciaDTO.getId());
	    //
	    // if (acmDepDTO == null) {
	    // if (!dependenciasFaltantes.toString().isEmpty()) {
	    // dependenciasFaltantes
	    // .append(Constantes.SEPARADOR_COMA + " ");
	    // }
	    // dependenciasFaltantes
	    // .append(tipoCargaDependenciaDTO
	    // .getEtiqueta());
	    // }
	    //
	    // }
	    // if (!dependenciasFaltantes.toString().isEmpty()) {
	    // JsfUtil.msgError("Previo a ingresar "
	    // + tipoCarga.getEtiqueta()
	    // + " debe cargar el(los) archivo(s) de:  "
	    // + dependenciasFaltantes);
	    // return;
	    // }
	    // }
	    // }
	    // Inicia proceso de archivos según prefijo
	    if (tipoCarga.getCodigo().startsWith(Constantes.PREFIJO_ESTUDIANTE)) {
		System.out
		        .println("========= INGRESA A PROCESAR ESTUDIANTE ========= ");
		List<ArchivoCargaMasivaDTO> listaArchivosEnProceso = new ArrayList<>();
		listaArchivosEnProceso = obtenerArchivosEnProceso();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}

		if (listaArchivosEnProceso != null
		        && !listaArchivosEnProceso.isEmpty()) {
		    JsfUtil.msgAdvert("Actualmente existe un archivo en proceso.");
		    return;
		} else {
		    procesarArchivoEstudiantes();
		    if (FacesContext.getCurrentInstance().getMessages()
			    .hasNext()) {
			return;
		    }
		}

	    } else if (tipoCarga.getCodigo().startsWith(
		    Constantes.PREFIJO_CARRERA_POR_ESTUDIANTE)) {
		List<ArchivoCargaMasivaDTO> listaArchivosEnProceso = new ArrayList<>();
		listaArchivosEnProceso = obtenerArchivosEnProceso();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		if (listaArchivosEnProceso != null
		        && !listaArchivosEnProceso.isEmpty()) {
		    JsfUtil.msgAdvert("Actualmente existe un archivo en proceso.");
		    return;
		} else {
		    procesarArchivoCarreraPorEstudiante();
		    if (FacesContext.getCurrentInstance().getMessages()
			    .hasNext()) {
			return;
		    }
		}

	    } else if (tipoCarga.getCodigo().startsWith(
		    Constantes.PREFIJO_MATRICULA)) {
		System.out
		        .println("========= INGRESA A PROCESAR MATRICULAS ========= ");
		List<ArchivoCargaMasivaDTO> listaArchivosEnProceso = new ArrayList<>();
		listaArchivosEnProceso = obtenerArchivosEnProceso();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		if (listaArchivosEnProceso != null
		        && !listaArchivosEnProceso.isEmpty()) {
		    JsfUtil.msgAdvert("Actualmente existe un archivo en proceso.");
		    return;
		} else {
		    procesarArchivoCarreraEstudiantePorPeriodo();
		    if (FacesContext.getCurrentInstance().getMessages()
			    .hasNext()) {
			return;
		    }
		}

	    } else if (tipoCarga.getCodigo().startsWith(
		    Constantes.PREFIJO_DOCENTE)) {
		System.out
		        .println("========= INGRESA A PROCESAR DOCENTE ========= ");
		List<ArchivoCargaMasivaDTO> listaArchivosEnProceso = new ArrayList<>();
		listaArchivosEnProceso = obtenerArchivosEnProceso();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		if (listaArchivosEnProceso != null
		        && !listaArchivosEnProceso.isEmpty()) {
		    JsfUtil.msgAdvert("Actualmente existe un archivo en proceso.");
		    return;
		} else {
		    procesarArchivoDocentes();
		    if (FacesContext.getCurrentInstance().getMessages()
			    .hasNext()) {
			return;
		    }
		}

	    } else if (tipoCarga.getCodigo().startsWith(
		    Constantes.PREFIJO_LIBROS)) {
		System.out
		        .println("========= INGRESA A PROCESAR LIBROS ========= ");
		List<ArchivoCargaMasivaDTO> listaArchivosEnProceso = new ArrayList<>();
		listaArchivosEnProceso = obtenerArchivosEnProceso();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		if (listaArchivosEnProceso != null
		        && !listaArchivosEnProceso.isEmpty()) {
		    JsfUtil.msgAdvert("Actualmente existe un archivo en proceso.");
		    return;
		} else {
		    procesarArchivoLibros();
		    if (FacesContext.getCurrentInstance().getMessages()
			    .hasNext()) {
			return;
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema.");
	    return;
	}

	// tipoCarga = null;
	// listaArchivosCarga = null;
	// periodosAcademicos = null;
    }

    /**
     * 
     * Obtiene archivos de carga masiva que se encuentran en proceso
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:19:35
     * @return
     */
    private List<ArchivoCargaMasivaDTO> obtenerArchivosEnProceso() {
	// Sincroniza lista de archivos en proceso con lo que se encuentra en
	// base
	List<ArchivoCargaMasivaDTO> listaArchivosCargaAux = new ArrayList<>();
	try {
	    if (tipoCarga.getCodigo().startsWith(Constantes.PREFIJO_MATRICULA)) {
		listaArchivosCargaAux = cargaMasivaServicio
		        .obtenerArchivosCargaPorIesYTipo(ies.getId(),
		                tipoCarga.getId());

		for (ArchivoCargaMasivaDTO acmDTO : listaArchivosCargaAux) {
		    if (acmDTO.getFrecuencia().equals(
			    idPeriodoAcademico.toString())) {
			listaArchivosCarga.add(acmDTO);
			break;
		    }
		}
	    } else {
		listaArchivosCarga = cargaMasivaServicio
		        .obtenerArchivosCargaPorIesYTipo(ies.getId(),
		                tipoCarga.getId());
	    }
	} catch (ServicioException se) {
	    JsfUtil.msgError("Consulte con el administrador Error al obtener Archivos Carga.");
	}

	// Verifica archivos en proceso
	// if (listaArchivosCarga != null && !listaArchivosCarga.isEmpty()) {
	// ArchivoCargaMasivaDTO archivoCarga = listaArchivosCarga.get(0);
	// if (tipoCarga.getCodigo().startsWith(Constantes.PREFIJO_MATRICULA)) {
	// if (archivoCarga.getEstado().equals(
	// EstadoCargaMasivaEnum.PROCESADO)
	// && archivoCarga.getFrecuencia().equals(
	// idPeriodoAcademico.toString())) {
	// JsfUtil.msgError("Sólo se permite una carga masiva del archivo por periodo académico.");
	// return null;
	// }
	// } else {
	// if (archivoCarga.getEstado().equals(
	// EstadoCargaMasivaEnum.PROCESADO)) {
	// mostrarPeriodosAcademicos = true;
	// JsfUtil.msgError("Sólo se permite una carga masiva del archivo.");
	// return null;
	// }
	// }
	// }

	List<String> estados = new ArrayList<>();
	estados.add(EstadoCargaMasivaEnum.PREVALIDADO.toString());
	estados.add(EstadoCargaMasivaEnum.VALIDADO.toString());

	List<ArchivoCargaMasivaDTO> listaArchivosEnProcesoDTO = null;
	try {
	    listaArchivosEnProcesoDTO = cargaMasivaServicio
		    .obtenerArchivosCargaPorIesTipoYEstado(ies.getId(),
		            tipoCarga.getId(), estados);
	} catch (ServicioException e) {
	    JsfUtil.msgError("Consulte con el administrador Error al recuperar archivo en proceso.");
	}
	return listaArchivosEnProcesoDTO;
    }

    /**
     * PROCESAMIENTO DE CARGAS MASIVA
     */

    /**
     * 
     * Procesa carga masiva de archivo de estudiantes
     * 
     * @author tfreire
     * @version 01/08/2014 - 11:11:38
     * @throws IOException
     */
    private void procesarArchivoEstudiantes() throws IOException {
	mostrarPeriodosAcademicos = true;
	try {
	    String mensajesError = validarArchivoEstudiantes();
	    if (mensajesError != null) {
		this.mensajesError = mensajesError;
		mostrarMensajeError = true;
		return;
	    }
	    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		return;
	    }

	    String urlPorIes = ruta + ies.getCodigo() + "/";
	    boolean creoDirectorio = ArchivoUtil.crearDirectorio(urlPorIes);
	    String nombreArchivo = "";
	    if (creoDirectorio) {
		nombreArchivo = ArchivoUtil.guardarArchivoEnDisco(urlPorIes,
		        obtenerPrefijoArchivo(),
		        this.uploadedFile.getInputstream(),
		        Constantes.EXTENSION_ARCHIVO_CSV);
	    }
	    if (nombreArchivo != null) {
		// Se asignan los valores de la carga
		ArchivoCargaMasivaDTO archivoCargaDTO = null;
		archivoCargaDTO = registrarEstadoCarga(nombreArchivo, urlPorIes);
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		// Determina como se procesará la informacion: sin o con hilo
		if (!estudiantesDTO.isEmpty()) {
		    if (estudiantesDTO.size() > Constantes.NUM_REGISTROS_SIN_HILO) {

			cargaMasivaServicio.procesarEstudiantesCargaHilo(
			        estudiantesDTO, archivoCargaDTO, ies);

		    } else {
			cargaMasivaServicio.procesarEstudiantes(estudiantesDTO,
			        archivoCargaDTO, ies);

		    }
		    JsfUtil.msgInfo("Estamos procesando sus datos, por favor revise en su email: "
			    + email
			    + " o vuelva a ingresar al sistema después de unos minutos.");
		    return;
		} else {
		    JsfUtil.msgError("No existe datos para procesar, comuníquese con el administrador del sistema");
		    return;
		}
	    } else {
		JsfUtil.msgError("No se pudo registrar el archivo, comuníquese con el administrador del sistema");
		return;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema");
	    return;
	}

    }

    /**
     * 
     * Procesa carga masiva de archivo de carrera por estudiante
     * 
     * @author tfreire
     * @version 01/08/2014 - 11:11:58
     * @throws IOException
     */
    private void procesarArchivoCarreraPorEstudiante() throws IOException {
	mostrarPeriodosAcademicos = true;
	try {
	    String mensajesError = validarArchivoCarreraPorEstudiante();
	    if (mensajesError != null) {
		this.mensajesError = mensajesError;
		mostrarMensajeError = true;
		return;
	    }
	    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		return;
	    }

	    String urlPorIes = ruta + ies.getCodigo() + "/";
	    boolean creoDirectorio = ArchivoUtil.crearDirectorio(urlPorIes);
	    String nombreArchivo = "";
	    if (creoDirectorio) {
		nombreArchivo = ArchivoUtil.guardarArchivoEnDisco(urlPorIes,
		        obtenerPrefijoArchivo(),
		        this.uploadedFile.getInputstream(),
		        Constantes.EXTENSION_ARCHIVO_CSV);
	    }
	    if (nombreArchivo != null) {
		// Se asignan los valores de la carga
		ArchivoCargaMasivaDTO archivoCargaDTO = null;
		archivoCargaDTO = registrarEstadoCarga(nombreArchivo, urlPorIes);
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		// Determina como se procesará la informacion: sin o con hilo

		if (carrerasEstudiantesDTO.size() > Constantes.NUM_REGISTROS_SIN_HILO) {
		    cargaMasivaServicio.procesarCarreraPorEstudianteCargaHilo(
			    carrerasEstudiantesDTO, archivoCargaDTO, ies);
		} else {
		    cargaMasivaServicio.procesarCarreraPorEstudiante(
			    carrerasEstudiantesDTO, archivoCargaDTO, ies);

		}
		JsfUtil.msgInfo("Estamos procesando sus datos, por favor revise en su email: "
		        + email
		        + " o vuelva a ingresar al sistema después de unos minutos.");
		return;
	    } else {
		JsfUtil.msgError("No se pudo registrar el archivo, comuníquese con el administrador del sistema");
		return;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema");
	    return;
	}

    }

    /**
     * 
     * Procesa carga masiva de archivo de carrera estudiantes por periodo
     * 
     * @author tfreire
     * @version 01/08/2014 - 11:12:17
     * @throws IOException
     */
    private void procesarArchivoCarreraEstudiantePorPeriodo()
	    throws IOException {
	try {
	    String mensajesError = validarArchivoCarreraEstudiantePorPeriodo();
	    if (mensajesError != null) {
		this.mensajesError = mensajesError;
		mostrarMensajeError = true;
		return;
	    }
	    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		return;
	    }

	    String urlPorIes = ruta + ies.getCodigo() + "/";
	    boolean creoDirectorio = ArchivoUtil.crearDirectorio(urlPorIes);
	    String nombreArchivo = "";
	    if (creoDirectorio) {
		nombreArchivo = ArchivoUtil.guardarArchivoEnDisco(urlPorIes,
		        obtenerPrefijoArchivo(),
		        this.uploadedFile.getInputstream(),
		        Constantes.EXTENSION_ARCHIVO_CSV);
	    }
	    if (nombreArchivo != null) {
		// Se asignan los valores de la carga
		ArchivoCargaMasivaDTO archivoCargaDTO = null;
		archivoCargaDTO = registrarEstadoCarga(nombreArchivo, urlPorIes);
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		// Determina como se procesará la informacion: sin o con hilo

		if (carreraEstudiantePeriodosDTO.size() > Constantes.NUM_REGISTROS_SIN_HILO) {
		    cargaMasivaServicio
			    .procesarCarreraEstudiantePorPeriodoCargaHilo(
			            carreraEstudiantePeriodosDTO,
			            archivoCargaDTO, ies);
		} else {
		    cargaMasivaServicio.procesarCarreraEstudiantePorPeriodo(
			    carreraEstudiantePeriodosDTO, archivoCargaDTO, ies);

		}
		JsfUtil.msgInfo("Estamos procesando sus datos, por favor revise en su email: "
		        + email
		        + " o vuelva a ingresar al sistema después de unos minutos.");
		return;
	    } else {
		JsfUtil.msgError("No se pudo registrar el archivo, comuníquese con el administrador del sistema");
		return;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema");
	    return;
	}

    }

    /**
     * 
     * Procesa carga masiva de archivo de docentes
     * 
     * @author tfreire
     * @version 01/08/2014 - 11:12:45
     * @throws IOException
     */
    private void procesarArchivoDocentes() throws IOException {
	mostrarPeriodosAcademicos = true;
	try {
	    String mensajesError = validarArchivoDocentes();
	    if (mensajesError != null) {
		this.mensajesError = mensajesError;
		mostrarMensajeError = true;
		return;
	    }
	    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		return;
	    }

	    String urlPorIes = ruta + ies.getCodigo() + "/";
	    boolean creoDirectorio = ArchivoUtil.crearDirectorio(urlPorIes);
	    String nombreArchivo = "";
	    if (creoDirectorio) {
		nombreArchivo = ArchivoUtil.guardarArchivoEnDisco(urlPorIes,
		        obtenerPrefijoArchivo(),
		        this.uploadedFile.getInputstream(),
		        Constantes.EXTENSION_ARCHIVO_CSV);
	    }
	    if (nombreArchivo != null) {
		// Se asignan los valores de la carga
		ArchivoCargaMasivaDTO archivoCargaDTO = null;
		archivoCargaDTO = registrarEstadoCarga(nombreArchivo, urlPorIes);
		// Determina como se procesará la informacion: sin o con hilo
		if (personasDTO.size() > Constantes.NUM_REGISTROS_SIN_HILO) {
		    cargaMasivaServicio.procesarDocentesCargaHilo(personasDTO,
			    archivoCargaDTO, ies);

		} else {
		    cargaMasivaServicio.procesarDocentes(personasDTO,
			    archivoCargaDTO, ies);

		}
		JsfUtil.msgInfo("Estamos procesando sus datos, por favor revise en su email: "
		        + email
		        + " o vuelva a ingresar al sistema después de unos minutos.");
	    } else {
		JsfUtil.msgError("No se pudo registrar el archivo, comuníquese con el administrador del sistema");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema");
	}

    }

    /**
     * 
     * Procesa carga masiva de archivo de Libros
     * 
     * @author tfreire
     * @version 01/08/2014 - 11:02:10
     * @throws IOException
     */
    private void procesarArchivoLibros() throws IOException {
	mostrarPeriodosAcademicos = true;
	try {
	    String mensajesError = validarArchivoLibros();
	    if (mensajesError != null) {
		this.mensajesError = mensajesError;
		mostrarMensajeError = true;
		return;
	    }
	    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		return;
	    }

	    String urlPorIes = ruta + ies.getCodigo() + "/";
	    boolean creoDirectorio = ArchivoUtil.crearDirectorio(urlPorIes);
	    String nombreArchivo = "";
	    if (creoDirectorio) {
		nombreArchivo = ArchivoUtil.guardarArchivoEnDisco(urlPorIes,
		        obtenerPrefijoArchivo(),
		        this.uploadedFile.getInputstream(),
		        Constantes.EXTENSION_ARCHIVO_CSV);
	    }
	    if (nombreArchivo != null) {
		// Se asignan los valores de la carga
		ArchivoCargaMasivaDTO archivoCargaDTO = null;
		archivoCargaDTO = registrarEstadoCarga(nombreArchivo, urlPorIes);
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		// Determina como se procesará la informacion: sin o con hilo
		if (librosDTO.size() > Constantes.NUM_REGISTROS_SIN_HILO) {
		    cargaMasivaServicio.procesarLibrosCargaHilo(librosDTO,
			    archivoCargaDTO, ies);
		} else {
		    cargaMasivaServicio.procesarLibros(librosDTO,
			    archivoCargaDTO, ies);

		}
		JsfUtil.msgInfo("Estamos procesando sus datos, por favor revise en su email: "
		        + email
		        + " o vuelva a ingresar al sistema después de unos minutos.");
		return;
	    } else {
		JsfUtil.msgError("No se pudo registrar el archivo, comuníquese con el administrador del sistema");
		return;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al procesar el archivo, comuníquese con el administrador del sistema");
	    return;
	}

    }

    /**
     * VALIDACIONES DE CARGAS MASIVA
     */

    /**
     * 
     * Validaciones carga masiva Archivo Estudiantes
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:14:50
     * @return
     */
    private String validarArchivoEstudiantes() {
	try {
	    // InputStreamReader inputStreamReader = new InputStreamReader(
	    // uploadedFile.getInputstream());
	    //
	    // LOG.info("Encoding==:" +
	    // inputStreamReader.getEncoding());
	    // if (!ENCODING.equals(inputStreamReader.getEncoding())) {
	    // JsfUtil.msgError("El archivo no tiene la codificación: "
	    // + ENCODING);
	    // return null;
	    // }
	    // BufferedReader reader = new BufferedReader(inputStreamReader);

	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    uploadedFile.getInputstream(), ENCODING));

	    CsvConfiguration configuracion = new CsvConfiguration();
	    configuracion.setFieldDelimiter(Constantes.SEPARADOR_ARCHIVO_CSV);
	    CsvIOFactory csvFactory = CsvIOFactory.createFactory(configuracion,
		    CargaEstudianteData.class);

	    Deserializer deserializer = csvFactory.createDeserializer();
	    deserializer.open(reader, tipoCarga.getNumeroColumnas());
	    if (!deserializer.hasNext()) {
		JsfUtil.msgError("El archivo no tiene datos para procesar");
		return null;
	    }

	    // Valida Detalle del Archivo
	    StringBuilder erroresArchivo = new StringBuilder();
	    erroresArchivo = validarDetalleArchivoEstudiantes(deserializer);
	    // En caso de existir errores exporta archivo de errores
	    if (erroresArchivo.toString().isEmpty()) {
		return null;
	    } else {
		return erroresArchivo.toString();
	    }
	} catch (InitialConfigurationException e) {
	    e.printStackTrace();
	    JsfUtil.msgError(e.getMessage());
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar el archivo, comuníquese con el administrador del sistema.");
	    return null;
	}

    }

    private StringBuilder validarDetalleArchivoEstudiantes(
	    Deserializer deserializer) {
	StringBuilder erroresArchivo = new StringBuilder();
	estudiantesDTO = new ArrayList<>();
	LinkedHashMap<String, Integer> cedulas = new LinkedHashMap<>();
	while (deserializer.hasNext()) {

	    try {
		CargaEstudianteData cargaEstudianteData = deserializer.next();
		if (cargaEstudianteData == null) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    "Registro no contiene datos.\r\n"));
		    continue;
		}
		boolean validacion = true;
		StringBuffer erroresValidacion = new StringBuffer();
		if (ec.gob.ceaaces.catalogo.enumeraciones.TipoDocumentoEnum.CEDULA
		        .getValue().equals(
		                cargaEstudianteData.getTipoIdentificacion())) {

		    if (!Util.validarCedula(cargaEstudianteData
			    .getIdentificacion())) {
			// si existe errores en la validacion de la cedula
			// se agrega el mensaje de error
			erroresValidacion
			        .append("El estudiante con CEDULA '"
			                + cargaEstudianteData
			                        .getIdentificacion()
			                + "', no pasa la validación del dígito verificador.\r\n");

			validacion = false;

		    }
		}

		if (validacion
		        && cedulas.get(cargaEstudianteData.getIdentificacion()) != null) {
		    erroresValidacion.append("La identificación '"
			    + cargaEstudianteData.getIdentificacion()
			    + "', se encuentra duplicada con la de línea -> "
			    + cedulas.get(cargaEstudianteData
			            .getIdentificacion()) + ".\r\n");

		    validacion = false;
		}
		if (cargaEstudianteData.getFechaNacimiento() != null) {
		    Date fechaActual = new Date();

		    SimpleDateFormat formateador = new SimpleDateFormat(
			    "dd/MM/yyyy");
		    String fechaFormateada = formateador
			    .format(cargaEstudianteData.getFechaNacimiento());
		    if (cargaEstudianteData.getFechaNacimiento().compareTo(
			    fechaActual) >= 0) {
			erroresValidacion.append("Fecha de nacimiento '"
			        + fechaFormateada
			        + "', debe ser menor a la fecha actual.");
			validacion = false;
		    }

		}
		if (!erroresValidacion.toString().isEmpty()) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    erroresValidacion.toString()));
		}

		// Recupera y transforma registro de archivo CVS
		if (validacion) {
		    cedulas.put(cargaEstudianteData.getIdentificacion(),
			    new Integer(deserializer.getInputPosition()
			            .getLineNumber()));
		    // Si no existe errores en el registro se agrega a
		    // la lista
		    EstudianteDTO estudianteDTO = new EstudianteDTO();
		    // PersonaDTO personaDTO = new PersonaDTO();
		    estudianteDTO.setTipoIdentificacion(cargaEstudianteData
			    .getTipoIdentificacion());
		    estudianteDTO.setIdentificacion(cargaEstudianteData
			    .getIdentificacion());
		    estudianteDTO.setNombres(cargaEstudianteData.getNombres());
		    estudianteDTO.setApellidoMaterno(cargaEstudianteData
			    .getSegundoApellido());
		    estudianteDTO.setApellidoPaterno(cargaEstudianteData
			    .getPrimerApellido());
		    estudianteDTO.setIdentificacion(cargaEstudianteData
			    .getIdentificacion());
		    estudianteDTO.setFechaNacimiento(cargaEstudianteData
			    .getFechaNacimiento());
		    estudianteDTO.setSexo(GeneroEnum
			    .valueOf(cargaEstudianteData.getGenero()));
		    estudianteDTO.setEtnia(EtniaEnum
			    .valueOf(cargaEstudianteData.getEtnia()));
		    estudianteDTO.setTipoIdentificacion(cargaEstudianteData
			    .getTipoIdentificacion());
		    estudianteDTO.setDiscapacidad(DiscapacidadEnum
			    .valueOf(cargaEstudianteData.getDiscapacidad()));

		    estudianteDTO.setEmailPersonal(cargaEstudianteData
			    .getEmailPersonal());
		    estudianteDTO
			    .setIdInformacionIes(informacionIesDTO.getId());
		    estudianteDTO.setIesDTO(ies);
		    estudianteDTO.setNumeroConadis(cargaEstudianteData
			    .getNumeroConadis());
		    estudianteDTO.setActivo(true);
		    estudianteDTO.setAuditoria(auditoriaDTO);
		    estudianteDTO.setFaseIesDTO(faseIesDTO);
		    estudianteDTO.setOrigenCarga(Constantes.ORIGEN_CARGA);
		    if (cargaEstudianteData.getPaisOrigen() != null) {
			PaisDTO paisDTO = new PaisDTO();
			paisDTO.setNombre(cargaEstudianteData.getPaisOrigen()
			        .toUpperCase());
			estudianteDTO.setPaisOrigen(paisDTO);
		    }
		    // PuebloDTO puebloDTO = new PuebloDTO();
		    // puebloDTO.setNombre(cargaEstudianteData.getPueblo());
		    // ParroquiaIesDTO parroquiaDTO = new ParroquiaIesDTO();
		    // parroquiaDTO.setNombre(cargaEstudianteData.getParroquia());

		    // estudianteDTO.setPuebloDTO(puebloDTO);
		    // estudianteDTO.setParroquiaDTO(parroquiaDTO);

		    estudianteDTO.setEmailInstitucional(cargaEstudianteData
			    .getEmailInstitucional());
		    estudianteDTO.setActivo(true);
		    estudianteDTO.setAuditoria(auditoriaDTO);
		    estudianteDTO.setFaseIesDTO(faseIesDTO);

		    estudiantesDTO.add(estudianteDTO);
		}
	    } catch (DeserializationException e) {

		erroresArchivo.append(generarLineaError(e, null));
	    }
	}
	deserializer.close(true);
	return erroresArchivo;
    }

    /**
     * 
     * Validaciones carga masiva Archivo EstudiantePorCarrera
     * 
     * @author tfreire
     * @version 24/07/2014 - 15:44:04
     * @return
     */
    private String validarArchivoCarreraPorEstudiante() {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    uploadedFile.getInputstream(), ENCODING));

	    CsvConfiguration configuracion = new CsvConfiguration();
	    configuracion.setFieldDelimiter(Constantes.SEPARADOR_ARCHIVO_CSV);
	    CsvIOFactory csvFactory = CsvIOFactory.createFactory(configuracion,
		    CargaCarreraEstudianteData.class);

	    Deserializer deserializer = csvFactory.createDeserializer();
	    deserializer.open(reader, tipoCarga.getNumeroColumnas());
	    if (!deserializer.hasNext()) {
		JsfUtil.msgError("El archivo no tiene datos para procesar");
		return null;
	    }

	    // Valida Detalle del Archivo
	    StringBuilder erroresArchivo = new StringBuilder();
	    erroresArchivo = validarDetalleArchivoCarreraPorEstudiante(deserializer);
	    // En caso de existir errores exporta archivo de errores
	    if (erroresArchivo.toString().isEmpty()) {
		return null;
	    } else {
		return erroresArchivo.toString();
	    }
	} catch (InitialConfigurationException e) {
	    e.printStackTrace();
	    JsfUtil.msgError(e.getMessage());
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar el archivo, comuníquese con el administrador del sistema.");
	    return null;
	}

    }

    private StringBuilder validarDetalleArchivoCarreraPorEstudiante(
	    Deserializer deserializer) {
	System.out
	        .println("INGRESA A validarDetalleArchivoCarreraPorEstudiante");
	StringBuilder erroresArchivo = new StringBuilder();
	carrerasEstudiantesDTO = new ArrayList<>();
	LinkedHashMap<String, Integer> carrerasEstudiantes = new LinkedHashMap<>();
	while (deserializer.hasNext()) {

	    try {
		CargaCarreraEstudianteData cargaCarreraEstudianteData = deserializer
		        .next();
		if (cargaCarreraEstudianteData == null) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    "Registro no contiene datos.\r\n"));
		    continue;
		}
		boolean validacion = true;
		StringBuffer erroresValidacion = new StringBuffer();
		if (carrerasEstudiantes.get(cargaCarreraEstudianteData
		        .getIdentificacion()
		        + "/"
		        + cargaCarreraEstudianteData.getCodigoCarrera()) != null) {
		    erroresValidacion
			    .append("La asignación (IDENTIFICACION/CODIGO CARRERA) '"
			            + cargaCarreraEstudianteData
			                    .getIdentificacion()
			            + "/"
			            + cargaCarreraEstudianteData
			                    .getCodigoCarrera()
			            + "', se encuentra duplicado con el registro: "
			            + carrerasEstudiantes.get(cargaCarreraEstudianteData
			                    .getIdentificacion()
			                    + "/"
			                    + cargaCarreraEstudianteData
			                            .getCodigoCarrera())
			            + ". \r\n");
		    validacion = false;
		}
		carrerasEstudiantes
		        .put(cargaCarreraEstudianteData.getIdentificacion()
		                + "/"
		                + cargaCarreraEstudianteData.getCodigoCarrera(),
		                new Integer(deserializer.getInputPosition()
		                        .getLineNumber()));

		if ((cargaCarreraEstudianteData.getFechaInicioPrimerNivel() == null && cargaCarreraEstudianteData
		        .getFechaConvalidacion() == null)
		        || (cargaCarreraEstudianteData
		                .getFechaInicioPrimerNivel() != null && cargaCarreraEstudianteData
		                .getFechaConvalidacion() != null)) {
		    erroresValidacion
			    .append("Debe registrar una fecha sea: Fecha Inicio Primer Nivel ó Fecha de Convalidación.\r\n");
		    validacion = false;
		}
		if (cargaCarreraEstudianteData.getFechaInicioPrimerNivel() != null) {
		    Date fechaActual = new Date();

		    SimpleDateFormat formateador = new SimpleDateFormat(
			    "dd/MM/yyyy");
		    String fechaFormateada = formateador
			    .format(cargaCarreraEstudianteData
			            .getFechaInicioPrimerNivel());
		    if (cargaCarreraEstudianteData.getFechaInicioPrimerNivel()
			    .compareTo(fechaActual) >= 0) {
			erroresValidacion.append("Fecha Inicio Primer Nivel '"
			        + fechaFormateada
			        + "', debe ser menor a la fecha actual.\r\n");
			validacion = false;
		    }

		}
		if (cargaCarreraEstudianteData.getFechaConvalidacion() != null) {
		    Date fechaActual = new Date();

		    SimpleDateFormat formateador = new SimpleDateFormat(
			    "dd/MM/yyyy");
		    String fechaFormateada = formateador
			    .format(cargaCarreraEstudianteData
			            .getFechaConvalidacion());
		    if (cargaCarreraEstudianteData.getFechaConvalidacion()
			    .compareTo(fechaActual) >= 0) {
			erroresValidacion.append("Fecha de Convalidación '"
			        + fechaFormateada
			        + "', debe ser menor a la fecha actual.\r\n");
			validacion = false;
		    }
		}
		if (cargaCarreraEstudianteData.getFechaGraduacion() != null) {
		    Date fechaActual = new Date();

		    SimpleDateFormat formateador = new SimpleDateFormat(
			    "dd/MM/yyyy");
		    String fechaFormateada = formateador
			    .format(cargaCarreraEstudianteData
			            .getFechaGraduacion());
		    if (cargaCarreraEstudianteData.getFechaGraduacion()
			    .compareTo(fechaActual) >= 0) {
			erroresValidacion.append("Fecha de Graduación '"
			        + fechaFormateada
			        + "', debe ser menor a la fecha actual.");
			validacion = false;
		    }

		}
		if (!erroresValidacion.toString().isEmpty()) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    erroresValidacion.toString()));
		}
		// Recupera y transforma registro de archivo CVS
		if (validacion) {
		    // Si no existe errores en el registro se agrega a
		    // la lista

		    CarreraEstudianteDTO carreraEstudianteDTO = new CarreraEstudianteDTO();
		    carreraEstudianteDTO
			    .setFechaInicioPrimerNivel(cargaCarreraEstudianteData
			            .getFechaInicioPrimerNivel());
		    carreraEstudianteDTO
			    .setFechaConvalidacion(cargaCarreraEstudianteData
			            .getFechaConvalidacion());

		    carreraEstudianteDTO
			    .setFechaGraduacion(cargaCarreraEstudianteData
			            .getFechaGraduacion());
		    EstudianteDTO estudianteDTO = new EstudianteDTO();
		    estudianteDTO.setIdentificacion(cargaCarreraEstudianteData
			    .getIdentificacion());
		    estudianteDTO
			    .setIdInformacionIes(informacionIesDTO.getId());
		    CarreraIesDTO carreraDTO = new CarreraIesDTO();
		    carreraDTO.setCodigo(cargaCarreraEstudianteData
			    .getCodigoCarrera());
		    InformacionCarreraDTO informacionCarreraDTO = new InformacionCarreraDTO();
		    informacionCarreraDTO.setCarreraIesDTO(carreraDTO);
		    carreraEstudianteDTO.setEstudianteDTO(estudianteDTO);
		    carreraEstudianteDTO
			    .setInformacionCarreraDTO(informacionCarreraDTO);
		    carreraEstudianteDTO.setEstudianteDTO(estudianteDTO);
		    carreraEstudianteDTO.setFaseIesDTO(faseIesDTO);
		    carreraEstudianteDTO.setActivo(true);
		    carreraEstudianteDTO.setAuditoriaDTO(auditoriaDTO);
		    carreraEstudianteDTO
			    .setOrigenCarga(Constantes.ORIGEN_CARGA);
		    carrerasEstudiantesDTO.add(carreraEstudianteDTO);
		}
	    } catch (DeserializationException e) {

		erroresArchivo.append(generarLineaError(e, null));
	    }
	}
	deserializer.close(true);
	return erroresArchivo;
    }

    /**
     * 
     * Validaciones carga masiva Archivo Estudiantes por Periodo
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:14:09
     * @return
     */
    private String validarArchivoCarreraEstudiantePorPeriodo() {
	System.out
	        .println("VALIDACION DE ARCHIVO - CABECERA - ESTUDIANTES POR PERIODO");
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    uploadedFile.getInputstream(), ENCODING));

	    CsvConfiguration configuracion = new CsvConfiguration();
	    configuracion.setFieldDelimiter(Constantes.SEPARADOR_ARCHIVO_CSV);
	    CsvIOFactory csvFactory = CsvIOFactory.createFactory(configuracion,
		    CargaCarreraEstudiantePeriodoData.class);

	    Deserializer deserializer = csvFactory.createDeserializer();
	    deserializer.open(reader, tipoCarga.getNumeroColumnas());
	    if (!deserializer.hasNext()) {
		JsfUtil.msgError("El archivo no tiene datos para procesar");
		return null;
	    }

	    // Valida Detalle del Archivo
	    StringBuilder erroresArchivo = new StringBuilder();
	    erroresArchivo = validarDetalleArchivoCarreraEstudiantesPorPeriodo(deserializer);
	    // En caso de existir errores exporta archivo de errores
	    if (erroresArchivo.toString().isEmpty()) {
		return null;
	    } else {
		return erroresArchivo.toString();
	    }
	} catch (InitialConfigurationException e) {
	    e.printStackTrace();
	    JsfUtil.msgError(e.getMessage());
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar el archivo, comuníquese con el administrador del sistema.");
	    return null;
	}

    }

    private StringBuilder validarDetalleArchivoCarreraEstudiantesPorPeriodo(
	    Deserializer deserializer) {
	StringBuilder erroresArchivo = new StringBuilder();
	carreraEstudiantePeriodosDTO = new ArrayList<>();
	LinkedHashMap<String, Integer> carreraEstudiantePorPeriodos = new LinkedHashMap<>();
	while (deserializer.hasNext()) {

	    try {
		CargaCarreraEstudiantePeriodoData cargaEstudiantePorPeriodoData = deserializer
		        .next();
		if (cargaEstudiantePorPeriodoData == null) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    "Registro no contiene datos.\r\n"));
		    continue;
		}
		boolean validacion = true;
		if (carreraEstudiantePorPeriodos
		        .get(cargaEstudiantePorPeriodoData.getCodigoCarrera()
		                + "/"
		                + cargaEstudiantePorPeriodoData
		                        .getIdentificacion()) != null) {
		    erroresArchivo
			    .append(generarLineaError(
			            deserializer,
			            "En el periodo académico , el registro '"
			                    + cargaEstudiantePorPeriodoData
			                            .getCodigoCarrera()
			                    + "/"
			                    + cargaEstudiantePorPeriodoData
			                            .getIdentificacion()
			                    + "', se encuentra duplicado con el registro: "
			                    + carreraEstudiantePorPeriodos.get(cargaEstudiantePorPeriodoData
			                            .getCodigoCarrera()
			                            + "/"
			                            + cargaEstudiantePorPeriodoData
			                                    .getIdentificacion())));
		    validacion = false;
		}
		// Recupera y transforma registro de archivo CVS
		if (validacion) {
		    carreraEstudiantePorPeriodos.put(
			    cargaEstudiantePorPeriodoData.getCodigoCarrera()
			            + "/"
			            + cargaEstudiantePorPeriodoData
			                    .getIdentificacion(), new Integer(
			            deserializer.getInputPosition()
			                    .getLineNumber()));
		    // si no existe errores en el registro se agrega a
		    // la lista

		    CarreraEstudiantePeriodoDTO carreraEstudiantePeriodoDTO = new CarreraEstudiantePeriodoDTO();
		    // Setea estudiante
		    EstudianteDTO estudianteDTO = new EstudianteDTO();
		    estudianteDTO
			    .setIdentificacion(cargaEstudiantePorPeriodoData
			            .getIdentificacion());
		    estudianteDTO
			    .setIdInformacionIes(informacionIesDTO.getId());
		    // Setea carrera
		    CarreraIesDTO carreraDTO = new CarreraIesDTO();
		    carreraDTO.setCodigo(cargaEstudiantePorPeriodoData
			    .getCodigoCarrera());
		    InformacionCarreraDTO informacionCarreraDTO = new InformacionCarreraDTO();
		    informacionCarreraDTO.setCarreraIesDTO(carreraDTO);
		    CarreraEstudianteDTO carreraEstudianteDTO = new CarreraEstudianteDTO();
		    carreraEstudianteDTO.setEstudianteDTO(estudianteDTO);
		    carreraEstudianteDTO
			    .setInformacionCarreraDTO(informacionCarreraDTO);
		    carreraEstudiantePeriodoDTO
			    .setCarreraEstudianteDTO(carreraEstudianteDTO);
		    carreraEstudiantePeriodoDTO
			    .setNumeroCreditosAprobados(Double
			            .parseDouble(cargaEstudiantePorPeriodoData
			                    .getNumeroCreditosAprobados()));
		    carreraEstudiantePeriodoDTO
			    .setSeccion(TipoSeccionEnum
			            .valueOf(cargaEstudiantePorPeriodoData
			                    .getSeccion()));
		    carreraEstudiantePeriodoDTO
			    .setCodigoMatricula(cargaEstudiantePorPeriodoData
			            .getCodigoMatricula());
		    // Setea periodo academico
		    PeriodoAcademicoDTO periodoAcademicoDTO = new PeriodoAcademicoDTO();
		    periodoAcademicoDTO.setId(idPeriodoAcademico);
		    carreraEstudiantePeriodoDTO
			    .setPeriodoAcademicoDTO(periodoAcademicoDTO);
		    carreraEstudiantePeriodoDTO.setFaseIesDTO(faseIesDTO);
		    carreraEstudiantePeriodoDTO.setActivo(true);
		    carreraEstudiantePeriodoDTO.setAuditoriaDTO(auditoriaDTO);
		    carreraEstudiantePeriodoDTO
			    .setOrigenCarga(Constantes.ORIGEN_CARGA);
		    carreraEstudiantePeriodosDTO
			    .add(carreraEstudiantePeriodoDTO);
		}
	    } catch (DeserializationException e) {

		erroresArchivo.append(generarLineaError(e, null));
	    }
	}
	deserializer.close(true);
	return erroresArchivo;
    }

    /**
     * 
     * Validaciones carga masiva Archivo Docentes
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:15:22
     * @return
     */
    private String validarArchivoDocentes() {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    uploadedFile.getInputstream(), ENCODING));

	    CsvConfiguration configuracion = new CsvConfiguration();
	    configuracion.setFieldDelimiter(Constantes.SEPARADOR_ARCHIVO_CSV);
	    CsvIOFactory csvFactory = CsvIOFactory.createFactory(configuracion,
		    CargaDocenteData.class);

	    Deserializer deserializer = csvFactory.createDeserializer();
	    deserializer.open(reader, tipoCarga.getNumeroColumnas());
	    if (!deserializer.hasNext()) {
		JsfUtil.msgError("El archivo no tiene datos para procesar");
		return null;
	    }

	    // Valida Detalle del Archivo
	    StringBuilder erroresArchivo = new StringBuilder();
	    erroresArchivo = validarDetalleArchivoDocentes(deserializer);
	    // En caso de existir errores exporta archivo de errores
	    if (erroresArchivo.toString().isEmpty()) {
		return null;
	    } else {
		return erroresArchivo.toString();
	    }
	} catch (InitialConfigurationException e) {
	    e.printStackTrace();
	    JsfUtil.msgError(e.getMessage());
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar el archivo, comuníquese con el administrador del sistema.");
	    return null;
	}

    }

    private StringBuilder validarDetalleArchivoDocentes(
	    Deserializer deserializer) {
	StringBuilder erroresArchivo = new StringBuilder();
	personasDTO = new ArrayList<>();
	LinkedHashMap<String, Integer> cedulas = new LinkedHashMap<>();
	while (deserializer.hasNext()) {

	    try {
		CargaDocenteData cargaDocenteData = deserializer.next();
		boolean validacion = true;
		if (cargaDocenteData == null) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    "Registro no contiene datos.\r\n"));
		    continue;
		}
		if (ec.gob.ceaaces.catalogo.enumeraciones.TipoDocumentoEnum.CEDULA
		        .getValue().equals(
		                cargaDocenteData.getTipoIdentificacion())) {

		    if (!Util.validarCedula(cargaDocenteData
			    .getIdentificacion())) {
			// si existe errores en la validacion de la cedula
			// se agrega el mensaje de error
			erroresArchivo
			        .append(generarLineaError(
			                deserializer,
			                "El docente con CEDULA '"
			                        + cargaDocenteData
			                                .getIdentificacion()
			                        + "', no pasa la validación del dígito verificador"));
			validacion = false;

		    }
		}
		if (validacion
		        && cedulas.get(cargaDocenteData.getIdentificacion()) != null) {
		    erroresArchivo
			    .append(generarLineaError(
			            deserializer,
			            "La identificación '"
			                    + cargaDocenteData
			                            .getIdentificacion()
			                    + "', se encuentra duplicada con la de línea -> "
			                    + cedulas.get(cargaDocenteData
			                            .getIdentificacion())));
		    validacion = false;
		}
		// Recupera y transforma registro de archivo CVS
		if (validacion) {
		    cedulas.put(cargaDocenteData.getIdentificacion(),
			    new Integer(deserializer.getInputPosition()
			            .getLineNumber()));
		    // Si no existe errores en el registro se agrega a
		    // la lista

		    PersonaDTO personaDTO = new PersonaDTO();
		    personaDTO.setTipoIdentificacion(cargaDocenteData
			    .getTipoIdentificacion());
		    personaDTO.setIdentificacion(cargaDocenteData
			    .getIdentificacion());
		    personaDTO.setNombres(cargaDocenteData.getNombres());
		    personaDTO.setApellidoMaterno(cargaDocenteData
			    .getSegundoApellido());
		    personaDTO.setApellidoPaterno(cargaDocenteData
			    .getPrimerApellido());
		    personaDTO.setIdentificacion(cargaDocenteData
			    .getIdentificacion());
		    personaDTO.setFechaNacimiento(cargaDocenteData
			    .getFechaNacimiento());
		    personaDTO.setSexo(GeneroEnum.valueOf(cargaDocenteData
			    .getGenero()));
		    personaDTO.setDireccion(cargaDocenteData.getDireccion());
		    personaDTO.setTipoIdentificacion(cargaDocenteData
			    .getTipoIdentificacion());
		    personaDTO.setDiscapacidad(DiscapacidadEnum
			    .valueOf(cargaDocenteData.getDiscapacidad()));

		    personaDTO.setDireccion(cargaDocenteData.getDireccion());
		    personaDTO.setEmailPersonal(cargaDocenteData
			    .getEmailPersonal());
		    personaDTO.setIdInformacionIes(informacionIesDTO.getId());
		    personaDTO.setIesDTO(ies);
		    personaDTO.setNumeroConadis(cargaDocenteData
			    .getNumeroConadis());
		    personaDTO.setActivo(true);
		    personaDTO.setAuditoria(auditoriaDTO);
		    personaDTO.setFaseIesDTO(faseIesDTO);
		    personaDTO.setOrigenCarga(Constantes.ORIGEN_CARGA);
		    PaisDTO paisDTO = new PaisDTO();
		    paisDTO.setNombre(cargaDocenteData.getPaisOrigen());
		    PuebloDTO puebloDTO = new PuebloDTO();
		    puebloDTO.setNombre(cargaDocenteData.getPueblo());
		    ParroquiaIesDTO parroquiaDTO = new ParroquiaIesDTO();
		    parroquiaDTO.setNombre(cargaDocenteData.getParroquia());
		    personaDTO.setPaisOrigen(paisDTO);
		    personaDTO.setPuebloDTO(puebloDTO);
		    personaDTO.setParroquiaDTO(parroquiaDTO);
		    DocenteDTO docenteDTO = new DocenteDTO();

		    docenteDTO.setEmailInstitucional(cargaDocenteData
			    .getEmailInstitucional());
		    docenteDTO.setFechaIngresoIes(cargaDocenteData
			    .getFechaIngresoIes());
		    docenteDTO.setFechaSalidaIes(cargaDocenteData
			    .getFechaSalidaIes());
		    docenteDTO.setActivo(true);
		    docenteDTO.setAuditoria(auditoriaDTO);
		    docenteDTO.setFaseIesDTO(faseIesDTO);
		    docenteDTO.setOrigenCarga(Constantes.ORIGEN_CARGA);
		    personaDTO.setDocenteDTO(docenteDTO);
		    personasDTO.add(personaDTO);
		}
	    } catch (DeserializationException e) {

		erroresArchivo.append(generarLineaError(e, null));
	    }
	}
	deserializer.close(true);
	return erroresArchivo;
    }

    /**
     * 
     * Validaciones carga masiva Archivo Libros
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:15:22
     * @return
     */
    private String validarArchivoLibros() {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    uploadedFile.getInputstream(), ENCODING));

	    CsvConfiguration configuracion = new CsvConfiguration();
	    configuracion.setFieldDelimiter(Constantes.SEPARADOR_ARCHIVO_CSV);
	    CsvIOFactory csvFactory = CsvIOFactory.createFactory(configuracion,
		    CargaLibroData.class);

	    Deserializer deserializer = csvFactory.createDeserializer();
	    deserializer.open(reader, tipoCarga.getNumeroColumnas());
	    if (!deserializer.hasNext()) {
		JsfUtil.msgError("El archivo no tiene datos para procesar");
		return null;
	    }

	    // Valida Detalle del Archivo
	    StringBuilder erroresArchivo = new StringBuilder();
	    erroresArchivo = validarDetalleArchivoLibros(deserializer);
	    // En caso de existir errores exporta archivo de errores
	    if (erroresArchivo.toString().isEmpty()) {
		return null;
	    } else {
		return erroresArchivo.toString();
	    }
	} catch (InitialConfigurationException e) {
	    e.printStackTrace();
	    JsfUtil.msgError(e.getMessage());
	    return null;
	} catch (IOException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar el archivo, comuníquese con el administrador del sistema.");
	    return null;
	}

    }

    private StringBuilder validarDetalleArchivoLibros(Deserializer deserializer) {
	StringBuilder erroresArchivo = new StringBuilder();
	librosDTO = new ArrayList<>();
	LinkedHashMap<String, Integer> libros = new LinkedHashMap<>();
	while (deserializer.hasNext()) {

	    try {
		CargaLibroData cargaLibro = deserializer.next();
		if (cargaLibro == null) {
		    erroresArchivo.append(generarLineaError(deserializer,
			    "Registro no contiene datos.\r\n"));
		    continue;
		}
		boolean validacion = true;
		if (libros.get(cargaLibro.getCodigoLibro()) != null) {
		    erroresArchivo
			    .append(generarLineaError(
			            deserializer,
			            "El Libro con código '"
			                    + cargaLibro.getCodigoLibro()
			                    + "', se encuentra duplicado con el registro: "
			                    + libros.get(cargaLibro
			                            .getCodigoLibro()))
			            + ".\r\n");
		    validacion = false;
		}
		// Recupera y transforma registro de archivo CVS
		if (validacion) {
		    libros.put(cargaLibro.getCodigoLibro(), new Integer(
			    deserializer.getInputPosition().getLineNumber()));
		    // si no existe errores en el registro se agrega a
		    // la lista

		    LibroDTO libroDTO = new LibroDTO();
		    libroDTO.setCodigo(cargaLibro.getCodigoLibro());
		    libroDTO.setTitulo(cargaLibro.getTitulo());
		    libroDTO.setTipo(TipoLibroEnum.valueOf(cargaLibro.getTipo()));
		    libroDTO.setTipoMedioSoporte(TipoMedioSoporteEnum
			    .valueOf(cargaLibro.getTipoMedioSoporte()));
		    libroDTO.setFaseIesDTO(faseIesDTO);
		    libroDTO.setNumEjemplares(Integer.parseInt(cargaLibro
			    .getNumEjemplares()));
		    libroDTO.setActivo(true);
		    libroDTO.setAuditoria(auditoriaDTO);
		    libroDTO.setNombreBibloteca(cargaLibro
			    .getNombreBiblioteca());
		    SedeIesDistribucionFisicaDTO sedeDistribucionFisicaDTO = new SedeIesDistribucionFisicaDTO();
		    sedeDistribucionFisicaDTO.setCodigo(cargaLibro
			    .getCodigoSedeDistribucion());
		    libroDTO.setSedeDistribucionFisicaDTO(sedeDistribucionFisicaDTO);
		    libroDTO.setOrigenCarga(Constantes.ORIGEN_CARGA);
		    librosDTO.add(libroDTO);
		}
	    } catch (DeserializationException e) {

		erroresArchivo.append(generarLineaError(e, null));
	    }
	}
	deserializer.close(true);
	return erroresArchivo;
    }

    /**
     * 
     * Genera Linea de Error a partir del archivo CSV
     * 
     * @author tfreire
     * @version 24/07/2014 - 15:44:35
     * @param objeto
     * @param mensaje
     * @return
     */
    private static final String generarLineaError(Object objeto, String mensaje) {
	if (objeto instanceof DeserializationException) {
	    DeserializationException objetoMensaje = (DeserializationException) objeto;
	    return objetoMensaje.getMessage();
	} else if (objeto instanceof Deserializer) {
	    Deserializer objetoMensaje = (Deserializer) objeto;
	    return "\r\nLÍNEA: "
		    + objetoMensaje.getInputPosition().getLineNumber() + "\r\n"
		    + mensaje;
	} else {
	    return "";
	}
    }

    /**
     * 
     * Registra estado de carga masiva
     * 
     * @author tfreire
     * @version 01/08/2014 - 10:11:54
     * @return
     */
    private ArchivoCargaMasivaDTO registrarEstadoCarga(String nombreArchivo,
	    String urlPorIes) {
	ArchivoCargaMasivaDTO archivoCargaDTO = new ArchivoCargaMasivaDTO();
	try {
	    if (listaArchivosCarga == null || listaArchivosCarga.isEmpty()) {
		archivoCargaDTO = new ArchivoCargaMasivaDTO();
	    } else {
		archivoCargaDTO = new ArchivoCargaMasivaDTO(); // listaArchivosCarga.get(0);
		archivoCargaDTO.setFaseIesDTO(faseIesDTO);
		archivoCargaDTO.setNombreArchivoError(null);
		archivoCargaDTO.setTotalErrores(null);
		archivoCargaDTO.setTotalProcesados(null);
		archivoCargaDTO.setTotalRegistros(null);
		archivoCargaDTO.setUrlError(null);
		if (tipoCarga.getCodigo().startsWith(
		        Constantes.PREFIJO_MATRICULA)) {
		    archivoCargaDTO
			    .setFrecuencia(idPeriodoAcademico.toString());
		}
	    }
	    archivoCargaDTO.setActivo(true);
	    archivoCargaDTO.setAuditoriaDTO(auditoriaDTO);
	    archivoCargaDTO.setEstado(EstadoCargaMasivaEnum.PREVALIDADO);
	    archivoCargaDTO.setFaseIesDTO(faseIesDTO);
	    archivoCargaDTO.setIesDTO(ies);
	    archivoCargaDTO.setNombreArchivo(nombreArchivo);
	    archivoCargaDTO.setTipoCargaMasivaDTO(tipoCarga);
	    archivoCargaDTO.setUrl(urlPorIes);
	    if (tipoCarga.getCodigo().startsWith(Constantes.PREFIJO_MATRICULA)) {
		archivoCargaDTO.setFrecuencia(idPeriodoAcademico.toString());
	    }

	    archivoCargaDTO = cargaMasivaServicio
		    .crearActualizar(archivoCargaDTO);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al registrar estado del archivo, comuníquese con el administrador del sistema");
	}

	return archivoCargaDTO;
    }

    public void limpiar() {
	listaArchivosCarga.clear();
	tipoCarga = null;
	idPeriodoAcademico = -1L;
	periodosAcademicos.clear();

    }

    private void cargarPeriodosAcademicos() {
	try {
	    idPeriodoAcademico = -1L;
	    periodosAcademicos.clear();
	    periodosAcademicos = registroServicio
		    .obtenerPeriodosAcademicosPorIes(informacionIesDTO.getId());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar periodos academicos de Ies, comuníquese con el administrador del sistema");
	    return;
	}
    }

    /**
     * 
     * Obtiene texto que se usará como cabecera de archivo de error
     * 
     * @author tfreire
     * @version 23/07/2014 - 11:18:25
     * @param archivoCargaMasivaDTO
     * @param iesDTO
     * @param tipoArchivo
     * @return
     */
    private String obtenerCabeceraArchivoErrorFormato() {
	String cabeceraError = "====================================================================================================\r\n"
	        + "CONSEJO DE EVALUACIÓN, ACREDITACIÓN Y ASEGURAMIENTO DE LA CALIDAD DE LA EDUCACIÓN SUPERIOR - CEAACES\r\n"
	        + "INSTITUCIÓN   :  "
	        + ies.getNombre()
	        + "\r\n"
	        + "TIPO ARCHIVO  :  Carga Masiva de "
	        + tipoCarga.getEtiqueta()
	        + "\r\n"
	        + "FECHA PROCESO :  "
	        + new Date()
	        + "\r\n"
	        + "====================================================================================================\r\n\r\n"
	        + "El archivo de carga masiva de "
	        + tipoCarga.getEtiqueta()
	        + " que está intentando subir tiene los siguientes ERRORES de FORMATO: \r\n";
	return cabeceraError;
    }

    public UploadedFile getUploadedFile() {
	return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
	this.uploadedFile = uploadedFile;
    }

    public String getRuta() {
	return ruta;
    }

    public void setRuta(String ruta) {
	this.ruta = ruta;
    }

    public TipoCargaMasivaDTO getTipoCarga() {
	return tipoCarga;
    }

    public IesDTO getIes() {
	return ies;
    }

    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public List<TipoCargaMasivaDTO> getTiposArchivo() {
	return tiposArchivo;
    }

    public void setTiposArchivo(List<TipoCargaMasivaDTO> tiposArchivo) {
	this.tiposArchivo = tiposArchivo;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public StreamedContent getArchivoCargaDescarga() {
	return archivoCargaDescarga;
    }

    public void setArchivoCargaDescarga(StreamedContent archivoCargaDescarga) {
	this.archivoCargaDescarga = archivoCargaDescarga;
    }

    public StreamedContent getPlantillaDescarga() {
	if (tipoCarga != null) {
	    String[] nombreArchivo = tipoCarga.getPlantilla().split("[.]");
	    String extension = nombreArchivo[nombreArchivo.length - 1];
	    return ArchivoUtil.obtenerDescarga(tipoCarga.getUrl(),
		    tipoCarga.getPlantilla(), extension);
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar un tipo de archivo");
	    return null;
	}

    }

    public StreamedContent exportarArchivoCarga(
	    ArchivoCargaMasivaDTO archivoCarga) throws IOException {
	return ArchivoUtil.descargarPlantilla(archivoCarga.getUrl(),
	        archivoCarga.getNombreArchivo(), Constantes.CONTENT_TYPE_CSV);
    }

    public StreamedContent exportarArchivoCargaError(
	    ArchivoCargaMasivaDTO archivoCarga) throws IOException {
	return ArchivoUtil.descargarPlantilla(archivoCarga.getUrlError(),
	        archivoCarga.getNombreArchivoError(),
	        Constantes.CONTENT_TYPE_TEXT);
    }

    public StreamedContent exportarArchivoErroresFormato() throws IOException {
	mostrarMensajeError = false;
	return ArchivoUtil.descargarPlantilla(
	        obtenerCabeceraArchivoErrorFormato()
	                + this.mensajesError.toString(),
	        Constantes.CONTENT_TYPE_TEXT);
    }

    public void setPlantillaDescarga(StreamedContent plantillaDescarga) {
	this.plantillaDescarga = plantillaDescarga;
    }

    public List<ArchivoCargaMasivaDTO> getListaArchivosCarga() {
	return listaArchivosCarga;
    }

    public Boolean getMostrarMensajeError() {
	return mostrarMensajeError;
    }

    public void setMostrarMensajeError(Boolean mostrarMensajeError) {
	this.mostrarMensajeError = mostrarMensajeError;
    }

    public void setTipoCarga(TipoCargaMasivaDTO tipoCarga) {
	this.tipoCarga = tipoCarga;
    }

    public List<PeriodoAcademicoDTO> getPeriodosAcademicos() {
	return periodosAcademicos;
    }

    public void setPeriodosAcademicos(
	    List<PeriodoAcademicoDTO> periodosAcademicos) {
	this.periodosAcademicos = periodosAcademicos;
    }

    public boolean isMostrarPeriodosAcademicos() {
	return mostrarPeriodosAcademicos;
    }

    public void setMostrarPeriodosAcademicos(boolean mostrarPeriodosAcademicos) {
	this.mostrarPeriodosAcademicos = mostrarPeriodosAcademicos;
    }

    public Long getIdPeriodoAcademico() {
	return idPeriodoAcademico;
    }

    public void setIdPeriodoAcademico(Long idPeriodoAcademico) {
	this.idPeriodoAcademico = idPeriodoAcademico;
    }

    public int getMesInicioPeriodoAcademico() {
	return mesInicioPeriodoAcademico;
    }

    public void setMesInicioPeriodoAcademico(int mesInicioPeriodoAcademico) {
	this.mesInicioPeriodoAcademico = mesInicioPeriodoAcademico;
    }

    public int getAnioInicioPeriodoAcademico() {
	return anioInicioPeriodoAcademico;
    }

    public void setAnioInicioPeriodoAcademico(int anioInicioPeriodoAcademico) {
	this.anioInicioPeriodoAcademico = anioInicioPeriodoAcademico;
    }

    public int getAnioFinPeriodoAcademico() {
	return anioFinPeriodoAcademico;
    }

    public void setAnioFinPeriodoAcademico(int anioFinPeriodoAcademico) {
	this.anioFinPeriodoAcademico = anioFinPeriodoAcademico;
    }

    public int getMesFinPeriodoAcademico() {
	return mesFinPeriodoAcademico;
    }

    public void setMesFinPeriodoAcademico(int mesFinPeriodoAcademico) {
	this.mesFinPeriodoAcademico = mesFinPeriodoAcademico;
    }

    public SelectItem[] getMESES() {
	return MESES;
    }

    public List<SelectItem> getANIOS() {
	return ANIOS;
    }

    public void setANIOS(List<SelectItem> aNIOS) {
	ANIOS = aNIOS;
    }

    public void convertMeses(ValueChangeEvent event) {
	mesInicioPeriodoAcademico = (Integer) event.getNewValue();
    }

    public void setFechaMin(Date fechaMin) {
	this.fechaMin = fechaMin;
    }

    public PeriodoAcademicoDTO getPeriodoAcademicoAgregar() {
	return periodoAcademicoAgregar;
    }

    public void setPeriodoAcademicoAgregar(
	    PeriodoAcademicoDTO periodoAcademicoAgregar) {
	this.periodoAcademicoAgregar = periodoAcademicoAgregar;
    }

    public PeriodoAcademicoDTO getPeriodoAcademicoEliminar() {
	return periodoAcademicoEliminar;
    }

    public void setPeriodoAcademicoEliminar(
	    PeriodoAcademicoDTO periodoAcademicoEliminar) {
	this.periodoAcademicoEliminar = periodoAcademicoEliminar;
    }

    public ArchivoCargaMasivaDTO getArchivoSeleccionado() {
	return archivoSeleccionado;
    }

    public void setArchivoSeleccionado(ArchivoCargaMasivaDTO archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

}
