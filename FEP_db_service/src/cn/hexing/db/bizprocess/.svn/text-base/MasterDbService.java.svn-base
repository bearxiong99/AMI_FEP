package cn.hexing.db.bizprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.procedure.DbProcedure;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.model.RtuCmdItem;
import cn.hexing.fk.model.RtuSetValue;
import cn.hexing.fk.model.RtuSynchronizeItem;
import cn.hexing.fk.model.UpgradeInfo;

/**
 * 业务处理器调用的主站数据库相关的操作。
 *
 */
public class MasterDbService {
	private static final Logger log = Logger.getLogger(MasterDbService.class);
	//Spring配置的属性
	private DataSource dataSource;
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String insertCommandCallResult;		//招测命令结果表插入
	private String insertCommandSetResult;		//设置命令结果表插入
	private String saveAutoTimeResult;			//自动对时结果表插入
	private String updateAutoTimeResult;		//更新自动对时结果
	private String deleteAutoTimeResult;		//删除已经成功对时
	private String insertRtuComdMag;			//终端操作命令管理表插入
	private String insertDlmsRtuComdMag;		//dlms终端操作命令管理表插入	
	private String insertGwAmmeterIdentification;//国网电表认证信息插入
	private DbProcedure funcGetRtuCommandSeq;	//从数据库获取终端下行命令序号。	
	private DbProcedure procUpdateCommandStatus;//更新主站操作命令状态
	private DbProcedure procUpdateParamResult;	//更新主站操作命令的参数设置结果
	private DbProcedure procPostCreateRtuAlert;	//异常后处理存储过程
	private DbProcedure procPostCreateRtuData;	//任务后处理存储过程
	private String sqlGetGwAmmeterIdentification;//国网电表认证信息获取
	private String sqlGetDlmsRtuFreeTag;		//dlms终端空闲标记获取
	private String sqlGetDlmsRtuBpAddr;		//dlms终端空闲标记获取
	private String sqlGetRtuComdItem;			//终端操作命令管理表查询
	private String sqlGetRtuSycItem;			//终端档案同步数据表查询
	private String sqlGetCommandSetResult;			//获取国网终端设置数据项标识(国网设置返回可能不含数据项)
	private String updateGwRtuSetValue;			//更新国网终端设置数据项设置返回结果
	private String updateDlmsRtuFreeTag;		//更新指定bp地址的dlms终端操空闲标记为空闲
	private String sqlupdateGwTerminalPubKey;//更新集中器本身公钥和密钥版本

	private String updateTaskStatus;  // 召测数据之后,更新任务状态
	private String getTaskStatus;//获取CZ_RW；
	private String getTaskIDByComID;
	private String updateCommandStatus; //更新命令状态
	private String sqlQueryRelatedCode; //查找数据项
	private String updateTaskSet;
	 
	private String updateConcentratorPro; //更新集中器升级
	private String updateSoftUpgrade;	//通过告警信息更新升级状态
	private String updateSoftUpgradeByRjsjId;//通过升级ID来更新状态
	private String sqlGetUpgradeInfo;
	private String sqlGetSoftVersion;//获取软件版本信息
	private ResultMapper<UpgradeInfo> mapperGetUpgradeInfo;
	private String sqlGetANSIDimension;//获取ANSI量纲
	
	
	private ResultMapper<RtuCmdItem> mapperGetRtuComdItem;
	private ResultMapper<RtuSetValue> mapperGetRtuSetValue;
	private ResultMapper<RtuSynchronizeItem> mapperGetRtuSycItem;
	
	private String sqlQuerySaveHeartRtu;
	
	
	/**
	 * 获得升级信息-gj_rjsj
	 * @param zdljdz
	 * @param cld
	 * @return
	 */
	public List<UpgradeInfo> getUpgradeInfo(long rjsjid) {
		ParameterizedRowMapper<UpgradeInfo> rm = new ParameterizedRowMapper<UpgradeInfo>(){
			public UpgradeInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetUpgradeInfo.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetUpgradeInfo, rm, rjsjid);
	}
	
	public List<String> getSaveHeartRtu(){
		List<Map<String, Object>> listMap = simpleJdbcTemplate.queryForList(sqlQuerySaveHeartRtu);
		List<String> saveList = new ArrayList<String>();
		for(Map<String,Object> map:listMap){
			for(Object rtu:map.values()){
				saveList.add((String) rtu);
			}
		}
		return saveList;
	}
	/**
	 * 获得软件版本信息-gj_sjwj
	 * @param bbxx
	 * @return 
	 * @return
	 */
	public  String getSoftVersion(String bbxx) {
		String result = "";
		try{
			result = simpleJdbcTemplate.queryForObject(this.sqlGetSoftVersion, String.class,bbxx);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return result;
	}

	/**
	 * 更新国网集中器公钥
	 * @param rtuId
	 * @param pubKey
	 */
	public void updateGwTerminalPubKey(String rtuId,String pubKey,int keyVersion){
		simpleJdbcTemplate.update(sqlupdateGwTerminalPubKey, pubKey,keyVersion,rtuId);
	}
	
	public void updateConcentratorPro(String fileName,String rtuId,String tn,String updateType,Date date,String updateStatus)
	{
		simpleJdbcTemplate.update(updateConcentratorPro, date,updateStatus,fileName,rtuId,tn,updateType);
	}
	public void updateSoftUpgrade(String rtuId,String tn,String rjbbh,String status){
		simpleJdbcTemplate.update(updateSoftUpgrade, status,rtuId,tn,rjbbh);
	}
	
	public void updateSoftUpgradeByRjsjId(long rjsjid,String status){
		simpleJdbcTemplate.update(updateSoftUpgradeByRjsjId,status,rjsjid);
	}
	
	/**
	 * 用于更新任务状态
	 * @param zt
	 * @param mlid
	 */
	public void updateTaskStatus(String zt,String mlid)
	{
		simpleJdbcTemplate.update(this.updateTaskStatus, zt,mlid);
	}
	/**
	 * get mlsl
	 * @param mlid
	 */
	public int getTaskStatus(String mlid){
		int result=0;
		try{
			return result=simpleJdbcTemplate.queryForObject(this.getTaskStatus,Integer.class,mlid);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return result;
	}
	
	public int getANSIDimension(String code){
		int result=0;
		try{
			return result=simpleJdbcTemplate.queryForObject(this.sqlGetANSIDimension,Integer.class,code);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return result;
	}
	
	public int getTaskIDByComID(String mlid){
		int result=0;
		try{
			return result=simpleJdbcTemplate.queryForObject(this.getTaskIDByComID, Integer.class, mlid);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return result;
	}
	public int updateTaskSet(String meterId,String taskNo){
		return this.simpleJdbcTemplate.update(updateTaskSet, meterId,taskNo);
	}
	/**
	 * 用于更新命令状态
	 * @param zt
	 * @param mlid
	 */
	public void updateCommandStatus(String zt,String zdjh,String mlid)
	{
		simpleJdbcTemplate.update(this.updateCommandStatus, zt,zdjh,mlid);
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
	}

	public int insertCommandCallResult(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.insertCommandCallResult, ps);
	}
	public int insertCommandSetResult(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.insertCommandSetResult, ps);
	}
	public int saveAutoTimeResult(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.saveAutoTimeResult, ps);
	}
	public int updateAutoTimeResult(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.updateAutoTimeResult, ps);
	}
	public int deleteAutoTimeResult(String bjjh){
		return simpleJdbcTemplate.update(this.deleteAutoTimeResult,bjjh);
	}
	public int insertGwAmmeterIdentification(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.insertGwAmmeterIdentification, ps);
	}
	public String getGwAmmeterIdentification(String zdjh,String dbdz){		
		return simpleJdbcTemplate.queryForObject(this.sqlGetGwAmmeterIdentification,String.class,zdjh,dbdz);
	}

	public int getDlmsRtuFreeTag(String zdljdz){
		int result=0;
		try{
			return result=simpleJdbcTemplate.queryForObject(this.sqlGetDlmsRtuFreeTag,Integer.class,zdljdz);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return result;
	}
	
	
	public String getRelatedCode(String obis,int class_Id,int attribute_id){
		String result = "";
		try{
			return result = simpleJdbcTemplate.queryForObject(
					sqlQueryRelatedCode, String.class, obis, class_Id, attribute_id);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return result;
	}
	
	public String getDlmsRtuBpAddr(String zdljdz){	
		String bpAddr="";
		try{
			bpAddr=simpleJdbcTemplate.queryForObject(this.sqlGetDlmsRtuBpAddr,String.class,zdljdz);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return bpAddr;
	}
	
	public void updateGwRtuSetValue(String sjxzt,long id,int cldh){
		simpleJdbcTemplate.update(this.updateGwRtuSetValue, sjxzt,id,cldh);
	}
	
	public void setUpdateGwRtuSetValue(String updateGwRtuSetValue) {
		this.updateGwRtuSetValue = updateGwRtuSetValue;
	}



	public void setSqlGetDlmsRtuBpAddr(String sqlGetDlmsRtuBpAddr) {
		this.sqlGetDlmsRtuBpAddr = sqlGetDlmsRtuBpAddr;
	}

	public void setSqlGetDlmsRtuFreeTag(String sqlGetDlmsRtuFreeTag) {
		this.sqlGetDlmsRtuFreeTag = sqlGetDlmsRtuFreeTag;
	}

	public void setInsertGwAmmeterIdentification(
			String insertGwAmmeterIdentification) {
		this.insertGwAmmeterIdentification = insertGwAmmeterIdentification;
	}

	public void insertRtuComdMag(Object obj){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return ;
		}
		try{
			BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
			simpleJdbcTemplate.update(this.insertRtuComdMag, ps);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void insertDlmsRtuComdMag(Object obj){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return ;
		}
		try{
			BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
			simpleJdbcTemplate.update(this.insertDlmsRtuComdMag, ps);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void updateDlmsRtuFreeTagByBpAddr(String bpAddr){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return ;
		}
		try{
			simpleJdbcTemplate.update(this.updateDlmsRtuFreeTag,bpAddr);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	
	public List<RtuCmdItem> getRtuComdItem(String zdljdz,int mlxh) {
		ParameterizedRowMapper<RtuCmdItem> rm = new ParameterizedRowMapper<RtuCmdItem>(){
			public RtuCmdItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuComdItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetRtuComdItem, rm, zdljdz,mlxh);
	}

	public List<RtuSetValue> getGwRtuSetValue(long cmdId) {
		ParameterizedRowMapper<RtuSetValue> rm = new ParameterizedRowMapper<RtuSetValue>(){
			public RtuSetValue mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuSetValue.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetCommandSetResult, rm, cmdId);
	}
	
	public List<RtuSynchronizeItem> getRtuSycItem(Date dt) {
		ParameterizedRowMapper<RtuSynchronizeItem> rm = new ParameterizedRowMapper<RtuSynchronizeItem>(){
			public RtuSynchronizeItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuSycItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetRtuSycItem, rm, dt);
	}
	//终端逻辑地址，终端业务类别，终端规约类型
	public int getRtuCommandSeq(String strRtua,String rtuPotocType){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return 1;
		}
		try{
			return this.funcGetRtuCommandSeq.executeFunctionInt(strRtua,rtuPotocType);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return 1;
	}
	
	public void procUpdateCommandStatus(Object param){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return ;
		}
		try{
			procUpdateCommandStatus.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void procUpdateParamResult(Object param){
		try{
			procUpdateParamResult.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	public void procPostCreateRtuAlert(Object param){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			return ;
		}
		try{
			procPostCreateRtuAlert.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	public void procPostCreateRtuData(Object param){
		try{
			procPostCreateRtuData.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void setFuncGetRtuCommandSeq(DbProcedure funcGetRtuCommandSeq) {
		this.funcGetRtuCommandSeq = funcGetRtuCommandSeq;
	}

	public void setProcUpdateCommandStatus(DbProcedure procUpdateCommandStatus) {
		this.procUpdateCommandStatus = procUpdateCommandStatus;
	}

	public void setProcUpdateParamResult(DbProcedure procUpdateParamResult) {
		this.procUpdateParamResult = procUpdateParamResult;
	}

	public void setInsertRtuComdMag(String insertRtuComdMag) {
		this.insertRtuComdMag = insertRtuComdMag;
	}

	public void setInsertDlmsRtuComdMag(String insertDlmsRtuComdMag) {
		this.insertDlmsRtuComdMag = insertDlmsRtuComdMag;
	}

	public void setSqlGetRtuComdItem(String sqlGetRtuComdItem) {
		this.sqlGetRtuComdItem = sqlGetRtuComdItem;
	}

	public void setMapperGetRtuComdItem(
			ResultMapper<RtuCmdItem> mapperGetRtuComdItem) {
		this.mapperGetRtuComdItem = mapperGetRtuComdItem;
	}

	public void setProcPostCreateRtuAlert(DbProcedure procPostCreateRtuAlert) {
		this.procPostCreateRtuAlert = procPostCreateRtuAlert;
	}

	public void setProcPostCreateRtuData(DbProcedure procPostCreateRtuData) {
		this.procPostCreateRtuData = procPostCreateRtuData;
	}

	public void setSqlGetRtuSycItem(String sqlGetRtuSycItem) {
		this.sqlGetRtuSycItem = sqlGetRtuSycItem;
	}

	public void setMapperGetRtuSycItem(
			ResultMapper<RtuSynchronizeItem> mapperGetRtuSycItem) {
		this.mapperGetRtuSycItem = mapperGetRtuSycItem;
	}

	public void setMapperGetRtuSetValue(
			ResultMapper<RtuSetValue> mapperGetRtuSetValue) {
		this.mapperGetRtuSetValue = mapperGetRtuSetValue;
	}

	public void setSqlGetGwAmmeterIdentification(
			String sqlGetGwAmmeterIdentification) {
		this.sqlGetGwAmmeterIdentification = sqlGetGwAmmeterIdentification;
	}

	public void setInsertCommandCallResult(String insertCommandCallResult) {
		this.insertCommandCallResult = insertCommandCallResult;
	}

	public void setInsertCommandSetResult(String insertCommandSetResult) {
		this.insertCommandSetResult = insertCommandSetResult;
	}
	public void setsaveAutoTimeResult(String saveAutoTimeResult) {
		this.saveAutoTimeResult = saveAutoTimeResult;
	}
	public void setSqlGetCommandSetResult(String sqlGetCommandSetResult) {
		this.sqlGetCommandSetResult = sqlGetCommandSetResult;
	}

	public void setUpdateDlmsRtuFreeTag(String updateDlmsRtuFreeTag) {
		this.updateDlmsRtuFreeTag = updateDlmsRtuFreeTag;
	}

	public String getUpdateTaskStatus() {
		return updateTaskStatus;
	}

	public void setUpdateTaskStatus(String updateTaskStatus) {
		this.updateTaskStatus = updateTaskStatus;
	}

	public String getUpdateCommandStatus() {
		return updateCommandStatus;
	}

	public void setUpdateCommandStatus(String updateCommandStatus) {
		this.updateCommandStatus = updateCommandStatus;
	}
	public String getDeleteAutoTimeResult() {
		return deleteAutoTimeResult;
	}

	public void setDeleteAutoTimeResult(String deleteAutoTimeResult) {
		this.deleteAutoTimeResult = deleteAutoTimeResult;
	}
	public String getSqlQueryRelatedCode() {
		return sqlQueryRelatedCode;
	}
	public void setSqlQueryRelatedCode(String queryRelatedCode) {
		this.sqlQueryRelatedCode = queryRelatedCode;
	}
	public final void setUpdateConcentratorPro(String updateConcentratorPro) {
		this.updateConcentratorPro = updateConcentratorPro;
	}
	public final void setUpdateSoftUpgrade(String updateSoftUpgrade) {
		this.updateSoftUpgrade = updateSoftUpgrade;
	}
	public final void setUpdateSoftUpgradeByRjsjId(String updateSoftUpgradeByRjsjId) {
		this.updateSoftUpgradeByRjsjId = updateSoftUpgradeByRjsjId;
	}
	public void setUpdateAutoTimeResult(String updateAutoTimeResult) {
		this.updateAutoTimeResult = updateAutoTimeResult;
	}

	public final void setSqlupdateGwTerminalPubKey(String sqlupdateGwTerminalPubKey) {
		this.sqlupdateGwTerminalPubKey = sqlupdateGwTerminalPubKey;
	}

	public final void setSqlGetUpgradeInfo(String sqlGetUpgradeInfo) {
		this.sqlGetUpgradeInfo = sqlGetUpgradeInfo;
	}
	public final void setSqlGetSoftVersion(String sqlGetSoftVersion) {
		this.sqlGetSoftVersion = sqlGetSoftVersion;
	}

	public final void setMapperGetUpgradeInfo(
			ResultMapper<UpgradeInfo> mapperGetUpgradeInfo) {
		this.mapperGetUpgradeInfo = mapperGetUpgradeInfo;
	}
	public String getGetTaskStatus() {
		return getTaskStatus;
	}
	public void setGetTaskStatus(String getTaskStatus) {
		this.getTaskStatus = getTaskStatus;
	}
	public String getGetTaskIDByComID() {
		return getTaskIDByComID;
	}
	public void setGetTaskIDByComID(String getTaskIDByComID) {
		this.getTaskIDByComID = getTaskIDByComID;
	}
	public void setUpdateTaskSet(String updateTaskSet) {
		this.updateTaskSet = updateTaskSet;
	}
	public void setSqlGetANSIDimension(String sqlGetANSIDimension) {
		this.sqlGetANSIDimension = sqlGetANSIDimension;
	}
	public String getSqlQuerySaveHeartRtu() {
		return sqlQuerySaveHeartRtu;
	}
	public void setSqlQuerySaveHeartRtu(String sqlQuerySaveHeartRtu) {
		this.sqlQuerySaveHeartRtu = sqlQuerySaveHeartRtu;
	}
	

}
