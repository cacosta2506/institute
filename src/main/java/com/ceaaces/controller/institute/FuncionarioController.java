/*
 * 
 * F * Desarrollado por: eviscarra
 * 
 * Copyright (c) 20
 * 
 * 
 * 
 * 
 * 3-2013 CEAACESS All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of CEAACESS.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with CEAACES.
 * 
 * CEAACES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 */
package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.beanutils.PropertyUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PaisDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.SubAreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FuncionarioDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.CategoriaTitularEnum;
import ec.gob.ceaaces.institutos.enumeraciones.DiscapacidadEnum;
import ec.gob.ceaaces.institutos.enumeraciones.GeneroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.RelacionIESEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TiempoDedicacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoFuncionarioEnum;
import ec.gob.ceaaces.seguridad.model.dtos.UsuarioDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.services.UsuarioIesCarrerasServicio;
import ec.gob.ceaaces.util.JsfUtil;
import ec.gob.ceaaces.util.Util;

public class FuncionarioController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 178648910196975660L;

    private static final Logger LOG = Logger
	    .getLogger(FuncionarioController.class.getSimpleName());

    @Autowired
    private CatalogoServicio catalagoServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private UsuarioIesCarrerasServicio uicServicio;

    @Autowired
    private RegistroServicio registroServicio;

    private int indiceTab;

    private Date fechaMin;
    private Date fechaMax;
    private Date fechaActual;

    private Long idPais;
    private Long idPaisFormacion;
    private Long idNivelTitulo = -99L;

    private Long idSubArea;
    private Long idArea = -99L;
    private List<AreaConocimientoDTO> areasConocimiento;
    private List<SubAreaConocimientoDTO> subareas;

    private List<PaisDTO> paises;
    private List<String> relacionesIes;
    private List<String> tiemposDedicacion;
    private List<String> categorias;
    private List<String> discapacidades;
    private List<String> generos;
    private List<String> tipoFuncionario;

    private List<IesDTO> listaIes;
    private List<IesDTO> iesFiltros;
    private CarreraIesDTO carreraSeleccionada;
    private IesDTO iesSeleccionada;
    private IesDTO iesFormacion;

    private List<FuncionarioDTO> funcionarios;
    private List<FuncionarioDTO> funcionariosFiltros;
    private FuncionarioDTO funcionarioSeleccionado;
    private final UsuarioDTO usuarioSistema;

    private PersonaDTO personaFuncionario;
    private ContratacionDTO contratoFuncionario;

    private List<ContratacionDTO> contrataciones;

    private boolean verTablaIes;
    private boolean verBtnIesDoc;
    private boolean verBtnIesCon;
    private boolean verBtnIesFor;
    private boolean verTabDatosPersonales;
    private boolean verTabContratacion;

    private String discapacidad = DiscapacidadEnum.NINGUNA.getValue();
    private String categoria;
    private String genero;
    private String tipoFunc;
    private String pestania = "Datos Personales";
    private String accion = "Guardar cambios";
    private String msgAccion = "";
    private String cedulaFuncionario;
    private Long idSedeSeleccionada;
    private String relacionSeleccionada;

    private FaseDTO faseEvaluacionDTO;
    private InformacionIesDTO informacionIesDTO;
    private InformacionCarreraDTO informacionCarreraDTO;

    private List<SedeIesDTO> listaSedeIesDto;
    private Boolean alertaEvaluador = false;
    private Integer numeroFuncionarios;

    // Filtros Funcionarios
    private int indice;

    private static final String ORIGEN_CARGA = "IES";
    private final static String URL_ELIMINADOS = "/home/jhomara/archivosIES/evaluacionCarreras/eliminados/";
    private final static String URL = "/home/jhomara/archivosIES/evaluacionCarreras";
    // private final static String URL_ELIMINADOS =
    // "/archivosIES/evaluacionCarreras/eliminados/";
    // private final static String URL = "/archivosIES/evaluacionCarreras";

    private String clienteId;

    private boolean editarContrato;

    private String perfil;

    private int registros = 10;
    private int indiceAtras;
    private int indiceSiguiente;
    private int numRegistros;
    private boolean busqueda;
    private String identificacion;
    private List<FuncionarioDTO> listaFuncionariosTodos;
    private static final Integer[] rangos = { 10, 20, 50, 100, 200 };
    private int contador;
    private FaseIesDTO faseIesDTO;
    private boolean habilitarSiguiente = true;

    public FuncionarioController() {

	this.paises = new ArrayList<>();
	this.listaIes = new ArrayList<>();
	this.iesFiltros = new ArrayList<IesDTO>();
	this.funcionarios = new ArrayList<>();
	this.funcionariosFiltros = new ArrayList<>();
	this.categorias = new ArrayList<>();
	this.tiemposDedicacion = new ArrayList<>();
	this.relacionesIes = new ArrayList<>();
	this.discapacidades = new ArrayList<>();
	this.generos = new ArrayList<>();
	this.tipoFuncionario = new ArrayList<>();
	this.contrataciones = new ArrayList<>();
	this.areasConocimiento = new ArrayList<>();
	this.subareas = new ArrayList<>();

	this.iesSeleccionada = new IesDTO();
	this.funcionarioSeleccionado = new FuncionarioDTO();
	this.usuarioSistema = new UsuarioDTO();
	this.carreraSeleccionada = new CarreraIesDTO();

	this.personaFuncionario = new PersonaDTO();
	this.contratoFuncionario = new ContratacionDTO();

	this.verBtnIesDoc = true;
	this.verTabDatosPersonales = false;
	this.verTabContratacion = true;

	this.listaFuncionariosTodos = new ArrayList<FuncionarioDTO>();
	this.listaSedeIesDto = new ArrayList<>();

    }

    @PostConstruct
    private void cargarDatosIniciales() {
	try {

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.alertaEvaluador = false;
	    this.discapacidades.clear();
	    this.generos.clear();
	    this.relacionesIes.clear();
	    this.tiemposDedicacion.clear();
	    this.categorias.clear();
	    this.funcionarios.clear();
	    this.funcionariosFiltros.clear();
	    this.areasConocimiento.clear();

	    for (DiscapacidadEnum dis : DiscapacidadEnum.values()) {
		this.discapacidades.add(dis.getValue());
	    }

	    for (GeneroEnum gen : GeneroEnum.values()) {
		this.generos.add(gen.getValue());
	    }

	    for (CategoriaTitularEnum cat : CategoriaTitularEnum.values()) {
		this.categorias.add(cat.getValue());
	    }

	    for (TiempoDedicacionEnum tid : TiempoDedicacionEnum.values()) {
		this.tiemposDedicacion.add(tid.getValue());
	    }

	    for (RelacionIESEnum rel : RelacionIESEnum.values()) {
		this.relacionesIes.add(rel.getValue());
	    }

	    for (TipoFuncionarioEnum tfe : TipoFuncionarioEnum.values()) {
		this.tipoFuncionario.add(tfe.getValue());
	    }

	    this.areasConocimiento = catalagoServicio
		    .obtenerAreasConocimiento();

	    this.perfil = controller.getPerfil().getNombre();

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    this.iesSeleccionada = controller.getIes();
	    this.informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());

	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.numeroFuncionarios = registroServicio
		    .totalFuncionariosPorIes(informacionIesDTO.getId());

	    cargarFuncionarios();
	    cargarPaises();
	    contextUsuario();
	    cargarSedeIes();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    /**
     * 
     * Seteo de usuario automatico
     * 
     * @author eteran
     * @version 09/09/2013 - 16:28:20
     */
    private void contextUsuario() {
	this.usuarioSistema.setUsuario(SecurityContextHolder.getContext()
	        .getAuthentication().getName());
    }

    /**
     * 
     * Carga todas las Ies
     * 
     * @author eteran
     * @version 04/09/2013 - 11:18:18
     */
    public void cargarIes() {
	this.listaIes.clear();

	try {
	    this.listaIes = uicServicio.obtenerIes();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se cargaron las Ies");
	    return;
	}
    }

    private void cargarFuncionarios() {

	try {

	    funcionarios.clear();
	    funcionariosFiltros.clear();
	    FuncionarioDTO funcionarioDTO = null;
	    if (busqueda) {
		limpiarListas();
		busqueda = false;
		habilitarSiguiente = true;
		cargarFuncionarios();
		identificacion = null;
		return;
	    }

	    if (indice < 0 || registros == 0) {
		funcionarios.clear();
		indice = 0;
		return;
	    }

	    LOG.info("1 listaFuncionariosTodos size.."
		    + this.listaFuncionariosTodos.size());

	    if (listaFuncionariosTodos.size() <= (numRegistros)) {
		int indiceaux = 0;
		if (listaFuncionariosTodos.size() != 0) {
		    indiceaux = listaFuncionariosTodos.size() - 1;
		} else {
		    indiceaux = 0;
		}

		this.listaFuncionariosTodos
		        .addAll(registroServicio.obtenerFuncionario(
		                funcionarioDTO, informacionIesDTO.getId(),
		                indiceaux, registros));

		LOG.info("2 listaFuncionariosTodos size.."
		        + this.listaFuncionariosTodos.size());

		List<FuncionarioDTO> auxLista = new ArrayList<FuncionarioDTO>();
		auxLista.addAll(listaFuncionariosTodos);
		if (auxLista.size() != 0) {
		    if (listaFuncionariosTodos.size() < indice + registros) {
			this.funcionarios = auxLista.subList(indice,
			        listaFuncionariosTodos.size());
		    } else {
			this.funcionarios = auxLista.subList(indice, indice
			        + registros);
		    }
		}
		this.funcionariosFiltros.addAll(funcionarios);
	    } else {
		List<FuncionarioDTO> auxLista = new ArrayList<FuncionarioDTO>();
		auxLista.addAll(listaFuncionariosTodos);
		if (auxLista.size() != 0) {
		    this.funcionarios = auxLista.subList(indice, indice
			    + registros);
		    this.funcionariosFiltros.addAll(funcionarios);
		}
	    }
	    if (this.funcionarios.size() < registros) {
		habilitarSiguiente = false;
	    }
	    System.out
		    .println("funcionarios size.." + this.funcionarios.size());
	    LOG.info("funcionariosFiltros size.."
		    + this.funcionariosFiltros.size());
	    LOG.info("listaFuncionariosTodos size.."
		    + this.listaFuncionariosTodos.size());
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void buscarFuncionarioPorCedula() {
	try {

	    if (!identificacion.equals("")) {
		indice = 0;
		FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
		this.funcionarios.clear();
		this.listaFuncionariosTodos.clear();
		this.funcionariosFiltros.clear();
		funcionarioDTO.setIdentificacion(identificacion);
		funcionarioDTO.setIdInformacionIes(this.informacionIesDTO
		        .getId());

		funcionarioDTO = registroServicio
		        .obtenerFuncionarioPorCedula(funcionarioDTO);

		if (funcionarioDTO != null) {
		    funcionarios.add(funcionarioDTO);
		    this.funcionariosFiltros.add(funcionarioDTO);
		    this.listaFuncionariosTodos.add(funcionarioDTO);

		    JsfUtil.msgInfo("Funcionario encontrado");
		} else {
		    JsfUtil.msgInfo("Funcionario no encontrado!");
		}
	    } else {
		JsfUtil.msgError("Ingrese número de cédula!");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Falló búsqueda de funcionario por cédula");
	}
	busqueda = true;
	habilitarSiguiente = true;
    }

    public void limpiarListas() {
	funcionarios.clear();
	funcionariosFiltros.clear();
	listaFuncionariosTodos.clear();
	indice = 0;
	registros = 10;
	numRegistros = 0;
    }

    /**
     * 
     * Para controlar la navegacion entre tabs del tabView
     * 
     * @author eteran
     * @version 10/09/2013 - 10:49:28
     * @param event
     */
    public void tomarTab(TabChangeEvent event) {
	this.pestania = event.getTab().getTitle();
	activarTab();
    }

    /**
     * 
     * Activa el tab de tabView segun la pestaña
     * 
     * @author eteran
     * @version 12/09/2013 - 14:40:19
     */
    public void activarTab() {
	if (this.pestania.equals("Datos Personales")) {
	    this.indiceTab = 0;
	}

	if (this.pestania.equals("Contratacion")) {
	    this.indiceTab = 1;
	}

	if (funcionarioSeleccionado.getId() == null) {
	    this.verTabDatosPersonales = false;
	    this.verTabContratacion = true;
	} else {
	    this.verTabDatosPersonales = false;
	    this.verTabContratacion = false;
	}
    }

    /**
     * 
     * @return
     */
    public String regresar() {
	limpiarListas();
	cargarFuncionarios();
	editarContrato = false;

	return "administracionFuncionarios?faces-redirct=true";
    }

    /**
     * 
     * Limpia el objeto iesSeleccionada
     * 
     * @author eteran
     * @version 03/09/2013 - 16:24:46
     */
    public void cancelarSeleccionIes() {
	if (this.pestania.equals("Formacion")) {
	    this.iesFormacion = new IesDTO();
	}
    }

    /**
     * 
     * Inicializa el objeto Funcionario
     * 
     * @author eviscarra
     * @version 12/09/2013 - 14:41:17
     * @param event
     */
    public void iniciarNuevoFuncionario() {
	this.funcionarioSeleccionado = new FuncionarioDTO();
	this.accion = "Guardar";
	this.indiceTab = 0;
	cargarPaises();
    }

    private void cargarPaises() {
	this.paises.clear();
	try {
	    this.paises = catalagoServicio.obtenerPaises();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar los paises");
	    return;
	}
    }

    private void nuevoRedirect() {
	if (funcionarioSeleccionado == null) {
	    return;
	}

	this.contrataciones.clear();

	// cargarDatosIniciales();

	if (funcionarioSeleccionado.getId() == null) {
	    funcionarioSeleccionado = new FuncionarioDTO();
	    idPais = -99L;
	}

	if (this.funcionarioSeleccionado.getId() != null) {
	    this.personaFuncionario = new PersonaDTO();

	    try {
		PropertyUtils.copyProperties(personaFuncionario,
		        funcionarioSeleccionado);
	    } catch (IllegalAccessException e1) {
		e1.printStackTrace();
	    } catch (InvocationTargetException e1) {
		e1.printStackTrace();
	    } catch (NoSuchMethodException e1) {
		e1.printStackTrace();
	    }

	    cargarPaises();

	    if (funcionarioSeleccionado.getIesDTO() == null) {
		personaFuncionario.setIesDTO(this.iesSeleccionada);
		funcionarioSeleccionado.setIesDTO(this.iesSeleccionada);
	    }

	    if (funcionarioSeleccionado.getPaisOrigen() != null) {
		idPais = funcionarioSeleccionado.getPaisOrigen().getId();
	    } else {
		idPais = -99L;
	    }

	    if (funcionarioSeleccionado.getSedeIesDTO() != null) {
		idSedeSeleccionada = funcionarioSeleccionado.getSedeIesDTO()
		        .getId();
	    }

	    if (funcionarioSeleccionado.getDiscapacidad() != null) {
		discapacidad = funcionarioSeleccionado.getDiscapacidad()
		        .getValue();
	    } else {
		funcionarioSeleccionado
		        .setDiscapacidad(DiscapacidadEnum.NINGUNA);
		discapacidad = funcionarioSeleccionado.getDiscapacidad()
		        .getValue();
	    }

	    if (funcionarioSeleccionado.getSexo() != null) {
		genero = funcionarioSeleccionado.getSexo().getValue();
	    } else {
		genero = funcionarioSeleccionado.getSexo().getValue();
	    }

	    if (funcionarioSeleccionado.getTipoFuncionario() != null) {
		tipoFunc = funcionarioSeleccionado.getTipoFuncionario()
		        .getValue();
	    }

	    if (funcionarioSeleccionado.getContratacionDTO() != null) {

		for (int i = 0; i < funcionarioSeleccionado
		        .getContratacionDTO().size(); i++) {
		    if (funcionarioSeleccionado.getContratacionDTO().get(i)
			    .getActivo()) {
			contrataciones.add(funcionarioSeleccionado
			        .getContratacionDTO().get(i));
		    }
		}
		contrataciones.removeAll(Collections.singleton(null));
	    }

	}
	activarTab();
    }

    public void nuevo() {
	nuevoRedirect();

	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/funcionarios/funcionario.jsf");

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public void cancelarIesFormacion() {
	this.iesFormacion = new IesDTO();
    }

    public void eliminarFuncionario() {
	try {
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	    auditoria.setFechaModificacion(new Date());
	    funcionarioSeleccionado.setAuditoria(auditoria);
	    funcionarioSeleccionado.setFaseIesDTO(faseIesDTO);
	    registroServicio.eliminarFuncionario(funcionarioSeleccionado);
	    iniciarNuevoFuncionario();
	    limpiarListas();
	    cargarFuncionarios();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo eliminar el Funcionario seleccionado");
	    return;
	}

    }

    public void eliminarContratacion() {
	boolean eliminar = false;
	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	auditoria.setFechaModificacion(new Date());
	for (int i = 0; i < contrataciones.size(); i++) {
	    if (contratoFuncionario.getId() != null
		    && contratoFuncionario.getId().equals(
		            contrataciones.get(i).getId())) {
		contrataciones.get(i).setActivo(false);
		personaFuncionario.setOrigenCarga(ORIGEN_CARGA);
		personaFuncionario.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		contratoFuncionario.setActivo(false);
		contratoFuncionario.setPersonaDTO(funcionarioSeleccionado);
		contratoFuncionario.setAuditoriaDTO(auditoria);
		contratoFuncionario.setFaseIesDTO(faseIesDTO);
		registroServicio.registrarContratacionDTO(contratoFuncionario);
		contrataciones.remove(contratoFuncionario);
		JsfUtil.msgInfo("Registro eliminado correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el contrato seleccionado");
		return;
	    }
	}

    }

    /**
     * 
     * Se toma la discapacidad del combo discapacidades
     * 
     * @author eteran
     * @version 12/09/2013 - 14:42:46
     * @param event
     */
    public void tomarDiscapacidad(ValueChangeEvent event) {
	this.discapacidad = (String) event.getNewValue();
    }

    /**
     * 
     * Se toma el genero del combo sexo
     * 
     * @author eteran
     * @version 12/09/2013 - 14:43:31
     * @param event
     */
    public void tomarGenero(ValueChangeEvent event) {
	this.genero = (String) event.getNewValue();
    }

    public void tomarAreaConocimiento() {
	cargarSubareasConocimiento(idArea);

	if (this.idArea.equals(-99L)) {
	    subareas.clear();
	}
    }

    public void tomarSubArea(ValueChangeEvent event) {
	this.idSubArea = (Long) event.getNewValue();

	if (idSubArea != -99L) {
	    SubAreaConocimientoDTO sub = new SubAreaConocimientoDTO();
	    sub.setId(this.idSubArea);
	}
    }

    private void cargarSubareasConocimiento(Long idArea) {
	this.subareas.clear();
	try {
	    this.subareas = catalagoServicio
		    .obtenerSubAreasConocimientoPorArea(idArea);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar las subareas de conocimiento");
	    return;
	}
    }

    public void guardar() {

	if (funcionarioSeleccionado == null) {
	    return;
	}
	if (validarDatosPersonales() == false) {
	    return;
	}

	funcionarioSeleccionado.setOrigenCarga(ORIGEN_CARGA);
	funcionarioSeleccionado.setIesDTO(this.iesSeleccionada);
	funcionarioSeleccionado.setFaseIesDTO(faseIesDTO);

	PersonaDTO perdoc = null;

	try {
	    perdoc = new PersonaDTO();
	    PropertyUtils.copyProperties(perdoc, funcionarioSeleccionado);
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	} catch (InvocationTargetException e1) {
	    e1.printStackTrace();
	} catch (NoSuchMethodException e1) {
	    e1.printStackTrace();
	}

	personaFuncionario = perdoc;
	personaFuncionario.setIesDTO(this.iesSeleccionada);
	PaisDTO nac = new PaisDTO();
	nac.setId(idPais);
	this.personaFuncionario.setPaisOrigen(nac);
	this.personaFuncionario.setDiscapacidad(DiscapacidadEnum
	        .parse(this.discapacidad));
	this.personaFuncionario.setSexo(GeneroEnum.parse(this.genero));
	this.funcionarioSeleccionado.setTipoFuncionario(TipoFuncionarioEnum
	        .parse(this.tipoFunc));

	SedeIesDTO sid = new SedeIesDTO();
	sid.setId(this.idSedeSeleccionada);
	this.funcionarioSeleccionado.setSedeIesDTO(sid);

	personaFuncionario.setIesDTO(this.iesSeleccionada);
	FuncionarioDTO resultado = null;
	try {
	    // if (pestania.equals("Datos Personales")) {

	    if (funcionarioSeleccionado.getId() == null) {
		personaFuncionario.setActivo(true);
		personaFuncionario.setFaseIesDTO(faseIesDTO);
		funcionarioSeleccionado.setActivo(true);
		if (contrataciones.isEmpty()) {
		    JsfUtil.msgAdvert("Ingrese los datos de contratación.");
		    indiceTab = 1;
		    verTabContratacion = false;
		} else {
		    funcionarioSeleccionado.setContratacionDTO(contrataciones);
		    resultado = registroServicio.registrarFuncionario(
			    personaFuncionario, funcionarioSeleccionado,
			    usuarioSistema);
		    RequestContext.getCurrentInstance().execute(
			    "dlgwContrato.hide()");
		    JsfUtil.msgInfo("Registro almacenado correctamente");
		}
	    } else {
		resultado = registroServicio.registrarFuncionario(
		        personaFuncionario, funcionarioSeleccionado,
		        usuarioSistema);
		JsfUtil.msgInfo("Registro almacenado correctamente");
	    }

	    // resultado = guardarPestania();

	} catch (Exception e) {

	    JsfUtil.msgError("No se puede almacenar el registro.");

	}
	if (funcionarioSeleccionado.getId() != null) {
	    activarTab();
	}
    }

    public void guardarContrato() {

	if (valicarCamposContratacion() == false) {
	    return;
	}

	boolean repetido = false;
	boolean nuevo = false;

	for (ContratacionDTO conDTO : contrataciones) {
	    if (conDTO.getNumeroContrato().equalsIgnoreCase(
		    contratoFuncionario.getNumeroContrato())) {
		if (contratoFuncionario.getId() == null) {
		    repetido = true;
		} else {
		    repetido = false;
		}
		break;
	    }
	}

	if (repetido) {
	    JsfUtil.msgAdvert("El número de contrato está duplicado");
	    return;
	} else {
	    clienteId = "";
	}

	contratoFuncionario.setCategoria(CategoriaTitularEnum.parse(categoria));
	contratoFuncionario.setActivo(true);
	contratoFuncionario.setRelacionIes(RelacionIESEnum
	        .parse(relacionSeleccionada));

	try {

	    if (funcionarioSeleccionado.getId() == null) {
		indiceTab = 1;
		contrataciones.add(contratoFuncionario);
		editarContrato = false;
		guardar();
	    } else {
		if (contratoFuncionario.getId() == null) {
		    nuevo = true;
		}
		contratoFuncionario.setFaseIesDTO(faseIesDTO);
		contratoFuncionario = registroServicio
		        .registrarContratacionDTO(contratoFuncionario);

		if (!nuevo) {
		    JsfUtil.msgInfo("Registro actualizado correctamente");
		    RequestContext.getCurrentInstance().execute(
			    "dlgwContrato.hide()");
		    nuevoRedirect();

		} else {
		    JsfUtil.msgInfo("Registro almacenado correctamente");
		    RequestContext.getCurrentInstance().execute(
			    "dlgwContrato.hide()");
		    personaFuncionario.getContratacionDTO().clear();
		    contrataciones.add(contratoFuncionario);
		    personaFuncionario.getContratacionDTO().addAll(
			    contrataciones);
		}
	    }

	    editarContrato = false;

	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrió un error al guardar el registro.");
	}

	// resultado = guardarPestania();

    }

    public List<Integer> getAniosTitulo() {
	List<Integer> aniosTitulo = new ArrayList<>();

	Calendar cal = Calendar.getInstance();
	int anio = cal.get(Calendar.YEAR);
	for (int i = 2014; i >= (anio - 60); i--) {
	    aniosTitulo.add(new Integer(i));
	}
	return aniosTitulo;
    }

    private FuncionarioDTO guardarPestania() {
	FuncionarioDTO resultado = null;

	try {
	    resultado = registroServicio
		    .registrarFuncionario(personaFuncionario,
		            funcionarioSeleccionado, usuarioSistema);
	} catch (ServicioException se) {
	    LOG.info(se.getMessage());
	    JsfUtil.msgAdvert(se.getMessage());
	} catch (RuntimeException re) {
	    LOG.info(re.getMessage());
	    JsfUtil.msgAdvert(re.getMessage());
	} catch (Exception e) {
	    LOG.info(e.getMessage());
	    e.printStackTrace();
	}

	cargarFuncionarios();
	obtenerFuncionario();

	return resultado;
    }

    private void obtenerFuncionario() {
	FuncionarioDTO f = funcionarioSeleccionado;
	for (FuncionarioDTO func : funcionarios) {
	    if (func.getId().equals(f.getId())) {
		funcionarioSeleccionado = func;
		break;
	    }
	}
    }

    private boolean validarDatosPersonales() {

	if (null == idSedeSeleccionada || idSedeSeleccionada.equals(0L)) {
	    JsfUtil.msgError("MATRIZ/EXTENSION es requerido");
	    return false;
	}

	if (funcionarioSeleccionado.getTipoIdentificacion().equals("CEDULA")) {
	    if (!Util
		    .validarCedula(funcionarioSeleccionado.getIdentificacion())) {
		JsfUtil.msgError("Número de cédula es inválido.");
		return false;
	    }
	}

	if (funcionarioSeleccionado.getNombres() == null
	        || funcionarioSeleccionado.getNombres().isEmpty()) {
	    JsfUtil.msgError("Nombres son requeridos");
	    return false;
	}

	if (funcionarioSeleccionado.getApellidoPaterno() == null
	        || funcionarioSeleccionado.getApellidoPaterno().isEmpty()) {
	    JsfUtil.msgError("Primer apellido es requerido");
	    return false;
	}

	if (funcionarioSeleccionado.getIdentificacion() == null
	        || funcionarioSeleccionado.getIdentificacion().isEmpty()) {
	    JsfUtil.msgError("Cédula es requerida");
	    return false;
	}

	// if (funcionarioSeleccionado.getEmailPersonal() != null
	// || !funcionarioSeleccionado.getEmailPersonal().isEmpty()) {
	// if (Util.validarEmail(funcionarioSeleccionado.getEmailPersonal()) ==
	// false) {
	// JsfUtil.msgError("Email Personal incorrecto");
	// return false;
	// }
	// }
	//
	// if (funcionarioSeleccionado.getEmailInstitucional() != null
	// || !funcionarioSeleccionado.getEmailInstitucional().isEmpty()) {
	// if (Util.validarEmail(funcionarioSeleccionado
	// .getEmailInstitucional()) == false) {
	// JsfUtil.msgError("Email Institucional incorrecto");
	// return false;
	// }
	// }

	if (funcionarioSeleccionado.getCargo() == null
	        || funcionarioSeleccionado.getCargo().isEmpty()) {
	    JsfUtil.msgError("Cargo es requerido");
	    return false;
	}

	if (null == tipoFunc || tipoFunc.equals("")) {
	    JsfUtil.msgError("Tipo Funcionario es requerido");
	    return false;
	}

	LOG.info("funcionarioSeleccionado.getId()__"
	        + funcionarioSeleccionado.getId());
	// if (funcionarioSeleccionado.getId() != null) {
	FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
	funcionarioDTO.setIdentificacion(funcionarioSeleccionado
	        .getIdentificacion());
	funcionarioDTO.setIdInformacionIes(this.informacionIesDTO.getId());

	try {

	    funcionarioDTO = registroServicio
		    .obtenerFuncionarioPorCedula(funcionarioDTO);
	    if (funcionarioDTO != null
		    && !(funcionarioDTO.getId().equals(funcionarioSeleccionado
		            .getId()))) {
		JsfUtil.msgError("El número de cédula ingresado ya existe.");
		return false;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al obtener datos de funcionario. Comuníquese con el administrador.");
	    return false;
	}
	// }

	return true;

    }

    private boolean valicarCamposContratacion() {

	if (contratoFuncionario.getActivo() == null) {
	    contratoFuncionario.setActivo(false);
	}

	if (contratoFuncionario.getNumeroContrato() == null
	        || contratoFuncionario.getNumeroContrato().equals("")) {
	    JsfUtil.msgAdvert("Numero de contrato es requerido");
	    return false;
	}

	if (null == relacionSeleccionada || relacionSeleccionada.equals("")) {
	    JsfUtil.msgAdvert("Relacion IES es requerida");
	    return false;
	}

	if (contratoFuncionario.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha inicio es requerida");
	    return false;
	} else {
	    if (contratoFuncionario.getFechaInicio().getTime() > new Date()
		    .getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que fecha actual");
		return false;
	    }
	}

	if (contratoFuncionario.getFechaInicio() != null
	        && contratoFuncionario.getFechaFin() != null) {
	    if (contratoFuncionario.getFechaInicio().getTime() > contratoFuncionario
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que Fecha fin");
		return false;
	    }
	}

	return true;
    }

    public void nuevoContrato(ActionEvent event) {
	this.contratoFuncionario = new ContratacionDTO();
	this.contratoFuncionario.getPersonaDTO().setId(
	        this.funcionarioSeleccionado.getId());
	editarContrato = true;

	FacesContext context = FacesContext.getCurrentInstance();
	this.clienteId = event.getComponent().getClientId(context);
    }

    public void editarContrato() {
	relacionSeleccionada = contratoFuncionario.getRelacionIes().getValue();
    }

    public void cancelarContrato() {
	this.contratoFuncionario = new ContratacionDTO();
	this.editarContrato = false;
    }

    public void tomarBotonGuardarDialogos(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();

	msgAccion = "";
	String idBotonGuardar = null;
	idBotonGuardar = event.getComponent().getClientId(context);

	if (idBotonGuardar.contains("btnBuscar")) {
	    msgAccion = "buscando funcionario";
	    return;
	} else {
	    msgAccion = "";
	    return;
	}
    }

    public void seleccionarCategoria(ValueChangeEvent e) {
	if (e != null && e.getNewValue() != null) {
	    contratoFuncionario.setCategoria(CategoriaTitularEnum.parse(e
		    .getNewValue().toString()));
	}

    }

    public void seleccionarTiempoDedicacion(ValueChangeEvent e) {
	if (e != null && e.getNewValue() != null) {
	    contratoFuncionario.setTiempoDedicacion(TiempoDedicacionEnum
		    .parse(e.getNewValue().toString()));
	}

    }

    public void seleccionarNivelTitulo(ValueChangeEvent e) {
	if (e != null && e.getNewValue() != null) {
	    idNivelTitulo = new Long(e.getNewValue().toString());
	}
    }

    public void seleccionarCursandoFormacion(ValueChangeEvent e) {
	if (e != null) {
	    if (new Boolean(e.getNewValue().toString())) {
		return;
	    }
	}

    }

    public void modificarIndice(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	String botonId = event.getComponent().getClientId(context).toString();
	if (botonId.contains("btnAtras")) {
	    if (listaFuncionariosTodos.size() < registros) {
		indiceAtras = indice - funcionarios.size();
	    } else {
		indiceAtras = indice - registros;
	    }
	    indice = indiceAtras;
	    indiceAtras = 0;
	    habilitarSiguiente = true;
	}

	if (botonId.contains("btnSiguiente")) {
	    if (listaFuncionariosTodos.size() < registros) {
		indice = 0;
	    } else {
		indiceSiguiente = indice + registros;
		indice = indiceSiguiente;
	    }
	    indiceSiguiente = 0;
	    if ((registros + indice) > numRegistros) {
		numRegistros = numRegistros + registros;
	    }
	}

    }

    public void tomarRango(ValueChangeEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	registros = ((Integer) event.getNewValue()).intValue();
	// indice = indice - registros;
	indice = 0;
	numRegistros = 0;
	listaFuncionariosTodos.clear();
	cargarFuncionarios();
	if (registros == 0) {
	    funcionarios.clear();
	    funcionariosFiltros.clear();
	}

	context.renderResponse();
    }

    public void cargarSedeIes() {

	try {
	    LOG.info("informacionIesDTO: " + informacionIesDTO.getId());
	    this.listaSedeIesDto.clear();
	    this.listaSedeIesDto = registroServicio
		    .obtenerSedesIes(this.informacionIesDTO.getId());
	    LOG.info("listaSedeIesDto.size: " + listaSedeIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    public FaseDTO getFaseEvaluacionDTO() {

	return faseEvaluacionDTO;
    }

    public void setFaseEvaluacionDTO(FaseDTO faseEvaluacionDTO) {
	this.faseEvaluacionDTO = faseEvaluacionDTO;
    }

    public InformacionIesDTO getInformacionIesDTO() {

	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	try {
	    // carreraServicio
	    // .obtenerInformacionCarreraPorCarrera(carrerasCarrera);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

    public boolean isEditarContrato() {
	return editarContrato;
    }

    public void setEditarContrato(boolean editarContrato) {
	this.editarContrato = editarContrato;
    }

    /**
     * @return the idArea
     */
    public Long getIdArea() {
	return idArea;
    }

    /**
     * @return the areasConocimiento
     */
    public List<AreaConocimientoDTO> getAreasConocimiento() {
	return areasConocimiento;
    }

    /**
     * @param idArea
     *            the idArea to set
     */
    public void setIdArea(Long idArea) {
	this.idArea = idArea;
    }

    /**
     * @param areasConocimiento
     *            the areasConocimiento to set
     */
    public void setAreasConocimiento(List<AreaConocimientoDTO> areasConocimiento) {
	this.areasConocimiento = areasConocimiento;
    }

    /**
     * @return the idSubArea
     */
    public Long getIdSubArea() {
	return idSubArea;
    }

    /**
     * @return the subareas
     */
    public List<SubAreaConocimientoDTO> getSubareas() {
	return subareas;
    }

    /**
     * @param idSubArea
     *            the idSubArea to set
     */
    public void setIdSubArea(Long idSubArea) {
	this.idSubArea = idSubArea;
    }

    /**
     * @param subareas
     *            the subareas to set
     */
    public void setSubareas(List<SubAreaConocimientoDTO> subareas) {
	this.subareas = subareas;
    }

    /**
     * @return the idNivelTitulo
     */
    public Long getIdNivelTitulo() {
	return idNivelTitulo;
    }

    /**
     * @param idNivelTitulo
     *            the idNivelTitulo to set
     */
    public void setIdNivelTitulo(Long idNivelTitulo) {
	this.idNivelTitulo = idNivelTitulo;
    }

    /**
     * @return the categoria
     */
    public String getCategoria() {
	return categoria;
    }

    /**
     * @param categoria
     *            the categoria to set
     */
    public void setCategoria(String categoria) {
	this.categoria = categoria;
    }

    /**
     * @return the carreraSeleccionada
     */
    public CarreraIesDTO getCarreraSeleccionada() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	carreraSeleccionada = controller.getCarrera();

	return carreraSeleccionada;
    }

    /**
     * @param carreraSeleccionada
     *            the carreraSeleccionada to set
     */
    public void setCarreraSeleccionada(CarreraIesDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    /**
     * @return the relacionesIes
     */
    public List<String> getRelacionesIes() {
	return relacionesIes;
    }

    /**
     * @param relacionesIes
     *            the relacionesIes to set
     */
    public void setRelacionesIes(List<String> relacionesIes) {
	this.relacionesIes = relacionesIes;
    }

    /**
     * @return the tiemposDedicacion
     */
    public List<String> getTiemposDedicacion() {
	return tiemposDedicacion;
    }

    /**
     * @param tiemposDedicacion
     *            the tiemposDedicacion to set
     */
    public void setTiemposDedicacion(List<String> tiemposDedicacion) {
	this.tiemposDedicacion = tiemposDedicacion;
    }

    /**
     * @return the categorias
     */
    public List<String> getCategorias() {
	return categorias;
    }

    /**
     * @param categorias
     *            the categorias to set
     */
    public void setCategorias(List<String> categorias) {
	this.categorias = categorias;
    }

    /**
     * @return the fechaMin
     */
    public Date getFechaMin() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 75;
	cal.set(anio, Calendar.JANUARY, 1);
	fechaMin = new Date(cal.getTimeInMillis());
	return fechaMin;
    }

    /**
     * @param fechaMin
     *            the fechaMin to set
     */
    public void setFechaMin(Date fechaMin) {
	this.fechaMin = fechaMin;
    }

    /**
     * @return the fechaMax
     */

    public Date getFechaMax() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 21;
	cal.set(anio, Calendar.JANUARY, 365);
	fechaMax = new Date(cal.getTimeInMillis());
	return fechaMax;
    }

    /**
     * @param fechaMax
     *            the fechaMax to set
     */
    public void setFechaMax(Date fechaMax) {
	this.fechaMax = fechaMax;
    }

    /**
     * @return the fechaActual
     */
    public Date getFechaActual() {
	Calendar cal = Calendar.getInstance();
	fechaActual = new Date(cal.getTimeInMillis());
	return fechaActual;
    }

    /**
     * @param fechaActual
     *            the fechaActual to set
     */
    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    /**
     * @return the idPaisFormacion
     */
    public Long getIdPaisFormacion() {
	return idPaisFormacion;
    }

    /**
     * @param idPaisFormacion
     *            the idPaisFormacion to set
     */
    public void setIdPaisFormacion(Long idPaisFormacion) {
	this.idPaisFormacion = idPaisFormacion;
    }

    /**
     * @return the msgAccion
     */
    public String getMsgAccion() {
	return msgAccion;
    }

    /**
     * @param msgAccion
     *            the msgAccion to set
     */
    public void setMsgAccion(String msgAccion) {
	this.msgAccion = msgAccion;
    }

    /**
     * @return the contrataciones
     */
    public List<ContratacionDTO> getContrataciones() {
	return contrataciones;
    }

    /**
     * @param contrataciones
     *            the contrataciones to set
     */
    public void setContrataciones(List<ContratacionDTO> contrataciones) {
	this.contrataciones = contrataciones;
    }

    /**
     * @return the idPais
     */
    public Long getIdPais() {
	return idPais;
    }

    /**
     * @param idPais
     *            the idPais to set
     */
    public void setIdPais(Long idPais) {
	this.idPais = idPais;
    }

    /**
     * @return the paises
     */
    public List<PaisDTO> getPaises() {
	return paises;
    }

    /**
     * @param paises
     *            the paises to set
     */
    public void setPaises(List<PaisDTO> paises) {
	this.paises = paises;
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
     * @return the verTabDatosPersonales
     */
    public boolean isVerTabDatosPersonales() {
	return verTabDatosPersonales;
    }

    /**
     * @param verTabDatosPersonales
     *            the verTabDatosPersonales to set
     */
    public void setVerTabDatosPersonales(boolean verTabDatosPersonales) {
	this.verTabDatosPersonales = verTabDatosPersonales;
    }

    /**
     * @return the verTabContratacion
     */
    public boolean isVerTabContratacion() {
	return verTabContratacion;
    }

    /**
     * @param verTabContratacion
     *            the verTabContratacion to set
     */
    public void setVerTabContratacion(boolean verTabContratacion) {
	this.verTabContratacion = verTabContratacion;
    }

    /**
     * @return the clienteId
     */
    public String getClienteId() {
	return clienteId;
    }

    /**
     * @param clienteId
     *            the clienteId to set
     */
    public void setClienteId(String clienteId) {
	this.clienteId = clienteId;
    }

    /**
     * @return the accion
     */
    public String getAccion() {
	return accion;
    }

    /**
     * @param accion
     *            the accion to set
     */
    public void setAccion(String accion) {
	this.accion = accion;
    }

    /**
     * @return the verBtnIesDoc
     */
    public boolean isVerBtnIesDoc() {
	return verBtnIesDoc;
    }

    /**
     * @param verBtnIesDoc
     *            the verBtnIesDoc to set
     */
    public void setVerBtnIesDoc(boolean verBtnIesDoc) {
	this.verBtnIesDoc = verBtnIesDoc;
    }

    /**
     * @return the verBtnIesCon
     */
    public boolean isVerBtnIesCon() {
	return verBtnIesCon;
    }

    /**
     * @param verBtnIesCon
     *            the verBtnIesCon to set
     */
    public void setVerBtnIesCon(boolean verBtnIesCon) {
	this.verBtnIesCon = verBtnIesCon;
    }

    /**
     * @return the verBtnIesFor
     */
    public boolean isVerBtnIesFor() {
	return verBtnIesFor;
    }

    /**
     * @param verBtnIesFor
     *            the verBtnIesFor to set
     */
    public void setVerBtnIesFor(boolean verBtnIesFor) {
	this.verBtnIesFor = verBtnIesFor;
    }

    /**
     * @return the pestania
     */
    public String getPestania() {
	return pestania;
    }

    /**
     * @param pestania
     *            the pestania to set
     */
    public void setPestania(String pestania) {
	this.pestania = pestania;
    }

    /**
     * @return the iesFormacion
     */
    public IesDTO getIesFormacion() {
	return iesFormacion;
    }

    /**
     * @param iesFormacion
     *            the iesFormacion to set
     */
    public void setIesFormacion(IesDTO iesFormacion) {
	this.iesFormacion = iesFormacion;
    }

    /**
     * @return the verTablaIes
     */
    public boolean isVerTablaIes() {
	return verTablaIes;
    }

    /**
     * @param verTablaIes
     *            the verTablaIes to set
     */
    public void setVerTablaIes(boolean verTablaIes) {
	this.verTablaIes = verTablaIes;
    }

    /**
     * @return the genero
     */
    public String getGenero() {
	return genero;
    }

    /**
     * @param genero
     *            the genero to set
     */
    public void setGenero(String genero) {
	this.genero = genero;
    }

    /**
     * @return the generos
     */
    public List<String> getGeneros() {
	return generos;
    }

    /**
     * @param generos
     *            the generos to set
     */
    public void setGeneros(List<String> generos) {
	this.generos = generos;
    }

    /**
     * @return the discapacidad
     */
    public String getDiscapacidad() {
	return discapacidad;
    }

    /**
     * @param discapacidad
     *            the discapacidad to set
     */
    public void setDiscapacidad(String discapacidad) {
	this.discapacidad = discapacidad;
    }

    /**
     * @return the discapacidades
     */
    public List<String> getDiscapacidades() {
	return discapacidades;
    }

    /**
     * @param discapacidades
     *            the discapacidades to set
     */
    public void setDiscapacidades(List<String> discapacidades) {
	this.discapacidades = discapacidades;
    }

    /**
     * @return the iesFiltros
     */
    public List<IesDTO> getIesFiltros() {
	return iesFiltros;
    }

    /**
     * @param iesFiltros
     *            the iesFiltros to set
     */
    public void setIesFiltros(List<IesDTO> iesFiltros) {
	this.iesFiltros = iesFiltros;
    }

    /**
     * @return the listaIes
     */
    public List<IesDTO> getListaIes() {
	iesFiltros.clear();
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
     * @return the funcionarios
     */
    public List<FuncionarioDTO> getFuncionarios() {

	return funcionarios;
    }

    /**
     * Seteo de ies
     * 
     * @param iesSeleccionada
     *            the iesSeleccionada to set
     */
    public void setIesSeleccionada(IesDTO iesSeleccionada) {
	this.iesSeleccionada = iesSeleccionada;
    }

    /**
     * @return the iesSeleccionada
     */
    public IesDTO getIesSeleccionada() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	iesSeleccionada = controller.getIes();
	return iesSeleccionada;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public int getIndice() {
	return indice;
    }

    public void setIndice(int indice) {
	this.indice = indice;
    }

    public int getRegistros() {
	return registros;
    }

    public void setRegistros(int registros) {
	this.registros = registros;
    }

    public int getIndiceAtras() {
	return indiceAtras;
    }

    public void setIndiceAtras(int indiceAtras) {
	this.indiceAtras = indiceAtras;
    }

    public int getIndiceSiguiente() {
	return indiceSiguiente;
    }

    public void setIndiceSiguiente(int indiceSiguiente) {
	this.indiceSiguiente = indiceSiguiente;
    }

    public int getNumRegistros() {
	return numRegistros;
    }

    public void setNumRegistros(int numRegistros) {
	this.numRegistros = numRegistros;
    }

    public boolean isBusqueda() {
	return busqueda;
    }

    public void setBusqueda(boolean busqueda) {
	this.busqueda = busqueda;
    }

    public Integer[] getRangos() {
	return rangos;
    }

    public int getContador() {
	return contador;
    }

    public void setContador(int contador) {
	this.contador = contador;
    }

    public void setFuncionarios(List<FuncionarioDTO> funcionarios) {
	this.funcionarios = funcionarios;
    }

    public List<FuncionarioDTO> getFuncionariosFiltros() {
	return funcionariosFiltros;
    }

    public void setFuncionariosFiltros(List<FuncionarioDTO> funcionariosFiltros) {
	this.funcionariosFiltros = funcionariosFiltros;
    }

    public List<FuncionarioDTO> getListaFuncionariosTodos() {
	return listaFuncionariosTodos;
    }

    public void setListaFuncionariosTodos(
	    List<FuncionarioDTO> listaFuncionariosTodos) {
	this.listaFuncionariosTodos = listaFuncionariosTodos;
    }

    public FuncionarioDTO getFuncionarioSeleccionado() {
	return funcionarioSeleccionado;
    }

    public void setFuncionarioSeleccionado(
	    FuncionarioDTO funcionarioSeleccionado) {
	this.funcionarioSeleccionado = funcionarioSeleccionado;
	try {
	    this.funcionarioSeleccionado = registroServicio
		    .obtenerFuncionarioPorId(funcionarioSeleccionado.getId());
	    this.pestania = "Datos Personales";
	    activarTab();
	    editarContrato = false;
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ocurrió un error al consultar el funcionario.");
	}
    }

    public PersonaDTO getPersonaFuncionario() {
	return personaFuncionario;
    }

    public void setPersonaFuncionario(PersonaDTO personaFuncionario) {
	this.personaFuncionario = personaFuncionario;
    }

    public ContratacionDTO getContratoFuncionario() {
	return contratoFuncionario;
    }

    public void setContratoFuncionario(ContratacionDTO contratoFuncionario) {
	this.contratoFuncionario = contratoFuncionario;
	this.contratoFuncionario.setPersonaDTO(funcionarioSeleccionado);
    }

    public List<String> getTipoFuncionario() {
	return tipoFuncionario;
    }

    public void setTipoFuncionario(List<String> tipoFuncionario) {
	this.tipoFuncionario = tipoFuncionario;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
    }

    public String getTipoFunc() {
	return tipoFunc;
    }

    public void setTipoFunc(String tipoFunc) {
	this.tipoFunc = tipoFunc;
    }

    public String getCedulaFuncionario() {
	return cedulaFuncionario;
    }

    public void setCedulaFuncionario(String cedulaFuncionario) {
	this.cedulaFuncionario = cedulaFuncionario;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public Integer getNumeroFuncionarios() {
	return numeroFuncionarios;
    }

    public void setNumeroFuncionarios(Integer numeroFuncionarios) {
	this.numeroFuncionarios = numeroFuncionarios;
    }

    public String getRelacionSeleccionada() {
	return relacionSeleccionada;
    }

    public void setRelacionSeleccionada(String relacionSeleccionada) {
	this.relacionSeleccionada = relacionSeleccionada;
    }

    public boolean isHabilitarSiguiente() {
	return habilitarSiguiente;
    }

    public void setHabilitarSiguiente(boolean habilitarSiguiente) {
	this.habilitarSiguiente = habilitarSiguiente;
    }

}
