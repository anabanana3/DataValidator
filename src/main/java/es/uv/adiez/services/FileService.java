package es.uv.adiez.services;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.uv.adiez.domain.File;
import es.uv.adiez.domain.FileSQL;
import es.uv.adiez.domain.User;

import es.uv.adiez.config.RabbitConfiguration;

@Service
public class FileService {
	@Value("${enpoint.usersAPI}")
	private String usersURL;
	@Value("${enpoint.filesAPI}")
	private String filesURL;
	private Gson gson = new Gson();
	
	@Autowired RabbitConfiguration rabbit;
	/**************************************************************/
	/*							MONGO							  */
	/**************************************************************/
	
	public List<File>  findByStatus(File.Status status) {
		List<File> files = new ArrayList<File>();
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+filesURL+"/files/status/"+status;
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return files;
		}
	     
	    ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 200) {
		   Type fileListType = new TypeToken<ArrayList<File>>(){}.getType();

		   files = (List<File>) gson.fromJson(result.getBody(), fileListType);
	   }
	   return files;
	}
	
	public File  findById(String id) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+filesURL+"/files/"+id;
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	     
	    ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 200) {
		  return gson.fromJson(result.getBody(), File.class);
	   }
	   return null;
	}
	
	public List<File> getPending(File.Status status) {
		List<File> files = findByStatus(status);
		List<File> fs = new ArrayList<File>();
		files.stream().forEach(f -> {
			FileSQL fsql = findByIdSQL(f.getId());
			if(fsql != null)f.setProducer(fsql.getOwner().getName());
			f.setData(f.getData().stream().limit(10).collect(Collectors.toList()));
			fs.add(f);
		});
		return fs;
	}
	
	public File updateStatus(File file, File.Status status) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+filesURL+"/files";
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new File();
		}
	    file.setStatus(status);
	    ResponseEntity<String> result = restTemplate.postForEntity(uri, file, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 201) {
		   return gson.fromJson(result.getBody(), File.class);
	   }
	   return new File();
	}
	
	public void prepareFile(String id, Boolean error) {
		File f = findById(id);
		//Actualizamos el estado
		updateStatus(f, File.Status.preparing);
		if(error) f.setStatus(File.Status.error);
		//enviamos el fichero a rabbit
		rabbit.sendData(gson.toJson(f, File.class));
	}
	/**************************************************************/
	/*							SQL 							  */
	/**************************************************************/
	public List<FileSQL>  findByOwnerSQL(User u) {
		List<FileSQL> files = new ArrayList<FileSQL>();
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/fileAPI/owner/"+u.getNif();
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return files;
		}
	     
	    ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 200) {
		   Type fileListType = new TypeToken<ArrayList<FileSQL>>(){}.getType();

		   files = (List<FileSQL>) gson.fromJson(result.getBody(), fileListType);
	   }
	   return files;
	}
	
	public FileSQL  findByIdSQL(String id) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/fileAPI/"+id;
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	     
	    ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 200) {
		  return gson.fromJson(result.getBody(), FileSQL.class);
	   }
	   return null;
	}
	
	public List<String> findIdsByOwner(User u)
	{
		List<FileSQL> files = findByOwnerSQL(u);
		List<String> ids = files.stream().map(f -> f.getFileId())
                .collect(Collectors.toList());
		return ids;
	}	
	
	public Boolean isMaxFilesReached(User u) {
		List<FileSQL> files = findByOwnerSQL(u);
		if(files.size() >= u.getQuantity()) return true;
		else return false;
	}
	
	public Boolean hasFilesWithStatus(User u, File.Status status) {
		Boolean hasFiles = false;
		List<String> ids = findIdsByOwner(u);
		List<File> files = findByStatus(status);
		List<Boolean> result = new ArrayList<Boolean>();
		result.add(hasFiles);
		files.stream().forEach(f -> {
			if(ids.contains(f.getId())) {
				result.set(0, true);
			}
		});
		return result.get(0);
	}
}
