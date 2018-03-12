/*******************************************************************************
 * Copyright 2018  Quentin Lefèvre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package com.github.qlefevre.opcvm.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author quentin
 *
 */
@ControllerAdvice
public class ErrorController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);

	
	@ExceptionHandler(Exception.class)
    public String handleError(Model model,HttpServletRequest request, Exception e)   {
		LOGGER.error("Request: " + request.getRequestURL() + " raised " + e,e);
        return "error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleError404(Model model,HttpServletRequest request, Exception e)   {
    	LOGGER.error("Request: " + request.getRequestURL() + " raised " + e,e);
        return "404";
    }
	
}
