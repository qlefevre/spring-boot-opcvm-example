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

public class IsinImpl implements ConstraintValidator<Isin, String> {

	private static final Pattern ISIN_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{10}$");

	public boolean isValid(String isin, ConstraintValidatorContext context) {
		return isin != null && ISIN_PATTERN.matcher(isin).matches();
	}

	@Override
	public void initialize(Isin constraintAnnotation) {
	}
}