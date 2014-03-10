package cn.hexing.db.initrtu.dao;

import java.util.List;

import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;

import com.hx.ansi.model.AnsiMeterRtu;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-5-13 上午10:03:24
 * @version 1.0 
 */

public interface AnsiMeterRtuDao {
	/**获得ANSI表档案*/
	public List<AnsiMeterRtu> loadAnsiMeterRtu();
	
	/**加载测量点*/
	public List<MeasuredPoint> loadMeasuredPoints();
	
	/**加载终端任务*/
	public List<RtuTask> loadRtuTasks();
	
	/**加载主站任务*/
	public List<RtuTask> loadMasterTasks();
}
