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
package com.github.qlefevre.opcvm.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import com.github.qlefevre.opcvm.validation.constraints.Domain;

@Entity
@Table(indexes = {@Index(name = "domain_idx",  columnList="domain", unique = true)})
public class QuoteExtractor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Domain
	private String domain;

	@Column
	@NotBlank
	private String isinRegex;

	@Column
	@Min(1)
	private int isinGroup;

	@Column
	@NotBlank
	private String nameRegex;

	@Column
	@Min(1)
	private int nameGroup;

	@Column
	@NotBlank
	private String quoteRegex;

	@Column
	@Min(1)
	private int quoteGroup;

	@Column
	@NotBlank
	private String changeRegex;

	@Column
	@Min(1)
	private int changeGroup;

	public QuoteExtractor() {
	}

	public Long getId() {
		return id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getIsinRegex() {
		return isinRegex;
	}

	public void setIsinRegex(String isinRegex) {
		this.isinRegex = isinRegex;
	}

	public int getIsinGroup() {
		return isinGroup;
	}

	public void setIsinGroup(int isinGroup) {
		this.isinGroup = isinGroup;
	}

	public String getNameRegex() {
		return nameRegex;
	}

	public void setNameRegex(String nameRegex) {
		this.nameRegex = nameRegex;
	}

	public int getNameGroup() {
		return nameGroup;
	}

	public void setNameGroup(int nameGroup) {
		this.nameGroup = nameGroup;
	}

	public String getQuoteRegex() {
		return quoteRegex;
	}

	public void setQuoteRegex(String quoteRegex) {
		this.quoteRegex = quoteRegex;
	}

	public int getQuoteGroup() {
		return quoteGroup;
	}

	public void setQuoteGroup(int quoteGroup) {
		this.quoteGroup = quoteGroup;
	}

	public String getChangeRegex() {
		return changeRegex;
	}

	public void setChangeRegex(String changeRegex) {
		this.changeRegex = changeRegex;
	}

	public int getChangeGroup() {
		return changeGroup;
	}

	public void setChangeGroup(int changeGroup) {
		this.changeGroup = changeGroup;
	}

	@Override
	public String toString() {
		return "QuoteExtractor [id=" + id + ", domain=" + domain + ", isinRegex=" + isinRegex + ", isinGroup="
				+ isinGroup + ", nameRegex=" + nameRegex + ", nameGroup=" + nameGroup + ", quoteRegex=" + quoteRegex
				+ ", quoteGroup=" + quoteGroup + ", changeRegex=" + changeRegex + ", changeGroup=" + changeGroup + "]";
	}


}