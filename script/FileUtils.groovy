package com.konantech.connector.groovy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

	public Map getBasicFileAttributes(String path) {
		Map result = new HashMap(2);
		try {
			BasicFileAttributes attr = Files.readAttributes(Paths.get(path), BasicFileAttributes.class);
			result.put("FILECREATEDATE", attr.creationTime().toMillis());
			result.put("FILEACCESSDATE", attr.lastAccessTime().toMillis());
		} catch (Exception ex) {
			long lastModified = new File(path).lastModified();
			result.put("FILECREATEDATE", lastModified);
			result.put("FILEACCESSDATE", lastModified);
		}
		return result;
	}

}