package es.uv.adiez.endpoints;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uv.adiez.domain.File;
import es.uv.adiez.domain.User;
import es.uv.adiez.services.UserService;

@RestController
public class UserController {
	@Autowired UserService us;
	
	@GetMapping("/api/producers")
	public List<User> getProducers(@RequestParam(value="type", defaultValue = "0") String type) {
		return us.findProducers(type).stream().map(u -> { 
			u.setPassword(null); 
			return u;
	      }).collect(Collectors.toList());
	}
	@PutMapping("/api/producers/{id}")
	public ResponseEntity<User> updateProducer(@PathVariable("id") String id, @RequestBody @Valid User user) {
		User u = us.update(id, user);
		return new ResponseEntity<>(u, HttpStatus.OK);
	}
	
	@PutMapping("/api/producers/approve/{id}")
	public ResponseEntity<User> approveProducer(@PathVariable("id") String id, @RequestBody @Valid User user) {
		User u = us.approve(id, user);
		return new ResponseEntity<>(u, HttpStatus.OK);
	}
	
	@DeleteMapping("/api/producers/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") String id) {
		this.us.delete(id);
		return new ResponseEntity<>(id, HttpStatus.OK);
	}
}
