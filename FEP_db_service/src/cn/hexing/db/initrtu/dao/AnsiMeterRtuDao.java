package cn.hexing.db.initrtu.dao;

import java.util.List;

import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;

import com.hx.ansi.model.AnsiMeterRtu;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-5-13 ����10:03:24
 * @version 1.0 
 */

public interface AnsiMeterRtuDao {
	/**���ANSI����*/
	public List<AnsiMeterRtu> loadAnsiMeterRtu();
	
	/**���ز�����*/
	public List<MeasuredPoint> loadMeasuredPoints();
	
	/**�����ն�����*/
	public List<RtuTask> loadRtuTasks();
	
	/**������վ����*/
	public List<RtuTask> loadMasterTasks();
}
