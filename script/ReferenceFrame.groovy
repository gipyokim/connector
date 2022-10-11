package com.konantech.connector.groovy;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import com.konantech.connector.dbg.JdbcUtils;

/**
 * Sample groovy implementing reference frame.
 * 
 * SELECT * FROM tbl_doc
 * ---------------------------
 * DOC_ID  SUBJECT  AUTHOR
 * ---------------------------
 *      1  Hello    JK,Yun
 *      2  World    BS,Kim
 * ---------------------------
 *
 * SELECT * FROM tbl_att
 * ---------------------------
 * DOC_ID  ATT_NAME  ATT_PATH
 * ---------------------------
 *      1  11.doc    /doc/1/11
 *      1  22.ppt    /doc/2/22
 *      1  33.xls    /doc/3/33
 * ---------------------------
 * 
 * Job File
 * ---------------------------
 * ...
 * query = SELECT * FROM tbl_doc
 * field_mapping = {
 *     doc_id = doc_id
 *	   subject = subject
 *	   author = author
 *	   att_name = ReferenceFrame.getAttachments(@doc_id@)
 *	   att_path = att_path
 * }
 *
 * FGF File
 * ---------------------------
 * <__doc_id__>1
 * <__subject__>Hello
 * <__author__>JK,Yun
 * <__att_name__>11.doc,22.ppt,33.xls
 * <__att_path__>/doc/1/11,/doc/2/22,/doc/3/33
 */

public class ReferenceFrame {

	DataSource ds = null;

	ReferenceFrame() {
		// _self means calling job's data source
		ds = JdbcUtils.getDataSource("_self");
	}

	public Map getAttachments(String doc_id) {
		final String sql = "SELECT att_name, att_path FROM tbl_att WHERE doc_id = ?";
		Map result = new HashMap();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, doc_id);
			rs = pstmt.executeQuery();
			
			StringBuilder name = new StringBuilder();
			StringBuilder path = new StringBuilder();
			int i = 0;
			while (rs.next()) {
				if (i++ > 0) {
					name.append(",");
					path.append(",");
				}
				name.append(rs.getString("att_name"));
				path.append(rs.getString("att_path"));
			}
			rs.close();
			pstmt.close();

			result.put("att_name", name.toString());
			result.put("att_path", path.toString());
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (conn != null)
				JdbcUtils.closeQuietly(conn);
		}
		return result;
	}

}