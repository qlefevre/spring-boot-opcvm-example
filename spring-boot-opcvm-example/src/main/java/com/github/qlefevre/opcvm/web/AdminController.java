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
package com.github.qlefevre.opcvm.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.qlefevre.opcvm.scheduling.CheckOpcvm;

@Controller
public class AdminController {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CheckOpcvm checkOpcvm;

	@RequestMapping("/admin/password/{password}")
	@ResponseBody
	public String password(@PathVariable String password) {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		String vEncodedPassword = encoder.encode(password);
		return vEncodedPassword;
    }

    @RequestMapping("/admin/checkopcvm")
    @ResponseBody
    public String checkOpcvms() {
        checkOpcvm.checkOpcvms();
        return "OK";
    }

	

}
