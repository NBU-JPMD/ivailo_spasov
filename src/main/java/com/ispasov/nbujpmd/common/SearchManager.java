package com.ispasov.nbujpmd.common;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class SearchManager implements Serializable {
	private static final Logger LOG = Logger.getLogger(SearchManager.class.getName());
	private static final String INDEXFILE = "index.dat";
	private static final long serialVersionUID = 70;

	private static SearchManager instance = null;
	private final Map<String, List<String>> index = new ConcurrentHashMap<>();
	private transient Object indexObject;
	private transient Method indexMethod;

	protected SearchManager() {
	}

	public synchronized static SearchManager getInstance() {
		if(instance == null) {
			try (FileInputStream fin = new FileInputStream(INDEXFILE);
				 ObjectInputStream ois = new ObjectInputStream(fin)) {
				instance = (SearchManager) ois.readObject();
			} catch (Exception e) {
				System.out.println(INDEXFILE + " not found. Creating new empty file.");
				instance = new SearchManager();
				instance.save();
			}
		}
		return instance;
	}

	public void loadPlugin(String className)
		 throws ClassNotFoundException, InstantiationException,
		 IllegalAccessException, NoSuchMethodException {
		ClassLoader classLoader = SearchManager.class.getClassLoader();
		Class<?> aClass = classLoader.loadClass(className);
		indexObject = aClass.newInstance();
		indexMethod = aClass.getDeclaredMethod("index", new Class<?>[] { String.class });
	}

	public synchronized void save() {
		try (FileOutputStream fin = new FileOutputStream(INDEXFILE);
			 ObjectOutputStream ois = new ObjectOutputStream(fin)) {
			ois.writeObject(this);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.toString(), e);
		}
	}

	private void addKeyword(String keyword, String fileName) {
		List<String> list = Collections.synchronizedList(new ArrayList<String>());
		list.add(fileName);
		list = index.putIfAbsent(keyword, list);
		if(list != null) {
			list.add(fileName);
		}
	}

	private Stream<String> pluginIndex(String line) {
		try {
			return Arrays.stream((String[])indexMethod.invoke(indexObject, line));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean pluginIndexFile(String fileName) {
		try {
			Set<String> keywords = Files.lines(Paths.get(fileName))
				 .flatMap(line -> pluginIndex(line))
				 .collect(Collectors.toSet());

			keywords.forEach(keyword -> addKeyword(keyword, fileName));
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		} catch (RuntimeException re) {
			LOG.log(Level.SEVERE, re.getCause().toString(), re.getCause());
			return false;
		}
		return true;
	}

	private String defaultIndex(String line) {
		String[] ret = line.split("\\s+");
		if(ret.length > 0){
			return ret[0];
		} else {
			return null;
		}
	}

	public boolean isPluginLoaded() {
		return indexObject != null && indexMethod != null;
	}

	public void indexFile(String fileName) {
		if (isPluginLoaded() == true && pluginIndexFile(fileName) == true) {
			return;
		}

		try {
			Set<String> keywords = Files.lines(Paths.get(fileName))
				 .map(line -> defaultIndex(line))
				 .filter(keyword -> keyword != null)
				 .collect(Collectors.toSet());

			keywords.forEach(keyword -> addKeyword(keyword, fileName));
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	public List<String> getFiles(String keyword) {
		return index.get(keyword);
	}
}