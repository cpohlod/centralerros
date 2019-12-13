package com.central.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.central.bo.Log;
import com.central.bo.Login;
import com.central.error.CentralNotFoundException;
import com.central.helper.LoginHelper;
import com.central.repo.LogRepository;
import com.central.repo.LoginRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class CentralController {

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_TYPE = "Content-Type";
	
    @Autowired
    private LoginRepository loginRepo;

    @Autowired
    private LogRepository logRepo;
    
    protected static ConcurrentHashMap<String, String> securityParams = new ConcurrentHashMap<>();
	
    @RequestMapping(value = "login", method = RequestMethod.POST)	
	@CrossOrigin(maxAge = 3600)
	@ResponseBody
	public void login(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonSecurity) {
		System.out.println("login");
		try {
			Login loginWeb = Login.jsonToLogin(jsonSecurity);
			Login loginBase = loginRepo.findByEmail(loginWeb.getEmail());
	    	if (loginBase == null) {
		    	geraLog("ERRO", "login", loginWeb.getEmail(), "Email incorreto");
	    		throw new CentralNotFoundException("Login incorreto");
	    	} 
	    	if (!loginWeb.getPwd().equals(loginBase.getPwd())) {
		    	geraLog("ERRO", "login", loginBase.getEmail(), "Senha incorreta");
		    	throw new CentralNotFoundException("Senha incorreta");
	    	}
			
			LoginHelper service = new LoginHelper(3);
			String token = service.sifra(loginWeb.getEmail());
			securityParams.put(token, jsonSecurity);
	    	String detail = "login efetuado pelo "+loginWeb.getName();
	    	geraLog("INFO", "login", loginWeb.getName(), detail);
			writeResponse(response, token);
		} catch (Exception e) {
	    	geraLog("ERRO", "login", "Sistema", "Erro no processamento do login "+jsonSecurity);
		}
	}
    
    // FindAll logs
    @GetMapping("/buscalogs/{token}")
    @CrossOrigin(maxAge = 3600)
    public List<Log> findAllLogs(@PathVariable String token) {
    	String orign = processaLogin(token);
    	String detail = "busca de todos os logs da aplicação pelo token "+token;
    	geraLog("INFO", "findAllLogs", orign.equals("")? token : orign, detail);

        return new ArrayList<Log>((Collection<? extends Log>) logRepo.findAll());
    }

	private void geraLog(String type, String title, String orign, String detail) {
    	Long quantity = new Long(1);
    	Calendar cal = Calendar.getInstance();
    	java.sql.Date dataSql = new java.sql.Date(cal.getTime().getTime());
    	Log log = new Log(title, type, orign, detail, quantity, dataSql);
        logRepo.save(log);
	}

    // Arquiva log by ID
    @GetMapping("/arquivalog/{id}/{token}")
    @CrossOrigin(maxAge = 3600)
    public void arquivalog(@PathVariable String token, @PathVariable Long id) {
    	String orign = processaLogin(token);
    	String detail = "arquiva log pelo id "+id;
    	geraLog("INFO", "arquivalog", orign.equals("")? token : orign, detail);

    	Optional<Log> logAux = logRepo.findById(id);
    	Log log = logAux.get();
    	log.setSituacao("A");
    	logRepo.save(log);
        
    }

    // Arquiva logs by IDs
    @RequestMapping(value = "arquivalogs/{token}", method = RequestMethod.POST)	
	@CrossOrigin(maxAge = 3600)
	@ResponseBody
    public void arquivalogs(@PathVariable String token, @RequestBody String jsonIds) {
    	String orign = processaLogin(token);
    	String detail = "arquivas log ids "+jsonIds;
    	geraLog("INFO", "arquivalogs", orign.equals("")? token : orign, detail);
    	System.out.println(jsonIds);
    	ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> map = mapper.readValue(jsonIds, Map.class);
			List<Integer> arr = (java.util.ArrayList)map.get("ids");
			for (Integer id : arr) {
		    	Optional<Log> logAux = logRepo.findById(new Long(id));
		    	Log log = logAux.get();
		    	log.setSituacao("A");
		    	logRepo.save(log);
			}
		} catch (Exception e) {
	    	geraLog("ERRO", "arquivalogs", orign.equals("")? token : orign, detail);
		}
    }

    // Delete log by ID
    @GetMapping("/deletalog/{id}/{token}")
    @CrossOrigin(maxAge = 3600)
    public void deletalog(@PathVariable String token, @PathVariable Long id) {
    	String orign = processaLogin(token);
    	String detail = "exclui log pelo id "+id;
    	geraLog("INFO", "deletelog", orign.equals("")? token : orign, detail);

    	logRepo.deleteById(id);
    }

    // Delete logs by IDs
    @RequestMapping(value = "deletalogs/{token}", method = RequestMethod.POST)	
	@CrossOrigin(maxAge = 3600)
	@ResponseBody
    public void deletalogs(@PathVariable String token, @RequestBody String jsonIds) {
    	String orign = processaLogin(token);
    	String detail = "exclui logs ids "+jsonIds;
    	geraLog("INFO", "deletalogs", orign.equals("")? token : orign, detail);
    	System.out.println(jsonIds);
    	ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> map = mapper.readValue(jsonIds, Map.class);
			List<Integer> arr = (java.util.ArrayList)map.get("ids");
			for (Integer id : arr) {
				logRepo.deleteById(new Long(id));
			}
		} catch (Exception e) {
	    	geraLog("ERRO", "arquivalogs", orign.equals("")? token : orign, detail);
		}
    }

    // Find by ID
    @GetMapping("/buscalog/{id}/{token}")
    @CrossOrigin(maxAge = 3600)
    public Log findlog(@PathVariable String token, @PathVariable Long id) {
    	String orign = processaLogin(token);
    	String detail = "busca pelo detalhe do log com id "+id;
    	geraLog("INFO", "findlog", orign.equals("")? token : orign, detail);

    	Optional<Log> log = logRepo.findById(id);
        return log.get();
    }

    // FindAll logins
    @GetMapping("/logins/{token}")
    @CrossOrigin(maxAge = 3600)
    public List<Login> findAllLogins(@PathVariable String token) {
		String orign = processaLogin(token);
    	String detail = "busca por todos os logins token "+token;
    	geraLog("INFO", "findAllLogins", orign.equals("")? token : orign, detail);

    	return new ArrayList<Login>((Collection<? extends Login>) loginRepo.findAll());
    }
    
    // Find
    @GetMapping("/buscalogin/{token}{email}")
    @CrossOrigin(maxAge = 3600)
    public Login findlogin(@PathVariable String token, @PathVariable String email) {
		String orign = processaLogin(token);
    	String detail = "busca pelas informações do login de token "+token;
    	geraLog("INFO", "findlogin", orign.equals("")? token : orign, detail);
    	
    	Login login = loginRepo.findByEmail(email);
    	if (login == null) {
    		throw new CentralNotFoundException("Login não encontrado");
    	} 
        return login;
    }

    @RequestMapping(value = "savelogin", method = RequestMethod.POST)	
	@CrossOrigin(maxAge = 3600)
	@ResponseBody
	public Login savelogin(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonSecurity) {
		System.out.println("savelogin");
		try {
	    	Login saveLogin = Login.jsonToLogin(jsonSecurity);
	    	Login login = loginRepo.findByEmail(saveLogin.getEmail());
	    	if (login==null) {
	        	String detail = "salva informações do novo usuário "+saveLogin.getName();
	        	geraLog("INFO", "savelogin", saveLogin.getName(), detail);

	        	return loginRepo.save(saveLogin);
	    	} else {
	        	String detail = "salva informações do usuário existente "+saveLogin.getName();
	        	geraLog("INFO", "savelogin", saveLogin.getName(), detail);
	    		return loginRepo.findById(login.getId())
	                .map(x -> {
	                    x.setName(saveLogin.getName());
	                    x.setEmail(saveLogin.getEmail());
	                    x.setPwd(saveLogin.getPwd());
	                    return loginRepo.save(x);
	                })
	                .orElseGet(() -> {
	                    login.setId(login.getId());
	                    return loginRepo.save(saveLogin);
	                });
	    	}
		} catch (IOException e) {
			throw new CentralNotFoundException("Informações de Login inválido");
		}		
    }

    @GetMapping("/deletalogin/{token}/{email}")
    @CrossOrigin(maxAge = 3600)
    public void deleteLogin(@PathVariable String token, @PathVariable String email) {
    	processaLogin(token);
    	Login login = loginRepo.findByEmail(email);
    	if (login == null) {
    		throw new CentralNotFoundException("Login não encontrado");
    	} 
    	String detail = "exclui usuário "+login.getName();
    	geraLog("INFO", "deleteLogin", login.getName(), detail);
        loginRepo.deleteById(login.getId());
    }
    
    
	private String processaLogin(String token) {
		return "ok";
//		String jsonSecurity = securityParams.get(token);
//		if (jsonSecurity==null) {
//	    	String detail = "Usuário não logado, favor efetuar o login na central de erros";
//	    	geraLog("ERROR", "processaLogin", token, detail);
//			throw new CentralNotFoundException(detail);
//		}
//		try {
//			Login login = Login.jsonToLogin(jsonSecurity);
//			return login.getName();
//		} catch (IOException e) {
//			throw new CentralNotFoundException("Informações de Login inválido");
//		}
	}

    private void writeResponse(HttpServletResponse response, String html) throws IOException {
		response.getOutputStream().write(html.getBytes(Charset.forName("UTF-8")));
		response.setHeader(CONTENT_TYPE, "text/html");
		response.setHeader(CONTENT_DISPOSITION, "inline");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.flushBuffer();
	}
    
}
