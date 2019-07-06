package com.jds.epathshala.nbirest.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jds.epathshala.nbirest.rest.payload.ApiResponse;
import com.jds.epathshala.nbirest.rest.payload.JwtAuthenticationResponse;
import com.jds.epathshala.nbirest.rest.payload.SignInRequest;
import com.jds.epathshala.nbirest.rest.payload.SignUpRequest;
import com.jds.epathshala.nbirest.security.JwtTokenProvider;
import com.jds.epathshala.nbirest.service.UserService;
import com.jds.epathshala.nbirest.service.impl.UserInputValidator;

@RestController
public class AuthenticationController {

	@Autowired
	UserService userService;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	UserInputValidator userInputValidator;

	@Autowired
	AuthenticationManager authenticationManager;

	@RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public ResponseEntity<ApiResponse> register(@Valid @RequestBody SignUpRequest singUpRequest) {

		if (userInputValidator.validateMobileNo(singUpRequest.getMobileNo())) {
			return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Phone number is already taken!"),
					HttpStatus.BAD_REQUEST);
		}

		if (singUpRequest.getEmail() != null && userInputValidator.validateEmailId(singUpRequest.getEmail())) {
			return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Email Address already in use!"),
					HttpStatus.BAD_REQUEST);
		}
		if(userInputValidator.validateProfileType(singUpRequest.getProfileType()))
		{
			return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Profile type is not valid!"),
					HttpStatus.BAD_REQUEST);
		}
		if (userService.register(singUpRequest)) {
			return new ResponseEntity<ApiResponse>(new ApiResponse(true, "User registered successfully"),
					HttpStatus.OK);
		}
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Some internal error is occured"),
				HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@RequestMapping(value = "/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = tokenProvider.generateToken(authentication);
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}
}
