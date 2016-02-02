package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoVariableEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoVariableEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.TituloSenescyt;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudiantePeriodoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvidenciaEstudianteController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaEstudianteController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    private List<VariableProcesoDTO> listaVariablesEvidencia = new ArrayList<VariableProcesoDTO>();;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
    private List<CarreraEstudianteDTO> listaCarrerasEstudiante = new ArrayList<CarreraEstudianteDTO>();
    private List<CarreraEstudiantePeriodoDTO> listaMatriculasEstudiante = new ArrayList<CarreraEstudiantePeriodoDTO>();
    private List<ConceptoDTO> conceptosEstudiante = new ArrayList<ConceptoDTO>();
    private PersonaDTO estudianteSeleccionado = new PersonaDTO();
    private CarreraEstudianteDTO carreraSeleccionada = new CarreraEstudianteDTO();
    private CarreraEstudiantePeriodoDTO periodoSeleccionado = new CarreraEstudiantePeriodoDTO();
    private List<PersonaDTO> listaEstudiantes = new ArrayList<PersonaDTO>();
    private FaseIesDTO faseiesDTO = new FaseIesDTO();;
    private IesDTO iesDTO = new IesDTO();;
    private EvidenciaDTO evidenciaDto = new EvidenciaDTO();;
    private EvidenciaDTO evidenciaSeleccionada = new EvidenciaDTO();;
    private StreamedContent documentoDescarga;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();;
    private ParametroDTO pDTO = new ParametroDTO();;
    private ParametroDTO peDTO = new ParametroDTO();;
    private Long idTablaSeleccionada;
    private OrigenInformacionEnum tabla;
    private UploadedFile file;
    private String fichero;
    private String usuario;
    private VariableProcesoDTO variableSeleccionada = new VariableProcesoDTO();

    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private Boolean evidenciaConcepto = false;
    private Boolean mostrarMatriculas = false;
    private Boolean mostrarCarreras = false;
    private ConceptoDTO conceptoSeleccionado = new ConceptoDTO();
    private InformacionIesDTO informacionIesDTO = new InformacionIesDTO();
    private List<TituloSenescyt> titulosSenescyt = new ArrayList<TituloSenescyt>();
    private int numeroGraduadosValidados;
    private List<ValorVariableDTO> valorVariableDTO = new ArrayList<ValorVariableDTO>();
    private boolean verActualizarMuestra;
    private boolean verActualizarGraduados;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionada;

    @PostConstruct
    public void start() {

	try {

	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");
	    faseiesDTO = controller.getFaseIesDTO();
	    LOG.info("faseIesDTO: " + faseiesDTO.getId());
	    iesDTO = controller.getIes();
	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    usuario = controller.getUsuario();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    // FASE DE RECTIFICACIÓN SE ELIMINA LA CARGA DE EVIDENCIAS DE
	    // ESTUDIANTES
	    // listaVariablesEvidencia = catalogoServicio
	    // .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
	    // .getProcesoDTO().getId(),
	    // GrupoVariableEnum.ESTUDIANTES.getValue(),
	    // TipoVariableEnum.CUANTITATIVA.getValue());
	    LOG.info("VARIABLES ESTUDIANTES: ------>>"
		    + listaVariablesEvidencia.size());
	    listaVariablesEvidencia = catalogoServicio
		    .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
		            .getProcesoDTO().getId(),
		            GrupoVariableEnum.GRADUADOS.getValue(),
		            TipoVariableEnum.CUANTITATIVA.getValue());
	    LOG.info("VARIABLES GRADUADOS: ------>>"
		    + listaVariablesEvidencia.size());
	    conceptosEstudiante = catalogoServicio
		    .obtenerConceptosPorGrupo("ESTUDIANTE");
	    verActualizarMuestra = false;
	    cargarEstudiantes();

	} catch (ServicioException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void cargarEstudiantes() {
	numeroGraduadosValidados = 0;
	ValorVariableDTO vvDTO;
	VariableProcesoDTO varProceso = new VariableProcesoDTO();
	List<PersonaDTO> estudiantesAux = new ArrayList<PersonaDTO>();
	listaEstudiantes.clear();
	valorVariableDTO.clear();
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	varProceso.setId(listaVariablesEvidencia.get(0).getId());
	try {
	    for (VariableProcesoDTO vp : listaVariablesEvidencia) {
		if (vp.getId().equals(varProceso.getId())) {
		    varProceso = vp;
		    variableSeleccionada = vp;
		    break;
		}
	    }
	    vvDTO = evaluacionServicio.obtenerUltimoValorVariable(varProceso
		    .getVariableDTO().getId(), null, informacionIesDTO.getId());

	    if (vvDTO == null) {
		if (varProceso.getVariablePadreDTO() != null
		        && !GrupoVariableEnum.GRADUADOS.getValue().equals(
		                varProceso.getVariableDTO()
		                        .getVariableGrupoDTO().getNemonico())) {

		    List<VariableProcesoDTO> variablesHijas = catalogoServicio
			    .obtenerVariablesHijas(varProceso
			            .getVariablePadreDTO().getId());
		    evaluacionServicio
			    .obtenerValorVariableMuestraEstratificada(
			            varProceso.getVariablePadreDTO(),
			            variablesHijas, informacionIesDTO.getId(),
			            auditoriaDTO, faseiesDTO);
		    vvDTO = evaluacionServicio.obtenerUltimoValorVariable(
			    varProceso.getVariableDTO().getId(), null,
			    informacionIesDTO.getId());

		} else {
		    vvDTO = evaluacionServicio.calcularValorVariableYMuestra(
			    varProceso, informacionIesDTO.getId(), faseiesDTO,
			    auditoriaDTO);

		}
		verActualizarMuestra = true;
		verActualizarGraduados = false;
	    } else {
		if (!GrupoVariableEnum.GRADUADOS.getValue().equals(
		        varProceso.getVariableDTO().getVariableGrupoDTO()
		                .getNemonico())) {
		    verActualizarMuestra = true;
		    verActualizarGraduados = false;
		} else {
		    verActualizarMuestra = false;
		    verActualizarGraduados = true;
		}
	    }

	    estudiantesAux = evaluacionServicio
		    .obtenerMuestraDetallePersonas(vvDTO.getCodigoMuestra());
	    valorVariableDTO.add(vvDTO);

	    listaEstudiantes.addAll(estudiantesAux);
	    evidenciaConcepto = false;
	    estudianteSeleccionado = new EstudianteDTO();
	    listaCarrerasEstudiante.clear();
	    listaMatriculasEstudiante.clear();
	    mostrarCarreras = false;
	    mostrarMatriculas = false;
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el administrador");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el administrador");
	}
    }

    public void actualizarMuestra() {
	List<PersonaDTO> estudiantesAux = new ArrayList<PersonaDTO>();
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	ValorVariableDTO vvDto = new ValorVariableDTO();
	listaEstudiantes.clear();
	try {
	    if (variableSeleccionada.getVariablePadreDTO() != null
		    && !GrupoVariableEnum.GRADUADOS.getValue().equals(
		            variableSeleccionada.getVariableDTO()
		                    .getVariableGrupoDTO().getNemonico())) {
		List<VariableProcesoDTO> variablesHijas = catalogoServicio
		        .obtenerVariablesHijas(variableSeleccionada
		                .getVariablePadreDTO().getId());
		evaluacionServicio.obtenerValorVariableMuestraEstratificada(
		        variableSeleccionada.getVariablePadreDTO(),
		        variablesHijas, informacionIesDTO.getId(),
		        auditoriaDTO, faseiesDTO);
		vvDto = evaluacionServicio.obtenerUltimoValorVariable(
		        variableSeleccionada.getVariableDTO().getId(), null,
		        informacionIesDTO.getId());
		verActualizarMuestra = true;
		verActualizarGraduados = false;

	    }
	    estudiantesAux = evaluacionServicio
		    .obtenerMuestraDetallePersonas(vvDto.getCodigoMuestra());
	    listaEstudiantes.addAll(estudiantesAux);
	    JsfUtil.msgInfo("Procedimiento realizado exitósamente.");
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error inesperado, comuníquese con el administrador.");
	}

    }

    /* Actualiza valor de la variable Graduados */
    public void actualizarValor() {
	List<PersonaDTO> estudiantesAux = new ArrayList<PersonaDTO>();
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	ValorVariableDTO vvDto = new ValorVariableDTO();
	listaEstudiantes.clear();
	try {
	    if (GrupoVariableEnum.GRADUADOS.getValue().equals(
		    variableSeleccionada.getVariableDTO().getVariableGrupoDTO()
		            .getNemonico())) {

		vvDto = evaluacionServicio.obtenerUltimoValorVariable(
		        variableSeleccionada.getVariableDTO().getId(), null,
		        informacionIesDTO.getId());
		if (vvDto != null) {
		    evaluacionServicio.obtenerValorVariable(vvDto, faseiesDTO,
			    auditoriaDTO, vvDto.getVariableProcesoDTO()
			            .getSqlListaId());
		} else {
		    evaluacionServicio.calcularValorVariableYMuestra(
			    variableSeleccionada, informacionIesDTO.getId(),
			    faseiesDTO, auditoriaDTO);
		}
		verActualizarMuestra = false;
		verActualizarGraduados = true;

	    }
	    estudiantesAux = evaluacionServicio
		    .obtenerMuestraDetallePersonas(vvDto.getCodigoMuestra());
	    listaEstudiantes.addAll(estudiantesAux);
	    JsfUtil.msgInfo("Procedimiento realizado exitósamente.");
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error inesperado, comuníquese con el administrador.");
	}

    }

    public void cargarCarrerasEstudiante() {
	try {
	    listaCarrerasEstudiante = registroServicio
		    .obtenerCarrerasEstudiante(estudianteSeleccionado.getId());
	    listaMatriculasEstudiante.clear();

	    for (VariableProcesoDTO vp : listaVariablesEvidencia) {
		if (vp.getId().equals(variableSeleccionada.getId())) {
		    variableSeleccionada = vp;
		    break;
		}
	    }

	    for (CarreraEstudianteDTO carEst : listaCarrerasEstudiante) {
		List<CarreraEstudiantePeriodoDTO> periodos = registroServicio
		        .obtenerPeriodosPorCarreraEstudiante(carEst.getId());
		if (periodos != null) {
		    listaMatriculasEstudiante.addAll(periodos);
		}
	    }
	    if (variableSeleccionada.getVariableDTO().getVariableGrupoDTO()
		    .getNemonico().equals("GRAD")) {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo("carreras_estudiante",
		                "ESTUDIANTE");
		tabla = OrigenInformacionEnum.CARRERA_ESTUDIANTE;
		mostrarMatriculas = false;
		mostrarCarreras = true;
	    } else {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                "carrera_estudiante_periodos", "ESTUDIANTE");
		tabla = OrigenInformacionEnum.CARRERA_ESTUDIANTE_PERIODO;
		mostrarMatriculas = true;
		mostrarCarreras = false;
	    }
	    carreraSeleccionada = new CarreraEstudianteDTO();
	    evidenciaConcepto = false;
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el administrador");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el administrador");
	}
    }

    public void cargarEvidencias() {

	evidenciaConcepto = true;
	if (conceptoSeleccionado.getCodigo().equals("ESTU")) {
	    tabla = OrigenInformacionEnum.CARRERA_ESTUDIANTE_PERIODO;
	    idTablaSeleccionada = periodoSeleccionado.getId();
	} else {
	    tabla = OrigenInformacionEnum.CARRERA_ESTUDIANTE;
	    idTablaSeleccionada = carreraSeleccionada.getId();
	}
	try {
	    if (idTablaSeleccionada != null) {
		listaEvidenciaConcepto = institutosServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                iesDTO.getId(), conceptoSeleccionado.getId(),
		                idTablaSeleccionada, tabla.getValor());
	    } else {
		evidenciaConcepto = false;
	    }

	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error al obtener evidencias. Comuníquese con el administrador.");
	    e.printStackTrace();
	}

    }

    public void cambioBanderaNegativo() {
	if (conceptoSeleccionado.getCodigo().equals("ESTU")) {
	    periodoSeleccionado.setVerificarEvidencia(false);
	    try {
		registroServicio
		        .registrarCarreraEstudiantePeriodo(periodoSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {
	    carreraSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarMatricula(carreraSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }

    public void verificarEvidencia() {
	Boolean fase = false;
	for (EvidenciaConceptoDTO evidenciaConceptoDTO : listaEvidenciaConcepto) {

	    if (evidenciaConceptoDTO.getId().equals(
		    evidenciaConceptoSeleccionada.getId())) {

		if (evidenciaConceptoDTO.getEvidenciasDTO().isEmpty()) {
		    cambioBanderaNegativo();
		}

		for (EvidenciaDTO evidencia : evidenciaConceptoDTO
		        .getEvidenciasDTO()) {

		    if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
			    .equals(faseiesDTO.getFaseProcesoDTO().getId())) {

			if (conceptoSeleccionado.getCodigo().equals("ESTU")) {
			    periodoSeleccionado.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarCarreraEstudiantePeriodo(periodoSeleccionado);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			} else {
			    carreraSeleccionada.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarMatricula(carreraSeleccionada);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}
			fase = true;
			break;
		    } else {
			cambioBanderaNegativo();
		    }
		    if (fase) {
			break;
		    }
		}
		if (fase) {
		    break;
		}
	    }
	}
    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idTablaSeleccionada + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idTablaSeleccionada + "/";
	LOG.info("DESTINO: " + destino);

	if (ArchivoUtil.crearDirectorio(urlDestino)) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(this.usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);
	    } catch (ServicioException e) {
		e.printStackTrace();
	    }
	    cargarEvidencias();
	    verificarEvidencia();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(listaEvidenciaConcepto.get(0));
	    evidenciaDto.setIdTabla(idTablaSeleccionada);
	    evidenciaDto.setFaseIesDTO(this.faseiesDTO);

	    evidenciaDto.setTabla(tabla.getValor());

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

		String nombreGenerado = iesDTO.getCodigo() + "_"
		        + idTablaSeleccionada + "_COD_"
		        + listaEvidenciaConcepto.get(0).getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + faseiesDTO.getId() + "/"
		        + iesDTO.getCodigo() + "/" + idTablaSeleccionada + "/";

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

			auditoria.setUsuarioModificacion(this.usuario);
			evidenciaDto.setAuditoriaDTO(auditoria);

			institutosServicio.crearActualizar(evidenciaDto);

			if (conceptoSeleccionado.getCodigo().equals("ESTU")) {
			    periodoSeleccionado.setVerificarEvidencia(true);
			    registroServicio
				    .registrarCarreraEstudiantePeriodo(periodoSeleccionado);
			} else {
			    carreraSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarMatricula(carreraSeleccionada);

			}

			evidenciaDto = new EvidenciaDTO();
			cargarEvidencias();
			JsfUtil.msgInfo("Evidencia subida correctamente");
		    }
		} catch (Exception e) {

		    e.printStackTrace();
		}
	    }
	} else {
	    JsfUtil.msgError("Debe escoger un documento");
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

    public void cargarTitulosSenescyt(String cedula) {
	WebServiceController wsController = (WebServiceController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("webServiceController");

	wsController.cargarIdentificacion(cedula);
	wsController.consultaSenescyt();
	List<TituloSenescyt> aux = new ArrayList<TituloSenescyt>();
	aux = wsController.getTitulosReconocidos();
	for (int i = 0; i < aux.size(); i++) {

	    if (aux.get(i).getNivel().equalsIgnoreCase("TECNICO/TECNOLOGO")
		    && this.iesDTO.getNombre().equals(
		            aux.get(i).getInstitucion())) {
		titulosSenescyt.add(aux.get(i));
	    }

	}

    }

    public List<VariableProcesoDTO> getListaVariablesEvidencia() {
	return listaVariablesEvidencia;
    }

    public void setListaVariablesEvidencia(
	    List<VariableProcesoDTO> listaVariablesEvidencia) {
	this.listaVariablesEvidencia = listaVariablesEvidencia;
    }

    public FaseIesDTO getFaseiesDTO() {
	return faseiesDTO;
    }

    public void setFaseiesDTO(FaseIesDTO faseiesDTO) {
	this.faseiesDTO = faseiesDTO;
    }

    public Long getIdTablaSeleccionada() {
	return idTablaSeleccionada;
    }

    public void setIdTablaSeleccionada(Long idTablaSeleccionada) {
	this.idTablaSeleccionada = idTablaSeleccionada;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
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

    public InstitutosServicio getInstitutosServicio() {
	return institutosServicio;
    }

    public void setInstitutosServicio(InstitutosServicio institutosServicio) {
	this.institutosServicio = institutosServicio;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
    }

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
    }

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
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

    public UploadedFile getFile() {
	return file;
    }

    public void setFile(UploadedFile file) {
	this.file = file;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
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

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public VariableProcesoDTO getVariableSeleccionada() {
	return variableSeleccionada;
    }

    public void setVariableSeleccionada(VariableProcesoDTO variableSeleccionada) {
	this.variableSeleccionada = variableSeleccionada;
    }

    public List<PersonaDTO> getListaEstudiantes() {
	return listaEstudiantes;
    }

    public void setListaEstudiantes(List<PersonaDTO> listaEstudiantes) {
	this.listaEstudiantes = listaEstudiantes;
    }

    public PersonaDTO getEstudianteSeleccionado() {
	return estudianteSeleccionado;
    }

    public void setEstudianteSeleccionado(PersonaDTO estudianteSeleccionado) {
	this.estudianteSeleccionado = estudianteSeleccionado;
    }

    public List<CarreraEstudianteDTO> getListaCarrerasEstudiante() {
	return listaCarrerasEstudiante;
    }

    public void setListaCarrerasEstudiante(
	    List<CarreraEstudianteDTO> listaCarrerasEstudiante) {
	this.listaCarrerasEstudiante = listaCarrerasEstudiante;
    }

    public List<CarreraEstudiantePeriodoDTO> getListaMatriculasEstudiante() {
	return listaMatriculasEstudiante;
    }

    public void setListaMatriculasEstudiante(
	    List<CarreraEstudiantePeriodoDTO> listaMatriculasEstudiante) {
	this.listaMatriculasEstudiante = listaMatriculasEstudiante;
    }

    public List<ConceptoDTO> getConceptosEstudiante() {
	return conceptosEstudiante;
    }

    public void setConceptosEstudiante(List<ConceptoDTO> conceptosEstudiante) {
	this.conceptosEstudiante = conceptosEstudiante;
    }

    public CarreraEstudianteDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(CarreraEstudianteDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public CarreraEstudiantePeriodoDTO getPeriodoSeleccionado() {
	return periodoSeleccionado;
    }

    public void setPeriodoSeleccionado(
	    CarreraEstudiantePeriodoDTO periodoSeleccionado) {
	this.periodoSeleccionado = periodoSeleccionado;
    }

    public Boolean getMostrarMatriculas() {
	return mostrarMatriculas;
    }

    public void setMostrarMatriculas(Boolean mostrarMatriculas) {
	this.mostrarMatriculas = mostrarMatriculas;
    }

    public OrigenInformacionEnum getTabla() {
	return tabla;
    }

    public void setTabla(OrigenInformacionEnum tabla) {
	this.tabla = tabla;
    }

    public Boolean getMostrarCarreras() {
	return mostrarCarreras;
    }

    public void setMostrarCarreras(Boolean mostrarCarreras) {
	this.mostrarCarreras = mostrarCarreras;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public List<TituloSenescyt> getTitulosSenescyt() {
	return titulosSenescyt;
    }

    public void setTitulosSenescyt(List<TituloSenescyt> titulosSenescyt) {
	this.titulosSenescyt = titulosSenescyt;
    }

    public int getNumeroGraduadosValidados() {
	return numeroGraduadosValidados;
    }

    public void setNumeroGraduadosValidados(int numeroGraduadosValidados) {
	this.numeroGraduadosValidados = numeroGraduadosValidados;
    }

    public List<ValorVariableDTO> getValorVariableDTO() {
	return valorVariableDTO;
    }

    public void setValorVariableDTO(List<ValorVariableDTO> valorVariableDTO) {
	this.valorVariableDTO = valorVariableDTO;
    }

    public boolean isVerActualizarMuestra() {
	return verActualizarMuestra;
    }

    public void setVerActualizarMuestra(boolean verActualizarMuestra) {
	this.verActualizarMuestra = verActualizarMuestra;
    }

    public boolean isVerActualizarGraduados() {
	return verActualizarGraduados;
    }

    public void setVerActualizarGraduados(boolean verActualizarGraduados) {
	this.verActualizarGraduados = verActualizarGraduados;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionada() {
	return evidenciaConceptoSeleccionada;
    }

    public void setEvidenciaConceptoSeleccionada(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionada) {
	this.evidenciaConceptoSeleccionada = evidenciaConceptoSeleccionada;
    }

}
