package com.fw.listenup.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fw.listenup.models.auth.RegistrationLookupDetail;
import com.fw.listenup.models.auth.UserAuthenticationDetail;
import com.fw.listenup.services.AuthService;

import ch.qos.logback.classic.Logger;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    //Controller method, serves as API entry point for retrieiving user auth details from db
    @GetMapping("/user/{email}")
    public ResponseEntity<UserAuthenticationDetail> getUserAuthenticationDetails(@PathVariable String email){
        logger.info("Login request made for user " + email);
        UserAuthenticationDetail uad = this.authService.getUserAuthenticationDetail(email);
        if(uad == null){
            logger.error("Login request failed for user " + email);
            return ResponseEntity.ok(new UserAuthenticationDetail("", ""));
        } 
        
        // logger.info("NOT NULL");
        return ResponseEntity.ok(uad);
    }

    //Takes submitted registration form arguments and checks that username/email are not taken
    @PostMapping("/register/isTaken")
    public ResponseEntity<?> lookupExistingUser(@RequestParam String email, @RequestParam String username){
        logger.info("Registration lookup request made for user " + email + " : " + username);
        try{
            RegistrationLookupDetail rld = this.authService.lookupExistingUser(email, username);
            if(rld == null){
                logger.error("Failed to retrieve registration lookup detail object for user " + email + " : " + username);
                throw new RuntimeException("Internal Server Error");
            }
            return ResponseEntity.ok(rld);

        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error: " + e.toString());
        }
        
        
    }

    //Registers new user
    @PostMapping("/register")
    public Map<String, Boolean> registerNewUser(@RequestParam String email, @RequestParam String username, @RequestParam String pw){
        logger.info("Registration request made for new user " + email);
        //Map result that returns whether user was properly registered
        Map<String, Boolean> res = new HashMap<String, Boolean>();
        String resKey = "isRegistered";
        try{
            boolean isRegistered = this.authService.registerNewUser(email, username, pw);
            if(!isRegistered) logger.error("Registration process failed for new user " + email);
            res.put(resKey, isRegistered);
        } catch(Exception e){
            logger.error("Registration request server error: " + e.toString());
            res.put(resKey, false);
        }

        return res;
    }

    
}