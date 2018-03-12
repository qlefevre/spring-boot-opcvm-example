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
package com.github.qlefevre.opcvm.util;

/**
 * @author quentin
 *
 */
public enum AlertType {
	// Le cours franchit un seuil à la hausse ou à la baisse
	TRIGGER_ALERT, 
	// Variation du cours par rapport à la veille
	CHANGE_ALERT,
	// Le cours sort du tunnel et atteint une valeur comprise entre deux bornes
	TUNNEL_ALERT,
	// Le cours franchit son plus haut de l'année
	HIGHEST_YEAR, 
	// Le cours franchit son plus bas de l'année
	LOWEST_YEAR,
	// Le cours franchit son plus haut sur 52 semaines
	HIGHEST_52WK, 
	// Le cours franchit son plus bas sur 52 
	LOWEST_52WK
}
