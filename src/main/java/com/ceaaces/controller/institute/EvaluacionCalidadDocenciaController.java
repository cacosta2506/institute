package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.VariableSubGrupoEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ItemEvaluacion;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CargoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CursoCapacitacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ExperienciaProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MuestraDetalleDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.enumeraciones.ConceptoEnum;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvaluacionCalidadDocenciaController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger
	    .getLogger(EvaluacionCalidadDocenciaController.class
	            .getSimpleName());

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    private InformacionIesDTO informacionIes;
    private FaseIesDTO faseIesDTO;

    private List<MuestraDetalleDTO> listaMuestraDetalle;
    private List<ItemEvaluacion> listaMuestraDetalle1;
    private ValorVariableDTO valorVariableDTO;
    private StreamedContent documentoDescarga;
    private Boolean cambiosVariable = false;
    private Boolean busqueda = false;
    private List<FormacionProfesionalDTO> listaFormacion;
    private String fichero;
    private String extensionDocumento;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private UploadedFile file;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private Boolean alertaEvaluador = false;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean descargar = false;
    private String identificacion;
    private String url;
    private Long idEvidencia;
    private String nombreFichero;
    private EvidenciaDTO evidenciaDto;
    private List<PersonaDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ItemEvaluacion itemnSeleccionado;
    private ContratacionDTO contratoSeleccionado;
    private ExperienciaProfesionalDTO experienciaSeleccionada;
    private CursoCapacitacionDTO capacitacionSeleccionada;
    private CargoAcademicoDTO cargoAcademicaSeleccionado;
    private List<CargoAcademicoDTO> listaCargosAcademicos;
    private List<PublicacionDTO> listaPublicaciones;
    private List<ExperienciaProfesionalDTO> listaExperienciaProfesional;
    private List<ItemEvaluacion> listaMuestraDetalleDocentes;
    private List<ItemEvaluacion> listaMuestraDetalleDocentesfilter;
    private List<ItemEvaluacion> listaMuestraDetalleDocentesOriginal;
    private List<ValorVariableDTO> listaVariablesCalidadDocenciaCuerpoDocente;
    private List<ValorVariableDTO> listaValorVariableOriginal;
    private List<ValorVariableDTO> listaValorVariableOriginalFormacion;
    private List<ValorVariableDTO> listaValorVariableOriginalDPC;
    private List<ValorVariableDTO> listaVariablesOriginalRemuneracion;
    private List<ValorVariableDTO> listaVariablesCalidadDocenciaRemuneracion;

    private List<ValorVariableDTO> listaVariablesCalidadDocenciaFormacionDesarrollo;
    private List<ValorVariableDTO> listaVariablesCalidadDocenciaProduccionConocimiento;;
    private List<ValorVariableDTO> listaValorVariablesModificados;
    private List<ItemEvaluacion> listaItemsModificados;
    private List<ContratacionDTO> listaContratacion;
    private ItemEvaluacion docenteSeleccionado;
    private Boolean alertaIngresoVerificado = false;
    private List<CursoCapacitacionDTO> listaCapacitacion;
    private Boolean evidencias = false;
    private Boolean contratos = false;
    private Boolean capacitacion = false;
    private Boolean cargos = false;
    private Boolean evidenciaConcepto = false;
    private Boolean experiencia = false;
    private Boolean formacion = false;
    private ConceptoDTO conceptoSeleccionado;
    private List<DocenteAsignaturaDTO> listaDistributivo;
    private String usuario;
    private String idProceso;
    private IesDTO ies;

    public EvaluacionCalidadDocenciaController() {
	this.listaMuestraDetalle = new ArrayList<>();
	this.valorVariableDTO = new ValorVariableDTO();
	this.listaMuestraDetalleDocentes = new ArrayList<ItemEvaluacion>();
	this.listaVariablesCalidadDocenciaCuerpoDocente = new ArrayList<>();
	this.listaVariablesCalidadDocenciaRemuneracion = new ArrayList<>();
	this.listaVariablesCalidadDocenciaFormacionDesarrollo = new ArrayList<>();
	this.listaExperienciaProfesional = new ArrayList<ExperienciaProfesionalDTO>();
	this.listaVariablesCalidadDocenciaProduccionConocimiento = new ArrayList<>();
	this.listaValorVariablesModificados = new ArrayList<>();
	this.listaMuestraDetalleDocentesOriginal = new ArrayList<ItemEvaluacion>();
	this.listaDocentes = new ArrayList<PersonaDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.listaPublicaciones = new ArrayList<PublicacionDTO>();
	this.listaValorVariableOriginal = new ArrayList<ValorVariableDTO>();
	this.listaValorVariableOriginalFormacion = new ArrayList<ValorVariableDTO>();
	this.listaCargosAcademicos = new ArrayList<CargoAcademicoDTO>();
	this.listaCapacitacion = new ArrayList<CursoCapacitacionDTO>();
	conceptoSeleccionado = new ConceptoDTO();
	this.listaMuestraDetalle1 = new ArrayList<ItemEvaluacion>();
	this.listaFormacion = new ArrayList<FormacionProfesionalDTO>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaDistributivo = new ArrayList<DocenteAsignaturaDTO>();
	docenteSeleccionado = new ItemEvaluacion();
	this.listaVariablesOriginalRemuneracion = new ArrayList<ValorVariableDTO>();
	this.listaMuestraDetalleDocentesfilter = new ArrayList<ItemEvaluacion>();
	formacionSeleccionada = new FormacionProfesionalDTO();
	itemnSeleccionado = new ItemEvaluacion();
	contratoSeleccionado = new ContratacionDTO();
	experienciaSeleccionada = new ExperienciaProfesionalDTO();
	capacitacionSeleccionada = new CursoCapacitacionDTO();
	cargoAcademicaSeleccionado = new CargoAcademicoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	this.listaValorVariableOriginalDPC = new ArrayList<ValorVariableDTO>();
	evidenciaDto = new EvidenciaDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();

    }

    @PostConstruct
    public void datosIniciales() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");
	    this.idProceso = controller.getFaseIesDTO().getProcesoDTO().getId()
		    .toString();

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.ies = controller.getIes();
	    this.faseIesDTO = controller.getFaseIesDTO();
	    LOG.info("****faseIesDTO: " + faseIesDTO.getId());

	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(ies);
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());

	    if (controller.getPerfil().getNombre().startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    cargarVariables();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    /**
     * Cargar la Lista de Variables por Grupo
     * 
     * @author eviscarra
     * @see calcularTotalVerificadoVariable
     */
    public String cargarVariables() {
	try {

	    /* Variables Calidad Docencia */
	    this.listaVariablesCalidadDocenciaCuerpoDocente = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "cuerpo_docente");

	    this.listaVariablesCalidadDocenciaRemuneracion = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "remuneracion");

	    this.listaVariablesCalidadDocenciaFormacionDesarrollo = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "formacion_desarrollo");

	    this.listaVariablesCalidadDocenciaProduccionConocimiento = evaluacionServicio
		    .obtenerInformacionVariables("PUB", informacionIes.getId(),
		            "%CUANTITATIVA%", "produccion_conocimiento");
	    cargarVariablesOriginales();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

	return "";
    }

    public void cargarVariablesOriginales() {

	listaValorVariableOriginal.clear();
	for (ValorVariableDTO variable : listaVariablesCalidadDocenciaCuerpoDocente) {
	    listaValorVariableOriginal
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaValorVariableOriginalFormacion.clear();
	for (ValorVariableDTO variable : listaVariablesCalidadDocenciaFormacionDesarrollo) {
	    listaValorVariableOriginalFormacion
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaVariablesOriginalRemuneracion.clear();
	for (ValorVariableDTO variable : listaVariablesCalidadDocenciaRemuneracion) {
	    listaVariablesOriginalRemuneracion
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaValorVariableOriginalDPC.clear();
	for (ValorVariableDTO variable : listaVariablesCalidadDocenciaProduccionConocimiento) {
	    listaValorVariableOriginalDPC
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
    }

    public void cargarMuestraVariableDetalleEstudiante(
	    List<PersonaDTO> estudiantes, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleDocentes.clear();
	for (int i = 0; i < estudiantes.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla()
		        .equals(estudiantes.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setDocente(estudiantes.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleDocentes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleDocentes.size());
		}
	    }
	}
    }

    public void cargarMuestraVariableDetallePublicacion(
	    List<PublicacionDTO> publicacion, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleDocentes.clear();
	for (int i = 0; i < publicacion.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla()
		        .equals(publicacion.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setPublicacionDTO(publicacion.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleDocentes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleDocentes.size());
		}
	    }
	}
    }

    public void cargarMuestraVariableDetalleProducciones(
	    List<ProduccionDTO> produccion, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleDocentes.clear();
	for (int i = 0; i < produccion.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla()
		        .equals(produccion.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setProduccionDTO(produccion.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleDocentes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleDocentes.size());
		}
	    }
	}
    }

    public void cargarMuestraVariableDetalleProyectos(
	    List<ProyectoDTO> proyecto, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleDocentes.clear();
	for (int i = 0; i < proyecto.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla().equals(proyecto.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setProyecto(proyecto.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleDocentes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleDocentes.size());
		}
	    }
	}
    }

    public void cargarItemMuestraDetalle() {
	listaMuestraDetalleDocentesOriginal.clear();
	for (ItemEvaluacion muestra : listaMuestraDetalleDocentes) {
	    listaMuestraDetalleDocentesOriginal
		    .add((ItemEvaluacion) SerializationUtils.clone(muestra));
	}
    }

    public void buscarDocentePorCedula() {
	if (!identificacion.equals("")) {
	    busqueda = true;
	    docenteSeleccionado = null;
	    evidencias = false;
	    contratos = false;
	    evidenciaConcepto = false;
	    experiencia = false;
	    capacitacion = false;
	    formacion = false;
	    cargos = false;
	    DocenteDTO docenteDTO = new DocenteDTO();
	    PersonaDTO personaDTO = new PersonaDTO();
	    docenteDTO.setIdentificacion(identificacion);
	    docenteDTO.setIdInformacionIes(this.informacionIes.getId());
	    try {
		docenteDTO = registroServicio
		        .obtenerDocentePorCedula(docenteDTO);

		if (docenteDTO != null && docenteDTO.getActivo()) {
		    personaDTO
			    .setIdentificacion(docenteDTO.getIdentificacion());
		    personaDTO.setNombres(docenteDTO.getNombres());
		    personaDTO.setApellidoPaterno(docenteDTO
			    .getApellidoPaterno());
		    personaDTO.setApellidoMaterno(docenteDTO
			    .getApellidoMaterno());

		    listaDocentes.clear();
		    listaDocentes.add(docenteDTO);
		    cargarMuestraVariableDetalleEstudiante(listaDocentes,
			    listaMuestraDetalle);
		    cargarItemMuestraDetalle();
		    JsfUtil.msgInfo("Registro encontrado");
		} else {
		    JsfUtil.msgInfo("Registro no encontrado!");
		}

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {
	    cargarDocentes();

	}
    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseIesDTO.getId() + "/" + ies.getCodigo() + "/"
	        + idEvidencia + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseIesDTO.getId() + "/" + ies.getCodigo() + "/"
	        + idEvidencia + "/";
	LOG.info("DESTINO: " + destino);

	if (ArchivoUtil.crearDirectorio(urlDestino)) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    cargarEConcepto();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void handleFileUpload(FileUploadEvent event) {

	UploadedFile file = event.getFile();

	// application code
    }

    public void uploadfile(FileUploadEvent event) {
	file = event.getFile();
	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(evidenciaConceptoSeleccionado);
	    evidenciaDto.setIdTabla(idEvidencia);
	    evidenciaDto.setFaseIesDTO(faseIesDTO);

	    evidenciaDto.setTabla(conceptoSeleccionado.getOrigen());

	    Date date = new Date();
	    DateFormat hourdateFormat = new SimpleDateFormat("ddMMYYHHmmss");
	    String[] nameFile = file.getFileName().split("[.]");
	    String extension = nameFile[nameFile.length - 1];
	    long max = 20 * 1024 * 1024;
	    long fileuploadsize = file.getSize();
	    if (fileuploadsize > max) {
		JsfUtil.msgError("El tamaño del archivo excede 10MB");
		return;
	    } else {

		byte[] contenido = this.file.getContents();

		LOG.info("NAME FILE: " + nameFile[0]);

		String nombreGenerado = nombreFichero + "_COD_"
		        + evidenciaConceptoSeleccionado.getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + faseIesDTO.getId() + "/"
		        + ies.getCodigo() + "/" + idEvidencia + "/";

		LOG.info("URL A GUARDAR: " + urlNew);
		try {
		    boolean creoArchivo = ArchivoUtil.escribirArchivo(
			    nombreGenerado, contenido, urlNew);
		    if (creoArchivo) {
			evidenciaDto.setNombreOriginal(nameFile[0] + "."
			        + extension);
			evidenciaDto.setNombreArchivo(nombreGenerado);
			evidenciaDto.setUrl(urlNew);
			evidenciaDto.setActivo(true);

			auditoria.setFechaModificacion(new Date());

			auditoria.setUsuarioModificacion(usuario);
			evidenciaDto.setAuditoriaDTO(auditoria);

			institutosServicio.crearActualizar(evidenciaDto);

			evidenciaDto = new EvidenciaDTO();
			cargarEConcepto();
			JsfUtil.msgInfo("Evidencia subida correctamente");
		    }
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	} else {
	    JsfUtil.msgError("Debe escoger un documento");
	}

    }

    public void cargarConceptos() {
	evidencias = true;
	contratos = false;
	capacitacion = false;
	evidenciaConcepto = false;
	experiencia = false;
	formacion = false;
	cargos = false;
	conceptoSeleccionado = null;
	try {
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());
	    listaDistributivo = registroServicio
		    .obtenerDistributivoPorDocenteEInformacionIes(
		            docenteSeleccionado.getDocente().getId(),
		            informacionIes.getId());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarEConceptoContrato() {
	contratos = true;
	conceptoSeleccionado = null;
	evidenciaConcepto = false;
	try {
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	for (ConceptoDTO variable : listaEvidencia) {
	    if (variable.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		conceptoSeleccionado = variable;
		break;
	    }
	}

	if (conceptoSeleccionado != null) {
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {

		contratos = true;

		try {
		    listaContratacion = registroServicio
			    .obtenerContratacionPorPersona(docenteSeleccionado
			            .getDocente().getId(), informacionIes
			            .getId());
		} catch (ServicioException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		LOG.info("listaContratacion: " + listaContratacion.size());

	    }
	}

    }

    public void cargarEConcepto() {
	try {

	    evidenciaConcepto = true;
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		idEvidencia = contratoSeleccionado.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + contratoSeleccionado.getId();

	    } else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		idEvidencia = experienciaSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + experienciaSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CAPACITACION.getValor())) {
		idEvidencia = capacitacionSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + capacitacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.FORMACION.getValor())) {
		idEvidencia = formacionSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + formacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CARGOS.getValor())) {
		idEvidencia = cargoAcademicaSeleccionado.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + cargoAcademicaSeleccionado.getId();

	    }

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(ies.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarEConceptoPublicacion() {
	LOG.info("cargarEConceptoPublicacion");
	conceptoSeleccionado = null;

	LOG.info("ConceptoEnum.PUBLICACIONES.getValue: "
	        + ConceptoEnum.PUBLICACIONES.getValue());
	conceptoSeleccionado = catalogoServicio
	        .obtenerConceptoPorCodigo(ConceptoEnum.PUBLICACIONES.getValue());
	LOG.info("conceptoSeleccionado" + conceptoSeleccionado);
	idEvidencia = itemnSeleccionado.getPublicacionDTO().getId();
	nombreFichero = ies.getCodigo() + "_"
	        + itemnSeleccionado.getPublicacionDTO().getId();

	cargarEConceptoProduccionPublicacion();

    }

    public void cargarEConceptoProduccion() {
	LOG.info("cargarEConceptoProduccion");
	conceptoSeleccionado = null;

	LOG.info("ConceptoEnum.PRODUCCIONES.getValue: "
	        + ConceptoEnum.PRODUCCIONES.getValue());
	conceptoSeleccionado = catalogoServicio
	        .obtenerConceptoPorCodigo(ConceptoEnum.PRODUCCIONES.getValue());
	LOG.info("conceptoSeleccionado" + conceptoSeleccionado);
	idEvidencia = itemnSeleccionado.getProduccionDTO().getId();
	nombreFichero = ies.getCodigo() + "_"
	        + itemnSeleccionado.getProduccionDTO().getId();

	cargarEConceptoProduccionPublicacion();

    }

    public void cargarEConceptoProduccionPublicacion() {
	LOG.info("cargarEConceptoProduccionPublicacion");

	try {

	    LOG.info("idEvidencia: " + idEvidencia);
	    LOG.info("conceptoSeleccionado.getId(): "
		    + conceptoSeleccionado.getId());

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(ies.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());
	    evidenciaConcepto = true;
	    LOG.info("listaEvidenciaConcepto size: "
		    + listaEvidenciaConcepto.size());
	    LOG.info("cargarEConceptoProduccion -- FIN --");
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarEvidencias() {

	try {
	    if (conceptoSeleccionado != null) {
		if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CONTRATOS.getValor())) {

		    contratos = true;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = false;

		    listaContratacion = registroServicio
			    .obtenerContratacionPorPersona(docenteSeleccionado
			            .getDocente().getId(), informacionIes
			            .getId());
		    LOG.info("listaContratacion: " + listaContratacion.size());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		    contratos = false;
		    experiencia = true;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = false;

		    listaExperienciaProfesional = registroServicio
			    .obtenerExperienciaProfesionalPorDocenteEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());
		    LOG.info("listaExperienciaProfesional: "
			    + listaExperienciaProfesional.size());

		}

		else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CAPACITACION.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = true;
		    formacion = false;
		    cargos = false;

		    listaCapacitacion = registroServicio
			    .obtenerCursosPorDocenteEIes(docenteSeleccionado
			            .getDocente().getId(), ies.getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.FORMACION.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = true;
		    cargos = false;

		    listaFormacion = registroServicio
			    .obtenerFormacionProfesionalPorPersonaEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CARGOS.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = true;

		    listaCargosAcademicos = registroServicio
			    .obtenerCargosAcademicosPorDocenteEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());

		}
	    } else {
		contratos = false;
		evidenciaConcepto = false;
		experiencia = false;
		capacitacion = false;
		formacion = false;
		cargos = false;
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarDocentes() {
	cargarItemMuestraDetalle();
	docenteSeleccionado = null;
	evidencias = false;
	identificacion = "";
	busqueda = false;
	contratos = false;
	evidenciaConcepto = false;
	experiencia = false;
	capacitacion = false;
	formacion = false;
	cargos = false;

	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    listaDocentes = evaluacionServicio
		    .obtenerMuestraDetallePersonas(valorVariableDTO
		            .getCodigoMuestra());
	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableDTO.getCodigoMuestra());
	    cargarMuestraVariableDetalleEstudiante(listaDocentes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalle();
	    context.execute("dlgMuestraDocentes.show()");

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarDocentesRemuneracion() {
	cargarItemMuestraDetalle();
	docenteSeleccionado = null;
	evidencias = false;
	contratos = false;
	evidenciaConcepto = false;
	experiencia = false;
	capacitacion = false;
	formacion = false;
	cargos = false;

	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    listaDocentes = evaluacionServicio
		    .obtenerMuestraDetallePersonas(valorVariableDTO
		            .getCodigoMuestra());
	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableDTO.getCodigoMuestra());
	    cargarMuestraVariableDetalleEstudiante(listaDocentes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalle();
	    context.execute("dlgMuestraDocentesRem.show()");

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarPuProdProy() {
	cargarItemMuestraDetalle();
	docenteSeleccionado = null;
	evidencias = false;
	contratos = false;
	evidenciaConcepto = false;
	experiencia = false;
	capacitacion = false;
	formacion = false;
	cargos = false;
	List<ProduccionDTO> listaProducciones = new ArrayList<>();
	List<ProyectoDTO> listaProyectos = new ArrayList<>();

	RequestContext context = RequestContext.getCurrentInstance();
	if (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
	        .getVariableGrupoDTO().getNemonico().equals("PUB")
	        && this.valorVariableDTO.getVariableProcesoDTO()
	                .getVariableDTO().getTablaMuestra()
	                .equals("publicaciones")) {
	    try {
		listaPublicaciones = evaluacionServicio
		        .obtenerMuestraDetallePublicacion(valorVariableDTO
		                .getCodigoMuestra());

		listaMuestraDetalle = evaluacionServicio
		        .obtenerMuestraDetalle(valorVariableDTO
		                .getCodigoMuestra());
		cargarMuestraVariableDetallePublicacion(listaPublicaciones,
		        listaMuestraDetalle);
		cargarItemMuestraDetalle();
		context.execute("dlgMuestraPublicaciones.show()");

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else if (this.valorVariableDTO.getVariableProcesoDTO()
	        .getVariableDTO().getVariableGrupoDTO().getNemonico()
	        .equals("PUB")
	        && this.valorVariableDTO.getVariableProcesoDTO()
	                .getVariableDTO().getTablaMuestra()
	                .equals("producciones")) {

	    try {
		listaProducciones = evaluacionServicio
		        .obtenerMuestraDetalleProducciones(this.valorVariableDTO
		                .getCodigoMuestra());
		listaMuestraDetalle = evaluacionServicio
		        .obtenerMuestraDetalle(valorVariableDTO
		                .getCodigoMuestra());

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    cargarMuestraVariableDetalleProducciones(listaProducciones,
		    listaMuestraDetalle);
	    context.execute("dlgMuestraProducciones.show()");

	} else if (this.valorVariableDTO.getVariableProcesoDTO()
	        .getVariableDTO().getVariableGrupoDTO().getNemonico()
	        .equals("PUB")
	        && this.valorVariableDTO.getVariableProcesoDTO()
	                .getVariableDTO().getTablaMuestra().equals("proyectos")) {

	    try {
		listaProyectos = evaluacionServicio
		        .obtenerMuestraDetalleProyectos(this.valorVariableDTO
		                .getCodigoMuestra());
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    cargarMuestraVariableDetalleProyectos(listaProyectos,
		    listaMuestraDetalle);
	    context.execute("dlgMuestraProyectos.show()");
	}
    }

    public void calcularVariablesCalidadDocencia() {
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    List<ValorVariableDTO> valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCalidadDocenciaCuerpoDocente) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesCalidadDocenciaCuerpoDocente.clear();
	    listaVariablesCalidadDocenciaCuerpoDocente
		    .addAll(valoresVariableNuevos);

	    valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCalidadDocenciaFormacionDesarrollo) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesCalidadDocenciaFormacionDesarrollo.clear();
	    listaVariablesCalidadDocenciaFormacionDesarrollo
		    .addAll(valoresVariableNuevos);

	    valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCalidadDocenciaProduccionConocimiento) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesCalidadDocenciaProduccionConocimiento.clear();
	    listaVariablesCalidadDocenciaProduccionConocimiento
		    .addAll(valoresVariableNuevos);

	    valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCalidadDocenciaRemuneracion) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesCalidadDocenciaRemuneracion.clear();
	    listaVariablesCalidadDocenciaRemuneracion
		    .addAll(valoresVariableNuevos);
	    cargarVariables();
	    context.execute("dlgCalcularVariable.hide()");

	} catch (Exception e) {

	    JsfUtil.msgError("Error del sistema. "
		    + " Comuníquese con el administrador.");
	}
    }

    /**
     * 
     * Lista de Muestra Detalle de Proyectos y Actividades de Vinculación
     * 
     * @author eviscarra
     * @version 11/06/2014 - 15:55:13
     */
    public void cargarMuestraDetalleVariablesCalidadDocencia() {
	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    this.listaMuestraDetalle.clear();
	    List<MuestraDetalleDTO> listaMuestraDetalle = new ArrayList<>();
	    List<PersonaDTO> listaDocentes = new ArrayList<>();
	    List<PublicacionDTO> listaPublicaciones = new ArrayList<>();
	    List<ProduccionDTO> listaProducciones = new ArrayList<>();
	    List<ProyectoDTO> listaProyectos = new ArrayList<>();

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(this.valorVariableDTO
		            .getCodigoMuestra());
	    LOG.info("LISTA MUESTRA DETALLE: " + listaMuestraDetalle.size());

	    if (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
		    .getId() == 102L) {
		alertaIngresoVerificado = true;
	    }

	    if (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
		    .getVariableGrupoDTO().getNemonico().equals("PUB")
		    && this.valorVariableDTO.getVariableProcesoDTO()
		            .getVariableDTO().getTablaMuestra()
		            .equals("publicaciones")) {

		// if
		// (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
		// .getId() == 138L
		// || this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 139L
		// || this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 140L) {
		// PUBLICACIONES

		listaPublicaciones = evaluacionServicio
		        .obtenerMuestraDetallePublicacion(this.valorVariableDTO
		                .getCodigoMuestra());
		LOG.info("listaPublicaciones: " + listaPublicaciones.size());

		for (int i = 0; i < listaPublicaciones.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaPublicaciones.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setPublicacionDTO(listaPublicaciones.get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    // this.listaMuestraDetalle.add(item);
			}
		    }
		}
		context.execute("dlgMuestraPublicaciones.show()");

	    } else

	    if (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
		    .getVariableGrupoDTO().getNemonico().equals("PUB")
		    && this.valorVariableDTO.getVariableProcesoDTO()
		            .getVariableDTO().getTablaMuestra()
		            .equals("producciones")) {

		// if (this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 142L
		// || this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 143L
		// || this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 144L) {

		// PRODUCCIONES

		listaProducciones = evaluacionServicio
		        .obtenerMuestraDetalleProducciones(this.valorVariableDTO
		                .getCodigoMuestra());

		LOG.info("listaProducciones: " + listaProducciones.size());

		for (int i = 0; i < listaProducciones.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaProducciones.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setProduccionDTO(listaProducciones.get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    // this.listaMuestraDetalle.add(item);
			    LOG.info("listaMuestraDetalle: "
				    + listaMuestraDetalle.size());
			}
		    }
		}
		context.execute("dlgMuestraProducciones.show()");

	    } else if (this.valorVariableDTO.getVariableProcesoDTO()
		    .getVariableDTO().getVariableGrupoDTO().getNemonico()
		    .equals("PUB")
		    && this.valorVariableDTO.getVariableProcesoDTO()
		            .getVariableDTO().getTablaMuestra()
		            .equals("proyectos")) {

		// else if (this.valorVariableDTO.getVariableProcesoDTO()
		// .getVariableDTO().getId() == 145L) {

		// PROYECTOS

		listaProyectos = evaluacionServicio
		        .obtenerMuestraDetalleProyectos(this.valorVariableDTO
		                .getCodigoMuestra());

		LOG.info("listaProyectos: " + listaProyectos.size());

		for (int i = 0; i < listaProyectos.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaProyectos.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setProyecto(listaProyectos.get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    // this.listaMuestraDetalle.add(item);
			    LOG.info("listaMuestraDetalle: "
				    + listaMuestraDetalle.size());
			}
		    }
		}
		context.execute("dlgMuestraProyectos.show()");

	    } else {// VARIABLES DE DOCENTES

		listaDocentes = evaluacionServicio
		        .obtenerMuestraDetallePersonas(this.valorVariableDTO
		                .getCodigoMuestra());

		LOG.info("listaDocentes: " + listaDocentes.size());

		for (int i = 0; i < listaDocentes.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaDocentes.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setDocente(listaDocentes.get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    // this.listaMuestraDetalle.add(item);
			    LOG.info("listaMuestraDetalle: "
				    + listaMuestraDetalle.size());
			}
		    }
		}

		if (VariableSubGrupoEnum.REMUNERACION.getValor().equals(
		        valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getSubGrupo())) {
		    // if (this.valorVariableDTO.getVariableProcesoDTO()
		    // .getVariableDTO().getId() == 19L
		    // || this.valorVariableDTO.getVariableProcesoDTO()
		    // .getVariableDTO().getId() == 127L) {
		    context.execute("dlgMuestraDocentesRem.show()");

		} else {
		    context.execute("dlgMuestraDocentes.show()");
		}
	    }
	} catch (ServicioException e1) {
	    e1.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {

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

    public void guardarValorVariable() {
	try {
	    int cont = 0;
	    for (int i = 0; i < this.listaVariablesCalidadDocenciaCuerpoDocente
		    .size(); i++) {

		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaVariablesCalidadDocenciaCuerpoDocente
		        .get(i);
		// vvdto.setValor("0.0");
		// vvdto.setValorVerificado(0.0);
		// vvdto.setRegistrosNoAceptados(0);
		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor().equals(
		                listaValorVariableOriginal.get(i).getValor()) || !vvdto
		                .getObservacion().equals(
		                        listaValorVariableOriginal.get(i)
		                                .getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    vvdto.setModificado(false);

		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariable(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		    cont++;

		}
	    }
	    this.listaVariablesCalidadDocenciaCuerpoDocente = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "cuerpo_docente");
	    if (cont > 0) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgInfo("No se ha realizado ningún cambio en las variables.");
	    }
	    cargarVariablesOriginales();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableFormacion() {
	try {
	    int cont = 0;
	    for (int i = 0; i < this.listaVariablesCalidadDocenciaFormacionDesarrollo
		    .size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaVariablesCalidadDocenciaFormacionDesarrollo
		        .get(i);
		// vvdto.setValor("0.0");
		// vvdto.setValorVerificado(0.0);
		// vvdto.setRegistrosNoAceptados(0);

		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor().equals(
		                listaValorVariableOriginalFormacion.get(i)
		                        .getValor()) || !vvdto.getObservacion()
		                .equals(listaValorVariableOriginalFormacion
		                        .get(i).getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    vvdto.setModificado(false);

		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariable(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		    cont++;
		}

	    }
	    this.listaVariablesCalidadDocenciaFormacionDesarrollo = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "formacion_desarrollo");
	    if (cont > 0) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgInfo("No se ha realizado ningún cambio en las variables.");
	    }
	    cargarVariablesOriginales();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableRemuneracion() {
	try {
	    int cont = 0;
	    for (int i = 0; i < this.listaVariablesCalidadDocenciaRemuneracion
		    .size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaVariablesCalidadDocenciaRemuneracion
		        .get(i);
		// vvdto.setValor("0.0");
		// vvdto.setValorVerificado(0.0);
		// vvdto.setRegistrosNoAceptados(0);

		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor().equals(
		                listaVariablesOriginalRemuneracion.get(i)
		                        .getValor()) || !vvdto.getObservacion()
		                .equals(listaVariablesOriginalRemuneracion.get(
		                        i).getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    vvdto.setModificado(false);

		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariable(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		    cont++;
		}

	    }
	    this.listaVariablesCalidadDocenciaRemuneracion = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "remuneracion");

	    if (cont > 0) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgInfo("No se ha realizado ningún cambio en las variables.");
	    }
	    cargarVariablesOriginales();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableDPC() {
	try {
	    int cont = 0;
	    for (int i = 0; i < this.listaVariablesCalidadDocenciaProduccionConocimiento
		    .size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaVariablesCalidadDocenciaProduccionConocimiento
		        .get(i);
		// vvdto.setValor("0.0");
		// vvdto.setValorVerificado(0.0);
		// vvdto.setRegistrosNoAceptados(0);

		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor()
		                .equals(listaValorVariableOriginalDPC.get(i)
		                        .getValor()) || !vvdto.getObservacion()
		                .equals(listaValorVariableOriginalDPC.get(i)
		                        .getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    vvdto.setModificado(false);

		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariable(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		    cont++;
		}

	    }
	    this.listaVariablesCalidadDocenciaProduccionConocimiento = evaluacionServicio
		    .obtenerInformacionVariables("PUB", informacionIes.getId(),
		            "%CUANTITATIVA%", "produccion_conocimiento");

	    if (cont > 0) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgInfo("No se ha realizado ningún cambio en las variables.");
	    }
	    cargarVariablesOriginales();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void tomarVariableEditada(ValorVariableDTO item) {
	LOG.info("=======>METODO tomarVariableEditada");
	this.listaValorVariablesModificados.add(item);

	LOG.info("TAMAÑO: " + listaValorVariablesModificados.size());
	for (int i = 0; i < this.listaValorVariablesModificados.size() - 1; i++) {
	    if (this.listaValorVariablesModificados.get(i).getId()
		    .equals(item.getId())) {
		this.listaValorVariablesModificados.remove(i);
	    }
	}
	LOG.info("listaValorVariablesModificados: "
	        + listaValorVariablesModificados.size());
    }

    public void guardarObservacionVariables() {
	LOG.info("====> METODO guardarObservacionVariables");
	LOG.info("listaValorVariablesModificados: "
	        + listaValorVariablesModificados.size());
	try {
	    for (ValorVariableDTO vvdto : this.listaValorVariablesModificados) {
		AuditoriaDTO adto = new AuditoriaDTO();
		adto.setFechaModificacion(new Date());
		adto.setUsuarioModificacion(this.usuario);
		vvdto.setAuditoriaDTO(adto);
		vvdto.setFaseIesDTO(this.faseIesDTO);
		vvdto.setIdInformacionIes(this.informacionIes.getId());
		vvdto.setModificado(false);

		LOG.info(new Date()
		        + "--> Registrando variable: "
		        + vvdto.getVariableProcesoDTO().getVariableDTO()
		                .getEtiqueta() + " id: " + vvdto.getId());
		LOG.info(new Date() + "--> Instituto: "
		        + informacionIes.getId());
		evaluacionServicio.registrarValorVariable(vvdto);
		LOG.info(new Date() + "--> VariableRegistrada: "
		        + vvdto.getId());
	    }
	    JsfUtil.msgInfo("Registros almacenados correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarMuestraDetalle() {
	if (busqueda) {
	    List<ItemEvaluacion> listaMuestraDetalleDocentesTemporal = new ArrayList<ItemEvaluacion>();
	    for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);
		if (null != vvdto.getMuestraDetalle().getObservaciones()
		        && !vvdto.getMuestraDetalle().getObservaciones()
		                .isEmpty()
		        && (!vvdto
		                .getMuestraDetalle()
		                .getVerificado()
		                .equals(listaMuestraDetalleDocentesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getVerificado()) || !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaMuestraDetalleDocentesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getObservaciones()))) {

		    if (vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (!vvdto.getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe seleccionar alguna opción del campo aceptado para: "
			        + vvdto.getDocente().getNombres()
			        + " "
			        + vvdto.getDocente().getApellidoPaterno()
			        + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;
		    }
		    if (!vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (null == vvdto.getMuestraDetalle()
			            .getObservaciones() || vvdto
			            .getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe ingresar la observación para: "
			        + vvdto.getDocente().getNombres() + " "
			        + vvdto.getDocente().getApellidoPaterno() + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;

		    }
		    if (vvdto
			    .getMuestraDetalle()
			    .getObservaciones()
			    .equals(listaMuestraDetalleDocentesOriginal.get(i)
			            .getMuestraDetalle().getObservaciones())) {
			JsfUtil.msgAdvert("Debe cambiar la observación de: "
			        + vvdto.getDocente().getNombres() + " "
			        + vvdto.getDocente().getApellidoPaterno() + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;
		    }
		}

		listaMuestraDetalleDocentesTemporal
		        .add((ItemEvaluacion) SerializationUtils.clone(vvdto));

	    }

	    cargarDocentes();
	    for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);
		for (int j = 0; j < listaMuestraDetalleDocentesTemporal.size(); j++) {
		    ItemEvaluacion vvdttemporal = listaMuestraDetalleDocentesTemporal
			    .get(j);
		    if (vvdto.getDocente().getId()
			    .equals(vvdttemporal.getDocente().getId())) {
			vvdto.getMuestraDetalle().setVerificado(
			        vvdttemporal.getMuestraDetalle()
			                .getVerificado());
			vvdto.getMuestraDetalle().setObservaciones(
			        vvdttemporal.getMuestraDetalle()
			                .getObservaciones());
		    }
		}
	    }

	}
	cambiosVariable = false;
	Integer modificados = 0;
	try {

	    for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);
		if (null != vvdto.getMuestraDetalle().getObservaciones()
		        && (!vvdto
		                .getMuestraDetalle()
		                .getVerificado()
		                .equals(listaMuestraDetalleDocentesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getVerificado()) || !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaMuestraDetalleDocentesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getObservaciones()))) {

		    if (vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (!vvdto.getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe seleccionar alguna opción del campo aceptado para: "
			        + vvdto.getDocente().getNombres()
			        + " "
			        + vvdto.getDocente().getApellidoPaterno()
			        + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;
		    }
		    if (!vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (null == vvdto.getMuestraDetalle()
			            .getObservaciones() || vvdto
			            .getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe ingresar la observación para: "
			        + vvdto.getDocente().getNombres() + " "
			        + vvdto.getDocente().getApellidoPaterno() + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;

		    }
		    if (vvdto
			    .getMuestraDetalle()
			    .getObservaciones()
			    .equals(listaMuestraDetalleDocentesOriginal.get(i)
			            .getMuestraDetalle().getObservaciones())) {
			JsfUtil.msgAdvert("Debe cambiar la observación de: "
			        + vvdto.getDocente().getNombres() + " "
			        + vvdto.getDocente().getApellidoPaterno() + " "
			        + vvdto.getDocente().getApellidoMaterno());
			return;
		    }
		} else {
		    modificados++;
		}
	    }
	    Double valor = 0.0;
	    Double todos = 0.0;
	    Double aceptado = 0.0;
	    Integer noAceptado = 0;
	    for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);

		if (vvdto.getMuestraDetalle().getVerificado().equals("1")) {

		    aceptado++;
		    valor += vvdto.getMuestraDetalle().getPonderacion();
		    todos += vvdto.getMuestraDetalle().getPonderacion();

		    vvdto.getMuestraDetalle().getAuditoria()
			    .setUsuarioModificacion(this.usuario);
		    vvdto.getMuestraDetalle().getAuditoria()
			    .setFechaModificacion(new Date());
		    vvdto.getMuestraDetalle().setFaseIesDTO(this.faseIesDTO);
		    evaluacionServicio.registrarMuestraDetalle(vvdto
			    .getMuestraDetalle());
		    cambiosVariable = true;

		} else {
		    if (vvdto.getMuestraDetalle().getVerificado().equals("0")
			    && null != valorVariableDTO.getValorVerificado()) {

			noAceptado++;
			todos += vvdto.getMuestraDetalle().getPonderacion();
			vvdto.getMuestraDetalle().getAuditoria()
			        .setUsuarioModificacion(this.usuario);
			vvdto.getMuestraDetalle().getAuditoria()
			        .setFechaModificacion(new Date());
			vvdto.getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			evaluacionServicio.registrarMuestraDetalle(vvdto
			        .getMuestraDetalle());
			cambiosVariable = true;

		    } else if (vvdto.getMuestraDetalle().getVerificado()
			    .equals("-2")
			    && null != valorVariableDTO.getValorVerificado()) {

			vvdto.getMuestraDetalle().getAuditoria()
			        .setUsuarioModificacion(this.usuario);
			vvdto.getMuestraDetalle().getAuditoria()
			        .setFechaModificacion(new Date());
			vvdto.getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			evaluacionServicio.registrarMuestraDetalle(vvdto
			        .getMuestraDetalle());
			cambiosVariable = true;

		    }
		}

	    }
	    valor = Math.rint(valor * 100) / 100;
	    todos = Math.rint(todos * 100) / 100;
	    Double valorfinal = valor;
	    // valorVariableDTO.getValorInicialPonderado()* valor / todos;
	    if (valorfinal.isNaN()) {
		valorfinal = 0.0;

	    }
	    valorVariableDTO.setValor(valorfinal.toString());
	    valorVariableDTO.setValorVerificado(aceptado);
	    valorVariableDTO.setRegistrosNoAceptados(noAceptado);
	    valorVariableDTO.setModificado(false);
	    evaluacionServicio.registrarValorVariable(valorVariableDTO);

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableDTO.getCodigoMuestra());
	    this.listaVariablesCalidadDocenciaCuerpoDocente = evaluacionServicio
		    .obtenerInformacionVariables("DOCGR",
		            informacionIes.getId(), "%CUANTITATIVA%",
		            "cuerpo_docente");

	    if (!valorVariableDTO.getTotalMuestra().equals(modificados)) {
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la Variable "
		        + valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getEtiqueta()
		        + ", corresponda a la modificación realizada en los criterios.");
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se ha realizado ningún cambio");
	    }
	    cargarMuestraVariableDetalleEstudiante(listaDocentes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalle();
	    cargarVariablesOriginales();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarMuestraDetalleRemuneracion() {

	try {
	    Double contador = 0.0;
	    Double valor = 0.0;
	    int noAceptados = 0;
	    boolean modificado = false;
	    for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);

		if ((vvdto.getMuestraDetalle().getObservaciones() != null)
		        && (!vvdto.getMuestraDetalle().getObservaciones()
		                .trim().equals(""))) {
		    if (!vvdto
			    .getMuestraDetalle()
			    .getObservaciones()
			    .equals(listaMuestraDetalleDocentesOriginal.get(i)
			            .getMuestraDetalle().getObservaciones())
			    || !vvdto
			            .getMuestraDetalle()
			            .getPonderacion()
			            .equals(listaMuestraDetalleDocentesOriginal
			                    .get(i).getMuestraDetalle()
			                    .getPonderacion())
			    || !vvdto
			            .getMuestraDetalle()
			            .getVerificado()
			            .equals(listaMuestraDetalleDocentesOriginal
			                    .get(i).getMuestraDetalle()
			                    .getVerificado())) {
			if (vvdto
			        .getMuestraDetalle()
			        .getObservaciones()
			        .equals(listaMuestraDetalleDocentesOriginal
			                .get(i).getMuestraDetalle()
			                .getObservaciones())) {
			    JsfUtil.msgAdvert("Debe cambiar la observación de: "
				    + vvdto.getDocente().getNombres()
				    + " "
				    + vvdto.getDocente().getApellidoPaterno()
				    + " "
				    + vvdto.getDocente().getApellidoMaterno());
			    return;
			}
			if (vvdto.getMuestraDetalle().getVerificado()
			        .equals("-1")) {
			    vvdto.getMuestraDetalle().setVerificado("1");
			}

			vvdto.getMuestraDetalle().getAuditoria()
			        .setUsuarioModificacion(this.usuario);
			vvdto.getMuestraDetalle().getAuditoria()
			        .setFechaModificacion(new Date());
			vvdto.getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			evaluacionServicio.registrarMuestraDetalle(vvdto
			        .getMuestraDetalle());
			modificado = true;
		    }
		} else {
		    JsfUtil.msgAdvert("Debe cambiar la observación de: "
			    + vvdto.getDocente().getNombres() + " "
			    + vvdto.getDocente().getApellidoPaterno() + " "
			    + vvdto.getDocente().getApellidoMaterno());
		    return;
		}

	    }

	    if (modificado == true) {

		for (int i = 0; i < this.listaMuestraDetalleDocentes.size(); i++) {
		    ItemEvaluacion vvdto = listaMuestraDetalleDocentes.get(i);
		    if ((vvdto.getMuestraDetalle().getObservaciones() != null)
			    && (!vvdto.getMuestraDetalle().getObservaciones()
			            .trim().equals(""))
			    && vvdto.getMuestraDetalle().getVerificado()
			            .equals("1")) {
			valor++;
			contador += vvdto.getMuestraDetalle().getPonderacion();
		    } else if (vvdto.getMuestraDetalle().getVerificado()
			    .equals("0")) {
			noAceptados++;
		    }
		}

		contador = Math.rint(contador * 100) / 100;
		valorVariableDTO.setValor(contador.toString());
		valorVariableDTO.setValorVerificado(valor);
		valorVariableDTO.setModificado(false);
		valorVariableDTO.setRegistrosNoAceptados(noAceptados);
		evaluacionServicio.registrarValorVariable(valorVariableDTO);
		listaMuestraDetalle = evaluacionServicio
		        .obtenerMuestraDetalle(valorVariableDTO
		                .getCodigoMuestra());
		this.listaVariablesCalidadDocenciaRemuneracion = evaluacionServicio
		        .obtenerInformacionVariables("DOCGR",
		                informacionIes.getId(), "%CUANTITATIVA%",
		                "remuneracion");
		JsfUtil.msgInfo("Registros almacenados correctamente");
		cargarMuestraVariableDetalleEstudiante(listaDocentes,
		        listaMuestraDetalle);
		cargarItemMuestraDetalle();
		cargarVariablesOriginales();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void registrarValoresMuestraDetalles() {

	LOG.info("METODO registrarValoresMuestraDetalles");
	LOG.info("listaItemsModificados: " + listaItemsModificados.size());
	int count = 0;
	try {
	    if (listaItemsModificados.size() != 0) {
		for (int i = 0; i < this.listaItemsModificados.size(); i++) {
		    LOG.info("TABLA: "
			    + listaItemsModificados.get(i).getMuestraDetalle()
			            .getTabla().toString());
		    LOG.info("TABLA VALOR: "
			    + listaItemsModificados.get(i).getMuestraDetalle()
			            .getTabla().getValor());
		    if (listaItemsModificados.get(i).getMuestraDetalle()
			    .getObservaciones() == null
			    || listaItemsModificados.get(i).getMuestraDetalle()
			            .getObservaciones().trim().equals("")) {
			count = -1;
			JsfUtil.msgError("Los datos NO han sido guardados. Debe ingresar una observación");
			break;
		    }

		    count++;
		    listaItemsModificados.get(i).getMuestraDetalle()
			    .getAuditoria()
			    .setUsuarioModificacion(this.usuario);
		    listaItemsModificados.get(i).getMuestraDetalle()
			    .setFaseIesDTO(this.faseIesDTO);
		    evaluacionServicio
			    .registrarMuestraDetalle(listaItemsModificados.get(
			            i).getMuestraDetalle());

		}
		if (count == listaItemsModificados.size()) {
		    LOG.info("IF COUNT");
		    guardarValorVariables();
		    JsfUtil.msgInfo("Los datos han sido guardados con éxito");
		}
		this.listaItemsModificados.clear();
	    } else {
		JsfUtil.msgAdvert("NO ha realizado cambios");
	    }
	    // if (seccion.equals("DOCENTES")) {
	    // listaVariablesDocentes.clear();
	    // cargarVariablesGlobales(listaValorVariableDocentes,
	    // listaVariablesDocentes);
	    // } else if (seccion.equals("ESTUDIANTES")) {
	    // listaVariablesEstudiante.clear();
	    // cargarVariablesGlobales(listaValorVariableEstudiantes,
	    // listaVariablesEstudiante);
	    // } else if (seccion.equals("VINCULACION")) {
	    // listaVariablesVinculacion.clear();
	    // cargarVariablesGlobales(listaValorVariableVinculacion,
	    // listaVariablesVinculacion);
	    // } else if (seccion.equals("PUBLICACIONES")) {
	    // listaVariablesPublicaciones.clear();
	    // cargarVariablesGlobales(listaValorVariablePublicaciones,
	    // listaVariablesPublicaciones);
	    // }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void guardarValorVariables() {

	try {

	    String totalValorAlto = "";
	    String totalValorMedio = "";
	    String totalValorBajo = "";
	    if ("Perfiles Egreso".equals(this.valorVariableDTO
		    .getVariableProcesoDTO().getVariableDTO().getNombre())
		    || "Perfiles Consultados".equals(this.valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO()
		            .getNombre())
		    || "Plan Curricular".equals(this.valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO().getId())) {
		LOG.info("this.valorVariableDTO.getCodigoMuestra(): "
		        + this.valorVariableDTO.getCodigoMuestra());
		totalValorAlto = evaluacionServicio.obtenerValoresMuestra(
		        "ALTO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();
		totalValorMedio = evaluacionServicio.obtenerValoresMuestra(
		        "MEDIO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();
		totalValorBajo = evaluacionServicio.obtenerValoresMuestra(
		        "BAJO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();

		LOG.info("totalValorAlto: " + totalValorAlto);
		LOG.info("totalValorMedio: " + totalValorMedio);
		LOG.info("totalValorBajo: " + totalValorBajo);

		ValorVariableDTO valorVariableAlto = new ValorVariableDTO();
		ValorVariableDTO valorVariableMedio = new ValorVariableDTO();
		ValorVariableDTO valorVariableBajo = new ValorVariableDTO();

		AuditoriaDTO a = new AuditoriaDTO();
		a.setFechaModificacion(new Date());
		a.setUsuarioModificacion(usuario);

		valorVariableAlto.setAuditoriaDTO(a);
		valorVariableAlto.setFaseIesDTO(this.faseIesDTO);
		valorVariableAlto.setIdInformacionIes(this.informacionIes
		        .getId());
		valorVariableAlto.setValor(totalValorAlto);

		valorVariableMedio.setAuditoriaDTO(a);
		valorVariableMedio.setFaseIesDTO(this.faseIesDTO);
		valorVariableMedio.setIdInformacionIes(this.informacionIes
		        .getId());
		valorVariableMedio.setValor(totalValorMedio);

		valorVariableBajo.setAuditoriaDTO(a);
		valorVariableBajo.setFaseIesDTO(this.faseIesDTO);
		valorVariableBajo.setIdInformacionIes(this.informacionIes
		        .getId());
		valorVariableBajo.setValor(totalValorBajo);

		if ("Perfiles Egreso".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(5L);
		    valorVariableMedio.getVariableProcesoDTO().setId(6L);
		    valorVariableBajo.getVariableProcesoDTO().setId(7L);
		}

		if ("Perfiles Consultados".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(8L);
		    valorVariableMedio.getVariableProcesoDTO().setId(9L);
		    valorVariableBajo.getVariableProcesoDTO().setId(10L);
		}

		if ("Plan Curricular".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(11L);
		    valorVariableMedio.getVariableProcesoDTO().setId(12L);
		    valorVariableBajo.getVariableProcesoDTO().setId(13L);
		}
		evaluacionServicio.registrarValorVariable(valorVariableAlto);
		evaluacionServicio.registrarValorVariable(valorVariableMedio);
		evaluacionServicio.registrarValorVariable(valorVariableBajo);
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public ValorVariableDTO getValorVariableDTO() {
	return valorVariableDTO;
    }

    public void setValorVariableDTO(ValorVariableDTO valorVariableDTO) {
	this.valorVariableDTO = valorVariableDTO;
    }

    public Boolean getAlertaIngresoVerificado() {
	return alertaIngresoVerificado;
    }

    public void setAlertaIngresoVerificado(Boolean alertaIngresoVerificado) {
	this.alertaIngresoVerificado = alertaIngresoVerificado;
    }

    public List<ValorVariableDTO> getListaVariablesCalidadDocenciaCuerpoDocente() {
	return listaVariablesCalidadDocenciaCuerpoDocente;
    }

    public void setListaVariablesCalidadDocenciaCuerpoDocente(
	    List<ValorVariableDTO> listaVariablesCalidadDocenciaCuerpoDocente) {
	this.listaVariablesCalidadDocenciaCuerpoDocente = listaVariablesCalidadDocenciaCuerpoDocente;
    }

    public List<ValorVariableDTO> getListaVariablesCalidadDocenciaRemuneracion() {
	return listaVariablesCalidadDocenciaRemuneracion;
    }

    public void setListaVariablesCalidadDocenciaRemuneracion(
	    List<ValorVariableDTO> listaVariablesCalidadDocenciaRemuneracion) {
	this.listaVariablesCalidadDocenciaRemuneracion = listaVariablesCalidadDocenciaRemuneracion;
    }

    public List<ValorVariableDTO> getListaVariablesCalidadDocenciaFormacionDesarrollo() {
	return listaVariablesCalidadDocenciaFormacionDesarrollo;
    }

    public void setListaVariablesCalidadDocenciaFormacionDesarrollo(
	    List<ValorVariableDTO> listaVariablesCalidadDocenciaFormacionDesarrollo) {
	this.listaVariablesCalidadDocenciaFormacionDesarrollo = listaVariablesCalidadDocenciaFormacionDesarrollo;
    }

    public List<ValorVariableDTO> getListaVariablesCalidadDocenciaProduccionConocimiento() {
	return listaVariablesCalidadDocenciaProduccionConocimiento;
    }

    public void setListaVariablesCalidadDocenciaProduccionConocimiento(
	    List<ValorVariableDTO> listaVariablesCalidadDocenciaProduccionConocimiento) {
	this.listaVariablesCalidadDocenciaProduccionConocimiento = listaVariablesCalidadDocenciaProduccionConocimiento;
    }

    public List<PersonaDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<PersonaDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
    }

    public List<MuestraDetalleDTO> getListaMuestraDetalle() {
	return listaMuestraDetalle;
    }

    public void setListaMuestraDetalle(
	    List<MuestraDetalleDTO> listaMuestraDetalle) {
	this.listaMuestraDetalle = listaMuestraDetalle;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleDocentes() {
	return listaMuestraDetalleDocentes;
    }

    public void setListaMuestraDetalleDocentes(
	    List<ItemEvaluacion> listaMuestraDetalleDocentes) {
	this.listaMuestraDetalleDocentes = listaMuestraDetalleDocentes;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleDocentesOriginal() {
	return listaMuestraDetalleDocentesOriginal;
    }

    public void setListaMuestraDetalleDocentesOriginal(
	    List<ItemEvaluacion> listaMuestraDetalleDocentesOriginal) {
	this.listaMuestraDetalleDocentesOriginal = listaMuestraDetalleDocentesOriginal;
    }

    public Boolean getEvidencias() {
	return evidencias;
    }

    public void setEvidencias(Boolean evidencias) {
	this.evidencias = evidencias;
    }

    public Boolean getContratos() {
	return contratos;
    }

    public void setContratos(Boolean contratos) {
	this.contratos = contratos;
    }

    public Boolean getCapacitacion() {
	return capacitacion;
    }

    public void setCapacitacion(Boolean capacitacion) {
	this.capacitacion = capacitacion;
    }

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
    }

    public String getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(String idProceso) {
	this.idProceso = idProceso;
    }

    public Boolean getExperiencia() {
	return experiencia;
    }

    public void setExperiencia(Boolean experiencia) {
	this.experiencia = experiencia;
    }

    public Boolean getFormacion() {
	return formacion;
    }

    public void setFormacion(Boolean formacion) {
	this.formacion = formacion;
    }

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public List<ContratacionDTO> getListaContratacion() {
	return listaContratacion;
    }

    public void setListaContratacion(List<ContratacionDTO> listaContratacion) {
	this.listaContratacion = listaContratacion;
    }

    public ItemEvaluacion getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    public void setDocenteSeleccionado(ItemEvaluacion docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
    }

    public IesDTO getIes() {
	return ies;
    }

    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    public Boolean getCargos() {
	return cargos;
    }

    public void setCargos(Boolean cargos) {
	this.cargos = cargos;
    }

    public List<ExperienciaProfesionalDTO> getListaExperienciaProfesional() {
	return listaExperienciaProfesional;
    }

    public void setListaExperienciaProfesional(
	    List<ExperienciaProfesionalDTO> listaExperienciaProfesional) {
	this.listaExperienciaProfesional = listaExperienciaProfesional;
    }

    public List<ItemEvaluacion> getListaMuestraDetalle1() {
	return listaMuestraDetalle1;
    }

    public void setListaMuestraDetalle1(
	    List<ItemEvaluacion> listaMuestraDetalle1) {
	this.listaMuestraDetalle1 = listaMuestraDetalle1;
    }

    public List<FormacionProfesionalDTO> getListaFormacion() {
	return listaFormacion;
    }

    public void setListaFormacion(List<FormacionProfesionalDTO> listaFormacion) {
	this.listaFormacion = listaFormacion;
    }

    public List<CargoAcademicoDTO> getListaCargosAcademicos() {
	return listaCargosAcademicos;
    }

    public void setListaCargosAcademicos(
	    List<CargoAcademicoDTO> listaCargosAcademicos) {
	this.listaCargosAcademicos = listaCargosAcademicos;
    }

    public List<ItemEvaluacion> getListaItemsModificados() {
	return listaItemsModificados;
    }

    public void setListaItemsModificados(
	    List<ItemEvaluacion> listaItemsModificados) {
	this.listaItemsModificados = listaItemsModificados;
    }

    public List<CursoCapacitacionDTO> getListaCapacitacion() {
	return listaCapacitacion;
    }

    public void setListaCapacitacion(
	    List<CursoCapacitacionDTO> listaCapacitacion) {
	this.listaCapacitacion = listaCapacitacion;
    }

    public List<ValorVariableDTO> getListaValorVariablesModificados() {
	return listaValorVariablesModificados;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public FormacionProfesionalDTO getFormacionSeleccionada() {
	return formacionSeleccionada;
    }

    public void setFormacionSeleccionada(
	    FormacionProfesionalDTO formacionSeleccionada) {
	this.formacionSeleccionada = formacionSeleccionada;
    }

    public ContratacionDTO getContratoSeleccionado() {
	return contratoSeleccionado;
    }

    public void setContratoSeleccionado(ContratacionDTO contratoSeleccionado) {
	this.contratoSeleccionado = contratoSeleccionado;
    }

    public ExperienciaProfesionalDTO getExperienciaSeleccionada() {
	return experienciaSeleccionada;
    }

    public void setExperienciaSeleccionada(
	    ExperienciaProfesionalDTO experienciaSeleccionada) {
	this.experienciaSeleccionada = experienciaSeleccionada;
    }

    public CursoCapacitacionDTO getCapacitacionSeleccionada() {
	return capacitacionSeleccionada;
    }

    public void setCapacitacionSeleccionada(
	    CursoCapacitacionDTO capacitacionSeleccionada) {
	this.capacitacionSeleccionada = capacitacionSeleccionada;
    }

    public CargoAcademicoDTO getCargoAcademicaSeleccionado() {
	return cargoAcademicaSeleccionado;
    }

    public void setCargoAcademicaSeleccionado(
	    CargoAcademicoDTO cargoAcademicaSeleccionado) {
	this.cargoAcademicaSeleccionado = cargoAcademicaSeleccionado;
    }

    public String getNombreFichero() {
	return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
	this.nombreFichero = nombreFichero;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
    }

    public List<ValorVariableDTO> getListaValorVariableOriginal() {
	return listaValorVariableOriginal;
    }

    public void setListaValorVariableOriginal(
	    List<ValorVariableDTO> listaValorVariableOriginal) {
	this.listaValorVariableOriginal = listaValorVariableOriginal;
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

    public List<ValorVariableDTO> getListaValorVariableOriginalFormacion() {
	return listaValorVariableOriginalFormacion;
    }

    public void setListaValorVariableOriginalFormacion(
	    List<ValorVariableDTO> listaValorVariableOriginalFormacion) {
	this.listaValorVariableOriginalFormacion = listaValorVariableOriginalFormacion;
    }

    public List<DocenteAsignaturaDTO> getListaDistributivo() {
	return listaDistributivo;
    }

    public void setListaDistributivo(
	    List<DocenteAsignaturaDTO> listaDistributivo) {
	this.listaDistributivo = listaDistributivo;
    }

    public List<ValorVariableDTO> getListaVariablesOriginalRemuneracion() {
	return listaVariablesOriginalRemuneracion;
    }

    public void setListaVariablesOriginalRemuneracion(
	    List<ValorVariableDTO> listaVariablesOriginalRemuneracion) {
	this.listaVariablesOriginalRemuneracion = listaVariablesOriginalRemuneracion;
    }

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public ParametroDTO getpDTO() {
	return pDTO;
    }

    public void setpDTO(ParametroDTO pDTO) {
	this.pDTO = pDTO;
    }

    public ParametroDTO getPeDTO() {
	return peDTO;
    }

    public void setPeDTO(ParametroDTO peDTO) {
	this.peDTO = peDTO;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
    }

    public UploadedFile getFile() {
	return file;
    }

    public void setFile(UploadedFile file) {
	this.file = file;
    }

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public List<ValorVariableDTO> getListaValorVariableOriginalDPC() {
	return listaValorVariableOriginalDPC;
    }

    public void setListaValorVariableOriginalDPC(
	    List<ValorVariableDTO> listaValorVariableOriginalDPC) {
	this.listaValorVariableOriginalDPC = listaValorVariableOriginalDPC;
    }

    public Boolean getCambiosVariable() {
	return cambiosVariable;
    }

    public void setCambiosVariable(Boolean cambiosVariable) {
	this.cambiosVariable = cambiosVariable;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleDocentesfilter() {
	return listaMuestraDetalleDocentesfilter;
    }

    public void setListaMuestraDetalleDocentesfilter(
	    List<ItemEvaluacion> listaMuestraDetalleDocentesfilter) {
	this.listaMuestraDetalleDocentesfilter = listaMuestraDetalleDocentesfilter;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public List<PublicacionDTO> getListaPublicaciones() {
	return listaPublicaciones;
    }

    public void setListaPublicaciones(List<PublicacionDTO> listaPublicaciones) {
	this.listaPublicaciones = listaPublicaciones;
    }

    public Boolean getBusqueda() {
	return busqueda;
    }

    public void setBusqueda(Boolean busqueda) {
	this.busqueda = busqueda;
    }

    public void setListaValorVariablesModificados(
	    List<ValorVariableDTO> listaValorVariablesModificados) {
	this.listaValorVariablesModificados = listaValorVariablesModificados;
    }

    public ItemEvaluacion getItemnSeleccionado() {
	return itemnSeleccionado;
    }

    public void setItemnSeleccionado(ItemEvaluacion itemnSeleccionado) {
	this.itemnSeleccionado = itemnSeleccionado;
    }

}
