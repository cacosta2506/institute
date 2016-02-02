package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.OrganizacionCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.RequisitoAsignaturaDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdministrarInformacionIesFaseController implements Serializable {

    /**
     * @author eviscarra
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarInformacionIesFaseController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private List<InformacionIesDTO> listaIes;
    private InformacionIesDTO informacionIes;

    private Long idFase;
    private List<FaseDTO> listaFases;
    private List<ProyectoDTO> listaProyectos;
    private List<DocenteDTO> listaDocente;
    private List<DocenteAsignaturaDTO> listaDocenteAsignatura;

    private OrganizacionCurricularDTO organizacionCurricular;
    private RequisitoAsignaturaDTO requisitoMalla;
    private List<MallaCurricularDTO> listaMallaCurricular;
    private Long idMalla;
    private FaseIesDTO faseIesDTO;
    private List<FaseIesDTO> listaIesFase;

    private FaseIesDTO informacionIesFase;

    public AdministrarInformacionIesFaseController() {
	this.listaIesFase = new ArrayList<FaseIesDTO>();
	this.listaIes = new ArrayList<InformacionIesDTO>();
	this.informacionIesFase = new FaseIesDTO();
	this.listaFases = new ArrayList<FaseDTO>();
	this.informacionIes = new InformacionIesDTO();
	this.organizacionCurricular = new OrganizacionCurricularDTO();
	this.requisitoMalla = new RequisitoAsignaturaDTO();
	this.listaMallaCurricular = new ArrayList<MallaCurricularDTO>();
	this.listaProyectos = new ArrayList<>();
	this.listaDocente = new ArrayList<>();
    }

    @PostConstruct
    public void start() {
	try {
	    LOG.info("1");
	    // listaIesFase = institutosServicio.obtenerInformacionIes();
	    listaIes = registroServicio.obtenerIes();
	    listaFases = catalogoServicio.obtenerFases();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void registrarInformacionIesFase() {

	if (this.informacionIes != null) {
	    // getInformacionIesFase().setInformacionIesDTO(informacionIes);
	    if (this.idFase != null) {
		FaseDTO faseE = new FaseDTO();
		faseE.setId(idFase);
		// getInformacionIesFase().setFaseEvaluacionDTO(faseE);
		if (this.informacionIesFase.getFechaInicio().before(
		        this.informacionIesFase.getFechaFin())) {
		    try {
			// Auditoria
			AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
			auditoriaDTO.setUsuarioModificacion("eviscarra");
			informacionIesFase.setAuditoriaDTO(auditoriaDTO);
			InformacionIesDTO informacionIesDTO = new InformacionIesDTO();
			// informacionIesDTO.setId(informacionIesFase
			// .getInformacionIesDTO().getId());
			// informacionIesDTO.setIes(informacionIesFase
			// .getInformacionIesDTO().getIes());
			informacionIesDTO.setAuditoria(auditoriaDTO);
			// informacionIesDTO.setIdFaseActual(informacionIesFase
			// .getFaseEvaluacionDTO().getId());
			// institutosServicio.crearActualizar(informacionIesFase);
			registroServicio.crearActualizar(informacionIesDTO);
			// this.informacionIesFase = new
			// InformacionIesFaseDTO();

			// try {
			// this.listaDocenteAsignatura = registroServicio
			// .obtenerAsignaturaPorDocente(57788L);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			LOG.info("listaDocenteAsignatura: "
			        + listaDocenteAsignatura.size());
			LOG.info("ASIGNATURA: "
			        + listaDocenteAsignatura.get(0)
			                .getAsignaturaDTO().getNombre());
			LOG.info("DOCENTE: "
			        + listaDocenteAsignatura.get(0).getDocenteDTO()
			                .getApellidoPaterno());
			JsfUtil.msgInfo("Registro almacenado correctamente");
		    } catch (ServicioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    } catch (Exception e) {
			JsfUtil.msgError("Error al guardar,  consulte con el administrador");
			e.printStackTrace();
		    }
		} else {
		    JsfUtil.msgError("La Fecha Fin no puede ser menor a la Fecha Inicio");
		}
	    }
	}
    }

    public void nuevaInformacionCarreraSede() {

    }

    public void editarInformacionCarreraSede() {

    }

    public void eliminarInformacionCarreraSede() {

    }

    public void guardarInformacionCarreraSede() {

    }

    public void salirInformacionCarreraSedeDetalle() {

    }

    public OrganizacionCurricularDTO getOrganizacionCurricular() {
	return organizacionCurricular;
    }

    public void setOrganizacionCurricular(
	    OrganizacionCurricularDTO organizacionCurricular) {
	this.organizacionCurricular = organizacionCurricular;
    }

    public RequisitoAsignaturaDTO getRequisitoMalla() {
	return requisitoMalla;
    }

    public void setRequisitoMalla(RequisitoAsignaturaDTO requisitoMalla) {
	this.requisitoMalla = requisitoMalla;
    }

    public List<MallaCurricularDTO> getListaMallaCurricular() {
	return listaMallaCurricular;
    }

    public void setListaMallaCurricular(
	    List<MallaCurricularDTO> listaMallaCurricular) {
	this.listaMallaCurricular = listaMallaCurricular;
    }

    public Long getIdMalla() {
	return idMalla;
    }

    public void setIdMalla(Long idMalla) {
	this.idMalla = idMalla;
    }

    public List<FaseIesDTO> getListaIesFase() {
	return listaIesFase;
    }

    public void setListaIesFase(List<FaseIesDTO> listaIesFase) {
	this.listaIesFase = listaIesFase;
    }

    public FaseIesDTO getInformacionIesFase() {
	return informacionIesFase;
    }

    public void setInformacionIesFase(FaseIesDTO informacionIesFase) {
	this.informacionIesFase = informacionIesFase;
    }

    public List<InformacionIesDTO> getListaIes() {
	return listaIes;
    }

    public void setListaIes(List<InformacionIesDTO> listaIes) {
	this.listaIes = listaIes;
    }

    public Long getIdFase() {
	return idFase;
    }

    public void setIdFase(Long idFase) {
	this.idFase = idFase;
    }

    public List<FaseDTO> getListaFases() {
	return listaFases;
    }

    public void setListaFases(List<FaseDTO> listaFases) {
	this.listaFases = listaFases;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public List<ProyectoDTO> getListaProyectos() {
	return listaProyectos;
    }

    public void setListaProyectos(List<ProyectoDTO> listaProyectos) {
	this.listaProyectos = listaProyectos;
    }

    public List<DocenteDTO> getListaDocente() {
	return listaDocente;
    }

    public void setListaDocente(List<DocenteDTO> listaDocente) {
	this.listaDocente = listaDocente;
    }

    public List<DocenteAsignaturaDTO> getListaDocenteAsignatura() {
	return listaDocenteAsignatura;
    }

    public void setListaDocenteAsignatura(
	    List<DocenteAsignaturaDTO> listaDocenteAsignatura) {
	this.listaDocenteAsignatura = listaDocenteAsignatura;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

}
