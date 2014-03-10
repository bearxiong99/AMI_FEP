package cn.hexing.auto.control.imp;


import javax.sql.DataSource;

import cn.hexing.auto.control.ICodeCreator;

public abstract class AbstractCodeCreator implements ICodeCreator{

	DataSource dataSource ;

	public AbstractCodeCreator(DataSource dataSource){
		this.dataSource = dataSource;
	};
	
}
