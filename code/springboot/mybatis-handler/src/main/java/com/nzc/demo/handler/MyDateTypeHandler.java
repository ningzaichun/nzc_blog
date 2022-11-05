package com.nzc.demo.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Ning zaichun
 */
@Slf4j
@MappedJdbcTypes({JdbcType.BIGINT})  //对应数据库类型
@MappedTypes({Date.class})			  //java数据类型
public class MyDateTypeHandler implements TypeHandler<Date>{

	/**
	 * 入库前的类型转换
	 * @param ps
	 * @param i
	 * @param parameter
	 * @param jdbcType
	 * @throws SQLException
	 */
	@Override
	public void setParameter(PreparedStatement ps, int i, Date parameter,
			JdbcType jdbcType) throws SQLException {
		log.info("setParameter(PreparedStatement ps, int i, Date parameter,JdbcType jdbcType)....");
		log.info("[{}],[{}]",parameter,jdbcType);
		ps.setLong(i, parameter.getTime());
	}

	/**
	 * 查询后的数据处理
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	@Override
	public Date getResult(ResultSet rs, String columnName) throws SQLException {
		log.info("getResult(ResultSet rs, String columnName)....",columnName);
		System.out.println(rs.getLong(columnName));
		return new Date(rs.getLong(columnName));
	}
	@Override
	public Date getResult(ResultSet rs, int columnIndex) throws SQLException {
		log.info("getResult(ResultSet rs, int columnIndex)....");
		return new Date(rs.getLong(columnIndex));
	}
	@Override
	public Date getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		log.info("getResult(CallableStatement cs, int columnIndex)....");
		return cs.getDate(columnIndex);
	}

}

