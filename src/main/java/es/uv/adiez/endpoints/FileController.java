package es.uv.adiez.endpoints;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.uv.adiez.domain.File;
import es.uv.adiez.domain.User;
import es.uv.adiez.domain.Views;
import es.uv.adiez.services.FileService;
import es.uv.adiez.services.UserService;

@RestController
@RequestMapping("/api/files")
public class FileController {
	@Autowired FileService fs;
	@Autowired UserService us;
	
	@GetMapping("/{status}")
	@JsonView(Views.MyResponseViews.class)
	public List<File> getPending(@PathVariable("status") File.Status status) {
		
		return fs.getPending(status);
	}
	
	@GetMapping("/prepare/{id}")
	public ResponseEntity prepareFile(@PathVariable("id") String id) {
		
		fs.prepareFile(id);
		return new ResponseEntity<>("Preparing file", HttpStatus.OK);
	}
	
}
