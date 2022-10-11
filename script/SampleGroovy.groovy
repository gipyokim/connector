package com.konantech.connector.groovy;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konantech.connector.dbg.JdbcUtils;
import com.konantech.connector.util.FilterUtils;
import com.konantech.connector.core.SkipRecordException;

public class SampleGroovy {

	private static final Logger log = LoggerFactory.getLogger(SampleGroovy.class);
	private static final String EMPTY = "";

	public String decision_IUD(String crd, String mod, String del) {
		if ("Y".equals(del))
			return "D";
		else if (crd.compareTo(mod) >= 0)
			return "I";
		else
			return "U";
	}

	public String removeTags(String htmlString) {
		if (htmlString == null || htmlString.length() == 0) {
			return htmlString;
		}
		String strUnEscapeHTML = htmlString.replaceAll("(?is)\\<SCRIPT.*?\\/SCRIPT\\>", "");
		strUnEscapeHTML = strUnEscapeHTML.replaceAll("(?is)\\<STYLE.*?\\/STYLE\\>", "");
		strUnEscapeHTML = strUnEscapeHTML.replaceAll("(?is)\\<!--.*?--\\>", "");
		strUnEscapeHTML = strUnEscapeHTML.replaceAll("(?s)\\<.*?\\>", "");
		strUnEscapeHTML = StringUtils.replace(strUnEscapeHTML,"&nbsp;", " ");
		strUnEscapeHTML = StringEscapeUtils.unescapeHtml4(strUnEscapeHTML);
		strUnEscapeHTML = strUnEscapeHTML.replaceAll("\\s+", " ");
		return strUnEscapeHTML.trim();
	}

	public String setValue(String value) {
		// 	Modify conditions to skip records
		/*if ("".equals(value)) {
			throw new SkipRecordException("Skip Record Value: " + value);
		}*/
		return value;
	}

	public String extractAstriskKey(String src, String s, String e) {
		if (src == null) {
			return null;
		}

		int start = Integer.parseInt(s);
		int end = Integer.parseInt(e);
		int sz = src.length();

		StringBuffer buf = new StringBuffer();
		char separator = ',';

		for (; start < sz && start <= end; start++) {
			buf.append(src.substring(0, start));
			if (start > 0)
				buf.append(separator);
		}
		buf.append(src);
		return buf.toString();
	}

	public String extractKey(String src) {
		if (src == null) {
			return null;
		}

		int csLen = src.length();
		if (csLen == 0) {
			return src;
		}

		// remove trailing whitespaces
		while (csLen > 0 && Character.isWhitespace(src.charAt(csLen - 1))) {
			csLen--;
		}

		StringBuffer buf = new StringBuffer();
		StringBuffer tmp = new StringBuffer(csLen);
		char separator = '|';
		int i = 0;
		for (; i < csLen - 1; i++) {
			char ch = src.charAt(i);
			if (!Character.isWhitespace(ch)) {
				tmp.append(Character.toLowerCase(ch));
				buf.append(tmp.toString());
				buf.append(separator);
			}
		}

		tmp.append(Character.toLowerCase(src.charAt(i)));

		csLen = tmp.length();
		for (i = csLen - 1; i > 0; i--) {
			buf.append(tmp.substring(i, csLen));
			buf.append(separator);
		}

		buf.append(tmp.toString());
		return buf.toString();
	}

	private int getCharacterType(char ch) {
		return Character.isDigit(ch) ? 'D' : Character.isLetter(ch) ? 'L' : 'O';
	}

	public String extractAccuKey(String src) {
		if (src == null) {
			return null;
		}
		int csLen = src.length();
		if (csLen == 0) {
			return src;
		}
		char[] c = src.toCharArray();
		List<String> list = new ArrayList<String>();
		int tokenStart = 0;
		int currentType = getCharacterType(c[tokenStart]);
		for (int pos = tokenStart + 1; pos < c.length; pos++) {
			int type = getCharacterType(c[pos]);
			if (type == currentType) {
				continue;
			}
			list.add(new String(c, tokenStart, pos - tokenStart));
			tokenStart = pos;
			currentType = type;
		}
		list.add(new String(c, tokenStart, c.length - tokenStart));

		char separator = '|';
		StringBuffer buf = new StringBuffer();
		StringBuffer tmp = new StringBuffer();
		while (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				tmp.append(list.get(i).trim());
				buf.append(tmp);
				buf.append(separator);
			}
			list.remove(0);
			tmp.setLength(0);
		}
		return buf.toString();
	}

	/**
	 * Extract text data using text filter. 
	 * You can also specify the filter ID by calling FilterUtils.extractFiledata(filterId, filepath)   
	 */
	public String fileFiltering(String filepath) throws Exception {
		return FilterUtils.extractFiledata(filepath);
	}

	public String spsfileFiltering(String filepath) throws Exception {
		File file = new File(filepath);
		String data = null;
		if (file.isFile()) {
			data = FilterUtils.extractFiledata(filepath);
			file.delete();
		}
		return data;
	}

	public String notesFiltering(String filepaths) throws Exception {
		StringBuffer buffer = new StringBuffer();
		String textSeparator = System.getProperty("separatorText");
		String fileSeparator = System.getProperty("separatorFile");
		String[] attpaths = filepaths.split("\\" + fileSeparator);
		int i = 0;
		for (String attpath : attpaths) {
			if (!"".equals(attpath)) {
				File file = new File(attpath);
				if (i++ > 0) {
					buffer.append(textSeparator);
				}
				buffer.append(FilterUtils.extractFiledata(file));
				file.delete();
			}
		}
		return buffer.toString();
	}

	public String blobFiltering(Object o, String filename) throws Exception {
		File file = null;
		if (o instanceof byte[]) {
			String extension = FilenameUtils.getExtension(filename);
			OutputStream out = null;
			try {
				file = createTempFile("blob", extension, "../temp");
				out = new BufferedOutputStream(new FileOutputStream(file));
				out.write((byte[]) o);
				out.close();
				if (file.length() > 0) {
					return FilterUtils.extractFiledata(file);
				}
			} finally {
				if (file != null) {
					file.delete();
				}
			}
		}
		return EMPTY;
	}

	public String getFtpFile(String path) {
		FTPClient client = null;
		File local = null;
		final String host = "ftp.kaist.ac.kr";
		final int port = 21;
		final String username = "anonymous";
		final String password = "anonymous";

		try {
			client = new FTPClient();
			client.connect(host, port);
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				if (client.login(username, password)) {
					String extension = FilenameUtils.getExtension(path);
					local = createTempFile("blob", extension, "../ftp");
					client.setFileType(FTP.BINARY_FILE_TYPE);
					OutputStream out = new FileOutputStream(local);
					client.retrieveFile(path, out);
					out.close();
					client.logout();

					return local.getAbsolutePath();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (client != null && client.isConnected()) {
				try {
					client.disconnect();
				} catch (IOException e) {
				}
			}
		}
		return EMPTY;
	}

	private static File createTempFile(String prefix, String extension,
			String dirpath) throws IOException {
		File directory = new File(dirpath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return File.createTempFile(prefix, "." + extension, directory);
	}

	public String readURLToString(String source) {
		try {
			return source.toURL().getText("UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Should be declraed as follwings in rc file.
	 * before = SampleGroovy.beforeProcess(@mode@)
	 *
	 * @param mode
	 *          Dump strategy, bulk or inc (incremental)
	 */
	public String beforeProcess(String mode) {
		System.out.println("Before process, " + mode + " mode");
		return selectOne();
	}

	/**
	 * Should be declraed as follwings in rc file.
	 * before = SampleGroovy.afterProcess(@mode@)
	 *
	 * @param mode
	 *          Dump strategy, bulk or inc (incremental)
	 */
	public String afterProcess(String mode) {
		System.out.println("After process, " + mode + " mode");
		return selectOne();
	}

	private String selectOne() {
		// _self references the data source used by this job.
		DataSource ds = JdbcUtils.getDataSource("_self");
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM dual");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// DO SOMETING
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			JdbcUtils.closeQuietly(conn);
		}
		return null;
	}

}
