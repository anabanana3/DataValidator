package es.uv.adiez.services;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.uv.adiez.domain.File;
import es.uv.adiez.domain.User;

@Service
public class UserService {
	@Value("${enpoint.usersAPI}")
	private String usersURL;
	@Value("${enpoint.filesAPI}")
	private String filesURL;
	
	@Autowired FileService fs; 
	private Gson gson = new Gson();
	
	public List<User> findProducers(String status){
		List<User> users = new ArrayList<User>();
		switch(status) {
			case "0": //all
				users = findAllProducers();
				break;
			case "1": //pending
				users = findAllPendingProducers();
				break;
			case "2": //quantity exceeded
				users = findQuantityExceededProducers();
				break;
			case "3": //with errors
				users = findProducersWithErrors();
				break;
		}
		return users;
	}
	public List<User> findAllProducers(){
		List<User> users = new ArrayList<User>();
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/userAPI/type/"+User.UserType.P;
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
		   Type userListType = new TypeToken<ArrayList<User>>(){}.getType();

		   users = (List<User>) gson.fromJson(result.getBody(), userListType);
	   }
	   return users;
	}
	
	public List<User> findAllPendingProducers(){
		List<User> all = findAllProducers();
		List<User> users = all.stream().filter(u -> u.getStatus().equals(User.Status.P)).collect(Collectors.toList());
		
	   return users;
	}
	
	public List<User> findQuantityExceededProducers(){
		List<User> all = findAllProducers();
		List<User> users = new ArrayList<User>();
		all.stream().forEach(u -> {
			if(fs.isMaxFilesReached(u)) users.add(u);
		});
	   return users;
	}
	
	public List<User> findProducersWithErrors(){
		List<User> all = findAllProducers();
		List<User> users = new ArrayList<User>();
		all.stream().forEach(u -> {
			if(fs.hasFilesWithStatus(u, File.Status.error)) users.add(u);
		});
	   return users;
	}
	
	public Optional<User> findByEmailAndActive(String email) {
		return Optional.ofNullable(getUserByEmailAndActive(email));//this.us.findByEmail(username);
	}
	
	public User getUserByEmailAndActive(String email) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/userAPI/emailActive/"+email;
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
		   return gson.fromJson(result.getBody(), User.class);
	   }
	   return null;
	}
	public User getUserByNif(String nif) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/userAPI/"+nif;
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
		   return gson.fromJson(result.getBody(), User.class);
	   }
	   return null;
	}
	public User createUser(User user) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/userAPI";
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new User();
		}
	     
	    ResponseEntity<String> result = restTemplate.postForEntity(uri, user, String.class);
	     
	    //Verify request succeed
	   if(result.getStatusCodeValue() == 201) {
		   return gson.fromJson(result.getBody(), User.class);
	   }
	   return new User();
	}
	public User approve(String id, User user) {
		Optional<User> u = Optional.ofNullable(getUserByNif(id));
		if(u.isEmpty()) return null;
		u.get().setQuantity(user.getQuantity());
		u.get().setStatus(User.Status.A);;
		return createUser(u.get());
	}
	
	public User update(String id, User user) {
		Optional<User> u = Optional.ofNullable(getUserByNif(id));
		if(u.isEmpty()) return null;
		u.get().setQuantity(user.getQuantity());
		if(user.getStatus() != null)u.get().setStatus(user.getStatus());
		if(user.getEmail() != null)u.get().setEmail(user.getEmail());
		if(user.getName() != null)u.get().setName(user.getName());
		if(user.getPassword() != null)u.get().setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		if(user.getPersonType() != null)u.get().setPersonType(user.getPersonType());

		return createUser(u.get());
	}
	
	public void delete(String id) {
		RestTemplate restTemplate = new RestTemplate();
	     
	    final String baseUrl = "http://"+usersURL+"/userAPI/"+id;
	    URI uri;
		try {
			uri = new URI(baseUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	    restTemplate.delete(uri);

	}
}
