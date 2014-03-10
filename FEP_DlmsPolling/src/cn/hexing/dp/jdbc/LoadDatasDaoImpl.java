package cn.hexing.dp.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskCode;
import cn.hexing.dp.model.TaskTemplate;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.model.DlmsMeterRtu;

public class LoadDatasDaoImpl implements LoadDatasDao {

	private static final Logger log = Logger.getLogger(LoadDatasDaoImpl.class);
	private SimpleJdbcTemplate simpleJdbcTemplate;		//��ӦdataSource����
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	private String sqlLoadItemRelated;
	private String sqlGetMasterTask;		//��ȡ��վ����
	private String sqlGetMasterTaskById;
	private String sqlGetMasterTaskTemplate; //��ȡ��վ����ģ��
	private String sqlGetMasterTaskTemplateById;
	private String sqlGetTaskTemplate;		//��ȡ�ն�����ģ��
	private String sqlGetRtuTask;			//��ȡ�ն�����������Ϣ
	private String sqlGetRtuTaskById;			//��ȡ�ն�����������Ϣ
	private String sqlGetTaskCodes;         //���ģ��ID��������Ķ�Ӧ  
	private String sqlGetDLMSGPRSMeter;		//��ȡDLMS��GPRS��
	private String sqlGetTaskTemplateById; //����ID����ն�����ģ��
	private ResultMapper<DlmsItemRelated> mapperLoadItemRelated;
	private ResultMapper<TaskTemplate> mapperGetTaskTemplate;
	private ResultMapper<RtuTask> mapperGetRtuTask;
	private ResultMapper<TaskCode>	mapperLoadTaskCodes;
	private ResultMapper<DlmsMeterRtu> mapperGprsMeter;
	
	
	/**
	 * ��ȡ�ն�����ģ��
	 */
	public List<TaskTemplate> getTaskTemplate(String protocol) {
		try{
			ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
				public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTaskTemplate.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetTaskTemplate, rm,protocol);
		}catch(Exception ex){
			log.error("getTaskTemplate error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	/**
	 * ��ȡ�ն�������Ϣ
	 */
	public List<RtuTask> getRtuTask(String protocol) {
		try{
			ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
				public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRtuTask.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetRtuTask, rm,protocol);
		}catch(Exception ex){
			log.error("getRtuTask error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	/**
	 * ���obis��code �Ķ�Ӧ��ϵ
	 * @return
	 */
	public List<DlmsItemRelated> loadDlmsItemRelated(){
		ParameterizedRowMapper<DlmsItemRelated> rowMap = new ParameterizedRowMapper<DlmsItemRelated>(){
			public DlmsItemRelated mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadItemRelated.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadItemRelated, rowMap);
	}
	/**
	 * ��ȡ��վ������Ϣ
	 */
	@Override
	public List<RtuTask> getMasterTask(String protocol) {
		try{
			ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
				public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRtuTask.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetMasterTask, rm,protocol);
		}catch(Exception ex){
			log.error("getRtuTask error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	/**
	 * ��ȡ��վ������Ϣ
	 */
	@Override
	public List<RtuTask> getMasterTaskById(String protocol,String rtuId) {
		try{
			ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
				public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRtuTask.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetMasterTaskById, rm,protocol,rtuId);
		}catch(Exception ex){
			log.error("getRtuTask error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	/**
	 * ��ȡ��վ����ģ��
	 */
	public List<TaskTemplate> getMasterTaskTemplate(String protocol) {
		try{
			ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
				public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTaskTemplate.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetMasterTaskTemplate, rm,protocol);
		}catch(Exception ex){
			log.error("getTaskTemplate error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	@Override
	public List<TaskTemplate> getMasterTaskTemplateById(String protocol,
			String mbid) {

		try{
			ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
				public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTaskTemplate.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetMasterTaskTemplateById, rm,protocol,mbid);
		}catch(Exception ex){
			log.error("getTaskTemplate error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	
	@Override
	public List<TaskCode> getTaskCodes() {
		try {
			ParameterizedRowMapper<TaskCode> rm = new ParameterizedRowMapper<TaskCode>() {
				public TaskCode mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return mapperLoadTaskCodes.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetTaskCodes, rm);
		} catch (Exception ex) {
			log.error("getTaskTemplate error:" + ex.getLocalizedMessage());
		}
		return null;
	}
	public final void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}
	public final void setSqlLoadItemRelated(String sqlLoadItemRelated) {
		this.sqlLoadItemRelated = sqlLoadItemRelated;
	}
	public final void setSqlGetMasterTask(String sqlGetMasterTask) {
		this.sqlGetMasterTask = sqlGetMasterTask;
	}
	public final void setSqlGetMasterTaskTemplate(String sqlGetMasterTaskTemplate) {
		this.sqlGetMasterTaskTemplate = sqlGetMasterTaskTemplate;
	}
	public final void setSqlGetTaskTemplate(String sqlGetTaskTemplate) {
		this.sqlGetTaskTemplate = sqlGetTaskTemplate;
	}
	public final void setSqlGetRtuTask(String sqlGetRtuTask) {
		this.sqlGetRtuTask = sqlGetRtuTask;
	}
	public final void setMapperLoadItemRelated(
			ResultMapper<DlmsItemRelated> mapperLoadItemRelated) {
		this.mapperLoadItemRelated = mapperLoadItemRelated;
	}
	public final void setMapperGetTaskTemplate(
			ResultMapper<TaskTemplate> mapperGetTaskTemplate) {
		this.mapperGetTaskTemplate = mapperGetTaskTemplate;
	}
	public final void setMapperGetRtuTask(ResultMapper<RtuTask> mapperGetRtuTask) {
		this.mapperGetRtuTask = mapperGetRtuTask;
	}
	public final void setSqlGetTaskCodes(String sqlGetTaskCodes) {
		this.sqlGetTaskCodes = sqlGetTaskCodes;
	}
	public final void setMapperLoadTaskCodes(
			ResultMapper<TaskCode> mapperLoadTaskCodes) {
		this.mapperLoadTaskCodes = mapperLoadTaskCodes;
	}
	public void setSqlGetDLMSGPRSMeter(String sqlGetDLMSGPRSMeter) {
		this.sqlGetDLMSGPRSMeter = sqlGetDLMSGPRSMeter;
	}
	public void setMapperGprsMeter(ResultMapper<DlmsMeterRtu> mapperGprsMeter) {
		this.mapperGprsMeter = mapperGprsMeter;
	}
	@Override
	public List<TaskTemplate> getTaskTemplateById(String protocol, String mbid) {

		try{
			ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
				public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTaskTemplate.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetTaskTemplateById, rm,protocol,mbid);
		}catch(Exception ex){
			log.error("getTaskTemplate error:"+ex.getLocalizedMessage());
		}
		return null;
	
	}
	@Override
	public List<RtuTask> getRtuTask(String protocol, String rtuId) {
		try{
			ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
				public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRtuTask.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.sqlGetRtuTaskById, rm,protocol,rtuId);
		}catch(Exception ex){
			log.error("getRtuTask error:"+ex.getLocalizedMessage());
		}
		return null;
	}
	public void setSqlGetTaskTemplateById(String sqlGetTaskTemplateById) {
		this.sqlGetTaskTemplateById = sqlGetTaskTemplateById;
	}
	public void setSqlGetRtuTaskById(String sqlGetRtuTaskById) {
		this.sqlGetRtuTaskById = sqlGetRtuTaskById;
	}
	public void setSqlGetMasterTaskById(String sqlGetMasterTaskById) {
		this.sqlGetMasterTaskById = sqlGetMasterTaskById;
	}
	public void setSqlGetMasterTaskTemplateById(String sqlGetMasterTaskTemplateById) {
		this.sqlGetMasterTaskTemplateById = sqlGetMasterTaskTemplateById;
	}
	@Override
	public List<DlmsMeterRtu> get24HourOnlineMeter() {
		List<DlmsMeterRtu> DlmsMeterRtuList= new ArrayList<DlmsMeterRtu>();
		ParameterizedRowMapper<DlmsMeterRtu> rm = new ParameterizedRowMapper<DlmsMeterRtu>() {
			public DlmsMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGprsMeter.mapOneRow(rs);
			}
		};
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//��ȡϵͳ��24Сʱ�����ߵ�GPRSͨ�ŵ�DLMS��
		String date=sdf.format(new Date());
		DlmsMeterRtuList = simpleJdbcTemplate.query(this.sqlGetDLMSGPRSMeter, rm,Protocol.DLMS,date,date);
		return DlmsMeterRtuList;
	}
}
