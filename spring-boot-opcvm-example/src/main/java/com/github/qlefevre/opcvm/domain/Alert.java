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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Range;

import com.github.qlefevre.opcvm.util.AlertType;
import com.github.qlefevre.opcvm.util.TrendType;
import com.github.qlefevre.opcvm.validation.constraints.Isin;

/*
Validité de l'alerte

Semaine
Mois


Le cours franchit un seuil à la hausse ou à la baisse
seuil
à la hausse 
à la baisse

Variation du cours par rapport à la veille
à la veille
en %

Le cours sort du tunnel et atteint une valeur comprise entre deux bornes
seuil bas
seuil haut

Le cours croise sa moyenne mobile pendant X jours à la hausse ou à la baisse
20
à la hausse
à la baisse
=> Bollinger


Le cours franchit son plus haut de l'année
Le cours franchit son plus bas de l'année

Le cours franchit son plus haut sur 52 semaines
Le cours franchit son plus bas sur 52 


*/
@Entity
public class Alert implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column
	@Email
	private String email;

	@Column
	@Isin
	private String isin;
	
	@Column
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;
	
	@Column
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date modificationDate;
	
	@Column
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;
	
	@Column
	@NotNull
	@Enumerated(EnumType.STRING)
	private AlertType type;
	
	@Column
	@Range(min=1,max=62)
	private int validityDuration;

	@Column(scale=2,name = "changevalue")
	@NotNull
	private double change; 
	
	@Column(scale=2)
	@NotNull
	private double changeAmount;
	
	@Column
	@NotNull
	@Enumerated(EnumType.STRING)
	private TrendType trend;

	@Column(scale=2)
	private double value;
	
	@Column(scale=2)
	private double minValue;
	
	@Column(scale=2)
	private double maxValue;
	
	@AssertTrue
	public boolean isValid(){
		return true;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public int getValidityDuration() {
		return validityDuration;
	}

	public void setValidityDuration(int validityDuration) {
		this.validityDuration = validityDuration;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}



	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	public double getChangeAmount() {
		return changeAmount;
	}

	public void setChangeAmount(double changeAmount) {
		this.changeAmount = changeAmount;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public TrendType getTrend() {
		return trend;
	}

	public void setTrend(TrendType trend) {
		this.trend = trend;
	}
	
	public boolean isTrendUp(){
		return TrendType.UP.equals(getTrend());
	}
	
}
