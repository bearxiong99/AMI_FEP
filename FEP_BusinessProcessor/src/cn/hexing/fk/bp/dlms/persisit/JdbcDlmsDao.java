package cn.hexing.fk.bp.dlms.persisit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.model.DlmsAlarmStatus;
import cn.hexing.fk.bp.model.DlmsCommand;
import cn.hexing.fk.bp.model.DlmsMultiScale;
import cn.hexing.fk.model.DlmsItemRelated;

public class JdbcDlmsDao 
{
	private SimpleJdbcTemplate simpleJdbcTemplate;		//��ӦdataSource����
	
	private String sqlLoadScaleItem;
	/**����������Ӧ*/
	private String sqlLoadItemRelated;
	/**���¼�����Կ*/
	private String sqlUpdateEncriptKey;
	/**ͨ������Ż������*/
	private String sqlLoadDlmsCommandByTaskNo;
	/**����Esam����Կ�汾*/
	private String sqlUpdateEsamKeyVersion;
	/**����ͨ������*/
	private String sqlUpdateChannelSet;
	/**��С�����*/
	private String sqlLoadBlockRelatedSmall;
	/**���DLMS�澯״̬*/
	private String sqlLoadDlmsAlarmStatus;
	/**��ö�����*/
	private String sqlLoadDlmsMultiScaleGroupByType;
	
	/**���Dlms���ͺŶ�Ӧ�İ汾��*/
	private String sqlLoadDlmsMeterModeMapVersion;
	
	private ResultMapper<DlmsAlarmStatus> mapperLoadDlmsAlarmStatus;
	
	private ResultMapper<DlmsCommand> mapperLoadDlmsCommand;
	
	private ResultMapper<DlmsScaleItem> mapperLoadScaleItem;
	
	private ResultMapper<DlmsItemRelated> mapperLoadItemRelated;
	
	private ResultMapper<DlmsMultiScale> mapplerLoadMultiScale;
	
	
	public Map<String,List<DlmsMultiScale>> loadDlmsMultiScale(){
		ParameterizedRowMapper<DlmsMultiScale> rowMap = new ParameterizedRowMapper<DlmsMultiScale>(){
			public DlmsMultiScale mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapplerLoadMultiScale.mapOneRow(rs);
			}
		};
		//�����ݿ��ｫ�������-ITEM_ID��Ӧ������ȡ����
		List<DlmsMultiScale> result = this.simpleJdbcTemplate.query(sqlLoadDlmsMultiScaleGroupByType, rowMap);
		Map<String,List<DlmsMultiScale>> resultMap = new HashMap<String, List<DlmsMultiScale>>();
		for(DlmsMultiScale model:result){
			String key = model.getItemId();
			model.setItemId(key);
			List<DlmsMultiScale> list = resultMap.get(key);
			if(list==null){
				list = new ArrayList<DlmsMultiScale>();
			}
			list.add(model);
			resultMap.put(key, list);
		}
		return resultMap;
	}
	
	public List<DlmsAlarmStatus> loadDlmsAlarmStatus(){
		ParameterizedRowMapper<DlmsAlarmStatus> rowMap = new ParameterizedRowMapper<DlmsAlarmStatus>(){
			public DlmsAlarmStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadDlmsAlarmStatus.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadDlmsAlarmStatus, rowMap);
	}
	
	/**
	 * ���ͺŶ�Ӧ�İ汾�ţ����ڶ����ٴ���
	 * @return
	 */
	public Map<String,String> getDlmsMeterModeMapVersion(){
		//M_MODLE  M_S_VERSION
		List<Map<String, Object>> listMap =simpleJdbcTemplate.queryForList(sqlLoadDlmsMeterModeMapVersion);
		Map<String,String> result = new HashMap<String, String>();
		for(int i=0;i<listMap.size();i++){
			Map<String, Object> temp = listMap.get(i);
			String mode=(String) temp.get("M_MODLE");
			String version=(String)temp.get("M_S_VERSION");
			result.put(mode.toUpperCase(), version.toUpperCase());
		}
		return result;
	}
	
	/**
	 * �������С���Ӧ
	 * @return
	 */
	public Map<String,List<String>> getBlockRelatedSmall(){
		List<Map<String, Object>> listMap = simpleJdbcTemplate.queryForList(sqlLoadBlockRelatedSmall);
		Map<String,List<String>> mapList = new HashMap<String, List<String>>();
		for(int i=0;i<listMap.size();i++){
			Map<String,Object> map=listMap.get(i);
			String bigItem=(String) map.get("DXSJXBM");
			String smallItem=(String)map.get("XXSJXBM");
			List<String> smallItems=mapList.get(bigItem);
			if(smallItems==null) smallItems = new ArrayList<String>();
			if(!smallItems.contains(smallItem))
				smallItems.add(smallItem);
			mapList.put(bigItem, smallItems);
		}
		return mapList;
	}
	
	public int updateChannelSet(String meterId,String channelNum){
		return this.simpleJdbcTemplate.update(sqlUpdateChannelSet, meterId,channelNum);
	}
	
	public int updateEncriptKey(String meterId,String newEncKey){
		return this.simpleJdbcTemplate.update(sqlUpdateEncriptKey, newEncKey,meterId);
	}
	
	public int updateEsamKeyVersion(String meterId,int keyVersion){
		return this.simpleJdbcTemplate.update(sqlUpdateEsamKeyVersion, keyVersion,meterId);
	}
	
	public List<DlmsScaleItem> loadDlmsScaleItem() {
		ParameterizedRowMapper<DlmsScaleItem> rowMap = new ParameterizedRowMapper<DlmsScaleItem>(){
			public DlmsScaleItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadScaleItem.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadScaleItem, rowMap);
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
	
	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
		return simpleJdbcTemplate;
	}

	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}

	public String getSqlLoadScaleItem() {
		return sqlLoadScaleItem;
	}

	public void setSqlLoadScaleItem(String sqlLoadScaleItem) {
		this.sqlLoadScaleItem = sqlLoadScaleItem;
	}

	public ResultMapper<DlmsScaleItem> getMapperLoadScaleItem() {
		return mapperLoadScaleItem;
	}

	public void setMapperLoadScaleItem(
			ResultMapper<DlmsScaleItem> mapperLoadScaleItem) {
		this.mapperLoadScaleItem = mapperLoadScaleItem;
	}


	public ResultMapper<DlmsItemRelated> getMapperLoadItemRelated() {
		return mapperLoadItemRelated;
	}


	public void setMapperLoadItemRelated(
			ResultMapper<DlmsItemRelated> mapperLoadItemRelated) {
		this.mapperLoadItemRelated = mapperLoadItemRelated;
	}
	public String getSqlLoadItemRelated() {
		return sqlLoadItemRelated;
	}
	public void setSqlLoadItemRelated(String sqlLoadItemRelated) {
		this.sqlLoadItemRelated = sqlLoadItemRelated;
	}

	public final String getSqlUpdateEncriptKey() {
		return sqlUpdateEncriptKey;
	}

	public final void setSqlUpdateEncriptKey(String sqlUpdateEncriptKey) {
		this.sqlUpdateEncriptKey = sqlUpdateEncriptKey;
	}

	public List<DlmsCommand> loadDlmsCommandsByTaskNo(long taskNo) {
		ParameterizedRowMapper<DlmsCommand> rowMap = new ParameterizedRowMapper<DlmsCommand>(){
			public DlmsCommand mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadDlmsCommand.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadDlmsCommandByTaskNo, rowMap,taskNo);
	}

	public final void setSqlLoadDlmsCommandByTaskNo(
			String sqlLoadDlmsCommandByTaskNo) {
		this.sqlLoadDlmsCommandByTaskNo = sqlLoadDlmsCommandByTaskNo;
	}

	public final void setMapperLoadDlmsCommand(
			ResultMapper<DlmsCommand> mapperLoadDlmsCommand) {
		this.mapperLoadDlmsCommand = mapperLoadDlmsCommand;
	}

	public final void setSqlUpdateEsamKeyVersion(String sqlUpdateEsamKeyVersion) {
		this.sqlUpdateEsamKeyVersion = sqlUpdateEsamKeyVersion;
	}

	public final void setSqlUpdateChannelSet(String sqlUpdateChannelSet) {
		this.sqlUpdateChannelSet = sqlUpdateChannelSet;
	}

	public final void setSqlLoadBlockRelatedSmall(String sqlLoadBlockRelatedSmall) {
		this.sqlLoadBlockRelatedSmall = sqlLoadBlockRelatedSmall;
	}

	public final void setSqlLoadDlmsAlarmStatus(String sqlLoadDlmsAlarmStatus) {
		this.sqlLoadDlmsAlarmStatus = sqlLoadDlmsAlarmStatus;
	}

	public final void setMapperLoadDlmsAlarmStatus(
			ResultMapper<DlmsAlarmStatus> mapperLoadDlmsAlarmStatus) {
		this.mapperLoadDlmsAlarmStatus = mapperLoadDlmsAlarmStatus;
	}

	public void setMapplerLoadMultiScale(
			ResultMapper<DlmsMultiScale> mapplerLoadMultiScale) {
		this.mapplerLoadMultiScale = mapplerLoadMultiScale;
	}

	public void setSqlLoadDlmsMultiScaleGroupByType(
			String sqlLoadDlmsMultiScaleGroupByType) {
		this.sqlLoadDlmsMultiScaleGroupByType = sqlLoadDlmsMultiScaleGroupByType;
	}

	public String getSqlLoadDlmsMeterModeMapVersion() {
		return sqlLoadDlmsMeterModeMapVersion;
	}

	public void setSqlLoadDlmsMeterModeMapVersion(
			String sqlLoadDlmsMeterModeMapVersion) {
		this.sqlLoadDlmsMeterModeMapVersion = sqlLoadDlmsMeterModeMapVersion;
	}

}	
