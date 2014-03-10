package cn.hexing.db.rtu.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.db.rtu.RtuRefreshDao;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.ComRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.model.TaskTemplateItem;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.model.AnsiMeterRtu;

public class JdbcRtuRefreshDao implements RtuRefreshDao {
	//配置属性
	private String sqlGetRtuByRtuId;
	private String sqlGetRtuByRtua;
	private ResultMapper<BizRtu> mapperGetRtu;
	private String sqlGetMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperGetMeasurePoints;
	
	private String sqlGetComRtuByRtua;
	private ResultMapper<ComRtu> mapperGetComRtu;
	
	private String sqlGetDlmsRtuByLogicAddr;
	private ResultMapper<DlmsMeterRtu> mapperGetDlmsRtu;
	
	private String sqlGetAnsiRtuByLogicAddr;
	private ResultMapper<AnsiMeterRtu> mapperGetAnsiRtu;
	
	private String sqlGetMasterTask;
	
	private String sqlGetRtuTask;
	private ResultMapper<RtuTask> mapperGetRtuTask;
	
	private String sqlGetMasterTaskTemplate;
	private ResultMapper<TaskTemplate> mapperGetMasterTaskTemplate;
	
	
	private String sqlGetMasterTaskTemplateItem;
	
	private String sqlGetTaskTemplate;
	private ResultMapper<TaskTemplate> mapperGetTaskTemplate;
	
	private String sqlGetTaskTemplateItem;
	private ResultMapper<TaskTemplateItem> mapperGetTaskTemplateItem;
	
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String sqlGetDLMSGPRSMeter;		//获取DLMS的GPRS表
	private ResultMapper<DlmsMeterRtu> mapperGprsMeter;
	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public List<MeasuredPoint> getMeasurePoints(String zdjh) {
		ParameterizedRowMapper<MeasuredPoint> rm = new ParameterizedRowMapper<MeasuredPoint>(){
			public MeasuredPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetMeasurePoints.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetMeasurePoints, rm, zdjh);
	}
	public List<DlmsMeterRtu> getDlmsMeterRtu(String protocol) {
		ParameterizedRowMapper<DlmsMeterRtu> rm = new ParameterizedRowMapper<DlmsMeterRtu>(){
			public DlmsMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGprsMeter.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetDLMSGPRSMeter, rm, protocol);
	}
	
	public BizRtu getRtu(String zdjh) {
		ParameterizedRowMapper<BizRtu> rm = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtu.mapOneRow(rs);
			}
		};
		BizRtu rtu=null;
		try{
			rtu = simpleJdbcTemplate.queryForObject(this.sqlGetRtuByRtuId, rm, zdjh);
		}catch(Exception ex){		
		}		
		if (rtu!=null){			
			List<MeasuredPoint> mps = getMeasurePoints(rtu.getRtuId());
			for( MeasuredPoint mp: mps )
				rtu.addMeasuredPoint(mp);
			//终端任务列表
			List<RtuTask> tasks = getRtuTasks(rtu.getRtuId());
			for( RtuTask task: tasks )
				rtu.addRtuTask(task);
			tasks = getMaterRtuTasks(rtu.getRtuId());
			for(RtuTask task:tasks)
				rtu.addRtuTask(task);
			
		}
		return rtu;
	}
	public DlmsMeterRtu getDlmsRtuByLogicAddr(String logicAddr){
		ParameterizedRowMapper<DlmsMeterRtu> rm = new ParameterizedRowMapper<DlmsMeterRtu>(){
			public DlmsMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetDlmsRtu.mapOneRow(rs);
			}
		};
		DlmsMeterRtu rtu = null;
		try {
			rtu = simpleJdbcTemplate.queryForObject(
					this.sqlGetDlmsRtuByLogicAddr, rm, logicAddr);
		} catch (Exception e) {}
		if(rtu!=null){
			List<MeasuredPoint> mps= getMeasurePoints(rtu.getMeterId());
			for( MeasuredPoint mp: mps )
				rtu.addMeasuredPoint(mp);
			List<RtuTask> tasks = getRtuTasks(rtu.getMeterId());
			for(RtuTask task : tasks){
				rtu.addRtuTask(task);
			}
			
			List<RtuTask> masterTasks = getMaterRtuTasks(rtu.getMeterId());
			for(RtuTask task : masterTasks){
				rtu.addRtuTask(task);
			}
		}
		return rtu;
	}
	public AnsiMeterRtu getAnsiRtuByLogicAddr(String logicAddr){
		ParameterizedRowMapper<AnsiMeterRtu> rm = new ParameterizedRowMapper<AnsiMeterRtu>(){
			public AnsiMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetAnsiRtu.mapOneRow(rs);
			}
		};
		AnsiMeterRtu rtu = null;
		try {
			rtu = simpleJdbcTemplate.queryForObject(
					this.sqlGetAnsiRtuByLogicAddr, rm, logicAddr);
		} catch (Exception e) {}
		if(rtu!=null){
			List<MeasuredPoint> mps= getMeasurePoints(rtu.getMeterId());
			for( MeasuredPoint mp: mps )
				rtu.addMeasuredPoint(mp);
			List<RtuTask> tasks = getRtuTasks(rtu.getMeterId());
			for(RtuTask task : tasks){
				rtu.addRtuTask(task);
			}
			
			List<RtuTask> masterTasks = getMaterRtuTasks(rtu.getMeterId());
			for(RtuTask task : masterTasks){
				rtu.addRtuTask(task);
			}
		}
		return rtu;
	}
	private List<RtuTask> getMaterRtuTasks(String meterId){
		ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuTask.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetMasterTask, rm, meterId);
	}
	
	public BizRtu getRtu(int rtua) {
		ParameterizedRowMapper<BizRtu> rm = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtu.mapOneRow(rs);
			}
		};
		BizRtu rtu=null;
		try{
			rtu = simpleJdbcTemplate.queryForObject(this.sqlGetRtuByRtua, rm, HexDump.toHex(rtua));
		}catch(Exception ex){		
		}
		if (rtu!=null){			
			List<MeasuredPoint> mps = getMeasurePoints(rtu.getRtuId());
			for( MeasuredPoint mp: mps )
				rtu.addMeasuredPoint(mp);
			//终端任务列表
			List<RtuTask> tasks = getRtuTasks(rtu.getRtuId());
			for( RtuTask task: tasks )
				rtu.addRtuTask(task);		
			List<RtuTask> masterTasks=getMaterRtuTasks(rtu.getRtuId());
			for(RtuTask task:masterTasks)
				rtu.addRtuTask(task);
		}
		return rtu;
	}
	@Override
	public TaskTemplate getMasterTaskTemplate(String templID) {
		ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetMasterTaskTemplate.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.queryForObject(this.sqlGetMasterTaskTemplate, rm, templID);
	}

	@Override
	public List<TaskTemplateItem> getMasterTaskItems(String templID) {
		ParameterizedRowMapper<TaskTemplateItem> rm = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetTaskTemplateItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetMasterTaskTemplateItem, rm, templID);
	}
	
	public ComRtu getComRtu(String logicalAddress) {
		ParameterizedRowMapper<ComRtu> rm = new ParameterizedRowMapper<ComRtu>(){
			public ComRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetComRtu.mapOneRow(rs);
			}
		};
		ComRtu rtu=null;
		try{
			rtu = simpleJdbcTemplate.queryForObject(this.sqlGetComRtuByRtua, rm, logicalAddress);
		}catch(Exception ex){		
		}
		return rtu;
	}

	public List<RtuTask> getRtuTasks(String zdjh) {
		ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuTask.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetRtuTask, rm, zdjh);
	}
	
	public TaskTemplate getTaskTemplate(String templID) {
		ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetTaskTemplate.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.queryForObject(this.sqlGetTaskTemplate, rm, templID);
	}

	public List<TaskTemplateItem> getTaskTemplateItems(String templID) {
		ParameterizedRowMapper<TaskTemplateItem> rm = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetTaskTemplateItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetTaskTemplateItem, rm, templID);
	}



	public void setMapperGetRtu(ResultMapper<BizRtu> mapperGetRtu) {
		this.mapperGetRtu = mapperGetRtu;
	}

	public void setMapperGetMeasurePoints(
			ResultMapper<MeasuredPoint> mapperGetMeasurePoints) {
		this.mapperGetMeasurePoints = mapperGetMeasurePoints;
	}



	public void setMapperGetRtuTask(ResultMapper<RtuTask> mapperGetRtuTask) {
		this.mapperGetRtuTask = mapperGetRtuTask;
	}

	public void setSqlGetTaskTemplate(String sqlGetTaskTemplate) {
		this.sqlGetTaskTemplate = sqlGetTaskTemplate;
	}

	public void setMapperGetTaskTemplate(
			ResultMapper<TaskTemplate> mapperGetTaskTemplate) {
		this.mapperGetTaskTemplate = mapperGetTaskTemplate;
	}

	public void setSqlGetTaskTemplateItem(String sqlGetTaskTemplateItem) {
		this.sqlGetTaskTemplateItem = sqlGetTaskTemplateItem;
	}

	public void setMapperGetTaskTemplateItem(
			ResultMapper<TaskTemplateItem> mapperGetTaskTemplateItem) {
		this.mapperGetTaskTemplateItem = mapperGetTaskTemplateItem;
	}

	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}

	public void setSqlGetRtuByRtuId(String sqlGetRtuByRtuId) {
		this.sqlGetRtuByRtuId = sqlGetRtuByRtuId;
	}
	
	public void setSqlGetRtuByRtua(String sqlGetRtuByRtua) {
		this.sqlGetRtuByRtua = sqlGetRtuByRtua;
	}

	public void setSqlGetComRtuByRtua(String sqlGetComRtuByRtua) {
		this.sqlGetComRtuByRtua = sqlGetComRtuByRtua;
	}

	public void setMapperGetComRtu(ResultMapper<ComRtu> mapperGetComRtu) {
		this.mapperGetComRtu = mapperGetComRtu;
	}
	
	public void setSqlGetMeasurePoints(String sqlGetMeasurePoints) {
		this.sqlGetMeasurePoints = sqlGetMeasurePoints;
	}

	public void setSqlGetRtuTask(String sqlGetRtuTask) {
		this.sqlGetRtuTask = sqlGetRtuTask;
	}

	public final void setSqlGetDlmsRtuByLogicAddr(String sqlGetDlmsRtuByLogicAddr) {
		this.sqlGetDlmsRtuByLogicAddr = sqlGetDlmsRtuByLogicAddr;
	}

	public final void setMapperGetDlmsRtu(
			ResultMapper<DlmsMeterRtu> mapperGetDlmsRtu) {
		this.mapperGetDlmsRtu = mapperGetDlmsRtu;
	}
	
	public final void setSqlGetAnsiRtuByLogicAddr(String sqlGetAnsiRtuByLogicAddr) {
		this.sqlGetAnsiRtuByLogicAddr = sqlGetAnsiRtuByLogicAddr;
	}
	
	public final void setMapperGetAnsiRtu(
			ResultMapper<AnsiMeterRtu> mapperGetAnsiRtu) {
		this.mapperGetAnsiRtu = mapperGetAnsiRtu;
	}
	
	
	public final void setSqlGetMasterTask(String sqlGetMasterTask) {
		this.sqlGetMasterTask = sqlGetMasterTask;
	}

	public final void setSqlGetMasterTaskTemplate(String sqlGetMasterTaskTemplate) {
		this.sqlGetMasterTaskTemplate = sqlGetMasterTaskTemplate;
	}

	public final void setSqlGetMasterTaskTemplateItem(
			String sqlGetMasterTaskTemplateItem) {
		this.sqlGetMasterTaskTemplateItem = sqlGetMasterTaskTemplateItem;
	}

	public final void setMapperGetMasterTaskTemplate(
			ResultMapper<TaskTemplate> mapperGetMasterTaskTemplate) {
		this.mapperGetMasterTaskTemplate = mapperGetMasterTaskTemplate;
	}

	public void setSqlGetDLMSGPRSMeter(String sqlGetDLMSGPRSMeter) {
		this.sqlGetDLMSGPRSMeter = sqlGetDLMSGPRSMeter;
	}

	public void setMapperGprsMeter(ResultMapper<DlmsMeterRtu> mapperGprsMeter) {
		this.mapperGprsMeter = mapperGprsMeter;
	}

}
