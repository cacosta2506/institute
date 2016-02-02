/*
 * 
 * Desarrollado por: eteran
 * 
 * Copyright (c) 2013-2014 CEAACESS All Rights Reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.FilterEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturasPorNivelDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.OrganizacionCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.RequisitoAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.AreaFormacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.AreaFormacionOrganizacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.OrganizacionMallaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoAsignaturaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoRequisitoMallaEnum;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

/**
 * Class description goes here.
 * 
 * @author eteran
 * 
 */
public class MallasController implements Serializable {

    /**
     * classVar1 documentation comment serialVersionUID...
     */

    private static final long serialVersionUID = 6732728257389169868L;

    private boolean verTablaHijas;
    private boolean verNuevaMalla;
    private boolean esCopiaMalla;
    private Double creditosAsignatura;
    private Double totalCreditos;
    private Double totalCreditosHijas;
    private String usuarioSistema;
    private String tipoAsignatura;
    private Long idSede;
    private Long idSedeImportacion;

    private static final SelectItem[] ORGANIZACION_MALLA = {
	    new SelectItem(
	            OrganizacionMallaEnum.BIMESTRE.getValue().toString(),
	            "Bimestres"),
	    new SelectItem(OrganizacionMallaEnum.TRIMESTRES.getValue()
	            .toString(), "Trimestres"),
	    new SelectItem(OrganizacionMallaEnum.CUATRIMESTRES.getValue()
	            .toString(), "Cuatrimestres"),
	    new SelectItem(OrganizacionMallaEnum.SEMESTRES.getValue()
	            .toString(), "Semestres"),
	    new SelectItem(OrganizacionMallaEnum.ANIOS.getValue().toString(),
	            "Años") };
    private final List<SelectItem> niveles = new ArrayList<>();

    private IesDTO ies;
    private InformacionIesDTO infoIes;
    private InformacionCarreraDTO carreraSeleccionada;
    private InformacionCarreraDTO carreraSeleccionadaImportacion;
    private List<SedeIesDTO> sedesImportacion = new ArrayList<>();
    private MallaCurricularDTO mallaSeleccionada;
    private MallaCurricularDTO mallaCrearEditar = new MallaCurricularDTO();
    private AsignaturaDTO materiaSeleccionada;
    private AsignaturaDTO preSeleccionado;
    private AsignaturaDTO coSeleccionado;
    private RequisitoAsignaturaDTO reqSeleccionado;

    private List<InformacionCarreraDTO> carreras;
    private List<InformacionCarreraDTO> carrerasImportacion;
    private List<MallaCurricularDTO> mallas;
    private List<MallaCurricularDTO> mallasImportacion = new ArrayList<>();
    private List<AsignaturaDTO> materias;
    private List<AsignaturaDTO> preRequisitos;
    private List<AsignaturaDTO> coRequisitos;
    private List<RequisitoAsignaturaDTO> requisitos;
    private List<RequisitoAsignaturaDTO> requisitosRemovidos;
    private List<OrganizacionCurricularDTO> organizacionCurricular;
    private List<AsignaturaDTO> materiasFiltros;
    private List<AsignaturaDTO> preRequisitosFiltros;
    private List<AsignaturaDTO> coRequisitosFiltros;
    private String organizacionMallaSelect = "0";
    private String numeroNivelesSelect = "0";

    private String nombreComponente;
    private Integer opcionRequisito;
    private AsignaturaDTO asignatura;
    private AsignaturaDTO hijaAsignatura;
    private AsignaturaDTO padreAsignatura;
    private List<AsignaturaDTO> hijas;

    private List<String> tiposAsignatura;
    private List<String> areasDeFormacion;
    private List<Integer> nivelesMallaSeleccionada;

    private Map<String, String> filtrosTablaAsignaturas;
    private DataTable tablaAsignaturas;
    private byte[] reporteBytes = null;
    private FaseIesDTO faseIesDTO = new FaseIesDTO();
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;
    private String perfil;

    @Autowired
    private RegistroServicio registroServicio;

    public MallasController() {

	ies = new IesDTO();
	infoIes = new InformacionIesDTO();
	carreraSeleccionada = new InformacionCarreraDTO();
	mallaSeleccionada = new MallaCurricularDTO();
	materiaSeleccionada = new AsignaturaDTO();
	preSeleccionado = new AsignaturaDTO();
	coSeleccionado = new AsignaturaDTO();
	reqSeleccionado = new RequisitoAsignaturaDTO();

	carreras = new ArrayList<>();
	mallas = new ArrayList<>();
	materias = new ArrayList<>();
	preRequisitos = new ArrayList<>();
	coRequisitos = new ArrayList<>();
	requisitos = new ArrayList<>();
	requisitosRemovidos = new ArrayList<>();
	materiasFiltros = new ArrayList<>();
	preRequisitosFiltros = new ArrayList<>();
	coRequisitosFiltros = new ArrayList<>();

	padreAsignatura = new AsignaturaDTO();
	hijaAsignatura = new AsignaturaDTO();
	asignatura = new AsignaturaDTO();
	tiposAsignatura = new ArrayList<>();
	hijas = new ArrayList<>();
	areasDeFormacion = new ArrayList<>();
	organizacionCurricular = new ArrayList<>();
	nivelesMallaSeleccionada = new ArrayList<>();

	filtrosTablaAsignaturas = new HashMap<>();
	tablaAsignaturas = new DataTable();

    }

    /**
     * @return the tablaAsignaturas
     */
    public DataTable getTablaAsignaturas() {
	return tablaAsignaturas;
    }

    /**
     * @param tablaAsignaturas
     *            the tablaAsignaturas to set
     */
    public void setTablaAsignaturas(DataTable tablaAsignaturas) {
	this.tablaAsignaturas = tablaAsignaturas;
    }

    /**
     * @return the filtrosTablaAsignaturas
     */
    public Map<String, String> getFiltrosTablaAsignaturas() {
	return filtrosTablaAsignaturas;
    }

    /**
     * @param filtrosTablaAsignaturas
     *            the filtrosTablaAsignaturas to set
     */
    public void setFiltrosTablaAsignaturas(
	    Map<String, String> filtrosTablaAsignaturas) {
	this.filtrosTablaAsignaturas = filtrosTablaAsignaturas;
    }

    /**
     * @return the tipoAsignatura
     */
    public String getTipoAsignatura() {
	return tipoAsignatura;
    }

    /**
     * @param tipoAsignatura
     *            the tipoAsignatura to set
     */
    public void setTipoAsignatura(String tipoAsignatura) {
	this.tipoAsignatura = tipoAsignatura;
    }

    /**
     * @return the verTablaHijas
     */
    public boolean isVerTablaHijas() {
	return verTablaHijas;
    }

    /**
     * @param verTablaHijas
     *            the verTablaHijas to set
     */
    public void setVerTablaHijas(boolean verTablaHijas) {
	this.verTablaHijas = verTablaHijas;
    }

    /**
     * @return the creditosAsignatura
     */
    public Double getCreditosAsignatura() {
	return creditosAsignatura;
    }

    /**
     * @param creditosAsignatura
     *            the creditosAsignatura to set
     */
    public void setCreditosAsignatura(Double creditosAsignatura) {
	this.creditosAsignatura = creditosAsignatura;
    }

    /**
     * @return the verNuevaMalla
     */
    public boolean isVerNuevaMalla() {
	return verNuevaMalla;
    }

    /**
     * @param verNuevaMalla
     *            the verNuevaMalla to set
     */
    public void setVerNuevaMalla(boolean verNuevaMalla) {
	this.verNuevaMalla = verNuevaMalla;
    }

    /**
     * @param nivelesMallaSeleccionada
     *            the nivelesMallaSeleccionada to set
     */
    public void setNivelesMallaSeleccionada(
	    List<Integer> nivelesMallaSeleccionada) {
	this.nivelesMallaSeleccionada = nivelesMallaSeleccionada;
    }

    /**
     * @return the nivelesMallaSeleccionada
     */
    public List<Integer> getNivelesMallaSeleccionada() {
	return nivelesMallaSeleccionada;
    }

    /**
     * @return the totalCreditosHijas
     */
    public Double getTotalCreditosHijas() {
	return totalCreditosHijas;
    }

    /**
     * @param totalCreditosHijas
     *            the totalCreditosHijas to set
     */
    public void setTotalCreditosHijas(Double totalCreditosHijas) {
	this.totalCreditosHijas = totalCreditosHijas;
    }

    /**
     * @return the totalCreditos
     */
    public Double getTotalCreditos() {
	return totalCreditos;
    }

    /**
     * @param totalCreditos
     *            the totalCreditos to set
     */
    public void setTotalCreditos(Double totalCreditos) {
	this.totalCreditos = totalCreditos;
    }

    public void pasarMalla(MallaCurricularDTO malla1) {
	ReporteController iController = (ReporteController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("reporteController");
	iController.setMalla(malla1);
	iController.generarReporte();
    }

    /**
     * @return the padreAsignatura
     */
    public AsignaturaDTO getPadreAsignatura() {
	return padreAsignatura;
    }

    /**
     * @param padreAsignatura
     *            the padreAsignatura to set
     */
    public void setPadreAsignatura(AsignaturaDTO padreAsignatura) {
	this.padreAsignatura = padreAsignatura;

	if (TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue().equals(
	        padreAsignatura.getTipoAsignatura())) {
	    obtenerAsignatura(false);
	    validarCreditosCompuesta();
	}
    }

    /**
     * @return the requisitosRemovidos
     */
    public List<RequisitoAsignaturaDTO> getRequisitosRemovidos() {
	return requisitosRemovidos;
    }

    /**
     * @param requisitosRemovidos
     *            the requisitosRemovidos to set
     */
    public void setRequisitosRemovidos(
	    List<RequisitoAsignaturaDTO> requisitosRemovidos) {
	this.requisitosRemovidos = requisitosRemovidos;
    }

    /**
     * @return the idSede
     */
    public Long getIdSede() {
	return idSede;
    }

    /**
     * @param idSede
     *            the idSede to set
     */
    public void setIdSede(Long idSede) {
	this.idSede = idSede;
    }

    /**
     * @return the preRequisitosFiltros
     */
    public List<AsignaturaDTO> getPreRequisitosFiltros() {
	return preRequisitosFiltros;
    }

    /**
     * @return the coRequisitosFiltros
     */
    public List<AsignaturaDTO> getCoRequisitosFiltros() {
	return coRequisitosFiltros;
    }

    /**
     * @param preRequisitosFiltros
     *            the preRequisitosFiltros to set
     */
    public void setPreRequisitosFiltros(List<AsignaturaDTO> preRequisitosFiltros) {
	this.preRequisitosFiltros = preRequisitosFiltros;
    }

    /**
     * @param coRequisitosFiltros
     *            the coRequisitosFiltros to set
     */
    public void setCoRequisitosFiltros(List<AsignaturaDTO> coRequisitosFiltros) {
	this.coRequisitosFiltros = coRequisitosFiltros;
    }

    /**
     * @return the materiasFiltros
     */
    public List<AsignaturaDTO> getMateriasFiltros() {
	return materiasFiltros;
    }

    /**
     * @param materiasFiltros
     *            the materiasFiltros to set
     */
    public void setMateriasFiltros(List<AsignaturaDTO> materiasFiltros) {
	this.materiasFiltros = materiasFiltros;
    }

    /**
     * @return the reqSeleccionado
     */
    public RequisitoAsignaturaDTO getReqSeleccionado() {
	return reqSeleccionado;
    }

    /**
     * @param reqSeleccionado
     *            the reqSeleccionado to set
     */
    public void setReqSeleccionado(RequisitoAsignaturaDTO reqSeleccionado) {
	this.reqSeleccionado = reqSeleccionado;
    }

    /**
     * @return the requisitos
     */
    public List<RequisitoAsignaturaDTO> getRequisitos() {
	return requisitos;
    }

    /**
     * @param requisitos
     *            the requisitos to set
     */
    public void setRequisitos(List<RequisitoAsignaturaDTO> requisitos) {
	this.requisitos = requisitos;
    }

    /**
     * @return the preSeleccionado
     */
    public AsignaturaDTO getPreSeleccionado() {
	return preSeleccionado;
    }

    /**
     * @return the coSeleccionado
     */
    public AsignaturaDTO getCoSeleccionado() {
	return coSeleccionado;
    }

    /**
     * @param preSeleccionado
     *            the preSeleccionado to set
     */
    public void setPreSeleccionado(AsignaturaDTO preSeleccionado) {
	this.preSeleccionado = preSeleccionado;
    }

    /**
     * @param coSeleccionado
     *            the coSeleccionado to set
     */
    public void setCoSeleccionado(AsignaturaDTO coSeleccionado) {
	this.coSeleccionado = coSeleccionado;
    }

    /**
     * @return the materiaSeleccionada
     */
    public AsignaturaDTO getMateriaSeleccionada() {
	return materiaSeleccionada;
    }

    /**
     * @param materiaSeleccionada
     *            the materiaSeleccionada to set
     */
    public void setMateriaSeleccionada(AsignaturaDTO materiaSeleccionada) {
	this.materiaSeleccionada = materiaSeleccionada;
    }

    /**
     * @return the preRequisitos
     */
    public List<AsignaturaDTO> getPreRequisitos() {
	return preRequisitos;
    }

    /**
     * @return the coRequisitos
     */
    public List<AsignaturaDTO> getCoRequisitos() {
	return coRequisitos;
    }

    /**
     * @param preRequisitos
     *            the preRequisitos to set
     */
    public void setPreRequisitos(List<AsignaturaDTO> preRequisitos) {
	this.preRequisitos = preRequisitos;
    }

    /**
     * @param coRequisitos
     *            the coRequisitos to set
     */
    public void setCoRequisitos(List<AsignaturaDTO> coRequisitos) {
	this.coRequisitos = coRequisitos;
    }

    /**
     * @return the mallaSeleccionada
     */
    public MallaCurricularDTO getMallaSeleccionada() {
	return mallaSeleccionada;
    }

    /**
     * @param mallaSeleccionada
     *            the mallaSeleccionada to set
     */
    public void setMallaSeleccionada(MallaCurricularDTO mallaSeleccionada) {
	this.mallaSeleccionada = mallaSeleccionada;

	nivelesMallaSeleccionada.clear();

	for (int i = 0; i < this.mallaSeleccionada.getNumeroNiveles(); i++) {
	    nivelesMallaSeleccionada.add(i + 1);
	}
    }

    /**
     * @return the carreraSeleccionada
     */
    public InformacionCarreraDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    /**
     * @param carreraSeleccionada
     *            the carreraSeleccionada to set
     */
    public void setCarreraSeleccionada(InformacionCarreraDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    /**
     * @return the ies
     */
    public IesDTO getIes() {
	return ies;
    }

    /**
     * @return the infoIes
     */
    public InformacionIesDTO getInfoIes() {
	return infoIes;
    }

    /**
     * @param ies
     *            the ies to set
     */
    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    /**
     * @param infoIes
     *            the infoIes to set
     */
    public void setInfoIes(InformacionIesDTO infoIes) {
	this.infoIes = infoIes;
    }

    /**
     * @return the carreras
     */
    public List<InformacionCarreraDTO> getCarreras() {
	return carreras;
    }

    /**
     * @return the mallas
     */
    public List<MallaCurricularDTO> getMallas() {
	return mallas;
    }

    /**
     * @return the materias
     */
    public List<AsignaturaDTO> getMaterias() {
	return materias;
    }

    /**
     * @param carreras
     *            the carreras to set
     */
    public void setCarreras(List<InformacionCarreraDTO> carreras) {
	this.carreras = carreras;
    }

    /**
     * @param mallas
     *            the mallas to set
     */
    public void setMallas(List<MallaCurricularDTO> mallas) {
	this.mallas = mallas;
    }

    /**
     * @param materias
     *            the materias to set
     */
    public void setMaterias(List<AsignaturaDTO> materias) {
	this.materias = materias;
    }

    /**
     * @return the opcionRequisito
     */
    public Integer getOpcionRequisito() {
	return opcionRequisito;
    }

    /**
     * @return the asignatura
     */
    public AsignaturaDTO getAsignatura() {
	return asignatura;
    }

    /**
     * @return the hijaAsignatura
     */
    public AsignaturaDTO getHijaAsignatura() {
	return hijaAsignatura;
    }

    /**
     * @return the hijas
     */
    public List<AsignaturaDTO> getHijas() {
	return hijas;
    }

    /**
     * @return the tiposAsignatura
     */
    public List<String> getTiposAsignatura() {
	return tiposAsignatura;
    }

    public void importarMalla() {
	idSedeImportacion = null;
	carreraSeleccionadaImportacion = null;
	mallasImportacion.clear();
	sedesImportacion = new ArrayList<>();
	sedesImportacion = infoIes.getSedeIes();
	// for (SedeIesDTO sede : infoIes.getSedeIes()) {
	// if (!sede.getId().equals(idSede)) {
	// sedesImportacion.add((SedeIesDTO) SerializationUtils
	// .clone(sede));
	// }
	// }
    }

    /**
     * @param opcionRequisito
     *            the opcionRequisito to set...
     */
    public void setOpcionRequisito(Integer opcionRequisito) {
	this.opcionRequisito = opcionRequisito;

	if (TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue().equals(
	        tipoAsignatura)) {
	    obtenerAsignatura(false);
	}

	if (this.opcionRequisito.equals(1)) {

	    verTablaHijas = false;
	    asignatura
		    .setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_SIN_CREDITOS
		            .getValue());

	    if (!hijas.isEmpty()) {
		JsfUtil.msgAdvert("Al Guardar una " + tipoAsignatura + " como "
		        + asignatura.getTipoAsignatura()
		        + " se eliminarán las asignaturas hijas");
	    }
	}

	if (this.opcionRequisito.equals(2)) {

	    verTablaHijas = false;
	    asignatura
		    .setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_CON_CREDITOS
		            .getValue());

	    if (!hijas.isEmpty()) {
		JsfUtil.msgAdvert("Al Guardar una " + tipoAsignatura + " como "
		        + asignatura.getTipoAsignatura()
		        + " se eliminarán las asignaturas hijas");
	    }
	}

	if (asignatura.getId() == null) {
	    hijas.clear();
	    totalCreditosHijas = 0.0;
	}

	if (this.opcionRequisito.equals(3)) {
	    verTablaHijas = true;
	    asignatura
		    .setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_COMPUESTA
		            .getValue());
	}

    }

    /**
     * @param asignatura
     *            the asignatura to set
     */
    public void setAsignatura(AsignaturaDTO asignatura) {
	this.asignatura = (AsignaturaDTO) SerializationUtils.clone(asignatura);

	if (asignatura != null && asignatura.getId() != null) {
	    for (int i = 0; i < tiposAsignatura.size(); i++) {
		if (tiposAsignatura.get(i).equals(
		        asignatura.getTipoAsignatura())) {
		    setOpcionRequisito(i + 1);
		    break;
		}
	    }
	}

    }

    /**
     * @param hijaAsignatura
     *            the hijaAsignatura to set
     */
    public void setHijaAsignatura(AsignaturaDTO hijaAsignatura) {
	this.hijaAsignatura = hijaAsignatura;
    }

    /**
     * @param hijas
     *            the hijas to set
     */
    public void setHijas(List<AsignaturaDTO> hijas) {
	this.hijas = hijas;
    }

    /**
     * @param tiposAsignatura
     *            the tiposAsignatura to set
     */
    public void setTiposAsignatura(List<String> tiposAsignatura) {
	this.tiposAsignatura = tiposAsignatura;
    }

    /**
     * @return the areasDeFormacion
     */
    public List<String> getAreasDeFormacion() {
	return areasDeFormacion;
    }

    /**
     * @param areasDeFormacion
     *            the areasDeFormacion to set
     */
    public void setAreasDeFormacion(List<String> areasDeFormacion) {
	this.areasDeFormacion = areasDeFormacion;
    }

    /**
     * @return the organizacionCurricular
     */
    public List<OrganizacionCurricularDTO> getOrganizacionCurricular() {
	return organizacionCurricular;
    }

    /**
     * @param organizacionCurricular
     *            the organizacionCurricular to set
     */
    public void setOrganizacionCurricular(
	    List<OrganizacionCurricularDTO> organizacionCurricular) {
	this.organizacionCurricular = organizacionCurricular;
    }

    @PostConstruct
    private void cargarIes() {
	usuarioSistema = SecurityContextHolder.getContext().getAuthentication()
	        .getName();

	for (TipoAsignaturaEnum tra : TipoAsignaturaEnum.values()) {
	    tiposAsignatura.add(tra.getValue());
	}

	for (AreaFormacionEnum af : AreaFormacionEnum.values()) {
	    areasDeFormacion.add(af.getValue());
	}

	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");

	faseIesDTO = controller.getFaseIesDTO();
	if (controller != null) {
	    ies = controller.getIes();
	    obtenerInformacionIes();
	}
	this.perfil = controller.getPerfil().getNombre();
	if (this.perfil.startsWith("IES_USUARIO")) {
	    alertaUsuarioIes = true;
	}

	if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
	    alertaFaseRectificacion = true;
	}
    }

    private void obtenerInformacionIes() {
	try {
	    infoIes = registroServicio.obtenerInformacionIesPorIes(ies);

	    infoIes.getSedeIes().clear();

	    infoIes.getSedeIes().addAll(
		    registroServicio.obtenerSedesIes(infoIes.getId()));

	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void cargarCarreras() {
	carreras.clear();
	mallas.clear();
	materias.clear();
	preRequisitos.clear();
	coRequisitos.clear();
	requisitos.clear();

	materiasFiltros.clear();
	preRequisitosFiltros.clear();
	coRequisitosFiltros.clear();

	mallaSeleccionada = new MallaCurricularDTO();
	materiaSeleccionada = new AsignaturaDTO();

	verNuevaMalla = false;

	if (idSede != null && idSede > 0) {
	    try {
		carreras = registroServicio.obtenerInfCarreraPorSede(idSede,
		        null);

		if (!carreras.isEmpty()) {
		    JsfUtil.msgInfo("Se encontraron " + carreras.size()
			    + " carreras");
		}

	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo obtoner las carreras");
		return;
	    }
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar la matriz o extensión");
	}
    }

    public void cargarCarrerasImportacion() {
	mallasImportacion.clear();
	carreraSeleccionadaImportacion = null;
	if (idSedeImportacion != null && idSedeImportacion > 0) {
	    try {
		carrerasImportacion = registroServicio
		        .obtenerInfCarreraPorSede(idSedeImportacion, null);
		for (int i = 0; i < carrerasImportacion.size(); i++) {
		    if (carrerasImportacion.get(i).getId()
			    .equals(carreraSeleccionada.getId())) {
			carrerasImportacion.remove(i);
		    }
		}
		if (!carrerasImportacion.isEmpty()) {
		    JsfUtil.msgInfo("Se encontraron "
			    + carrerasImportacion.size() + " carreras");
		}
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo obtoner las carreras");
		return;
	    }
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar la matriz o extensión");
	}
    }

    public void cargarMallas() {
	mallas.clear();
	materias.clear();
	preRequisitos.clear();
	coRequisitos.clear();
	requisitos.clear();
	materiasFiltros.clear();
	preRequisitosFiltros.clear();
	coRequisitosFiltros.clear();

	materiaSeleccionada = new AsignaturaDTO();
	mallaSeleccionada = new MallaCurricularDTO();

	verNuevaMalla = false;

	if (carreraSeleccionada != null && carreraSeleccionada.getId() != null) {
	    try {
		mallas = registroServicio
		        .obtenerMallaCurricular(carreraSeleccionada);
		for (MallaCurricularDTO malla : mallas) {
		    if (malla.getReforma() != null) {
			for (MallaCurricularDTO mallaReforma : mallas) {
			    if (malla.getReforma().equals(mallaReforma.getId())) {
				malla.setReformaTexto("("
				        + mallaReforma.getCodigoUnico() + ") "
				        + mallaReforma.getDescripcion());
				break;
			    }
			}
		    }
		}

		verNuevaMalla = true;

	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo obtener las mallas");
		return;
	    }
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar una carrera");
	}
    }

    public void cargarMallasImportacion() {
	if (carreraSeleccionadaImportacion != null
	        && carreraSeleccionadaImportacion != null) {
	    try {
		mallasImportacion = registroServicio
		        .obtenerMallaCurricular(carreraSeleccionadaImportacion);
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("Error al obtener las mallas, comuníquese con el administrador del sistema");
		return;
	    }
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar una carrera");
	}
    }

    public void cargarAsignaturas() {
	materias.clear();
	materiasFiltros.clear();
	materiaSeleccionada = new AsignaturaDTO();

	if (mallaSeleccionada != null && mallaSeleccionada.getId() != null) {
	    try {
		List<AsignaturaDTO> materiasTemp = null;

		materiasTemp = registroServicio
		        .obtenerAsignaturasPorMalla(mallaSeleccionada.getId());

		if (materiasTemp != null) {
		    for (AsignaturaDTO mat : materiasTemp) {
			if (mat.getIdAsignaturaPadre() == null) {
			    materias.add(mat);
			}
		    }
		}

		// Se suma los creditos de las asignaturas
		validarNumeroCreditos();

		materiasFiltros.addAll(materias);

	    } catch (ServicioException e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo obtener las asignaturas");
		return;
	    }
	} else {
	    JsfUtil.msgAdvert("Debe seleccionar una malla curricular");
	    return;
	}

	// carga de organizacion curricular
	cargarOrganizacionCurricular();
    }

    private void validarNumeroCreditos() {
	totalCreditos = 0.0;

	for (AsignaturaDTO asi : materias) {
	    if (asi.getNumeroCreditos() != null) {
		totalCreditos += asi.getNumeroCreditos();
	    }
	}
    }

    // Valida que el total de creditos de las asignaturas hijas sea igual a la
    // asignatura padre
    private void validarCreditosCompuesta() {
	totalCreditosHijas = 0.0;

	for (AsignaturaDTO hija : hijas) {
	    if (hija.getNumeroCreditos() != null && hija.getObligatorio()) {
		totalCreditosHijas += hija.getNumeroCreditos();
	    }
	}
    }

    public void cargarRequisitos() {
	requisitos.clear();
	requisitosRemovidos.clear();
	preRequisitos.clear();
	coRequisitos.clear();
	preRequisitosFiltros.clear();
	coRequisitosFiltros.clear();

	if (materiaSeleccionada != null && materiaSeleccionada.getId() != null) {

	    Long idMateriaSeleccionada = materiaSeleccionada.getId();

	    try {
		materiaSeleccionada = registroServicio
		        .obtenerAsignatura(idMateriaSeleccionada);
	    } catch (ServicioException e) {
		e.printStackTrace();
		return;
	    }

	    // Pre requisitos temporal
	    List<RequisitoAsignaturaDTO> presFalseTemp = new ArrayList<>();

	    // Remove los pre requisitos asignaturas que son false
	    for (RequisitoAsignaturaDTO pre : materiaSeleccionada
		    .getPreRequisitoMalla()) {
		if (!pre.getActivo()) {
		    presFalseTemp.add(pre);
		}
	    }

	    materiaSeleccionada.getPreRequisitoMalla().removeAll(presFalseTemp);

	    // Co requisitos temporal
	    List<RequisitoAsignaturaDTO> cosFalseTemp = new ArrayList<>();

	    // Remove los co requisitos asignaturas que son false
	    for (RequisitoAsignaturaDTO co : materiaSeleccionada
		    .getCorRequisitoMalla()) {
		if (!co.getActivo()) {
		    cosFalseTemp.add(co);
		}
	    }

	    materiaSeleccionada.getCorRequisitoMalla().removeAll(cosFalseTemp);

	    for (AsignaturaDTO mat : materias) {

		// Si una asignatura(mat) es de tipo
		// "asginatura sin creditos" no tendra nivel y por lo tanto
		// no debe agregarse
		if (null != mat.getNivelMateria()) {

		    // Se agregan como pre requisitos, todas las
		    // asignaturas(materias) cuyo nivelMateria
		    // es menor al nivel de la materiaSeleccionada
		    if (mat.getNivelMateria() < materiaSeleccionada
			    .getNivelMateria()) {
			preRequisitos.add(mat);
		    }

		    // Se agregan como co requisitos, todas las
		    // asignaturas(materias) cuyo nivelMateria
		    // es igual al nivel de la materiaSeleccionada
		    if (mat.getNivelMateria().equals(
			    materiaSeleccionada.getNivelMateria())
			    && !mat.getId().equals(materiaSeleccionada.getId())) {
			coRequisitos.add(mat);
		    }
		}
	    }

	    // Remove de preRequisitos los pre requisitos existentes de la
	    // materiaSeleccionada
	    List<AsignaturaDTO> presTemp = new ArrayList<>();

	    for (RequisitoAsignaturaDTO pre : materiaSeleccionada
		    .getPreRequisitoMalla()) {

		for (AsignaturaDTO as : preRequisitos) {
		    if (as.getId().equals(
			    pre.getAsignaturaRequisitoDTO().getId())) {
			presTemp.add(as);
		    }
		}
	    }

	    if (!presTemp.isEmpty()) {
		preRequisitos.removeAll(presTemp);
	    }

	    // Remove de coRequisitos los co requisitos existentes de la
	    // materiaSeleccionada
	    List<AsignaturaDTO> cosTemp = new ArrayList<>();

	    for (RequisitoAsignaturaDTO co : materiaSeleccionada
		    .getCorRequisitoMalla()) {

		for (AsignaturaDTO as : coRequisitos) {
		    if (as.getId().equals(
			    co.getAsignaturaRequisitoDTO().getId())) {
			cosTemp.add(as);
		    }
		}
	    }

	    if (!cosTemp.isEmpty()) {
		coRequisitos.removeAll(cosTemp);
	    }

	    preRequisitosFiltros.addAll(preRequisitos);
	    coRequisitosFiltros.addAll(coRequisitos);

	    // Se agregan los pre y co requisitos en una sola lista
	    requisitos.addAll(materiaSeleccionada.getPreRequisitoMalla());
	    requisitos.addAll(requisitos.size(),
		    materiaSeleccionada.getCorRequisitoMalla());

	} else {
	    JsfUtil.msgAdvert("Debe seleccionar una asignatura");
	}
    }

    public void cargarRequisitosAsignatura() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (materiaSeleccionada.getId() != null
		    && !materiaSeleccionada.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede modificar esta información en fase de Rectificación.");
		return;
	    }
	}

	RequisitoAsignaturaDTO reqAsignatura = new RequisitoAsignaturaDTO();

	reqAsignatura.setAsignaturaDTO(materiaSeleccionada);
	reqAsignatura.setActivo(true);

	if (preSeleccionado != null && preSeleccionado.getId() != null) {
	    reqAsignatura.setTipoEnum(TipoRequisitoMallaEnum.PRE);
	    reqAsignatura.setAsignaturaRequisitoDTO(preSeleccionado);

	    // Remove un pre requisito seleccionado de preRequisitos
	    for (int i = 0; i < preRequisitos.size(); i++) {
		if (preRequisitos.get(i).getId()
		        .equals(preSeleccionado.getId())) {
		    preRequisitos.remove(i);
		    break;
		}
	    }

	    // Remove un pre requisito seleccionado de preRequisitosFiltros
	    for (int i = 0; i < preRequisitosFiltros.size(); i++) {
		if (preRequisitosFiltros.get(i).getId()
		        .equals(preSeleccionado.getId())) {
		    preRequisitosFiltros.remove(i);
		    break;
		}
	    }

	    JsfUtil.msgInfo("Pre-requisito seleccionado: "
		    + preSeleccionado.getCodigo());
	}

	if (coSeleccionado != null && coSeleccionado.getId() != null) {
	    reqAsignatura.setTipoEnum(TipoRequisitoMallaEnum.COR);
	    reqAsignatura.setAsignaturaRequisitoDTO(coSeleccionado);

	    // Remove un co requisito seleccionado de coRequisitos
	    for (int i = 0; i < coRequisitos.size(); i++) {
		if (coRequisitos.get(i).getId().equals(coSeleccionado.getId())) {
		    coRequisitos.remove(i);
		    break;
		}
	    }

	    // Remove un co requisito seleccionado de coRequisitosFiltros
	    for (int i = 0; i < coRequisitosFiltros.size(); i++) {
		if (coRequisitosFiltros.get(i).getId()
		        .equals(coSeleccionado.getId())) {
		    coRequisitosFiltros.remove(i);
		    break;
		}
	    }

	    JsfUtil.msgInfo("Co-requisito seleccionado: "
		    + coSeleccionado.getCodigo());
	}

	preSeleccionado = new AsignaturaDTO();
	coSeleccionado = new AsignaturaDTO();

	// Primero: remove requisito si existe
	for (int i = 0; i < requisitos.size(); i++) {
	    if (requisitos.get(i).getAsignaturaRequisitoDTO().getId()
		    .equals(reqAsignatura.getAsignaturaRequisitoDTO().getId())) {
		requisitos.remove(i);
		break;
	    }
	}

	// Segundo: se agrega el requisito
	requisitos.add(reqAsignatura);

    }

    public void quitarRequisito() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (reqSeleccionado.getId() != null
		    && !reqSeleccionado.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede modificar esta información en fase de Rectificación.");
		return;
	    }
	}

	if (reqSeleccionado != null
	        && reqSeleccionado.getAsignaturaRequisitoDTO() != null
	        && reqSeleccionado.getAsignaturaRequisitoDTO().getId() != null) {

	    for (int i = 0; i < requisitos.size(); i++) {
		if (requisitos.get(i) != null) {
		    if (requisitos
			    .get(i)
			    .getAsignaturaRequisitoDTO()
			    .getId()
			    .equals(reqSeleccionado.getAsignaturaRequisitoDTO()
			            .getId())) {
			requisitos.remove(i);
			break;
		    }
		}
	    }

	    if (reqSeleccionado.getTipoEnum()
		    .equals(TipoRequisitoMallaEnum.PRE)) {
		preRequisitos.add(reqSeleccionado.getAsignaturaRequisitoDTO());
		preRequisitosFiltros.add(reqSeleccionado
		        .getAsignaturaRequisitoDTO());

		JsfUtil.msgInfo("Pre-requisito disponible: "
		        + reqSeleccionado.getAsignaturaRequisitoDTO()
		                .getCodigo());

	    }

	    if (reqSeleccionado.getTipoEnum()
		    .equals(TipoRequisitoMallaEnum.COR)) {
		coRequisitos.add(reqSeleccionado.getAsignaturaRequisitoDTO());
		coRequisitosFiltros.add(reqSeleccionado
		        .getAsignaturaRequisitoDTO());

		JsfUtil.msgInfo("Co-requisito disponible: "
		        + reqSeleccionado.getAsignaturaRequisitoDTO()
		                .getCodigo());

	    }

	    if (reqSeleccionado.getId() != null) {
		reqSeleccionado.setActivo(false);
		requisitosRemovidos.add(reqSeleccionado);
	    }

	    reqSeleccionado = new RequisitoAsignaturaDTO();

	} else {
	    JsfUtil.msgAdvert("Debe seleccionar un requisito");
	}
    }

    public void nuevaMalla(ActionEvent evt) {
	this.mallaCrearEditar = (MallaCurricularDTO) SerializationUtils
	        .clone(new MallaCurricularDTO());
	cargarOrganizacionesVacias(mallaCrearEditar);
	organizacionMallaSelect = "0";
	numeroNivelesSelect = "0";
    }

    public void editarMalla(MallaCurricularDTO malla, Boolean idEditMalla) {
	System.out.println("esCopiaMalla.." + idEditMalla);
	// if (idEditMalla) {
	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (malla.getId() != null
	// && !malla.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar esta malla en fase de Rectificación.");
	// return;
	// }
	// }
	// }
	mallaCrearEditar = (MallaCurricularDTO) SerializationUtils.clone(malla);
	List<OrganizacionCurricularDTO> organizacionCurricularList = null;

	try {
	    organizacionCurricularList = registroServicio
		    .obtenerOrganizacionCurricular(mallaCrearEditar.getId());
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}

	if (organizacionCurricularList == null
	        || organizacionCurricularList.isEmpty()) {
	    cargarOrganizacionesVacias(mallaCrearEditar);
	} else {
	    mallaCrearEditar
		    .setOrganizacionesCurricularDTO(organizacionCurricularList);
	}
	if (mallaCrearEditar.getOrganizacionMallaEnum() != null) {
	    organizacionMallaSelect = mallaCrearEditar
		    .getOrganizacionMallaEnum().getValue();
	}
	if (mallaCrearEditar.getNumeroNiveles() != null) {
	    cargarNiveles();
	    numeroNivelesSelect = mallaCrearEditar.getNumeroNiveles()
		    .toString();
	}
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgCrearEditarMallaId.show();");
    }

    private void cargarOrganizacionesVacias(MallaCurricularDTO malla) {
	malla.getOrganizacionesCurricularDTO().clear();
	List<OrganizacionCurricularDTO> organizacionCurricularList = new ArrayList<>();

	OrganizacionCurricularDTO organizacionCurricularInicial = new OrganizacionCurricularDTO();
	organizacionCurricularInicial
	        .setAreaFormacion(AreaFormacionOrganizacionEnum.INICIAL);

	OrganizacionCurricularDTO organizacionCurricularProfesional = new OrganizacionCurricularDTO();
	organizacionCurricularProfesional
	        .setAreaFormacion(AreaFormacionOrganizacionEnum.PROFESIONAL);

	OrganizacionCurricularDTO organizacionCurricularPerfil = new OrganizacionCurricularDTO();
	organizacionCurricularPerfil
	        .setAreaFormacion(AreaFormacionOrganizacionEnum.TITULACION);

	organizacionCurricularList.add(organizacionCurricularInicial);
	organizacionCurricularList.add(organizacionCurricularProfesional);
	organizacionCurricularList.add(organizacionCurricularPerfil);

	malla.setOrganizacionesCurricularDTO(organizacionCurricularList);
    }

    private boolean validarFormularioEditarCrearMalla(
	    MallaCurricularDTO mallaGuardar) {
	if ("0".equals(organizacionMallaSelect)) {
	    JsfUtil.msgError("La organización de la malla es requerida");
	    return false;
	}

	if (numeroNivelesSelect == null || "0".equals(numeroNivelesSelect)
	        || "".equals(numeroNivelesSelect)) {
	    JsfUtil.msgError("El número de niveles es requerido");
	    return false;
	}

	if (mallaGuardar.getFechaFinVigencia() != null
	        && mallaGuardar.getFechaInicioVigencia().compareTo(
	                mallaGuardar.getFechaFinVigencia()) >= 0) {
	    JsfUtil.msgError("La fecha de Inicio debe ser mayor a la fecha final");
	    return false;
	}
	if ((new Integer(0)).equals(mallaGuardar.getAniosSinTesis())) {
	    JsfUtil.msgError("Los meses sin tesis deben ser mayores a 0");
	    return false;
	}
	if ((new Integer(0)).equals(mallaGuardar.getAniosConTesis())) {
	    JsfUtil.msgError("Los meses con tesis deben ser mayores a 0");
	    return false;
	}
	if ((new Integer(0)).equals(mallaGuardar.getCreditosSinTesis())) {
	    JsfUtil.msgError("Los créditos sin tesis deben ser mayores a 0");
	    return false;
	}
	if ((new Integer(0)).equals(mallaGuardar.getCreditosConTesis())) {
	    JsfUtil.msgError("Los créditos con tesis deben ser mayores a 0");
	    return false;
	}
	if (mallaGuardar.getAniosSinTesis() > mallaGuardar.getAniosConTesis()) {
	    JsfUtil.msgError("Los meses sin tesis deben ser menores o iguales a los meses con tesis");
	    return false;
	}
	if (mallaGuardar.getCreditosSinTesis() > mallaGuardar
	        .getCreditosConTesis()) {
	    JsfUtil.msgError("Los créditos sin tesis deben ser menores o iguales a los créditos con tesis");
	    return false;
	}

	for (MallaCurricularDTO malla : mallas) {
	    if (!malla.getId().equals(mallaGuardar.getId())
		    && malla.getFechaInicioVigencia().equals(
		            mallaGuardar.getFechaInicioVigencia())) {
		JsfUtil.msgError("Ya existe una malla con la misma fecha de Inicio de vigencia");
		return false;
	    }
	}

	try {
	    SedeIesDTO sede = registroServicio.obtenerSedeIesPorId(idSede);
	    if (sede.getFechaCreacion() != null
		    && sede.getFechaCreacion().compareTo(
		            mallaGuardar.getFechaInicioVigencia()) > 0) {

		SimpleDateFormat formatoFecha = new SimpleDateFormat(
		        "dd-MM-yyyy");
		JsfUtil.msgError("La fecha de inicio debe ser mayor a la fecha de creación de la matriz/extensión ("
		        + formatoFecha.format(sede.getFechaCreacion()) + ")");
		return false;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error en la validación del registro, comuníquese con el administrador del sistema.");
	    return false;
	}

	return true;
    }

    public void crearMallaImportada() {
	mallaCrearEditar
	        .setInformacionCarreraDTO(carreraSeleccionadaImportacion);
	esCopiaMalla = true;
	guardarMalla(true);
    }

    public void guardarMalla(boolean esImportacion) {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	if (esCopiaMalla) {
	    try {
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuarioSistema);

		MallaCurricularDTO copiaMalla = (MallaCurricularDTO) SerializationUtils
		        .clone(mallaCrearEditar);
		Long idMalla = copiaMalla.getId();
		copiaMalla.setId(null);
		copiaMalla.setCodigoUnico(null);
		if (esImportacion) {
		    copiaMalla.setReforma(null);
		} else {
		    copiaMalla.setReforma(idMalla);
		}
		copiaMalla.setAuditoria(auditoria);
		Map<AreaFormacionOrganizacionEnum, Long> organizacionesAnterioresIds = new HashMap<>();
		Map<Long, Long> organizacionesNuevasIds = new HashMap<>();
		for (OrganizacionCurricularDTO organizacion : copiaMalla
		        .getOrganizacionesCurricularDTO()) {
		    organizacionesAnterioresIds.put(
			    organizacion.getAreaFormacion(),
			    organizacion.getId());
		    organizacion.setId(null);
		    organizacion.setAuditoria(auditoria);
		    organizacion.setMallaDTO(copiaMalla);
		}
		copiaMalla = insertarActualizarMalla(copiaMalla);
		// si no pasó las validaciones retorna null
		if (copiaMalla == null) {
		    return;
		}

		List<OrganizacionCurricularDTO> organizaciones = registroServicio
		        .obtenerOrganizacionCurricular(copiaMalla.getId());
		for (OrganizacionCurricularDTO org : organizaciones) {
		    organizacionesNuevasIds.put(organizacionesAnterioresIds
			    .get(org.getAreaFormacion()), org.getId());
		}

		List<AsignaturaDTO> asignaturas = registroServicio
		        .obtenerAsignaturasPorMalla(idMalla);
		int contador = 1;
		Map<Long, Long> idsAsignatura = new HashMap<>();
		List<AsignaturaDTO> asignaturasHijas = new ArrayList<>();

		for (AsignaturaDTO asignatura : asignaturas) {
		    Long id = asignatura.getId();
		    asignatura
			    .setOrganizacionCurricular(organizacionesNuevasIds
			            .get(asignatura.getOrganizacionCurricular()));
		    asignatura.setMallaCurricularDTO(copiaMalla);
		    asignatura.setId(null);
		    asignatura.setAuditoria(auditoria);
		    asignatura.setCodigoUnico(ies.getSiglas()
			    + "_"
			    + copiaMalla.getCodigoUnico()
			    + "_"
			    + asignatura.getCodigo()
			    + "_"
			    + asignatura.getNombre().substring(0, 3)
			            .toUpperCase() + "_" + contador);
		    contador++;

		    asignatura.setFaseIesDTO(faseIesDTO);
		    AsignaturaDTO asignaturaCreada = registroServicio
			    .crearActualizar(asignatura);
		    idsAsignatura.put(id, asignaturaCreada.getId());
		    if (asignaturaCreada.getIdAsignaturaPadre() != null) {
			asignaturasHijas.add(asignaturaCreada);
		    }

		    for (RequisitoAsignaturaDTO requisito : asignatura
			    .getCorRequisitoMalla()) {
			if (requisito.getActivo()) {
			    requisito.setId(null);
			    requisito.setAuditoria(auditoria);
			    requisito.setAsignaturaDTO(asignaturaCreada);
			    requisito.setFaseIesDTO(faseIesDTO);
			    registroServicio.crearActualizar(requisito);
			}
		    }
		    for (RequisitoAsignaturaDTO requisito : asignatura
			    .getPreRequisitoMalla()) {
			if (requisito.getActivo()) {
			    requisito.setId(null);
			    requisito.setAuditoria(auditoria);
			    requisito.setAsignaturaDTO(asignaturaCreada);
			    requisito.setFaseIesDTO(faseIesDTO);
			    registroServicio.crearActualizar(requisito);
			}
		    }
		}

		for (AsignaturaDTO asignaturaHija : asignaturasHijas) {
		    asignaturaHija.setIdAsignaturaPadre(idsAsignatura
			    .get(asignaturaHija.getIdAsignaturaPadre()));
		    asignaturaHija.setFaseIesDTO(faseIesDTO);
		    registroServicio.crearActualizar(asignaturaHija);
		}

		cargarMallas();
		JsfUtil.msgInfo("La copia de la malla se realizó correctamente");
		context.addCallbackParam("cerrarVentana", true);
	    } catch (Exception e) {
		JsfUtil.msgError("Error al copiar la malla, comuníquese con el administrador del sistema");
		e.printStackTrace();
	    }

	} else {
	    try {
		mallaCrearEditar.setVerificarEvidencia(false);
		if (insertarActualizarMalla(mallaCrearEditar) != null) {

		    JsfUtil.msgInfo("Se guardó la nueva malla correctamente");
		    context.addCallbackParam("cerrarVentana", true);
		} else {
		    return;
		}
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("Error al guardar la malla, comuníquese con el administrador del sistema.");
		return;
	    }
	}

    }

    private MallaCurricularDTO insertarActualizarMalla(
	    MallaCurricularDTO mallaGuardar) throws Exception {

	if (!validarFormularioEditarCrearMalla(mallaGuardar)) {
	    return null;
	}

	String usuario = SecurityContextHolder.getContext().getAuthentication()
	        .getName();
	// ListaIesController controller = (ListaIesController) FacesContext
	// .getCurrentInstance().getExternalContext().getSessionMap()
	// .get("listaIesController");
	SedeIesDTO sedeIes = registroServicio.obtenerSedeIesPorId(idSede);

	if (mallaGuardar.getId() == null) {
	    mallaGuardar.setActivo(Boolean.TRUE);
	    mallaGuardar.setFaseIesDTO(faseIesDTO);
	}
	if (mallaGuardar.getCodigoUnico() == null) {
	    mallaGuardar.setCodigoUnico(sedeIes.getCodigo() + "-"
		    + carreraSeleccionada.getCarreraIesDTO().getCodigo() + "-"
		    + obtenerCodigoSecuencialMalla());
	}
	mallaGuardar.setInformacionCarreraDTO(carreraSeleccionada);
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuario);

	mallaGuardar.setAuditoria(auditoriaDTO);
	if (numeroNivelesSelect != null && !"".equals(numeroNivelesSelect)
	        && !"0".equals(numeroNivelesSelect)) {
	    mallaGuardar.setNumeroNiveles(new Integer(numeroNivelesSelect));
	}
	// TODO: revisar
	// mallaCrearEditar.setIdFaseIes(controller.getFaseIesDTO().getId());
	MallaCurricularDTO malla = registroServicio
	        .registrarMallaCurricular(mallaGuardar);
	mallaGuardar.setId(malla.getId());

	for (OrganizacionCurricularDTO organizacion : mallaGuardar
	        .getOrganizacionesCurricularDTO()) {
	    organizacion.setAuditoria(auditoriaDTO);
	    // TODO: revisar
	    // organizacion.setIdFaseIes(controller.getFaseIesDTO().getId());
	    organizacion.setActivo(Boolean.TRUE);
	}

	registroServicio.registrarOrganizacionCurricular(
	        mallaGuardar.getOrganizacionesCurricularDTO(), malla);

	cargarMallas();

	materias.clear();
	materiasFiltros.clear();

	preRequisitos.clear();
	coRequisitos.clear();

	preRequisitosFiltros.clear();
	coRequisitosFiltros.clear();

	mallaSeleccionada = new MallaCurricularDTO();
	materiaSeleccionada = new AsignaturaDTO();
	return malla;
    }

    public boolean verficarMallatieneAsignaturas() {

	List<AsignaturaDTO> listaAsignaturaM = new ArrayList<AsignaturaDTO>();

	try {

	    if (mallaCrearEditar != null && mallaCrearEditar.getId() != null) {

		List<AsignaturaDTO> materiasTemp = new ArrayList<AsignaturaDTO>();

		materiasTemp = registroServicio
		        .obtenerAsignaturasPorMalla(mallaCrearEditar.getId());

		if (materiasTemp != null) {
		    for (AsignaturaDTO mat : materiasTemp) {
			if (mat.getIdAsignaturaPadre() == null) {
			    listaAsignaturaM.add(mat);
			}
		    }
		}
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	}

	if (listaAsignaturaM.isEmpty()) {
	    return false;
	} else {
	    JsfUtil.msgInfo("No se puede eliminar la Malla tiene asignaturas activas");
	    return true;
	}

    }

    public void eliminarMalla() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (mallaCrearEditar.getId() != null
		    && !mallaCrearEditar.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar esta malla en fase de Rectificación.");
		return;
	    }
	}

	if (verficarMallatieneAsignaturas()) {
	    return;
	}

	try {
	    String usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    mallaCrearEditar.setFaseIesDTO(this.faseIesDTO);
	    mallaCrearEditar.setActivo(Boolean.FALSE);

	    for (OrganizacionCurricularDTO organizacion : mallaCrearEditar
		    .getOrganizacionesCurricularDTO()) {
		organizacion.setAuditoria(auditoriaDTO);
		// TODO: revisar
		// organizacion.setIdFaseIes(controller.getFaseIesDTO().getId());
		organizacion.setActivo(Boolean.FALSE);
	    }

	    mallaCrearEditar.setInformacionCarreraDTO(carreraSeleccionada);
	    MallaCurricularDTO malla = registroServicio
		    .registrarMallaCurricular(mallaCrearEditar);

	    registroServicio.registrarOrganizacionCurricular(
		    mallaCrearEditar.getOrganizacionesCurricularDTO(), malla);

	    List<AsignaturaDTO> asignaturas = registroServicio
		    .obtenerAsignaturasPorMalla(mallaCrearEditar.getId());
	    for (AsignaturaDTO asignatura : asignaturas) {
		asignatura.setMallaCurricularDTO(mallaCrearEditar);
		asignatura.setAuditoria(auditoriaDTO);
		asignatura.setActivo(Boolean.FALSE);
		asignatura.setFaseIesDTO(this.faseIesDTO);
		registroServicio.crearActualizar(asignatura);

		for (RequisitoAsignaturaDTO requisito : asignatura
		        .getPreRequisitoMalla()) {
		    if (requisito.getActivo()) {
			requisito.setAuditoria(auditoriaDTO);
			requisito.setAsignaturaDTO(asignatura);
			requisito.setActivo(Boolean.FALSE);
			requisito.setFaseIesDTO(this.faseIesDTO);
			registroServicio.crearActualizar(requisito);
		    }
		}
		for (RequisitoAsignaturaDTO requisito : asignatura
		        .getCorRequisitoMalla()) {
		    if (requisito.getActivo()) {
			requisito.setAuditoria(auditoriaDTO);
			requisito.setAsignaturaDTO(asignatura);
			requisito.setActivo(Boolean.FALSE);
			requisito.setFaseIesDTO(faseIesDTO);
			registroServicio.crearActualizar(requisito);
		    }
		}
	    }

	    cargarMallas();
	    materiaSeleccionada = new AsignaturaDTO();
	    mallaSeleccionada = new MallaCurricularDTO();

	    JsfUtil.msgInfo("La malla " + malla.getCodigoUnico()
		    + " se elimino correctamente");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar la malla, por favor comuníquese con el administrador del sistema");
	}
    }

    private String obtenerCodigoSecuencialMalla() throws Exception {
	List<MallaCurricularDTO> mallasSecuencial = registroServicio
	        .obtenerTodasMallaCurricular(carreraSeleccionada);
	int secuencial = 0;
	for (MallaCurricularDTO mallaDto : mallasSecuencial) {
	    if (mallaDto.getCodigoUnico() != null) {
		int secuencialLista = Integer.parseInt(mallaDto
		        .getCodigoUnico().substring(
		                mallaDto.getCodigoUnico().length() - 3));
		if (secuencialLista > secuencial) {
		    secuencial = secuencialLista;
		}
	    }
	}
	String codigoSecuencial = String.valueOf(secuencial + 1);
	if (codigoSecuencial.length() == 1) {
	    codigoSecuencial = "00" + codigoSecuencial;
	} else if (codigoSecuencial.length() == 2) {
	    codigoSecuencial = "0" + codigoSecuencial;
	}
	return codigoSecuencial;
    }

    /**
     * @return the mallaCrearEditar
     */
    public MallaCurricularDTO getMallaCrearEditar() {
	return mallaCrearEditar;
    }

    /**
     * @param mallaCrearEditar
     *            the mallaCrearEditar to set
     */
    public void setMallaCrearEditar(MallaCurricularDTO mallaCrearEditar) {
	this.mallaCrearEditar = mallaCrearEditar;
    }

    /**
     * @return the fecahActual
     */
    public Date getFecahActual() {
	return new Date();
    }

    /**
     * @return the organizacionMalla
     */
    public SelectItem[] getOrganizacionMalla() {
	return ORGANIZACION_MALLA;
    }

    /**
     * @return the niveles
     */
    public List<SelectItem> getNiveles() {
	return niveles;
    }

    /**
     * @param organizacionMalla
     *            the organizacionMalla to set
     */
    public void setOrganizacionMallaSelect(String organizacionMallaSelect) {
	this.organizacionMallaSelect = organizacionMallaSelect;
    }

    public String getOrganizacionMallaSelect() {
	return organizacionMallaSelect;
    }

    public void cargarNiveles() {
	niveles.clear();
	numeroNivelesSelect = "0";
	if (!"0".equals(organizacionMallaSelect)) {

	    if (OrganizacionMallaEnum.BIMESTRE.getValue().toString()
		    .equals(organizacionMallaSelect)) {
		mallaCrearEditar
		        .setOrganizacionMallaEnum(OrganizacionMallaEnum.BIMESTRE);
		for (int i = 1; i <= 18; i++) {
		    niveles.add(new SelectItem(i, String.valueOf(i)));
		}
	    } else if (OrganizacionMallaEnum.TRIMESTRES.getValue().toString()
		    .equals(organizacionMallaSelect)) {
		mallaCrearEditar
		        .setOrganizacionMallaEnum(OrganizacionMallaEnum.TRIMESTRES);
		for (int i = 1; i <= 32; i++) {
		    niveles.add(new SelectItem(i, String.valueOf(i)));
		}
	    } else if (OrganizacionMallaEnum.CUATRIMESTRES.getValue()
		    .toString().equals(organizacionMallaSelect)) {
		mallaCrearEditar
		        .setOrganizacionMallaEnum(OrganizacionMallaEnum.CUATRIMESTRES);
		for (int i = 1; i <= 24; i++) {
		    niveles.add(new SelectItem(i, String.valueOf(i)));
		}
	    } else if (OrganizacionMallaEnum.SEMESTRES.getValue().toString()
		    .equals(organizacionMallaSelect)) {
		mallaCrearEditar
		        .setOrganizacionMallaEnum(OrganizacionMallaEnum.SEMESTRES);
		for (int i = 1; i <= 16; i++) {
		    niveles.add(new SelectItem(i, String.valueOf(i)));
		}
	    } else if (OrganizacionMallaEnum.ANIOS.getValue().toString()
		    .equals(organizacionMallaSelect)) {
		mallaCrearEditar
		        .setOrganizacionMallaEnum(OrganizacionMallaEnum.ANIOS);
		for (int i = 1; i <= 8; i++) {
		    niveles.add(new SelectItem(i, String.valueOf(i)));
		}
	    }
	}
    }

    /**
     * @return the numeroNivelesSelect
     */
    public String getNumeroNivelesSelect() {
	return numeroNivelesSelect;
    }

    /**
     * @param numeroNivelesSelect
     *            the numeroNivelesSelect to set
     */
    public void setNumeroNivelesSelect(String numeroNivelesSelect) {
	this.numeroNivelesSelect = numeroNivelesSelect;
    }

    // TODO: solo para pruebas
    public void obtenerFiltros(FilterEvent event) {
	tablaAsignaturas = (DataTable) event.getSource();

	Map<String, String> filtros = tablaAsignaturas.getFilters();

	for (Map.Entry<String, String> fil : filtros.entrySet()) {
	    filtrosTablaAsignaturas.put(fil.getKey(), fil.getValue());
	}
    }

    public void registrarRequisitosAsignatura() {
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuarioSistema);

	// Add de los requisitos que tienen id y como activo false
	requisitos.addAll(requisitos.size(), requisitosRemovidos);

	for (RequisitoAsignaturaDTO ras : requisitos) {
	    ras.setAuditoria(auditoriaDTO);
	    ras.setFaseIesDTO(this.faseIesDTO);
	    try {
		registroServicio.crearActualizar(ras);
	    } catch (ServicioException e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo guardar los requisitos");
		return;
	    }
	}

	Long idMateriaSeleccionada = materiaSeleccionada.getId();

	// Para refrescar los datos de las asignaturas
	cargarAsignaturas();

	try {
	    materiaSeleccionada = registroServicio
		    .obtenerAsignatura(idMateriaSeleccionada);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}

	for (int i = 0; i < materias.size(); i++) {
	    if (materias.get(i).getId().equals(idMateriaSeleccionada)) {
		materias.remove(i);
		break;
	    }
	}

	materias.add(0, materiaSeleccionada);

	cargarRequisitos();

	JsfUtil.msgInfo("Los requisitos se guardaron correctamente");
    }

    /**
     * 
     * Para definir que boton hace que cosa
     * 
     * @author eteran
     * @version 19/05/2014 - 16:43:03
     * @param event
     */
    public void tomarNombreComponente(ActionEvent event) {

	FacesContext ctx = FacesContext.getCurrentInstance();
	nombreComponente = event.getComponent().getClientId(ctx);

	if (nombreComponente.contains("btnNuevaAsignatura")) {
	    asignatura = new AsignaturaDTO();
	    setOpcionRequisito(2);
	    asignatura
		    .setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_CON_CREDITOS
		            .getValue());
	}

	if (nombreComponente.contains("btnNuevaHijaAsignatura")) {
	    if (padreAsignatura.getId() == null) {
		JsfUtil.msgAdvert("Primero se debe crear una asignatura padre");
		return;
	    }

	    hijaAsignatura = new AsignaturaDTO();
	    hijaAsignatura.setActivo(true);
	    hijaAsignatura.setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_HIJA
		    .getValue());
	    hijaAsignatura.setNivelMateria(asignatura.getNivelMateria());
	    hijaAsignatura.setIdAsignaturaPadre(asignatura.getId());
	    hijaAsignatura.setAreaFormacion(asignatura.getAreaFormacion());
	    hijaAsignatura.setOrganizacionCurricular(asignatura
		    .getOrganizacionCurricular());

	    RequestContext rctx = RequestContext.getCurrentInstance();
	    rctx.execute("dialogAsignaturaHija.show()");
	}
    }

    public void obtenerAsignatura(boolean actualizar) {
	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (asignatura.getId() != null
	// && !asignatura.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar esta malla en fase de Rectificación.");
	// return;
	// }
	// }

	hijas.clear();

	try {
	    hijas = registroServicio.obtenerAsignaturaHijas(asignatura.getId());
	    if (actualizar) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogAsignatura.show()");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}
    }

    private boolean validarAsignatura(AsignaturaDTO asignaturaDTO) {
	if (asignaturaDTO.getId() == null) {
	    for (AsignaturaDTO asi : materias) {
		if (asi.getCodigo().equalsIgnoreCase(asignaturaDTO.getCodigo())) {
		    JsfUtil.msgError("El código ya existe");
		    return false;
		}
	    }
	}

	if (TipoAsignaturaEnum.ASIGNATURA_CON_CREDITOS.getValue().equals(
	        asignaturaDTO.getTipoAsignatura())
	        || TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue().equals(
	                asignaturaDTO.getTipoAsignatura())) {
	    if (asignaturaDTO.getNumeroCreditos() == null) {
		JsfUtil.msgError("Número de créditos es requerido");
		return false;
	    }
	}

	if (asignaturaDTO.getNombre() == null
	        || asignaturaDTO.getNombre().isEmpty()) {
	    JsfUtil.msgError("Nombre es requerido");
	    return false;
	}

	if (asignaturaDTO.getCodigo() == null
	        || asignaturaDTO.getCodigo().isEmpty()) {
	    JsfUtil.msgError("Codigo es requerido");
	    return false;
	}

	return true;
    }

    public void registrarAsignaturaHija() {
	if (!validarAsignatura(hijaAsignatura)) {
	    return;
	}

	Double totalCreditosHijasTemp = totalCreditosHijas;

	validarCreditosCompuesta();

	if (hijaAsignatura.getId() == null && hijaAsignatura.getObligatorio()) {
	    totalCreditosHijas += hijaAsignatura.getNumeroCreditos();
	}

	if (totalCreditosHijas > padreAsignatura.getNumeroCreditos()) {
	    totalCreditosHijas = totalCreditosHijas
		    - hijaAsignatura.getNumeroCreditos();
	    hijaAsignatura.setNumeroCreditos(creditosAsignatura);
	    totalCreditosHijas = totalCreditosHijasTemp;
	    JsfUtil.msgError("Los créditos registrados en las asignaturas obligatorias, no pueden superar a los créditos registrados en la asignatura padre");
	    return;
	}

	String msg = "La asignatura hija fue registrada correctamente";

	if (nombreComponente.contains("btnEliminarHijaAsignatura")) {
	    hijaAsignatura.setActivo(false);
	    msg = "Asignatura eliminda";
	}

	if (hijaAsignatura.getId() == null) {
	    hijaAsignatura.setActivo(true);

	    hijaAsignatura.setCodigoUnico(ies.getSiglas() + "_"
		    + mallaSeleccionada.getCodigoUnico() + "_"
		    + hijaAsignatura.getCodigo() + "_"
		    + hijaAsignatura.getNombre().substring(0, 3).toUpperCase()
		    + "_" + (hijas.size() + 1));
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date());
	auditoria.setUsuarioModificacion(usuarioSistema);
	hijaAsignatura.setFaseIesDTO(faseIesDTO);
	hijaAsignatura.setAuditoria(auditoria);
	hijaAsignatura.setMallaCurricularDTO(mallaSeleccionada);

	try {
	    hijaAsignatura = registroServicio.crearActualizar(hijaAsignatura);

	    if (hijaAsignatura != null) {
		JsfUtil.msgInfo(msg);
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo realizar la operacion");
	    return;
	}

	obtenerAsignatura(false);
	cargarAsignaturas();

	hijaAsignatura = new AsignaturaDTO();
	hijaAsignatura.setTipoAsignatura(TipoAsignaturaEnum.ASIGNATURA_HIJA
	        .getValue());
	hijaAsignatura.setNivelMateria(asignatura.getNivelMateria());
	hijaAsignatura.setIdAsignaturaPadre(asignatura.getId());
	hijaAsignatura.setAreaFormacion(asignatura.getAreaFormacion());
	hijaAsignatura.setOrganizacionCurricular(asignatura
	        .getOrganizacionCurricular());
    }

    public void registrarAsignatura() {
	if (!validarAsignatura(asignatura)) {
	    return;
	}

	validarNumeroCreditos();

	if (asignatura.getNumeroCreditos() != null
	        && asignatura.getId() == null) {
	    totalCreditos += asignatura.getNumeroCreditos();
	}

	if (totalCreditos > mallaSeleccionada.getCreditosConTesis()) {
	    cargarAsignaturas();
	    JsfUtil.msgError("Los créditos registrados, no pueden superar a Créditos con Tesis de la malla curricular seleccionada");
	    return;
	}

	String msg = "La asignatura fue registrada correctamente";

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date());
	auditoria.setUsuarioModificacion(usuarioSistema);

	if (nombreComponente.contains("btnEliminarAsignatura")) {
	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (asignatura.getId() != null
		        && !asignatura.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar esta asignatura en fase de Rectificación.");
		    return;
		}
	    }

	    eliminarTodasLasHijas(auditoria);
	    asignatura.setActivo(false);
	    msg = "Asignatura eliminada";
	}

	if (asignatura.getId() == null) {
	    asignatura.setActivo(true);
	    asignatura.setFaseIesDTO(faseIesDTO);
	    asignatura.setCodigoUnico(ies.getSiglas() + "_"
		    + mallaSeleccionada.getCodigoUnico() + "_"
		    + asignatura.getCodigo() + "_"
		    + asignatura.getNombre().substring(0, 3).toUpperCase()
		    + "_" + (materias.size() + 1));
	}

	asignatura.setAuditoria(auditoria);
	asignatura.setMallaCurricularDTO(mallaSeleccionada);
	if (!asignatura.getTipoAsignatura().equals(
	        TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue())
	        && !hijas.isEmpty()) {
	    eliminarTodasLasHijas(auditoria);
	}

	if (asignatura.getTipoAsignatura().equals(
	        TipoAsignaturaEnum.ASIGNATURA_SIN_CREDITOS.getValue())) {
	    asignatura.setNivelMateria(null);
	    asignatura.setNumeroCreditos(0.0);
	}

	try {
	    asignatura = registroServicio.crearActualizar(asignatura);

	    if (asignatura.getTipoAsignatura().equals(
		    TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue())) {
		setPadreAsignatura(asignatura);
	    }

	    if (asignatura != null) {
		JsfUtil.msgInfo(msg);
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo realizar la operacion");
	    return;
	}

	cargarAsignaturas();

	if (opcionRequisito == 3) {
	    padreAsignatura = asignatura;
	    validarCreditosCompuesta();
	    return;
	}

	if (nombreComponente.contains("btnEliminarAsignatura")) {
	    materiaSeleccionada = new AsignaturaDTO();
	}

	asignatura = new AsignaturaDTO();
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dialogAsignatura.hide()");
    }

    private void eliminarTodasLasHijas(AuditoriaDTO auditoria) {
	for (AsignaturaDTO hija : hijas) {
	    hija.setActivo(false);
	    hija.setAuditoria(auditoria);
	    hija.setFaseIesDTO(faseIesDTO);
	    try {
		registroServicio.crearActualizar(hija);
	    } catch (ServicioException e) {
		e.printStackTrace();
		return;
	    }
	}
    }

    private void cargarOrganizacionCurricular() {
	organizacionCurricular.clear();

	try {
	    organizacionCurricular = registroServicio
		    .obtenerOrganizacionCurricular(mallaSeleccionada.getId());
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}

	if (organizacionCurricular == null) {
	    JsfUtil.msgError("No se pudo obtener la organizacion curricular");
	}
    }

    public void generarReporteMalla(MallaCurricularDTO mallaReporte) {
	HttpServletRequest request = (HttpServletRequest) FacesContext
	        .getCurrentInstance().getExternalContext().getRequest();

	ByteArrayOutputStream outReporteMallas = new ByteArrayOutputStream();
	String pathJasperMallas = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/reportesJasper/mallas/mallas.jasper";
	String pathJasperAsignaturasCompuestas = request.getSession()
	        .getServletContext().getRealPath("")
	        + "/reportesJasper/mallas/asignaturasCompuestas/asignaturasCompuestas.jasper";

	Map<String, Object> parametrosReporteMallas = new HashMap<>();
	Map<String, Object> parametrosReporteAsignaturasCompuestas = new HashMap<>();

	String pathLogo = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/images/logo_ceaaces.png";
	parametrosReporteMallas.put("par_logo", pathLogo);
	parametrosReporteAsignaturasCompuestas.put("par_logo", pathLogo);

	parametrosReporteMallas.put("SUBREPORT_DIR", request.getSession()
	        .getServletContext().getRealPath("")
	        + "/reportesJasper/mallas/");
	parametrosReporteAsignaturasCompuestas.put("SUBREPORT_DIR", request
	        .getSession().getServletContext().getRealPath("")
	        + "/reportesJasper/mallas/asignaturasCompuestas/");

	try {
	    cargarDatosReporte(mallaReporte);

	    List<MallaCurricularDTO> listaReporte = new ArrayList<MallaCurricularDTO>();
	    listaReporte.add(mallaReporte);

	    JRDataSource dataSourceMallas = new JRBeanCollectionDataSource(
		    listaReporte);
	    JRDataSource dataSourceCompuestas = new JRBeanCollectionDataSource(
		    mallaReporte.getOrganizacionesCurricularDTO());

	    if (dataSourceMallas != null) {
		JRXlsExporter exporter = new JRXlsExporter();
		cargarPropiedadesReporte(exporter);

		JasperPrint jasperPrintMallas = JasperFillManager.fillReport(
		        pathJasperMallas, parametrosReporteMallas,
		        dataSourceMallas);
		JasperPrint jasperPrintCompuestas = JasperFillManager
		        .fillReport(pathJasperAsignaturasCompuestas,
		                parametrosReporteAsignaturasCompuestas,
		                dataSourceCompuestas);

		ArrayList<JasperPrint> hojasReporte = new ArrayList<JasperPrint>();
		hojasReporte.add(jasperPrintMallas);
		hojasReporte.add(jasperPrintCompuestas);

		exporter.setExporterInput(SimpleExporterInput
		        .getInstance(hojasReporte));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
		        outReporteMallas));
		exporter.exportReport();
		reporteBytes = outReporteMallas.toByteArray();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void presentarReporteXls() {
	HttpServletResponse response = (HttpServletResponse) FacesContext
	        .getCurrentInstance().getExternalContext().getResponse();
	try {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
		    "dd-MM-yyyy");
	    response.setContentType("application/vnd.ms-excel");
	    response.setHeader(
		    "Content-Disposition",
		    "attachment; filename=mallas_"
		            + simpleDateFormat.format(new Date()) + ".xls");
	    response.setContentLength(reporteBytes.length);
	    ServletOutputStream out = response.getOutputStream();
	    out.write(reporteBytes, 0, reporteBytes.length);
	    out.flush();
	    FacesContext.getCurrentInstance().responseComplete();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void generarReporteGrafico(MallaCurricularDTO mallaReporte) {
	HttpServletRequest request = (HttpServletRequest) FacesContext
	        .getCurrentInstance().getExternalContext().getRequest();

	ByteArrayOutputStream outReporte = new ByteArrayOutputStream();
	String pathJasperReporte = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/reportesJasper/mallasGrafico/mallas.jasper";

	Map<String, Object> parametrosReporte = new HashMap<>();

	String pathLogo = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/images/logo_ceaaces.png";
	parametrosReporte.put("par_logo", pathLogo);

	parametrosReporte.put("SUBREPORT_DIR", request.getSession()
	        .getServletContext().getRealPath("")
	        + "/reportesJasper/mallasGrafico/");
	try {
	    cargarDatosReporte(mallaReporte);

	    List<MallaCurricularDTO> listaReporte = new ArrayList<MallaCurricularDTO>();
	    listaReporte.add(mallaReporte);

	    JRDataSource dataSourceMallas = new JRBeanCollectionDataSource(
		    listaReporte);
	    if (dataSourceMallas != null) {
		JRXlsExporter exporter = new JRXlsExporter();
		cargarPropiedadesReporte(exporter);

		JasperPrint jasperPrint = JasperFillManager.fillReport(
		        pathJasperReporte, parametrosReporte, dataSourceMallas);
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
		        outReporte));

		exporter.exportReport();
		reporteBytes = outReporte.toByteArray();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void generarReporteGraficoWeb(MallaCurricularDTO mallaReporte) {
	mallaSeleccionada = null;
	try {
	    cargarDatosReporte(mallaReporte);
	    mallaSeleccionada = mallaReporte;
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema.");
	}
    }

    private void cargarPropiedadesReporte(JRXlsExporter exporter) {
	SimpleXlsReportConfiguration configuracionExcel = new SimpleXlsReportConfiguration();
	configuracionExcel.setDetectCellType(Boolean.TRUE);
	configuracionExcel.setOnePagePerSheet(Boolean.FALSE);
	configuracionExcel.setWhitePageBackground(Boolean.FALSE);
	configuracionExcel.setRemoveEmptySpaceBetweenColumns(Boolean.TRUE);
	configuracionExcel.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
	configuracionExcel.setIgnoreCellBorder(Boolean.FALSE);
	exporter.setConfiguration(configuracionExcel);
    }

    private void cargarDatosReporte(MallaCurricularDTO mallaReporte)
	    throws Exception {
	mallaReporte.setAsignaturasDTO(registroServicio
	        .obtenerAsignaturasPorMalla(mallaReporte.getId()));

	mallaReporte.setOrganizacionesCurricularDTO(registroServicio
	        .obtenerOrganizacionCurricular(mallaReporte.getId()));

	for (OrganizacionCurricularDTO organizacion : mallaReporte
	        .getOrganizacionesCurricularDTO()) {
	    organizacion.setAsignaturasDTO(registroServicio.obtenerAsignaturas(
		    mallaReporte.getId(), organizacion.getId()));

	    TreeSet<Integer> niveles = new TreeSet<>();
	    List<AsignaturasPorNivelDTO> asignaturasPorNivelDTOLista = new ArrayList<>();
	    for (AsignaturaDTO asignatura : organizacion.getAsignaturasDTO()) {

		List<RequisitoAsignaturaDTO> correquisitos = new ArrayList<>();
		for (RequisitoAsignaturaDTO co : asignatura
		        .getCorRequisitoMalla()) {
		    if (co.getActivo()) {
			correquisitos.add(co);
		    }
		}
		asignatura.getCorRequisitoMalla().clear();
		asignatura.getCorRequisitoMalla().addAll(correquisitos);

		List<RequisitoAsignaturaDTO> prerrequisitos = new ArrayList<>();
		for (RequisitoAsignaturaDTO pre : asignatura
		        .getPreRequisitoMalla()) {
		    if (pre.getActivo()) {
			prerrequisitos.add(pre);
		    }
		}
		asignatura.getPreRequisitoMalla().clear();
		asignatura.getPreRequisitoMalla().addAll(prerrequisitos);

		AsignaturasPorNivelDTO asignaturasPorNivelDTO = new AsignaturasPorNivelDTO();
		if (TipoAsignaturaEnum.ASIGNATURA_COMPUESTA.getValue().equals(
		        asignatura.getTipoAsignatura())) {
		    asignatura.setAsignaturasHijas(registroServicio
			    .obtenerAsignaturaHijas(asignatura.getId()));
		}
		if (asignatura.getIdAsignaturaPadre() == null
		        && asignatura.getNivelMateria() != null) {
		    if (niveles.add(asignatura.getNivelMateria())) {
			List<AsignaturaDTO> asignaturas = new ArrayList<>();
			asignaturas.add(asignatura);
			asignaturasPorNivelDTO.setNivelMateria(asignatura
			        .getNivelMateria());
			asignaturasPorNivelDTO.setAsignaturasDTO(asignaturas);
			asignaturasPorNivelDTOLista.add(asignaturasPorNivelDTO);
		    } else {
			for (AsignaturasPorNivelDTO asignaturaNivel : asignaturasPorNivelDTOLista) {
			    if (asignaturaNivel.getNivelMateria().equals(
				    asignatura.getNivelMateria())) {
				asignaturaNivel.getAsignaturasDTO().add(
				        asignatura);
				break;
			    }
			}
		    }
		}
		if (asignatura.getNivelMateria() == null) {
		    if (niveles.add(9999)) {
			List<AsignaturaDTO> asignaturas = new ArrayList<>();
			asignaturas.add(asignatura);
			asignaturasPorNivelDTO.setNivelMateria(9999);
			asignaturasPorNivelDTO.setAsignaturasDTO(asignaturas);
			asignaturasPorNivelDTOLista.add(asignaturasPorNivelDTO);
		    } else {
			for (AsignaturasPorNivelDTO asignaturaNivel : asignaturasPorNivelDTOLista) {
			    if (new Integer(9999).equals(asignaturaNivel
				    .getNivelMateria())) {
				asignaturaNivel.getAsignaturasDTO().add(
				        asignatura);
				break;
			    }
			}
		    }
		}

	    }
	    organizacion.setAsignaturasPorNivel(asignaturasPorNivelDTOLista);
	}
    }

    /**
     * @return the esCopiaMalla
     */
    public boolean isEsCopiaMalla() {
	return esCopiaMalla;
    }

    /**
     * @param esCopiaMalla
     *            the esCopiaMalla to set
     */
    public void setEsCopiaMalla(boolean esCopiaMalla) {
	this.esCopiaMalla = esCopiaMalla;
    }

    /**
     * @return the idSedeImportacion
     */
    public Long getIdSedeImportacion() {
	return idSedeImportacion;
    }

    /**
     * @param idSedeImportacion
     *            the idSedeImportacion to set
     */
    public void setIdSedeImportacion(Long idSedeImportacion) {
	this.idSedeImportacion = idSedeImportacion;
    }

    /**
     * @return the carreraSeleccionadaImportacion
     */
    public InformacionCarreraDTO getCarreraSeleccionadaImportacion() {
	return carreraSeleccionadaImportacion;
    }

    /**
     * @param carreraSeleccionadaImportacion
     *            the carreraSeleccionadaImportacion to set
     */
    public void setCarreraSeleccionadaImportacion(
	    InformacionCarreraDTO carreraSeleccionadaImportacion) {
	this.carreraSeleccionadaImportacion = carreraSeleccionadaImportacion;
    }

    /**
     * @return the carrerasImportacion
     */
    public List<InformacionCarreraDTO> getCarrerasImportacion() {
	return carrerasImportacion;
    }

    /**
     * @param carrerasImportacion
     *            the carrerasImportacion to set
     */
    public void setCarrerasImportacion(
	    List<InformacionCarreraDTO> carrerasImportacion) {
	this.carrerasImportacion = carrerasImportacion;
    }

    /**
     * @return the mallasImportacion
     */
    public List<MallaCurricularDTO> getMallasImportacion() {
	return mallasImportacion;
    }

    /**
     * @param mallasImportacion
     *            the mallasImportacion to set
     */
    public void setMallasImportacion(List<MallaCurricularDTO> mallasImportacion) {
	this.mallasImportacion = mallasImportacion;
    }

    /**
     * @return the sedesImportacion
     */
    public List<SedeIesDTO> getSedesImportacion() {
	return sedesImportacion;
    }

    /**
     * @param sedesImportacion
     *            the sedesImportacion to set
     */
    public void setSedesImportacion(List<SedeIesDTO> sedesImportacion) {
	this.sedesImportacion = sedesImportacion;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

}
