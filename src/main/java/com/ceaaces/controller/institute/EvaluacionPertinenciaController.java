package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ItemEvaluacion;
import ec.gob.ceaaces.data.MuestraDetalle;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ActividadVinculacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MuestraDetalleDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ParticipacionProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ResultadoCriteriosDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableCualitativaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.dtos.historico.MuestraDetalleHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ValorVariableHistoricoDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evaluacionPertinenciaController")
public class EvaluacionPertinenciaController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1711633276053493785L;

    private static final Logger LOG = Logger
	    .getLogger(EvaluacionPertinenciaController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    private IesDTO ies;
    private InformacionIesDTO informacionIes;
    private FaseIesDTO faseIesDTO;
    private ValorVariableDTO valorVariableDTO;
    private ValorVariableCualitativaDTO valorVariableCualitativaDTO;
    private VariableProcesoDTO variableProcesoDTO;
    private MuestraDetalleDTO muestraDetalleDTO;
    private ResultadoCriteriosDTO resultadoCriteriosDTO;
    private CarreraIesDTO carreraIesDTO;
    private EvidenciaDTO evidenciaSeleccionada;
    private String fichero;
    private Boolean mostrarCEvidencias = false;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;

    private List<ValorVariableDTO> listaVariablesPertinencia;
    private List<ItemEvaluacion> listaMuestraDetalle;
    private List<ItemEvaluacion> listaItemsModificados;
    private List<ValorVariableDTO> variablesModificadosPertinencia;
    private List<ValorVariableHistoricoDTO> historicosVariable;
    private List<MuestraDetalleHistoricoDTO> historicoMuestraDetalle;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<ProyectoDTO> listaProyectosActividad;
    private InformacionCarreraDTO informacionCarreraDTO = new InformacionCarreraDTO();

    private boolean mostrarEvidencias;

    private String idProceso;
    private String usuario;
    private Date fechaActual = new Date();
    private Boolean alertaIngresoVerificado = false;
    private Boolean alertaMulticarrera = false;
    private Boolean alertaUnicarrera = false;
    private Boolean alertaHistorico = false;
    private UploadedFile file;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private MallaCurricularDTO mallaDTO;

    public EvaluacionPertinenciaController() {

	this.valorVariableDTO = new ValorVariableDTO();
	this.valorVariableCualitativaDTO = new ValorVariableCualitativaDTO();
	this.variableProcesoDTO = new VariableProcesoDTO();
	this.muestraDetalleDTO = new MuestraDetalleDTO();
	this.carreraIesDTO = new CarreraIesDTO();
	this.resultadoCriteriosDTO = new ResultadoCriteriosDTO();
	this.historicosVariable = new ArrayList<>();
	this.historicoMuestraDetalle = new ArrayList<>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaVariablesPertinencia = new ArrayList<>();
	this.listaMuestraDetalle = new ArrayList<>();
	this.listaItemsModificados = new ArrayList<>();
	this.variablesModificadosPertinencia = new ArrayList<>();
	this.pDTO = new ParametroDTO();
	this.peDTO = new ParametroDTO();
	this.evidenciaDto = new EvidenciaDTO();
	this.evidenciaSeleccionada = new EvidenciaDTO();
	this.evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	this.listaProyectosActividad = new ArrayList<>();
	this.mallaDTO = new MallaCurricularDTO();
    }

    @PostConstruct
    public void datosIniciales() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId()
		    .toString();

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.ies = controller.getIes();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(ies);

	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());

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
	    this.listaVariablesPertinencia = evaluacionServicio
		    .obtenerInformacionVariables("PERT",
		            this.informacionIes.getId(), "CUANTITATIVA%", null);

	    for (ValorVariableDTO variable : listaVariablesPertinencia) {
		variablesModificadosPertinencia
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }
	    ValorVariableDTO valorVariable = new ValorVariableDTO();
	    valorVariable.setVariableProcesoDTO(variableProcesoDTO);
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

	return "";
    }

    public void calcularVariablesPertinencia() {
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    historicosVariable.clear();
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    List<ValorVariableDTO> valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesPertinencia) {
		// valorVariableActual.setCodigoMuestra(new
		// Integer(informacionIes
		// .getId() + variableProcesoDTO.getId().toString()));
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesPertinencia.clear();
	    listaVariablesPertinencia.addAll(valoresVariableNuevos);
	    context.execute("dlgCalcularVariable.hide()");

	} catch (Exception se) {
	    se.printStackTrace();
	    JsfUtil.msgError("Error al calcular las variables. Comuníquese con el Administrador");
	}
    }

    /**
     * Método para llenar el cálculo de Si/No, Completo/Incompleto de las
     * Variables con Muestra
     * 
     * @author eviscarra
     * @param ListaVV
     *            Lista de tipo ValorVariableDTO
     * @param lista
     *            Lista de tipo ItemMuestraDetalle
     */
    public void calcularTotalVerificadoVariable(List<ValorVariableDTO> ListaVV,
	    List<ItemEvaluacion> lista) {
	lista.clear();
	try {

	    for (int i = 0; i < ListaVV.size(); i++) {
		if (!ListaVV.get(i).getVariableProcesoDTO()
		        .getMuestraEstratificada()) {
		    int countSi = 0;
		    int countNo = 0;

		    if (ListaVV.get(i).getCodigoMuestra() != null) {
			countSi = evaluacionServicio.obtenerValoresMuestra("1",
			        ListaVV.get(i).getCodigoMuestra());
			countNo = evaluacionServicio.obtenerValoresMuestra("0",
			        ListaVV.get(i).getCodigoMuestra());
		    }

		    MuestraDetalle md = new MuestraDetalle();
		    md.setContadorSi(countSi);
		    md.setContadorNo(countNo);
		    if (ListaVV.get(i).getTotalMuestra() != null) {
			if ((countSi + countNo) == ListaVV.get(i)
			        .getTotalMuestra()) {
			    md.setEstadoVerficacion("Completo");
			} else {
			    md.setEstadoVerficacion("Incompleto");
			}
		    }

		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setValorVariable(ListaVV.get(i));
		    item.setMuestraDetalleAux(md);
		    lista.add(item);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    /**
     * 
     * Lista de Muestra Detalle de Proyectos y Actividades de Vinculación
     * 
     * @author eviscarra
     * @version 11/06/2014 - 15:55:13
     */
    public void cargarMuestraDetalleVariablesPertinencia() {
	historicosVariable.clear();
	listaEvidenciaConcepto.clear();
	mostrarEvidencias = false;
	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    this.listaMuestraDetalle.clear();
	    this.alertaMulticarrera = true;
	    this.alertaUnicarrera = true;
	    List<MuestraDetalleDTO> listaMuestraDetalle = new ArrayList<>();
	    List<ProyectoDTO> listaProyectos = new ArrayList<>();
	    List<ActividadVinculacionDTO> listaActividades = new ArrayList<>();
	    List<ActividadVinculacionDTO> listaActividadesPropias = new ArrayList<>();

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(this.valorVariableDTO
		            .getCodigoMuestra());

	    if (this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
		    .getEtiqueta().toLowerCase().contains("unicarrera")
		    || this.valorVariableDTO.getVariableProcesoDTO()
		            .getVariableDTO().getEtiqueta().toLowerCase()
		            .contains("multicarrera")
		    || this.valorVariableDTO.getVariableProcesoDTO()
		            .getVariableDTO().getEtiqueta().toLowerCase()
		            .contains("alineados")) {
		if (this.valorVariableDTO.getVariableProcesoDTO()
		        .getVariableDTO().getEtiqueta().toLowerCase()
		        .contains("multicarrera")) {
		    alertaMulticarrera = true;
		    alertaUnicarrera = false;
		} else if (this.valorVariableDTO.getVariableProcesoDTO()
		        .getVariableDTO().getEtiqueta().toLowerCase()
		        .contains("unicarrera")) {
		    alertaUnicarrera = true;
		    alertaMulticarrera = false;
		} else if (this.valorVariableDTO.getVariableProcesoDTO()
		        .getVariableDTO().getEtiqueta().toLowerCase()
		        .contains("alineados")) {
		    alertaUnicarrera = false;
		    alertaMulticarrera = false;
		}

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
			    List<ParticipacionProyectoDTO> participaciones = new ArrayList<>();
			    for (ParticipacionProyectoDTO participacion : listaProyectos
				    .get(i).getListaParticipacionProyectosDTO()) {
				if (participacion.getPersonaDTO() != null) {
				    participaciones.add(participacion);
				}
			    }
			    listaProyectos.get(i)
				    .setListaParticipacionProyectosDTO(
				            participaciones);

			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    this.listaMuestraDetalle.add(item);
			}
		    }
		}
		context.execute("dlgMuestraProyecto.show()");

	    } else if (this.valorVariableDTO.getVariableProcesoDTO()
		    .getVariableDTO().getEtiqueta().toLowerCase()
		    .contains("actividades de vinculación")) {
		listaProyectosActividad.clear();
		listaActividades = evaluacionServicio
		        .obtenerMuestraDetalleActividades(this.valorVariableDTO
		                .getCodigoMuestra());

		List<ActividadVinculacionDTO> actividadesIes = registroServicio
		        .obtenerActividadesVinculacionPorIes(this.informacionIes
		                .getId());
		List<ActividadVinculacionDTO> actividadOtras = new ArrayList<>();

		for (ActividadVinculacionDTO act : listaActividades) {
		    int cont = 0;
		    for (ActividadVinculacionDTO actIes : actividadesIes) {
			if (actIes.getId().equals(act.getId())) {
			    listaActividadesPropias.add(act);
			    cont = 1;
			    break;
			}
		    }
		    if (cont == 0) {
			actividadOtras.add(act);
		    }
		}
		if (listaActividadesPropias.size() < listaActividades.size()) {
		    List<ProyectoDTO> proyectos = registroServicio
			    .obtenerProyectos(informacionIes.getId(),
			            "VINCULACION");
		    for (ActividadVinculacionDTO act : actividadOtras) {
			for (ProyectoDTO proyecto : proyectos) {
			    if (proyecto.getId().equals(act.getId())
				    && "ACTIVIDAD_VINCULACION".equals(proyecto
				            .getCategoriaEvaluacion())) {
				listaProyectosActividad.add(proyecto);
				break;
			    }
			}
		    }
		}
		LOG.info("LISTA ACTIVIDADES: " + listaActividadesPropias.size());

		for (int i = 0; i < listaActividadesPropias.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaActividadesPropias.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setActividadVinculacionDTO(listaActividadesPropias
				    .get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    this.listaMuestraDetalle.add(item);
			    LOG.info("listaMuestraDetalle: "
				    + listaMuestraDetalle.size());
			}
		    }
		}
		context.execute("dlgMuestraActividades.show()");

	    }

	    listaItemsModificados.clear();
	    for (ItemEvaluacion item : this.listaMuestraDetalle) {
		this.listaItemsModificados
		        .add((ItemEvaluacion) SerializationUtils.clone(item));
	    }
	} catch (ServicioException e1) {
	    e1.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void registrarValoresMuestraDetalles(String seccion) {
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuario);
	try {
	    for (int i = 0; i < this.listaMuestraDetalle.size(); i++) {
		ItemEvaluacion vvdto = listaMuestraDetalle.get(i);
		if (vvdto.getMuestraDetalle().getVerificado() != null) {
		    if (!vvdto
			    .getMuestraDetalle()
			    .getVerificado()
			    .equals(listaItemsModificados.get(i)
			            .getMuestraDetalle().getVerificado())) {
			if (vvdto.getMuestraDetalle().getObservaciones() == null
			        || vvdto.getMuestraDetalle().getObservaciones()
			                .isEmpty()) {
			    if ("VINCULACION".equals(seccion)) {
				// JsfUtil.msgError("Debe ingresar la observación del proyecto: "
				// + vvdto.getProyecto().getNombre());
				continue;
			    }
			    return;
			} else if (vvdto
			        .getMuestraDetalle()
			        .getObservaciones()
			        .equals(listaItemsModificados.get(i)
			                .getMuestraDetalle().getObservaciones())) {

			    if ("VINCULACION".equals(seccion)) {
				// JsfUtil.msgError("Debe ingresar la observación del proyecto: "
				// + vvdto.getProyecto().getNombre());
				continue;
			    } else {

				JsfUtil.msgAdvert("Debe cambiar la observación del proyecto: "
				        + vvdto.getProyecto().getNombre());
				return;
			    }
			}
		    }
		}
	    }
	    boolean modificado = false;
	    for (int i = 0; i < this.listaMuestraDetalle.size(); i++) {
		ItemEvaluacion vvdto = listaMuestraDetalle.get(i);
		if (vvdto.getMuestraDetalle().getVerificado() != null) {
		    if (!vvdto
			    .getMuestraDetalle()
			    .getVerificado()
			    .equals(listaItemsModificados.get(i)
			            .getMuestraDetalle().getVerificado())) {

			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setAuditoria(auditoriaDTO);
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			if ("VINCULACION".equals(seccion)) {
			    listaMuestraDetalle.get(i).getProyecto()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i).getProyecto()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarProyecto(listaMuestraDetalle.get(
				            i).getProyecto());
			}
			evaluacionServicio
			        .registrarMuestraDetalle(listaMuestraDetalle
			                .get(i).getMuestraDetalle());
			modificado = true;

		    } else if (vvdto
			    .getMuestraDetalle()
			    .getVerificado()
			    .equals(listaItemsModificados.get(i)
			            .getMuestraDetalle().getVerificado())
			    && vvdto.getMuestraDetalle().getObservaciones() != null
			    && !vvdto.getMuestraDetalle().getObservaciones()
			            .isEmpty()
			    && !vvdto
			            .getMuestraDetalle()
			            .getObservaciones()
			            .equals(listaItemsModificados.get(i)
			                    .getMuestraDetalle()
			                    .getObservaciones())) {
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setAuditoria(auditoriaDTO);
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			if ("VINCULACION".equals(seccion)) {
			    listaMuestraDetalle.get(i).getProyecto()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i).getProyecto()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarProyecto(listaMuestraDetalle.get(
				            i).getProyecto());
			}
			evaluacionServicio
			        .registrarMuestraDetalle(listaMuestraDetalle
			                .get(i).getMuestraDetalle());
			modificado = true;
		    }
		} else if (vvdto.getMuestraDetalle().getObservaciones() != null
		        && !vvdto.getMuestraDetalle().getObservaciones()
		                .isEmpty()
		        && !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaItemsModificados.get(i)
		                        .getMuestraDetalle().getObservaciones())) {
		    listaMuestraDetalle.get(i).getMuestraDetalle()
			    .setAuditoria(auditoriaDTO);
		    listaMuestraDetalle.get(i).getMuestraDetalle()
			    .setFaseIesDTO(this.faseIesDTO);
		    evaluacionServicio
			    .registrarMuestraDetalle(listaMuestraDetalle.get(i)
			            .getMuestraDetalle());
		    modificado = true;
		}
	    }

	    listaItemsModificados.clear();
	    for (ItemEvaluacion item : this.listaMuestraDetalle) {
		this.listaItemsModificados
		        .add((ItemEvaluacion) SerializationUtils.clone(item));
	    }

	    if ("VINCULACION".equals(seccion)) {
		Double valor = 0.0;
		int verificado = 0;
		int noAceptados = 0;
		for (ItemEvaluacion ie : listaMuestraDetalle) {
		    if (ie.getMuestraDetalle().getVerificado().equals("1")) {
			if (ie.getMuestraDetalle().getPonderacion() != null) {
			    valor = valor
				    + ie.getMuestraDetalle().getPonderacion();
			}
			verificado++;
		    } else if (ie.getMuestraDetalle().getVerificado()
			    .equals("0")) {
			noAceptados++;
		    }
		}
		valorVariableDTO.setValor(valor.toString());
		valorVariableDTO.setValorVerificado(Double.parseDouble(String
		        .valueOf(verificado)));
		valorVariableDTO.setRegistrosNoAceptados(noAceptados);
		valorVariableDTO.setAuditoriaDTO(auditoriaDTO);
		valorVariableDTO.setModificado(false);
		evaluacionServicio.registrarValorVariable(valorVariableDTO);

		this.listaVariablesPertinencia = evaluacionServicio
		        .obtenerInformacionVariables("PERT",
		                this.informacionIes.getId(), "CUANTITATIVA%",
		                null);
		variablesModificadosPertinencia.clear();
		for (ValorVariableDTO variable : listaVariablesPertinencia) {
		    variablesModificadosPertinencia
			    .add((ValorVariableDTO) SerializationUtils
			            .clone(variable));
		}

	    }
	    if (modificado) {
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la variable "
		        + valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getEtiqueta()
		        + ", corresponda a la modificación realizada en las muestras.");
		JsfUtil.msgInfo("Registros actualizados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se ha realizado ningún cambio");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void guardarObservacionVariablesPertinencia() {
	try {
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);
	    boolean modificado = false;
	    for (int i = 0; i < this.listaVariablesPertinencia.size(); i++) {
		ValorVariableDTO vvdto = listaVariablesPertinencia.get(i);
		if ((vvdto.getValor() != null && !vvdto.getValor().isEmpty() && !vvdto
		        .getValor().equals(
		                variablesModificadosPertinencia.get(i)
		                        .getValor()))
		        || (vvdto.getObservacion() != null
		                && !vvdto.getObservacion().isEmpty() && !vvdto
		                .getObservacion().equals(
		                        variablesModificadosPertinencia.get(i)
		                                .getObservacion()))) {
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    modificado = true;
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
	    }

	    this.listaVariablesPertinencia = evaluacionServicio
		    .obtenerInformacionVariables("PERT",
		            this.informacionIes.getId(), "CUANTITATIVA%", null);

	    variablesModificadosPertinencia.clear();
	    for (ValorVariableDTO variable : listaVariablesPertinencia) {
		variablesModificadosPertinencia
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }
	    if (modificado) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se han realizado cambios sobre las variables.");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void obtenerHistoricoVariable(ValorVariableDTO valorVariable) {
	LOG.info("obtenerHistoricoVariable ");
	historicosVariable.clear();
	listaEvidenciaConcepto.clear();
	// historicosVariable = evaluacionServicio
	// .obtenerHistoricoValorVariable(idValorVariable);
	historicosVariable = evaluacionServicio
	        .obtenerHistoricoPorIesYVariableProceso(valorVariable
	                .getIdInformacionIes(), valorVariable
	                .getVariableProcesoDTO().getId());
	LOG.info("historicosVariable SIZE: " + historicosVariable.size());
    }

    public void obtenerHistoricoMuestra(Long idMuestraDetalle) {
	historicoMuestraDetalle.clear();
	listaEvidenciaConcepto.clear();
	mostrarEvidencias = false;
	historicoMuestraDetalle = evaluacionServicio
	        .obtenerHistoricoMuestraDetalle(idMuestraDetalle);
    }

    public void cargarListaEvidencias(MuestraDetalleDTO muestra,
	    String grupoConcepto) {
	muestraDetalleDTO = muestra;
	try {
	    mostrarEvidencias = true;
	    List<ConceptoDTO> listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso,
		            GrupoConceptoEnum.parse(grupoConcepto).getValor());

	    ConceptoDTO conceptoSeleccionado = null;
	    for (ConceptoDTO concepto : listaEvidencia) {
		if (concepto.getOrigen().equals(
		        valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getTablaMuestra())) {
		    if ("proyectos".equals(valorVariableDTO
			    .getVariableProcesoDTO().getVariableDTO()
			    .getTablaMuestra())) {
			if ("PRYVIN".equals(concepto.getCodigo())) {
			    conceptoSeleccionado = concepto;
			} else if ("PRYINV".equals(concepto.getCodigo())
			        && valorVariableDTO.getVariableProcesoDTO()
			                .getDescripcion().toLowerCase()
			                .contains("alineados")) {
			    conceptoSeleccionado = concepto;
			}
		    } else {
			conceptoSeleccionado = concepto;
		    }
		}
	    }

	    listaEvidenciaConcepto.clear();
	    if (conceptoSeleccionado != null) {
		listaEvidenciaConcepto = institutosServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                ies.getId(), conceptoSeleccionado.getId(),
		                muestraDetalleDTO.getIdTabla(),
		                valorVariableDTO.getVariableProcesoDTO()
		                        .getVariableDTO().getTablaMuestra());
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    e.printStackTrace();
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {
	fichero = evidencia.getNombreArchivo();
	url = evidencia.getUrl();
	String[] nombreFichero = evidencia.getNombreArchivo().split("[.]");

	extensionDocumento = nombreFichero[nombreFichero.length - 1];
	descargar = true;
	return;

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

    public String getFichero() {
	return fichero;
    }

    public String getUrl() {
	return url;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public List<ValorVariableDTO> getListaVariablesPertinencia() {
	return listaVariablesPertinencia;
    }

    public void setListaVariablesPertinencia(
	    List<ValorVariableDTO> listaVariablesPertinencia) {
	this.listaVariablesPertinencia = listaVariablesPertinencia;
    }

    public IesDTO getIes() {
	return ies;
    }

    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public Date getFechaActual() {
	return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
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

    public ValorVariableDTO getValorVariableDTO() {
	return valorVariableDTO;
    }

    public void setValorVariableDTO(ValorVariableDTO valorVariableDTO) {
	this.valorVariableDTO = valorVariableDTO;
    }

    public List<ItemEvaluacion> getListaMuestraDetalle() {
	return listaMuestraDetalle;
    }

    public void setListaMuestraDetalle(List<ItemEvaluacion> listaMuestraDetalle) {
	this.listaMuestraDetalle = listaMuestraDetalle;
    }

    public Boolean getAlertaIngresoVerificado() {
	return alertaIngresoVerificado;
    }

    public void setAlertaIngresoVerificado(Boolean alertaIngresoVerificado) {
	this.alertaIngresoVerificado = alertaIngresoVerificado;
    }

    public VariableProcesoDTO getVariableProcesoDTO() {
	return variableProcesoDTO;
    }

    public void setVariableProcesoDTO(VariableProcesoDTO variableProcesoDTO) {
	this.variableProcesoDTO = variableProcesoDTO;
    }

    public MuestraDetalleDTO getMuestraDetalleDTO() {
	return muestraDetalleDTO;
    }

    public void setMuestraDetalleDTO(MuestraDetalleDTO muestraDetalleDTO) {
	this.muestraDetalleDTO = muestraDetalleDTO;
    }

    public Boolean getAlertaMulticarrera() {
	return alertaMulticarrera;
    }

    public void setAlertaMulticarrera(Boolean alertaMulticarrera) {
	this.alertaMulticarrera = alertaMulticarrera;
    }

    public Boolean getAlertaUnicarrera() {
	return alertaUnicarrera;
    }

    public void setAlertaUnicarrera(Boolean alertaUnicarrera) {
	this.alertaUnicarrera = alertaUnicarrera;
    }

    public Boolean getAlertaHistorico() {
	return alertaHistorico;
    }

    public void setAlertaHistorico(Boolean alertaHistorico) {
	this.alertaHistorico = alertaHistorico;
    }

    public ResultadoCriteriosDTO getResultadoCriteriosDTO() {
	return resultadoCriteriosDTO;
    }

    public void setResultadoCriteriosDTO(
	    ResultadoCriteriosDTO resultadoCriteriosDTO) {
	this.resultadoCriteriosDTO = resultadoCriteriosDTO;
    }

    public CarreraIesDTO getCarreraIesDTO() {
	return carreraIesDTO;
    }

    public void setCarreraIesDTO(CarreraIesDTO carreraIesDTO) {
	this.carreraIesDTO = carreraIesDTO;
    }

    public List<ValorVariableHistoricoDTO> getHistoricosVariable() {
	return historicosVariable;
    }

    public List<MuestraDetalleHistoricoDTO> getHistoricoMuestraDetalle() {
	return historicoMuestraDetalle;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public boolean getMostrarEvidencias() {
	return mostrarEvidencias;
    }

    public ValorVariableCualitativaDTO getValorVariableCualitativaDTO() {
	return valorVariableCualitativaDTO;
    }

    public void setValorVariableCualitativaDTO(
	    ValorVariableCualitativaDTO valorVariableCualitativaDTO) {
	this.valorVariableCualitativaDTO = valorVariableCualitativaDTO;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
    }

    public String getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(String idProceso) {
	this.idProceso = idProceso;
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

    public void setFichero(String fichero) {
	this.fichero = fichero;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public void setHistoricosVariable(
	    List<ValorVariableHistoricoDTO> historicosVariable) {
	this.historicosVariable = historicosVariable;
    }

    public void setHistoricoMuestraDetalle(
	    List<MuestraDetalleHistoricoDTO> historicoMuestraDetalle) {
	this.historicoMuestraDetalle = historicoMuestraDetalle;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
    }

    public void setMostrarEvidencias(boolean mostrarEvidencias) {
	this.mostrarEvidencias = mostrarEvidencias;
    }

    public Boolean getMostrarCEvidencias() {
	return mostrarCEvidencias;
    }

    public void setMostrarCEvidencias(Boolean mostrarCEvidencias) {
	this.mostrarCEvidencias = mostrarCEvidencias;
    }

    public List<ItemEvaluacion> getListaItemsModificados() {
	return listaItemsModificados;
    }

    public void setListaItemsModificados(
	    List<ItemEvaluacion> listaItemsModificados) {
	this.listaItemsModificados = listaItemsModificados;
    }

    public List<ValorVariableDTO> getVariablesModificadosPertinencia() {
	return variablesModificadosPertinencia;
    }

    public void setVariablesModificadosPertinencia(
	    List<ValorVariableDTO> variablesModificadosPertinencia) {
	this.variablesModificadosPertinencia = variablesModificadosPertinencia;
    }

    public List<ProyectoDTO> getListaProyectosActividad() {
	return listaProyectosActividad;
    }

    public void setListaProyectosActividad(
	    List<ProyectoDTO> listaProyectosActividad) {
	this.listaProyectosActividad = listaProyectosActividad;
    }

    public MallaCurricularDTO getMallaDTO() {
	return mallaDTO;
    }

    public void setMallaDTO(MallaCurricularDTO mallaDTO) {
	this.mallaDTO = mallaDTO;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

}