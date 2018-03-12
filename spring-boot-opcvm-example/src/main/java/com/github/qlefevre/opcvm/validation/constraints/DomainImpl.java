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
package com.github.qlefevre.opcvm.validation.constraints;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DomainImpl implements ConstraintValidator<Domain, String> {

	private static final Pattern DOMAIN_PATTERN = Pattern.compile("^\\w{1,250}\\.[a-zA-Z]{2,3}$");

	public boolean isValid(String domain, ConstraintValidatorContext context) {
		return domain != null && DOMAIN_PATTERN.matcher(domain).matches();
	}

	@Override
	public void initialize(Domain constraintAnnotation) {
	}
}