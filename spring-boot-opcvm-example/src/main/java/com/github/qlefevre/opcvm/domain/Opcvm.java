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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.github.qlefevre.opcvm.validation.constraints.Isin;

/**
 * @author quentin
 *
 */
@Entity
@Table(indexes = {@Index(name = "isin_idx",  columnList="isin", unique = true)})
public class Opcvm implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Isin
	private String isin;

	@Column(scale=2)
	@NotNull
	private double quote;

	@Column(scale=2,name = "changevalue")
	@NotNull
	private double change;

	@Column
	@NotBlank
	private String name;

	@Column
	private boolean pea;

	@Column
	private boolean peapme;

	public Long getId() {
		return id;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public double getQuote() {
		return quote;
	}

	public void setQuote(double quote) {
		this.quote = quote;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPea() {
		return pea;
	}

	public void setPea(boolean pea) {
		this.pea = pea;
	}

	public boolean isPeapme() {
		return peapme;
	}

	public void setPeapme(boolean peapme) {
		this.peapme = peapme;
	}

	@Override
	public String toString() {
		return "Opcvm [id=" + id + ", isin=" + isin + ", quote=" + quote + ", change=" + change + ", name=" + name
				+ ", pea=" + pea + ", peapme=" + peapme + "]";
	}

}
