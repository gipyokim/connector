package com.konantech.connector.groovy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import SCSL.SLBsUtil;
import SCSL.SLDsFile;

import com.konantech.connector.core.TextFilter;
import com.konantech.connector.util.Config;
import com.konantech.connector.util.FilterUtils;

public class SoftcampDRM {
	
	/**
	 * Change following environment variables.
	 */
	private static final String IDDAC	= "SECURITYDOMAIN";
	private static final String KEYFILE = "C:\\konan\\softcamp\\keyfile\\keyDAC.sc";
	private static final String DRMPROC = "C:\\konan\\softcamp\\softcamp.properties";
	private static final String DRMDIR	= "C:\\konan\\connector\\drm";
	
	SLDsFile mSLDsFile = null;
	SLBsUtil mSLBsUtil = null;
	
	public SoftcampDRM() throws IOException {
		FileUtils.forceMkdir(new File(DRMDIR));

		mSLDsFile = new SLDsFile();
		mSLBsUtil = new SLBsUtil();

		mSLDsFile.SLDsInitDAC();
		mSLDsFile.SettingPathForProperty(DRMPROC);
	}
	
	public String fileFiltering(String filePath) throws Exception {
		return fileFiltering(FilenameUtils.getName(filePath), filePath);
	}
	
	public String fileFiltering(String fileName, String filePath)
			throws Exception {
		String fileBody = StringUtils.EMPTY;
		if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(filePath)) {
			return fileBody;
		}
		File srcFile = new File(filePath);
		if (srcFile.exists()) {
			TextFilter tf = Config.getFilter(FilterUtils.DEFAULT_FILTER);
			if (tf != null && tf.accept(fileName)) {
				int rc = mSLBsUtil.isEncryptFile(filePath);
				if (rc != 0) {
					// encrypted
					String tempFilePath = DRMDIR + "\\" + fileName;
					rc = mSLDsFile.CreateDecryptFileDAC(KEYFILE, IDDAC,
							filePath, tempFilePath);
					if (rc == 0) {
						fileBody = FilterUtils.extractFiledata(tempFilePath);
						new File(tempFilePath).delete();
					}
				} else {
					fileBody = FilterUtils.extractFiledata(filePath);
				}
			}
		}
		return fileBody;
	}

}
