package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProgramaEstudiosAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminProgramasEstudioAsignaturaController implements Serializable {

    private static final long serialVersionUID = -4967773530740528864L;

    private static final Logger LOG = Logger
	    .getLogger(AdminProgramasEstudioAsignaturaController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private CatalogoServicio catalogosServicio;

    private String usuario;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIes;
    private FaseDTO faseEvaluacion;
    private FaseIesDTO faseIesDTO;
    private AsignaturaDTO asignaturaDto;

    private Long idSedeSeleccionada;
    private Long idSedeDistribucionSeleccionada;
    private String perfil;
    private Boolean alertaEvaluador = false;
    private Integer totalPeas = 0;
    private String codigoPea;

    /* Asignaturas */
    private Long idSedeIesSeleccionada;
    private Long idInformacionCarreraSeleccionada;
    private Long idMallaSeleccionada;
    private List<SedeIesDTO> listaSedeIesAsignaturaDto;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto;
    private List<MallaCurricularDTO> listaMallaDto;
    private DualListModel<AsignaturaDTO> listaAsignaturasDTO;
    private List<AsignaturaDTO> source;
    private List<AsignaturaDTO> target;
    private List<AsignaturaDTO> targetAux;
    private List<MallaCurricularDTO> listaMallaCurricular;
    private List<String> listaPeasDiferentes;
    private List<SedeIesDTO> listaSedeIesDto;
    private List<AsignaturaDTO> listaAsignaturas;
    private List<AsignaturaDTO> listaAsignaturasPorPea;
    private AsignaturaDTO asignaturaSeleccionada;

    private ProgramaEstudiosAsignaturaDTO peaSeleccionado;

    public AdminProgramasEstudioAsignaturaController() {

	iesDTO = new IesDTO();
	informacionIes = new InformacionIesDTO();
	faseEvaluacion = new FaseDTO();
	faseIesDTO = new FaseIesDTO();

	listaMallaCurricular = new ArrayList<MallaCurricularDTO>();
	listaInformacionCarreraDto = new ArrayList<InformacionCarreraDTO>();
	listaMallaDto = new ArrayList<MallaCurricularDTO>();
	source = new ArrayList<AsignaturaDTO>();
	target = new ArrayList<AsignaturaDTO>();
	targetAux = new ArrayList<AsignaturaDTO>();
	listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source, target);
	asignaturaDto = new AsignaturaDTO();
	listaPeasDiferentes = new ArrayList<>();
	listaSedeIesDto = new ArrayList<>();
	listaSedeIesAsignaturaDto = new ArrayList<>();
	listaAsignaturas = new ArrayList<>();
	peaSeleccionado = new ProgramaEstudiosAsignaturaDTO();
	listaAsignaturasPorPea = new ArrayList<>();
	asignaturaSeleccionada = new AsignaturaDTO();
    }

    @PostConstruct
    public void start() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();
	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.perfil = controller.getPerfil().getNombre();

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public List<SedeIesDTO> cargarSedeIes() {

	List<SedeIesDTO> sedesIesDto = new ArrayList<SedeIesDTO>();
	try {
	    sedesIesDto = registroServicio.obtenerSedesIes(informacionIes
		    .getId());
	    LOG.info("listaSedeIesDto.size: " + sedesIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
	return sedesIesDto;
    }

    public void cargarCarreras() {

	try {
	    if (null == idSedeIesSeleccionada) {
		return;
	    }

	    listaInformacionCarreraDto.clear();
	    listaInformacionCarreraDto = registroServicio
		    .obtenerInfCarreraPorSede(idSedeIesSeleccionada, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void cargarMalla() {
	try {
	    if (null == idInformacionCarreraSeleccionada) {
		return;
	    }
	    listaMallaDto.clear();
	    InformacionCarreraDTO informacionCarreraDto = new InformacionCarreraDTO();
	    informacionCarreraDto.setId(idInformacionCarreraSeleccionada);
	    listaMallaDto = registroServicio
		    .obtenerMallaCurricular(informacionCarreraDto);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarAsignaturas() {

	try {
	    if (null == idMallaSeleccionada)
		return;

	    // source.clear();
	    listaAsignaturas = registroServicio
		    .obtenerAsignaturasPorMalla(idMallaSeleccionada);

	    // target = listaAsignaturasDTO.getTarget();
	    //
	    // if (null == target)
	    // target = new ArrayList<AsignaturaDTO>();
	    //
	    // listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source,
	    // target);
	    // for (AsignaturaDTO asignaturaDTO :
	    // listaAsignaturasDTO.getTarget()) {
	    // for (AsignaturaDTO aDTO : listaAsignaturasDTO.getSource()) {
	    // if (asignaturaDTO.getId().equals(aDTO.getId())) {
	    // listaAsignaturasDTO.getSource().remove(aDTO);
	    // break;
	    // }
	    // }
	    // }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void agregarAsignatura(AsignaturaDTO asignatura) {
	listaAsignaturasPorPea.add(asignatura);
    }

    public void eliminarAsignatura() {
	for (AsignaturaDTO asignatura : listaAsignaturasPorPea) {
	    if (asignatura.getId().equals(asignaturaSeleccionada.getId())) {
		listaAsignaturasPorPea.remove(asignatura);
		break;
	    }
	}
    }

    public void onTransfer(TransferEvent event) {
	StringBuilder builder = new StringBuilder();

	for (Object item : event.getItems()) {
	    LOG.info("clase: " + item.getClass());
	    String nombre = ((AsignaturaDTO) item).getNombre();
	    LOG.info("nomb: " + nombre);
	    builder.append(((AsignaturaDTO) item).getNombre()).append("<br />");

	    LOG.info("nombre: " + builder.toString());
	    AsignaturaDTO asignatura = (AsignaturaDTO) item;
	    targetAux.add(asignatura);
	}

	LOG.info("lista temporal: " + targetAux.size());

    }

    public void mostrarLista() {

	if (listaAsignaturasDTO.getSource().isEmpty()) {
	    JsfUtil.msgError("No hay entrada");

	}

	if (listaAsignaturasDTO.getTarget().isEmpty()) {
	    JsfUtil.msgError("No hay salida");

	}

	LOG.info("Lista Entrada: " + listaAsignaturasDTO.getSource().size());

	LOG.info("Lista Salida:" + listaAsignaturasDTO.getTarget().size());

    }

    public void nuevoPEA() {
	peaSeleccionado = new ProgramaEstudiosAsignaturaDTO();
	peaSeleccionado.setActivo(true);
	idInformacionCarreraSeleccionada = null;
	idMallaSeleccionada = null;
	idSedeIesSeleccionada = null;
	listaAsignaturas.clear();
	listaAsignaturasDTO.getTarget().clear();
	listaAsignaturasDTO.getSource().clear();
	try {
	    cargarListasIes();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void cargarListasIes() {
	listaSedeIesDto.clear();
	listaSedeIesDto = cargarSedeIes();
	listaSedeIesAsignaturaDto.clear();
	listaSedeIesAsignaturaDto = cargarSedeIes();
    }

    public void cargarEdicion(ProgramaEstudiosAsignaturaDTO peaDTO) {

	peaSeleccionado = peaDTO;

	idSedeIesSeleccionada = null;
	idInformacionCarreraSeleccionada = null;
	idMallaSeleccionada = null;
	listaAsignaturasDTO.getSource().clear();
	listaAsignaturasDTO.getTarget().clear();
	listaAsignaturas.clear();
	cargarListasIes();

	obtenerAsignaturas();

    }

    private void obtenerAsignaturas() {

	try {
	    listaAsignaturasPorPea = registroServicio
		    .obtenerAsignaturasPorPEA(this.peaSeleccionado.getId());
	    // listaAsignaturasDTO.setTarget(listaAsignaturas);
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public FaseDTO getFaseEvaluacion() {
	return faseEvaluacion;
    }

    public void setFaseEvaluacion(FaseDTO faseEvaluacion) {
	this.faseEvaluacion = faseEvaluacion;
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

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public DualListModel<AsignaturaDTO> getListaAsignaturasDTO() {
	return listaAsignaturasDTO;
    }

    public void setListaAsignaturasDTO(
	    DualListModel<AsignaturaDTO> listaAsignaturasDTO) {
	this.listaAsignaturasDTO = listaAsignaturasDTO;
    }

    public List<MallaCurricularDTO> getListaMallaCurricular() {
	return listaMallaCurricular;
    }

    public void setListaMallaCurricular(
	    List<MallaCurricularDTO> listaMallaCurricular) {
	this.listaMallaCurricular = listaMallaCurricular;
    }

    public Long getIdSedeIesSeleccionada() {
	return idSedeIesSeleccionada;
    }

    public void setIdSedeIesSeleccionada(Long idSedeIesSeleccionada) {
	this.idSedeIesSeleccionada = idSedeIesSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesAsignaturaDto() {
	return listaSedeIesAsignaturaDto;
    }

    public void setListaSedeIesAsignaturaDto(
	    List<SedeIesDTO> listaSedeIesAsignaturaDto) {
	this.listaSedeIesAsignaturaDto = listaSedeIesAsignaturaDto;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraDto() {
	return listaInformacionCarreraDto;
    }

    public void setListaInformacionCarreraDto(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto) {
	this.listaInformacionCarreraDto = listaInformacionCarreraDto;
    }

    public Long getIdInformacionCarreraSeleccionada() {
	return idInformacionCarreraSeleccionada;
    }

    public void setIdInformacionCarreraSeleccionada(
	    Long idInformacionCarreraSeleccionada) {
	this.idInformacionCarreraSeleccionada = idInformacionCarreraSeleccionada;
    }

    public List<MallaCurricularDTO> getListaMallaDto() {
	return listaMallaDto;
    }

    public void setListaMallaDto(List<MallaCurricularDTO> listaMallaDto) {
	this.listaMallaDto = listaMallaDto;
    }

    public Long getIdMallaSeleccionada() {
	return idMallaSeleccionada;
    }

    public void setIdMallaSeleccionada(Long idMallaSeleccionada) {
	this.idMallaSeleccionada = idMallaSeleccionada;
    }

    public List<AsignaturaDTO> getSource() {
	return source;
    }

    public void setSource(List<AsignaturaDTO> source) {
	this.source = source;
    }

    public List<AsignaturaDTO> getTarget() {
	return target;
    }

    public void setTarget(List<AsignaturaDTO> target) {
	this.target = target;
    }

    public List<AsignaturaDTO> getTargetAux() {
	return targetAux;
    }

    public void setTargetAux(List<AsignaturaDTO> targetAux) {
	this.targetAux = targetAux;
    }

    public AsignaturaDTO getAsignaturaDto() {
	return asignaturaDto;
    }

    public void setAsignaturaDto(AsignaturaDTO asignaturaDto) {
	this.asignaturaDto = asignaturaDto;
    }

    public Integer getTotalPeas() {
	return totalPeas;
    }

    public void setTotalPeas(Integer totalPeas) {
	this.totalPeas = totalPeas;
    }

    public List<String> getListaPeasDiferentes() {
	return listaPeasDiferentes;
    }

    public void setListaPeasDiferentes(List<String> listaPeasDiferentes) {
	this.listaPeasDiferentes = listaPeasDiferentes;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    public String getCodigoPea() {
	return codigoPea;
    }

    public void setCodigoPea(String codigoPea) {
	this.codigoPea = codigoPea;
    }

    public List<AsignaturaDTO> getListaAsignaturas() {
	return listaAsignaturas;
    }

    public void setListaAsignaturas(List<AsignaturaDTO> listaAsignaturas) {
	this.listaAsignaturas = listaAsignaturas;
    }

    public ProgramaEstudiosAsignaturaDTO getPeaSeleccionado() {
	return peaSeleccionado;
    }

    public void setPeaSeleccionado(ProgramaEstudiosAsignaturaDTO peaSeleccionado) {
	this.peaSeleccionado = peaSeleccionado;
    }

    public List<AsignaturaDTO> getListaAsignaturasPorPea() {
	return listaAsignaturasPorPea;
    }

    public void setListaAsignaturasPorPea(
	    List<AsignaturaDTO> listaAsignaturasPorPea) {
	this.listaAsignaturasPorPea = listaAsignaturasPorPea;
    }

    public AsignaturaDTO getAsignaturaSeleccionada() {
	return asignaturaSeleccionada;
    }

    public void setAsignaturaSeleccionada(AsignaturaDTO asignaturaSeleccionada) {
	this.asignaturaSeleccionada = asignaturaSeleccionada;
    }

}