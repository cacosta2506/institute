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
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CargoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CursoCapacitacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ExperienciaProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvidenciaDocentesController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaDocentesController.class.getSimpleName());

    private List<DocenteDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<ContratacionDTO> listaContratacion;
    private List<ExperienciaProfesionalDTO> listaExperienciaProfesional;
    private List<CursoCapacitacionDTO> listaCapacitacion;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<FormacionProfesionalDTO> listaFormacion;
    private List<CargoAcademicoDTO> listaCargosAcademicos;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private FaseIesDTO faseiesDTO;
    private StreamedContent documentoDescarga;
    private IesDTO iesDTO;
    private String[] tabla;
    private UploadedFile file;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO docenteSeleccionado;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ContratacionDTO contratoSeleccionado;
    private ExperienciaProfesionalDTO experienciaSeleccionada;
    private CursoCapacitacionDTO capacitacionSeleccionada;
    private CargoAcademicoDTO cargoAcademicaSeleccionado;
    private ConceptoDTO conceptoSeleccionado;
    private DocenteDTO docenteDto;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean evidencias = false;
    private Boolean evidenciaConcepto = false;
    private Boolean contratos = false;
    private String usuario;
    private Long idProceso;
    private Boolean experiencia = false;
    private Boolean capacitacion = false;
    private Boolean formacion = false;
    private Boolean cargos = false;
    private Long idEvidencia;
    private String fichero;
    private String nombreFichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionada;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    public EvidenciaDocentesController() {
	this.listaDocentes = new ArrayList<DocenteDTO>();
	iesDTO = new IesDTO();
	faseiesDTO = new FaseIesDTO();
	this.listaContratacion = new ArrayList<ContratacionDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaExperienciaProfesional = new ArrayList<ExperienciaProfesionalDTO>();
	this.listaCapacitacion = new ArrayList<CursoCapacitacionDTO>();
	this.listaFormacion = new ArrayList<FormacionProfesionalDTO>();
	experienciaSeleccionada = new ExperienciaProfesionalDTO();
	capacitacionSeleccionada = new CursoCapacitacionDTO();
	formacionSeleccionada = new FormacionProfesionalDTO();
	informacionIesDto = new InformacionIesDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaDto = new EvidenciaDTO();
	conceptoSeleccionado = new ConceptoDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	// cargarDocente();

    }

    @PostConstruct
    public void start() {
	DocenteDTO docenteDto = null;
	// evidencias = false;
	try {
	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");
	    faseiesDTO = controller.getFaseIesDTO();
	    usuario = controller.getUsuario();
	    iesDTO = controller.getIes();
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    listaDocentes = registroServicio.obtenerDocentesPorInformacionIes(
		    docenteDto, informacionIesDto.getId());

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarDocente() {
	evidencias = true;
	contratos = false;
	capacitacion = false;
	evidenciaConcepto = false;
	experiencia = false;
	formacion = false;
	conceptoSeleccionado = null;
	try {
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarEvidencias() {
	LOG.info("conceptoSeleccionado: " + conceptoSeleccionado.getOrigen());
	LOG.info("docenteSeleccionado.getId(): " + docenteSeleccionado.getId());
	LOG.info("informacionIesDto.getId(): " + informacionIesDto.getId());
	LOG.info("IES: " + iesDTO.getId());
	System.out
	        .println("ORIGEN: " + OrigenInformacionEnum.CARGOS.getValor());
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
			    .obtenerContratacionPorPersona(
			            docenteSeleccionado.getId(),
			            informacionIesDto.getId());
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
			            docenteSeleccionado.getId(), iesDTO.getId());
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
			    .obtenerCursosPorDocenteEIes(
			            docenteSeleccionado.getId(), iesDTO.getId());
		    LOG.info("listaCapacitacion: " + listaCapacitacion.size());

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
			            docenteSeleccionado.getId(), iesDTO.getId());
		    LOG.info("listaFormacion: " + listaFormacion.size());

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
			            docenteSeleccionado.getId(), iesDTO.getId());

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

    public void cargarEConcepto() {
	try {
	    evidenciaConcepto = true;
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		idEvidencia = contratoSeleccionado.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + contratoSeleccionado.getId();

	    } else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		idEvidencia = experienciaSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + experienciaSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CAPACITACION.getValor())) {
		idEvidencia = capacitacionSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + capacitacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.FORMACION.getValor())) {
		idEvidencia = formacionSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + formacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CARGOS.getValor())) {
		idEvidencia = cargoAcademicaSeleccionado.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + cargoAcademicaSeleccionado.getId();

	    }

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), conceptoSeleccionado.getId(),
		            idEvidencia, conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cambioBanderaNegativo() {
	if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CONTRATOS.getValor())) {
	    contratoSeleccionado.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarContratacionDTO(contratoSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
	    experienciaSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio
		        .registrarExperienciaProfDocente(experienciaSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CAPACITACION.getValor())) {
	    capacitacionSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio
		        .registrarCapacitacion(capacitacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.FORMACION.getValor())) {
	    formacionSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarFormacion(formacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CARGOS.getValor())) {
	    cargoAcademicaSeleccionado.setVerificarEvidencia(false);
	    try {
		registroServicio
		        .registrarCargoAcademico(cargoAcademicaSeleccionado);
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
			if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CONTRATOS.getValor())) {
			    contratoSeleccionado.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarContratacionDTO(contratoSeleccionado);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			} else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
			    experienciaSeleccionada.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarExperienciaProfDocente(experienciaSeleccionada);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CAPACITACION.getValor())) {
			    capacitacionSeleccionada
				    .setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarCapacitacion(capacitacionSeleccionada);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.FORMACION.getValor())) {
			    formacionSeleccionada.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarFormacion(formacionSeleccionada);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CARGOS.getValor())) {
			    cargoAcademicaSeleccionado
				    .setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarCargoAcademico(cargoAcademicaSeleccionado);
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
	        + idEvidencia + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
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
	    verificarEvidencia();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(evidenciaConceptoSeleccionado);
	    evidenciaDto.setIdTabla(idEvidencia);
	    evidenciaDto.setFaseIesDTO(faseiesDTO);

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

		String urlNew = pDTO.getValor() + faseiesDTO.getId() + "/"
		        + iesDTO.getCodigo() + "/" + idEvidencia + "/";

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
			if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CONTRATOS.getValor())) {
			    contratoSeleccionado.setVerificarEvidencia(true);
			    registroServicio
				    .registrarContratacionDTO(contratoSeleccionado);

			} else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
			    experienciaSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarExperienciaProfDocente(experienciaSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CAPACITACION.getValor())) {
			    capacitacionSeleccionada
				    .setVerificarEvidencia(true);
			    registroServicio
				    .registrarCapacitacion(capacitacionSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.FORMACION.getValor())) {
			    formacionSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarFormacion(formacionSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CARGOS.getValor())) {
			    cargoAcademicaSeleccionado
				    .setVerificarEvidencia(true);
			    registroServicio
				    .registrarCargoAcademico(cargoAcademicaSeleccionado);

			}

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

    public List<DocenteDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<DocenteDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public PersonaDTO getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    public void setDocenteSeleccionado(PersonaDTO docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
    }

    public DocenteDTO getDocenteDto() {
	return docenteDto;
    }

    public void setDocenteDto(DocenteDTO docenteDto) {
	this.docenteDto = docenteDto;
    }

    public Boolean getEvidencias() {
	return evidencias;
    }

    public void setEvidencias(Boolean evidencias) {
	this.evidencias = evidencias;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
    }

    public Boolean getContratos() {
	return contratos;
    }

    public void setContratos(Boolean contratos) {
	this.contratos = contratos;
    }

    public List<ContratacionDTO> getListaContratacion() {
	return listaContratacion;
    }

    public void setListaContratacion(List<ContratacionDTO> listaContratacion) {
	this.listaContratacion = listaContratacion;
    }

    public ContratacionDTO getContratoSeleccionado() {
	return contratoSeleccionado;
    }

    public void setContratoSeleccionado(ContratacionDTO contratoSeleccionado) {
	this.contratoSeleccionado = contratoSeleccionado;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
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

    public String[] getTabla() {
	return tabla;
    }

    public void setTabla(String[] tabla) {
	this.tabla = tabla;
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

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public List<ExperienciaProfesionalDTO> getListaExperienciaProfesional() {
	return listaExperienciaProfesional;
    }

    public void setListaExperienciaProfesional(
	    List<ExperienciaProfesionalDTO> listaExperienciaProfesional) {
	this.listaExperienciaProfesional = listaExperienciaProfesional;
    }

    public Boolean getExperiencia() {
	return experiencia;
    }

    public void setExperiencia(Boolean experiencia) {
	this.experiencia = experiencia;
    }

    public ExperienciaProfesionalDTO getExperienciaSeleccionada() {
	return experienciaSeleccionada;
    }

    public void setExperienciaSeleccionada(
	    ExperienciaProfesionalDTO experienciaSeleccionada) {
	this.experienciaSeleccionada = experienciaSeleccionada;
    }

    public Boolean getCapacitacion() {
	return capacitacion;
    }

    public void setCapacitacion(Boolean capacitacion) {
	this.capacitacion = capacitacion;
    }

    public List<CursoCapacitacionDTO> getListaCapacitacion() {
	return listaCapacitacion;
    }

    public void setListaCapacitacion(
	    List<CursoCapacitacionDTO> listaCapacitacion) {
	this.listaCapacitacion = listaCapacitacion;
    }

    public CursoCapacitacionDTO getCapacitacionSeleccionada() {
	return capacitacionSeleccionada;
    }

    public void setCapacitacionSeleccionada(
	    CursoCapacitacionDTO capacitacionSeleccionada) {
	this.capacitacionSeleccionada = capacitacionSeleccionada;
    }

    public Boolean getFormacion() {
	return formacion;
    }

    public void setFormacion(Boolean formacion) {
	this.formacion = formacion;
    }

    public List<FormacionProfesionalDTO> getListaFormacion() {
	return listaFormacion;
    }

    public void setListaFormacion(List<FormacionProfesionalDTO> listaFormacion) {
	this.listaFormacion = listaFormacion;
    }

    public FormacionProfesionalDTO getFormacionSeleccionada() {
	return formacionSeleccionada;
    }

    public void setFormacionSeleccionada(
	    FormacionProfesionalDTO formacionSeleccionada) {
	this.formacionSeleccionada = formacionSeleccionada;
    }

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
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

    public String getNombreFichero() {
	return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
	this.nombreFichero = nombreFichero;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public FaseIesDTO getFaseiesDTO() {
	return faseiesDTO;
    }

    public void setFaseiesDTO(FaseIesDTO faseiesDTO) {
	this.faseiesDTO = faseiesDTO;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public Boolean getCargos() {
	return cargos;
    }

    public void setCargos(Boolean cargos) {
	this.cargos = cargos;
    }

    public List<CargoAcademicoDTO> getListaCargosAcademicos() {
	return listaCargosAcademicos;
    }

    public void setListaCargosAcademicos(
	    List<CargoAcademicoDTO> listaCargosAcademicos) {
	this.listaCargosAcademicos = listaCargosAcademicos;
    }

    public CargoAcademicoDTO getCargoAcademicaSeleccionado() {
	return cargoAcademicaSeleccionado;
    }

    public void setCargoAcademicaSeleccionado(
	    CargoAcademicoDTO cargoAcademicaSeleccionado) {
	this.cargoAcademicaSeleccionado = cargoAcademicaSeleccionado;
    }

    public Long getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(Long idProceso) {
	this.idProceso = idProceso;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionada() {
	return evidenciaConceptoSeleccionada;
    }

    public void setEvidenciaConceptoSeleccionada(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionada) {
	this.evidenciaConceptoSeleccionada = evidenciaConceptoSeleccionada;
    }

}
